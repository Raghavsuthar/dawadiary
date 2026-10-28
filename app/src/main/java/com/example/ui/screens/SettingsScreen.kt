package com.example.ui.screens

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.unit.dp
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
    val scope = rememberCoroutineScope()

    var showPinDialog by remember { mutableStateOf(false) }
    var showEraseDialog by remember { mutableStateOf(false) }
    var showBackupPasswordDialog by remember { mutableStateOf(false) }
    var showRestorePasswordDialog by remember { mutableStateOf(false) }
    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = BackupManager.createEncryptedBackup(context, "DawaDiaryBackupKey", uri)
                statusMessage = if (success) "Backup exported successfully!" else "Failed to export backup."
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

        if (statusMessage != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(statusMessage.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
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
                        subtitle = { Text("Require PIN to open app or view medical vault", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailing = {
                            TextButton(onClick = { showPinDialog = true }) {
                                Text("Configure", fontWeight = FontWeight.Bold)
                            }
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
                        subtitle = { Text("Export profiles and medication data protected by password", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailing = {
                            TextButton(onClick = { createBackupLauncher.launch("dawadiary_backup_${System.currentTimeMillis()}.dawadiary") }) {
                                Text("Export", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    MdDivider()
                    MdListRow(
                        leading = { Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        title = { Text("Restore From Backup File", fontWeight = FontWeight.SemiBold) },
                        subtitle = { Text("Restore data from an encrypted .dawadiary file", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Configure Security PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a 4-digit PIN to protect DawaDiary.")
                    MdTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = "4-Digit PIN"
                    )
                }
            },
            confirmButton = {
                MdPrimaryButton(
                    onClick = {
                        if (pinInput.length == 4) {
                            onSetPin(pinInput)
                            showPinDialog = false
                            statusMessage = "PIN configured successfully."
                        }
                    },
                    label = { Text("Set PIN") }
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    onClearPin()
                    showPinDialog = false
                    statusMessage = "PIN removed."
                }) {
                    Text("Remove PIN", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    if (showRestorePasswordDialog && selectedRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { showRestorePasswordDialog = false },
            title = { Text("Confirm Backup Restore", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to restore data from this backup file?") },
            confirmButton = {
                MdPrimaryButton(
                    onClick = {
                        scope.launch {
                            val success = BackupManager.restoreEncryptedBackup(context, "DawaDiaryBackupKey", selectedRestoreUri!!)
                            statusMessage = if (success) "Backup restored successfully!" else "Failed to restore backup."
                            showRestorePasswordDialog = false
                        }
                    },
                    label = { Text("Confirm Restore") }
                )
            },
            dismissButton = {
                TextButton(onClick = { showRestorePasswordDialog = false }) { Text("Cancel") }
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
