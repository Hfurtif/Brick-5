package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alexandre",
    val currency: String = "FCFA",
    val monthlySalary: Double = 0.0,
    val payDayOfMonth: Int = 28,
    val dailyReminderHour: Int = 20,
    val dailyReminderMinute: Int = 0,
    val fixedCharges: Double = 0.0,
    val needsBudgetPercentage: Int = 50,
    val wantsBudgetPercentage: Int = 30,
    val savingsBudgetPercentage: Int = 20,
    val payYourselfFirstPercentage: Int = 10,
    val isOnboarded: Boolean = false,
    val pinCode: String = "",
    val biometricEnabled: Boolean = false,
    val hideAmounts: Boolean = false,
    val themeMode: String = "DARK",
    val streakDays: Int = 1,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bucket: String, // "NEEDS", "WANTS", "SAVINGS", "INCOME"
    val icon: String = "📁",
    val colorHex: String = "#00E5FF",
    val isDefault: Boolean = true
)

@Entity(tableName = "budget_settings")
data class BudgetSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val monthlyIncomeBase: Double = 0.0,
    val needsPercentage: Int = 50,
    val wantsPercentage: Int = 30,
    val savingsPercentage: Int = 20,
    val alertThresholdPercentage: Int = 80,
    val alertOnExceeded: Boolean = true,
    val currency: String = "FCFA",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class FinancialGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: Long = 0L,
    val category: String = "EPARGNE"
)

@Entity(tableName = "assets_liabilities")
data class AssetLiability(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "ACTIF", "PASSIF"
    val category: String, // "IMMOBILIER", "CAISSE", "TONTINE", "BUSINESS", "DETTE", "CREDIT"
    val value: Double,
    val monthlyCashflow: Double = 0.0,
    val note: String = ""
)

enum class PriorityLevel(val label: String, val advice: String) {
    ESSENTIAL("Indispensable (Besoins 50%)", "Dépense prioritaire vitale"),
    COMFORT("Confort / Plaisir (Envies 30%)", "Dépense modulable ou reportable"),
    IMPULSE("Pulsion / Risque", "Attention au gaspillage et fuites financières"),
    INVESTMENT("Investissement / Épargne (20%)", "Bâtit ton avenir et ta sécurité")
}

enum class ExpenseClassification(val label: String, val description: String) {
    NEEDS("Besoins (50%)", "Nourriture, loyer, transport, santé, électricité"),
    WANTS("Envies (30%)", "Sorties, loisirs, shopping, restaurants"),
    SAVINGS("Épargne / Dettes (20%)", "Tontine, réserve de sécurité, investissement, remboursement")
}
