package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.room.withTransaction
import com.example.data.AppDatabase
import com.example.data.entity.*
import com.example.reminder.ReminderManager
import com.example.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

sealed class BackupResult {
    data class Success(
        val message: String,
        val profilesCount: Int,
        val medicinesCount: Int,
        val recordsCount: Int,
        val attachmentsCount: Int
    ) : BackupResult()

    data class Failure(val error: String) : BackupResult()
}

object BackupManager {

    private const val BACKUP_VERSION = 2

    suspend fun createEncryptedBackup(
        context: Context,
        password: String,
        destinationUri: Uri
    ): BackupResult {
        return withContext(Dispatchers.IO) {
            try {
                if (password.length < 6) {
                    return@withContext BackupResult.Failure("Password must be at least 6 characters long.")
                }

                val db = AppDatabase.getInstance(context)
                val rootJson = JSONObject().apply {
                    put("version", BACKUP_VERSION)
                    put("exportedAt", System.currentTimeMillis())
                    put("appName", "DawaDiary")
                }

                val dataJson = JSONObject()

                // 1. Profiles
                val profiles = db.profileDao().getAllProfiles()
                val profilesArr = JSONArray()
                for (p in profiles) {
                    profilesArr.put(JSONObject().apply {
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
                dataJson.put("profiles", profilesArr)

                // 2. Medicines
                val medicines = db.medicineDao().getAllMedicinesList()
                val medicinesArr = JSONArray()
                for (m in medicines) {
                    medicinesArr.put(JSONObject().apply {
                        put("id", m.id)
                        put("profileId", m.profileId)
                        put("name", m.name)
                        put("strength", m.strength)
                        put("form", m.form)
                        put("doseDescription", m.doseDescription)
                        put("foodInstruction", m.foodInstruction)
                        put("prescribingDoctor", m.prescribingDoctor)
                        put("startDate", m.startDate)
                        if (m.endDate != null) put("endDate", m.endDate)
                        put("stockCount", m.stockCount)
                        put("lowStockThreshold", m.lowStockThreshold)
                        put("isActive", m.isActive)
                        put("createdAt", m.createdAt)
                    })
                }
                dataJson.put("medicines", medicinesArr)

                // 3. Schedules
                val schedules = db.scheduleDao().getAllSchedulesList()
                val schedulesArr = JSONArray()
                for (s in schedules) {
                    schedulesArr.put(JSONObject().apply {
                        put("id", s.id)
                        put("medicineId", s.medicineId)
                        put("scheduleType", s.scheduleType)
                        put("timesOfDay", s.timesOfDay)
                        put("daysOfWeekMask", s.daysOfWeekMask)
                        put("intervalDays", s.intervalDays)
                        if (s.taperingEndDate != null) put("taperingEndDate", s.taperingEndDate)
                        put("isActive", s.isActive)
                    })
                }
                dataJson.put("schedules", schedulesArr)

                // 4. Dose Logs
                val doseLogs = db.doseLogDao().getAllDoseLogsList()
                val doseLogsArr = JSONArray()
                for (d in doseLogs) {
                    doseLogsArr.put(JSONObject().apply {
                        put("id", d.id)
                        put("profileId", d.profileId)
                        put("medicineId", d.medicineId)
                        if (d.scheduleId != null) put("scheduleId", d.scheduleId)
                        put("scheduledDateTime", d.scheduledDateTime)
                        if (d.actualActionDateTime != null) put("actualActionDateTime", d.actualActionDateTime)
                        put("status", d.status)
                        put("notes", d.notes)
                    })
                }
                dataJson.put("doseLogs", doseLogsArr)

                // 5. Records
                val records = db.recordDao().getAllRecordsList()
                val recordsArr = JSONArray()
                for (r in records) {
                    recordsArr.put(JSONObject().apply {
                        put("id", r.id)
                        put("profileId", r.profileId)
                        put("title", r.title)
                        put("recordType", r.recordType)
                        put("date", r.date)
                        put("doctorHospital", r.doctorHospital)
                        put("notes", r.notes)
                        put("createdAt", r.createdAt)
                    })
                }
                dataJson.put("records", recordsArr)

                // 6. Record Files
                val recordFiles = db.recordDao().getAllRecordFilesList()
                val recordFilesArr = JSONArray()
                for (rf in recordFiles) {
                    recordFilesArr.put(JSONObject().apply {
                        put("id", rf.id)
                        put("recordId", rf.recordId)
                        put("fileName", rf.fileName)
                        put("mimeType", rf.mimeType)
                        put("encryptedFilePath", File(rf.encryptedFilePath).name) // relative name
                        put("fileSizeBytes", rf.fileSizeBytes)
                        put("createdAt", rf.createdAt)
                    })
                }
                dataJson.put("recordFiles", recordFilesArr)

                // 7. Tags & CrossRefs
                val tags = db.recordDao().getAllTagsList()
                val tagsArr = JSONArray()
                for (t in tags) {
                    tagsArr.put(JSONObject().apply {
                        put("id", t.id)
                        put("name", t.name)
                    })
                }
                dataJson.put("tags", tagsArr)

                val crossRefs = db.recordDao().getAllRecordTagCrossRefs()
                val crossRefsArr = JSONArray()
                for (cr in crossRefs) {
                    crossRefsArr.put(JSONObject().apply {
                        put("recordId", cr.recordId)
                        put("tagId", cr.tagId)
                    })
                }
                dataJson.put("recordTagCrossRefs", crossRefsArr)

                // 8. Vitals
                val vitals = db.vitalDao().getAllVitalsList()
                val vitalsArr = JSONArray()
                for (v in vitals) {
                    vitalsArr.put(JSONObject().apply {
                        put("id", v.id)
                        put("profileId", v.profileId)
                        put("vitalType", v.vitalType)
                        put("valuePrimary", v.valuePrimary)
                        if (v.valueSecondary != null) put("valueSecondary", v.valueSecondary)
                        put("unit", v.unit)
                        put("measuredAt", v.measuredAt)
                        put("notes", v.notes)
                    })
                }
                dataJson.put("vitals", vitalsArr)

                // 9. Appointments
                val appointments = db.appointmentDao().getAllAppointmentsList()
                val appointmentsArr = JSONArray()
                for (a in appointments) {
                    appointmentsArr.put(JSONObject().apply {
                        put("id", a.id)
                        put("profileId", a.profileId)
                        put("doctorName", a.doctorName)
                        put("specialty", a.specialty)
                        put("clinicHospital", a.clinicHospital)
                        put("appointmentDateTime", a.appointmentDateTime)
                        put("notes", a.notes)
                        put("isCompleted", a.isCompleted)
                    })
                }
                dataJson.put("appointments", appointmentsArr)

                rootJson.put("data", dataJson)

                // 10. Attachment File Payloads
                val attachmentsArr = JSONArray()
                val recordsDir = FileManager.getRecordsDir(context)
                var attachmentsCount = 0
                for (rf in recordFiles) {
                    val file = File(recordsDir, File(rf.encryptedFilePath).name)
                    if (file.exists() && file.isFile) {
                        val fileBytes = file.readBytes()
                        attachmentsArr.put(JSONObject().apply {
                            put("relativeFileName", file.name)
                            put("mimeType", rf.mimeType)
                            put("base64Payload", Base64.encodeToString(fileBytes, Base64.NO_WRAP))
                        })
                        attachmentsCount++
                    }
                }
                rootJson.put("attachments", attachmentsArr)

                // Encrypt whole payload
                val plainBytes = rootJson.toString().toByteArray(Charsets.UTF_8)
                val encryptedBytes = CryptoManager.encryptBackup(plainBytes, password)

                context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                    out.write(encryptedBytes)
                } ?: return@withContext BackupResult.Failure("Cannot write to destination URI.")

                BackupResult.Success(
                    message = "Backup created successfully.",
                    profilesCount = profiles.size,
                    medicinesCount = medicines.size,
                    recordsCount = records.size,
                    attachmentsCount = attachmentsCount
                )
            } catch (t: Throwable) {
                BackupResult.Failure(t.message ?: "Failed to create backup.")
            }
        }
    }

    suspend fun restoreEncryptedBackup(
        context: Context,
        password: String,
        sourceUri: Uri
    ): BackupResult {
        return withContext(Dispatchers.IO) {
            val stageDir = File(context.cacheDir, "restore_stage_${System.currentTimeMillis()}")
            try {
                val encBytes = context.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() }
                    ?: return@withContext BackupResult.Failure("Could not read backup file from selected location.")

                // 1. Authenticated Decryption
                val decryptedBytes = try {
                    CryptoManager.decryptBackup(encBytes, password)
                } catch (t: Throwable) {
                    return@withContext BackupResult.Failure("Incorrect password or corrupted backup file.")
                }

                // 2. JSON Validation
                val jsonStr = String(decryptedBytes, Charsets.UTF_8)
                val rootJson = try {
                    JSONObject(jsonStr)
                } catch (t: Throwable) {
                    return@withContext BackupResult.Failure("Malformed backup file structure.")
                }

                val version = rootJson.optInt("version", -1)
                if (version != 2 && version != 1) {
                    return@withContext BackupResult.Failure("Unsupported backup format version ($version).")
                }

                val dataJson = rootJson.optJSONObject("data")
                    ?: return@withContext BackupResult.Failure("Missing data payload in backup.")

                val profilesArr = dataJson.optJSONArray("profiles") ?: JSONArray()
                val medicinesArr = dataJson.optJSONArray("medicines") ?: JSONArray()
                val schedulesArr = dataJson.optJSONArray("schedules") ?: JSONArray()
                val doseLogsArr = dataJson.optJSONArray("doseLogs") ?: JSONArray()
                val recordsArr = dataJson.optJSONArray("records") ?: JSONArray()
                val recordFilesArr = dataJson.optJSONArray("recordFiles") ?: JSONArray()
                val tagsArr = dataJson.optJSONArray("tags") ?: JSONArray()
                val crossRefsArr = dataJson.optJSONArray("recordTagCrossRefs") ?: JSONArray()
                val vitalsArr = dataJson.optJSONArray("vitals") ?: JSONArray()
                val appointmentsArr = dataJson.optJSONArray("appointments") ?: JSONArray()

                val attachmentsArr = rootJson.optJSONArray("attachments") ?: JSONArray()

                // Validate Profiles
                val restoredProfiles = mutableListOf<ProfileEntity>()
                val profileIdSet = mutableSetOf<Long>()
                for (i in 0 until profilesArr.length()) {
                    val p = profilesArr.getJSONObject(i)
                    val id = p.getLong("id")
                    val name = p.getString("name").trim()
                    if (name.isBlank()) {
                        return@withContext BackupResult.Failure("Backup contains an invalid profile without a name.")
                    }
                    profileIdSet.add(id)
                    restoredProfiles.add(
                        ProfileEntity(
                            id = id,
                            name = name,
                            relationship = p.optString("relationship", "Other"),
                            dob = p.optString("dob", ""),
                            gender = p.optString("gender", ""),
                            bloodGroup = p.optString("bloodGroup", ""),
                            allergies = p.optString("allergies", ""),
                            chronicConditions = p.optString("chronicConditions", ""),
                            emergencyContactName = p.optString("emergencyContactName", ""),
                            emergencyContactPhone = p.optString("emergencyContactPhone", ""),
                            isSelf = p.optBoolean("isSelf", false),
                            createdAt = p.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }

                // Validate Medicines
                val restoredMedicines = mutableListOf<MedicineEntity>()
                val medicineIdSet = mutableSetOf<Long>()
                for (i in 0 until medicinesArr.length()) {
                    val m = medicinesArr.getJSONObject(i)
                    val id = m.getLong("id")
                    val profileId = m.getLong("profileId")
                    if (profileIdSet.isNotEmpty() && !profileIdSet.contains(profileId)) {
                        return@withContext BackupResult.Failure("Data integrity violation: Medicine $id references non-existent profile $profileId.")
                    }
                    val name = m.getString("name").trim()
                    if (name.isBlank()) {
                        return@withContext BackupResult.Failure("Backup contains invalid medicine without a name.")
                    }
                    medicineIdSet.add(id)
                    restoredMedicines.add(
                        MedicineEntity(
                            id = id,
                            profileId = profileId,
                            name = name,
                            strength = m.optString("strength", ""),
                            form = m.optString("form", "Tablet"),
                            doseDescription = m.optString("doseDescription", "1 tablet"),
                            foodInstruction = m.optString("foodInstruction", "ANY_TIME"),
                            prescribingDoctor = m.optString("prescribingDoctor", ""),
                            startDate = m.optString("startDate", ""),
                            endDate = if (m.has("endDate")) m.getString("endDate") else null,
                            stockCount = m.optInt("stockCount", 0),
                            lowStockThreshold = m.optInt("lowStockThreshold", 5),
                            isActive = m.optBoolean("isActive", true),
                            createdAt = m.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }

                // Validate Schedules
                val restoredSchedules = mutableListOf<ScheduleEntity>()
                for (i in 0 until schedulesArr.length()) {
                    val s = schedulesArr.getJSONObject(i)
                    val medId = s.getLong("medicineId")
                    if (medicineIdSet.isNotEmpty() && !medicineIdSet.contains(medId)) {
                        return@withContext BackupResult.Failure("Data integrity violation: Schedule references missing medicine $medId.")
                    }
                    restoredSchedules.add(
                        ScheduleEntity(
                            id = s.optLong("id", 0),
                            medicineId = medId,
                            scheduleType = s.optString("scheduleType", "DAILY"),
                            timesOfDay = s.optString("timesOfDay", "08:00"),
                            daysOfWeekMask = s.optInt("daysOfWeekMask", 127),
                            intervalDays = s.optInt("intervalDays", 1),
                            taperingEndDate = if (s.has("taperingEndDate")) s.getString("taperingEndDate") else null,
                            isActive = s.optBoolean("isActive", true)
                        )
                    )
                }

                // Validate Dose Logs
                val restoredDoseLogs = mutableListOf<DoseLogEntity>()
                for (i in 0 until doseLogsArr.length()) {
                    val d = doseLogsArr.getJSONObject(i)
                    val profId = d.getLong("profileId")
                    val medId = d.getLong("medicineId")
                    if (profileIdSet.isNotEmpty() && !profileIdSet.contains(profId)) continue
                    if (medicineIdSet.isNotEmpty() && !medicineIdSet.contains(medId)) continue
                    restoredDoseLogs.add(
                        DoseLogEntity(
                            id = d.optLong("id", 0),
                            profileId = profId,
                            medicineId = medId,
                            scheduleId = if (d.has("scheduleId")) d.getLong("scheduleId") else null,
                            scheduledDateTime = d.getLong("scheduledDateTime"),
                            actualActionDateTime = if (d.has("actualActionDateTime")) d.getLong("actualActionDateTime") else null,
                            status = d.optString("status", "PENDING"),
                            notes = d.optString("notes", "")
                        )
                    )
                }

                // Validate Records
                val restoredRecords = mutableListOf<RecordEntity>()
                val recordIdSet = mutableSetOf<Long>()
                for (i in 0 until recordsArr.length()) {
                    val r = recordsArr.getJSONObject(i)
                    val id = r.getLong("id")
                    val profId = r.getLong("profileId")
                    if (profileIdSet.isNotEmpty() && !profileIdSet.contains(profId)) continue
                    recordIdSet.add(id)
                    restoredRecords.add(
                        RecordEntity(
                            id = id,
                            profileId = profId,
                            title = r.optString("title", "Medical Record"),
                            date = r.optString("date", ""),
                            doctorHospital = r.optString("doctorHospital", r.optString("doctorOrHospital", "")),
                            recordType = r.optString("recordType", r.optString("category", "Prescription")),
                            notes = r.optString("notes", ""),
                            createdAt = r.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }

                // Validate Record Files
                val recordsDir = FileManager.getRecordsDir(context)
                stageDir.mkdirs()
                val stagedFiles = mutableMapOf<String, File>()

                // Stage Attachments
                for (i in 0 until attachmentsArr.length()) {
                    val att = attachmentsArr.getJSONObject(i)
                    val relName = att.getString("relativeFileName").replace(Regex("[^a-zA-Z0-9._-]"), "_")
                    val base64 = att.getString("base64Payload")
                    val bytes = try {
                        Base64.decode(base64, Base64.NO_WRAP)
                    } catch (t: Throwable) {
                        return@withContext BackupResult.Failure("Corrupted attachment payload for $relName.")
                    }
                    val targetFile = File(stageDir, relName)
                    FileOutputStream(targetFile).use { it.write(bytes) }
                    stagedFiles[relName] = targetFile
                }

                val restoredRecordFiles = mutableListOf<RecordFileEntity>()
                for (i in 0 until recordFilesArr.length()) {
                    val rf = recordFilesArr.getJSONObject(i)
                    val recId = rf.getLong("recordId")
                    if (recordIdSet.isNotEmpty() && !recordIdSet.contains(recId)) continue
                    val relName = File(rf.getString("encryptedFilePath")).name
                    val finalDest = File(recordsDir, relName)
                    restoredRecordFiles.add(
                        RecordFileEntity(
                            id = rf.optLong("id", 0),
                            recordId = recId,
                            fileName = rf.optString("fileName", "Document"),
                            mimeType = rf.optString("mimeType", "application/octet-stream"),
                            encryptedFilePath = finalDest.absolutePath,
                            fileSizeBytes = rf.optLong("fileSizeBytes", 0L),
                            createdAt = rf.optLong("createdAt", rf.optLong("uploadedAt", System.currentTimeMillis()))
                        )
                    )
                }

                // Tags and Vitals and Appointments
                val restoredTags = mutableListOf<TagEntity>()
                for (i in 0 until tagsArr.length()) {
                    val t = tagsArr.getJSONObject(i)
                    restoredTags.add(TagEntity(id = t.optLong("id", 0), name = t.getString("name")))
                }

                val restoredCrossRefs = mutableListOf<RecordTagCrossRef>()
                for (i in 0 until crossRefsArr.length()) {
                    val cr = crossRefsArr.getJSONObject(i)
                    restoredCrossRefs.add(
                        RecordTagCrossRef(recordId = cr.getLong("recordId"), tagId = cr.getLong("tagId"))
                    )
                }

                val restoredVitals = mutableListOf<VitalEntity>()
                for (i in 0 until vitalsArr.length()) {
                    val v = vitalsArr.getJSONObject(i)
                    val profId = v.getLong("profileId")
                    if (profileIdSet.isNotEmpty() && !profileIdSet.contains(profId)) continue
                    restoredVitals.add(
                        VitalEntity(
                            id = v.optLong("id", 0),
                            profileId = profId,
                            vitalType = v.optString("vitalType", "BLOOD_PRESSURE"),
                            valuePrimary = v.optDouble("valuePrimary", 0.0),
                            valueSecondary = if (v.has("valueSecondary")) v.getDouble("valueSecondary") else null,
                            unit = v.optString("unit", ""),
                            measuredAt = v.optLong("measuredAt", System.currentTimeMillis()),
                            notes = v.optString("notes", "")
                        )
                    )
                }

                val restoredAppointments = mutableListOf<AppointmentEntity>()
                for (i in 0 until appointmentsArr.length()) {
                    val a = appointmentsArr.getJSONObject(i)
                    val profId = a.getLong("profileId")
                    if (profileIdSet.isNotEmpty() && !profileIdSet.contains(profId)) continue
                    restoredAppointments.add(
                        AppointmentEntity(
                            id = a.optLong("id", 0),
                            profileId = profId,
                            doctorName = a.optString("doctorName", ""),
                            specialty = a.optString("specialty", ""),
                            clinicHospital = a.optString("clinicHospital", ""),
                            appointmentDateTime = a.optLong("appointmentDateTime", System.currentTimeMillis()),
                            notes = a.optString("notes", ""),
                            isCompleted = a.optBoolean("isCompleted", false)
                        )
                    )
                }

                // 3. ATOMIC TRANSACTIONAL DATABASE UPDATE
                val db = AppDatabase.getInstance(context)
                db.withTransaction {
                    // Wipe existing tables
                    db.doseLogDao().deleteAllDoseLogs()
                    db.scheduleDao().deleteAllSchedules()
                    db.medicineDao().deleteAllMedicines()
                    db.recordDao().deleteAllRecordFiles()
                    db.recordDao().deleteAllRecordTags()
                    db.recordDao().deleteAllRecords()
                    db.recordDao().deleteAllTags()
                    db.vitalDao().deleteAllVitals()
                    db.appointmentDao().deleteAllAppointments()
                    db.profileDao().deleteAllProfiles()

                    // Insert restored records
                    if (restoredProfiles.isNotEmpty()) db.profileDao().insertProfiles(restoredProfiles)
                    if (restoredMedicines.isNotEmpty()) db.medicineDao().insertMedicines(restoredMedicines)
                    if (restoredSchedules.isNotEmpty()) db.scheduleDao().insertSchedules(restoredSchedules)
                    if (restoredDoseLogs.isNotEmpty()) db.doseLogDao().insertDoseLogs(restoredDoseLogs)
                    if (restoredRecords.isNotEmpty()) db.recordDao().insertRecords(restoredRecords)
                    if (restoredRecordFiles.isNotEmpty()) db.recordDao().insertRecordFiles(restoredRecordFiles)
                    if (restoredTags.isNotEmpty()) db.recordDao().insertTags(restoredTags)
                    if (restoredCrossRefs.isNotEmpty()) db.recordDao().insertRecordTagCrossRefs(restoredCrossRefs)
                    if (restoredVitals.isNotEmpty()) db.vitalDao().insertVitals(restoredVitals)
                    if (restoredAppointments.isNotEmpty()) db.appointmentDao().insertAppointments(restoredAppointments)
                }

                // 4. Move staged attachments into recordsDir
                for ((relName, stagedFile) in stagedFiles) {
                    val destFile = File(recordsDir, relName)
                    stagedFile.copyTo(destFile, overwrite = true)
                }

                // 5. Re-schedule alarms
                ReminderManager.syncAndScheduleAlarms(context)

                BackupResult.Success(
                    message = "Restore completed successfully.",
                    profilesCount = restoredProfiles.size,
                    medicinesCount = restoredMedicines.size,
                    recordsCount = restoredRecords.size,
                    attachmentsCount = stagedFiles.size
                )
            } catch (t: Throwable) {
                BackupResult.Failure(t.message ?: "Failed to restore backup.")
            } finally {
                // Clean up stage dir
                if (stageDir.exists()) {
                    stageDir.deleteRecursively()
                }
            }
        }
    }
}
