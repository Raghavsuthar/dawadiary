package com.example.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val appContext = context.applicationContext

        when (action) {
            ReminderManager.ACTION_REMINDER -> {
                val doseId = intent.getLongExtra(ReminderManager.EXTRA_DOSE_ID, -1L)
                val medicineId = intent.getLongExtra(ReminderManager.EXTRA_MEDICINE_ID, -1L)
                val medicineName = intent.getStringExtra(ReminderManager.EXTRA_MEDICINE_NAME) ?: "Medicine"
                val medicineDose = intent.getStringExtra(ReminderManager.EXTRA_MEDICINE_DOSE) ?: ""
                val profileName = intent.getStringExtra(ReminderManager.EXTRA_PROFILE_NAME) ?: "Patient"

                if (doseId == -1L) return

                showMedicineNotification(
                    context = appContext,
                    doseId = doseId,
                    medicineId = medicineId,
                    medicineName = medicineName,
                    medicineDose = medicineDose,
                    profileName = profileName
                )

                ReminderManager.scheduleEscalationCheck(appContext, doseId, medicineName, profileName)
            }

            ReminderManager.ACTION_ESCALATE -> {
                val doseId = intent.getLongExtra(ReminderManager.EXTRA_DOSE_ID, -1L)
                val medicineName = intent.getStringExtra(ReminderManager.EXTRA_MEDICINE_NAME) ?: "Medicine"
                val profileName = intent.getStringExtra(ReminderManager.EXTRA_PROFILE_NAME) ?: "Patient"

                if (doseId == -1L) return

                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance(appContext)
                    val dose = db.doseLogDao().getDoseById(doseId)
                    if (dose != null && dose.status == "PENDING") {
                        db.doseLogDao().updateDoseStatus(doseId, "MISSED", System.currentTimeMillis())
                        showMissedNotification(appContext, doseId, medicineName, profileName)
                    }
                }
            }
        }
    }

    private fun showMedicineNotification(
        context: Context,
        doseId: Long,
        medicineId: Long,
        medicineName: String,
        medicineDose: String,
        profileName: String
    ) {
        ReminderManager.initNotificationChannels(context)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "today")
            putExtra("highlight_dose_id", doseId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            doseId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_TAKE
            putExtra(ReminderManager.EXTRA_DOSE_ID, doseId)
            putExtra(ReminderManager.EXTRA_MEDICINE_ID, medicineId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            (doseId * 10 + 1).toInt(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(ReminderManager.EXTRA_DOSE_ID, doseId)
            putExtra(ReminderManager.EXTRA_MEDICINE_ID, medicineId)
            putExtra(ReminderManager.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(ReminderManager.EXTRA_MEDICINE_DOSE, medicineDose)
            putExtra(ReminderManager.EXTRA_PROFILE_NAME, profileName)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (doseId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SKIP
            putExtra(ReminderManager.EXTRA_DOSE_ID, doseId)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (doseId * 10 + 3).toInt(),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ReminderManager.CHANNEL_REMINDERS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to take medicine: $medicineName")
            .setContentText("For $profileName • $medicineDose")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Taken", takenPendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "Snooze 10m", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Skip", skipPendingIntent)
            .setFullScreenIntent(contentPendingIntent, true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(doseId.toInt(), notification)
    }

    private fun showMissedNotification(
        context: Context,
        doseId: Long,
        medicineName: String,
        profileName: String
    ) {
        val notification = NotificationCompat.Builder(context, ReminderManager.CHANNEL_REMINDERS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Missed Dose Alert: $medicineName")
            .setContentText("A scheduled dose for $profileName was not logged.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((doseId + 100000).toInt(), notification)
    }
}
