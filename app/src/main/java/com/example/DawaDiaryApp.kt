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
            ReminderManager.syncAndScheduleAlarms(this@DawaDiaryApp)
        }
    }
}
