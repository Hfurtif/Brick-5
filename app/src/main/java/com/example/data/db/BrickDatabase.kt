package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.AssetLiability
import com.example.data.model.FinancialGoal
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getTransactionsSince(startTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getTransactionsSinceDirect(startTime: Long): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM financial_goals ORDER BY id ASC")
    fun getAllGoals(): Flow<List<FinancialGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: FinancialGoal): Long

    @Update
    suspend fun updateGoal(goal: FinancialGoal)

    @Delete
    suspend fun deleteGoal(goal: FinancialGoal)
}

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets_liabilities ORDER BY id ASC")
    fun getAllAssetsLiabilities(): Flow<List<AssetLiability>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssetLiability(item: AssetLiability): Long

    @Delete
    suspend fun deleteAssetLiability(item: AssetLiability)
}

@Database(
    entities = [
        UserProfile::class,
        TransactionEntity::class,
        FinancialGoal::class,
        AssetLiability::class
    ],
    version = 4,
    exportSchema = false
)
abstract class BrickDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
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
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
