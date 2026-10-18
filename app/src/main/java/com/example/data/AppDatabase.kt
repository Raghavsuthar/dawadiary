package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProfileEntity::class,
        MedicineEntity::class,
        ScheduleEntity::class,
        DoseLogEntity::class,
        RecordEntity::class,
        RecordFileEntity::class,
        VitalEntity::class,
        AppointmentEntity::class,
        TagEntity::class,
        RecordTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun medicineDao(): MedicineDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun doseLogDao(): DoseLogDao
    abstract fun recordDao(): RecordDao
    abstract fun vitalDao(): VitalDao
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dawadiary.db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            val profileId = database.profileDao().insertProfile(
                                ProfileEntity(
                                    name = "Raghav Suthar",
                                    relationship = "Self",
                                    dob = "1990-05-15",
                                    gender = "Male",
                                    bloodGroup = "O+",
                                    allergies = "Penicillin, Sulfa drugs",
                                    chronicConditions = "Hypertension",
                                    emergencyContactName = "Emergency Services",
                                    emergencyContactPhone = "+1 555-0199",
                                    isSelf = true
                                )
                            )
                            val med1 = database.medicineDao().insertMedicine(
                                MedicineEntity(
                                    profileId = profileId,
                                    name = "Amlodipine",
                                    strength = "5mg",
                                    form = "Tablet",
                                    doseDescription = "1 tablet",
                                    foodInstruction = "AFTER_FOOD",
                                    prescribingDoctor = "Dr. Mehta",
                                    startDate = "2026-01-01",
                                    stockCount = 28,
                                    lowStockThreshold = 7
                                )
                            )
                            database.scheduleDao().insertSchedule(
                                ScheduleEntity(
                                    medicineId = med1,
                                    scheduleType = "DAILY",
                                    timesOfDay = "08:00, 20:00"
                                )
                            )
                            val med2 = database.medicineDao().insertMedicine(
                                MedicineEntity(
                                    profileId = profileId,
                                    name = "Vitamin D3",
                                    strength = "60000 IU",
                                    form = "Tablet",
                                    doseDescription = "1 capsule",
                                    foodInstruction = "WITH_FOOD",
                                    prescribingDoctor = "Dr. Mehta",
                                    startDate = "2026-01-01",
                                    stockCount = 3,
                                    lowStockThreshold = 5
                                )
                            )
                            database.scheduleDao().insertSchedule(
                                ScheduleEntity(
                                    medicineId = med2,
                                    scheduleType = "DAILY",
                                    timesOfDay = "12:00"
                                )
                            )
                            val now = System.currentTimeMillis()
                            database.doseLogDao().insertDoseLog(
                                DoseLogEntity(
                                    medicineId = med1,
                                    profileId = profileId,
                                    scheduledDateTime = now - 3600000,
                                    actualActionDateTime = now - 3400000,
                                    status = "TAKEN"
                                )
                            )
                            database.doseLogDao().insertDoseLog(
                                DoseLogEntity(
                                    medicineId = med1,
                                    profileId = profileId,
                                    scheduledDateTime = now + 14400000,
                                    status = "PENDING"
                                )
                            )
                            database.doseLogDao().insertDoseLog(
                                DoseLogEntity(
                                    medicineId = med2,
                                    profileId = profileId,
                                    scheduledDateTime = now + 3600000,
                                    status = "PENDING"
                                )
                            )
                            database.vitalDao().insertVital(
                                VitalEntity(
                                    profileId = profileId,
                                    vitalType = "BLOOD_PRESSURE",
                                    valuePrimary = 120.0,
                                    valueSecondary = 80.0,
                                    unit = "mmHg"
                                )
                            )
                            database.appointmentDao().insertAppointment(
                                AppointmentEntity(
                                    profileId = profileId,
                                    doctorName = "Dr. Arvind Mehta",
                                    specialty = "Cardiologist",
                                    clinicHospital = "City Heart Clinic",
                                    appointmentDateTime = now + 86400000 * 3,
                                    notes = "Routine blood pressure checkup and prescription refill"
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
