package com.example.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.withTransaction
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

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                when (action) {
                    "ACTION_TAKE" -> {
                        db.withTransaction {
                            val rows = db.doseLogDao().updateDoseStatusConditional(
                                id = doseId,
                                expectedStatus = "PENDING",
                                newStatus = "TAKEN",
                                actionTime = System.currentTimeMillis()
                            )
                            if (rows > 0) {
                                db.medicineDao().decrementStock(medicineId, 1)
                            }
                        }
                    }
                    "ACTION_SNOOZE" -> {
                        val medName = intent.getStringExtra("medicine_name") ?: "Medication"
                        val doseDesc = intent.getStringExtra("dose_desc") ?: "Dose"
                        val profName = intent.getStringExtra("profile_name") ?: "Patient"
                        val updatedRows = db.doseLogDao().updateDoseStatusConditional(
                            id = doseId,
                            expectedStatus = "PENDING",
                            newStatus = "SNOOZED",
                            actionTime = System.currentTimeMillis(),
                            notes = "Snoozed from notification"
                        )
                        if (updatedRows > 0) {
                            ReminderManager.scheduleSnooze(context, doseId, medicineId, medName, doseDesc, profName)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
