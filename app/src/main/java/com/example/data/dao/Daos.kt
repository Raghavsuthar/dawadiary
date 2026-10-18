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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

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
    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineWithScheduleById(id: Long): MedicineWithSchedule?

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Long): MedicineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    @Query("UPDATE medicines SET stockCount = MAX(0, stockCount - :decrement) WHERE id = :id")
    suspend fun decrementStock(id: Long, decrement: Int = 1)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE medicineId = :medicineId AND isActive = 1")
    suspend fun getSchedulesForMedicine(medicineId: Long): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Query("DELETE FROM schedules WHERE medicineId = :medicineId")
    suspend fun deleteSchedulesForMedicine(medicineId: Long)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)
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

    @Query("SELECT * FROM dose_logs WHERE medicineId = :medicineId AND scheduledDateTime = :scheduledDateTime LIMIT 1")
    suspend fun findExistingDose(medicineId: Long, scheduledDateTime: Long): DoseLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLog(doseLog: DoseLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDoseLogs(doseLogs: List<DoseLogEntity>)

    @Update
    suspend fun updateDoseLog(doseLog: DoseLogEntity)

    @Query("UPDATE dose_logs SET status = :status, actualActionDateTime = :actionTime, notes = :notes WHERE id = :id")
    suspend fun updateDoseStatus(id: Long, status: String, actionTime: Long, notes: String = "")
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity): Long

    @Update
    suspend fun updateRecord(record: RecordEntity)

    @Delete
    suspend fun deleteRecord(record: RecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordFile(recordFile: RecordFileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordFiles(recordFiles: List<RecordFileEntity>)

    @Query("SELECT * FROM record_files WHERE recordId = :recordId")
    suspend fun getFilesForRecord(recordId: Long): List<RecordFileEntity>

    @Delete
    suspend fun deleteRecordFile(recordFile: RecordFileEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTagsFlow(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecordTagCrossRef(crossRef: RecordTagCrossRef)

    @Query("DELETE FROM record_tags WHERE recordId = :recordId")
    suspend fun clearTagsForRecord(recordId: Long)
}

@Dao
interface VitalDao {
    @Query("SELECT * FROM vitals WHERE profileId = :profileId ORDER BY measuredAt DESC")
    fun getVitalsForProfileFlow(profileId: Long): Flow<List<VitalEntity>>

    @Query("SELECT * FROM vitals WHERE profileId = :profileId AND vitalType = :type ORDER BY measuredAt ASC")
    fun getVitalsByTypeFlow(profileId: Long, type: String): Flow<List<VitalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVital(vital: VitalEntity): Long

    @Delete
    suspend fun deleteVital(vital: VitalEntity)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE profileId = :profileId ORDER BY appointmentDateTime ASC")
    fun getAppointmentsForProfileFlow(profileId: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE appointmentDateTime >= :nowMillis ORDER BY appointmentDateTime ASC")
    fun getUpcomingAppointmentsFlow(nowMillis: Long): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)
}
