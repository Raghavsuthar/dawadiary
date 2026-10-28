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
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object ReminderManager {

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

    suspend fun syncAndScheduleAlarms(context: Context) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val today = LocalDate.now()

            val activeMeds = db.medicineDao().getAllActiveMedicinesWithScheduleList()
            val existingProfiles = db.profileDao().getAllProfiles()
            val profileMap = existingProfiles.associateBy { it.id }

            for (medWithSched in activeMeds) {
                val med = medWithSched.medicine
                if (!med.isActive) continue

                // Check start and end date validity
                val startDate = try {
                    if (med.startDate.isNotBlank()) LocalDate.parse(med.startDate) else today
                } catch (e: Exception) {
                    today
                }

                if (today.isBefore(startDate)) continue

                if (!med.endDate.isNullOrBlank()) {
                    try {
                        val endDate = LocalDate.parse(med.endDate)
                        if (today.isAfter(endDate)) continue
                    } catch (e: Exception) {
                        // ignore malformed date
                    }
                }

                val profile = profileMap[med.profileId] ?: continue

                for (sched in medWithSched.schedules) {
                    if (!sched.isActive) continue

                    // Check schedule type applicability for today
                    if (sched.scheduleType == "AS_NEEDED") {
                        // PRN medicines are taken as needed, no preset daily alarms
                        continue
                    }

                    if (sched.scheduleType == "SPECIFIC_DAYS") {
                        // Day of week: 1=Monday .. 7=Sunday
                        val dayVal = today.dayOfWeek.value
                        val maskBit = 1 shl (dayVal - 1)
                        if ((sched.daysOfWeekMask and maskBit) == 0) {
                            continue
                        }
                    } else if (sched.scheduleType == "INTERVAL") {
                        val interval = if (sched.intervalDays > 0) sched.intervalDays else 1
                        val daysBetween = ChronoUnit.DAYS.between(startDate, today)
                        if (daysBetween < 0 || (daysBetween % interval) != 0L) {
                            continue
                        }
                    }

                    if (!sched.taperingEndDate.isNullOrBlank()) {
                        try {
                            val taperingEnd = LocalDate.parse(sched.taperingEndDate)
                            if (today.isAfter(taperingEnd)) continue
                        } catch (e: Exception) {
                            // ignore malformed tapering date
                        }
                    }

                    val times = sched.timesOfDay.split(",").map { it.trim() }
                    for (timeStr in times) {
                        try {
                            val parts = timeStr.split(":")
                            if (parts.size == 2) {
                                val hour = parts[0].toInt()
                                val min = parts[1].toInt()
                                val schedTime = today.atTime(hour, min).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                                val existing = db.doseLogDao().findExistingDose(med.id, schedTime)
                                val doseId = if (existing == null) {
                                    db.doseLogDao().insertDoseLog(
                                        DoseLogEntity(
                                            profileId = med.profileId,
                                            medicineId = med.id,
                                            scheduleId = sched.id,
                                            scheduledDateTime = schedTime,
                                            status = "PENDING"
                                        )
                                    )
                                } else {
                                    existing.id
                                }

                                if (schedTime > System.currentTimeMillis()) {
                                    scheduleExactAlarm(context, doseId, med.id, med.name, med.doseDescription, profile.name, schedTime)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
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
                if (d.status == "PENDING") {
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
