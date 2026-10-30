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

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(9999.dp)
private val FieldShape = RoundedCornerShape(12.dp)
private val ButtonShape = RoundedCornerShape(16.dp)

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

@Composable
fun MdListRow(
    leading: @Composable () -> Unit = {},
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit = {},
    trailing: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clickableModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Row(
        modifier = clickableModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading()
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            title()
            subtitle()
        }
        trailing()
    }
}

@Composable
fun MdPrimaryButton(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fullWidth: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        contentPadding = PaddingValues(horizontal = 20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier).height(52.dp)
    ) {
        label()
    }
}

@Composable
fun MdOutlineButton(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fullWidth: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        contentPadding = PaddingValues(horizontal = 20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier).height(52.dp)
    ) {
        label()
    }
}

@Composable
fun MdTextButton(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        modifier = modifier
    ) {
        label()
    }
}

@Composable
fun MdStatusChip(
    bgColor: Color,
    textColor: Color,
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Surface(
        color = bgColor,
        shape = ChipShape,
        modifier = modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MdChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = ChipShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .height(48.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            label()
        }
    }
}

@Composable
fun MdConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
    confirmColor: Color = MaterialTheme.colorScheme.primary
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = { Text(text = message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmColor,
                    contentColor = Color.White
                )
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun MdStatusBanner(
    bgColor: Color,
    textColor: Color,
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.85f)
                )
            }
            if (action != null) {
                Spacer(modifier = Modifier.width(8.dp))
                action()
            }
        }
    }
}

@Composable
fun MdTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder) } } else null,
            leadingIcon = if (leadingIcon != null) {
                { Icon(imageVector = leadingIcon, contentDescription = null) }
            } else null,
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            shape = FieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                focusedBorderColor = MdPrimary,
                unfocusedBorderColor = MdOutlineStrong,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (isError && !errorMessage.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
fun MdAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    selected: Boolean = false
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@Composable
fun MdEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(20.dp))
            action()
        }
    }
}

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
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
        // SOS pill (error container, icon + explicit uppercase label)
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
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
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
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Profile switch pill (48dp touch target)
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
            icon = { Icon(Icons.Default.Today, contentDescription = "Today") },
            label = { Text("Today") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = currentTab == "medicines",
            onClick = { onTabClick("medicines") },
            icon = { Icon(Icons.Default.Medication, contentDescription = "Medicines") },
            label = { Text("Medicines") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = currentTab == "records",
            onClick = { onTabClick("records") },
            icon = { Icon(Icons.Default.Folder, contentDescription = "Records") },
            label = { Text("Records") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = currentTab == "family",
            onClick = { onTabClick("family") },
            icon = { Icon(Icons.Default.Group, contentDescription = "Family") },
            label = { Text("Family") },
            colors = itemColors
        )
        NavigationBarItem(
            selected = currentTab == "settings",
            onClick = { onTabClick("settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            colors = itemColors
        )
    }
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
