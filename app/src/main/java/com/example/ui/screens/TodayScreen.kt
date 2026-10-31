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
        // ΓöÇΓöÇ Greeting header (minimalist, no card border) ΓöÇΓöÇ
        item {
            TodayHeader(
                profile = activeProfile,
                totalDoses = totalDoses,
                takenDoses = takenDoses,
                adherencePercent = adherencePercent
            )
        }

        // ── Hero: adherence summary on brand gradient ──
        if (totalDoses > 0) {
            item {
                val heroShape = RoundedCornerShape(28.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .dawaCardShadow(8.dp, heroShape)
                        .clip(heroShape)
                        .background(BrandBrush)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 48.dp, y = (-48).dp)
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = (-36).dp, y = 36.dp)
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(BrandGlow.copy(alpha = 0.10f))
                    )
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MdProgressRing(
                            progress = takenDoses.toFloat() / totalDoses,
                            size = 92.dp,
                            stroke = 9.dp,
                            ringColor = Color.White,
                            trackColor = Color.White.copy(alpha = 0.22f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$adherencePercent%",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Done",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TODAY'S PROGRESS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (allDone) "Every dose taken today!" else "You're doing wonderfully!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = if (allDone) Icons.Default.Verified else Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$takenDoses of $totalDoses doses taken",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // ΓöÇΓöÇ Low stock alert (subtle banner) ΓöÇΓöÇ
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

        // ΓöÇΓöÇ All done celebration (subtle) ΓöÇΓöÇ
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

        // ΓöÇΓöÇ Schedule section ΓöÇΓöÇ
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

        // ΓöÇΓöÇ Empty / doses ΓöÇΓöÇ
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
