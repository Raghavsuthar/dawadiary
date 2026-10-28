package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

data class MedicineWithSchedule(
    @Embedded val medicine: MedicineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicineId"
    )
    val schedules: List<ScheduleEntity>
)

data class RecordWithFiles(
    @Embedded val record: RecordEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recordId"
    )
    val files: List<RecordFileEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RecordTagCrossRef::class,
            parentColumn = "recordId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)

data class DoseWithMedicine(
    @Embedded val doseLog: DoseLogEntity,
    @Relation(
        parentColumn = "medicineId",
        entityColumn = "id"
    )
    val medicine: MedicineEntity,
    @Relation(
        parentColumn = "profileId",
        entityColumn = "id"
    )
    val profile: ProfileEntity
)

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY isSelf DESC, name ASC")
    fun getAllProfilesFlow(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles ORDER BY isSelf DESC, name ASC")
    suspend fun getAllProfiles(): List<ProfileEntity>

    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getProfileByIdFlow(id: Long): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE isSelf = 1 LIMIT 1")
    suspend fun getSelfProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<ProfileEntity>)

    @Update
    suspend fun updateProfile(profile: ProfileEntity): Int

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles")
    suspend fun deleteAllProfiles()
}

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE profileId = :profileId AND isActive = 1 ORDER BY name ASC")
    fun getActiveMedicinesForProfile(profileId: Long): Flow<List<MedicineEntity>>

    @Transaction
    @Query("SELECT * FROM medicines WHERE profileId = :profileId AND isActive = 1 ORDER BY name ASC")
    fun getMedicinesWithScheduleForProfile(profileId: Long): Flow<List<MedicineWithSchedule>>

    @Transaction
    @Query("SELECT * FROM medicines WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveMedicinesWithSchedule(): Flow<List<MedicineWithSchedule>>

    @Transaction
    @Query("SELECT * FROM medicines WHERE isActive = 1")
    suspend fun getAllActiveMedicinesWithScheduleList(): List<MedicineWithSchedule>

    @Transaction
    @Query("SELECT * FROM medicines")
    suspend fun getAllMedicinesList(): List<MedicineEntity>

    @Transaction
    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineWithScheduleById(id: Long): MedicineWithSchedule?

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Long): MedicineEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity): Int

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    @Query("UPDATE medicines SET isActive = 0 WHERE id = :id")
    suspend fun archiveMedicine(id: Long)

    @Query("UPDATE medicines SET stockCount = CASE WHEN stockCount >= :decrement THEN stockCount - :decrement ELSE 0 END WHERE id = :id AND stockCount > 0")
    suspend fun decrementStock(id: Long, decrement: Int = 1): Int

    @Query("DELETE FROM medicines")
    suspend fun deleteAllMedicines()
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE medicineId = :medicineId AND isActive = 1")
    suspend fun getSchedulesForMedicine(medicineId: Long): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE medicineId = :medicineId")
    suspend fun getAllSchedulesForMedicine(medicineId: Long): List<ScheduleEntity>

    @Query("SELECT * FROM schedules")
    suspend fun getAllSchedulesList(): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity): Int

    @Query("DELETE FROM schedules WHERE medicineId = :medicineId")
    suspend fun deleteSchedulesForMedicine(medicineId: Long)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Query("DELETE FROM schedules")
    suspend fun deleteAllSchedules()
}

@Dao
interface DoseLogDao {
    @Transaction
    @Query("SELECT * FROM dose_logs WHERE scheduledDateTime BETWEEN :startMillis AND :endMillis ORDER BY scheduledDateTime ASC")
    fun getDosesBetweenFlow(startMillis: Long, endMillis: Long): Flow<List<DoseWithMedicine>>

    @Transaction
    @Query("SELECT * FROM dose_logs WHERE profileId = :profileId AND scheduledDateTime BETWEEN :startMillis AND :endMillis ORDER BY scheduledDateTime ASC")
    fun getDosesForProfileBetweenFlow(profileId: Long, startMillis: Long, endMillis: Long): Flow<List<DoseWithMedicine>>

    @Transaction
    @Query("SELECT * FROM dose_logs WHERE profileId = :profileId AND scheduledDateTime BETWEEN :startMillis AND :endMillis ORDER BY scheduledDateTime ASC")
    suspend fun getDosesForProfileBetween(profileId: Long, startMillis: Long, endMillis: Long): List<DoseWithMedicine>

    @Query("SELECT * FROM dose_logs WHERE id = :id")
    suspend fun getDoseById(id: Long): DoseLogEntity?

    @Query("SELECT * FROM dose_logs WHERE medicineId = :medicineId")
    suspend fun getDosesForMedicine(medicineId: Long): List<DoseLogEntity>

