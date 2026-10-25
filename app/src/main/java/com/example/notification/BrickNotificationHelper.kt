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

    fun sendBudgetThresholdAlert(
        context: Context,
        categoryType: com.example.ui.viewmodel.BudgetCategoryType,
        ratio: Double,
        spent: Double,
        target: Double,
        currency: String
    ) {
        createChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO_TAB", 2)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            categoryType.ordinal + 8000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val percent = (ratio * 100).toInt()
        val remaining = maxOf(0.0, target - spent)
        val isExceeded = ratio >= 1.0

        val title = if (isExceeded) {
            "🚨 Plafond Dépassé : ${categoryType.label} ($percent%)"
        } else {
            "⚠️ Alerte 80% Budget : ${categoryType.label} ($percent%)"
        }

        val shortMessage = if (isExceeded) {
            "Plafond de ${String.format("%,.0f", target)} $currency dépassé de ${String.format("%,.0f", spent - target)} $currency."
        } else {
            "Plus que ${String.format("%,.0f", remaining)} $currency disponible avant d'atteindre votre plafond de ${String.format("%,.0f", target)} $currency."
        }

        val recommendation = when (categoryType) {
            com.example.ui.viewmodel.BudgetCategoryType.NEEDS ->
                "Vos charges incompressibles consomment l'essentiel du budget. Vérifiez les dépenses de nourriture, transport ou loyer."
            com.example.ui.viewmodel.BudgetCategoryType.WANTS ->
                "Freinez les sorties, loisirs et achats d'impulsion pour sécuriser votre reste à vivre."
            com.example.ui.viewmodel.BudgetCategoryType.SAVINGS ->
                "Priorisez votre tontine et votre épargne de sécurité avant tout autre engagement."
        }

        val bigText = buildString {
            append("Consommation : ${String.format("%,.0f", spent)} $currency sur ${String.format("%,.0f", target)} $currency ($percent%).\n")
            append(shortMessage)
            append("\n\n💡 Conseil BRICK :\n")
            append(recommendation)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(shortMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(categoryType.ordinal + 8000, notification)
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
