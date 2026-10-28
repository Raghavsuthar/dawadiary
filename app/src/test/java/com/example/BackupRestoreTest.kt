package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.*
import com.example.util.BackupManager
import com.example.util.BackupResult
import com.example.util.FileManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupRestoreTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var backupFile: File

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.getInstance(context)
        backupFile = File(context.cacheDir, "test_backup.dawadiary")
        if (backupFile.exists()) backupFile.delete()
    }

    @After
    fun tearDown() {
        if (backupFile.exists()) backupFile.delete()
    }

    @Test
    fun `export and restore roundtrip succeeds with correct password and preserves data`() = runBlocking {
        // Clean db
        db.profileDao().deleteAllProfiles()

        // Insert test entities
        val profId = db.profileDao().insertProfile(
            ProfileEntity(name = "Original User", relationship = "Self", dob = "1992-04-10", gender = "Female", bloodGroup = "B+")
        )
        val medId = db.medicineDao().insertMedicine(
            MedicineEntity(
                profileId = profId,
                name = "Atorvastatin",
                strength = "20 mg",
                form = "Tablet",
                doseDescription = "1 tablet",
                foodInstruction = "At Bedtime",
                startDate = "2026-01-01",
                stockCount = 28
            )
        )
        val schedId = db.scheduleDao().insertSchedule(
            ScheduleEntity(
                medicineId = medId,
                scheduleType = "DAILY",
                timesOfDay = "22:00"
            )
        )
        db.vitalDao().insertVital(
            VitalEntity(
                profileId = profId,
                vitalType = "BLOOD_PRESSURE",
                valuePrimary = 120.0,
                valueSecondary = 80.0,
                unit = "mmHg"
            )
        )
        db.appointmentDao().insertAppointment(
            AppointmentEntity(
                profileId = profId,
                doctorName = "Dr. Mehta",
                specialty = "Cardiology",
                clinicHospital = "Heart Care Clinic",
                appointmentDateTime = 1770000000000L,
                notes = "Annual checkup"
            )
        )

        val uri = Uri.fromFile(backupFile)
        val password = "StrongBackupPassword123"

        // 1. Export backup
        val exportResult = BackupManager.createEncryptedBackup(context, password, uri)
        assertTrue("Export must succeed: $exportResult", exportResult is BackupResult.Success)
        assertTrue(backupFile.exists())
        assertTrue(backupFile.length() > 0)

        // 2. Test restore with WRONG password
        val wrongPwdResult = BackupManager.restoreEncryptedBackup(context, "WrongPassword999", uri)
        assertTrue("Restore with wrong password must fail", wrongPwdResult is BackupResult.Failure)

        // Verify original data is unchanged
        val profsBefore = db.profileDao().getAllProfiles()
        assertEquals(1, profsBefore.size)
        assertEquals("Original User", profsBefore[0].name)

        // 3. Clear database to simulate fresh device restore
        db.profileDao().deleteAllProfiles()
        assertTrue(db.profileDao().getAllProfiles().isEmpty())
        assertTrue(db.medicineDao().getAllMedicinesList().isEmpty())

        // 4. Test restore with CORRECT password
        val restoreResult = BackupManager.restoreEncryptedBackup(context, password, uri)
        assertTrue("Restore with correct password must succeed: $restoreResult", restoreResult is BackupResult.Success)

        // Verify all entities restored
        val restoredProfiles = db.profileDao().getAllProfiles()
        assertEquals(1, restoredProfiles.size)
        assertEquals("Original User", restoredProfiles[0].name)

        val restoredMeds = db.medicineDao().getAllMedicinesList()
        assertEquals(1, restoredMeds.size)
        assertEquals("Atorvastatin", restoredMeds[0].name)

        val restoredVitals = db.vitalDao().getAllVitalsList()
        assertEquals(1, restoredVitals.size)
        assertEquals("BLOOD_PRESSURE", restoredVitals[0].vitalType)

        val restoredAppts = db.appointmentDao().getAllAppointmentsList()
        assertEquals(1, restoredAppts.size)
        assertEquals("Dr. Mehta", restoredAppts[0].doctorName)
    }

    @Test
    fun `tampered ciphertext fails authenticated restore`() = runBlocking {
        db.profileDao().deleteAllProfiles()
        db.profileDao().insertProfile(
            ProfileEntity(name = "Test Subject", relationship = "Self", dob = "1995-01-01", gender = "Male", bloodGroup = "A+")
        )

        val uri = Uri.fromFile(backupFile)
        val password = "SecureKey#2026"
        val exportResult = BackupManager.createEncryptedBackup(context, password, uri)
        assertTrue(exportResult is BackupResult.Success)

        // Tamper with backup file bytes
        val bytes = backupFile.readBytes()
        bytes[bytes.size - 5] = (bytes[bytes.size - 5].toInt() xor 0xFF).toByte()
        backupFile.writeBytes(bytes)

        val restoreResult = BackupManager.restoreEncryptedBackup(context, password, uri)
        assertTrue("Tampered backup file must fail authentication", restoreResult is BackupResult.Failure)
    }
}
