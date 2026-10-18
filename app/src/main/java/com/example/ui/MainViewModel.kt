package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DawaDiaryApp
import com.example.data.AppDatabase
import com.example.data.dao.DoseWithMedicine
import com.example.data.dao.MedicineWithSchedule
import com.example.data.dao.RecordWithFiles
import com.example.data.entity.*
import com.example.reminder.ReminderManager
import com.example.util.BackupManager
import com.example.util.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val securityPrefs = (application as DawaDiaryApp).securityPreferences

    val isLocked = securityPrefs.isLocked
    val isFlagSecureEnabled = securityPrefs.flagSecureFlow
    val fontScale = securityPrefs.fontScaleFlow
    val isHighContrast = securityPrefs.highContrastFlow
    val activeProfileId = securityPrefs.activeProfileIdFlow

    private val _isConsentAccepted = MutableStateFlow(securityPrefs.isConsentAccepted())
    val isConsentAccepted = _isConsentAccepted.asStateFlow()

    private val _isDisclaimerAccepted = MutableStateFlow(securityPrefs.isDisclaimerAccepted())
    val isDisclaimerAccepted = _isDisclaimerAccepted.asStateFlow()

    val allProfiles: StateFlow<List<ProfileEntity>> = db.profileDao().getAllProfilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<ProfileEntity?> = combine(allProfiles, activeProfileId) { profiles, id ->
        profiles.find { it.id == id } ?: profiles.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _todayDate = MutableStateFlow(LocalDate.now())
    val todayDate = _todayDate.asStateFlow()

    val todayDoses: StateFlow<List<DoseWithMedicine>> = combine(activeProfileId, _todayDate) { profId, date ->
        Pair(profId, date)
    }.flatMapLatest { (profId, date) ->
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (profId == -1L) {
            db.doseLogDao().getDosesBetweenFlow(startOfDay, endOfDay)
        } else {
            db.doseLogDao().getDosesForProfileBetweenFlow(profId, startOfDay, endOfDay)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMedicines: StateFlow<List<MedicineWithSchedule>> = activeProfileId.flatMapLatest { profId ->
        if (profId == -1L) {
            db.medicineDao().getAllActiveMedicinesWithSchedule()
        } else {
            db.medicineDao().getMedicinesWithScheduleForProfile(profId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentRecords: StateFlow<List<RecordWithFiles>> = activeProfileId.flatMapLatest { profId ->
        if (profId == -1L) {
            db.recordDao().getAllRecordsFlow()
        } else {
            db.recordDao().getRecordsForProfileFlow(profId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentVitals: StateFlow<List<VitalEntity>> = activeProfileId.flatMapLatest { profId ->
        if (profId == -1L) flowOf(emptyList()) else db.vitalDao().getVitalsForProfileFlow(profId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingAppointments: StateFlow<List<AppointmentEntity>> = db.appointmentDao()
        .getUpcomingAppointmentsFlow(System.currentTimeMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshDoses()
    }

    fun refreshDoses() {
        ReminderManager.syncAndScheduleAlarms(getApplication())
    }

    fun switchProfile(id: Long) {
        securityPrefs.setActiveProfileId(id)
    }

    fun markDoseTaken(doseId: Long, medicineId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            db.doseLogDao().updateDoseStatus(doseId, "TAKEN", System.currentTimeMillis())
            db.medicineDao().decrementStock(medicineId, 1)
            refreshDoses()
        }
    }

    fun markDoseSnoozed(doseWithMedicine: DoseWithMedicine) {
        viewModelScope.launch(Dispatchers.IO) {
            db.doseLogDao().updateDoseStatus(
                doseWithMedicine.doseLog.id,
                "SNOOZED",
                System.currentTimeMillis(),
                "Snoozed for 10 minutes"
            )
            ReminderManager.scheduleSnooze(
                context = getApplication(),
                doseId = doseWithMedicine.doseLog.id,
                medicineId = doseWithMedicine.medicine.id,
                medicineName = doseWithMedicine.medicine.name,
                doseDesc = doseWithMedicine.medicine.doseDescription,
                profileName = doseWithMedicine.profile.name
            )
        }
    }

    fun markDoseSkipped(doseId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            db.doseLogDao().updateDoseStatus(doseId, "SKIPPED", System.currentTimeMillis(), "Skipped by patient")
            refreshDoses()
        }
    }

    fun saveProfile(profile: ProfileEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = db.profileDao().insertProfile(profile)
            if (securityPrefs.getActiveProfileId() == -1L) {
                securityPrefs.setActiveProfileId(id)
            }
        }
    }

    fun deleteProfile(profile: ProfileEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val records = db.recordDao().getRecordsForProfileFlow(profile.id).first()
            for (rec in records) {
                for (file in rec.files) {
                    FileManager.deleteEncryptedFile(file.encryptedFilePath)
                }
            }
            db.profileDao().deleteProfile(profile)
            val remaining = db.profileDao().getAllProfiles()
            if (remaining.isNotEmpty()) {
                securityPrefs.setActiveProfileId(remaining.first().id)
            } else {
                securityPrefs.setActiveProfileId(-1L)
            }
            refreshDoses()
        }
    }

    fun saveMedicine(medicine: MedicineEntity, schedule: ScheduleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val medId = db.medicineDao().insertMedicine(medicine)
            db.scheduleDao().deleteSchedulesForMedicine(medId)
            db.scheduleDao().insertSchedule(schedule.copy(medicineId = medId))
            refreshDoses()
        }
    }

    fun deleteMedicine(medicine: MedicineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.medicineDao().deleteMedicine(medicine)
            refreshDoses()
        }
    }

    fun saveRecord(record: RecordEntity, fileUris: List<Pair<Uri, String>>, tags: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val recordId = db.recordDao().insertRecord(record)
            for ((uri, originalName) in fileUris) {
                try {
                    val (encPath, size) = FileManager.saveEncryptedFile(getApplication(), uri, originalName)
                    val mime = getApplication<Application>().contentResolver.getType(uri) ?: "application/octet-stream"
                    db.recordDao().insertRecordFile(
                        RecordFileEntity(
                            recordId = recordId,
                            fileName = originalName,
                            mimeType = mime,
                            encryptedFilePath = encPath,
                            fileSizeBytes = size
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            for (tag in tags) {
                val cleanTag = tag.trim()
                if (cleanTag.isNotEmpty()) {
                    val tagEntity = db.recordDao().getTagByName(cleanTag)
                    val tagId = tagEntity?.id ?: db.recordDao().insertTag(TagEntity(name = cleanTag))
                    db.recordDao().insertRecordTagCrossRef(RecordTagCrossRef(recordId, tagId))
                }
            }
        }
    }

    fun deleteRecord(recordWithFiles: RecordWithFiles) {
        viewModelScope.launch(Dispatchers.IO) {
            for (f in recordWithFiles.files) {
                FileManager.deleteEncryptedFile(f.encryptedFilePath)
            }
            db.recordDao().deleteRecord(recordWithFiles.record)
        }
    }

    fun saveVital(vital: VitalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.vitalDao().insertVital(vital)
        }
    }

    fun saveAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.appointmentDao().insertAppointment(appointment)
        }
    }

    fun acceptConsent() {
        securityPrefs.setConsentAccepted(true)
        _isConsentAccepted.value = true
    }

    fun acceptDisclaimer() {
        securityPrefs.setDisclaimerAccepted(true)
        _isDisclaimerAccepted.value = true
    }

    fun unlockApp(pin: String): Boolean {
        return securityPrefs.verifyPin(pin)
    }

    fun unlockWithBiometric() {
        securityPrefs.unlock()
    }

    fun setPin(pin: String) {
        securityPrefs.setPin(pin)
    }

    fun clearPin() {
        securityPrefs.clearPin()
    }

    fun toggleFlagSecure(enabled: Boolean) {
        securityPrefs.setFlagSecureEnabled(enabled)
    }

    fun setFontScale(scale: Float) {
        securityPrefs.setFontScale(scale)
    }

    fun toggleHighContrast(enabled: Boolean) {
        securityPrefs.setHighContrast(enabled)
    }

    fun setLanguage(lang: String) {
        securityPrefs.setLanguage(lang)
    }

    fun getLanguage(): String = securityPrefs.getLanguage()

    fun eraseAllData(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            FileManager.wipeAllEncryptedFiles(getApplication())
            db.profileDao().deleteAllProfiles()
            securityPrefs.clearAllData()
            _isConsentAccepted.value = false
            _isDisclaimerAccepted.value = false
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}
