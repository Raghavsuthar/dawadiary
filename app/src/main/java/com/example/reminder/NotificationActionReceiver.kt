package com.example.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val appContext = context.applicationContext
        val doseId = intent.getLongExtra(ReminderManager.EXTRA_DOSE_ID, -1L)
        val medicineId = intent.getLongExtra(ReminderManager.EXTRA_MEDICINE_ID, -1L)

        if (doseId == -1L) return

        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(doseId.toInt())

        when (action) {
            ACTION_TAKE -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance(appContext)
                    db.doseLogDao().updateDoseStatus(doseId, "TAKEN", System.currentTimeMillis())

                    if (medicineId != -1L) {
                        db.medicineDao().decrementStock(medicineId, 1)
                        val med = db.medicineDao().getMedicineById(medicineId)
                        if (med != null && med.stockCount <= med.lowStockThreshold) {
                            showLowStockAlert(appContext, med.id, med.name, med.stockCount)
                        }
                    }
                    ReminderManager.syncAndScheduleAlarms(appContext)
                }
            }

            ACTION_SNOOZE -> {
                val medicineName = intent.getStringExtra(ReminderManager.EXTRA_MEDICINE_NAME) ?: "Medicine"
                val doseDesc = intent.getStringExtra(ReminderManager.EXTRA_MEDICINE_DOSE) ?: ""
                val profileName = intent.getStringExtra(ReminderManager.EXTRA_PROFILE_NAME) ?: "Patient"

                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance(appContext)
                    db.doseLogDao().updateDoseStatus(doseId, "SNOOZED", System.currentTimeMillis(), "Snoozed for 10 minutes")
                    ReminderManager.scheduleSnooze(
                        context = appContext,
                        doseId = doseId,
                        medicineId = medicineId,
                        medicineName = medicineName,
                        doseDesc = doseDesc,
                        profileName = profileName
                    )
                }
            }

            ACTION_SKIP -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance(appContext)
                    db.doseLogDao().updateDoseStatus(doseId, "SKIPPED", System.currentTimeMillis(), "Skipped by user")
                    ReminderManager.syncAndScheduleAlarms(appContext)
                }
            }
        }
    }

    private fun showLowStockAlert(context: Context, medId: Long, name: String, remaining: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, ReminderManager.CHANNEL_REFILLS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Low Medication Stock: $name")
            .setContentText("Only $remaining doses remaining! Please refill soon.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        notificationManager.notify((medId + 200000).toInt(), notification)
    }

    companion object {
        const val ACTION_TAKE = "com.example.ACTION_NOTIFICATION_TAKE"
        const val ACTION_SNOOZE = "com.example.ACTION_NOTIFICATION_SNOOZE"
        const val ACTION_SKIP = "com.example.ACTION_NOTIFICATION_SKIP"
    }
}
