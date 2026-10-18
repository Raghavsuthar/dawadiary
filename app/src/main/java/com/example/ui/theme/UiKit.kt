package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

// ┌────────────────────────────────────────────────────────────────────┐
// │  DawaDiary Friendly Colour UI Kit                                       │
// │  Shared primitives so every screen looks consistent & uncluttered. │
// └────────────────────────────────────────────────────────────────────┘

private val CardShape = RoundedCornerShape(22.dp)
private val ChipShape = RoundedCornerShape(14.dp)
private val FieldShape = RoundedCornerShape(16.dp)
private val ButtonShape = RoundedCornerShape(16.dp)

// ── Soft elevated card with generous rounded corners ──────────────────
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
        color = MaterialTheme.colorScheme.surface,
        shape = CardShape,
        tonalElevation = 2.dp,
        content = content
    )
}

// ── Divider (subtle, replaces heavy borders) ─────────────────────────
@Composable
fun MdDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
) {
    HorizontalDivider(
        modifier = modifier,
        color = color,
        thickness = thickness
    )
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

// ── Primary filled button ─────────────────────────────────────────────
@Composable
fun MdPrimaryButton(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
            contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        ),
        shape = ButtonShape,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(50.dp)
    ) {
        label()
    }
}

// ── Outline button ───────────────────────────────────────────────────
@Composable
fun MdOutlineButton(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        shape = ButtonShape,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(50.dp)
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
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        )
    ) {
        label()
    }
}

// ── Small pill chip ──────────────────────────────────────────────────
@Composable
fun MdChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        shape = ChipShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier
    )
}

// ── Minimalist outlined text field ───────────────────────────────────
@Composable
fun MdTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
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
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = FieldShape,
        modifier = modifier
    )
}

// ── Icon + label pill (for status chips) ─────────────────────────────
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
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
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
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
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
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Top strip: app title / profile + emergency (minimalist) ──────────
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile chip (left)
        MdAvatar(
            initials = profileInitial,
            size = 36.dp,
            modifier = Modifier.clickable(onClick = onProfileClick)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = profileName ?: stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.all_profiles),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        // Emergency (right) — subtle icon button, not a big red block
        IconButton(
            onClick = onEmergencyClick,
            modifier = Modifier.testTag("emergency_button")
        ) {
            Icon(
                imageVector = Icons.Default.Emergency,
                contentDescription = stringResource(R.string.emergency_card),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
        }
    }
    MdDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

// ── Bottom navigation (minimalist, tonal) ────────────────────────────
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
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
    tint: Color,
    size: Dp = 20.dp
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(size)
    )
}

// ── Minimalist confirm dialog ──────────────────────────────────────────
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
                shape = RoundedCornerShape(10.dp)
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
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

// ── Minimalist info dialog ─────────────────────────────────────────────
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
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
