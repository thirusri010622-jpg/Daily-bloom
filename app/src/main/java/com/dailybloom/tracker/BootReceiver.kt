package com.dailybloom.tracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderReceiver.createChannel(context)
            ReminderScheduler.scheduleAll(context)
        }
    }
}
