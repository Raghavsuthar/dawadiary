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
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val activeMeds = db.medicineDao().getAllActiveMedicinesWithScheduleList()
            val existingProfiles = db.profileDao().getAllProfiles()
            val profileMap = existingProfiles.associateBy { it.id }

            for (medWithSched in activeMeds) {
                val med = medWithSched.medicine
                val profile = profileMap[med.profileId] ?: continue

                for (sched in medWithSched.schedules) {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent)
            }
        } catch (e: Exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent)
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