    @Query("SELECT * FROM dose_logs")
    suspend fun getAllDoseLogsList(): List<DoseLogEntity>

    @Query("SELECT * FROM dose_logs WHERE medicineId = :medicineId AND scheduledDateTime = :scheduledDateTime LIMIT 1")
    suspend fun findExistingDose(medicineId: Long, scheduledDateTime: Long): DoseLogEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDoseLog(doseLog: DoseLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLogs(doseLogs: List<DoseLogEntity>)

    @Update
    suspend fun updateDoseLog(doseLog: DoseLogEntity): Int

    @Query("UPDATE dose_logs SET status = :newStatus, actualActionDateTime = :actionTime, notes = :notes WHERE id = :id AND status = :expectedStatus")
    suspend fun updateDoseStatusConditional(id: Long, expectedStatus: String, newStatus: String, actionTime: Long, notes: String = ""): Int

    @Query("UPDATE dose_logs SET status = :status, actualActionDateTime = :actionTime, notes = :notes WHERE id = :id")
    suspend fun updateDoseStatus(id: Long, status: String, actionTime: Long, notes: String = "")

    @Query("DELETE FROM dose_logs WHERE medicineId = :medicineId AND status = 'PENDING'")
    suspend fun cancelPendingDosesForMedicine(medicineId: Long)

    @Query("DELETE FROM dose_logs")
    suspend fun deleteAllDoseLogs()
}

@Dao
interface RecordDao {
    @Transaction
    @Query("SELECT * FROM records WHERE profileId = :profileId ORDER BY date DESC, createdAt DESC")
    fun getRecordsForProfileFlow(profileId: Long): Flow<List<RecordWithFiles>>

    @Transaction
    @Query("SELECT * FROM records ORDER BY date DESC, createdAt DESC")
    fun getAllRecordsFlow(): Flow<List<RecordWithFiles>>

    @Transaction
    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getRecordById(id: Long): RecordWithFiles?

    @Query("SELECT * FROM records")
    suspend fun getAllRecordsList(): List<RecordEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRecord(record: RecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<RecordEntity>)

    @Update
    suspend fun updateRecord(record: RecordEntity): Int

    @Delete
    suspend fun deleteRecord(record: RecordEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRecordFile(recordFile: RecordFileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordFiles(recordFiles: List<RecordFileEntity>)

    @Query("SELECT * FROM record_files WHERE recordId = :recordId")
    suspend fun getFilesForRecord(recordId: Long): List<RecordFileEntity>

    @Query("SELECT * FROM record_files")
    suspend fun getAllRecordFilesList(): List<RecordFileEntity>

    @Delete
    suspend fun deleteRecordFile(recordFile: RecordFileEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTagsFlow(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags")
    suspend fun getAllTagsList(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecordTagCrossRef(crossRef: RecordTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordTagCrossRefs(crossRefs: List<RecordTagCrossRef>)

    @Query("SELECT * FROM record_tags")
    suspend fun getAllRecordTagCrossRefs(): List<RecordTagCrossRef>

    @Query("DELETE FROM record_tags WHERE recordId = :recordId")
    suspend fun clearTagsForRecord(recordId: Long)

    @Query("DELETE FROM record_files")
    suspend fun deleteAllRecordFiles()

    @Query("DELETE FROM record_tags")
    suspend fun deleteAllRecordTags()

    @Query("DELETE FROM records")
    suspend fun deleteAllRecords()

    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()
}

@Dao
interface VitalDao {
    @Query("SELECT * FROM vitals WHERE profileId = :profileId ORDER BY measuredAt DESC")
    fun getVitalsForProfileFlow(profileId: Long): Flow<List<VitalEntity>>

    @Query("SELECT * FROM vitals WHERE profileId = :profileId AND vitalType = :type ORDER BY measuredAt ASC")
    fun getVitalsByTypeFlow(profileId: Long, type: String): Flow<List<VitalEntity>>

    @Query("SELECT * FROM vitals")
    suspend fun getAllVitalsList(): List<VitalEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVital(vital: VitalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitals(vitals: List<VitalEntity>)

    @Delete
    suspend fun deleteVital(vital: VitalEntity)

    @Query("DELETE FROM vitals")
    suspend fun deleteAllVitals()
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE profileId = :profileId ORDER BY appointmentDateTime ASC")
    fun getAppointmentsForProfileFlow(profileId: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE appointmentDateTime >= :nowMillis ORDER BY appointmentDateTime ASC")
    fun getUpcomingAppointmentsFlow(nowMillis: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments")
    suspend fun getAllAppointmentsList(): List<AppointmentEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity): Int

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    @Query("DELETE FROM appointments")
    suspend fun deleteAllAppointments()
}
