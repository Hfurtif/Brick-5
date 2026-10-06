package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.BrickDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

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
        if (intent.action == ACTION_REFRESH_RESTE_A_VIVRE || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, BrickResteAVivreWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            updateWidgets(context, appWidgetManager, allWidgetIds)
        }
    }

    private fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (appWidgetIds.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = BrickDatabase.getInstance(context)
            val user = db.userDao().getUserProfileDirect()

            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfToday = cal.timeInMillis

            val todayTxs = db.transactionDao().getTransactionsSinceDirect(startOfToday)
            val todaySpent = todayTxs.sumOf { it.amount }

            val totalDaysInMonth = max(1, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH))
            val monthlySalary = user?.monthlySalary ?: 2400.0
            val fixedCharges = user?.fixedCharges ?: 950.0
            val currency = user?.currency ?: "€"

            // Daily quota available for discretionary spend
            val availableMonthly = max(0.0, monthlySalary - fixedCharges)
            val dailyBudget = availableMonthly / totalDaysInMonth
            val resteAVivre = dailyBudget - todaySpent

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_reste_a_vivre)

                // Formatting Reste à vivre text and status
                val (amountText, amountColor, statusText, statusColor) = when {
                    resteAVivre >= dailyBudget * 0.4 -> {
                        Tuple4(
                            String.format(Locale.getDefault(), "+%.1f %s", resteAVivre, currency),
                            Color.parseColor("#38BDF8"), // Cyan
                            "🛡️ Dans les clous",
                            Color.parseColor("#10B981") // Green
                        )
                    }
                    resteAVivre >= 0 -> {
                        Tuple4(
                            String.format(Locale.getDefault(), "+%.1f %s", resteAVivre, currency),
                            Color.parseColor("#F59E0B"), // Amber
                            "⚠️ Limite proche",
                            Color.parseColor("#F59E0B")
                        )
                    }
                    else -> {
                        Tuple4(
                            String.format(Locale.getDefault(), "%.1f %s", resteAVivre, currency),
                            Color.parseColor("#EF4444"), // Red
                            "🚨 Quota dépassé",
                            Color.parseColor("#EF4444")
                        )
                    }
                }

                views.setTextViewText(R.id.widget_amount, amountText)
                views.setTextColor(R.id.widget_amount, amountColor)

                val detailsText = "Dépensé : ${todaySpent.toInt()} $currency • Quota : ${dailyBudget.toInt()} $currency"
                views.setTextViewText(R.id.widget_details, detailsText)

                views.setTextViewText(R.id.widget_status, statusText)
                views.setTextColor(R.id.widget_status, statusColor)

                // Open app on click on widget body
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context,
                    widgetId,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

                // Manual refresh button on click
                val refreshIntent = Intent(context, BrickResteAVivreWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH_RESTE_A_VIVRE
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
                    widgetId,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_refresh_btn, refreshPendingIntent)

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    companion object {
        const val ACTION_REFRESH_RESTE_A_VIVRE = "com.example.brick.ACTION_REFRESH_RESTE_A_VIVRE"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, BrickResteAVivreWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_RESTE_A_VIVRE
            }
            context.sendBroadcast(intent)
        }
    }
}
