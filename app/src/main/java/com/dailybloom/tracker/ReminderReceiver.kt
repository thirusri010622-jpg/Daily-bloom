package com.dailybloom.tracker

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderScheduler {

    private val reminders = listOf(
        Triple(101, "Water reminder", 4 to 0),
        Triple(102, "Morning gratitude", 4 to 5),
        Triple(103, "Eat coconut", 17 to 0),
        Triple(104, "Evening gratitude", 20 to 55)
    )

    fun scheduleAll(context: Context) {
        reminders.forEach { (id, title, time) ->
            schedule(context, id, title, time.first, time.second)
        }
    }

    fun schedule(
        context: Context,
        id: Int,
        title: String,
        hour: Int,
        minute: Int
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("title", title)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}

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

                val manager =
                    context.getSystemService(NotificationManager::class.java)

                manager.createNotificationChannel(channel)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getIntExtra("id", 0) ?: 0
        val title = intent?.getStringExtra("title") ?: "Daily reminder"
        val hour = intent?.getIntExtra("hour", 0) ?: 0
        val minute = intent?.getIntExtra("minute", 0) ?: 0

        createChannel(context)

        val openIntent = Intent(context, MainActivity::class.java)

        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Daily Bloom 🌸")
            .setContentText(title)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .build()

        val manager =
            context.getSystemService(NotificationManager::class.java)

        try {
            manager.notify(id, notification)
        } catch (_: SecurityException) {
        }

        ReminderScheduler.schedule(
            context,
            id,
            title,
            hour,
            minute
        )
    }
}
