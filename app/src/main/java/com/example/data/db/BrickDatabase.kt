package com.example.data.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Query("DELETE FROM user_profile")
    suspend fun clearAll()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getTransactionsSince(startTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getTransactionsSinceDirect(startTime: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE priority = :priority ORDER BY timestamp DESC")
    fun getTransactionsByPriority(priority: String): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEPENSE' AND priority = :priority AND timestamp BETWEEN :startTime AND :endTime")
    fun getSpentByPriorityBetween(priority: String, startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEPENSE' AND timestamp BETWEEN :startTime AND :endTime")
    fun getTotalSpentBetween(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'REVENU' AND timestamp BETWEEN :startTime AND :endTime")
    fun getTotalIncomeBetween(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT * FROM transactions WHERE is_asset = :isAsset ORDER BY date DESC")
    fun getTransactionsByAssetClassification(isAsset: Boolean): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startTime AND :endTime ORDER BY date DESC")
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY bucket ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE bucket = :bucket ORDER BY name ASC")
    fun getCategoriesByBucket(bucket: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun clearAll()
}

@Dao
interface BudgetSettingsDao {
    @Query("SELECT * FROM budget_settings WHERE id = 1 LIMIT 1")
    fun getBudgetSettings(): Flow<BudgetSettingsEntity?>

    @Query("SELECT * FROM budget_settings WHERE id = 1 LIMIT 1")
    suspend fun getBudgetSettingsDirect(): BudgetSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: BudgetSettingsEntity)

    @Query("DELETE FROM budget_settings")
    suspend fun clearAll()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY id ASC")
    fun getAllGoals(): Flow<List<FinancialGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: FinancialGoal): Long

    @Update
    suspend fun updateGoal(goal: FinancialGoal)

    @Delete
    suspend fun deleteGoal(goal: FinancialGoal)

    @Query("DELETE FROM goals")
    suspend fun clearAll()
}

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets_liabilities ORDER BY type ASC, value DESC")
    fun getAllAssetsLiabilities(): Flow<List<AssetLiability>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssetLiability(item: AssetLiability): Long

    @Delete
    suspend fun deleteAssetLiability(item: AssetLiability)

    @Query("DELETE FROM assets_liabilities")
    suspend fun clearAll()
}

@Database(
    entities = [
        UserProfile::class,
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetSettingsEntity::class,
        FinancialGoal::class,
        AssetLiability::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BrickDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetSettingsDao(): BudgetSettingsDao
    abstract fun goalDao(): GoalDao
    abstract fun assetDao(): AssetDao

    companion object {
        @Volatile
        private var INSTANCE: BrickDatabase? = null

        fun getInstance(context: Context): BrickDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrickDatabase::class.java,
                    "brick_finance.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                populateDefault503020Data(database)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateDefault503020Data(db: BrickDatabase) {
            val defaultCategories = listOf(
                // NEEDS (50%)
                CategoryEntity(name = "Alimentation", bucket = "NEEDS", icon = "🍲", colorHex = "#00E5FF"),
                CategoryEntity(name = "Logement & Loyer", bucket = "NEEDS", icon = "🏠", colorHex = "#0284C7"),
                CategoryEntity(name = "Factures (Eau/CIE)", bucket = "NEEDS", icon = "💡", colorHex = "#38BDF8"),
                CategoryEntity(name = "Transport & Carburant", bucket = "NEEDS", icon = "🚕", colorHex = "#0EA5E9"),
                CategoryEntity(name = "Santé & Pharmacie", bucket = "NEEDS", icon = "💊", colorHex = "#14B8A6"),
                CategoryEntity(name = "Éducation & Frais", bucket = "NEEDS", icon = "📚", colorHex = "#06B6D4"),

                // WANTS (30%)
                CategoryEntity(name = "Sorties & Restaurants", bucket = "WANTS", icon = "🍹", colorHex = "#8B5CF6"),
                CategoryEntity(name = "Shopping & Vêtements", bucket = "WANTS", icon = "🛍️", colorHex = "#A78BFA"),
                CategoryEntity(name = "Loisirs & Divertissement", bucket = "WANTS", icon = "🎮", colorHex = "#C084FC"),
                CategoryEntity(name = "Beauté & Soins", bucket = "WANTS", icon = "✨", colorHex = "#D8B4FE"),

                // SAVINGS (20%)
                CategoryEntity(name = "Tontine", bucket = "SAVINGS", icon = "🤝", colorHex = "#FFB800"),
                CategoryEntity(name = "Réserve de Sécurité", bucket = "SAVINGS", icon = "🛡️", colorHex = "#10B981"),
                CategoryEntity(name = "Investissement & Business", bucket = "SAVINGS", icon = "📈", colorHex = "#F59E0B"),
                CategoryEntity(name = "Remboursement de Dettes", bucket = "SAVINGS", icon = "💳", colorHex = "#EF4444"),

                // INCOME
                CategoryEntity(name = "Salaire & Revenus", bucket = "INCOME", icon = "💵", colorHex = "#10B981"),
                CategoryEntity(name = "Vente & Commerce", bucket = "INCOME", icon = "📦", colorHex = "#34D399"),
                CategoryEntity(name = "Gain de Tontine", bucket = "INCOME", icon = "🏆", colorHex = "#FBBF24")
            )
            db.categoryDao().insertCategories(defaultCategories)

            val defaultSettings = BudgetSettingsEntity(
                id = 1,
                monthlyIncomeBase = 0.0,
                needsPercentage = 50,
                wantsPercentage = 30,
                savingsPercentage = 20,
                alertThresholdPercentage = 80,
                alertOnExceeded = true,
                currency = "FCFA"
            )
            db.budgetSettingsDao().insertOrUpdate(defaultSettings)
        }
    }
}
