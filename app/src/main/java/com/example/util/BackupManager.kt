package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.AppDatabase
import com.example.data.entity.*
import com.example.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream

object BackupManager {

    suspend fun createEncryptedBackup(context: Context, destinationUri: Uri, password: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(context)
                val rootJson = JSONObject()

                val profiles = db.profileDao().getAllProfiles()
                val profilesArray = JSONArray()
                for (p in profiles) {
                    profilesArray.put(JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("relationship", p.relationship)
                        put("dob", p.dob)
                        put("gender", p.gender)
                        put("bloodGroup", p.bloodGroup)
                        put("allergies", p.allergies)
                        put("chronicConditions", p.chronicConditions)
                        put("emergencyContactName", p.emergencyContactName)
                        put("emergencyContactPhone", p.emergencyContactPhone)
                        put("isSelf", p.isSelf)
                        put("createdAt", p.createdAt)
                    })
                }
                rootJson.put("profiles", profilesArray)

                val medsWithSched = db.medicineDao().getAllActiveMedicinesWithScheduleList()
                val medsArray = JSONArray()
                val schedArray = JSONArray()
                for (ms in medsWithSched) {
                    val m = ms.medicine
                    medsArray.put(JSONObject().apply {
                        put("id", m.id)
                        put("profileId", m.profileId)
                        put("name", m.name)
                        put("strength", m.strength)
                        put("form", m.form)
                        put("doseDescription", m.doseDescription)
                        put("foodInstruction", m.foodInstruction)
                        put("prescribingDoctor", m.prescribingDoctor)
                        put("startDate", m.startDate)
                        put("endDate", m.endDate ?: "")
                        put("stockCount", m.stockCount)
                        put("lowStockThreshold", m.lowStockThreshold)
                        put("isActive", m.isActive)
                        put("createdAt", m.createdAt)
                    })
                    for (s in ms.schedules) {
                        schedArray.put(JSONObject().apply {
                            put("id", s.id)
                            put("medicineId", s.medicineId)
                            put("scheduleType", s.scheduleType)
                            put("timesOfDay", s.timesOfDay)
                            put("daysOfWeekMask", s.daysOfWeekMask)
                            put("intervalDays", s.intervalDays)
                            put("taperingEndDate", s.taperingEndDate ?: "")
                            put("isActive", s.isActive)
                        })
                    }
                }
                rootJson.put("medicines", medsArray)
                rootJson.put("schedules", schedArray)

                val recordsDir = FileManager.getRecordsDir(context)
                val filesJsonArray = JSONArray()
                if (recordsDir.exists()) {
                    recordsDir.listFiles()?.forEach { file ->
                        if (file.isFile) {
                            val base64Content = android.util.Base64.encodeToString(file.readBytes(), android.util.Base64.NO_WRAP)
                            filesJsonArray.put(JSONObject().apply {
                                put("name", file.name)
                                put("data", base64Content)
                            })
                        }
                    }
                }
                rootJson.put("record_files_blob", filesJsonArray)

                val rawBytes = rootJson.toString().toByteArray(Charsets.UTF_8)
                val encryptedBackup = CryptoManager.encryptBackup(rawBytes, password)

                context.contentResolver.openOutputStream(destinationUri)?.use { os ->
                    os.write(encryptedBackup)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    suspend fun restoreEncryptedBackup(context: Context, sourceUri: Uri, password: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream = context.contentResolver.openInputStream(sourceUri)
                    ?: return@withContext false

                val encryptedBytes = inputStream.use { it.readBytes() }
                val decryptedBytes = CryptoManager.decryptBackup(encryptedBytes, password)
                val rootJson = JSONObject(String(decryptedBytes, Charsets.UTF_8))

                val db = AppDatabase.getInstance(context)

                val profilesArray = rootJson.optJSONArray("profiles") ?: JSONArray()
                for (i in 0 until profilesArray.length()) {
                    val p = profilesArray.getJSONObject(i)
                    db.profileDao().insertProfile(
                        ProfileEntity(
                            id = p.optLong("id", 0L),
                            name = p.getString("name"),
                            relationship = p.optString("relationship", "Other"),
                            dob = p.optString("dob", ""),
                            gender = p.optString("gender", "Other"),
                            bloodGroup = p.optString("bloodGroup", "Unknown"),
                            allergies = p.optString("allergies", ""),
                            chronicConditions = p.optString("chronicConditions", ""),
                            emergencyContactName = p.optString("emergencyContactName", ""),
                            emergencyContactPhone = p.optString("emergencyContactPhone", ""),
                            isSelf = p.optBoolean("isSelf", false),
                            createdAt = p.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }

                val medsArray = rootJson.optJSONArray("medicines") ?: JSONArray()
                for (i in 0 until medsArray.length()) {
                    val m = medsArray.getJSONObject(i)
                    db.medicineDao().insertMedicine(
                        MedicineEntity(
                            id = m.optLong("id", 0L),
                            profileId = m.getLong("profileId"),
                            name = m.getString("name"),
                            strength = m.optString("strength", ""),
                            form = m.optString("form", "Tablet"),
                            doseDescription = m.optString("doseDescription", "1 tablet"),
                            foodInstruction = m.optString("foodInstruction", "ANY_TIME"),
                            prescribingDoctor = m.optString("prescribingDoctor", ""),
                            startDate = m.optString("startDate", ""),
                            endDate = if (m.optString("endDate").isNotEmpty()) m.optString("endDate") else null,
                            stockCount = m.optInt("stockCount", 0),
                            lowStockThreshold = m.optInt("lowStockThreshold", 5),
                            isActive = m.optBoolean("isActive", true),
                            createdAt = m.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }

                val schedArray = rootJson.optJSONArray("schedules") ?: JSONArray()
                for (i in 0 until schedArray.length()) {
                    val s = schedArray.getJSONObject(i)
                    db.scheduleDao().insertSchedule(
                        ScheduleEntity(
                            id = s.optLong("id", 0L),
                            medicineId = s.getLong("medicineId"),
                            scheduleType = s.optString("scheduleType", "DAILY"),
                            timesOfDay = s.optString("timesOfDay", "08:00"),
                            daysOfWeekMask = s.optInt("daysOfWeekMask", 127),
                            intervalDays = s.optInt("intervalDays", 1),
                            taperingEndDate = if (s.optString("taperingEndDate").isNotEmpty()) s.optString("taperingEndDate") else null,
                            isActive = s.optBoolean("isActive", true)
                        )
                    )
                }

                val filesArray = rootJson.optJSONArray("record_files_blob") ?: JSONArray()
                val recordsDir = FileManager.getRecordsDir(context)
                for (i in 0 until filesArray.length()) {
                    val f = filesArray.getJSONObject(i)
                    val fileName = f.getString("name")
                    val data = android.util.Base64.decode(f.getString("data"), android.util.Base64.NO_WRAP)
                    val targetFile = File(recordsDir, fileName)
                    targetFile.writeBytes(data)
                }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
}
