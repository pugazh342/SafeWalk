package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmethystDeep
import com.example.ui.theme.AmethystPrimary
import com.example.ui.theme.GuardianCyan
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.SafeGreen

@Composable
fun AuthDialog(
    currentUser: UserProfile?,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSignIn: (email: String, pass: String) -> Unit,
    onSignUp: (email: String, pass: String, name: String, phone: String, role: UserRole, badge: String?) -> Unit,
    onSignOut: () -> Unit,
    onQuickDemoLogin: (role: UserRole) -> Unit
) {
    if (currentUser != null) {
        // Authenticated Profile Dialog
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MidnightCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (currentUser.role == UserRole.VOLUNTEER) GuardianCyan else AmethystPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentUser.role == UserRole.VOLUNTEER) Icons.Default.Shield else Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = Color(0xFF16082B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Authenticated Account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MidnightElevated,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SafeGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = currentUser.role.name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SafeGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Email: ${currentUser.email}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "Phone: ${currentUser.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (currentUser.volunteerBadge != null) {
                                Text(text = "Badge: ${currentUser.volunteerBadge}", fontSize = 12.sp, color = GuardianCyan)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Authenticated with Supabase Cloud Auth. All live GPS tracks and emergency broadcasts are attributed to this identity.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AmethystPrimary)
                ) {
                    Text("Done", color = Color(0xFF16082B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onSignOut) {
                    Text("Sign Out", color = AlertRed)
                }
            }
        )
        return
    }

    // Sign In / Sign Up Dialog
    var selectedAuthTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Sign Up
    var selectedRole by remember { mutableStateOf(UserRole.WALKER) }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var badgeInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightCard,
        title = {
            Column {
                Text(
                    text = "SafeWalk Identity & Access",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Supabase Auth • Real-time GPS Telemetry Stream",
                    fontSize = 11.sp,
                    color = GuardianCyan
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Tab: Sign In vs Sign Up
                TabRow(
                    selectedTabIndex = selectedAuthTab,
                    containerColor = MidnightElevated,
                    contentColor = AmethystPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedAuthTab == 0,
                        onClick = { selectedAuthTab = 0 },
                        text = { Text("Sign In", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_auth_signin")
                    )
                    Tab(
                        selected = selectedAuthTab == 1,
                        onClick = { selectedAuthTab = 1 },
                        text = { Text("Sign Up", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_auth_signup")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Role selector
                Text(
                    text = "SELECT YOUR ROLE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Walker Option
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedRole = UserRole.WALKER }
                            .border(
                                width = if (selectedRole == UserRole.WALKER) 1.5.dp else 0.dp,
                                color = if (selectedRole == UserRole.WALKER) AmethystPrimary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        color = if (selectedRole == UserRole.WALKER) AmethystDeep.copy(alpha = 0.5f) else MidnightElevated
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, tint = AmethystPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Walker", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Volunteer Option
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedRole = UserRole.VOLUNTEER }
                            .border(
                                width = if (selectedRole == UserRole.VOLUNTEER) 1.5.dp else 0.dp,
                                color = if (selectedRole == UserRole.VOLUNTEER) GuardianCyan else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        color = if (selectedRole == UserRole.VOLUNTEER) GuardianCyan.copy(alpha = 0.25f) else MidnightElevated
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = GuardianCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Volunteer Guard", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input fields
                if (selectedAuthTab == 1) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name", color = Color.Gray) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = AmethystPrimary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Emergency Phone Number", color = Color.Gray) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = AmethystPrimary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_phone_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedRole == UserRole.VOLUNTEER) {
                        OutlinedTextField(
                            value = badgeInput,
                            onValueChange = { badgeInput = it },
                            label = { Text("Volunteer Badge / Certification ID", color = Color.Gray) },
                            leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = GuardianCyan) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_badge_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email Address", color = Color.Gray) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = AmethystPrimary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password (min 6 characters)", color = Color.Gray) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AmethystPrimary) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage, fontSize = 11.sp, color = AlertRed)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // One-tap quick presets
                Text(
                    text = "OR ONE-TAP QUICK PRESET",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onQuickDemoLogin(UserRole.WALKER) },
                        modifier = Modifier.weight(1f).height(38.dp).testTag("quick_walker_login")
                    ) {
                        Text("Demo Walker", fontSize = 11.sp, color = AmethystPrimary)
                    }

                    OutlinedButton(
                        onClick = { onQuickDemoLogin(UserRole.VOLUNTEER) },
                        modifier = Modifier.weight(1f).height(38.dp).testTag("quick_volunteer_login")
                    ) {
                        Text("Demo Guard", fontSize = 11.sp, color = GuardianCyan)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedAuthTab == 0) {
                        if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                            onSignIn(emailInput.trim(), passwordInput.trim())
                        }
                    } else {
                        if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                            onSignUp(
                                emailInput.trim(),
                                passwordInput.trim(),
                                nameInput.ifBlank { emailInput.substringBefore("@") },
                                phoneInput.ifBlank { "+1-555-010-0000" },
                                selectedRole,
                                badgeInput.ifBlank { if (selectedRole == UserRole.VOLUNTEER) "SW-7842" else null }
                            )
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = AmethystPrimary),
                modifier = Modifier.testTag("auth_submit_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF16082B), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (selectedAuthTab == 0) "Sign In" else "Create Account",
                        color = Color(0xFF16082B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
