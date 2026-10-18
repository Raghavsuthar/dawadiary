package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MdPrimaryButton

@Composable
fun ConsentDisclaimerDialog(
    showConsent: Boolean,
    onAcceptConsent: () -> Unit,
    showDisclaimer: Boolean,
    onAcceptDisclaimer: () -> Unit
) {
    if (!showConsent && !showDisclaimer) return

    val isConsentPhase = showConsent

    Dialog(
        onDismissRequest = { /* mandatory modal */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("consent_disclaimer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isConsentPhase) "Data Privacy & Security" else "Medical Disclaimer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isConsentPhase) {
                        "DawaDiary stores your health data, medication schedules, and clinical records directly on your device. We respect your medical privacy:\n\n" +
                                "• Zero third-party tracker or ad network sharing.\n" +
                                "• On-device database and encrypted backups.\n" +
                                "• Optional hardware-backed biometric screen lock.\n\n" +
                                "By continuing, you consent to local data processing on your device."
                    } else {
                        "DawaDiary is a digital health diary and medication schedule assistant:\n\n" +
                                "• It does not replace professional medical diagnosis, treatment, or advice.\n" +
                                "• Always follow the advice of your licensed doctor or pharmacist.\n" +
                                "• In case of emergency, contact local emergency healthcare services immediately."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                MdPrimaryButton(
                    onClick = {
                        if (isConsentPhase) onAcceptConsent() else onAcceptDisclaimer()
                    },
                    label = {
                        Text(if (isConsentPhase) "I Agree & Consent" else "I Understand & Accept", fontWeight = FontWeight.Bold)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("accept_disclaimer_button")
                )
            }
        }
    }
}
