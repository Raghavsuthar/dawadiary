package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.entity.DoseLogEntity
import com.example.data.entity.MedicineEntity
import com.example.data.entity.ScheduleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ReminderManager {
    const val CHANNEL_REMINDERS_ID = "channel_medicine_reminders"
    const val CHANNEL_REFILLS_ID = "channel_medicine_refills"
    const val ACTION_REMINDER = "com.example.ACTION_MEDICINE_REMINDER"
    const val ACTION_ESCALATE = "com.example.ACTION_ESCALATE_MISSED"

    const val EXTRA_DOSE_ID = "extra_dose_id"
    const val EXTRA_MEDICINE_ID = "extra_medicine_id"
    const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
    const val EXTRA_MEDICINE_DOSE = "extra_medicine_dose"
    const val EXTRA_PROFILE_NAME = "extra_profile_name"
    const val EXTRA_SCHEDULED_TIME = "extra_scheduled_time"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority reminders for scheduled patient medications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setSound(
                    alarmSound,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }

            val refillChannel = NotificationChannel(
                CHANNEL_REFILLS_ID,
                "Refill Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when medication stock is running low"
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(refillChannel)
        }
    }

    fun syncAndScheduleAlarms(context: Context, targetDate: LocalDate = LocalDate.now()) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(appContext)
                val medicinesWithSchedules = db.medicineDao().getAllActiveMedicinesWithScheduleList()
                val profiles = db.profileDao().getAllProfiles().associateBy { it.id }

                val startOfDay = targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfDay = targetDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                for (medWithSched in medicinesWithSchedules) {
                    val medicine = medWithSched.medicine
                    val schedules = medWithSched.schedules

                    for (schedule in schedules) {
                        if (!schedule.isActive) continue
                        if (!isScheduleActiveOnDate(schedule, medicine, targetDate)) continue

                        val times = schedule.timesOfDay.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        for (timeStr in times) {
                            try {
                                val time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
                                val scheduledDateTime = targetDate.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                                val existing = db.doseLogDao().findExistingDose(medicine.id, scheduledDateTime)
                                val doseId = if (existing == null) {
                                    val newDose = DoseLogEntity(
                                        profileId = medicine.profileId,
                                        medicineId = medicine.id,
                                        scheduleId = schedule.id,
                                        scheduledDateTime = scheduledDateTime,
                                        status = if (scheduledDateTime < System.currentTimeMillis() - 60 * 60 * 1000) "MISSED" else "PENDING"
                                    )
                                    db.doseLogDao().insertDoseLog(newDose)
                                } else {
                                    existing.id
                                }

                                val nowMillis = System.currentTimeMillis()
                                if (scheduledDateTime > nowMillis && (existing == null || existing.status == "PENDING" || existing.status == "SNOOZED")) {
                                    val profileName = profiles[medicine.profileId]?.name ?: "Patient"
                                    scheduleExactAlarm(
                                        context = appContext,
                                        doseId = doseId,
                                        medicine = medicine,
                                        profileName = profileName,
                                        triggerAtMillis = scheduledDateTime
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("ReminderManager", "Failed parsing time: $timeStr", e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ReminderManager", "Error in syncAndScheduleAlarms", e)
            }
        }
    }

    private fun isScheduleActiveOnDate(schedule: ScheduleEntity, medicine: MedicineEntity, date: LocalDate): Boolean {
        try {
            val start = LocalDate.parse(medicine.startDate)
            if (date.isBefore(start)) return false
            if (medicine.endDate != null) {
                val end = LocalDate.parse(medicine.endDate)
                if (date.isAfter(end)) return false
            }
        } catch (_: Exception) {}

        return when (schedule.scheduleType) {
            "DAILY" -> true
            "SPECIFIC_DAYS" -> {
                val dayBit = 1 shl (date.dayOfWeek.value - 1)
                (schedule.daysOfWeekMask and dayBit) != 0
            }
            "INTERVAL" -> {
                try {
                    val start = LocalDate.parse(medicine.startDate)
                    val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(start, date)
                    val interval = if (schedule.intervalDays > 0) schedule.intervalDays else 1
                    daysDiff >= 0 && (daysDiff % interval == 0L)
                } catch (_: Exception) {
                    true
                }
            }
            "TAPERING" -> {
                if (schedule.taperingEndDate != null) {
                    try {
                        val taperEnd = LocalDate.parse(schedule.taperingEndDate)
                        !date.isAfter(taperEnd)
                    } catch (_: Exception) {
                        true
                    }
                } else true
            }
            "PRN" -> false
            else -> true
        }
    }

    fun scheduleExactAlarm(
        context: Context,
        doseId: Long,
        medicine: MedicineEntity,
        profileName: String,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_DOSE_ID, doseId)
            putExtra(EXTRA_MEDICINE_ID, medicine.id)
            putExtra(EXTRA_MEDICINE_NAME, medicine.name)
            putExtra(EXTRA_MEDICINE_DOSE, "${medicine.doseDescription} (${medicine.strength})")
            putExtra(EXTRA_PROFILE_NAME, profileName)
            putExtra(EXTRA_SCHEDULED_TIME, triggerAtMillis)
        }

        val requestCode = (doseId xor (triggerAtMillis and 0xFFFF)).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            0,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: SecurityException) {
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } catch (ex: Exception) {
                Log.e("ReminderManager", "Failed to schedule alarm", ex)
            }
        }
    }

    fun scheduleSnooze(
        context: Context,
        doseId: Long,
        medicineId: Long,
        medicineName: String,
        doseDesc: String,
        profileName: String
    ) {
        val snoozeMillis = System.currentTimeMillis() + 10 * 60 * 1000
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_DOSE_ID, doseId)
            putExtra(EXTRA_MEDICINE_ID, medicineId)
            putExtra(EXTRA_MEDICINE_NAME, medicineName)
            putExtra(EXTRA_MEDICINE_DOSE, doseDesc)
            putExtra(EXTRA_PROFILE_NAME, profileName)
            putExtra(EXTRA_SCHEDULED_TIME, snoozeMillis)
        }

        val requestCode = (doseId + 9999).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeMillis, pendingIntent)
        } catch (e: Exception) {
            Log.e("ReminderManager", "Failed to schedule snooze", e)
        }
    }

    fun scheduleEscalationCheck(
        context: Context,
        doseId: Long,
        medicineName: String,
        profileName: String
    ) {
        val escalationMillis = System.currentTimeMillis() + 30 * 60 * 1000
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ESCALATE
            putExtra(EXTRA_DOSE_ID, doseId)
            putExtra(EXTRA_MEDICINE_NAME, medicineName)
            putExtra(EXTRA_PROFILE_NAME, profileName)
        }

        val requestCode = (doseId + 55555).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, escalationMillis, pendingIntent)
        } catch (e: Exception) {
            Log.e("ReminderManager", "Failed to schedule escalation check", e)
        }
    }
}
