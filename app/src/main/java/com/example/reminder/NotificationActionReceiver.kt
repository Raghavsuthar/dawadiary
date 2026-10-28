package com.example.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val doseId = intent.getLongExtra("dose_id", -1L)
        val medicineId = intent.getLongExtra("medicine_id", -1L)
        val action = intent.action ?: return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel((ReminderManager.NOTIFICATION_ID_PREFIX + doseId).toInt())

        val db = AppDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            when (action) {
                "ACTION_TAKE" -> {
                    db.doseLogDao().updateDoseStatus(doseId, "TAKEN", System.currentTimeMillis())
                    db.medicineDao().decrementStock(medicineId, 1)
                }
                "ACTION_SNOOZE" -> {
                    val medName = intent.getStringExtra("medicine_name") ?: "Medication"
                    val doseDesc = intent.getStringExtra("dose_desc") ?: "Dose"
                    val profName = intent.getStringExtra("profile_name") ?: "Patient"
                    db.doseLogDao().updateDoseStatus(doseId, "SNOOZED", System.currentTimeMillis(), "Snoozed from notification")
                    ReminderManager.scheduleSnooze(context, doseId, medicineId, medName, doseDesc, profName)
                }
            }
        }
    }
}
