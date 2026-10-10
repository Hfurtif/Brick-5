package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alexandre",
    val currency: String = "FCFA",
    val monthlySalary: Double = 350000.0,
    val payDayOfMonth: Int = 28,
    val dailyReminderHour: Int = 20,
    val dailyReminderMinute: Int = 0,
    val fixedCharges: Double = 120000.0, // Loyer, charges, factures fixes
    val needsBudgetPercentage: Int = 50,
    val wantsBudgetPercentage: Int = 30,
    val savingsBudgetPercentage: Int = 20,
    val payYourselfFirstPercentage: Int = 20,
    val isOnboarded: Boolean = false,
    val pinCode: String = "", // Vide si désactivé
    val biometricEnabled: Boolean = false, // Empreinte digitale ou code du smartphone
    val hideAmounts: Boolean = false,
    val themeMode: String = "SYSTEM", // SYSTEM (Auto), DARK, LIGHT
    val streakDays: Int = 0,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String, // Alimentation, Logement, Transports, Loisirs, Shopping, Abonnements, Santé, Investissement, Dettes, Autre
    val type: String, // BESOIN, ENVIE, EPARGNE, ACTIF, PASSIF
    val priority: String = "UTILE", // ESSENTIEL, UTILE, SUPERFLU
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false
)

@Entity(tableName = "financial_goals")
data class FinancialGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: Long = System.currentTimeMillis() + (180L * 24 * 3600 * 1000), // 6 mois
    val category: String = "EPARGNE" // EPARGNE, URGENCE, ACTIF, PROJET
)

@Entity(tableName = "assets_liabilities")
data class AssetLiability(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // ACTIF ou PASSIF
    val category: String = "Liquidités", // Liquidités, Bourse, Immobilier, Crypto, Crédit Conso, etc.
    val value: Double, // Valeur actuelle ou capital restant
    val monthlyCashflow: Double, // Revenu passif (+) ou Mensualité/Coût mensuel (-)
    val note: String = ""
)

enum class ExpenseClassification(val label: String, val description: String) {
    BESOIN("Besoin (50%)", "Indispensable pour vivre et travailler"),
    ENVIE("Envie (30%)", "Plaisirs, sorties, confort et loisirs"),
    EPARGNE("Épargne & Actif (20%)", "Se payer en premier pour sa liberté future")
}

enum class PriorityLevel(val label: String, val advice: String) {
    ESSENTIEL("Essentielle", "Ne peut être différée sans risque"),
    UTILE("Utile", "Améliore le quotidien mais négociable"),
    SUPERFLU("Superflue", "Achat impulsif ou confort superflu à différer")
}
