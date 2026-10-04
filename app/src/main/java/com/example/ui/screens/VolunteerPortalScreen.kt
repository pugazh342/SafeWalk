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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RequestStatus
import com.example.model.UrgencyLevel
import com.example.model.UserProfile
import com.example.model.WalkRequest
import com.example.service.LatLngPoint
import com.example.service.SafePlace
import com.example.ui.components.InteractiveRadarMap
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmethystDeep
import com.example.ui.theme.AmethystPrimary
import com.example.ui.theme.GuardianCyan
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.WarningAmber

@Composable
fun VolunteerPortalScreen(
    isOnDuty: Boolean,
    incomingRequests: List<WalkRequest>,
    activeEscort: WalkRequest?,
    volunteerLocation: LatLngPoint?,
    userLocation: LatLngPoint,
    routeHistory: List<LatLngPoint>,
    safePlaces: List<SafePlace>,
    isSupabaseGpsStreaming: Boolean,
    trackingUrl: String,
    currentUser: UserProfile?,
    onToggleDuty: () -> Unit,
    onAcceptRequest: (WalkRequest) -> Unit,
    onCompleteEscort: (WalkRequest) -> Unit,
    onCallWalker: (String) -> Unit,
    onCallEmergency: (String) -> Unit,
    onShareTrackingUrl: (String) -> Unit,
    onTriggerManualGpsStream: () -> Unit,
    onOpenAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDutyTab by remember { mutableIntStateOf(0) } // 0: Requests, 1: Live Radar Map

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Volunteer Profile & Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onOpenAuth() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GuardianCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.name?.take(2)?.uppercase() ?: "VG",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00363A)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.name ?: "Volunteer Guardian",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified Volunteer",
                                    tint = SafeGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Badge #${currentUser?.volunteerBadge ?: "SW-7842"} • Supabase Auth",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Duty Toggle Switch
                    Switch(
                        checked = isOnDuty,
                        onCheckedChange = { onToggleDuty() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SafeGreen,
                            checkedTrackColor = SafeGreen.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("volunteer_duty_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    VolunteerStatBox("Rating", "${currentUser?.volunteerRating ?: 4.96f} ★", SafeGreen)
                    VolunteerStatBox("Escorts", "${currentUser?.completedEscorts ?: 64} Done", GuardianCyan)
                    VolunteerStatBox("GPS Stream", if (isSupabaseGpsStreaming) "LIVE ⚡" else "STANDBY", if (isSupabaseGpsStreaming) SafeGreen else Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ----------------------------------------------------
        // ACTIVE ESCORT MISSION MODE (Live Radar Map + Console)
        // ----------------------------------------------------
        if (activeEscort != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2B14)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SafeGreen)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTIVE ESCORT MISSION IN PROGRESS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeGreen
                            )
                        }

                        // Supabase Live telemetry indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(SafeGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SUPABASE LIVE ⚡",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Walker: ${activeEscort.walkerName}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Pickup: ${activeEscort.pickupLocation}",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "Destination: ${activeEscort.destination}",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Embedded Real-Time Map Visualization for Volunteer
                    InteractiveRadarMap(
                        userLocation = userLocation,
                        volunteerLocation = volunteerLocation ?: userLocation,
                        routeHistory = routeHistory,
                        safePlaces = safePlaces,
                        trackingUrl = trackingUrl,
                        onCallEmergency = onCallEmergency,
                        onShareTrackingUrl = onShareTrackingUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .testTag("volunteer_active_radar_map")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onCallWalker(activeEscort.walkerPhone) },
                            colors = ButtonDefaults.buttonColors(containerColor = MidnightElevated),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Walker", fontSize = 11.sp, color = Color.White)
                        }

                        Button(
                            onClick = { onCompleteEscort(activeEscort) },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                            modifier = Modifier.weight(1.4f).height(44.dp).testTag("confirm_safe_arrival_button")
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF03280B), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm Safe Arrival", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF03280B))
                        }
                    }
                }
            }
        } else if (!isOnDuty) {
            // Off-duty screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "You are currently Off Duty",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Toggle the duty switch above to receive escort requests from women nearby and stream live safety GPS.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onToggleDuty,
                        colors = ButtonDefaults.buttonColors(containerColor = GuardianCyan)
                    ) {
                        Text("Go On Duty Now", color = Color(0xFF00363A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // On Duty - Navigation between Requests & Patrol Radar Map
            TabRow(
                selectedTabIndex = selectedDutyTab,
                containerColor = MidnightElevated,
                contentColor = GuardianCyan,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedDutyTab == 0,
                    onClick = { selectedDutyTab = 0 },
                    text = {
                        Text(
                            text = "Walk Requests (${incomingRequests.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedDutyTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_volunteer_requests")
                )
                Tab(
                    selected = selectedDutyTab == 1,
                    onClick = { selectedDutyTab = 1 },
                    text = {
                        Text(
                            text = "Patrol Radar Map",
                            fontSize = 12.sp,
                            fontWeight = if (selectedDutyTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_volunteer_radar")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedDutyTab == 1) {
                // Standby Patrol Radar Map
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Radar, contentDescription = null, tint = GuardianCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PATROL RADAR • FUSED GPS STREAM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GuardianCyan
                            )
                        }

                        Button(
                            onClick = onTriggerManualGpsStream,
                            colors = ButtonDefaults.buttonColors(containerColor = MidnightElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ping Supabase", fontSize = 10.sp, color = SafeGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    InteractiveRadarMap(
                        userLocation = userLocation,
                        volunteerLocation = volunteerLocation,
                        routeHistory = routeHistory,
                        safePlaces = safePlaces,
                        trackingUrl = trackingUrl,
                        onCallEmergency = onCallEmergency,
                        onShareTrackingUrl = onShareTrackingUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("volunteer_patrol_radar_map")
                    )
                }
            } else {
                // Incoming Requests List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    if (incomingRequests.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No pending requests nearby. Standing by on safety radar.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(incomingRequests) { request ->
                            WalkRequestCard(
                                request = request,
                                onAccept = { onAcceptRequest(request) },
                                onCall = { onCallWalker(request.walkerPhone) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VolunteerStatBox(title: String, value: String, valueColor: Color) {
    Surface(
        color = MidnightElevated,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun WalkRequestCard(
    request: WalkRequest,
    onAccept: () -> Unit,
    onCall: () -> Unit
) {
    val urgencyColor = when (request.urgencyLevel) {
        UrgencyLevel.HIGH_UNLIT_AREA -> AlertRed
        UrgencyLevel.MODERATE_NIGHT_WALK -> WarningAmber
        UrgencyLevel.STANDARD_TRANSIT -> GuardianCyan
    }

    val urgencyLabel = when (request.urgencyLevel) {
        UrgencyLevel.HIGH_UNLIT_AREA -> "HIGH PRIORITY • UNLIT AREA"
        UrgencyLevel.MODERATE_NIGHT_WALK -> "MODERATE PRIORITY • NIGHT CORRIDOR"
        UrgencyLevel.STANDARD_TRANSIT -> "TRANSIT ESCORT REQUEST"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MidnightCard,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(urgencyColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = urgencyLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = urgencyColor
                    )
                }

                Text(
                    text = "${request.distanceMeters}m away • ~${request.estimatedWalkMin} min",
                    fontSize = 11.sp,
                    color = GuardianCyan
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Walker: ${request.walkerName}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = AlertRed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Pickup: ${request.pickupLocation}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Destination: ${request.destination}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1.5f).height(42.dp).testTag("accept_request_${request.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = GuardianCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = Color(0xFF00363A), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Accept & Escort", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00363A))
                }
            }
        }
    }
}
