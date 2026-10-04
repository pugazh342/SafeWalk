package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppMode
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmethystPrimary
import com.example.ui.theme.GuardianCyan
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.SafeGreen

@Composable
fun SafeWalkTopBar(
    currentMode: AppMode,
    currentUser: UserProfile?,
    onOpenAuth: () -> Unit,
    onModeChange: (AppMode) -> Unit,
    isSosActive: Boolean,
    isWalkActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = if (isSosActive) listOf(AlertRed, Color(0xFF88001B))
                                    else listOf(AmethystPrimary, GuardianCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSosActive) Icons.Default.Security else Icons.Default.Shield,
                            contentDescription = "SafeWalk Shield",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SafeWalk",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isSosActive) AlertRed.copy(alpha = 0.2f)
                                        else GuardianCyan.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Encrypted",
                                        tint = if (isSosActive) AlertRed else GuardianCyan,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isSosActive) "SOS BROADCAST" else "E2E ENCRYPTED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSosActive) AlertRed else GuardianCyan
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isWalkActive) "Journey Active • GPS Synchronized" else "Night Guard & Community Escorts",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isWalkActive) SafeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Profile / Auth Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onOpenAuth() }
                            .testTag("auth_profile_button"),
                        color = MidnightElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentUser != null) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (currentUser.role == UserRole.VOLUNTEER) GuardianCyan else AmethystPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser.name.take(1).uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16082B)
                                    )
                                }
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = currentUser.name.take(7),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Sign In",
                                    tint = AmethystPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sign In",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AmethystPrimary
                                )
                            }
                        }
                    }

                    // GPS / Guard indicator dot
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MidnightElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSosActive) AlertRed else SafeGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isSosActive) "SOS" else "GPS LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSosActive) AlertRed else SafeGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Two-Mode Toggle Switcher Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MidnightElevated)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val isWalker = currentMode == AppMode.USER_WALKER
                val isVolunteer = currentMode == AppMode.VOLUNTEER

                val walkerBg by animateColorAsState(
                    targetValue = if (isWalker) AmethystPrimary else Color.Transparent,
                    label = "walkerBg"
                )
                val volunteerBg by animateColorAsState(
                    targetValue = if (isVolunteer) GuardianCyan else Color.Transparent,
                    label = "volunteerBg"
                )

                // Walker Mode Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(walkerBg)
                        .clickable { onModeChange(AppMode.USER_WALKER) }
                        .padding(vertical = 8.dp)
                        .testTag("mode_walker_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "Walker Mode",
                            tint = if (isWalker) Color(0xFF16082B) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I Am Walking",
                            fontSize = 13.sp,
                            fontWeight = if (isWalker) FontWeight.Bold else FontWeight.Medium,
                            color = if (isWalker) Color(0xFF16082B) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Volunteer Mode Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(volunteerBg)
                        .clickable { onModeChange(AppMode.VOLUNTEER) }
                        .padding(vertical = 8.dp)
                        .testTag("mode_volunteer_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Volunteer Guard Mode",
                            tint = if (isVolunteer) Color(0xFF00363A) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Volunteer Guard",
                            fontSize = 13.sp,
                            fontWeight = if (isVolunteer) FontWeight.Bold else FontWeight.Medium,
                            color = if (isVolunteer) Color(0xFF00363A) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
