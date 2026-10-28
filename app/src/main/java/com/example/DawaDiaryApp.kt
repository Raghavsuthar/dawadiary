package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.entity.ProfileEntity
import com.example.reminder.ReminderManager
import com.example.security.SecurityPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DawaDiaryApp : Application() {

    lateinit var securityPreferences: SecurityPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        securityPreferences = SecurityPreferences(this)

        ReminderManager.initNotificationChannels(this)

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(this@DawaDiaryApp)
            val existingProfiles = db.profileDao().getAllProfiles()
            if (existingProfiles.isEmpty()) {
                val selfId = db.profileDao().insertProfile(
                    ProfileEntity(
                        name = "My Self",
                        relationship = "Self",
                        dob = "1990-01-01",
                        gender = "Male",
                        bloodGroup = "O+",
                        isSelf = true
                    )
                )
                if (securityPreferences.getActiveProfileId() == -1L) {
                    securityPreferences.setActiveProfileId(selfId)
                }
            } else if (securityPreferences.getActiveProfileId() == -1L) {
                securityPreferences.setActiveProfileId(existingProfiles.first().id)
            }

            ReminderManager.syncAndScheduleAlarms(this@DawaDiaryApp)
        }
    }
}
