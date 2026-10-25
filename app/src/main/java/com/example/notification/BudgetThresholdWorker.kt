package com.example.notification

import android.content.Context
import androidx.work.*
import com.example.data.db.BrickDatabase
import com.example.ui.viewmodel.BudgetCategoryType
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that monitors 50/30/20 budget category consumption
 * and triggers high-priority local notifications when any category
 * (Besoins 50%, Envies 30%, Épargne 20%) reaches or exceeds 80% of its allocated limit.
 */
class BudgetThresholdWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val db = BrickDatabase.getInstance(applicationContext)

        val user = db.userDao().getUserProfileDirect() ?: return Result.success()
        val budgetSettings = db.budgetSettingsDao().getBudgetSettingsDirect()

        // Calculate current month time boundaries
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)

        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startTime = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endTime = cal.timeInMillis

        val monthTransactions = db.transactionDao().getTransactionsBetweenDirect(startTime, endTime)
        val monthIncomes = monthTransactions.filter { it.type == "REVENU" }.sumOf { it.amount }

        val effectiveIncome = if ((budgetSettings?.monthlyIncomeBase ?: 0.0) > 0.0) {
            budgetSettings!!.monthlyIncomeBase
        } else if (user.monthlySalary > 0.0) {
            user.monthlySalary
        } else {
            monthIncomes
        }

        if (effectiveIncome <= 0.0) {
            return Result.success()
        }

        // Percentage allocations
        val needsPct = budgetSettings?.needsPercentage ?: user.needsBudgetPercentage
        val wantsPct = budgetSettings?.wantsPercentage ?: user.wantsBudgetPercentage
        val savingsPct = budgetSettings?.savingsPercentage ?: user.savingsBudgetPercentage

        val needsTarget = effectiveIncome * (needsPct / 100.0)
        val wantsTarget = effectiveIncome * (wantsPct / 100.0)
        val savingsTarget = effectiveIncome * (savingsPct / 100.0)

        // Categorize expenses
        var needsSpent = 0.0
        var wantsSpent = 0.0
        var savingsSpent = 0.0

        val expenses = monthTransactions.filter { it.type == "DEPENSE" }
        for (tx in expenses) {
            when (tx.priority) {
                "NEEDS" -> needsSpent += tx.amount
                "WANTS" -> wantsSpent += tx.amount
                "SAVINGS" -> savingsSpent += tx.amount
                else -> {
                    when (tx.category.uppercase()) {
                        "LOGEMENT", "LOYER", "NOURRITURE", "ALIMENTATION", "SANTE", "TRANSPORT", "FACTURES", "ELECTRICITE", "EAU", "EDUCATION" ->
                            needsSpent += tx.amount
                        "SORTIES", "RESTAURANT", "SHOPPING", "DIVERTISSEMENT", "LOISIRS", "VOYAGES", "BEAUTE" ->
                            wantsSpent += tx.amount
                        else -> savingsSpent += tx.amount
                    }
                }
            }
        }

        val prefs = applicationContext.getSharedPreferences("brick_budget_worker_prefs", Context.MODE_PRIVATE)
        val force = inputData.getBoolean(KEY_FORCE_CHECK, false)
        val now = System.currentTimeMillis()
        val coolDownMs = 12 * 60 * 60 * 1000L // 12 hours anti-spam cooldown per threshold level

        val buckets = listOf(
            Triple(BudgetCategoryType.NEEDS, needsSpent, needsTarget),
            Triple(BudgetCategoryType.WANTS, wantsSpent, wantsTarget),
            Triple(BudgetCategoryType.SAVINGS, savingsSpent, savingsTarget)
        )

        val threshold = (budgetSettings?.alertThresholdPercentage ?: 80) / 100.0

        for ((category, spent, target) in buckets) {
            if (target > 0.0) {
                val ratio = spent / target
                if (ratio >= threshold) {
                    val thresholdTag = if (ratio >= 1.0) "EXCEEDED" else "WARNING_80"
                    val prefKey = "alert_sent_${category.name}_${thresholdTag}_${month}_${year}"
                    val lastSent = prefs.getLong(prefKey, 0L)

                    if (force || (now - lastSent > coolDownMs)) {
                        BrickNotificationHelper.sendBudgetThresholdAlert(
                            context = applicationContext,
                            categoryType = category,
                            ratio = ratio,
                            spent = spent,
                            target = target,
                            currency = user.currency
                        )
                        prefs.edit().putLong(prefKey, now).apply()
                    }
                }
            }
        }

        return Result.success()
    }

    companion object {
        const val KEY_FORCE_CHECK = "FORCE_CHECK"
        const val WORK_NAME_PERIODIC = "brick_budget_threshold_periodic"
        const val WORK_NAME_IMMEDIATE = "brick_budget_threshold_immediate"

        /**
         * Enqueues an immediate one-time WorkManager check.
         * Triggered when a new expense is logged or when the user tests the budget alert in UI.
         */
        fun enqueueCheck(context: Context, force: Boolean = false) {
            val input = workDataOf(KEY_FORCE_CHECK to force)
            val request = OneTimeWorkRequestBuilder<BudgetThresholdWorker>()
                .setInputData(input)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_IMMEDIATE,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        /**
         * Schedules periodic background monitoring using WorkManager.
         * Runs every 6 hours to alert the user even when the app hasn't been opened.
         */
        fun schedulePeriodicCheck(context: Context) {
            val periodicRequest = PeriodicWorkRequestBuilder<BudgetThresholdWorker>(
                6, TimeUnit.HOURS,
                30, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }
    }
}
