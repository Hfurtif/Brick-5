package com.example.ai

data class BrickBadge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean = false
)

data class FinancialLeak(
    val category: String,
    val monthlyEstimate: Double,
    val annualCost: Double,
    val advice: String
)

data class PurchaseSimulationResult(
    val canAfford: Boolean,
    val score: Int,
    val verdict: String,
    val advice: String,
    val daysOfWorkRequired: Double,
    val impactOnGoals: String
)

data class FinancialSnapshot(
    val resteAVivreDuJour: Double,
    val monthlyNetCashflow: Double,
    val financialHealthScore: Int,
    val netWorth: Double,
    val activeLeaks: List<FinancialLeak>
)

data class CoachMessage(
    val id: String,
    val sender: String, // "USER" or "COACH"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ParsedExpense(
    val title: String,
    val amount: Double,
    val category: String,
    val type: String = "DEPENSE",
    val priority: String = "NEEDS"
)
