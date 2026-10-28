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
import com.example.R
import com.example.data.dao.MedicineWithSchedule
import com.example.data.entity.AppointmentEntity
import com.example.data.entity.ProfileEntity
import com.example.data.entity.VitalEntity
import com.example.ui.components.EmergencyCardDialog
import com.example.ui.theme.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
    var showEmergencyCard by remember { mutableStateOf(false) }
    var showAddVitalDialog by remember { mutableStateOf(false) }
    var showAddApptDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("family_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = "Family Health Profiles",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Manage dependents, emergency info & vitals in one place",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MdDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Active profile summary card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MdAvatar(
                                initials = activeProfile?.name?.take(1) ?: "U",
                                size = 48.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = activeProfile?.name ?: "Family Member",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${activeProfile?.relationship ?: "Self"} • Blood Group: ${activeProfile?.bloodGroup?.ifBlank { "N/A" } ?: "N/A"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        MdPrimaryButton(
                            onClick = { showEmergencyCard = true },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Emergency", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }

                    if (!activeProfile?.allergies.isNullOrBlank() || !activeProfile?.chronicConditions.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        MdDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        if (!activeProfile?.allergies.isNullOrBlank()) {
                            Text(
                                text = "Allergies: ${activeProfile?.allergies}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
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
        }

        // Family members row
        item {
            MdSection(
                title = { Text("Family Members (${profiles.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                action = {
                    TextButton(onClick = { showAddProfileDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.add_profile), fontWeight = FontWeight.Bold)
                    }
                }
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(profiles, key = { it.id }) { p ->
                    val isSelected = p.id == activeProfile?.id
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clickable { onSelectProfile(p.id) }
                            .widthIn(min = 120.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MdAvatar(initials = p.name.take(1), size = 36.dp, selected = isSelected)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(p.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(p.relationship, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Vitals Section
        item {
            MdSection(
                title = { Text("Health Vitals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                action = {
                    TextButton(onClick = { showAddVitalDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Vital", fontWeight = FontWeight.Bold)
                    }
                }
            )

            if (vitals.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "No vitals recorded yet. Tap '+ Log Vital' to track BP, Blood Sugar, or Weight.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (v in vitals.take(5)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (v.vitalType) {
                                            "BLOOD_PRESSURE" -> Icons.Default.Favorite
                                            "BLOOD_SUGAR" -> Icons.Default.WaterDrop
                                            "WEIGHT" -> Icons.Default.MonitorWeight
                                            else -> Icons.Default.Thermostat
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = v.vitalType.replace("_", " "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                val reading = if (v.valueSecondary != null) {
                                    "${v.valuePrimary.toInt()}/${v.valueSecondary.toInt()} ${v.unit}"
                                } else {
                                    "${v.valuePrimary} ${v.unit}"
                                }
                                Text(
                                    text = reading,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Appointments Section
        item {
            MdSection(
                title = { Text("Doctor Appointments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                action = {
                    TextButton(onClick = { showAddApptDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Appointment", fontWeight = FontWeight.Bold)
                    }
                }
            )

            if (appointments.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "No upcoming doctor checkups scheduled.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (appt in appointments) {
                        val dateFormatted = Instant.ofEpochMilli(appt.appointmentDateTime)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy • hh:mm a"))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = appt.doctorName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${appt.specialty} • ${appt.clinicHospital}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dateFormatted,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
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
        AddProfileDialog(
            onDismiss = { showAddProfileDialog = false },
            onSave = { p ->
                onSaveProfile(p)
                showAddProfileDialog = false
            }
        )
    }

    if (showAddVitalDialog && activeProfile != null) {
        AddVitalDialog(
            profileId = activeProfile.id,
            onDismiss = { showAddVitalDialog = false },
            onSave = { v ->
                onSaveVital(v)
                showAddVitalDialog = false
            }
        )
    }

    if (showAddApptDialog && activeProfile != null) {
        AddAppointmentDialog(
            profileId = activeProfile.id,
            onDismiss = { showAddApptDialog = false },
            onSave = { a ->
                onSaveAppointment(a)
                showAddApptDialog = false
            }
        )
    }
}

@Composable
fun AddProfileDialog(
    onDismiss: () -> Unit,
    onSave: (ProfileEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("Family Member") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var bloodGroup by remember { mutableStateOf("O+") }
    var allergies by remember { mutableStateOf("") }
    var conditions by remember { mutableStateOf("") }
    var emContactName by remember { mutableStateOf("") }
    var emContactPhone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Family Member", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MdTextField(value = name, onValueChange = { name = it }, label = "Full Name *")
                MdTextField(value = relationship, onValueChange = { relationship = it }, label = "Relationship (Mother, Father, Child)")
                MdTextField(value = dob, onValueChange = { dob = it }, label = "Date of Birth (YYYY-MM-DD)")
                MdTextField(value = bloodGroup, onValueChange = { bloodGroup = it }, label = "Blood Group (e.g. O+, B+, A-)")
                MdTextField(value = allergies, onValueChange = { allergies = it }, label = "Known Allergies (e.g. Penicillin)")
                MdTextField(value = conditions, onValueChange = { conditions = it }, label = "Chronic Conditions (e.g. Hypertension)")
                MdTextField(value = emContactName, onValueChange = { emContactName = it }, label = "Emergency Contact Person")
                MdTextField(value = emContactPhone, onValueChange = { emContactPhone = it }, label = "Emergency Phone Number")
            }
        },
        confirmButton = {
            MdPrimaryButton(
                onClick = {
                    if (name.isBlank()) return@MdPrimaryButton
                    onSave(
                        ProfileEntity(
                            name = name.trim(),
                            relationship = relationship.trim(),
                            dob = dob.trim(),
                            gender = gender,
                            bloodGroup = bloodGroup.trim(),
                            allergies = allergies.trim(),
                            chronicConditions = conditions.trim(),
                            emergencyContactName = emContactName.trim(),
                            emergencyContactPhone = emContactPhone.trim(),
                            isSelf = false
                        )
                    )
                },
                label = { Text("Save Member", fontWeight = FontWeight.Bold) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddVitalDialog(
    profileId: Long,
    onDismiss: () -> Unit,
    onSave: (VitalEntity) -> Unit
) {
    var vitalType by remember { mutableStateOf("BLOOD_PRESSURE") }
    var val1 by remember { mutableStateOf("120") }
    var val2 by remember { mutableStateOf("80") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Health Vital", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Vital Metric:", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = vitalType == "BLOOD_PRESSURE",
                        onClick = { vitalType = "BLOOD_PRESSURE"; val1 = "120"; val2 = "80" },
                        label = { Text("BP") }
                    )
                    FilterChip(
                        selected = vitalType == "BLOOD_SUGAR",
                        onClick = { vitalType = "BLOOD_SUGAR"; val1 = "110"; val2 = "" },
                        label = { Text("Sugar") }
                    )
                    FilterChip(
                        selected = vitalType == "WEIGHT",
                        onClick = { vitalType = "WEIGHT"; val1 = "70"; val2 = "" },
                        label = { Text("Weight") }
                    )
                }

                if (vitalType == "BLOOD_PRESSURE") {
                    MdTextField(value = val1, onValueChange = { val1 = it }, label = "Systolic (mmHg)")
                    MdTextField(value = val2, onValueChange = { val2 = it }, label = "Diastolic (mmHg)")
                } else {
                    MdTextField(value = val1, onValueChange = { val1 = it }, label = "Reading Value")
                }
            }
        },
        confirmButton = {
            MdPrimaryButton(
                onClick = {
                    val v1 = val1.toDoubleOrNull() ?: 0.0
                    val v2 = if (vitalType == "BLOOD_PRESSURE") val2.toDoubleOrNull() else null
                    val unit = when (vitalType) {
                        "BLOOD_PRESSURE" -> "mmHg"
                        "BLOOD_SUGAR" -> "mg/dL"
                        "WEIGHT" -> "kg"
                        else -> ""
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
                label = { Text("Save Vital", fontWeight = FontWeight.Bold) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddAppointmentDialog(
    profileId: Long,
    onDismiss: () -> Unit,
    onSave: (AppointmentEntity) -> Unit
) {
    val context = LocalContext.current
    var doctor by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var hospital by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var selectedDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(10, 0)) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d, yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }

    val datePickerDialog = remember(context, selectedDate) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
    }

    val timePickerDialog = remember(context, selectedTime) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedTime = LocalTime.of(hourOfDay, minute)
            },
            selectedTime.hour,
            selectedTime.minute,
            false
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Doctor Appointment", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MdTextField(value = doctor, onValueChange = { doctor = it }, label = "Doctor Name *")
                MdTextField(value = specialty, onValueChange = { specialty = it }, label = "Specialty (e.g. Cardiologist)")
                MdTextField(value = hospital, onValueChange = { hospital = it }, label = "Clinic / Hospital")

                Text("Appointment Date & Time:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(selectedDate.format(dateFormatter), style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(
                        onClick = { timePickerDialog.show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(selectedTime.format(timeFormatter), style = MaterialTheme.typography.bodySmall)
                    }
                }

                MdTextField(value = notes, onValueChange = { notes = it }, label = "Notes / Reason for visit")
            }
        },
        confirmButton = {
            MdPrimaryButton(
                onClick = {
                    if (doctor.isBlank()) return@MdPrimaryButton
                    val appointmentEpoch = LocalDateTime.of(selectedDate, selectedTime)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    onSave(
                        AppointmentEntity(
                            profileId = profileId,
                            doctorName = doctor.trim(),
                            specialty = specialty.trim(),
                            clinicHospital = hospital.trim(),
                            appointmentDateTime = appointmentEpoch,
                            notes = notes.trim()
                        )
                    )
                },
                label = { Text("Save Appointment", fontWeight = FontWeight.Bold) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

