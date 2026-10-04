package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.BackendDispatchReport
import com.example.service.LatLngPoint
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedDark
import com.example.ui.theme.GuardianCyan
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.SafeGreen

@Composable
fun SosAlertModal(
    isSosActive: Boolean,
    userLocation: LatLngPoint,
    smsDispatchedCount: Int,
    policeNotified: Boolean,
    backendReport: BackendDispatchReport,
    isStrobeActive: Boolean,
    isSirenActive: Boolean,
    isRecordingAudio: Boolean,
    onToggleStrobe: () -> Unit,
    onToggleSiren: () -> Unit,
    onCancelSos: () -> Unit,
    onCallNumber: (String) -> Unit
) {
    AnimatedVisibility(
        visible = isSosActive,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF140206).copy(alpha = 0.98f))
                .padding(20.dp)
                .testTag("sos_active_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emergency Header
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(AlertRed, AlertRedDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "SOS Active",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "EMERGENCY SOS ACTIVE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = AlertRed
                )

                Text(
                    text = "Live telemetry & encrypted GPS tracking broadcasted",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Telemetry status cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MidnightCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AUTOMATED RESPONSE ACTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GuardianCyan,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SosStatusRow(
                            icon = Icons.Default.LocalPolice,
                            title = "Nearby Police Station (112)",
                            subtitle = if (policeNotified) "Dispatched • High-priority alert triggered" else "Alerting...",
                            statusColor = SafeGreen
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SosStatusRow(
                            icon = Icons.Default.Sms,
                            title = "Emergency Contacts Broadcast",
                            subtitle = if (smsDispatchedCount > 0) "Sent to $smsDispatchedCount trusted contacts with map link" else "Broadcasting SMS telemetry...",
                            statusColor = SafeGreen
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SosStatusRow(
                            icon = Icons.Default.Mic,
                            title = "Audio Evidence Recorder",
                            subtitle = if (isRecordingAudio) "Recording live ambient audio evidence to secure vault" else "Audio evidence secured",
                            statusColor = if (isRecordingAudio) AlertRed else SafeGreen
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SosStatusRow(
                            icon = Icons.Default.CloudSync,
                            title = "Supabase Real Postgres Cloud DB",
                            subtitle = backendReport.supabaseStatus,
                            statusColor = if (backendReport.supabaseStatus.contains("✅")) SafeGreen else GuardianCyan
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SosStatusRow(
                            icon = Icons.Default.Forum,
                            title = "Discord Emergency Sentinel Webhook",
                            subtitle = backendReport.discordStatus,
                            statusColor = if (backendReport.discordStatus.contains("✅")) SafeGreen else AlertRed
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SosStatusRow(
                            icon = Icons.Default.Email,
                            title = "Direct Gmail Emergency Alert Email",
                            subtitle = backendReport.emailStatus,
                            statusColor = if (backendReport.emailStatus.contains("✅")) SafeGreen else AlertRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current GPS Location Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MidnightElevated),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AlertRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "GPS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AlertRed)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Live Coordinates Sent",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${"%.5f".format(userLocation.latitude)}° N, ${"%.5f".format(userLocation.longitude)}° W",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Deterrent Controls (Strobe & Siren)
                Text(
                    text = "HARDWARE DETERRENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onToggleStrobe,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStrobeActive) Color.White else MidnightElevated,
                            contentColor = if (isStrobeActive) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isStrobeActive) "Strobe ON" else "Strobe OFF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onToggleSiren,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSirenActive) AlertRed else MidnightElevated,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isSirenActive) "Siren 110dB" else "Siren OFF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Direct One-Tap Speed Dial
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onCallNumber("112") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Police (112)", fontSize = 12.sp, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { onCallNumber("1091") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = GuardianCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Helpline (1091)", fontSize = 12.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Cancel SOS Button
                Button(
                    onClick = onCancelSos,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("cancel_sos_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF03280B), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I AM SAFE (DISMISS SOS)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF03280B)
                    )
                }
            }
        }
    }
}

@Composable
private fun SosStatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MidnightElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = subtitle, fontSize = 11.sp, color = Color.LightGray)
        }
    }
}
