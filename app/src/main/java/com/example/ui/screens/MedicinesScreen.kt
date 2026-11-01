package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
            .testTag("medicines_screen")
    ) {
        // ΓöÇΓöÇ Header ΓöÇΓöÇ
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
        ) {
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
        MdDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // Stitch-inspired schedule summary: live count, never demo data.
        Row(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
                .background(MdPrimaryContainer, RoundedCornerShape(999.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Medication, contentDescription = null, tint = MdPrimary, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(7.dp))
            Text(
                text = "${medicines.size} ACTIVE MEDICINE${if (medicines.size == 1) "" else "S"}",
                style = MaterialTheme.typography.labelSmall,
                color = MdPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // ΓöÇΓöÇ Content ΓöÇΓöÇ
        if (medicines.isEmpty()) {
            MdEmptyState(
                icon = Icons.Default.Medication,
                title = "No Medicines Added Yet",
                subtitle = stringResource(R.string.empty_medicines),
                action = {
                    MdPrimaryButton(
                        onClick = {
                            editingMedicineWithSchedule = null
                            showDialog = true
                        },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.add_medicine), fontWeight = FontWeight.SemiBold)
                            }
                        },
                        fullWidth = true
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(medicines, key = { it.medicine.id }) { medWithSched ->
                    MedicineRow(
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

    // ΓöÇΓöÇ Add / edit dialog ΓöÇΓöÇ
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

    // ΓöÇΓöÇ Delete confirm ΓöÇΓöÇ
    if (medicineToDelete != null) {
        MdConfirmDialog(
            title = "Delete Medicine?",
            message = "Are you sure you want to remove ${medicineToDelete?.name}? Scheduled reminders for this medicine will be cancelled.",
            onConfirm = {
                medicineToDelete?.let { onDeleteMedicine(it) }
                medicineToDelete = null
            },
            onDismiss = { medicineToDelete = null },
            confirmLabel = "Delete",
            confirmColor = MdError
        )
    }
}

@Composable
fun MedicineRow(
    medWithSched: MedicineWithSchedule,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val med = medWithSched.medicine
    val sched = medWithSched.schedules.firstOrNull()
    val isLowStock = med.stockCount <= med.lowStockThreshold

    val rowShape = RoundedCornerShape(24.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .dawaCardShadow(4.dp, rowShape)
            .clip(rowShape)
            .clickable(onClick = onEdit)
            .testTag("medicine_row_${med.id}"),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = rowShape,
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
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
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
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Medicine",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Medicine",
                            tint = MdError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            MdDivider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

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
                    color = if (isLowStock) MdErrorContainer else MdSuccessContainer,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = if (isLowStock) "Low Stock: ${med.stockCount}" else "Stock: ${med.stockCount}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) MdError else MdSuccess,
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
                        text = if (existing == null) stringResource(R.string.add_medicine) else stringResource(R.string.edit_medicine),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                MdDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Section: Medicine Details
                Text(
                    text = "Medicine Details",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                MdTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (error != null) error = null
                    },
                    label = stringResource(R.string.medicine_name),
                    placeholder = "e.g. Paracetamol, Metformin",
                    isError = error != null,
                    errorMessage = error,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MdTextField(
                        value = strength,
                        onValueChange = { strength = it },
                        label = stringResource(R.string.medicine_strength),
                        placeholder = "500 mg",
                        modifier = Modifier.weight(1f)
                    )
                    MdTextField(
                        value = doseDescription,
                        onValueChange = { doseDescription = it },
                        label = "Dosage",
                        placeholder = "1 tablet",
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Medicine Form",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val forms = listOf("Tablet", "Capsule", "Syrup", "Injection", "Drops", "Inhaler")
                forms.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { f ->
                            MdChip(
                                selected = form == f,
                                onClick = { form = f },
                                label = { Text(f) }
                            )
                        }
                    }
                }

                // Section: Schedule & Food Instructions
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
                    MdChip(
                        selected = foodInstruction == "BEFORE_FOOD",
                        onClick = { foodInstruction = "BEFORE_FOOD" },
                        label = { Text("Before Food") }
                    )
                    MdChip(
                        selected = foodInstruction == "AFTER_FOOD",
                        onClick = { foodInstruction = "AFTER_FOOD" },
                        label = { Text("After Food") }
                    )
                    MdChip(
                        selected = foodInstruction == "ANY_TIME",
                        onClick = { foodInstruction = "ANY_TIME" },
                        label = { Text("Anytime") }
                    )
                }

                MdTextField(
                    value = timesOfDay,
                    onValueChange = { timesOfDay = it },
                    label = "Reminder Times (24h, comma separated)",
                    placeholder = "08:00, 14:00, 20:00",
                    modifier = Modifier.fillMaxWidth()
                )

                // Section: Doctor & Stock
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Doctor & Inventory",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                MdTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = stringResource(R.string.prescribing_doctor),
                    placeholder = "e.g. Dr. Sharma",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MdTextField(
                        value = stockCountStr,
                        onValueChange = { stockCountStr = it.filter { char -> char.isDigit() } },
                        label = stringResource(R.string.stock_count),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    MdTextField(
                        value = thresholdStr,
                        onValueChange = { thresholdStr = it.filter { char -> char.isDigit() } },
                        label = stringResource(R.string.low_stock_threshold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

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
                            if (name.isBlank()) {
                                error = "Medicine name is required"
                                return@MdPrimaryButton
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
                        label = { Text(stringResource(R.string.save), fontWeight = FontWeight.Bold) },
                        fullWidth = false,
                    )
                }
            }
        }
    }
}

