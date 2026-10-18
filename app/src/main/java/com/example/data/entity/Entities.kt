package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relationship: String,
    val dob: String,
    val gender: String,
    val bloodGroup: String,
    val allergies: String = "",
    val chronicConditions: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val isSelf: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "medicines",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val name: String,
    val strength: String,
    val form: String,
    val doseDescription: String,
    val foodInstruction: String,
    val prescribingDoctor: String = "",
    val startDate: String,
    val endDate: String? = null,
    val stockCount: Int = 0,
    val lowStockThreshold: Int = 5,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicineId"])]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long,
    val scheduleType: String,
    val timesOfDay: String,
    val daysOfWeekMask: Int = 127,
    val intervalDays: Int = 1,
    val taperingEndDate: String? = null,
    val isActive: Boolean = true
)

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"]), Index(value = ["medicineId"])]
)
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val medicineId: Long,
    val scheduleId: Long? = null,
    val scheduledDateTime: Long,
    val actualActionDateTime: Long? = null,
    val status: String,
    val notes: String = ""
)

@Entity(
    tableName = "records",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val title: String,
    val date: String,
    val doctorHospital: String = "",
    val recordType: String,
    val notes: String = "",
    val linkedMedicineName: String = "",
    val linkedCondition: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "record_files",
    foreignKeys = [
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recordId"])]
)
data class RecordFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordId: Long,
    val fileName: String,
    val mimeType: String,
    val encryptedFilePath: String,
    val fileSizeBytes: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "vitals",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class VitalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val vitalType: String,
    val valuePrimary: Double,
    val valueSecondary: Double? = null,
    val unit: String,
    val measuredAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val doctorName: String,
    val specialty: String = "",
    val clinicHospital: String = "",
    val appointmentDateTime: Long,
    val notes: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "record_tags",
    primaryKeys = ["recordId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = RecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recordId"]), Index(value = ["tagId"])]
)
data class RecordTagCrossRef(
    val recordId: Long,
    val tagId: Long
)
