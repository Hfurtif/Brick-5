package com.example.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.db.BrickDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.max

class DailyEveningRecapWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val db = BrickDatabase.getInstance(appContext)
        val user = db.userDao().getUserProfileDirect() ?: return Result.success()

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis

        val todayTxs = db.transactionDao().getTransactionsSinceDirect(startOfToday)
        val todayTotal = todayTxs.sumOf { it.amount }

        // Local daily budget estimate
        val remainingDays = max(1, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH) - Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        val dailyBudget = (user.monthlySalary - user.fixedCharges) / max(1, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH))

        val shortSummary = when {
            todayTotal == 0.0 -> "Journée zéro dépense ! Aucune fuite détectée aujourd'hui."
            todayTotal > dailyBudget -> "Aujourd'hui a dépassé ton quota journalier (${dailyBudget.toInt()} ${user.currency}). Freine les envies demain !"
            todayTotal <= dailyBudget * 0.7 -> "Très bonne gestion aujourd'hui ! Tu as dépensé moins que prévu."
            else -> "Budget journalier respecté (${dailyBudget.toInt()} ${user.currency}). Continue à tenir tes comptes !"
        }

        BrickNotificationHelper.sendEveningNotificationWithSummary(
            context = appContext,
            todaySpent = todayTotal,
            currency = user.currency,
            summary = shortSummary
        )

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "brick_daily_evening_recap_work"

        fun schedule(context: Context, hour: Int, minute: Int) {
            try {
                val now = Calendar.getInstance()
                val target = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (target.before(now)) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                val initialDelayMillis = target.timeInMillis - now.timeInMillis

                val workRequest = PeriodicWorkRequestBuilder<DailyEveningRecapWorker>(24, TimeUnit.HOURS)
                    .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
            } catch (e: Exception) {
                android.util.Log.w("DailyEveningRecapWorker", "Failed to schedule worker: ${e.message}")
            }
        }

        fun runImmediateTest(context: Context) {
            val testWorkRequest = androidx.work.OneTimeWorkRequestBuilder<DailyEveningRecapWorker>()
                .build()
            WorkManager.getInstance(context).enqueue(testWorkRequest)
        }
    }
}
