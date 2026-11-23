
package com.dailybloom.tracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "daily_bloom_reminders"

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Daily Bloom Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val manager = context.getSystemService(
                    NotificationManager::class.java
                )
                manager.createNotificationChannel(channel)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val title = intent?.getStringExtra("title") ?: "Daily reminder"

        createChannel(context)

        val openApp = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Daily Bloom")
            .setContentText(title)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_REMINDER)
            .build()

        val manager = context.getSystemService(
            NotificationManager::class.java
        )

        try {
            manager.notify(
                intent?.getIntExtra("id", 0) ?: 0,
                notification
            )
        } catch (_: SecurityException) {
            // Notification permission may be disabled.
        }
    }
}
