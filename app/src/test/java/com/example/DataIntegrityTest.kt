package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.DoseLogEntity
import com.example.data.entity.MedicineEntity
import com.example.data.entity.ProfileEntity
import com.example.data.entity.ScheduleEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DataIntegrityTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `profile update edits existing row without creating duplicate`() = runBlocking {
        val profileId = db.profileDao().insertProfile(
            ProfileEntity(
                name = "Patient D",
                relationship = "Self",
                dob = "1992-03-04",
                gender = "Other",
                bloodGroup = "O+"
            )
        )
        val original = db.profileDao().getProfileById(profileId)!!
        val updated = original.copy(name = "Updated Patient D", allergies = "Penicillin")

        val affected = db.profileDao().updateProfile(updated)

        assertEquals(1, affected)
        assertEquals(1, db.profileDao().getAllProfiles().size)
        assertEquals("Updated Patient D", db.profileDao().getProfileById(profileId)?.name)
        assertEquals("Penicillin", db.profileDao().getProfileById(profileId)?.allergies)
    }

    @Test
    fun `snooze only transitions pending dose and repeated action is ignored`() = runBlocking {
        val profileId = db.profileDao().insertProfile(
            ProfileEntity(name = "Patient E", relationship = "Self", dob = "1990-01-01", gender = "Other", bloodGroup = "A+")
        )
        val medicineId = db.medicineDao().insertMedicine(
            MedicineEntity(
                profileId = profileId,
                name = "Medicine E",
                strength = "10 mg",
                form = "Tablet",
                doseDescription = "1 tablet",
                foodInstruction = "After food",
                startDate = "2026-01-01"
            )
        )
        val doseId = db.doseLogDao().insertDoseLog(
            DoseLogEntity(
                profileId = profileId,
                medicineId = medicineId,
                scheduledDateTime = System.currentTimeMillis(),
                status = "PENDING"
            )
        )

        val firstUpdate = db.doseLogDao().updateDoseStatusConditional(
            doseId, "PENDING", "SNOOZED", System.currentTimeMillis(), "Snoozed"
        )
        val repeatedUpdate = db.doseLogDao().updateDoseStatusConditional(
            doseId, "PENDING", "SNOOZED", System.currentTimeMillis(), "Snoozed again"
        )

        assertEquals(1, firstUpdate)
        assertEquals(0, repeatedUpdate)
        assertEquals("SNOOZED", db.doseLogDao().getDoseById(doseId)?.status)

        for (terminalStatus in listOf("TAKEN", "SKIPPED")) {
            val terminalDoseId = db.doseLogDao().insertDoseLog(
                DoseLogEntity(
                    profileId = profileId,
                    medicineId = medicineId,
                    scheduledDateTime = System.currentTimeMillis(),
                    status = terminalStatus
                )
            )
            val staleSnooze = db.doseLogDao().updateDoseStatusConditional(
                terminalDoseId, "PENDING", "SNOOZED", System.currentTimeMillis(), "Stale snooze"
            )
            assertEquals(0, staleSnooze)
            assertEquals(terminalStatus, db.doseLogDao().getDoseById(terminalDoseId)?.status)
        }
    }

    @Test
    fun `medicine update does not delete schedule or dose logs`() = runBlocking {
        val profId = db.profileDao().insertProfile(
            ProfileEntity(name = "Patient A", relationship = "Self", dob = "1990-01-01", gender = "Male", bloodGroup = "O+")
        )
        val medId = db.medicineDao().insertMedicine(
            MedicineEntity(
                profileId = profId,
                name = "Metformin",
                strength = "500 mg",
                form = "Tablet",
                doseDescription = "1 tablet",
                foodInstruction = "With Food",
                startDate = "2026-01-01",
                stockCount = 30
            )
        )
        val schedId = db.scheduleDao().insertSchedule(
            ScheduleEntity(
                medicineId = medId,
                scheduleType = "DAILY",
                timesOfDay = "08:00, 20:00"
            )
        )
        val doseId = db.doseLogDao().insertDoseLog(
            DoseLogEntity(
                profileId = profId,
                medicineId = medId,
                scheduleId = schedId,
                scheduledDateTime = System.currentTimeMillis(),
                status = "PENDING"
            )
        )

        // Update medicine name and strength using updateMedicine
        val updatedMed = db.medicineDao().getMedicineById(medId)!!.copy(name = "Metformin Extended Release", strength = "1000 mg")
        val affected = db.medicineDao().updateMedicine(updatedMed)
        assertEquals(1, affected)

        // Verify schedules and dose logs were preserved intact!
        val schedules = db.scheduleDao().getAllSchedulesForMedicine(medId)
        assertEquals(1, schedules.size)
        assertEquals(schedId, schedules[0].id)

        val dose = db.doseLogDao().getDoseById(doseId)
        assertEquals("PENDING", dose?.status)
    }

    @Test
    fun `idempotent mark taken prevents duplicate stock decrement`() = runBlocking {
        val profId = db.profileDao().insertProfile(
            ProfileEntity(name = "Patient B", relationship = "Self", dob = "1985-05-15", gender = "Female", bloodGroup = "B+")
        )
        val medId = db.medicineDao().insertMedicine(
            MedicineEntity(
                profileId = profId,
                name = "Thyroxine",
                strength = "50 mcg",
                form = "Tablet",
                doseDescription = "1 tablet",
                foodInstruction = "Before Food",
                startDate = "2026-01-01",
                stockCount = 10
            )
        )
        val doseId = db.doseLogDao().insertDoseLog(
            DoseLogEntity(
                profileId = profId,
                medicineId = medId,
                scheduledDateTime = System.currentTimeMillis(),
                status = "PENDING"
            )
        )

        // First transition PENDING -> TAKEN should succeed and decrement stock
        val rows1 = db.doseLogDao().updateDoseStatusConditional(doseId, "PENDING", "TAKEN", System.currentTimeMillis())
        assertEquals(1, rows1)
        if (rows1 > 0) {
            db.medicineDao().decrementStock(medId, 1)
        }

        val medAfterFirst = db.medicineDao().getMedicineById(medId)!!
        assertEquals(9, medAfterFirst.stockCount)

        // Second transition attempt (e.g. repeated tap or racing notification action) must fail
        val rows2 = db.doseLogDao().updateDoseStatusConditional(doseId, "PENDING", "TAKEN", System.currentTimeMillis())
        assertEquals(0, rows2)
        if (rows2 > 0) {
            db.medicineDao().decrementStock(medId, 1)
        }

        // Stock count must remain 9, not 8!
        val medAfterSecond = db.medicineDao().getMedicineById(medId)!!
        assertEquals(9, medAfterSecond.stockCount)
    }

    @Test
    fun `decrement stock is guarded against going below zero`() = runBlocking {
        val profId = db.profileDao().insertProfile(
            ProfileEntity(name = "Patient C", relationship = "Self", dob = "1980-01-01", gender = "Other", bloodGroup = "A-")
        )
        val medId = db.medicineDao().insertMedicine(
            MedicineEntity(
                profileId = profId,
                name = "Aspirin",
                strength = "75 mg",
                form = "Tablet",
                doseDescription = "1 tablet",
                foodInstruction = "After Food",
                startDate = "2026-01-01",
                stockCount = 1
            )
        )

        db.medicineDao().decrementStock(medId, 1)
        assertEquals(0, db.medicineDao().getMedicineById(medId)!!.stockCount)

        // Decrementing when stock is 0 should remain 0
        db.medicineDao().decrementStock(medId, 1)
        assertEquals(0, db.medicineDao().getMedicineById(medId)!!.stockCount)
    }
}
