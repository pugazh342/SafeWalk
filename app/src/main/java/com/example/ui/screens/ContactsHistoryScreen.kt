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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.BackendDispatchReport
import com.example.data.EmergencyContact
import com.example.data.SosLog
import com.example.data.WalkSession
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmethystPrimary
import com.example.ui.theme.GuardianCyan
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ContactsHistoryScreen(
    contacts: List<EmergencyContact>,
    walkHistory: List<WalkSession>,
    sosHistory: List<SosLog>,
    backendReport: BackendDispatchReport,
    isBackendBusy: Boolean,
    onTestBackend: () -> Unit,
    onAddContact: (name: String, phone: String, relationship: String, isPrimary: Boolean, isPolice: Boolean) -> Unit,
    onDeleteContact: (EmergencyContact) -> Unit,
    onCallContact: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MidnightCard,
            contentColor = AmethystPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AmethystPrimary
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Contacts (${contacts.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("tab_contacts")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Walk Logs (${walkHistory.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("tab_walk_logs")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("SOS Events (${sosHistory.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("tab_sos_logs")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Cloud Backend", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("tab_cloud_backend")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> {
                // Emergency Contacts List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRUSTED EMERGENCY CIRCLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AmethystPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp).testTag("add_contact_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF16082B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Contact", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16082B))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(contacts) { contact ->
                        ContactCard(
                            contact = contact,
                            onCall = { onCallContact(contact.phone) },
                            onDelete = { onDeleteContact(contact) }
                        )
                    }
                }
            }

            1 -> {
                // Walk History
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (walkHistory.isEmpty()) {
                        item {
                            EmptyHistoryBox("No past walk sessions recorded yet. Start a SafeWalk to log your protected routes.")
                        }
                    } else {
                        items(walkHistory) { session ->
                            WalkSessionCard(session = session)
                        }
                    }
                }
            }

            2 -> {
                // SOS History
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (sosHistory.isEmpty()) {
                        item {
                            EmptyHistoryBox("No SOS alarms logged. Your past journeys have been safe.")
                        }
                    } else {
                        items(sosHistory) { log ->
                            SosLogCard(log = log)
                        }
                    }
                }
            }

            3 -> {
                // Cloud Backend & Real Notification Gateway
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MidnightCard,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "REAL BACKEND & NOTIFICATION CHANNELS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GuardianCyan,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "SafeWalk is connected to your dedicated Supabase cloud database, Discord Emergency Webhook, and direct Gmail SMTP alert relay.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = onTestBackend,
                                    enabled = !isBackendBusy,
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isBackendBusy) MidnightElevated else AmethystPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("test_backend_button")
                                ) {
                                    Text(
                                        text = if (isBackendBusy) "Broadcasting Test Signals..." else "Send Test Alert (Supabase + Discord + Email)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBackendBusy) Color.Gray else Color(0xFF16082B)
                                    )
                                }
                            }
                        }
                    }

                    // Channel 1: Supabase
                    item {
                        BackendChannelCard(
                            title = "Supabase PostgreSQL Database",
                            endpoint = "xqxkyfwyuszducjctgwb.supabase.co",
                            description = "Stores real-time SOS incident logs, active walk sessions, and volunteer pairing telemetry via PostgREST.",
                            status = backendReport.supabaseStatus,
                            statusColor = if (backendReport.supabaseStatus.contains("✅")) SafeGreen else GuardianCyan
                        )
                    }

                    // Channel 2: Discord Webhook
                    item {
                        BackendChannelCard(
                            title = "Discord Emergency Sentinel Webhook",
                            endpoint = "discord.com/api/webhooks/152952...",
                            description = "Instantly broadcasts rich red emergency embeds with clickable Google Maps hyperlinks, live GPS coordinates, and escort identity.",
                            status = backendReport.discordStatus,
                            statusColor = if (backendReport.discordStatus.contains("✅")) SafeGreen else AlertRed
                        )
                    }

                    // Channel 3: Email Alerts
                    item {
                        BackendChannelCard(
                            title = "Direct Gmail SMTP Emergency Alert",
                            endpoint = "smtp.gmail.com:465 (SSL)",
                            description = "Directly dispatches formatted HTML emergency alerts to emergency contact (kpugahmani21@gmai.com) with map links.",
                            status = backendReport.emailStatus,
                            statusColor = if (backendReport.emailStatus.contains("✅")) SafeGreen else AlertRed
                        )
                    }
                }
            }
        }
    }

    // Add Contact Dialog
    if (showAddDialog) {
        var nameInput by remember { mutableStateOf("") }
        var phoneInput by remember { mutableStateOf("") }
        var relationInput by remember { mutableStateOf("") }
        var isPrimary by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = MidnightCard,
            title = {
                Text(
                    text = "Add Emergency Contact",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_contact_name")
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_contact_phone")
                    )
                    OutlinedTextField(
                        value = relationInput,
                        onValueChange = { relationInput = it },
                        label = { Text("Relationship (e.g. Sister, Roommate)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_contact_relation")
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isPrimary,
                            onCheckedChange = { isPrimary = it },
                            colors = CheckboxDefaults.colors(checkedColor = SafeGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Set as Primary Guardian (1st to receive SOS)", fontSize = 12.sp, color = Color.LightGray)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && phoneInput.isNotBlank()) {
                            onAddContact(nameInput, phoneInput, relationInput.ifBlank { "Guardian" }, isPrimary, false)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmethystPrimary),
                    modifier = Modifier.testTag("save_contact_button")
                ) {
                    Text("Save Contact", color = Color(0xFF16082B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ContactCard(
    contact: EmergencyContact,
    onCall: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MidnightCard,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (contact.isPolice) AlertRed.copy(alpha = 0.2f) else MidnightElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (contact.isPolice) Icons.Default.LocalPolice else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (contact.isPolice) AlertRed else AmethystPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (contact.isPrimary) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SafeGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("PRIMARY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                        }
                    }
                }
                Text(
                    text = "${contact.relationship} • ${contact.phone}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
                Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = SafeGreen, modifier = Modifier.size(18.dp))
            }

            if (!contact.isPolice) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = AlertRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun WalkSessionCard(session: WalkSession) {
    val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(session.startTime))
    val min = session.durationSeconds / 60
    val sec = session.durationSeconds % 60

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MidnightCard,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SafeGreen.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.destination,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$dateStr • ${session.distanceMeters}m • %02d:%02d".format(min, sec),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (session.volunteerName != null) {
                    Text(
                        text = "Escorted by ${session.volunteerName}",
                        fontSize = 11.sp,
                        color = GuardianCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun SosLogCard(log: SosLog) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MidnightCard,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AlertRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SOS Trigger: ${log.triggerReason}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlertRed
                )
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "GPS: ${"%.4f".format(log.latitude)}, ${"%.4f".format(log.longitude)} • Dispatched: ${log.dispatchedContactsCount} contacts",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun BackendChannelCard(
    title: String,
    endpoint: String,
    description: String,
    status: String,
    statusColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MidnightCard,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (status.contains("✅")) "ONLINE" else "STANDBY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = endpoint,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = GuardianCyan
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Live Status: $status",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = statusColor
            )
        }
    }
}

