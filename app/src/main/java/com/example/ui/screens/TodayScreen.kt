package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    val adherencePercent = if (totalDoses > 0) (takenDoses * 100) / totalDoses else 0
    val allDone = totalDoses > 0 && takenDoses == totalDoses

    val lowStockMeds = activeMedicines.filter { it.medicine.stockCount <= it.medicine.lowStockThreshold }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("today_screen_list")
            .padding(bottom = 16.dp),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Greeting header (minimalist, no card border) ──
        item {
            TodayHeader(
                profile = activeProfile,
                totalDoses = totalDoses,
                takenDoses = takenDoses,
                adherencePercent = adherencePercent
            )
        }

        // ── Low stock alert (subtle banner) ──
        if (lowStockMeds.isNotEmpty()) {
            item {
                MdStatusBanner(
                    bgColor = MdWarningContainer,
                    textColor = MdWarning,
                    icon = Icons.Default.Warning,
                    title = "Refill Alert",
                    subtitle = lowStockMeds.joinToString(", ") { "${it.medicine.name} (${it.medicine.stockCount} left)" }
                )
            }
        }

        // ── All done celebration (subtle) ──
        if (allDone) {
            item {
                MdStatusBanner(
                    bgColor = MdSuccessContainer,
                    textColor = MdSuccess,
                    icon = Icons.Default.CheckCircle,
                    title = "All Doses Taken Today",
                    subtitle = stringResource(R.string.all_done_celebration)
                )
            }
        }

        // ── Schedule section ──
        item {
            MdSection(
                title = {
                    Text(
                        text = stringResource(R.string.today_schedule),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                action = {
                    if (totalDoses > 0) {
                        Text(
                            text = "$takenDoses of $totalDoses Logged",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                padding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
            )
            MdDivider(modifier = Modifier.padding(horizontal = 0.dp))
        }

        // ── Empty / doses ──
        if (doses.isEmpty()) {
            item {
                MdEmptyState(
                    icon = Icons.Default.Medication,
                    title = "No Doses Scheduled for Today",
                    subtitle = stringResource(R.string.empty_today),
                    action = {
                        MdPrimaryButton(
                            onClick = onAddMedicineClick,
                            label = {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.add_medicine), fontWeight = FontWeight.SemiBold)
                                }
                            },
                            fullWidth = true
                        )
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
fun TodayHeader(
    profile: ProfileEntity?,
    totalDoses: Int,
    takenDoses: Int,
    adherencePercent: Int
) {
    val todayFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
    val currentHour = LocalTime.now().hour
    val greeting = when {
        currentHour < 12 -> "Good morning"
        currentHour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = todayFormatted,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$greeting, ${profile?.name ?: "Family"}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "A little care, every day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            MdAvatar(
                initials = profile?.name?.take(1) ?: "D",
                size = 40.dp,
                modifier = Modifier.align(Alignment.Top)
            )
        }

        if (totalDoses > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            MdDivider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.adherence_rate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$takenDoses of $totalDoses doses taken",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                MdStatusChip(
                    bgColor = if (adherencePercent == 100) MdSuccessContainer else MdPrimaryContainer,
                    textColor = if (adherencePercent == 100) MdSuccess else MdPrimary,
                    text = "$adherencePercent%",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { takenDoses.toFloat() / totalDoses },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (adherencePercent == 100) MdSuccess else MdPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "No doses scheduled for today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* card itself is not an action; buttons handle input */ }
            .testTag("dose_card_${dose.id}"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
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
        }
    }
}

// ── Subtle colored banner (replaces bordered alert cards) ────────────
@Composable
private fun MdStatusBanner(
    bgColor: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(textColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}
