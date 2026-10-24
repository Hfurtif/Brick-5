package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for financial transactions supporting income/expense tracking,
 * 50/30/20 budget allocations, and asset/liability classifications.
 *
 * @param id Unique identifier (auto-generated)
 * @param amount Monetary amount of the transaction
 * @param category Transaction category (e.g., "Alimentation", "Loyer", "Salaire", "Tontine")
 * @param type Transaction type ("INCOME" or "EXPENSE", also supports "REVENU" or "DEPENSE")
 * @param date Timestamp of the transaction in milliseconds
 * @param isAsset Boolean flag for asset/liability classification (true = Asset, false = Liability / Standard expense)
 * @param title Short title or description of the transaction
 * @param priority 50/30/20 classification priority ("NEEDS", "WANTS", "SAVINGS")
 * @param timestamp Alias for date for backwards compatibility
 * @param note Optional note or memo
 * @param isRecurring Whether this is a recurring recurring charge or income
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val type: String, // "INCOME" or "EXPENSE" (also "REVENU" / "DEPENSE")
    val date: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_asset")
    val isAsset: Boolean = false, // Boolean flag for asset/liability classification
    val title: String = "",
    val priority: String = "NEEDS", // "NEEDS", "WANTS", "SAVINGS"
    val timestamp: Long = date,
    val note: String = "",
    val isRecurring: Boolean = false
)

typealias TransactionEntity = Transaction
