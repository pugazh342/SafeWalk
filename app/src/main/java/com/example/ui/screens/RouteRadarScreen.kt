package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.model.Volunteer
import com.example.service.LatLngPoint
import com.example.service.SafePlace
import com.example.service.SafePlaceType
import com.example.ui.components.InteractiveRadarMap
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmethystPrimary
import com.example.ui.theme.GuardianCyan
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.WarningAmber

@Composable
fun RouteRadarScreen(
    userLocation: LatLngPoint,
    volunteerLocation: LatLngPoint?,
    pairedVolunteer: Volunteer?,
    routeHistory: List<LatLngPoint>,
    safePlaces: List<SafePlace>,
    trackingUrl: String,
    isSupabaseGpsStreaming: Boolean = false,
    onCallPlace: (String) -> Unit,
    onShareTrackingUrl: (String) -> Unit,
    onCopyTrackingUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Encrypted Telemetry Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AES-256 GCM SECURED TELEMETRY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafeGreen
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isSupabaseGpsStreaming) SafeGreen else GuardianCyan)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isSupabaseGpsStreaming) "SUPABASE LIVE ⚡" else "${routeHistory.size} Waypoints",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSupabaseGpsStreaming) SafeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dual-GPS Synchronization",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = if (pairedVolunteer != null) "Walker + ${pairedVolunteer.name} live track" else "Solo Walker Telemetry Stream",
                            fontSize = 11.sp,
                            color = GuardianCyan
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { onCopyTrackingUrl(trackingUrl) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = AmethystPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { onShareTrackingUrl(trackingUrl) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share Link", tint = GuardianCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive Radar View (takes 280dp)
        InteractiveRadarMap(
            userLocation = userLocation,
            volunteerLocation = volunteerLocation,
            routeHistory = routeHistory,
            safePlaces = safePlaces,
            trackingUrl = trackingUrl,
            onCallEmergency = onCallPlace,
            onShareTrackingUrl = onShareTrackingUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .testTag("interactive_radar_map")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Nearby Safe Havens & Police Stations list
        Text(
            text = "VERIFIED SAFE HAVENS & POLICE BOOTHS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            items(safePlaces) { place ->
                SafePlaceRow(place = place, onCall = { onCallPlace(place.phone) })
            }
        }
    }
}

@Composable
private fun SafePlaceRow(place: SafePlace, onCall: () -> Unit) {
    val icon = when (place.type) {
        SafePlaceType.POLICE_STATION -> Icons.Default.LocalPolice
        SafePlaceType.HOSPITAL -> Icons.Default.LocationOn
        SafePlaceType.TRANSIT_HUB -> Icons.Default.Navigation
        SafePlaceType.COMMUNITY_HAVEN -> Icons.Default.Shield
        SafePlaceType.PHARMACY_247 -> Icons.Default.LocationOn
    }

    val typeColor = when (place.type) {
        SafePlaceType.POLICE_STATION -> AlertRed
        SafePlaceType.HOSPITAL -> SafeGreen
        SafePlaceType.TRANSIT_HUB -> GuardianCyan
        SafePlaceType.COMMUNITY_HAVEN -> AmethystPrimary
        SafePlaceType.PHARMACY_247 -> WarningAmber
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MidnightCard,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = typeColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = place.address,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${place.distanceMeters}m away • Verified 24/7",
                    fontSize = 11.sp,
                    color = SafeGreen
                )
            }

            Button(
                onClick = onCall,
                colors = ButtonDefaults.buttonColors(containerColor = MidnightElevated),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Call", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}
