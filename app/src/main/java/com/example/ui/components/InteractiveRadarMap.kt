package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.LatLngPoint
import com.example.service.SafePlace
import com.example.service.SafePlaceType
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmethystDeep
import com.example.ui.theme.AmethystPrimary
import com.example.ui.theme.GuardianCyan
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.WarningAmber
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InteractiveRadarMap(
    userLocation: LatLngPoint,
    volunteerLocation: LatLngPoint?,
    routeHistory: List<LatLngPoint>,
    safePlaces: List<SafePlace>,
    trackingUrl: String,
    onCallEmergency: (String) -> Unit,
    onShareTrackingUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlace by remember { mutableStateOf<SafePlace?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "radarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MidnightCard)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(safePlaces, userLocation) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val scale = size.width * 28000f

                        // Find if tap is near any safe place
                        val clicked = safePlaces.firstOrNull { place ->
                            val dx = (place.longitude - userLocation.longitude) * scale
                            val dy = -(place.latitude - userLocation.latitude) * scale
                            val px = centerX + dx.toFloat()
                            val py = centerY + dy.toFloat()
                            val dist = kotlin.math.hypot(tapOffset.x - px, tapOffset.y - py)
                            dist < 60f
                        }
                        selectedPlace = clicked
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f
            val maxRadius = kotlin.math.min(centerX, centerY) * 0.95f

            // 1. Draw subtle night coordinate grid
            val gridSpacing = 40.dp.toPx()
            var x = 0f
            while (x < canvasW) {
                drawLine(
                    color = MidnightBorder.copy(alpha = 0.35f),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }
            var y = 0f
            while (y < canvasH) {
                drawLine(
                    color = MidnightBorder.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }

            // 2. Draw Concentric Radar Rings
            val rings = listOf(0.25f, 0.5f, 0.75f, 1.0f)
            rings.forEach { ratio ->
                drawCircle(
                    color = GuardianCyan.copy(alpha = 0.12f),
                    radius = maxRadius * ratio,
                    center = Offset(centerX, centerY),
                    style = Stroke(
                        width = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            }

            // Crosshairs
            drawLine(
                color = GuardianCyan.copy(alpha = 0.2f),
                start = Offset(centerX - maxRadius, centerY),
                end = Offset(centerX + maxRadius, centerY),
                strokeWidth = 1f
            )
            drawLine(
                color = GuardianCyan.copy(alpha = 0.2f),
                start = Offset(centerX, centerY - maxRadius),
                end = Offset(centerX, centerY + maxRadius),
                strokeWidth = 1f
            )

            // 3. Radar Sweep Line & Cone
            val rad = Math.toRadians(sweepAngle.toDouble())
            val sweepX = centerX + maxRadius * cos(rad).toFloat()
            val sweepY = centerY + maxRadius * sin(rad).toFloat()
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(GuardianCyan.copy(alpha = 0.8f), Color.Transparent),
                    start = Offset(centerX, centerY),
                    end = Offset(sweepX, sweepY)
                ),
                start = Offset(centerX, centerY),
                end = Offset(sweepX, sweepY),
                strokeWidth = 2f
            )

            // Scale factor to map lat/lng deltas to canvas pixels
            val scale = canvasW * 28000f

            // 4. Draw Route History Polyline
            if (routeHistory.size > 1) {
                val path = Path()
                routeHistory.forEachIndexed { index, pt ->
                    val dx = (pt.longitude - userLocation.longitude) * scale
                    val dy = -(pt.latitude - userLocation.latitude) * scale
                    val px = centerX + dx.toFloat()
                    val py = centerY + dy.toFloat()
                    if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }

                // Corridor halo
                drawPath(
                    path = path,
                    color = AmethystPrimary.copy(alpha = 0.25f),
                    style = Stroke(width = 12f)
                )
                // Core track line
                drawPath(
                    path = path,
                    color = AmethystPrimary,
                    style = Stroke(width = 3.5f)
                )
            }

            // 5. Draw Nearby Safe Havens / Police Stations
            safePlaces.forEach { place ->
                val dx = (place.longitude - userLocation.longitude) * scale
                val dy = -(place.latitude - userLocation.latitude) * scale
                val px = centerX + dx.toFloat()
                val py = centerY + dy.toFloat()

                val pinColor = when (place.type) {
                    SafePlaceType.POLICE_STATION -> AlertRed
                    SafePlaceType.HOSPITAL -> SafeGreen
                    SafePlaceType.TRANSIT_HUB -> GuardianCyan
                    SafePlaceType.COMMUNITY_HAVEN -> AmethystPrimary
                    SafePlaceType.PHARMACY_247 -> WarningAmber
                }

                // Outer aura ring
                drawCircle(
                    color = pinColor.copy(alpha = 0.22f),
                    radius = 16f,
                    center = Offset(px, py)
                )
                // Core pin
                drawCircle(
                    color = pinColor,
                    radius = 7.5f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = Offset(px, py)
                )
            }

            // 6. Draw Paired Volunteer Location
            volunteerLocation?.let { vol ->
                val dx = (vol.longitude - userLocation.longitude) * scale
                val dy = -(vol.latitude - userLocation.latitude) * scale
                val px = centerX + dx.toFloat()
                val py = centerY + dy.toFloat()

                // Companion radar beacon
                drawCircle(
                    color = SafeGreen.copy(alpha = 0.35f),
                    radius = 22f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = SafeGreen,
                    radius = 9f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(px, py)
                )

                // Dotted line between Walker and Volunteer
                drawLine(
                    color = SafeGreen,
                    start = Offset(centerX, centerY),
                    end = Offset(px, py),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
            }

            // 7. Draw User (Walker) at Center with dynamic pulse
            val pulseRadius = 14f * pulseScale
            drawCircle(
                color = AmethystPrimary.copy(alpha = (1f - (pulseScale - 0.8f) / 1.6f).coerceIn(0f, 0.7f)),
                radius = pulseRadius,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = AmethystDeep,
                radius = 13f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = AmethystPrimary,
                radius = 8.5f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = Offset(centerX, centerY)
            )
        }

        // Radar Overlay Badges
        // Top Left: Coordinates & Range
        Surface(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart),
            color = MidnightElevated.copy(alpha = 0.88f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(GuardianCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RADAR RANGE: 800m",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GuardianCyan
                    )
                }
                Text(
                    text = "${"%.4f".format(userLocation.latitude)}°N, ${"%.4f".format(userLocation.longitude)}°W",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Top Right: Live E2E Encryption Share Badge
        Surface(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopEnd)
                .clickable { onShareTrackingUrl(trackingUrl) },
            color = MidnightElevated.copy(alpha = 0.88f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Tracking Link",
                    tint = AmethystPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Share Stream",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AmethystPrimary
                )
            }
        }

        // Bottom Legend Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = MidnightElevated.copy(alpha = 0.92f),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = AmethystPrimary, label = "You (Walker)")
                volunteerLocation?.let {
                    LegendItem(color = SafeGreen, label = "Volunteer Escort")
                }
                LegendItem(color = AlertRed, label = "Police Station")
                LegendItem(color = GuardianCyan, label = "Safe Havens")
            }
        }
    }

    // Detail Dialog when a Safe Place pin is tapped
    selectedPlace?.let { place ->
        AlertDialog(
            onDismissRequest = { selectedPlace = null },
            containerColor = MidnightCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (place.type == SafePlaceType.POLICE_STATION) Icons.Default.LocalPolice else Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (place.type == SafePlaceType.POLICE_STATION) AlertRed else GuardianCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Address: ${place.address}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Proximity: ${place.distanceMeters}m away from your live GPS location",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = SafeGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Verified 24/7 safe zone with video surveillance and lighted security perimeter.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCallEmergency(place.phone)
                        selectedPlace = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call (${place.phone})")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedPlace = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
