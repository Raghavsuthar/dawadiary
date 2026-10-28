package com.example.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.DawaDiaryApp
import com.example.MainActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val doseId = intent.getLongExtra("dose_id", -1L)
        val medicineId = intent.getLongExtra("medicine_id", -1L)
        val rawMedicineName = intent.getStringExtra("medicine_name") ?: "Medication"
        val rawDoseDesc = intent.getStringExtra("dose_desc") ?: "Scheduled dose"
        val rawProfileName = intent.getStringExtra("profile_name") ?: "Patient"

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val dose = db.doseLogDao().getDoseById(doseId)
                if (dose == null || dose.status != "PENDING") {
                    // Dose is already taken, skipped, or cancelled
                    return@launch
                }

                val medicine = db.medicineDao().getMedicineById(medicineId)
                if (medicine == null || !medicine.isActive) {
                    // Medicine was archived or deleted
                    return@launch
                }

                val app = context.applicationContext as? DawaDiaryApp
                val isPrivate = app?.securityPreferences?.isPrivateNotificationsEnabled() ?: false

                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context,
                    doseId.toInt(),
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val takeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = "ACTION_TAKE"
                    putExtra("dose_id", doseId)
                    putExtra("medicine_id", medicineId)
                }
                val takePendingIntent = PendingIntent.getBroadcast(
                    context,
                    (doseId * 10 + 1).toInt(),
                    takeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = "ACTION_SNOOZE"
                    putExtra("dose_id", doseId)
                    putExtra("medicine_id", medicineId)
                    putExtra("medicine_name", rawMedicineName)
                    putExtra("dose_desc", rawDoseDesc)
                    putExtra("profile_name", rawProfileName)
                }
                val snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    (doseId * 10 + 2).toInt(),
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Generic public version for lock screen privacy
                val publicNotification = NotificationCompat.Builder(context, ReminderManager.CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle("Dose Reminder")
                    .setContentText("Time for scheduled medication")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .build()

                val title = if (isPrivate) "Dose Reminder" else "Dose Reminder: $rawMedicineName"
                val content = if (isPrivate) "Time for scheduled medication" else "For $rawProfileName • $rawDoseDesc"

                val notification = NotificationCompat.Builder(context, ReminderManager.CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .setContentIntent(openAppPendingIntent)
                    .setPublicVersion(publicNotification)
                    .addAction(android.R.drawable.checkbox_on_background, "Take Now", takePendingIntent)
                    .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 10m", snoozePendingIntent)
                    .build()

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify((ReminderManager.NOTIFICATION_ID_PREFIX + doseId).toInt(), notification)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
