package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.BrickDatabase
import com.example.data.model.TransactionEntity
import com.example.data.repository.BrickRepository
import com.example.notification.BrickNotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class Budget503020ViewModel(
    application: Application,
    injectedRepository: BrickRepository?
) : AndroidViewModel(application) {

    constructor(application: Application) : this(application, null)

    private val repository: BrickRepository = injectedRepository ?: run {
        val db = BrickDatabase.getInstance(application)
        BrickRepository(
            userDao = db.userDao(),
            transactionDao = db.transactionDao(),
            goalDao = db.goalDao(),
            assetDao = db.assetDao(),
            categoryDao = db.categoryDao(),
            budgetSettingsDao = db.budgetSettingsDao()
        )
    }

    private val cal = Calendar.getInstance()
    private val _customIncomeOverride = MutableStateFlow<Double?>(null)
    val customIncomeOverride: StateFlow<Double?> = _customIncomeOverride.asStateFlow()

    private val _selectedMonth = MutableStateFlow(cal.get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(cal.get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _dismissedAlertIds = MutableStateFlow<Set<String>>(emptySet())
    val dismissedAlertIds: StateFlow<Set<String>> = _dismissedAlertIds.asStateFlow()

    private val _alertNotificationEvent = MutableSharedFlow<BudgetAlert>(extraBufferCapacity = 10)
    val alertNotificationEvent: SharedFlow<BudgetAlert> = _alertNotificationEvent.asSharedFlow()

    private val previousExceededAlertIds = mutableSetOf<String>()

    val uiState: StateFlow<Budget503020UiState> = combine(
        repository.userProfile,
        repository.allTransactions,
        _customIncomeOverride,
        combine(_selectedMonth, _selectedYear, _dismissedAlertIds) { m, y, d -> Triple(m, y, d) }
    ) { profile, transactions, customIncome, (month, year, dismissed) ->
        val currency = profile?.currency ?: "FCFA"

        // Calculate income: either custom override, user profile monthlySalary, or total REVENU in this month
        val calStart = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calEnd = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val startTime = calStart.timeInMillis
        val endTime = calEnd.timeInMillis

        val monthTransactions = transactions.filter { it.timestamp in startTime..endTime }
        val monthIncomes = monthTransactions.filter { it.type == "REVENU" }.sumOf { it.amount }

        val effectiveIncome = customIncome
            ?: if ((profile?.monthlySalary ?: 0.0) > 0) profile!!.monthlySalary
            else maxOf(monthIncomes, 0.0)

        // Target allocations
        val needsPct = profile?.needsBudgetPercentage ?: 50
        val wantsPct = profile?.wantsBudgetPercentage ?: 30
        val savingsPct = profile?.savingsBudgetPercentage ?: 20

        val needsTarget = effectiveIncome * (needsPct / 100.0)
        val wantsTarget = effectiveIncome * (wantsPct / 100.0)
        val savingsTarget = effectiveIncome * (savingsPct / 100.0)

        // Expense mapping
        val expenses = monthTransactions.filter { it.type == "DEPENSE" }
        var needsSpent = 0.0
        var wantsSpent = 0.0
        var savingsSpent = 0.0

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

        val needsBreakdown = BudgetBucketBreakdown(
            category = BudgetCategoryType.NEEDS,
            percentage = needsPct,
            targetAmount = needsTarget,
            spentAmount = needsSpent,
            remainingAmount = needsTarget - needsSpent,
            ratio = if (needsTarget > 0) needsSpent / needsTarget else 0.0,
            isExceeded = needsTarget > 0 && needsSpent > needsTarget
        )

        val wantsBreakdown = BudgetBucketBreakdown(
            category = BudgetCategoryType.WANTS,
            percentage = wantsPct,
            targetAmount = wantsTarget,
            spentAmount = wantsSpent,
            remainingAmount = wantsTarget - wantsSpent,
            ratio = if (wantsTarget > 0) wantsSpent / wantsTarget else 0.0,
            isExceeded = wantsTarget > 0 && wantsSpent > wantsTarget
        )

        val savingsBreakdown = BudgetBucketBreakdown(
            category = BudgetCategoryType.SAVINGS,
            percentage = savingsPct,
            targetAmount = savingsTarget,
            spentAmount = savingsSpent,
            remainingAmount = savingsTarget - savingsSpent,
            ratio = if (savingsTarget > 0) savingsSpent / savingsTarget else 0.0,
            isExceeded = savingsTarget > 0 && savingsSpent > savingsTarget
        )

        val totalSpent = needsSpent + wantsSpent + savingsSpent
        val totalRemaining = effectiveIncome - totalSpent
        val globalRatio = if (effectiveIncome > 0) totalSpent / effectiveIncome else 0.0

        val health = when {
            needsBreakdown.isExceeded || wantsBreakdown.isExceeded || globalRatio > 1.0 -> BudgetHealth.CRITICAL
            needsBreakdown.ratio > 0.85 || wantsBreakdown.ratio > 0.85 || globalRatio > 0.85 -> BudgetHealth.WARNING
            else -> BudgetHealth.HEALTHY
        }

        // Build alerts
        val generatedAlerts = mutableListOf<BudgetAlert>()

        listOf(needsBreakdown, wantsBreakdown, savingsBreakdown).forEach { bucket ->
            if (bucket.targetAmount > 0) {
                if (bucket.isExceeded) {
                    val alert = BudgetAlert(
                        id = "${bucket.category.name}_EXCEEDED_${month}_$year",
                        category = bucket.category,
                        severity = BudgetAlertSeverity.EXCEEDED,
                        targetAmount = bucket.targetAmount,
                        spentAmount = bucket.spentAmount,
                        overAmount = bucket.spentAmount - bucket.targetAmount,
                        spentPercentageOfTarget = bucket.ratio * 100.0,
                        currency = currency,
                        title = "Plafond ${bucket.category.label} dépassé !",
                        message = "Vous avez dépensé ${String.format("%,.0f", bucket.spentAmount)} $currency pour un plafond de ${String.format("%,.0f", bucket.targetAmount)} $currency (+${String.format("%,.0f", bucket.spentAmount - bucket.targetAmount)} $currency d'excédent).",
                        recommendation = when (bucket.category) {
                            BudgetCategoryType.NEEDS -> "Attention aux charges imprévues. Vérifiez vos factures ou compensez sur vos envies."
                            BudgetCategoryType.WANTS -> "Gelez temporairement vos sorties et dépenses plaisir pour préserver votre reste à vivre."
                            BudgetCategoryType.SAVINGS -> "Votre poche épargne ou dettes est sous tension, maintenez vos priorités."
                        }
                    )
                    generatedAlerts.add(alert)
                } else if (bucket.ratio >= 0.80) {
                    val alert = BudgetAlert(
                        id = "${bucket.category.name}_WARNING_${month}_$year",
                        category = bucket.category,
                        severity = BudgetAlertSeverity.WARNING,
                        targetAmount = bucket.targetAmount,
                        spentAmount = bucket.spentAmount,
                        overAmount = 0.0,
                        spentPercentageOfTarget = bucket.ratio * 100.0,
                        currency = currency,
                        title = "Attention : ${bucket.category.label} à ${(bucket.ratio * 100).toInt()}%",
                        message = "Il ne vous reste que ${String.format("%,.0f", bucket.remainingAmount)} $currency avant d'atteindre votre plafond.",
                        recommendation = "Ralentissez le rythme de dépenses dans ce poste avant la fin du mois."
                    )
                    generatedAlerts.add(alert)
                }
            }
        }

        // Trigger notifications for new alerts (both 80% warning and 100% exceeded)
        generatedAlerts.filter { it.severity == BudgetAlertSeverity.EXCEEDED || it.severity == BudgetAlertSeverity.WARNING }.forEach { alert ->
            if (!previousExceededAlertIds.contains(alert.id)) {
                previousExceededAlertIds.add(alert.id)
                viewModelScope.launch {
                    _alertNotificationEvent.emit(alert)
                    sendSystemNotificationForAlert(getApplication(), alert)
                }
            }
        }

        val activeAlerts = generatedAlerts.filter { !dismissed.contains(it.id) }

        Budget503020UiState(
            monthlyIncome = effectiveIncome,
            currency = currency,
            selectedMonth = month,
            selectedYear = year,
            needs = needsBreakdown,
            wants = wantsBreakdown,
            savings = savingsBreakdown,
            totalSpent = totalSpent,
            totalRemaining = totalRemaining,
            globalRatio = globalRatio,
            health = health,
            activeAlerts = activeAlerts,
            dismissedAlertIds = dismissed,
            hasExceededCategories = needsBreakdown.isExceeded || wantsBreakdown.isExceeded || savingsBreakdown.isExceeded,
            isCustomIncome = customIncome != null
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Budget503020UiState()
    )

    fun setCustomIncome(income: Double?) {
        _customIncomeOverride.value = income
    }

    fun clearCustomIncome() {
        _customIncomeOverride.value = null
    }

    fun selectMonth(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun resetToCurrentMonth() {
        val now = Calendar.getInstance()
        _selectedMonth.value = now.get(Calendar.MONTH)
        _selectedYear.value = now.get(Calendar.YEAR)
    }

    fun dismissAlert(alertId: String) {
        _dismissedAlertIds.update { it + alertId }
    }

    fun restoreAlerts() {
        _dismissedAlertIds.value = emptySet()
    }

    fun sendSystemNotificationForAlert(context: Context, alert: BudgetAlert) {
        BrickNotificationHelper.sendBudgetAlert(context, alert)
    }
}
