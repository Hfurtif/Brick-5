package com.example.ui.viewmodel

enum class BudgetCategoryType(val label: String, val percentage: Int) {
    NEEDS("Besoins vitaux", 50),
    WANTS("Envies & Plaisirs", 30),
    SAVINGS("Épargne & Dettes", 20)
}

enum class BudgetAlertSeverity {
    INFO,
    WARNING,
    CRITICAL,
    EXCEEDED
}

enum class BudgetHealth {
    HEALTHY,
    WARNING,
    CRITICAL
}

data class BudgetBucketBreakdown(
    val category: BudgetCategoryType,
    val percentage: Int,
    val targetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val ratio: Double,
    val isExceeded: Boolean
)

data class BudgetAlert(
    val id: String,
    val category: BudgetCategoryType,
    val severity: BudgetAlertSeverity,
    val targetAmount: Double,
    val spentAmount: Double,
    val overAmount: Double,
    val spentPercentageOfTarget: Double,
    val currency: String,
    val title: String,
    val message: String,
    val recommendation: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Budget503020UiState(
    val monthlyIncome: Double = 0.0,
    val currency: String = "FCFA",
    val selectedMonth: Int = 0,
    val selectedYear: Int = 2026,
    val needs: BudgetBucketBreakdown = BudgetBucketBreakdown(BudgetCategoryType.NEEDS, 50, 0.0, 0.0, 0.0, 0.0, false),
    val wants: BudgetBucketBreakdown = BudgetBucketBreakdown(BudgetCategoryType.WANTS, 30, 0.0, 0.0, 0.0, 0.0, false),
    val savings: BudgetBucketBreakdown = BudgetBucketBreakdown(BudgetCategoryType.SAVINGS, 20, 0.0, 0.0, 0.0, 0.0, false),
    val totalSpent: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val globalRatio: Double = 0.0,
    val health: BudgetHealth = BudgetHealth.HEALTHY,
    val activeAlerts: List<BudgetAlert> = emptyList(),
    val dismissedAlertIds: Set<String> = emptySet(),
    val hasExceededCategories: Boolean = false,
    val isCustomIncome: Boolean = false
)
