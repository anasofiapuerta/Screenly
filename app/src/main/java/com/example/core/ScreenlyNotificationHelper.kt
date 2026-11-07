package com.example.core

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.domain.model.AlertThreshold

object ScreenlyNotificationHelper {

    private const val CHANNEL_ID = "screenly_usage_limits_channel"
    private const val CHANNEL_NAME = "Alertas de Límites de Uso"
    private const val CHANNEL_DESC = "Notificaciones automáticas al alcanzar el 80% y 100% del límite diario"

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun sendLimitNotification(
        context: Context,
        appId: String,
        appName: String,
        threshold: AlertThreshold,
        minutesUsed: Int,
        limitMinutes: Int
    ) {
        if (!PermissionHelper.hasNotificationPermission(context)) {
            return
        }

        try {
            ensureNotificationChannel(context)

            val usedFormatted = TimeUtils.formatMinutesToDuration(minutesUsed)
            val limitFormatted = TimeUtils.formatMinutesToDuration(limitMinutes)

            val title = when (threshold) {
                AlertThreshold.REACHED_100 -> "Límite diario alcanzado: $appName"
                AlertThreshold.PROXIMITY_80 -> "Límite próximo (80%): $appName"
            }

            val body = when (threshold) {
                AlertThreshold.REACHED_100 ->
                    "Has utilizado $usedFormatted de tu límite diario de $limitFormatted en $appName. Es momento de tomar un descanso."
                AlertThreshold.PROXIMITY_80 ->
                    "Llevas $usedFormatted de $limitFormatted en $appName. Estás por alcanzar tu límite diario."
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("extra_target_app_id", appId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                appId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val notificationId = (appId.hashCode() and 0x7FFFFFFF) +
                if (threshold == AlertThreshold.REACHED_100) 1000 else 800

            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: Exception) {
            // Safely ignore if notification dispatch fails in restricted test environments
        }
    }
}
