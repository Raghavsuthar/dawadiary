package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.dao.MedicineWithSchedule
import com.example.data.entity.AppointmentEntity
import com.example.data.entity.ProfileEntity
import com.example.data.entity.VitalEntity
import com.example.ui.components.EmergencyCardDialog
import com.example.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun FamilyScreen(
    profiles: List<ProfileEntity>,
    activeProfile: ProfileEntity?,
    activeMedicines: List<MedicineWithSchedule>,
    vitals: List<VitalEntity>,
    appointments: List<AppointmentEntity>,
    onSelectProfile: (Long) -> Unit,
    onSaveProfile: (ProfileEntity) -> Unit,
    onDeleteProfile: (ProfileEntity) -> Unit,
    onSaveVital: (VitalEntity) -> Unit,
    onSaveAppointment: (AppointmentEntity) -> Unit
) {
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<ProfileEntity?>(null) }
    var profileToDelete by remember { mutableStateOf<ProfileEntity?>(null) }
    var showEmergencyCard by remember { mutableStateOf(false) }
    var showLogVitalDialog by remember { mutableStateOf(false) }
    var showAddAppointmentDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
            .testTag("family_screen")
            .verticalScroll(rememberScrollState())
    ) {
        // ΓöÇΓöÇ Active Profile Health Summary (minimalist surface) ΓöÇΓöÇ
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.profiles_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Quick access & health overview",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        MdDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // Active profile card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MdAvatar(
                            initials = activeProfile?.name?.take(1) ?: "U",
                            size = 52.dp,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = activeProfile?.name ?: "Family Member",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${activeProfile?.relationship ?: "Self"} ΓÇó Blood Group: ${activeProfile?.bloodGroup?.ifBlank { "N/A" } ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    MdPrimaryButton(
                        onClick = { showEmergencyCard = true },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.emergency_card),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                if (!activeProfile?.allergies.isNullOrBlank() || !activeProfile?.chronicConditions.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    MdDivider(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(10.dp))

                    if (!activeProfile?.allergies.isNullOrBlank()) {
                        Text(
                            text = "Allergies: ${activeProfile?.allergies}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MdError,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (!activeProfile?.chronicConditions.isNullOrBlank()) {
                        Text(
                            text = "Conditions: ${activeProfile?.chronicConditions}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ΓöÇΓöÇ Profiles section ΓöÇΓöÇ
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.profiles_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                MdTextButton(
                    onClick = {
                        editingProfile = null
                        showAddProfileDialog = true
                    },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.add_profile), fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(profiles) { profile ->
                val isSelected = profile.id == activeProfile?.id
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = if (isSelected) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    },
                    modifier = Modifier
                        .clickable { onSelectProfile(profile.id) }
                        .widthIn(min = 140.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MdAvatar(
                            initials = profile.name.take(1),
                            size = 40.dp,
                            selected = isSelected,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = profile.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = profile.relationship,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isSelected) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = "Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ΓöÇΓöÇ Vitals section ΓöÇΓöÇ
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.vitals_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                MdTextButton(
                    onClick = { showLogVitalDialog = true },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.log_vital), fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }
        }

        if (vitals.isEmpty()) {
            MdEmptyState(
                icon = Icons.Default.FavoriteBorder,
                title = "No vitals recorded yet",
                subtitle = "Tap '+ Log Vital' to record blood pressure, sugar, weight, or SpO2.",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vitals.take(6), key = { it.id }) { vital ->
                    VitalRow(vital = vital)
                }
            }
        }

        // ΓöÇΓöÇ Appointments section ΓöÇΓöÇ
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.appointments_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                MdTextButton(
                    onClick = { showAddAppointmentDialog = true },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.add_appointment), fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }
        }

        if (appointments.isEmpty()) {
            MdEmptyState(
                icon = Icons.Default.EventNote,
                title = "No upcoming doctor appointments",
                subtitle = "Scheduled visits will appear here.",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(appointments, key = { it.id }) { appt ->
                    AppointmentRow(appointment = appt)
                }
            }
        }
    }

    if (showEmergencyCard) {
        EmergencyCardDialog(
            profile = activeProfile,
            activeMedicines = activeMedicines,
            onDismiss = { showEmergencyCard = false }
        )
    }

    if (showAddProfileDialog) {
        AddEditProfileDialog(
            existing = editingProfile,
            onDismiss = { showAddProfileDialog = false },
            onSave = { p ->
                onSaveProfile(p)
                showAddProfileDialog = false
            }
        )
    }

    if (showLogVitalDialog && activeProfile != null) {
        LogVitalDialog(
            profileId = activeProfile.id,
            onDismiss = { showLogVitalDialog = false },
            onSave = { v ->
                onSaveVital(v)
                showLogVitalDialog = false
            }
        )
    }

    if (showAddAppointmentDialog && activeProfile != null) {
        AddAppointmentDialog(
            profileId = activeProfile.id,
            onDismiss = { showAddAppointmentDialog = false },
            onSave = { a ->
                onSaveAppointment(a)
                showAddAppointmentDialog = false
            }
        )
    }

    if (profileToDelete != null) {
        MdConfirmDialog(
            title = "Delete Family Profile?",
            message = "Are you sure you want to delete profile '${profileToDelete?.name}'? All medicines and records for this profile will be permanently removed.",
            onConfirm = {
                profileToDelete?.let { onDeleteProfile(it) }
                profileToDelete = null
            },
            onDismiss = { profileToDelete = null },
            confirmLabel = "Delete",
            confirmColor = MdError
        )
    }
}

