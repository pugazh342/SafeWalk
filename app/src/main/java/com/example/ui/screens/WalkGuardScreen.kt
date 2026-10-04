package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Volunteer
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedDark
import com.example.ui.theme.AmethystDeep
import com.example.ui.theme.AmethystPrimary
import com.example.ui.theme.GuardianCyan
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.SafeGreen

@Composable
fun WalkGuardScreen(
    isWalkActive: Boolean,
    currentDestination: String,
    walkDurationSeconds: Int,
    walkDistanceMeters: Int,
    pairedVolunteer: Volunteer?,
    nearbyVolunteers: List<Volunteer>,
    currentDecibels: Float,
    isAudioMonitoring: Boolean,
    isStrobeActive: Boolean,
    isSirenActive: Boolean,
    sosCountdown: Int?,
    onDestinationChange: (String) -> Unit,
    onStartWalk: () -> Unit,
    onCompleteWalk: () -> Unit,
    onPairVolunteer: (Volunteer) -> Unit,
    onUnpairVolunteer: () -> Unit,
    onRequestSosCountdown: () -> Unit,
    onCancelSosCountdown: () -> Unit,
    onTriggerInstantSos: () -> Unit,
    onTriggerFakeCall: () -> Unit,
    onToggleStrobe: () -> Unit,
    onToggleSiren: () -> Unit,
    onNavigateToRadar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickDestinations = listOf("Home", "Metro Station", "Campus Library", "Friend's House", "Bus Terminal")
    var customDestInput by remember { mutableStateOf(currentDestination) }

    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val sosPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sosPulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Hero Banner Art
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_safewalk_hero_1791154939572),
                contentDescription = "SafeWalk Night Escort",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xDD0B0814))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = if (isWalkActive) "🛡️ Safe Journey in Progress" else "Night Walking Guard Active",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = if (pairedVolunteer != null) "Escort Companion: ${pairedVolunteer.name}" else "Community volunteers ready to escort you safely",
                    style = MaterialTheme.typography.bodySmall,
                    color = GuardianCyan
                )
            }
        }

        // Active Journey or Start SafeWalk Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("journey_card"),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isWalkActive) {
                    // Walk in progress view
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(SafeGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTIVE WALK SESSION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeGreen
                            )
                        }

                        val min = walkDurationSeconds / 60
                        val sec = walkDurationSeconds % 60
                        Text(
                            text = "%02d:%02d".format(min, sec),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Destination: $currentDestination",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Distance Covered: ${walkDistanceMeters}m",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Corridor: Secured (GPS Live)",
                            fontSize = 12.sp,
                            color = GuardianCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateToRadar,
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MidnightElevated),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = GuardianCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Radar", fontSize = 13.sp, color = Color.White)
                        }

                        Button(
                            onClick = onCompleteWalk,
                            modifier = Modifier.weight(1.2f).height(46.dp).testTag("complete_walk_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF022B0A), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("I've Arrived Safely", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF022B0A))
                        }
                    }
                } else {
                    // Start Walk configuration
                    Text(
                        text = "WHERE ARE YOU WALKING TO?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmethystPrimary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customDestInput,
                        onValueChange = {
                            customDestInput = it
                            onDestinationChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter destination address...", color = Color.Gray) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = AmethystPrimary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmethystPrimary,
                            unfocusedBorderColor = MidnightBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Presets
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickDestinations) { dest ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        customDestInput = dest
                                        onDestinationChange(dest)
                                    },
                                color = if (customDestInput == dest) AmethystDeep else MidnightElevated
                            ) {
                                Text(
                                    text = dest,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = if (customDestInput == dest) FontWeight.Bold else FontWeight.Normal,
                                    color = if (customDestInput == dest) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onStartWalk,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_walk_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AmethystPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, tint = Color(0xFF16082B), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Protected SafeWalk",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16082B)
                        )
                    }
                }
            }
        }

        // Volunteer Escort Companion Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = GuardianCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COMMUNITY VOLUNTEER ESCORTS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GuardianCyan
                        )
                    }

                    Text(
                        text = "${nearbyVolunteers.size} Near You",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (pairedVolunteer != null) {
                    // Paired Volunteer Active Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MidnightElevated,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(SafeGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pairedVolunteer.avatarInitials,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF03280B)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pairedVolunteer.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Verified",
                                        tint = SafeGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = "${pairedVolunteer.rating}★ • ${pairedVolunteer.completedEscorts} walks • ${pairedVolunteer.verificationTag}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Walking alongside / Synchronized telemetry",
                                    fontSize = 11.sp,
                                    color = SafeGreen
                                )
                            }

                            OutlinedButton(
                                onClick = onUnpairVolunteer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Unpair", fontSize = 11.sp, color = AlertRed)
                            }
                        }
                    }
                } else {
                    // Nearby Volunteers List to Pair
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        nearbyVolunteers.forEach { volunteer ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MidnightElevated,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(AmethystDeep),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = volunteer.avatarInitials,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = volunteer.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.VerifiedUser,
                                                contentDescription = "Verified",
                                                tint = SafeGreen,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        Text(
                                            text = "${volunteer.distanceMeters}m away • ~${volunteer.estimatedArrivalMin} min arrival",
                                            fontSize = 11.sp,
                                            color = GuardianCyan
                                        )
                                        Text(
                                            text = volunteer.verificationTag,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = { onPairVolunteer(volunteer) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GuardianCyan),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(
                                            text = "Escort",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00363A)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // EMERGENCY PANIC SOS BUTTON
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (sosCountdown != null) {
                // Countdown cancel buffer
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AlertRedDark,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🚨 SOS DISPATCHING IN $sosCountdown SECONDS...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap below immediately to cancel if false alarm",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCancelSosCountdown,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("CANCEL SOS (I'M OK)", fontWeight = FontWeight.Bold, color = AlertRedDark)
                        }
                    }
                }
            } else {
                // Big Round SOS Panic Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(AlertRed, Color(0xFF990019))
                                )
                            )
                            .border(4.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                            .clickable { onRequestSosCountdown() }
                            .testTag("panic_sos_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "SOS Panic",
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "EMERGENCY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "SOS",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Press to alert Emergency Contacts & Police with live link",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Quick Safety Actions Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "QUICK DEFENSE TOOLS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Fake Escape Call Button
                    QuickToolItem(
                        icon = Icons.Default.PhoneCallback,
                        label = "Pretend Call",
                        isActive = false,
                        activeColor = SafeGreen,
                        onClick = onTriggerFakeCall,
                        testTag = "quick_fake_call"
                    )

                    // Strobe Flashlight Button
                    QuickToolItem(
                        icon = Icons.Default.FlashOn,
                        label = if (isStrobeActive) "Strobe ON" else "Strobe Flash",
                        isActive = isStrobeActive,
                        activeColor = Color.White,
                        onClick = onToggleStrobe,
                        testTag = "quick_strobe"
                    )

                    // 110dB Siren Alarm Button
                    QuickToolItem(
                        icon = Icons.Default.VolumeUp,
                        label = if (isSirenActive) "Siren ON" else "110dB Siren",
                        isActive = isSirenActive,
                        activeColor = AlertRed,
                        onClick = onToggleSiren,
                        testTag = "quick_siren"
                    )

                    // Mic Audio Guardian Level
                    QuickToolItem(
                        icon = Icons.Default.Mic,
                        label = "${currentDecibels.toInt()} dB Guard",
                        isActive = isAudioMonitoring,
                        activeColor = GuardianCyan,
                        onClick = { },
                        testTag = "quick_audio_guard"
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickToolItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor.copy(alpha = 0.25f) else MidnightElevated)
                .border(
                    width = if (isActive) 1.5.dp else 0.dp,
                    color = if (isActive) activeColor else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
