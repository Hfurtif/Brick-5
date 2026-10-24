package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class BrickRepository(
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val goalDao: GoalDao,
    private val assetDao: AssetDao,
    private val categoryDao: CategoryDao? = null,
    private val budgetSettingsDao: BudgetSettingsDao? = null
) {
    val userProfile: Flow<UserProfile?> = userDao.getUserProfile()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allGoals: Flow<List<FinancialGoal>> = goalDao.getAllGoals()
    val allAssetsLiabilities: Flow<List<AssetLiability>> = assetDao.getAllAssetsLiabilities()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao?.getAllCategories() ?: emptyFlow()
    val budgetSettings: Flow<BudgetSettingsEntity?> = budgetSettingsDao?.getBudgetSettings() ?: emptyFlow()

    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsBetween(startTime, endTime)
    }

    fun getSpentByPriorityBetween(priority: String, startTime: Long, endTime: Long): Flow<Double?> {
        return transactionDao.getSpentByPriorityBetween(priority, startTime, endTime)
    }

    fun getTotalSpentBetween(startTime: Long, endTime: Long): Flow<Double?> {
        return transactionDao.getTotalSpentBetween(startTime, endTime)
    }

    fun getTotalIncomeBetween(startTime: Long, endTime: Long): Flow<Double?> {
        return transactionDao.getTotalIncomeBetween(startTime, endTime)
    }

    fun getTransactionsByAssetClassification(isAsset: Boolean): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByAssetClassification(isAsset)
    }

    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRange(startTime, endTime)
    }

    fun getCategoriesByBucket(bucket: String): Flow<List<CategoryEntity>> {
        return categoryDao?.getCategoriesByBucket(bucket) ?: emptyFlow()
    }

    suspend fun addCategory(category: CategoryEntity): Long {
        return categoryDao?.insertCategory(category) ?: -1L
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao?.deleteCategory(category)
    }

    suspend fun saveBudgetSettings(settings: BudgetSettingsEntity) {
        budgetSettingsDao?.insertOrUpdate(settings)
    }

    suspend fun getBudgetSettingsDirect(): BudgetSettingsEntity? {
        return budgetSettingsDao?.getBudgetSettingsDirect()
    }

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
        userDao.clearAll()
        transactionDao.clearAll()
        goalDao.clearAll()
        assetDao.clearAll()
        categoryDao?.clearAll()
        budgetSettingsDao?.clearAll()
        initializeDefaultDataIfEmpty()
    }

    suspend fun initializeDefaultDataIfEmpty() {
        val existing = userDao.getUserProfileDirect()
        if (existing == null) {
            val defaultUser = UserProfile(
                id = 1,
                name = "Alexandre",
                currency = "FCFA",
                monthlySalary = 0.0,
                payDayOfMonth = 28,
                dailyReminderHour = 20,
                dailyReminderMinute = 0,
                fixedCharges = 0.0,
                needsBudgetPercentage = 50,
                wantsBudgetPercentage = 30,
                savingsBudgetPercentage = 20,
                payYourselfFirstPercentage = 10,
                isOnboarded = false
            )
            userDao.insertOrUpdateProfile(defaultUser)
        }
    }
}
