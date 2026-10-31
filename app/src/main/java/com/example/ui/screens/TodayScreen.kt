package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.dao.DoseWithMedicine
import com.example.data.dao.MedicineWithSchedule
import com.example.data.entity.ProfileEntity
import com.example.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(
    activeProfile: ProfileEntity?,
    doses: List<DoseWithMedicine>,
    activeMedicines: List<MedicineWithSchedule>,
    onMarkTaken: (doseId: Long, medicineId: Long) -> Unit,
    onMarkSnooze: (DoseWithMedicine) -> Unit,
    onMarkSkip: (doseId: Long) -> Unit,
    onAddMedicineClick: () -> Unit
) {
    val totalDoses = doses.size
    val takenDoses = doses.count { it.doseLog.status == "TAKEN" }
    val pendingDoses = doses.count { it.doseLog.status == "PENDING" || it.doseLog.status == "SNOOZED" }
    val adherencePercent = if (totalDoses > 0) (takenDoses * 100) / totalDoses else 0
    val lowStockMeds = activeMedicines.filter { it.medicine.stockCount <= it.medicine.lowStockThreshold }
    val nextDose = doses.firstOrNull { it.doseLog.status == "PENDING" || it.doseLog.status == "SNOOZED" }
    val dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMM"))

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("today_screen_list"),
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = "Good ${when (LocalTime.now().hour) { in 5..11 -> "morning"; in 12..16 -> "afternoon"; else -> "evening" }}, ${activeProfile?.name ?: "there"}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A2B33)
                )
                Text(
                    text = "A little care, every day. · $dateLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF61757A),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFD1F0E0),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Your health,", style = MaterialTheme.typography.titleLarge, color = Color(0xFF1A2B33))
                        Text("in good hands.", style = MaterialTheme.typography.titleLarge, color = Color(0xFF147D70), fontWeight = FontWeight.SemiBold)
                        Text(
                            "Small steps make a big difference.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF61757A),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            "TODAY'S CARE PLAN",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier
                                .padding(top = 14.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF147D70))
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFFB8E7D2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF147D70), modifier = Modifier.size(38.dp))
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Today at a glance", style = MaterialTheme.typography.titleLarge, color = Color(0xFF1A2B33))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StitchMetricTile(
                        value = "$takenDoses / $totalDoses",
                        label = "Doses taken",
                        tint = Color(0xFFE8E0FA),
                        modifier = Modifier.weight(1f)
                    )
                    StitchMetricTile(
                        value = pendingDoses.toString(),
                        label = "Upcoming",
                        tint = Color(0xFFFFE8DB),
                        modifier = Modifier.weight(1f)
                    )
                    StitchMetricTile(
                        value = "$adherencePercent%",
                        label = "Today's progress",
                        tint = Color(0xFFFFEBA6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (lowStockMeds.isNotEmpty()) {
            item {
                MdStatusBanner(
                    bgColor = Color(0xFFFFE8DB),
                    textColor = Color(0xFF8A493B),
                    icon = Icons.Default.Warning,
                    title = "Almost running low",
                    subtitle = lowStockMeds.joinToString(", ") { "${it.medicine.name} · ${it.medicine.stockCount} left" }
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Coming up next", style = MaterialTheme.typography.titleLarge, color = Color(0xFF1A2B33))
                if (nextDose != null) {
                    val nextTime = Instant.ofEpochMilli(nextDose.doseLog.scheduledDateTime)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("h:mm a"))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("UP NEXT", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFA7D61), modifier = Modifier
                                    .clip(RoundedCornerShape(50)).background(Color(0xFFFFE8DB)).padding(horizontal = 10.dp, vertical = 5.dp))
                                Text(nextDose.medicine.name, style = MaterialTheme.typography.titleMedium, color = Color(0xFF1A2B33), modifier = Modifier.padding(top = 8.dp))
                                Text("${nextDose.medicine.doseDescription} · ${nextDose.medicine.strength}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF61757A))
                            }
                            Text(nextTime, style = MaterialTheme.typography.labelLarge, color = Color(0xFF147D70), modifier = Modifier
                                .clip(RoundedCornerShape(50)).background(Color(0xFFD1F0E0)).padding(horizontal = 12.dp, vertical = 8.dp))
                        }
                    }
                } else {
                    Text("No upcoming doses scheduled.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF61757A))
                }
            }
        }

        item {
            Surface(
                onClick = onAddMedicineClick,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 1.dp
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFFE8E0FA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF147D70))
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("Add a health record or medicine", style = MaterialTheme.typography.titleSmall, color = Color(0xFF1A2B33))
                        Text("Keep your care plan up to date", style = MaterialTheme.typography.bodySmall, color = Color(0xFF61757A))
                    }
                }
            }
        }

        item {
            Text("Today's full schedule", style = MaterialTheme.typography.titleLarge, color = Color(0xFF1A2B33))
        }

        if (doses.isEmpty()) {
            item {
                MdEmptyState(
                    icon = Icons.Default.Medication,
                    title = "No Doses Scheduled for Today",
                    subtitle = stringResource(R.string.empty_today),
                    action = {
                        MdPrimaryButton(onClick = onAddMedicineClick, label = { Text(stringResource(R.string.add_medicine), fontWeight = FontWeight.SemiBold) }, fullWidth = true)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            items(doses, key = { it.doseLog.id }) { doseWithMed ->
                DoseCard(
                    doseWithMedicine = doseWithMed,
                    onTaken = { onMarkTaken(doseWithMed.doseLog.id, doseWithMed.medicine.id) },
                    onSnooze = { onMarkSnooze(doseWithMed) },
                    onSkip = { onMarkSkip(doseWithMed.doseLog.id) }
                )
            }
        }
    }
}

@Composable
private fun StitchMetricTile(
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 88.dp),
        color = tint,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 13.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = Color(0xFF1A2B33), fontWeight = FontWeight.Medium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF61757A), modifier = Modifier.padding(top = 5.dp))
        }
    }
}

