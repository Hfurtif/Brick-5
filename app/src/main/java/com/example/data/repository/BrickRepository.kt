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
        goalDao.clearAll()
        assetDao.clearAll()
        val defaultProfile = UserProfile(
            name = "",
            currency = "FCFA",
            monthlySalary = 350000.0,
            payDayOfMonth = 28,
            dailyReminderHour = 20,
            dailyReminderMinute = 0,
            fixedCharges = 120000.0,
            needsBudgetPercentage = 50,
            wantsBudgetPercentage = 30,
            savingsBudgetPercentage = 20,
            payYourselfFirstPercentage = 20,
            isOnboarded = false,
            biometricEnabled = false,
            streakDays = 0
        )
        userDao.insertOrUpdateProfile(defaultProfile)
    }

    suspend fun initializeDefaultDataIfEmpty() {
        val existingUser = userDao.getUserProfileDirect()
        if (existingUser == null) {
            val profile = UserProfile(
                name = "",
                currency = "FCFA",
                monthlySalary = 350000.0,
                payDayOfMonth = 28,
                dailyReminderHour = 20,
                dailyReminderMinute = 0,
                fixedCharges = 120000.0,
                needsBudgetPercentage = 50,
                wantsBudgetPercentage = 30,
                savingsBudgetPercentage = 20,
                payYourselfFirstPercentage = 20,
                isOnboarded = false,
                biometricEnabled = false,
                streakDays = 0
            )
            userDao.insertOrUpdateProfile(profile)
        }
    }
}
