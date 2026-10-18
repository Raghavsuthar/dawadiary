package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.dao.MedicineWithSchedule
import com.example.data.entity.MedicineEntity
import com.example.data.entity.ProfileEntity
import com.example.data.entity.ScheduleEntity
import com.example.ui.theme.*
import java.time.LocalDate

@Composable
fun MedicinesScreen(
    activeProfile: ProfileEntity?,
    medicines: List<MedicineWithSchedule>,
    onSaveMedicine: (MedicineEntity, ScheduleEntity) -> Unit,
    onDeleteMedicine: (MedicineEntity) -> Unit,
    showAddDialogInitially: Boolean = false
) {
    var showDialog by remember { mutableStateOf(showAddDialogInitially) }
    var editingMedicineWithSchedule by remember { mutableStateOf<MedicineWithSchedule?>(null) }
    var medicineToDelete by remember { mutableStateOf<MedicineEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingMedicineWithSchedule = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("add_medicine_screen_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_medicine)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("medicines_screen")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.medicines_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = activeProfile?.name?.let { "Prescribed for $it" } ?: "Active Regimens",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${medicines.size} Active",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (medicines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Medication,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Medicines Added Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.empty_medicines),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    editingMedicineWithSchedule = null
                                    showDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.add_medicine), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(medicines, key = { it.medicine.id }) { medWithSched ->
                        MedicineCardItem(
                            medWithSched = medWithSched,
                            onEdit = {
                                editingMedicineWithSchedule = medWithSched
                                showDialog = true
                            },
                            onDelete = { medicineToDelete = medWithSched.medicine }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddEditMedicineDialog(
            activeProfileId = activeProfile?.id ?: 1L,
            existing = editingMedicineWithSchedule,
            onDismiss = {
                showDialog = false
                editingMedicineWithSchedule = null
            },
            onSave = { med, sched ->
                onSaveMedicine(med, sched)
                showDialog = false
                editingMedicineWithSchedule = null
            }
        )
    }

    if (medicineToDelete != null) {
        AlertDialog(
            onDismissRequest = { medicineToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Medicine?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove ${medicineToDelete?.name}? Scheduled reminders for this medicine will be cancelled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        medicineToDelete?.let { onDeleteMedicine(it) }
                        medicineToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { medicineToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun MedicineCardItem(
    medWithSched: MedicineWithSchedule,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val med = medWithSched.medicine
    val sched = medWithSched.schedules.firstOrNull()
    val isLowStock = med.stockCount <= med.lowStockThreshold

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medicine_card_${med.id}")
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (med.form.lowercase()) {
                                "syrup" -> Icons.Default.WineBar
                                "injection" -> Icons.Default.Vaccines
                                "drops" -> Icons.Default.WaterDrop
                                else -> Icons.Default.Medication
                            },
                            contentDescription = med.form,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = med.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${med.doseDescription} • ${med.strength} (${med.form})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Medicine",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Medicine",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Times: ${sched?.timesOfDay ?: "Not set"}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (med.prescribingDoctor.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Dr. ${med.prescribingDoctor}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isLowStock) MedicalErrorContainer else MedicalSuccessContainer
                ) {
                    Text(
                        text = if (isLowStock) "Low Stock: ${med.stockCount}" else "Stock: ${med.stockCount}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) MedicalError else MedicalSuccess,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditMedicineDialog(
    activeProfileId: Long,
    existing: MedicineWithSchedule?,
    onDismiss: () -> Unit,
    onSave: (MedicineEntity, ScheduleEntity) -> Unit
) {
    var name by remember { mutableStateOf(existing?.medicine?.name.orEmpty()) }
    var strength by remember { mutableStateOf(existing?.medicine?.strength.orEmpty()) }
    var form by remember { mutableStateOf(existing?.medicine?.form ?: "Tablet") }
    var doseDescription by remember { mutableStateOf(existing?.medicine?.doseDescription ?: "1 tablet") }
    var foodInstruction by remember { mutableStateOf(existing?.medicine?.foodInstruction ?: "AFTER_FOOD") }
    var doctor by remember { mutableStateOf(existing?.medicine?.prescribingDoctor.orEmpty()) }
    var stockCountStr by remember { mutableStateOf((existing?.medicine?.stockCount ?: 30).toString()) }
    var thresholdStr by remember { mutableStateOf((existing?.medicine?.lowStockThreshold ?: 5).toString()) }

    var scheduleType by remember { mutableStateOf(existing?.schedules?.firstOrNull()?.scheduleType ?: "DAILY") }
    var timesOfDay by remember { mutableStateOf(existing?.schedules?.firstOrNull()?.timesOfDay ?: "08:00,20:00") }

    var error by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
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
                        text = if (existing == null) stringResource(R.string.add_medicine) else stringResource(R.string.edit_medicine),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                // Section 1: Medicine Details
                Text(
                    text = "Medicine Details",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (error != null) error = null
                    },
                    label = { Text(stringResource(R.string.medicine_name)) },
                    placeholder = { Text("e.g. Paracetamol, Metformin") },
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = strength,
                        onValueChange = { strength = it },
                        label = { Text(stringResource(R.string.medicine_strength)) },
                        placeholder = { Text("500 mg") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = doseDescription,
                        onValueChange = { doseDescription = it },
                        label = { Text("Dosage") },
                        placeholder = { Text("1 tablet") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Text(
                    text = "Medicine Form",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val forms = listOf("Tablet", "Capsule", "Syrup", "Injection", "Drops", "Inhaler")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    forms.take(3).forEach { f ->
                        FilterChip(
                            selected = form == f,
                            onClick = { form = f },
                            label = { Text(f) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    forms.drop(3).forEach { f ->
                        FilterChip(
                            selected = form == f,
                            onClick = { form = f },
                            label = { Text(f) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Section 2: Instructions & Timing
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Schedule & Food Instructions",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(R.string.food_instruction),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = foodInstruction == "BEFORE_FOOD",
                        onClick = { foodInstruction = "BEFORE_FOOD" },
                        label = { Text("Before Food") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = foodInstruction == "AFTER_FOOD",
                        onClick = { foodInstruction = "AFTER_FOOD" },
                        label = { Text("After Food") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = foodInstruction == "ANY_TIME",
                        onClick = { foodInstruction = "ANY_TIME" },
                        label = { Text("Anytime") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                OutlinedTextField(
                    value = timesOfDay,
                    onValueChange = { timesOfDay = it },
                    label = { Text("Reminder Times (24h format, comma separated)") },
                    placeholder = { Text("08:00, 14:00, 20:00") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Section 3: Doctor & Stock
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Doctor & Inventory",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text(stringResource(R.string.prescribing_doctor)) },
                    placeholder = { Text("e.g. Dr. Sharma") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockCountStr,
                        onValueChange = { stockCountStr = it.filter { char -> char.isDigit() } },
                        label = { Text(stringResource(R.string.stock_count)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = thresholdStr,
                        onValueChange = { thresholdStr = it.filter { char -> char.isDigit() } },
                        label = { Text(stringResource(R.string.low_stock_threshold)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                error = "Medicine name is required"
                                return@Button
                            }
                            val stock = stockCountStr.toIntOrNull() ?: 0
                            val threshold = thresholdStr.toIntOrNull() ?: 5
                            val med = MedicineEntity(
                                id = existing?.medicine?.id ?: 0L,
                                profileId = activeProfileId,
                                name = name.trim(),
                                strength = strength.trim(),
                                form = form,
                                doseDescription = doseDescription.trim(),
                                foodInstruction = foodInstruction,
                                prescribingDoctor = doctor.trim(),
                                startDate = existing?.medicine?.startDate ?: LocalDate.now().toString(),
                                stockCount = stock,
                                lowStockThreshold = threshold
                            )
                            val cleanTimes = timesOfDay.split(",")
                                .map { it.trim() }
                                .filter { it.matches(Regex("\\d{1,2}:\\d{2}")) }
                                .joinToString(",") {
                                    val parts = it.split(":")
                                    "%02d:%02d".format(parts[0].toInt(), parts[1].toInt())
                                }

                            val sched = ScheduleEntity(
                                id = existing?.schedules?.firstOrNull()?.id ?: 0L,
                                medicineId = existing?.medicine?.id ?: 0L,
                                scheduleType = scheduleType,
                                timesOfDay = if (cleanTimes.isNotEmpty()) cleanTimes else "08:00"
                            )
                            onSave(med, sched)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