@Composable
fun TodayHeader(
    profile: ProfileEntity?,
    totalDoses: Int,
    takenDoses: Int,
    adherencePercent: Int
) {
    val todayFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    val currentHour = LocalTime.now().hour
    val greeting = when {
        currentHour < 12 -> "Good morning"
        currentHour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = todayFormatted.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MdSecondary
            )
            Text(
                text = "On this device",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(MdPrimaryContainer, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$greeting, ${profile?.name ?: "Family"}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Here is your care plan for a healthy and restful day.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DoseCard(
    doseWithMedicine: DoseWithMedicine,
    onTaken: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit
) {
    val dose = doseWithMedicine.doseLog
    val med = doseWithMedicine.medicine

    val timeFormatted = Instant.ofEpochMilli(dose.scheduledDateTime)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("hh:mm a"))

    val isActionable = dose.status == "PENDING" || dose.status == "SNOOZED"

    val (statusBadgeBg, statusBadgeText, statusIcon) = when (dose.status) {
        "TAKEN" -> Triple(MdSuccessContainer, MdSuccess, Icons.Default.CheckCircle)
        "SNOOZED" -> Triple(MdWarningContainer, MdWarning, Icons.Default.Snooze)
        "SKIPPED" -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.Close)
        "MISSED" -> Triple(MdErrorContainer, MdError, Icons.Default.Warning)
        else -> Triple(MdPrimaryContainer, MdPrimary, Icons.Default.Schedule)
    }

    val cardShape = RoundedCornerShape(24.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .dawaCardShadow(4.dp, cardShape)
            .clip(cardShape)
            .testTag("dose_card_${dose.id}"),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = cardShape
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left indicator stripe (status-colored, like above)
            MdListRowStripe(
                color = when (dose.status) {
                    "TAKEN" -> MdSuccess
                    "SNOOZED" -> MdWarning
                    "MISSED" -> MdError
                    "SKIPPED" -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
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
                            text = "${med.doseDescription} • ${med.strength}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    MdStatusChip(
                        bgColor = statusBadgeBg,
                        textColor = statusBadgeText,
                        icon = statusIcon,
                        text = dose.status
                    )
                }
            }

            val foodText = when (med.foodInstruction) {
                "BEFORE_FOOD" -> stringResource(R.string.food_before)
                "AFTER_FOOD" -> stringResource(R.string.food_after)
                "WITH_FOOD" -> stringResource(R.string.food_with)
                else -> stringResource(R.string.food_any)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = foodText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isActionable) {
                Spacer(modifier = Modifier.height(14.dp))
                MdDivider(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MdPrimaryButton(
                        onClick = onTaken,
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = stringResource(R.string.action_take), fontWeight = FontWeight.Bold)
                            }
                        },
                        fullWidth = true,
                        modifier = Modifier.weight(1.5f)
                    )
                    MdOutlineButton(
                        onClick = onSnooze,
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Snooze, contentDescription = null, tint = MdWarning, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "10m", color = MdWarning, fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.weight(1.2f)
                    )
                    MdTextButton(
                        onClick = onSkip,
                        label = { Text(text = stringResource(R.string.action_skip), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }
            } // Column (card content)
        } // Row (stripe + content)
    } // Surface (card)
} // DoseCard
