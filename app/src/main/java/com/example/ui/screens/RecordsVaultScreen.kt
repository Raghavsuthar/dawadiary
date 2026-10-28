package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.dao.RecordWithFiles
import com.example.data.entity.ProfileEntity
import com.example.data.entity.RecordEntity
import com.example.data.entity.RecordFileEntity
import com.example.ui.theme.*
import com.example.util.FileManager
import java.time.LocalDate

@Composable
fun RecordsVaultScreen(
    activeProfile: ProfileEntity?,
    records: List<RecordWithFiles>,
    onSaveRecord: (RecordEntity, List<Pair<Uri, String>>, List<String>) -> Unit,
    onDeleteRecord: (RecordWithFiles) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var viewRecord by remember { mutableStateOf<RecordWithFiles?>(null) }

    val filteredRecords = records.filter { recWithFiles ->
        val rec = recWithFiles.record
        val matchesQuery = searchQuery.isBlank() ||
                rec.title.contains(searchQuery, ignoreCase = true) ||
                rec.doctorHospital.contains(searchQuery, ignoreCase = true) ||
                recWithFiles.tags.any { it.name.contains(searchQuery, ignoreCase = true) }

        val matchesFilter = when (selectedFilter) {
            "PRESCRIPTION" -> rec.recordType == "PRESCRIPTION"
            "LAB_REPORT" -> rec.recordType == "LAB_REPORT"
            "IMAGING" -> rec.recordType == "IMAGING"
            "VACCINATION" -> rec.recordType == "VACCINATION"
            else -> true
        }

        matchesQuery && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_record_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Medical Record")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = "Medical Records Vault",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "End-to-End Encrypted (AES-256-GCM) on-device",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MdDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Search bar
            MdTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search by title, doctor, tag",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Category filter chips
            val categories = listOf(
                "ALL" to "All",
                "PRESCRIPTION" to "Prescriptions",
                "LAB_REPORT" to "Lab Reports",
                "IMAGING" to "Scans & Imaging",
                "VACCINATION" to "Vaccines"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            if (filteredRecords.isEmpty()) {
                MdEmptyState(
                    icon = Icons.Default.Folder,
                    title = "No Records Found",
                    subtitle = "Upload prescriptions, test reports, or scan summaries safely.",
                    action = {
                        MdPrimaryButton(
                            onClick = { showAddDialog = true },
                            label = { Text("Add First Record", fontWeight = FontWeight.SemiBold) }
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRecords, key = { it.record.id }) { recWithFiles ->
                        RecordRow(
                            recordWithFiles = recWithFiles,
                            onClick = { viewRecord = recWithFiles },
                            onDelete = { onDeleteRecord(recWithFiles) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddRecordDialog(
            activeProfileId = activeProfile?.id ?: 1L,
            onDismiss = { showAddDialog = false },
            onSave = { record, uris, tags ->
                onSaveRecord(record, uris, tags)
                showAddDialog = false
            }
        )
    }

    if (viewRecord != null) {
        ViewRecordDetailsDialog(
            recordWithFiles = viewRecord!!,
            onDismiss = { viewRecord = null }
        )
    }
}

@Composable
fun RecordRow(
    recordWithFiles: RecordWithFiles,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val rec = recordWithFiles.record

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("record_row_${rec.id}"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (rec.recordType) {
                                "PRESCRIPTION" -> Icons.Default.Description
                                "LAB_REPORT" -> Icons.Default.Science
                                "IMAGING" -> Icons.Default.CameraAlt
                                "VACCINATION" -> Icons.Default.Vaccines
                                "BILL_INSURANCE" -> Icons.Default.ReceiptLong
                                else -> Icons.Default.Folder
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = rec.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${rec.date} • ${rec.doctorHospital.ifBlank { "General Health" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (recordWithFiles.files.isNotEmpty() || recordWithFiles.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (recordWithFiles.files.isNotEmpty()) {
                        MdStatusChip(
                            bgColor = MaterialTheme.colorScheme.surfaceVariant,
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            icon = Icons.Default.AttachFile,
                            text = "${recordWithFiles.files.size} file(s)"
                        )
                    }
                    for (t in recordWithFiles.tags.take(3)) {
                        MdStatusChip(
                            bgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            textColor = MaterialTheme.colorScheme.primary,
                            text = "#${t.name}"
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordDialog(
    activeProfileId: Long,
    onDismiss: () -> Unit,
    onSave: (RecordEntity, List<Pair<Uri, String>>, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var recordType by remember { mutableStateOf("PRESCRIPTION") }
    var notes by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    val attachedUris = remember { mutableStateListOf<Pair<Uri, String>>() }

    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        for (u in uris) {
            val name = u.lastPathSegment ?: "file_${System.currentTimeMillis()}"
            attachedUris.add(Pair(u, name))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medical Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MdTextField(value = title, onValueChange = { title = it }, label = "Record Title *")
                MdTextField(value = doctor, onValueChange = { doctor = it }, label = "Doctor / Hospital")

                var typeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = recordType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Record Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        listOf("PRESCRIPTION", "LAB_REPORT", "IMAGING", "VACCINATION", "BILL_INSURANCE", "OTHER").forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    recordType = t
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                MdTextField(value = tagsInput, onValueChange = { tagsInput = it }, label = "Tags (comma separated)")
                MdTextField(value = notes, onValueChange = { notes = it }, label = "Doctor Notes", singleLine = false, maxLines = 3)

                OutlinedButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Attach Document / Scan (${attachedUris.size})")
                }
            }
        },
        confirmButton = {
            MdPrimaryButton(
                onClick = {
                    if (title.isBlank()) return@MdPrimaryButton
                    val rec = RecordEntity(
                        profileId = activeProfileId,
                        title = title.trim(),
                        date = LocalDate.now().toString(),
                        doctorHospital = doctor.trim(),
                        recordType = recordType,
                        notes = notes.trim()
                    )
                    val tagList = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    onSave(rec, attachedUris.toList(), tagList)
                },
                label = { Text("Save Record", fontWeight = FontWeight.Bold) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ViewRecordDetailsDialog(
    recordWithFiles: RecordWithFiles,
    onDismiss: () -> Unit
) {
    val rec = recordWithFiles.record
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(rec.title, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Date: ${rec.date}", style = MaterialTheme.typography.bodyMedium)
                if (rec.doctorHospital.isNotBlank()) {
                    Text(text = "Doctor / Hospital: ${rec.doctorHospital}", style = MaterialTheme.typography.bodyMedium)
                }
                Text(text = "Type: ${rec.recordType}", style = MaterialTheme.typography.bodyMedium)

                if (rec.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Notes:\n${rec.notes}", style = MaterialTheme.typography.bodySmall)
                }

                if (recordWithFiles.files.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Attached Files (${recordWithFiles.files.size}):", fontWeight = FontWeight.Bold)
                    for (f in recordWithFiles.files) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = f.fileName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    val decrypted = FileManager.decryptToShareableFile(context, f.encryptedFilePath, f.fileName)
                                    if (decrypted != null) {
                                        val uri = FileManager.getShareableUri(context, decrypted)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = f.mimeType
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Document"))
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
