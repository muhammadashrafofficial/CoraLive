package com.example.coralive.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.coralive.MainActivity

object GoalNotificationHelper {
    private const val CHANNEL_ID = "live_goals_channel"
    private const val CHANNEL_NAME = "إشعارات المباريات والأهداف"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات فورية للأهداف وأحداث الأندية والمباريات المفضلة"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createOpenAppIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun showGoalNotification(
        context: Context,
        matchId: String,
        homeTeam: String,
        awayTeam: String,
        homeScore: Int,
        awayScore: Int,
        scorerInfo: String = "تحديث النتيجة المباشرة"
    ) {
        if (!hasNotificationPermission(context)) return
        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⚽ جووول! $homeTeam $homeScore - $awayScore $awayTeam")
            .setContentText(scorerInfo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createOpenAppIntent(context, matchId.hashCode()))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(matchId.hashCode(), notification)
    }

    fun showMatchStatusNotification(
        context: Context,
        matchId: String,
        title: String,
        body: String
    ) {
        if (!hasNotificationPermission(context)) return
        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createOpenAppIntent(context, (matchId + title).hashCode()))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify((matchId + title).hashCode(), notification)
    }
}