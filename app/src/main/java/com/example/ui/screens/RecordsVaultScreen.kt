package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.dao.RecordWithFiles
import com.example.data.entity.ProfileEntity
import com.example.data.entity.RecordEntity
import com.example.ui.theme.*
import com.example.util.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

@Composable
fun RecordsVaultScreen(
    activeProfile: ProfileEntity?,
    records: List<RecordWithFiles>,
    onSaveRecord: (RecordEntity, List<Pair<Uri, String>>, List<String>) -> Unit,
    onDeleteRecord: (RecordWithFiles) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var viewingRecord by remember { mutableStateOf<RecordWithFiles?>(null) }
    var recordToDelete by remember { mutableStateOf<RecordWithFiles?>(null) }

    val categories = listOf(
        "ALL" to "All",
        "PRESCRIPTION" to "Prescriptions",
        "LAB_REPORT" to "Lab Reports",
        "IMAGING" to "Imaging / Scans",
        "DISCHARGE_SUMMARY" to "Discharge",
        "VACCINATION" to "Vaccine",
        "BILL_INSURANCE" to "Bills / Insurance",
        "DOCTOR_NOTE" to "Doctor Notes",
        "OTHER" to "Other"
    )

    val filteredRecords = records.filter { item ->
        val rec = item.record
        val matchesCategory = selectedCategory == "ALL" || rec.recordType.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
                rec.title.contains(searchQuery, ignoreCase = true) ||
                rec.doctorHospital.contains(searchQuery, ignoreCase = true) ||
                rec.notes.contains(searchQuery, ignoreCase = true) ||
                item.tags.any { it.name.contains(searchQuery, ignoreCase = true) }
        matchesCategory && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
            .testTag("records_vault_screen")
            .verticalScroll(rememberScrollState())
    ) {
        // ΓöÇΓöÇ Header ΓöÇΓöÇ
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.nav_records),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = activeProfile?.name?.let { "Records for $it" } ?: "Encrypted Health Vault",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        MdDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // ΓöÇΓöÇ Search ΓöÇΓöÇ
        MdTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = stringResource(R.string.search_records),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // ΓöÇΓöÇ Category chips ΓöÇΓöÇ
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { (key, label) ->
                MdChip(
                    selected = selectedCategory == key,
                    onClick = { selectedCategory = key },
                    label = { Text(label) }
                )
            }
        }

        // ΓöÇΓöÇ Content ΓöÇΓöÇ
        if (filteredRecords.isEmpty()) {
            MdEmptyState(
                icon = if (searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.FolderOpen,
                title = if (searchQuery.isNotEmpty()) "No Matching Records" else "No Records Stored Yet",
                subtitle = if (searchQuery.isNotEmpty()) "Try adjusting your search terms or clearing the filter." else stringResource(R.string.empty_records),
                action = if (searchQuery.isEmpty()) {
                    @Composable {
                        MdPrimaryButton(
                            onClick = { showAddDialog = true },
                            label = {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.add_record), fontWeight = FontWeight.SemiBold)
                                }
                            },
                            fullWidth = true
                        )
                    }
                } else {
                    @Composable {}
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRecords, key = { it.record.id }) { item ->
                    RecordRow(
                        recordWithFiles = item,
                        onClick = { viewingRecord = item },
                        onDelete = { recordToDelete = item }
                    )
                }
            }
        }
    }

    // ΓöÇΓöÇ Add record dialog ΓöÇΓöÇ
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

    // ΓöÇΓöÇ View record modal ΓöÇΓöÇ
    if (viewingRecord != null) {
        ViewRecordModal(
            recordWithFiles = viewingRecord!!,
            onDismiss = { viewingRecord = null }
        )
    }

    // ΓöÇΓöÇ Delete confirm ΓöÇΓöÇ
    if (recordToDelete != null) {
        MdConfirmDialog(
            title = "Delete Medical Record?",
            message = "Are you sure you want to delete '${recordToDelete?.record?.title}'? Encrypted files associated with this record will also be removed.",
            onConfirm = {
                recordToDelete?.let { onDeleteRecord(it) }
                recordToDelete = null
            },
            onDismiss = { recordToDelete = null },
            confirmLabel = "Delete",
            confirmColor = MdError
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
            .dawaCardShadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("record_row_${rec.id}"),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(20.dp),
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
                            text = "${rec.recordType.replace("_", " ")} • ${rec.date}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Record",
                        tint = MdError,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (rec.doctorHospital.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = rec.doctorHospital,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (rec.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = rec.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            MdDivider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    recordWithFiles.tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#${tag.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (recordWithFiles.files.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${recordWithFiles.files.size} Encrypted File(s)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddRecordDialog(
    activeProfileId: Long,
    onDismiss: () -> Unit,
    onSave: (RecordEntity, List<Pair<Uri, String>>, List<String>) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var recordType by remember { mutableStateOf("PRESCRIPTION") }
    var doctor by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    val attachedUris = remember { mutableStateListOf<Pair<Uri, String>>() }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var titleError by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = it.lastPathSegment ?: "document_${System.currentTimeMillis()}"
            attachedUris.add(Pair(it, fileName))
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            attachedUris.add(Pair(it, fileName))
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { bmp ->
            capturedBitmap = bmp
            try {
                val tempDir = FileManager.getCacheImagesDir(context)
                val tempFile = File(tempDir, "temp_scan_${System.currentTimeMillis()}.jpg")
                java.io.FileOutputStream(tempFile).use { fos ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 92, fos)
                }
                val contentUri = FileManager.getShareableUri(context, tempFile)
                attachedUris.add(Pair(contentUri, "Scan_${System.currentTimeMillis()}.jpg"))
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Failed to process captured image", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (t: Throwable) {
                android.widget.Toast.makeText(
                    context,
                    "Camera unavailable: ${t.localizedMessage ?: "Could not launch camera"}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            android.widget.Toast.makeText(
                context,
                "Camera permission is needed to scan documents",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

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
                        text = stringResource(R.string.add_record),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                MdDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Section: Record Information
                Text(
                    text = "Record Information",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                MdTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError != null) titleError = null
                    },
                    label = stringResource(R.string.record_title),
                    placeholder = "e.g. Dr. Sharma Prescription, CBC Blood Test",
                    isError = titleError != null,
                    errorMessage = titleError,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Record Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val types = listOf(
                    "PRESCRIPTION" to "Prescription",
                    "LAB_REPORT" to "Lab Report",
                    "IMAGING" to "Imaging",
                    "DISCHARGE_SUMMARY" to "Discharge",
                    "VACCINATION" to "Vaccine",
                    "BILL_INSURANCE" to "Bill / Insurance"
                )
                types.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { (key, label) ->
                            MdChip(
                                selected = recordType == key,
                                onClick = { recordType = key },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                // Section: Doctor / Hospital & Notes
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Doctor / Hospital & Notes",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                MdTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = stringResource(R.string.doctor_hospital),
                    placeholder = "e.g. City Care Hospital, Dr. Verma",
                    modifier = Modifier.fillMaxWidth()
                )

                MdTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = stringResource(R.string.notes_label),
                    placeholder = "Follow-up advice, diagnosis notes, dosage guidance",
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    singleLine = false
                )

                MdTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = "Tags (comma separated)",
                    placeholder = "e.g. Cardiology, 2026, AnnualCheckup",
                    modifier = Modifier.fillMaxWidth()
                )

                // Section: Attachments
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Encrypted Attachments",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${attachedUris.size} selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MdOutlineButton(
                        onClick = {
                            val hasCamPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasCamPerm) {
                                try {
                                    cameraLauncher.launch(null)
                                } catch (t: Throwable) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Could not open camera: ${t.localizedMessage ?: "Camera error"}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                try {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                } catch (t: Throwable) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Could not request camera permission",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MdOutlineButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MdOutlineButton(
                        onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("File")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (attachedUris.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        attachedUris.forEachIndexed { index, pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pair.second,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { attachedUris.removeAt(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove file",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                            if (title.isBlank()) {
                                titleError = "Record title is required"
                                return@MdPrimaryButton
                            }
                            val record = RecordEntity(
                                profileId = activeProfileId,
                                title = title.trim(),
                                date = LocalDate.now().toString(),
                                doctorHospital = doctor.trim(),
                                recordType = recordType,
                                notes = notes.trim()
                            )
                            val tagList = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            onSave(record, attachedUris.toList(), tagList)
                        },
                        label = { Text(stringResource(R.string.save), fontWeight = FontWeight.Bold) },
                    )
                }
            }
        }
    }
}

@Composable
fun ViewRecordModal(
    recordWithFiles: RecordWithFiles,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val rec = recordWithFiles.record

    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(recordWithFiles) {
        val firstFile = recordWithFiles.files.firstOrNull()
        if (firstFile != null) {
            withContext(Dispatchers.IO) {
                previewBitmap = FileManager.loadDecryptedBitmap(firstFile.encryptedFilePath)
            }
        }
    }

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rec.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text(
                        text = "${rec.recordType.replace("_", " ")} • Date: ${rec.date}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                if (rec.doctorHospital.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Doctor / Hospital: ${rec.doctorHospital}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (rec.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Clinical Notes:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = rec.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (previewBitmap != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Encrypted Document Preview:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    androidx.compose.foundation.Image(
                        bitmap = previewBitmap!!.asImageBitmap(),
                        contentDescription = "Decrypted Document Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                MdPrimaryButton(
                    onClick = {
                        val file = recordWithFiles.files.firstOrNull()
                        if (file != null) {
                            val decryptedTemp = FileManager.decryptToShareableFile(context, file.encryptedFilePath, file.fileName)
                            if (decryptedTemp != null) {
                                val uri = FileManager.getShareableUri(context, decryptedTemp)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = file.mimeType
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, rec.title)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Medical Record"))
                            }
                        } else {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "DawaDiary Record:\nTitle: ${rec.title}\nDate: ${rec.date}\nDoctor: ${rec.doctorHospital}\nNotes: ${rec.notes}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Record Summary"))
                        }
                    },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.share), fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                )
            }
        }
    }
}
