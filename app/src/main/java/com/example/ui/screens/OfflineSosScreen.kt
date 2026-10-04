package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmergencyContact
import com.example.model.Volunteer
import com.example.service.LatLngPoint
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedDark
import com.example.ui.theme.GuardianCyan
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.WarningAmber

@Composable
fun OfflineSosScreen(
    userLocation: LatLngPoint,
    pairedVolunteer: Volunteer?,
    contacts: List<EmergencyContact>,
    isOfflineSosActive: Boolean,
    isStrobeActive: Boolean,
    isSirenActive: Boolean,
    onToggleOfflineSos: () -> Unit,
    onToggleStrobe: () -> Unit,
    onToggleSiren: () -> Unit,
    onDispatchOfflineSms: () -> Unit,
    onCallNumber: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Offline Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = WarningAmber.copy(alpha = 0.15f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "OFFLINE LOW-CONNECTIVITY GUARDIAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                    Text(
                        text = "No internet required. Dispatches raw GPS via SMS & triggers hardware acoustic alarms directly.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Offline SOS Trigger Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (isOfflineSosActive) AlertRedDark else MidnightCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isOfflineSosActive) "🚨 OFFLINE EMERGENCY SOS ACTIVE!" else "OFFLINE PANIC BROADCAST",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isOfflineSosActive) Color.White else AlertRed,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(if (isOfflineSosActive) Color.White else AlertRed)
                        .border(3.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable { onToggleOfflineSos() }
                        .testTag("offline_sos_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isOfflineSosActive) AlertRedDark else Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (isOfflineSosActive) "STOP" else "BROADCAST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOfflineSosActive) AlertRedDark else Color.White
                        )
                        Text(
                            text = "SOS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isOfflineSosActive) AlertRedDark else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isOfflineSosActive) "Siren & Strobe pulsing. Tap above to cancel."
                    else "Tap to activate Siren, Strobe, and broadcast Offline GPS SMS.",
                    fontSize = 12.sp,
                    color = if (isOfflineSosActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware Deterrents Grid
        Text(
            text = "STANDALONE HARDWARE DEFENSES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Flashlight Strobe
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleStrobe() }
                    .testTag("offline_strobe_button"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isStrobeActive) Color.White else MidnightCard
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = if (isStrobeActive) Color.Black else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isStrobeActive) "Strobe Pulsing" else "Tactical Strobe",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isStrobeActive) Color.Black else Color.White
                    )
                    Text(
                        text = "10Hz deterrence",
                        fontSize = 10.sp,
                        color = if (isStrobeActive) Color.DarkGray else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Synthesized Siren
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleSiren() }
                    .testTag("offline_siren_button"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSirenActive) AlertRed else MidnightCard
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isSirenActive) "Siren Blasting" else "110dB Alarm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "High pitch alert",
                        fontSize = 10.sp,
                        color = if (isSirenActive) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Direct SMS Dispatcher
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OFFLINE SMS TELEMETRY DISPATCH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafeGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Compiles raw GPS coordinates (${"%.4f".format(userLocation.latitude)}, ${"%.4f".format(userLocation.longitude)}), battery level, and volunteer info into an instant cellular SMS broadcast.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDispatchOfflineSms,
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("dispatch_offline_sms_button")
                ) {
                    Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = Color(0xFF03280B), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dispatch Offline SMS (${contacts.size} Contacts)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF03280B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Offline Direct Emergency Calls
        Text(
            text = "ONE-TAP EMERGENCY DIALERS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onCallNumber("112") },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.LocalPolice, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Police (112)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onCallNumber("1091") },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GuardianCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color(0xFF00363A), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Helpline (1091)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00363A))
            }
        }
    }
}
