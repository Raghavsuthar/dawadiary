package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.PinVerifyResult
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    onUnlockWithPin: (String) -> PinVerifyResult,
    onBiometricRequest: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lockoutSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(lockoutSeconds) {
        if (lockoutSeconds > 0) {
            delay(1000)
            lockoutSeconds -= 1
            if (lockoutSeconds == 0L) {
                errorMessage = null
            } else {
                errorMessage = "Too many failed attempts. Locked for ${lockoutSeconds}s"
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("lock_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "DawaDiary Secure Lock",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Enter your 4-digit PIN or authenticate with Biometrics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isFilled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Numeric Keypad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in keys) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (key in row) {
                            when (key) {
                                "BIO" -> {
                                    IconButton(
                                        onClick = onBiometricRequest,
                                        modifier = Modifier.size(64.dp),
                                        enabled = lockoutSeconds == 0L
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Biometric Login",
                                            tint = if (lockoutSeconds == 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                "DEL" -> {
                                    TextButton(
                                        onClick = {
                                            if (enteredPin.isNotEmpty() && lockoutSeconds == 0L) {
                                                enteredPin = enteredPin.dropLast(1)
                                                errorMessage = null
                                            }
                                        },
                                        modifier = Modifier.size(64.dp),
                                        enabled = lockoutSeconds == 0L
                                    ) {
                                        Text(
                                            text = "⌫",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                else -> {
                                    FilledTonalButton(
                                        onClick = {
                                            if (lockoutSeconds > 0L) return@FilledTonalButton
                                            if (enteredPin.length < 4) {
                                                enteredPin += key
                                                errorMessage = null
                                                if (enteredPin.length == 4) {
                                                    when (val res = onUnlockWithPin(enteredPin)) {
                                                        is PinVerifyResult.Success -> {
                                                            errorMessage = null
                                                        }
                                                        is PinVerifyResult.Incorrect -> {
                                                            errorMessage = "Incorrect PIN (${res.attemptsRemaining} attempts left)"
                                                            enteredPin = ""
                                                        }
                                                        is PinVerifyResult.LockedOut -> {
                                                            lockoutSeconds = res.secondsRemaining
                                                            errorMessage = "Too many failed attempts. Locked for ${res.secondsRemaining}s"
                                                            enteredPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(64.dp),
                                        shape = CircleShape,
                                        enabled = lockoutSeconds == 0L,
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
