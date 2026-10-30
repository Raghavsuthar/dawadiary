package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

// ┌────────────────────────────────────────────────────────────────────┐
// │  DawaDiary "Warm Clinical Material" UI Kit (Stitch design)         │
// │  Shared primitives so every screen looks consistent & uncluttered. │
// └────────────────────────────────────────────────────────────────────┘

// 8dp/4dp grid, welcoming curvature: cards 20dp, buttons 16dp, fields 12dp,
// chips & status badges fully rounded pills.
private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(9999.dp)
private val FieldShape = RoundedCornerShape(12.dp)
private val ButtonShape = RoundedCornerShape(16.dp)

// ── Pristine white card (Level 1 surface over warm off-white canvas) ─
@Composable
fun MdSurface(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(padding),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = CardShape,
        tonalElevation = 0.dp,
        content = content
    )
}

// ── Warm ambient shadow card (design token Level 1: 0px 2px 8px) ─────
@Composable
fun MdCardShadow() = Level1AmbientAlpha

// ── Divider (outline-variant, replaces heavy borders) ────────────────
@Composable
fun MdDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outline
) {
    HorizontalDivider(
        modifier = modifier,
        color = color,
        thickness = thickness
    )
}

// ── Left-edge status indicator stripe (4dp, dynamically coded) ───────
@Composable
fun MdListRowStripe(
    color: Color,
    modifier: Modifier = Modifier,
    width: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .background(color)
    )
}

// ── Accessible progress ring (Adherence summary) ─────────────────────
@Composable
fun MdProgressRing(
    progress: Float,
    size: Dp = 80.dp,
    stroke: Dp = 7.dp,
    centerContent: @Composable () -> Unit
) {
    val ringColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.size(size),
            color = ringColor,
            trackColor = trackColor,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeWidth = stroke
        )
        centerContent()
    }
}

// ── Section header: title + optional trailing action ─────────────────
@Composable
fun MdSection(
    title: @Composable () -> Unit,
    action: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(padding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        title()
        action()
    }
}

// ── Simple text row with leading icon ────────────────────────────────
@Composable
fun MdListRow(
    leading: @Composable () -> Unit = {},
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit = {},
    trailing: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clickableModifier = if (onClick != null) modifier.clickable(onClick = onClick!!) else modifier
    Row(
        modifier = clickableModifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            title()
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                subtitle()
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

// ── Filled primary button (52dp, teal, radius 16 — motor-friendly) ───
@Composable
fun MdPrimaryButton(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = false,
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                containerColor != null -> containerColor
                else -> MaterialTheme.colorScheme.primary
            },
            contentColor = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                contentColor != null -> contentColor
                else -> MaterialTheme.colorScheme.onPrimary
            }
        ),
        shape = ButtonShape,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(52.dp)
    ) {
        label()
    }
}

// ── Tonal button (mint/shell — non-primary affirmations) ─────────────
@Composable
fun MdOutlineButton(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = false
) {
    Button(
        onClick = onClick,
        border = null,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = ButtonShape,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(52.dp)
    ) {
        label()
    }
}

// ── Text button ──────────────────────────────────────────────────────
@Composable
fun MdTextButton(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        )
    ) {
        label()
    }
}

// ── Fully rounded pill chip (prevents misinterpretation) ─────────────
@Composable
fun MdChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        border = null,
        contentPadding = PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = ChipShape,
        modifier = modifier
            .height(48.dp)
    ) {
        label()
    }
}

// ── Minimalist outlined text field (12dp radius, visible helpers) ────
@Composable
fun MdTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    minLines: Int = 1,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        isError = isError,
        supportingText = supportingText,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MdOutlineStrong,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = FieldShape,
        modifier = modifier
    )
}

// ── Compact status pill (32dp: icon + explicit text, never icon alone) ─
@Composable
fun MdStatusChip(
    bgColor: Color,
    textColor: Color,
    icon: ImageVector = Icons.Default.Schedule,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = bgColor,
        shape = ChipShape,
        modifier = modifier.height(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── Empty state ──────────────────────────────────────────────────────
@Composable
fun MdEmptyState(
    icon: ImageVector = Icons.Default.Inbox,
    title: String,
    subtitle: String,
    action: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MdOutlineStrong,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(20.dp))
            action()
        }
    }
}

