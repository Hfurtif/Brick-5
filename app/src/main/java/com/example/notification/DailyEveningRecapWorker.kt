package com.example.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.db.BrickDatabase
import com.example.data.repository.BrickRepository
import java.util.Calendar

class DailyEveningRecapWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val db = BrickDatabase.getInstance(applicationContext)
        val repository = BrickRepository(db.userDao(), db.transactionDao(), db.goalDao(), db.assetDao())

        val user = db.userDao().getUserProfileDirect() ?: return Result.success()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = cal.timeInMillis

        val todayTransactions = db.transactionDao().getTransactionsSinceDirect(todayStart)
        val spentToday = todayTransactions.filter { it.type == "DEPENSE" }.sumOf { it.amount }

        val resteAVivre = if (user.monthlySalary > 0) {
            maxOf(0.0, (user.monthlySalary - user.fixedCharges) / 30.0 - spentToday)
        } else {
            0.0
        }

        BrickNotificationHelper.sendDailyRecapNotification(
            applicationContext,
            resteAVivre,
            spentToday,
            user.currency
        )

        return Result.success()
    }
}
