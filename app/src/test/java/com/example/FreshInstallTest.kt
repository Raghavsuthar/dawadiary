package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FreshInstallTest {

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
    fun `fresh install database has no pre-populated fake profiles or medicines`() = runBlocking {
        val profiles = db.profileDao().getAllProfiles()
        assertTrue("Profiles must be empty on fresh install; no hard-coded sample profiles", profiles.isEmpty())

        val medicines = db.medicineDao().getAllMedicinesList()
        assertTrue("Medicines must be empty on fresh install; no hard-coded medications", medicines.isEmpty())

        val doses = db.doseLogDao().getAllDoseLogsList()
        assertTrue("Dose logs must be empty on fresh install", doses.isEmpty())

        val records = db.recordDao().getAllRecordsList()
        assertTrue("Records must be empty on fresh install", records.isEmpty())

        val vitals = db.vitalDao().getAllVitalsList()
        assertTrue("Vitals must be empty on fresh install", vitals.isEmpty())

        val appointments = db.appointmentDao().getAllAppointmentsList()
        assertTrue("Appointments must be empty on fresh install", appointments.isEmpty())
    }
}
