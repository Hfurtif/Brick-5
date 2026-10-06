package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object BrickNotificationHelper {

    const val CHANNEL_ID = "brick_daily_recap_channel"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Rappel Quotidien BRICK"
            val descriptionText = "Notifications du soir pour le récapitulatif des dépenses du jour"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendEveningNotification(
        context: Context,
        todaySpent: Double,
        currency: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.brick.ACTION_EVENING_RECAP"
            putExtra("open_evening_recap", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedAmount = String.format("%,.0f", todaySpent).replace(',', ' ')

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🌙 BRICK • Bilan de ta journée")
            .setContentText("Aujourd'hui, tu as dépensé $formattedAmount $currency.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Aujourd'hui, tu as dépensé $formattedAmount $currency. Touche pour voir le détail de tes dépenses et ton conseil IA du soir.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted yet
        }
    }

    fun sendEveningNotificationWithSummary(
        context: Context,
        todaySpent: Double,
        currency: String,
        summary: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.brick.ACTION_EVENING_RECAP"
            putExtra("open_evening_recap", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedAmount = String.format("%,.0f", todaySpent).replace(',', ' ')

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🌙 BRICK • Bilan du soir ($formattedAmount $currency)")
            .setContentText("Aujourd'hui, tu as dépensé $formattedAmount $currency.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Aujourd'hui, tu as dépensé $formattedAmount $currency.\n$summary\nTouche pour consulter le détail de tes dépenses.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted yet
        }
    }
}
