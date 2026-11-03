package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in setOf(
                Intent.ACTION_BOOT_COMPLETED,
                "android.intent.action.QUICKBOOT_POWERON",
                "android.intent.action.DATE_CHANGED",
                "android.intent.action.MY_PACKAGE_REPLACED"
            )) {
            ReminderManager.initNotificationChannels(context)
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ReminderManager.syncAndScheduleAlarms(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
