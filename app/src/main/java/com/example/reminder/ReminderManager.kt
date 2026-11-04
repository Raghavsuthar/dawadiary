package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.entity.DoseLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object ReminderManager {

    private val scheduleSyncMutex = Mutex()

    const val CHANNEL_ID = "dawadiary_reminders_channel"
    const val CHANNEL_NAME = "Medication Dose Reminders"
    const val NOTIFICATION_ID_PREFIX = 1000

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority reminders for scheduled medication doses"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    suspend fun <T> withScheduleSyncLock(block: suspend () -> T): T = scheduleSyncMutex.withLock { block() }

    suspend fun syncAndScheduleAlarms(context: Context) = scheduleSyncMutex.withLock {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val today = LocalDate.now()
            val planningDates = ReminderSchedulePlanner.planningDates(today)
            val activeMeds = db.medicineDao().getAllActiveMedicinesWithScheduleList()
            val profileMap = db.profileDao().getAllProfiles().associateBy { it.id }

            for (medWithSched in activeMeds) {
                val med = medWithSched.medicine
                if (!med.isActive) continue
                val startDate = try {
                    if (med.startDate.isNotBlank()) LocalDate.parse(med.startDate) else today
                } catch (_: Exception) {
                    today
                }
                val endDate = med.endDate?.takeIf { it.isNotBlank() }?.let {
                    try { LocalDate.parse(it) } catch (_: Exception) { null }
                }
                val profile = profileMap[med.profileId] ?: continue

                for (sched in medWithSched.schedules) {
                    if (!sched.isActive || sched.scheduleType == "AS_NEEDED") continue
                    val taperingEnd = sched.taperingEndDate?.takeIf { it.isNotBlank() }?.let {
                        try { LocalDate.parse(it) } catch (_: Exception) { null }
                    }

                    for (scheduleDate in planningDates) {
                        if (scheduleDate.isBefore(startDate) || (endDate != null && scheduleDate.isAfter(endDate))) continue
                        if (taperingEnd != null && scheduleDate.isAfter(taperingEnd)) continue

                        if (!ReminderSchedulePlanner.appliesOnDate(
                                scheduleType = sched.scheduleType,
                                date = scheduleDate,
                                startDate = startDate,
                                daysOfWeekMask = sched.daysOfWeekMask,
                                intervalDays = sched.intervalDays
                            )) continue

                        for (timeStr in sched.timesOfDay.split(",").map { it.trim() }) {
                            try {
                                val parts = timeStr.split(":")
                                if (parts.size != 2) continue
                                val scheduledAt = scheduleDate.atTime(parts[0].toInt(), parts[1].toInt())
                                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                val existing = db.doseLogDao().findExistingDose(med.id, scheduledAt)
                                val doseId = existing?.id ?: db.doseLogDao().insertDoseLog(
                                    DoseLogEntity(
                                        profileId = med.profileId,
                                        medicineId = med.id,
                                        scheduleId = sched.id,
                                        scheduledDateTime = scheduledAt,
                                        status = "PENDING"
                                    )
                                )
                                val now = System.currentTimeMillis()
                                when (existing?.status ?: "PENDING") {
                                    "PENDING" -> if (scheduledAt > now) {
                                        scheduleExactAlarm(context, doseId, med.id, med.name, med.doseDescription, profile.name, scheduledAt)
                                    } else {
                                        cancelAlarmForDose(context, doseId)
                                    }
                                    "SNOOZED" -> {
                                        val snoozeAt = (existing?.actualActionDateTime ?: now) + 10 * 60 * 1000L
                                        if (snoozeAt > now) {
                                            scheduleExactAlarm(context, doseId, med.id, med.name, med.doseDescription, profile.name, snoozeAt)
                                        } else {
                                            cancelAlarmForDose(context, doseId)
                                        }
                                    }
                                    else -> cancelAlarmForDose(context, doseId)
                                }
                            } catch (_: Exception) {
                                // Ignore malformed legacy schedule values; form validation prevents new invalid times.
                            }
                        }
                    }
                }
            }
        }
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleExactAlarm(
        context: Context,
        doseId: Long,
        medicineId: Long,
        medicineName: String,
        doseDesc: String,
        profileName: String,
        scheduledTime: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("dose_id", doseId)
            putExtra("medicine_id", medicineId)
            putExtra("medicine_name", medicineName)
            putExtra("dose_desc", doseDesc)
            putExtra("profile_name", profileName)
            putExtra("scheduled_time", scheduledTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            doseId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent)
            }
        } catch (e: Exception) {
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent)
            } catch (ignored: Exception) {}
        }
    }

    fun cancelAlarmForDose(context: Context, doseId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                doseId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        } catch (ignored: Exception) {}
    }

    suspend fun cancelAlarmsForMedicine(context: Context, medicineId: Long) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val doses = db.doseLogDao().getDosesForMedicine(medicineId)
            for (d in doses) {
                if (d.status == "PENDING" || d.status == "SNOOZED") {
                    cancelAlarmForDose(context, d.id)
                }
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
        val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000 // 10 minutes
        scheduleExactAlarm(context, doseId, medicineId, medicineName, doseDesc, profileName, snoozeTime)
    }
}
