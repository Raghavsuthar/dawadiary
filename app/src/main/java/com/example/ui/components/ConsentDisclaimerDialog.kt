package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MdDivider
import com.example.ui.theme.MdPrimaryButton

@Composable
fun ConsentDisclaimerDialog(
    showConsent: Boolean,
    onAcceptConsent: () -> Unit,
    showDisclaimer: Boolean,
    onAcceptDisclaimer: () -> Unit
) {
    if (showConsent) {
        Dialog(
            onDismissRequest = { /* mandatory on first launch */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Privacy & Data Protection Notice",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    MdDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Your health data stays 100% on this device.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• All medical records and prescription scans are encrypted on-device using AES-256-GCM.\n" +
                                "• No telemetry, health metrics, or medications are shared or sold to third parties.\n" +
                                "• You can back up your data locally or erase all records at any time in Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    MdPrimaryButton(
                        onClick = onAcceptConsent,
                        label = { Text("I Agree & Continue", fontWeight = FontWeight.Bold) },
                        fullWidth = true
                    )
                }
            }
        }
    } else if (showDisclaimer) {
        Dialog(
            onDismissRequest = { /* mandatory on first launch */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Medical Advice Disclaimer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    MdDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "DawaDiary is a medication tracking and organization tool. It is NOT a substitute for professional medical diagnosis, advice, or treatment.\n\n" +
                                "Always consult your physician, pharmacist, or qualified healthcare provider regarding your medications, dosages, side effects, or any medical conditions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    MdPrimaryButton(
                        onClick = onAcceptDisclaimer,
                        label = { Text("I Understand & Accept", fontWeight = FontWeight.Bold) },
                        fullWidth = true
                    )
                }
            }
        }
    }
}
