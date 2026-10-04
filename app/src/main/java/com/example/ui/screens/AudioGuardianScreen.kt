package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmethystDeep
import com.example.ui.theme.AmethystPrimary
import com.example.ui.theme.GuardianCyan
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.WarningAmber

@Composable
fun AudioGuardianScreen(
    currentDecibels: Float,
    isAudioMonitoring: Boolean,
    isRecordingEvidence: Boolean,
    onToggleMonitoring: () -> Unit,
    onStartEvidenceRecording: () -> Unit,
    onTriggerFakeCall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var distressThreshold by remember { mutableFloatStateOf(80f) }
    var selectedCaller by remember { mutableStateOf("Dad (Waiting at Corner)") }
    val callerOptions = listOf(
        "Dad (Waiting at Corner)",
        "Mom (Checking on You)",
        "Brother Alex (Live Tracking)",
        "Campus Security Escort Dispatch"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Decibel Live Level Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = GuardianCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ACOUSTIC DISTRESS MONITOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GuardianCyan
                        )
                    }

                    Switch(
                        checked = isAudioMonitoring,
                        onCheckedChange = { onToggleMonitoring() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SafeGreen,
                            checkedTrackColor = SafeGreen.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("toggle_mic_monitoring")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // dB Meter Gauge Circle
                val isDistress = currentDecibels >= distressThreshold
                val gaugeColor = if (isDistress) AlertRed else if (currentDecibels > 65f) WarningAmber else SafeGreen

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(gaugeColor.copy(alpha = 0.15f))
                        .border(3.dp, gaugeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${currentDecibels.toInt()}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = gaugeColor
                        )
                        Text(
                            text = "DECIBELS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (!isAudioMonitoring) "Microphone monitor paused"
                    else if (isDistress) "⚠️ HIGH AUDIO SPIKE / SCREAM THRESHOLD EXCEEDED!"
                    else "Ambient noise normal • Listening for scream or distress...",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (!isAudioMonitoring) MaterialTheme.colorScheme.onSurfaceVariant else gaugeColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Threshold Slider
                Text(
                    text = "Distress Trigger Sensitivity: ${distressThreshold.toInt()} dB",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                Slider(
                    value = distressThreshold,
                    onValueChange = { distressThreshold = it },
                    valueRange = 65f..95f,
                    colors = SliderDefaults.colors(
                        thumbColor = AmethystPrimary,
                        activeTrackColor = AmethystPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("distress_slider")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sensitive (65dB)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Loud Scream (95dB)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Automated Evidence Recording Vault Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.RadioButtonChecked, contentDescription = null, tint = AlertRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AUTOMATED AUDIO EVIDENCE RECORDER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertRed
                        )
                    }

                    if (isRecordingEvidence) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AlertRed.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("RECORDING (30s)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AlertRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "When SOS is triggered, SafeWalk automatically captures a 30-second encrypted audio snippet as legal tamper-proof evidence and attaches it to the incident report.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onStartEvidenceRecording,
                    enabled = !isRecordingEvidence,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecordingEvidence) MidnightElevated else AlertRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("record_evidence_button")
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRecordingEvidence) "Recording Evidence in Background..." else "Capture 30s Evidence Clip Now",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Fake Phone Call Simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PhoneCallback, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAFETY PRETEND / ESCAPE CALL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafeGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Triggers a realistic incoming phone call to help you safely detour away from uncomfortable situations or deter stalkers in unlit alleys.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SELECT CALLER IDENTITY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    callerOptions.forEach { caller ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedCaller = caller },
                            color = if (selectedCaller == caller) AmethystDeep.copy(alpha = 0.6f) else MidnightElevated
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedCaller == caller) SafeGreen else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = caller,
                                    fontSize = 13.sp,
                                    color = if (selectedCaller == caller) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selectedCaller == caller) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { onTriggerFakeCall(selectedCaller) },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trigger_fake_call_button")
                ) {
                    Icon(imageVector = Icons.Default.PhoneCallback, contentDescription = null, tint = Color(0xFF03280B), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trigger Fake Call (in 3s)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF03280B)
                    )
                }
            }
        }
    }
}
