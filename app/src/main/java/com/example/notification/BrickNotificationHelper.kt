package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.ui.viewmodel.BudgetAlert
import com.example.ui.viewmodel.BudgetAlertSeverity

object BrickNotificationHelper {
    private const val CHANNEL_BUDGET_ALERTS = "brick_budget_alerts"
    private const val CHANNEL_DAILY_RECAP = "brick_daily_recap"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val alertChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                "Alertes Dépassement Budget",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications lorsque vos catégories 50/30/20 approchent ou dépassent leur plafond."
                enableVibration(true)
            }

            val recapChannel = NotificationChannel(
                CHANNEL_DAILY_RECAP,
                "Récapitulatif du Soir",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Bilan quotidien de vos finances et reste à vivre."
            }

            manager.createNotificationChannel(alertChannel)
            manager.createNotificationChannel(recapChannel)
        }
    }

    fun sendBudgetAlert(context: Context, alert: BudgetAlert) {
        createChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            alert.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconRes = android.R.drawable.stat_notify_error

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(iconRes)
            .setContentTitle("BRICK ⚠️ " + alert.title)
            .setContentText(alert.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${alert.message}\n\n💡 Conseil BRICK : ${alert.recommendation}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(alert.id.hashCode(), notification)
    }

    fun sendDailyRecapNotification(
        context: Context,
        resteAVivre: Double,
        spentToday: Double,
        currency: String
    ) {
        createChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedReste = String.format("%,.0f %s", resteAVivre, currency)
        val formattedSpent = String.format("%,.0f %s", spentToday, currency)

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_RECAP)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("BRICK 🌙 Bilan du Soir")
            .setContentText("Dépenses du jour: $formattedSpent | Reste à vivre: $formattedReste")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Aujourd'hui, vous avez dépensé $formattedSpent.\nVotre reste à vivre disponible est de $formattedReste.\nGardez le contrôle sur votre forteresse financière !"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(1001, notification)
    }
}
