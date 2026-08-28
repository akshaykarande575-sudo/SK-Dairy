package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.FarmAlert

object NotificationHelper {
  private const val CHANNEL_ID_ALERTS = "dairy_farm_alerts_channel"
  private const val CHANNEL_NAME_ALERTS = "Dairy Breeding & Milk Alerts"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID_ALERTS,
        CHANNEL_NAME_ALERTS,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alerts for cow calving (5 days prior), 3-month post delivery checks, and pregnancy checks"
        enableVibration(true)
      }
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  fun showNotification(context: Context, alert: FarmAlert, isMarathi: Boolean = true) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val title = if (isMarathi) alert.titleMr else alert.titleEn
    val message = if (isMarathi) alert.messageMr else alert.messageEn

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      alert.id.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setContentIntent(pendingIntent)
      .setAutoCancel(true)
      .build()

    manager.notify(alert.id.hashCode(), notification)
  }
}
