package com.example.data.repository

import com.example.data.db.AssetDao
import com.example.data.db.GoalDao
import com.example.data.db.TransactionDao
import com.example.data.db.UserDao
import com.example.data.model.AssetLiability
import com.example.data.model.FinancialGoal
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class BrickRepository(
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val goalDao: GoalDao,
    private val assetDao: AssetDao
) {
    val userProfile: Flow<UserProfile?> = userDao.getUserProfile()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allGoals: Flow<List<FinancialGoal>> = goalDao.getAllGoals()
    val allAssetsLiabilities: Flow<List<AssetLiability>> = assetDao.getAllAssetsLiabilities()

    suspend fun saveUserProfile(profile: UserProfile) {
        userDao.insertOrUpdateProfile(profile)
    }

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun addGoal(goal: FinancialGoal): Long {
        return goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: FinancialGoal) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: FinancialGoal) {
        goalDao.deleteGoal(goal)
    }

    suspend fun addAssetLiability(item: AssetLiability): Long {
        return assetDao.insertAssetLiability(item)
    }

    suspend fun deleteAssetLiability(item: AssetLiability) {
        assetDao.deleteAssetLiability(item)
    }

    suspend fun resetAllData() {
        transactionDao.clearAll()
        val defaultProfile = UserProfile(
            name = "Alexandre",
            monthlySalary = 2500.0,
            fixedCharges = 980.0,
            isOnboarded = true
        )
        userDao.insertOrUpdateProfile(defaultProfile)
    }

    suspend fun initializeDefaultDataIfEmpty() {
        val existingUser = userDao.getUserProfileDirect()
        if (existingUser == null) {
            val profile = UserProfile(
                name = "Alexandre",
                currency = "€",
                monthlySalary = 2600.0,
                payDayOfMonth = 28,
                dailyReminderHour = 20,
                dailyReminderMinute = 0,
                fixedCharges = 980.0,
                needsBudgetPercentage = 50,
                wantsBudgetPercentage = 30,
                savingsBudgetPercentage = 20,
                payYourselfFirstPercentage = 20,
                isOnboarded = true,
                streakDays = 5
            )
            userDao.insertOrUpdateProfile(profile)

            // Seed realistic starter transactions for current month
            val now = System.currentTimeMillis()
            val dayMillis = 24L * 3600 * 1000

            val starterTransactions = listOf(
                TransactionEntity(
                    title = "Loyer & Charges",
                    amount = 750.0,
                    category = "Logement",
                    type = "BESOIN",
                    priority = "ESSENTIEL",
                    timestamp = now - (14 * dayMillis),
                    isRecurring = true
                ),
                TransactionEntity(
                    title = "Courses Bio & Marché",
                    amount = 124.50,
                    category = "Alimentation",
                    type = "BESOIN",
                    priority = "ESSENTIEL",
                    timestamp = now - (2 * dayMillis)
                ),
                TransactionEntity(
                    title = "Resto Bar Neon Lounge",
                    amount = 58.00,
                    category = "Sorties & Loisirs",
                    type = "ENVIE",
                    priority = "UTILE",
                    timestamp = now - (1 * dayMillis)
                ),
                TransactionEntity(
                    title = "Pass Navigo / Carburant",
                    amount = 86.40,
                    category = "Transports",
                    type = "BESOIN",
                    priority = "ESSENTIEL",
                    timestamp = now - (10 * dayMillis),
                    isRecurring = true
                ),
                TransactionEntity(
                    title = "Abonnement Spotify & Netflix",
                    amount = 27.98,
                    category = "Abonnements",
                    type = "ENVIE",
                    priority = "UTILE",
                    timestamp = now - (8 * dayMillis),
                    isRecurring = true
                ),
                TransactionEntity(
                    title = "Virement PEA ETF Monde",
                    amount = 350.00,
                    category = "Investissement",
                    type = "EPARGNE",
                    priority = "ESSENTIEL",
                    note = "Se payer en premier !",
                    timestamp = now - (12 * dayMillis)
                ),
                TransactionEntity(
                    title = "Sneakers en promo",
                    amount = 95.00,
                    category = "Shopping",
                    type = "ENVIE",
                    priority = "SUPERFLU",
                    note = "Achat impulsif identifié",
                    timestamp = now - (4 * dayMillis)
                )
            )

            for (t in starterTransactions) {
                transactionDao.insertTransaction(t)
            }

            // Seed Goals
            goalDao.insertGoal(
                FinancialGoal(
                    title = "Fonds d'Urgence (3 mois)",
                    targetAmount = 5000.0,
                    currentAmount = 3200.0,
                    category = "URGENCE"
                )
            )
            goalDao.insertGoal(
                FinancialGoal(
                    title = "Apport Investissement Locatif",
                    targetAmount = 15000.0,
                    currentAmount = 6400.0,
                    category = "ACTIF"
                )
            )

            // Seed Assets & Liabilities (Père Riche Père Pauvre)
            assetDao.insertAssetLiability(
                AssetLiability(
                    name = "Livret A Sécurisé",
                    type = "ACTIF",
                    category = "Liquidités & Livrets",
                    value = 3200.0,
                    monthlyCashflow = 8.0,
                    note = "Épargne de précaution 3% net"
                )
            )
            assetDao.insertAssetLiability(
                AssetLiability(
                    name = "PEA (ETF MSCI World)",
                    type = "ACTIF",
                    category = "Investissements & Bourse",
                    value = 4850.0,
                    monthlyCashflow = 32.0,
                    note = "Intérêts composés à long terme"
                )
            )
            assetDao.insertAssetLiability(
                AssetLiability(
                    name = "Crédit Smartphone 24 mois",
                    type = "PASSIF",
                    category = "Crédit Conso",
                    value = 520.0,
                    monthlyCashflow = -35.0,
                    note = "Passif : retire de l'argent de ta poche chaque mois"
                )
            )
        }
    }
}