// ── Pill avatar with initials ────────────────────────────────────────
@Composable
fun MdAvatar(
    initials: String,
    size: Dp = 40.dp,
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondaryContainer
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

// ── Top strip: brand icon + title + SOS pill + profile switch ────────
@Composable
fun MdTopStrip(
    profileName: String?,
    profileInitial: String,
    onProfileClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MdAvatar(
            initials = profileInitial,
            size = 32.dp,
            modifier = Modifier.clickable(onClick = onProfileClick)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = profileName ?: stringResource(R.string.all_profiles),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        // SOS Card pill (error-container, icon + uppercase label)
        Button(
            onClick = onEmergencyClick,
            border = null,
            contentPadding = PaddingValues(horizontal = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = ChipShape,
            modifier = Modifier
                .height(40.dp)
                .testTag("emergency_button")
        ) {
            Icon(
                imageVector = Icons.Default.Emergency,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(R.string.sos_card),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Profile switch pill (48dp min target)
        Button(
            onClick = onProfileClick,
            border = null,
            contentPadding = PaddingValues(end = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = ChipShape,
            modifier = Modifier
                .height(48.dp)
                .defaultMinSize(minWidth = 48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.all_profiles),
                modifier = Modifier.size(20.dp)
            )
        }
    }
    MdDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

// ── Bottom navigation (canvas surface, teal active state) ────────────
@Composable
fun MdBottomNav(
    currentTab: String,
    onTabClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = Color.Transparent
        )

        NavigationBarItem(
            selected = currentTab == "today",
            onClick = { onTabClick("today") },
            icon = { Icon(imageVector = Icons.Default.Today, contentDescription = stringResource(R.string.nav_today)) },
            label = { Text(stringResource(R.string.nav_today)) },
            colors = itemColors,
            modifier = Modifier.testTag("tab_today")
        )
        NavigationBarItem(
            selected = currentTab == "medicines",
            onClick = { onTabClick("medicines") },
            icon = { Icon(imageVector = Icons.Default.Medication, contentDescription = stringResource(R.string.nav_medicines)) },
            label = { Text(stringResource(R.string.nav_medicines)) },
            colors = itemColors,
            modifier = Modifier.testTag("tab_medicines")
        )
        NavigationBarItem(
            selected = currentTab == "records",
            onClick = { onTabClick("records") },
            icon = { Icon(imageVector = Icons.Default.FolderSpecial, contentDescription = stringResource(R.string.nav_records)) },
            label = { Text(stringResource(R.string.nav_records)) },
            colors = itemColors,
            modifier = Modifier.testTag("tab_records")
        )
        NavigationBarItem(
            selected = currentTab == "family",
            onClick = { onTabClick("family") },
            icon = { Icon(imageVector = Icons.Default.People, contentDescription = stringResource(R.string.nav_family)) },
            label = { Text(stringResource(R.string.nav_family)) },
            colors = itemColors,
            modifier = Modifier.testTag("tab_family")
        )
        NavigationBarItem(
            selected = currentTab == "settings",
            onClick = { onTabClick("settings") },
            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
            label = { Text(stringResource(R.string.nav_settings)) },
            colors = itemColors,
            modifier = Modifier.testTag("tab_settings")
        )
    }
}

// ── Small inline section icon ──────────────────────────────────────────
@Composable
fun MdIcon(
    imageVector: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    size: Dp = 20.dp
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(size)
    )
}

// ── Confirmation dialog (white rounded card) ──────────────────────────
@Composable
fun MdConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String,
    confirmColor: Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = null,
                tint = confirmColor,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor, contentColor = Color.White),
                shape = ButtonShape
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            MdTextButton(
                onClick = onDismiss,
                label = { Text(stringResource(R.string.cancel)) }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(20.dp)
    )
}

// ── Info dialog (white rounded card) ──────────────────────────────────
@Composable
fun MdInfoDialog(
    title: String,
    body: @Composable () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = body,
        confirmButton = {
            MdTextButton(
                onClick = onDismiss,
                label = { Text("Understood") }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(20.dp)
    )
}