@Composable
fun VitalRow(vital: VitalEntity) {
    val dateStr = Instant.ofEpochMilli(vital.measuredAt)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("d MMM yyyy, hh:mm a"))

    val displayValue = if (vital.valueSecondary != null) {
        "${vital.valuePrimary.toInt()} / ${vital.valueSecondary.toInt()} ${vital.unit}"
    } else {
        "${vital.valuePrimary} ${vital.unit}"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (vital.vitalType) {
                            "BLOOD_PRESSURE" -> Icons.Default.Favorite
                            "BLOOD_SUGAR" -> Icons.Default.WaterDrop
                            "WEIGHT" -> Icons.Default.MonitorWeight
                            "SPO2" -> Icons.Default.Air
                            else -> Icons.Default.Thermostat
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = vital.vitalType.replace("_", " "),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(
                    text = displayValue,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun AppointmentRow(appointment: AppointmentEntity) {
    val dateStr = Instant.ofEpochMilli(appointment.appointmentDateTime)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy at hh:mm a"))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Dr. ${appointment.doctorName}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = appointment.specialty,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            MdDivider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            if (appointment.clinicHospital.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = appointment.clinicHospital,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (appointment.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = appointment.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun AddEditProfileDialog(
    existing: ProfileEntity?,
    onDismiss: () -> Unit,
    onSave: (ProfileEntity) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name.orEmpty()) }
    var relationship by remember { mutableStateOf(existing?.relationship ?: "Mother") }
    var dob by remember { mutableStateOf(existing?.dob ?: "1965-01-01") }
    var gender by remember { mutableStateOf(existing?.gender ?: "Female") }
    var bloodGroup by remember { mutableStateOf(existing?.bloodGroup ?: "B+") }
    var allergies by remember { mutableStateOf(existing?.allergies.orEmpty()) }
    var chronicConditions by remember { mutableStateOf(existing?.chronicConditions.orEmpty()) }
    var emergencyContactName by remember { mutableStateOf(existing?.emergencyContactName.orEmpty()) }
    var emergencyContactPhone by remember { mutableStateOf(existing?.emergencyContactPhone.orEmpty()) }
    var nameError by remember { mutableStateOf<String?>(null) }

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
                        text = if (existing == null) stringResource(R.string.add_profile) else stringResource(R.string.edit_profile),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                MdDivider(modifier = Modifier.padding(vertical = 4.dp))

                MdTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = stringResource(R.string.field_name),
                    placeholder = "e.g. Grandma Sunita, Father",
                    isError = nameError != null,
                    errorMessage = nameError,
                    modifier = Modifier.fillMaxWidth()
                )

                MdTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = stringResource(R.string.field_relationship),
                    placeholder = "e.g. Mother, Father, Spouse, Child",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MdTextField(
                        value = bloodGroup,
                        onValueChange = { bloodGroup = it },
                        label = "Blood Group",
                        placeholder = "A+, B+, O+",
                        modifier = Modifier.weight(1f)
                    )
                    MdTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = "Gender",
                        placeholder = "Female, Male",
                        modifier = Modifier.weight(1f)
                    )
                }

                MdTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = stringResource(R.string.field_allergies),
                    placeholder = "e.g. Penicillin, Peanuts (optional)",
                    modifier = Modifier.fillMaxWidth()
                )

                MdTextField(
                    value = chronicConditions,
                    onValueChange = { chronicConditions = it },
                    label = stringResource(R.string.field_chronic_conditions),
                    placeholder = "e.g. Hypertension, Type 2 Diabetes",
                    modifier = Modifier.fillMaxWidth()
                )

                MdTextField(
                    value = emergencyContactPhone,
                    onValueChange = { emergencyContactPhone = it },
                    label = stringResource(R.string.field_emergency_contact),
                    placeholder = "Phone number for emergencies",
                    modifier = Modifier.fillMaxWidth()
                )

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
                            if (name.isBlank()) {
                                nameError = "Name is required"
                                return@MdPrimaryButton
                            }
                            onSave(
                                ProfileEntity(
                                    id = existing?.id ?: 0L,
                                    name = name.trim(),
                                    relationship = relationship.trim(),
                                    dob = dob.trim(),
                                    gender = gender.trim(),
                                    bloodGroup = bloodGroup.trim(),
                                    allergies = allergies.trim(),
                                    chronicConditions = chronicConditions.trim(),
                                    emergencyContactName = emergencyContactName.trim(),
                                    emergencyContactPhone = emergencyContactPhone.trim(),
                                    isSelf = existing?.isSelf ?: false
                                )
                            )
                        },
                        label = { Text(stringResource(R.string.save), fontWeight = FontWeight.Bold) },
                    )
                }
            }
        }
    }
}

