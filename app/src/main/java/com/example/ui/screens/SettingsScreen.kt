package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.*
import com.example.util.BackupManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    isFlagSecure: Boolean,
    fontScale: Float,
    isHighContrast: Boolean,
    currentLanguage: String,
    onToggleFlagSecure: (Boolean) -> Unit,
    onSetFontScale: (Float) -> Unit,
    onToggleHighContrast: (Boolean) -> Unit,
    onSetLanguage: (String) -> Unit,
    onSetPin: (String) -> Unit,
    onClearPin: () -> Unit,
    onEraseAllData: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showPinDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showBackupPasswordDialog by remember { mutableStateOf(false) }
    var showRestorePasswordDialog by remember { mutableStateOf(false) }
    var showDisclaimerInfoDialog by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var pendingBackupUri by remember { mutableStateOf<Uri?>(null) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let {
            pendingBackupUri = it
            showBackupPasswordDialog = true
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            pendingRestoreUri = it
            showRestorePasswordDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Screen title ──
        Column {
            Text(
                text = stringResource(R.string.nav_settings),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Preferences, Security & Health Data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── Section: Language ──
        MdSection(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MdIcon(Icons.Default.Translate, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.language_selection),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            action = {},
            padding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
        )
        MdDivider(modifier = Modifier.padding(horizontal = 0.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MdChip(
                selected = currentLanguage == "en",
                onClick = { onSetLanguage("en") },
                label = { Text("English", fontWeight = if (currentLanguage == "en") FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.weight(1f)
            )
            MdChip(
                selected = currentLanguage == "hi",
                onClick = { onSetLanguage("hi") },
                label = { Text("हिंदी", fontWeight = if (currentLanguage == "hi") FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.weight(1f)
            )
            MdChip(
                selected = currentLanguage == "gu",
                onClick = { onSetLanguage("gu") },
                label = { Text("ગુજરાતી", fontWeight = if (currentLanguage == "gu") FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.weight(1f)
            )
        }

        // ── Section: Accessibility ──
        MdSection(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MdIcon(Icons.Default.FormatSize, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Accessibility & Comfort",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            action = {},
            padding = PaddingValues(horizontal = 0.dp, vertical = 12.dp)
        )
        MdDivider(modifier = Modifier.padding(horizontal = 0.dp))

        Text(
            text = stringResource(R.string.font_size_scale),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MdChip(
                selected = fontScale == 1.0f,
                onClick = { onSetFontScale(1.0f) },
                label = { Text("Normal (100%)") },
                modifier = Modifier.weight(1f)
            )
            MdChip(
                selected = fontScale == 1.15f,
                onClick = { onSetFontScale(1.15f) },
                label = { Text("Large (115%)") },
                modifier = Modifier.weight(1f)
            )
            MdChip(
                selected = fontScale == 1.3f,
                onClick = { onSetFontScale(1.3f) },
                label = { Text("Extra Large") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "High Contrast Mode",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Darkens text and outlines for enhanced legibility",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isHighContrast,
                onCheckedChange = onToggleHighContrast
            )
        }

        // ── Section: Privacy & Security ──
        MdSection(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MdIcon(Icons.Default.Security, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.app_lock),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            action = {},
            padding = PaddingValues(horizontal = 0.dp, vertical = 12.dp)
        )
        MdDivider(modifier = Modifier.padding(horizontal = 0.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.pin_lock),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Require 4-digit PIN or fingerprint to access",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MdPrimaryButton(
                onClick = { showPinDialog = true },
                label = { Text("Set PIN") },
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.flag_secure_label),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Blocks screenshots and masks screen in Android recents",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isFlagSecure,
                onCheckedChange = onToggleFlagSecure
            )
        }

        // ── Section: Reminder reliability ──
        MdSection(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MdIcon(Icons.Default.Alarm, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.battery_optimization_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            action = {},
            padding = PaddingValues(horizontal = 0.dp, vertical = 12.dp)
        )
        MdDivider(modifier = Modifier.padding(horizontal = 0.dp))

        Text(
            text = stringResource(R.string.battery_optimization_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        MdOutlineButton(
            onClick = {
                try {
                    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Battery optimizations already disabled!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                }
            },
            label = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.disable_battery_optimization))
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        // ── Section: Backup & Restore ──
        MdSection(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MdIcon(Icons.Default.Backup, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Encrypted Backup & Restore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            action = {},
            padding = PaddingValues(horizontal = 0.dp, vertical = 12.dp)
        )
        MdDivider(modifier = Modifier.padding(horizontal = 0.dp))

        Text(
            text = "Export your encrypted records and medicines safely using password-protected encryption.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MdPrimaryButton(
                onClick = {
                    createBackupLauncher.launch("dawadiary_backup_${System.currentTimeMillis()}.dawa")
                },
                label = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export")
                    }
                },
                modifier = Modifier.weight(1f).height(46.dp)
            )
            MdOutlineButton(
                onClick = {
                    openBackupLauncher.launch(arrayOf("*/*"))
                },
                label = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore")
                    }
                },
                modifier = Modifier.weight(1f).height(46.dp)
            )
        }

        // ── Section: About ──
        MdSection(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MdIcon(Icons.Default.Info, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "About DawaDiary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            action = {},
            padding = PaddingValues(horizontal = 0.dp, vertical = 12.dp)
        )
        MdDivider(modifier = Modifier.padding(horizontal = 0.dp))

        Text(
            text = "DawaDiary is a patient-facing medication reminder and family health records vault with local AES-256 encrypted storage.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        MdTextButton(
            onClick = { showDisclaimerInfoDialog = true },
            modifier = Modifier.padding(horizontal = 16.dp).align(Alignment.Start),
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Medical Disclaimer & Privacy Notice")
                }
            }
        )

        // ── Danger Zone: Erase all data ──
        MdSection(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MdIcon(Icons.Default.DeleteForever, tint = MdError)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.delete_all_data),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MdError
                    )
                }
            },
            action = {},
            padding = PaddingValues(horizontal = 0.dp, vertical = 12.dp)
        )
        MdDivider(modifier = Modifier.padding(horizontal = 0.dp))

        Text(
            text = stringResource(R.string.delete_all_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        MdPrimaryButton(
            onClick = { showDeleteConfirmDialog = true },
            label = { Text("Erase All Data Permanently", fontWeight = FontWeight.Bold) },
            fullWidth = true,
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = MdError,
            contentColor = Color.White
        )
    }

    // ── Dialogs ──
    if (showPinDialog) {
        SetPinDialog(
            onDismiss = { showPinDialog = false },
            onPinSaved = { pin ->
                onSetPin(pin)
                showPinDialog = false
                Toast.makeText(context, "PIN Saved Successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showBackupPasswordDialog && pendingBackupUri != null) {
        BackupPasswordDialog(
            title = "Set Backup Password",
            buttonLabel = "Export Backup",
            onDismiss = { showBackupPasswordDialog = false },
            onSubmit = { pwd ->
                showBackupPasswordDialog = false
                coroutineScope.launch {
                    val success = BackupManager.createEncryptedBackup(context, pendingBackupUri!!, pwd)
                    if (success) {
                        Toast.makeText(context, "Encrypted backup saved successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Failed to create backup.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (showRestorePasswordDialog && pendingRestoreUri != null) {
        BackupPasswordDialog(
            title = "Enter Backup Password",
            buttonLabel = "Restore Backup",
            onDismiss = { showRestorePasswordDialog = false },
            onSubmit = { pwd ->
                showRestorePasswordDialog = false
                coroutineScope.launch {
                    val success = BackupManager.restoreEncryptedBackup(context, pendingRestoreUri!!, pwd)
                    if (success) {
                        Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Restore failed. Please check password.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (showDisclaimerInfoDialog) {
        MdInfoDialog(
            title = "Medical Disclaimer & Privacy",
            body = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.medical_disclaimer_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.dpdp_consent_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            onDismiss = { showDisclaimerInfoDialog = false }
        )
    }

    if (showDeleteConfirmDialog) {
        MdConfirmDialog(
            title = "Erase All Data?",
            message = "Are you absolutely sure? All encrypted records, medicine schedules, and profiles will be wiped from this phone forever. This action cannot be undone.",
            onConfirm = {
                showDeleteConfirmDialog = false
                onEraseAllData()
            },
            onDismiss = { showDeleteConfirmDialog = false },
            confirmLabel = "Erase Everything",
            confirmColor = MdError
        )
    }
}

@Composable
fun SetPinDialog(
    onDismiss: () -> Unit,
    onPinSaved: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 12.dp),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.set_pin_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                MdDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Enter a 4-digit numeric PIN to secure your app",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                MdTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4) {
                            pin = it.filter { char -> char.isDigit() }
                            if (error != null) error = null
                        }
                    },
                    label = { Text("4-Digit PIN") },
                    placeholder = { Text("••••") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                MdTextField(
                    value = confirmPin,
                    onValueChange = {
                        if (it.length <= 4) {
                            confirmPin = it.filter { char -> char.isDigit() }
                            if (error != null) error = null
                        }
                    },
                    label = { Text("Confirm PIN") },
                    placeholder = { Text("••••") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MdTextButton(
                        onClick = onDismiss,
                        label = { Text(stringResource(R.string.cancel)) }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    MdPrimaryButton(
                        onClick = {
                            if (pin.length != 4) {
                                error = "PIN must be exactly 4 digits"
                                return@MdPrimaryButton
                            }
                            if (pin != confirmPin) {
                                error = "PINs do not match"
                                return@MdPrimaryButton
                            }
                            onPinSaved(pin)
                        },
                        label = { Text("Save PIN", fontWeight = FontWeight.Bold) },
                    )
                }
            }
        }
    }
}

@Composable
fun BackupPasswordDialog(
    title: String,
    buttonLabel: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 12.dp),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                MdDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Enter a password to encrypt or decrypt your backup archive",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                MdTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (error != null) error = null
                    },
                    label = { Text("Backup Password") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MdTextButton(
                        onClick = onDismiss,
                        label = { Text(stringResource(R.string.cancel)) }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    MdPrimaryButton(
                        onClick = {
                            if (password.length < 4) {
                                error = "Password must be at least 4 characters"
                                return@MdPrimaryButton
                            }
                            onSubmit(password)
                        },
                        label = { Text(buttonLabel, fontWeight = FontWeight.Bold) },
                    )
                }
            }
        }
    }
}
