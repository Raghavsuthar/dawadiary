package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.ConsentDisclaimerDialog
import com.example.ui.components.EmergencyCardDialog
import com.example.ui.components.LockScreen
import com.example.ui.screens.*
import com.example.ui.theme.DawaDiaryTheme
import com.example.ui.theme.MdBottomNav
import com.example.ui.theme.MdTopStrip

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* User permission response handled */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
            val isFlagSecure by viewModel.isFlagSecureEnabled.collectAsStateWithLifecycle()
            val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()
            val isHighContrast by viewModel.isHighContrast.collectAsStateWithLifecycle()
            val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
            val isConsentAccepted by viewModel.isConsentAccepted.collectAsStateWithLifecycle()
            val isDisclaimerAccepted by viewModel.isDisclaimerAccepted.collectAsStateWithLifecycle()

            // Apply FLAG_SECURE dynamically
            LaunchedEffect(isFlagSecure) {
                if (isFlagSecure) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            val currentDensity = LocalDensity.current
            val customDensity = remember(currentDensity, fontScale) {
                Density(currentDensity.density, fontScale)
            }

            CompositionLocalProvider(LocalDensity provides customDensity) {
                DawaDiaryTheme(
                    darkTheme = false,
                    highContrast = isHighContrast
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        DawaDiaryAppContent(
                            viewModel = viewModel,
                            onRequestBiometric = { showBiometricPrompt() }
                        )

                        if (isLocked) {
                            LockScreen(
                                onUnlockWithPin = { pin -> viewModel.unlockApp(pin) },
                                onBiometricRequest = { showBiometricPrompt() }
                            )
                        }

                        ConsentDisclaimerDialog(
                            showConsent = !isConsentAccepted,
                            onAcceptConsent = { viewModel.acceptConsent() },
                            showDisclaimer = isConsentAccepted && !isDisclaimerAccepted,
                            onAcceptDisclaimer = { viewModel.acceptDisclaimer() }
                        )
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.unlockWithBiometric()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock DawaDiary")
            .setSubtitle("Authenticate using your fingerprint or screen lock")
            .setNegativeButtonText("Use PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DawaDiaryAppContent(
    viewModel: MainViewModel,
    onRequestBiometric: () -> Unit
) {
    var currentTab by remember { mutableStateOf("today") }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val todayDoses by viewModel.todayDoses.collectAsStateWithLifecycle()
    val currentMedicines by viewModel.currentMedicines.collectAsStateWithLifecycle()
    val currentRecords by viewModel.currentRecords.collectAsStateWithLifecycle()
    val currentVitals by viewModel.currentVitals.collectAsStateWithLifecycle()
    val upcomingAppointments by viewModel.upcomingAppointments.collectAsStateWithLifecycle()
    val isFlagSecure by viewModel.isFlagSecureEnabled.collectAsStateWithLifecycle()
    val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()
    val isHighContrast by viewModel.isHighContrast.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        MdTopStrip(
            profileName = activeProfile?.name,
            profileInitial = activeProfile?.name?.take(1) ?: "D",
            onProfileClick = { currentTab = "family" },
            onEmergencyClick = { showEmergencyDialog = true }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                "today" -> TodayScreen(
                    activeProfile = activeProfile,
                    doses = todayDoses,
                    activeMedicines = currentMedicines,
                    onMarkTaken = { doseId, medId -> viewModel.markDoseTaken(doseId, medId) },
                    onMarkSnooze = { doseWithMed -> viewModel.markDoseSnoozed(doseWithMed) },
                    onMarkSkip = { doseId -> viewModel.markDoseSkipped(doseId) },
                    onAddMedicineClick = { currentTab = "medicines" }
                )

                "medicines" -> MedicinesScreen(
                    activeProfile = activeProfile,
                    medicines = currentMedicines,
                    onSaveMedicine = { med, sched -> viewModel.saveMedicine(med, sched) },
                    onDeleteMedicine = { med -> viewModel.deleteMedicine(med) }
                )

                "records" -> RecordsVaultScreen(
                    activeProfile = activeProfile,
                    records = currentRecords,
                    onSaveRecord = { rec, uris, tags -> viewModel.saveRecord(rec, uris, tags) },
                    onDeleteRecord = { recWithFiles -> viewModel.deleteRecord(recWithFiles) }
                )

                "family" -> FamilyScreen(
                    profiles = allProfiles,
                    activeProfile = activeProfile,
                    activeMedicines = currentMedicines,
                    vitals = currentVitals,
                    appointments = upcomingAppointments,
                    onSelectProfile = { id -> viewModel.switchProfile(id); currentTab = "family" },
                    onSaveProfile = { prof -> viewModel.saveProfile(prof) },
                    onDeleteProfile = { prof -> viewModel.deleteProfile(prof) },
                    onSaveVital = { vital -> viewModel.saveVital(vital) },
                    onSaveAppointment = { appt -> viewModel.saveAppointment(appt) }
                )

                "settings" -> SettingsScreen(
                    isFlagSecure = isFlagSecure,
                    fontScale = fontScale,
                    isHighContrast = isHighContrast,
                    currentLanguage = viewModel.getLanguage(),
                    onToggleFlagSecure = { enabled -> viewModel.toggleFlagSecure(enabled) },
                    onSetFontScale = { scale -> viewModel.setFontScale(scale) },
                    onToggleHighContrast = { enabled -> viewModel.toggleHighContrast(enabled) },
                    onSetLanguage = { lang -> viewModel.setLanguage(lang) },
                    onSetPin = { pin -> viewModel.setPin(pin) },
                    onClearPin = { viewModel.clearPin() },
                    onEraseAllData = {
                        viewModel.eraseAllData {
                            currentTab = "today"
                        }
                    }
                )
            }
        }

        MdBottomNav(
            currentTab = currentTab,
            onTabClick = { currentTab = it }
        )
    }

    if (showEmergencyDialog) {
        EmergencyCardDialog(
            profile = activeProfile,
            activeMedicines = currentMedicines,
            onDismiss = { showEmergencyDialog = false }
        )
    }
}
