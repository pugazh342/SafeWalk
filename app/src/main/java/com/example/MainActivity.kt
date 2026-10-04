package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppMode
import com.example.ui.SafeWalkViewModel
import com.example.ui.components.AuthDialog
import com.example.ui.components.FakeCallOverlay
import com.example.ui.components.SafeWalkTopBar
import com.example.ui.components.SosAlertModal
import com.example.ui.screens.AudioGuardianScreen
import com.example.ui.screens.ContactsHistoryScreen
import com.example.ui.screens.OfflineSosScreen
import com.example.ui.screens.RouteRadarScreen
import com.example.ui.screens.VolunteerPortalScreen
import com.example.ui.screens.WalkGuardScreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AmethystDeep
import com.example.ui.theme.AmethystPrimary
import com.example.ui.theme.GuardianCyan
import com.example.ui.theme.MidnightBase
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.SafeWalkTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeWalkTheme {
                SafeWalkApp()
            }
        }
    }
}

@Composable
fun SafeWalkApp(
    viewModel: SafeWalkViewModel = viewModel()
) {
    val context = LocalContext.current

    // Request vital safety permissions on launch
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Permissions handled gracefully
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.CALL_PHONE
            )
        )
    }

    // Collect Toast and Intent events
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.externalIntentEvent.collectLatest { intent ->
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open external app: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ViewModel State
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val isWalkActive by viewModel.isWalkActive.collectAsStateWithLifecycle()
    val currentDestination by viewModel.currentWalkDestination.collectAsStateWithLifecycle()
    val walkDurationSeconds by viewModel.walkDurationSeconds.collectAsStateWithLifecycle()
    val walkDistanceMeters by viewModel.walkDistanceMeters.collectAsStateWithLifecycle()
    val pairedVolunteer by viewModel.pairedVolunteer.collectAsStateWithLifecycle()
    val nearbyVolunteers by viewModel.nearbyVolunteers.collectAsStateWithLifecycle()

    val isSosActive by viewModel.isSosActive.collectAsStateWithLifecycle()
    val sosCountdown by viewModel.sosCountdown.collectAsStateWithLifecycle()
    val isOfflineSosMode by viewModel.isOfflineSosMode.collectAsStateWithLifecycle()
    val isStrobeActive by viewModel.isStrobeActive.collectAsStateWithLifecycle()
    val isSirenActive by viewModel.isSirenActive.collectAsStateWithLifecycle()
    val policeNotified by viewModel.policeDispatchNotified.collectAsStateWithLifecycle()
    val smsDispatchedCount by viewModel.smsDispatchedCount.collectAsStateWithLifecycle()

    val backendReport by viewModel.backendReport.collectAsStateWithLifecycle()
    val isBackendBusy by viewModel.isBackendBusy.collectAsStateWithLifecycle()

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val authErrorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()
    val isSupabaseGpsStreaming by viewModel.isSupabaseGpsStreaming.collectAsStateWithLifecycle()

    var showAuthDialog by remember { mutableStateOf(false) }

    val currentDecibels by viewModel.currentDecibels.collectAsStateWithLifecycle()
    val isAudioMonitoring by viewModel.isAudioMonitoring.collectAsStateWithLifecycle()
    val isRecordingEvidence by viewModel.isRecordingEvidence.collectAsStateWithLifecycle()

    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val volunteerLocation by viewModel.volunteerLocation.collectAsStateWithLifecycle()
    val routeHistory by viewModel.routeHistory.collectAsStateWithLifecycle()
    val safePlaces by viewModel.safePlaces.collectAsStateWithLifecycle()

    val emergencyContacts by viewModel.emergencyContacts.collectAsStateWithLifecycle()
    val walkHistory by viewModel.walkHistory.collectAsStateWithLifecycle()
    val sosHistory by viewModel.sosHistory.collectAsStateWithLifecycle()

    // Volunteer Portal State
    val isVolunteerOnDuty by viewModel.isVolunteerOnDuty.collectAsStateWithLifecycle()
    val incomingRequests by viewModel.incomingRequests.collectAsStateWithLifecycle()
    val activeEscortRequest by viewModel.activeEscortRequest.collectAsStateWithLifecycle()

    // Fake Call State
    val fakeCallState by viewModel.fakeCallHelper.callState.collectAsStateWithLifecycle()
    val fakeCallerName by viewModel.fakeCallHelper.callerName.collectAsStateWithLifecycle()
    val fakeCallDuration by viewModel.fakeCallHelper.callDurationSec.collectAsStateWithLifecycle()

    val trackingUrl = remember(userLocation) {
        viewModel.locationTracker.generateEncryptedLiveTrackingUrl(
            viewModel.activeWalkId.value ?: "walk_live"
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(MidnightBase)) {
        Scaffold(
            topBar = {
                SafeWalkTopBar(
                    currentMode = currentMode,
                    currentUser = currentUser,
                    onOpenAuth = { showAuthDialog = true },
                    onModeChange = { newMode ->
                        viewModel.setAppMode(newMode)
                    },
                    isSosActive = isSosActive,
                    isWalkActive = isWalkActive
                )
            },
            bottomBar = {
                if (currentMode == AppMode.USER_WALKER) {
                    NavigationBar(
                        containerColor = MidnightElevated,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                    contentDescription = "Walk Guard",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Walk Guard", fontSize = 10.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF16082B),
                                selectedTextColor = AmethystPrimary,
                                indicatorColor = AmethystPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_walk_guard")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = "Route Radar",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Radar Map", fontSize = 10.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00363A),
                                selectedTextColor = GuardianCyan,
                                indicatorColor = GuardianCyan,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_radar")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Audio Guardian",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Audio Guard", fontSize = 10.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF16082B),
                                selectedTextColor = AmethystPrimary,
                                indicatorColor = AmethystPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_audio")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                                    contentDescription = "Offline SOS",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Offline SOS", fontSize = 10.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = AlertRed,
                                indicatorColor = AlertRed,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_offline_sos")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.ContactPhone,
                                    contentDescription = "Contacts & Logs",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Contacts", fontSize = 10.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF16082B),
                                selectedTextColor = AmethystPrimary,
                                indicatorColor = AmethystPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_contacts")
                        )
                    }
                }
            },
            containerColor = MidnightBase
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (currentMode == AppMode.VOLUNTEER) {
                    // Volunteer Guardian Portal
                    VolunteerPortalScreen(
                        isOnDuty = isVolunteerOnDuty,
                        incomingRequests = incomingRequests,
                        activeEscort = activeEscortRequest,
                        volunteerLocation = volunteerLocation,
                        userLocation = userLocation,
                        routeHistory = routeHistory,
                        safePlaces = safePlaces,
                        isSupabaseGpsStreaming = isSupabaseGpsStreaming,
                        trackingUrl = trackingUrl,
                        currentUser = currentUser,
                        onToggleDuty = { viewModel.toggleVolunteerDuty() },
                        onAcceptRequest = { request -> viewModel.acceptWalkRequest(request) },
                        onCompleteEscort = { request -> viewModel.completeEscortSession(request) },
                        onCallWalker = { phone -> viewModel.dispatchHelper.dialEmergencyNumber(phone) },
                        onCallEmergency = { phone -> viewModel.dispatchHelper.dialEmergencyNumber(phone) },
                        onShareTrackingUrl = { url ->
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, "SafeWalk Escort Live Stream: $url")
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Escort Stream"))
                        },
                        onTriggerManualGpsStream = { viewModel.triggerImmediateGpsStream() },
                        onOpenAuth = { showAuthDialog = true }
                    )
                } else {
                    // Walker Mode Tabs
                    when (selectedTab) {
                        0 -> WalkGuardScreen(
                            isWalkActive = isWalkActive,
                            currentDestination = currentDestination,
                            walkDurationSeconds = walkDurationSeconds,
                            walkDistanceMeters = walkDistanceMeters,
                            pairedVolunteer = pairedVolunteer,
                            nearbyVolunteers = nearbyVolunteers,
                            currentDecibels = currentDecibels,
                            isAudioMonitoring = isAudioMonitoring,
                            isStrobeActive = isStrobeActive,
                            isSirenActive = isSirenActive,
                            sosCountdown = sosCountdown,
                            onDestinationChange = { viewModel.setDestination(it) },
                            onStartWalk = { viewModel.startSafeWalk() },
                            onCompleteWalk = { viewModel.completeSafeWalk() },
                            onPairVolunteer = { viewModel.pairWithVolunteer(it) },
                            onUnpairVolunteer = { viewModel.unpairCurrentVolunteer() },
                            onRequestSosCountdown = { viewModel.requestSosWithCountdown(3, "Instant Panic Button") },
                            onCancelSosCountdown = { viewModel.cancelSosCountdown() },
                            onTriggerInstantSos = { viewModel.triggerSos("Panic Button Pressed") },
                            onTriggerFakeCall = { viewModel.triggerFakeCall("Dad (Waiting at Corner)") },
                            onToggleStrobe = { viewModel.toggleStrobeFlash() },
                            onToggleSiren = { viewModel.toggleSirenSound() },
                            onNavigateToRadar = { selectedTab = 1 }
                        )

                        1 -> RouteRadarScreen(
                            userLocation = userLocation,
                            volunteerLocation = volunteerLocation,
                            pairedVolunteer = pairedVolunteer,
                            routeHistory = routeHistory,
                            safePlaces = safePlaces,
                            trackingUrl = trackingUrl,
                            isSupabaseGpsStreaming = isSupabaseGpsStreaming,
                            onCallPlace = { phone -> viewModel.dispatchHelper.dialEmergencyNumber(phone) },
                            onShareTrackingUrl = { url ->
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "SafeWalk Live E2E Tracking Stream: $url")
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Live Walk Stream"))
                            },
                            onCopyTrackingUrl = { url ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("SafeWalk Tracking URL", url)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Encrypted link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        2 -> AudioGuardianScreen(
                            currentDecibels = currentDecibels,
                            isAudioMonitoring = isAudioMonitoring,
                            isRecordingEvidence = isRecordingEvidence,
                            onToggleMonitoring = { viewModel.toggleAudioMonitoring() },
                            onStartEvidenceRecording = {
                                viewModel.captureAudioEvidenceNow()
                            },
                            onTriggerFakeCall = { caller -> viewModel.triggerFakeCall(caller) }
                        )

                        3 -> OfflineSosScreen(
                            userLocation = userLocation,
                            pairedVolunteer = pairedVolunteer,
                            contacts = emergencyContacts,
                            isOfflineSosActive = isOfflineSosMode,
                            isStrobeActive = isStrobeActive,
                            isSirenActive = isSirenActive,
                            onToggleOfflineSos = { viewModel.toggleOfflineSosMode() },
                            onToggleStrobe = { viewModel.toggleStrobeFlash() },
                            onToggleSiren = { viewModel.toggleSirenSound() },
                            onDispatchOfflineSms = {
                                val msg = viewModel.dispatchHelper.buildEmergencySmsMessage(
                                    userLat = userLocation.latitude,
                                    userLng = userLocation.longitude,
                                    volunteer = pairedVolunteer,
                                    walkDestination = currentDestination,
                                    trackingUrl = trackingUrl
                                )
                                viewModel.dispatchHelper.dispatchSmsToContacts(
                                    contacts = emergencyContacts,
                                    message = msg,
                                    onSuccess = { count ->
                                        Toast.makeText(context, "Offline SMS dispatched to $count contacts!", Toast.LENGTH_LONG).show()
                                    },
                                    onFallbackToIntent = { intent ->
                                        context.startActivity(intent)
                                    }
                                )
                            },
                            onCallNumber = { phone -> viewModel.dispatchHelper.dialEmergencyNumber(phone) }
                        )

                        4 -> ContactsHistoryScreen(
                            contacts = emergencyContacts,
                            walkHistory = walkHistory,
                            sosHistory = sosHistory,
                            backendReport = backendReport,
                            isBackendBusy = isBackendBusy,
                            onTestBackend = { viewModel.testBackendNotifications() },
                            onAddContact = { name, phone, rel, isPri, isPol ->
                                viewModel.addEmergencyContact(name, phone, rel, isPri, isPol)
                            },
                            onDeleteContact = { viewModel.deleteEmergencyContact(it) },
                            onCallContact = { phone -> viewModel.dispatchHelper.dialEmergencyNumber(phone) }
                        )
                    }
                }
            }
        }

        // Active Emergency SOS Modal Overlay
        SosAlertModal(
            isSosActive = isSosActive,
            userLocation = userLocation,
            smsDispatchedCount = smsDispatchedCount,
            policeNotified = policeNotified,
            backendReport = backendReport,
            isStrobeActive = isStrobeActive,
            isSirenActive = isSirenActive,
            isRecordingAudio = isRecordingEvidence,
            onToggleStrobe = { viewModel.toggleStrobeFlash() },
            onToggleSiren = { viewModel.toggleSirenSound() },
            onCancelSos = { viewModel.cancelSos() },
            onCallNumber = { phone -> viewModel.dispatchHelper.dialEmergencyNumber(phone) }
        )

        // Fake Phone Call Overlay
        FakeCallOverlay(
            callState = fakeCallState,
            callerName = fakeCallerName,
            callDurationSec = fakeCallDuration,
            onAccept = { viewModel.acceptFakeCall() },
            onDeclineOrEnd = { viewModel.endFakeCall() }
        )

        // Supabase User & Volunteer Authentication Dialog
        if (showAuthDialog) {
            AuthDialog(
                currentUser = currentUser,
                isLoading = isAuthLoading,
                errorMessage = authErrorMessage,
                onDismiss = { showAuthDialog = false },
                onSignIn = { email, pass ->
                    viewModel.signIn(email, pass)
                },
                onSignUp = { email, pass, name, phone, role, badge ->
                    viewModel.signUp(email, pass, name, phone, role, badge)
                },
                onSignOut = {
                    viewModel.signOut()
                    showAuthDialog = false
                },
                onQuickDemoLogin = { role ->
                    viewModel.quickDemoLogin(role)
                    showAuthDialog = false
                }
            )
        }
    }
}
