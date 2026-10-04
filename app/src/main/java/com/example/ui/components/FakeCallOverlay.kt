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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.service.FakeCallState
import com.example.ui.theme.AlertRed
import com.example.ui.theme.SafeGreen

@Composable
fun FakeCallOverlay(
    callState: FakeCallState,
    callerName: String,
    callDurationSec: Int,
    onAccept: () -> Unit,
    onDeclineOrEnd: () -> Unit
) {
    AnimatedVisibility(
        visible = callState != FakeCallState.IDLE,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07050E).copy(alpha = 0.98f))
                .padding(24.dp)
                .testTag("fake_call_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize().padding(vertical = 40.dp)
            ) {
                // Caller Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (callState == FakeCallState.RINGING) "INCOMING CALL..." else "ON CALL",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = if (callState == FakeCallState.RINGING) SafeGreen else Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2A2244)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Caller Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = callerName,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (callState == FakeCallState.CONNECTED) {
                        val min = callDurationSec / 60
                        val sec = callDurationSec % 60
                        Text(
                            text = "%02d:%02d".format(min, sec),
                            style = MaterialTheme.typography.titleMedium,
                            color = SafeGreen
                        )
                    } else {
                        Text(
                            text = "Mobile • Live Safety Check",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                // Middle: Simulated Conversation Prompts (when connected)
                if (callState == FakeCallState.CONNECTED) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1E1733),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🗣️ Simulated Escape Voice Guidance:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\"Hey! I'm watching your GPS live right now, you're just 1 block away. Keep walking straight, I'm waiting outside the gate! Speak out loud so anyone nearby knows you're talking to me.\"",
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Bottom Call Action Buttons
                if (callState == FakeCallState.RINGING) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decline
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = onDeclineOrEnd,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(AlertRed)
                                    .testTag("decline_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallEnd,
                                    contentDescription = "Decline Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Decline", fontSize = 12.sp, color = Color.LightGray)
                        }

                        // Accept
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = onAccept,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(SafeGreen)
                                    .testTag("accept_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Accept Call",
                                    tint = Color(0xFF03280B),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Answer", fontSize = 12.sp, color = Color.LightGray)
                        }
                    }
                } else if (callState == FakeCallState.CONNECTED) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B2245))
                            ) {
                                Icon(imageVector = Icons.Default.Mic, contentDescription = "Mute", tint = Color.White)
                            }
                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B2245))
                            ) {
                                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Speaker", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        IconButton(
                            onClick = onDeclineOrEnd,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(AlertRed)
                                .testTag("end_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "End Call", fontSize = 12.sp, color = Color.LightGray)
                    }
                }
            }
        }
    }
}