@Composable
fun LogVitalDialog(
    profileId: Long,
    onDismiss: () -> Unit,
    onSave: (VitalEntity) -> Unit
) {
    var vitalType by remember { mutableStateOf("BLOOD_PRESSURE") }
    var val1 by remember { mutableStateOf("120") }
    var val2 by remember { mutableStateOf("80") }

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
                        text = stringResource(R.string.log_vital),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                MdDivider(modifier = Modifier.padding(vertical = 4.dp))

                val types = listOf(
                    "BLOOD_PRESSURE" to "Blood Pressure",
                    "BLOOD_SUGAR" to "Blood Sugar",
                    "WEIGHT" to "Weight",
                    "SPO2" to "SpO2 Oxygen",
                    "TEMPERATURE" to "Temperature"
                )
                Text(
                    text = "Select Vital Sign",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                types.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { (k, label) ->
                            MdChip(
                                selected = vitalType == k,
                                onClick = {
                                    vitalType = k
                                    when (k) {
                                        "BLOOD_PRESSURE" -> { val1 = "120"; val2 = "80" }
                                        "BLOOD_SUGAR" -> { val1 = "110" }
                                        "WEIGHT" -> { val1 = "68" }
                                        "SPO2" -> { val1 = "98" }
                                        "TEMPERATURE" -> { val1 = "98.6" }
                                    }
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                if (vitalType == "BLOOD_PRESSURE") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MdTextField(
                            value = val1,
                            onValueChange = { val1 = it },
                            label = "Systolic (mmHg)",
                            modifier = Modifier.weight(1f)
                        )
                        MdTextField(
                            value = val2,
                            onValueChange = { val2 = it },
                            label = "Diastolic (mmHg)",
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    val unitLabel = when (vitalType) {
                        "BLOOD_SUGAR" -> "mg/dL"
                        "WEIGHT" -> "kg"
                        "SPO2" -> "%"
                        else -> "┬░F"
                    }
                    MdTextField(
                        value = val1,
                        onValueChange = { val1 = it },
                        label = "Value ($unitLabel)",
                        modifier = Modifier.fillMaxWidth()
                    )
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
                            val v1 = val1.toDoubleOrNull() ?: 0.0
                            val v2 = if (vitalType == "BLOOD_PRESSURE") val2.toDoubleOrNull() else null
                            val unit = when (vitalType) {
                                "BLOOD_PRESSURE" -> "mmHg"
                                "BLOOD_SUGAR" -> "mg/dL"
                                "WEIGHT" -> "kg"
                                "SPO2" -> "%"
                                else -> "┬░F"
                            }
                            onSave(
                                VitalEntity(
                                    profileId = profileId,
                                    vitalType = vitalType,
                                    valuePrimary = v1,
                                    valueSecondary = v2,
                                    unit = unit
                                )
                            )
                        },
                        label = { Text(stringResource(R.string.save), fontWeight = FontWeight.Bold) },
                    )
                }
            }
        }
    }
}

@Composable
fun AddAppointmentDialog(
    profileId: Long,
    onDismiss: () -> Unit,
    onSave: (AppointmentEntity) -> Unit
) {
    var doctor by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("Physician") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var doctorError by remember { mutableStateOf<String?>(null) }

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
                        text = stringResource(R.string.add_appointment),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                MdDivider(modifier = Modifier.padding(vertical = 4.dp))

                MdTextField(
                    value = doctor,
                    onValueChange = {
                        doctor = it
                        if (doctorError != null) doctorError = null
                    },
                    label = stringResource(R.string.doctor_name),
                    placeholder = "e.g. Dr. Verma",
                    isError = doctorError != null,
                    errorMessage = doctorError,
                    modifier = Modifier.fillMaxWidth()
                )

                MdTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = "Specialty",
                    placeholder = "e.g. Cardiologist, General Physician",
                    modifier = Modifier.fillMaxWidth()
                )

                MdTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = stringResource(R.string.clinic_location),
                    placeholder = "Hospital or Clinic Address",
                    modifier = Modifier.fillMaxWidth()
                )

                MdTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = stringResource(R.string.notes_label),
                    placeholder = "Reason for visit, documents to bring",
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    singleLine = false
                )

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
                            if (doctor.isBlank()) {
                                doctorError = "Doctor name is required"
                                return@MdPrimaryButton
                            }
                            onSave(
                                AppointmentEntity(
                                    profileId = profileId,
                                    doctorName = doctor.trim(),
                                    specialty = specialty.trim(),
                                    clinicHospital = location.trim(),
                                    appointmentDateTime = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
                                    notes = notes.trim()
                                )
                            )
                        },
                        label = { Text(stringResource(R.string.save), fontWeight = FontWeight.Bold) },
                    )
                }
            }
        }
    }
}
