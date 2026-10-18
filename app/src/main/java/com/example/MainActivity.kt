package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

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
            val isFlagSecure by viewModel.isFlagSecureEnabled.collectAsStateWithLifecycle()
            val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()
            val isHighContrast by viewModel.isHighContrast.collectAsStateWithLifecycle()
            val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
            val isConsentAccepted by viewModel.isConsentAccepted.collectAsStateWithLifecycle()
            val isDisclaimerAccepted by viewModel.isDisclaimerAccepted.collectAsStateWithLifecycle()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* user handled */ }

                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

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

            // Apply custom font scaling for elderly comfort
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

                        // Lock Screen overlay
                        if (isLocked) {
                            LockScreen(
                                onUnlockWithPin = { pin -> viewModel.unlockApp(pin) },
                                onBiometricRequest = { showBiometricPrompt() }
                            )
                        }

                        // First-run DPDP Consent & Medical Disclaimer
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
    var showProfileDropdown by remember { mutableStateOf(false) }

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showProfileDropdown = true }
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeProfile?.name?.take(1) ?: "D",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = activeProfile?.name ?: stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${activeProfile?.relationship ?: "Family"} ▾",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            DropdownMenu(
                                expanded = showProfileDropdown,
                                onDismissRequest = { showProfileDropdown = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                allProfiles.forEach { profile ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${profile.name} (${profile.relationship})",
                                                fontWeight = if (profile.id == activeProfile?.id) FontWeight.Bold else FontWeight.Normal,
                                                color = if (profile.id == activeProfile?.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            viewModel.switchProfile(profile.id)
                                            showProfileDropdown = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (profile.isSelf) Icons.Default.Person else Icons.Default.FamilyRestroom,
                                                contentDescription = null,
                                                tint = if (profile.id == activeProfile?.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showEmergencyDialog = true },
                            modifier = Modifier
                                .testTag("emergency_button")
                                .size(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = "Emergency Medical Card",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    NavigationBarItem(
                        selected = currentTab == "today",
                        onClick = { currentTab = "today" },
                        icon = { Icon(imageVector = Icons.Default.Today, contentDescription = stringResource(R.string.nav_today)) },
                        label = { Text(stringResource(R.string.nav_today), fontWeight = if (currentTab == "today") FontWeight.Bold else FontWeight.Normal) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_today")
                    )
                    NavigationBarItem(
                        selected = currentTab == "medicines",
                        onClick = { currentTab = "medicines" },
                        icon = { Icon(imageVector = Icons.Default.Medication, contentDescription = stringResource(R.string.nav_medicines)) },
                        label = { Text(stringResource(R.string.nav_medicines), fontWeight = if (currentTab == "medicines") FontWeight.Bold else FontWeight.Normal) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_medicines")
                    )
                    NavigationBarItem(
                        selected = currentTab == "records",
                        onClick = { currentTab = "records" },
                        icon = { Icon(imageVector = Icons.Default.FolderSpecial, contentDescription = stringResource(R.string.nav_records)) },
                        label = { Text(stringResource(R.string.nav_records), fontWeight = if (currentTab == "records") FontWeight.Bold else FontWeight.Normal) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_records")
                    )
                    NavigationBarItem(
                        selected = currentTab == "family",
                        onClick = { currentTab = "family" },
                        icon = { Icon(imageVector = Icons.Default.People, contentDescription = stringResource(R.string.nav_family)) },
                        label = { Text(stringResource(R.string.nav_family), fontWeight = if (currentTab == "family") FontWeight.Bold else FontWeight.Normal) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_family")
                    )
                    NavigationBarItem(
                        selected = currentTab == "settings",
                        onClick = { currentTab = "settings" },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                        label = { Text(stringResource(R.string.nav_settings), fontWeight = if (currentTab == "settings") FontWeight.Bold else FontWeight.Normal) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
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
                    onSelectProfile = { id -> viewModel.switchProfile(id) },
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
    }

    if (showEmergencyDialog) {
        EmergencyCardDialog(
            profile = activeProfile,
            activeMedicines = currentMedicines,
            onDismiss = { showEmergencyDialog = false }
        )
    }
}
