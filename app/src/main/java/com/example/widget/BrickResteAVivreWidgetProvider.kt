package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.aistudio.brick.finances.R
import com.example.MainActivity
import com.example.data.db.BrickDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BrickResteAVivreWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.brick.ACTION_REFRESH_RESTE_A_VIVRE" ||
            intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, BrickResteAVivreWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            updateWidgets(context, appWidgetManager, allWidgetIds)
        }
    }

    companion object {
        fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = BrickDatabase.getInstance(context)
                val user = db.userDao().getUserProfileDirect()

                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val todayTxs = db.transactionDao().getTransactionsSinceDirect(cal.timeInMillis)
                val spentToday = todayTxs.filter { it.type == "DEPENSE" }.sumOf { it.amount }

                val currency = user?.currency ?: "FCFA"
                val monthlySalary = user?.monthlySalary ?: 0.0
                val fixedCharges = user?.fixedCharges ?: 0.0

                val dailyBudget = if (monthlySalary > 0) {
                    maxOf(0.0, (monthlySalary - fixedCharges) / 30.0)
                } else {
                    val allTxs = db.transactionDao().getTransactionsSinceDirect(0L)
                    val cash = allTxs.filter { it.type == "REVENU" }.sumOf { it.amount } -
                            allTxs.filter { it.type == "DEPENSE" }.sumOf { it.amount }
                    maxOf(0.0, cash / 30.0)
                }

                val resteAVivre = maxOf(0.0, dailyBudget - spentToday)

                for (appWidgetId in appWidgetIds) {
                    val openAppIntent = Intent(context, MainActivity::class.java)
                    val openPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val refreshIntent = Intent(context, BrickResteAVivreWidgetProvider::class.java).apply {
                        action = "com.example.brick.ACTION_REFRESH_RESTE_A_VIVRE"
                    }
                    val refreshPendingIntent = PendingIntent.getBroadcast(
                        context,
                        1,
                        refreshIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val views = RemoteViews(context.packageName, R.layout.widget_reste_a_vivre).apply {
                        setTextViewText(R.id.widget_amount, String.format("%,.0f %s", resteAVivre, currency))
                        setTextViewText(R.id.widget_details, "Dépensé aujourd'hui: ${String.format("%,.0f", spentToday)} $currency")
                        setOnClickPendingIntent(R.id.widget_root, openPendingIntent)
                        setOnClickPendingIntent(R.id.widget_refresh_btn, refreshPendingIntent)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
