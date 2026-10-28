package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.reminder.ReminderManager
import com.example.ui.theme.*
import com.example.util.BackupResult
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    isFlagSecure: Boolean,
    fontScale: Float,
    isHighContrast: Boolean,
    isPrivateNotifications: Boolean,
    currentLanguage: String,
    onToggleFlagSecure: (Boolean) -> Unit,
    onSetFontScale: (Float) -> Unit,
    onToggleHighContrast: (Boolean) -> Unit,
    onTogglePrivateNotifications: (Boolean) -> Unit,
    onSetLanguage: (String) -> Unit,
    onSetPin: (String) -> Unit,
    onClearPin: () -> Unit,
    onExportBackup: suspend (password: String, destinationUri: Uri) -> BackupResult,
    onRestoreBackup: suspend (password: String, sourceUri: Uri) -> BackupResult,
    onEraseAllData: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showPinDialog by remember { mutableStateOf(false) }
    var showEraseDialog by remember { mutableStateOf(false) }
    var showExportPasswordDialog by remember { mutableStateOf(false) }
    var showRestorePasswordDialog by remember { mutableStateOf(false) }
    var exportPassword by remember { mutableStateOf("") }
    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var lastBackupResult by remember { mutableStateOf<BackupResult?>(null) }
    var canScheduleExact by remember { mutableStateOf(ReminderManager.canScheduleExactAlarms(context)) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null && exportPassword.isNotBlank()) {
            scope.launch {
                val result = onExportBackup(exportPassword, uri)
                lastBackupResult = result
                exportPassword = ""
            }
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedRestoreUri = uri
            showRestorePasswordDialog = true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = "Settings & Privacy",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Security, display comfort, backup and data protection",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MdDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Backup result notification card
        if (lastBackupResult != null) {
            item {
                when (val res = lastBackupResult) {
                    is BackupResult.Success -> {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Operation Successful", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(res.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    "Processed: ${res.profilesCount} profiles, ${res.medicinesCount} medicines, ${res.recordsCount} records, ${res.attachmentsCount} attachments.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    is BackupResult.Failure -> {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(res.error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                    null -> {}
                }
            }
        }

        // Section: Language Selection
        item {
            MdSection(title = { Text("Language / भाषा / ભાષા", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) })
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select App Interface Language", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "en" to "English",
                            "hi" to "हिन्दी",
                            "gu" to "ગુજરાતી"
                        ).forEach { (code, label) ->
                            val isSel = currentLanguage == code
                            FilterChip(
                                selected = isSel,
                                onClick = { onSetLanguage(code) },
                                label = { Text(label, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                }
            }
        }

        // Section: Security & Privacy
        item {
            MdSection(title = { Text("Security & Privacy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) })
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    MdListRow(
                        leading = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        title = { Text("4-Digit Security PIN", fontWeight = FontWeight.SemiBold) },
                        subtitle = { Text("PBKDF2 salted hash with auto-lockout protection", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailing = {
                            TextButton(onClick = { showPinDialog = true }) {
                                Text("Configure", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    MdDivider()
                    MdListRow(
                        leading = { Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        title = { Text("Hide Medicine Names in Notifications", fontWeight = FontWeight.SemiBold) },
                        subtitle = { Text("Hides specific drug names on lock screen to protect privacy", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailing = {
                            Switch(
                                checked = isPrivateNotifications,
                                onCheckedChange = onTogglePrivateNotifications
                            )
                        }
                    )
                    MdDivider()
                    MdListRow(
                        leading = { Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        title = { Text("Prevent Screen Capture (FLAG_SECURE)", fontWeight = FontWeight.SemiBold) },
                        subtitle = { Text("Blocks screenshots & hides content in recent apps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailing = {
                            Switch(
                                checked = isFlagSecure,
                                onCheckedChange = onToggleFlagSecure
                            )
                        }
                    )
                }
            }
        }

        // Section: Exact Alarm Permission
        item {
            MdSection(title = { Text("Medication Reminder Accuracy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) })
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (canScheduleExact) "Exact Alarms: Allowed" else "Exact Alarms: Restricted",
                                fontWeight = FontWeight.SemiBold,
                                color = if (canScheduleExact) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = if (canScheduleExact) "Doses will trigger at the exact minute even when screen is off."
                                else "Android may delay notifications. Tap below to enable exact alarms.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    } catch (ignored: Exception) {}
                                }
                            ) {
                                Text("Allow")
                            }
                        }
                    }
                }
            }
        }

        // Section: Accessibility & Display
        item {
            MdSection(title = { Text("Accessibility & Display", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) })
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Font Size for Elderly Comfort", fontWeight = FontWeight.SemiBold)
                    Text("Increase text size across the entire application", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1.0f to "Standard", 1.15f to "Large", 1.3f to "Extra Large").forEach { (scale, label) ->
                            val isSel = fontScale == scale
                            FilterChip(
                                selected = isSel,
                                onClick = { onSetFontScale(scale) },
                                label = { Text(label) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    MdDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("High Contrast Mode", fontWeight = FontWeight.SemiBold)
                            Text("Maximizes text and border contrast for visual clarity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isHighContrast,
                            onCheckedChange = onToggleHighContrast
                        )
                    }
                }
            }
        }

        // Section: Encrypted Backup & Restore
        item {
            MdSection(title = { Text("Local Encrypted Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) })
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    MdListRow(
                        leading = { Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        title = { Text("Export Encrypted Backup (.dawadiary)", fontWeight = FontWeight.SemiBold) },
                        subtitle = { Text("All profiles, medicines, dose logs, and encrypted attachments bundled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailing = {
                            TextButton(onClick = { showExportPasswordDialog = true }) {
                                Text("Export", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    MdDivider()
                    MdListRow(
                        leading = { Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        title = { Text("Restore From Backup File", fontWeight = FontWeight.SemiBold) },
                        subtitle = { Text("Atomic restore with password decryption & format verification", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailing = {
                            TextButton(onClick = { openBackupLauncher.launch(arrayOf("*/*")) }) {
                                Text("Restore", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }
        }

        // Section: Danger Zone
        item {
            MdSection(title = { Text("Data & Privacy (DPDP Compliance)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) })
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Erase All Data Permanently", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text(
                        "Irreversibly deletes all family profiles, prescriptions, encrypted medical files, and logs from this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showEraseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Erase Everything", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var pinConfirm by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Configure Security PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a 4-digit PIN to protect DawaDiary with PBKDF2 encryption.")
                    MdTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                pinInput = it
                                pinError = null
                            }
                        },
                        label = "Enter 4-Digit PIN"
                    )
                    MdTextField(
                        value = pinConfirm,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                pinConfirm = it
                                pinError = null
                            }
                        },
                        label = "Confirm 4-Digit PIN"
                    )
                    if (pinError != null) {
                        Text(pinError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                MdPrimaryButton(
                    onClick = {
                        if (pinInput.length != 4) {
                            pinError = "PIN must be exactly 4 digits."
                            return@MdPrimaryButton
                        }
                        if (pinInput != pinConfirm) {
                            pinError = "PINs do not match."
                            return@MdPrimaryButton
                        }
                        onSetPin(pinInput)
                        showPinDialog = false
                    },
                    label = { Text("Set PIN") }
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    onClearPin()
                    showPinDialog = false
                }) {
                    Text("Remove PIN", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    // Export Password Dialog
    if (showExportPasswordDialog) {
        var pwd1 by remember { mutableStateOf("") }
        var pwd2 by remember { mutableStateOf("") }
        var pwdError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showExportPasswordDialog = false },
            title = { Text("Set Backup Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Your backup will be encrypted using AES-256-GCM and PBKDF2 (65,536 iterations). You MUST remember this password to restore your data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = pwd1,
                        onValueChange = { pwd1 = it; pwdError = null },
                        label = { Text("Password (min 6 characters)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pwd2,
                        onValueChange = { pwd2 = it; pwdError = null },
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pwdError != null) {
                        Text(pwdError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                MdPrimaryButton(
                    onClick = {
                        if (pwd1.length < 6) {
                            pwdError = "Password must be at least 6 characters."
                            return@MdPrimaryButton
                        }
                        if (pwd1 != pwd2) {
                            pwdError = "Passwords do not match."
                            return@MdPrimaryButton
                        }
                        exportPassword = pwd1
                        showExportPasswordDialog = false
                        createBackupLauncher.launch("dawadiary_backup_${System.currentTimeMillis()}.dawadiary")
                    },
                    label = { Text("Choose File Location") }
                )
            },
            dismissButton = {
                TextButton(onClick = { showExportPasswordDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Restore Password Dialog
    if (showRestorePasswordDialog && selectedRestoreUri != null) {
        var restorePwd by remember { mutableStateOf("") }
        var restoreError by remember { mutableStateOf<String?>(null) }
        var isRestoring by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isRestoring) showRestorePasswordDialog = false },
            title = { Text("Enter Backup Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter the password used when creating this backup file. Live records will be replaced with backup data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = restorePwd,
                        onValueChange = { restorePwd = it; restoreError = null },
                        label = { Text("Backup Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (restoreError != null) {
                        Text(restoreError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (isRestoring) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                MdPrimaryButton(
                    onClick = {
                        if (restorePwd.isBlank()) {
                            restoreError = "Please enter the backup password."
                            return@MdPrimaryButton
                        }
                        isRestoring = true
                        scope.launch {
                            val res = onRestoreBackup(restorePwd, selectedRestoreUri!!)
                            isRestoring = false
                            lastBackupResult = res
                            if (res is BackupResult.Success) {
                                showRestorePasswordDialog = false
                                selectedRestoreUri = null
                            } else if (res is BackupResult.Failure) {
                                restoreError = res.error
                            }
                        }
                    },
                    label = { Text("Decrypt & Restore") }
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    if (!isRestoring) {
                        showRestorePasswordDialog = false
                        selectedRestoreUri = null
                    }
                }) { Text("Cancel") }
            }
        )
    }

    if (showEraseDialog) {
        AlertDialog(
            onDismissRequest = { showEraseDialog = false },
            title = { Text("Erase All Data?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = { Text("This will permanently delete all medication records, medical scans, profiles, and encryption keys from your device. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onEraseAllData()
                        showEraseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Everything", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEraseDialog = false }) { Text("Cancel") }
            }
        )
    }
}
