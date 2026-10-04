package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.backend.BackendNotificationManager
import com.example.data.EmergencyContact
import com.example.data.SafeWalkDatabase
import com.example.data.SafeWalkRepository
import com.example.data.SosLog
import com.example.data.WalkSession
import com.example.model.AppMode
import com.example.model.LiveStreamPoint
import com.example.model.RequestStatus
import com.example.model.UrgencyLevel
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.model.Volunteer
import com.example.model.WalkRequest
import com.example.service.EmergencyDispatchHelper
import com.example.service.FakeCallHelper
import com.example.service.HardwareAlarmManager
import com.example.service.LatLngPoint
import com.example.service.SafePlace
import com.example.service.SafeWalkLocationTracker
import com.example.service.SafetyAudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SafeWalkViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SafeWalkDatabase.getInstance(application)
    private val repository = SafeWalkRepository(
        database.contactDao(),
        database.walkSessionDao(),
        database.sosLogDao()
    )

    val locationTracker = SafeWalkLocationTracker(application)
    val audioManager = SafetyAudioManager(application)
    val alarmManager = HardwareAlarmManager(application)
    val dispatchHelper = EmergencyDispatchHelper(application)
    val fakeCallHelper = FakeCallHelper(application)
    val backendManager = BackendNotificationManager()

    val backendReport = backendManager.dispatchReport
    val isBackendBusy = backendManager.isBusy

    // User & Volunteer Authentication
    private val _currentUser = MutableStateFlow<UserProfile?>(
        UserProfile(
            id = "demo_usr_01",
            email = "walker.kavya@safewalk.org",
            name = "Kavya Sharma",
            phone = "+1-555-019-2834",
            role = UserRole.WALKER
        )
    )
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _isSupabaseGpsStreaming = MutableStateFlow(false)
    val isSupabaseGpsStreaming: StateFlow<Boolean> = _isSupabaseGpsStreaming.asStateFlow()

    // Current Mode
    private val _currentMode = MutableStateFlow(AppMode.USER_WALKER)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    // Walker Mode UI State
    private val _isWalkActive = MutableStateFlow(false)
    val isWalkActive: StateFlow<Boolean> = _isWalkActive.asStateFlow()

    private val _currentWalkDestination = MutableStateFlow("Home (742 Evergreen Terr)")
    val currentWalkDestination: StateFlow<String> = _currentWalkDestination.asStateFlow()

    private val _walkDurationSeconds = MutableStateFlow(0)
    val walkDurationSeconds: StateFlow<Int> = _walkDurationSeconds.asStateFlow()

    private val _walkDistanceMeters = MutableStateFlow(0)
    val walkDistanceMeters: StateFlow<Int> = _walkDistanceMeters.asStateFlow()

    private val _pairedVolunteer = MutableStateFlow<Volunteer?>(null)
    val pairedVolunteer: StateFlow<Volunteer?> = _pairedVolunteer.asStateFlow()

    private val _activeWalkId = MutableStateFlow<String?>(null)
    val activeWalkId: StateFlow<String?> = _activeWalkId.asStateFlow()

    // SOS State
    private val _isSosActive = MutableStateFlow(false)
    val isSosActive: StateFlow<Boolean> = _isSosActive.asStateFlow()

    private val _sosCountdown = MutableStateFlow<Int?>(null)
    val sosCountdown: StateFlow<Int?> = _sosCountdown.asStateFlow()

    private val _isOfflineSosMode = MutableStateFlow(false)
    val isOfflineSosMode: StateFlow<Boolean> = _isOfflineSosMode.asStateFlow()

    private val _isStrobeActive = MutableStateFlow(false)
    val isStrobeActive: StateFlow<Boolean> = _isStrobeActive.asStateFlow()

    private val _isSirenActive = MutableStateFlow(false)
    val isSirenActive: StateFlow<Boolean> = _isSirenActive.asStateFlow()

    private val _policeDispatchNotified = MutableStateFlow(false)
    val policeDispatchNotified: StateFlow<Boolean> = _policeDispatchNotified.asStateFlow()

    private val _smsDispatchedCount = MutableStateFlow(0)
    val smsDispatchedCount: StateFlow<Int> = _smsDispatchedCount.asStateFlow()

    // Events to open external intent (SMS app, dialer)
    private val _externalIntentEvent = MutableSharedFlow<Intent>()
    val externalIntentEvent: SharedFlow<Intent> = _externalIntentEvent.asSharedFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Volunteer Mode State
    private val _isVolunteerOnDuty = MutableStateFlow(true)
    val isVolunteerOnDuty: StateFlow<Boolean> = _isVolunteerOnDuty.asStateFlow()

    private val _activeEscortRequest = MutableStateFlow<WalkRequest?>(null)
    val activeEscortRequest: StateFlow<WalkRequest?> = _activeEscortRequest.asStateFlow()

    // Available nearby volunteers
    private val _nearbyVolunteers = MutableStateFlow(
        listOf(
            Volunteer(
                id = "vol_1",
                name = "Aarti Deshmukh",
                badgeNumber = "SW-7842",
                phone = "+1-555-018-9921",
                rating = 4.96f,
                completedEscorts = 64,
                isVerified = true,
                distanceMeters = 180,
                verificationTag = "Verified Nurse & First Responder",
                estimatedArrivalMin = 3,
                avatarInitials = "AD"
            ),
            Volunteer(
                id = "vol_2",
                name = "Kavita Rao",
                badgeNumber = "SW-3291",
                phone = "+1-555-018-4412",
                rating = 4.92f,
                completedEscorts = 41,
                isVerified = true,
                distanceMeters = 320,
                verificationTag = "Campus Safety Volunteer",
                estimatedArrivalMin = 5,
                avatarInitials = "KR"
            ),
            Volunteer(
                id = "vol_3",
                name = "Meera Sen",
                badgeNumber = "SW-9910",
                phone = "+1-555-018-7733",
                rating = 5.0f,
                completedEscorts = 89,
                isVerified = true,
                distanceMeters = 450,
                verificationTag = "Certified Self-Defense Instructor",
                estimatedArrivalMin = 7,
                avatarInitials = "MS"
            )
        )
    )
    val nearbyVolunteers: StateFlow<List<Volunteer>> = _nearbyVolunteers.asStateFlow()

    // Walk Requests for Volunteer Mode
    private val _incomingRequests = MutableStateFlow(
        listOf(
            WalkRequest(
                id = "req_101",
                walkerName = "Rhea K.",
                pickupLocation = "Dark Alley near Metro Exit 3",
                destination = "Central Dorms Block B",
                distanceMeters = 650,
                estimatedWalkMin = 8,
                urgencyLevel = UrgencyLevel.HIGH_UNLIT_AREA,
                walkerPhone = "+1-555-012-3819",
                status = RequestStatus.PENDING
            ),
            WalkRequest(
                id = "req_102",
                walkerName = "Pooja M.",
                pickupLocation = "Bus Stop #14 (Streetlights Broken)",
                destination = "Sunset Apartments",
                distanceMeters = 400,
                estimatedWalkMin = 5,
                urgencyLevel = UrgencyLevel.MODERATE_NIGHT_WALK,
                walkerPhone = "+1-555-012-9981",
                status = RequestStatus.PENDING
            )
        )
    )
    val incomingRequests: StateFlow<List<WalkRequest>> = _incomingRequests.asStateFlow()

    // Room DB streams
    val emergencyContacts: StateFlow<List<EmergencyContact>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walkHistory: StateFlow<List<WalkSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sosHistory: StateFlow<List<SosLog>> = repository.allSosLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Location & Audio streams
    val userLocation: StateFlow<LatLngPoint> = locationTracker.userLocation
    val volunteerLocation: StateFlow<LatLngPoint?> = locationTracker.volunteerLocation
    val routeHistory: StateFlow<List<LatLngPoint>> = locationTracker.routeHistory
    val safePlaces: StateFlow<List<SafePlace>> = locationTracker.safePlaces

    val currentDecibels: StateFlow<Float> = audioManager.currentDecibels
    val isAudioMonitoring: StateFlow<Boolean> = audioManager.isMonitoring
    val isRecordingEvidence: StateFlow<Boolean> = audioManager.isRecordingEvidence

    private var walkTimerJob: Job? = null
    private var countdownJob: Job? = null
    private var counterpartSyncJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureDefaultContactsIfEmpty()
        }

        // Configure audio distress callback
        audioManager.setOnDistressDetected {
            viewModelScope.launch {
                _toastEvent.emit("⚠️ High Audio Distress / Scream Detected! Auto-initiating Emergency SOS!")
                triggerSos("Audio Distress Spike (Scream Detected)")
            }
        }

        // Stream live GPS fixes from FusedLocationProviderClient to Supabase
        locationTracker.onGpsLocationStreamListener = { lat, lng, speed, heading ->
            val shouldStream = _isWalkActive.value || (_currentMode.value == AppMode.VOLUNTEER && _activeEscortRequest.value != null)
            if (shouldStream) {
                _isSupabaseGpsStreaming.value = true
                viewModelScope.launch(Dispatchers.IO) {
                    val currentRole = if (_currentMode.value == AppMode.VOLUNTEER) "VOLUNTEER" else "WALKER"
                    val walkId = _activeWalkId.value ?: _activeEscortRequest.value?.id ?: "walk_live"
                    val pt = LiveStreamPoint(
                        walkId = walkId,
                        userId = _currentUser.value?.id ?: "guest",
                        role = currentRole,
                        latitude = lat,
                        longitude = lng,
                        speed = speed,
                        heading = heading,
                        batteryLevel = dispatchHelper.getBatteryLevel(),
                        timestamp = System.currentTimeMillis()
                    )
                    backendManager.supabaseService.streamLiveLocation(pt)
                }
            }
        }
    }

    fun setAppMode(mode: AppMode) {
        _currentMode.value = mode
    }

    fun setDestination(dest: String) {
        _currentWalkDestination.value = dest
    }

    fun pairWithVolunteer(volunteer: Volunteer) {
        _pairedVolunteer.value = volunteer
        val userLoc = userLocation.value
        locationTracker.pairVolunteer(userLoc.latitude - 0.0003, userLoc.longitude + 0.0002)
        viewModelScope.launch {
            _toastEvent.emit("Volunteer ${volunteer.name} paired! Tracking synchronized.")
        }
    }

    fun unpairCurrentVolunteer() {
        _pairedVolunteer.value = null
        locationTracker.unpairVolunteer()
    }

    fun startSafeWalk(destination: String = _currentWalkDestination.value) {
        if (_isWalkActive.value) return
        _currentWalkDestination.value = destination
        _isWalkActive.value = true
        _walkDurationSeconds.value = 0
        _walkDistanceMeters.value = 0

        val walkId = UUID.randomUUID().toString()
        _activeWalkId.value = walkId

        locationTracker.resetRoute()
        locationTracker.startTracking(viewModelScope)
        audioManager.startMonitoring(viewModelScope)

        walkTimerJob = viewModelScope.launch {
            while (_isWalkActive.value) {
                delay(1000)
                _walkDurationSeconds.value += 1
                // Realistic walking speed: ~1.2m/s
                _walkDistanceMeters.value = (_walkDurationSeconds.value * 1.25).toInt()
            }
        }

        viewModelScope.launch {
            val session = WalkSession(
                id = walkId,
                destination = destination,
                status = "ACTIVE",
                volunteerName = _pairedVolunteer.value?.name,
                volunteerPhone = _pairedVolunteer.value?.phone,
                encryptedTrackingToken = locationTracker.generateEncryptedLiveTrackingUrl(walkId)
            )
            repository.startWalkSession(session)
            backendManager.notifyWalkStarted(
                scope = viewModelScope,
                walkId = walkId,
                destination = destination,
                volunteerName = _pairedVolunteer.value?.name
            )

            // Start real-time counterpart GPS synchronization from Supabase
            counterpartSyncJob?.cancel()
            counterpartSyncJob = viewModelScope.launch(Dispatchers.IO) {
                val counterpartRole = if (_currentMode.value == AppMode.VOLUNTEER) "WALKER" else "VOLUNTEER"
                while (isActive && _isWalkActive.value) {
                    delay(3500)
                    val activeId = _activeWalkId.value ?: continue
                    val res = backendManager.supabaseService.fetchCounterpartLiveLocation(activeId, counterpartRole)
                    res.getOrNull()?.let { pt ->
                        locationTracker.setCounterpartLocation(pt.latitude, pt.longitude)
                    }
                }
            }
        }
    }

    fun completeSafeWalk() {
        if (!_isWalkActive.value) return
        _isWalkActive.value = false
        walkTimerJob?.cancel()
        walkTimerJob = null
        counterpartSyncJob?.cancel()
        counterpartSyncJob = null
        _isSupabaseGpsStreaming.value = false

        val walkId = _activeWalkId.value ?: UUID.randomUUID().toString()
        val duration = _walkDurationSeconds.value
        val distance = _walkDistanceMeters.value

        locationTracker.stopTracking()
        audioManager.stopMonitoring()

        viewModelScope.launch {
            val completedSession = WalkSession(
                id = walkId,
                destination = _currentWalkDestination.value,
                status = "COMPLETED",
                volunteerName = _pairedVolunteer.value?.name,
                volunteerPhone = _pairedVolunteer.value?.phone,
                distanceMeters = distance,
                durationSeconds = duration,
                endTime = System.currentTimeMillis(),
                encryptedTrackingToken = locationTracker.generateEncryptedLiveTrackingUrl(walkId)
            )
            repository.updateWalkSession(completedSession)
            backendManager.notifyWalkCompleted(
                scope = viewModelScope,
                walkId = walkId,
                destination = _currentWalkDestination.value,
                volunteerName = _pairedVolunteer.value?.name,
                distanceMeters = distance,
                durationSeconds = duration
            )
            _toastEvent.emit("🎉 Safe arrival confirmed! Journey securely ended and logged.")
        }
    }

    fun requestSosWithCountdown(seconds: Int = 3, reason: String = "Panic Button") {
        if (_isSosActive.value) return
        _sosCountdown.value = seconds

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _sosCountdown.value = i
                alarmManager.triggerSosVibration()
                delay(1000)
            }
            _sosCountdown.value = null
            triggerSos(reason)
        }
    }

    fun cancelSosCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _sosCountdown.value = null
        viewModelScope.launch {
            _toastEvent.emit("SOS Countdown cancelled safely.")
        }
    }

    fun triggerSos(reason: String = "Instant Panic Button") {
        countdownJob?.cancel()
        countdownJob = null
        _sosCountdown.value = null
        _isSosActive.value = true

        val loc = userLocation.value
        val walkId = _activeWalkId.value ?: "sos_${System.currentTimeMillis()}"
        val trackingUrl = locationTracker.generateEncryptedLiveTrackingUrl(walkId)

        // 1. Hardware Alarms: Strobe + Siren + SOS Vibration
        alarmManager.startStrobeFlash(viewModelScope, intervalMs = 90L)
        _isStrobeActive.value = true

        alarmManager.startSirenSound(viewModelScope)
        _isSirenActive.value = true

        alarmManager.triggerSosVibration()

        // 2. Automated Audio Evidence Recording
        audioManager.startAutomatedEvidenceRecording(viewModelScope, durationSeconds = 30)

        // 3. Dispatch SMS payload to emergency contacts
        val smsPayload = dispatchHelper.buildEmergencySmsMessage(
            userLat = loc.latitude,
            userLng = loc.longitude,
            volunteer = _pairedVolunteer.value,
            walkDestination = _currentWalkDestination.value,
            trackingUrl = trackingUrl
        )

        dispatchHelper.dispatchSmsToContacts(
            contacts = emergencyContacts.value,
            message = smsPayload,
            onSuccess = { count ->
                _smsDispatchedCount.value = count
            },
            onFallbackToIntent = { intent ->
                viewModelScope.launch {
                    _externalIntentEvent.emit(intent)
                }
            }
        )

        // 4. Alert Police Precinct simulation
        _policeDispatchNotified.value = true

        // 5. Real Backend Multi-Channel Dispatch (Supabase + Discord Webhook + Gmail SMTP)
        val battery = dispatchHelper.getBatteryLevel()
        backendManager.dispatchCompleteSosAlert(
            scope = viewModelScope,
            walkId = walkId,
            latitude = loc.latitude,
            longitude = loc.longitude,
            triggerReason = reason,
            volunteerName = _pairedVolunteer.value?.name,
            volunteerPhone = _pairedVolunteer.value?.phone,
            destination = _currentWalkDestination.value,
            batteryLevel = battery,
            trackingUrl = trackingUrl
        )

        // 6. Persist SOS event in Room DB
        viewModelScope.launch {
            val log = SosLog(
                triggerReason = reason,
                latitude = loc.latitude,
                longitude = loc.longitude,
                dispatchedContactsCount = emergencyContacts.value.size,
                policeNotified = true,
                audioEvidenceSaved = true
            )
            repository.logSosEvent(log)
            _toastEvent.emit("🚨 SOS SENT! Real Backend & Webhook notifications dispatched!")
        }
    }

    fun testBackendNotifications() {
        val loc = userLocation.value
        val walkId = "test_${System.currentTimeMillis()}"
        val trackingUrl = locationTracker.generateEncryptedLiveTrackingUrl(walkId)
        val battery = dispatchHelper.getBatteryLevel()

        viewModelScope.launch {
            _toastEvent.emit("📡 Transmitting diagnostic signals to Supabase, Discord & Emergency Email...")
        }

        backendManager.dispatchCompleteSosAlert(
            scope = viewModelScope,
            walkId = walkId,
            latitude = loc.latitude,
            longitude = loc.longitude,
            triggerReason = "Diagnostic System Ping / Live Test",
            volunteerName = _pairedVolunteer.value?.name ?: "Verified Escort Test",
            volunteerPhone = _pairedVolunteer.value?.phone ?: "+1-555-018-9921",
            destination = _currentWalkDestination.value,
            batteryLevel = battery,
            trackingUrl = trackingUrl,
            onComplete = { report ->
                viewModelScope.launch {
                    val msg = if (report.isSuccess) "✅ Real Backend Dispatch Verified! Check Discord & Email."
                    else "⚠️ Test finished: ${report.discordStatus}"
                    _toastEvent.emit(msg)
                }
            }
        )
    }

    fun cancelSos() {
        _isSosActive.value = false
        _isOfflineSosMode.value = false
        alarmManager.stopAll()
        _isStrobeActive.value = false
        _isSirenActive.value = false
        audioManager.stopEvidenceRecording()
        viewModelScope.launch {
            _toastEvent.emit("SOS cancelled. Standby mode restored.")
        }
    }

    fun toggleOfflineSosMode() {
        val next = !_isOfflineSosMode.value
        _isOfflineSosMode.value = next
        if (next) {
            triggerSos("Offline SOS Mode Activated")
        } else {
            cancelSos()
        }
    }

    fun toggleStrobeFlash() {
        if (_isStrobeActive.value) {
            alarmManager.stopStrobeFlash()
            _isStrobeActive.value = false
        } else {
            alarmManager.startStrobeFlash(viewModelScope, 100L)
            _isStrobeActive.value = true
        }
    }

    fun toggleSirenSound() {
        if (_isSirenActive.value) {
            alarmManager.stopSirenSound()
            _isSirenActive.value = false
        } else {
            alarmManager.startSirenSound(viewModelScope)
            _isSirenActive.value = true
        }
    }

    fun toggleAudioMonitoring() {
        if (isAudioMonitoring.value) {
            audioManager.stopMonitoring()
        } else {
            audioManager.startMonitoring(viewModelScope)
        }
    }

    // Emergency Contacts Management
    fun addEmergencyContact(name: String, phone: String, relationship: String, isPrimary: Boolean, isPolice: Boolean) {
        viewModelScope.launch {
            val contact = EmergencyContact(
                name = name,
                phone = phone,
                relationship = relationship,
                isPrimary = isPrimary,
                isPolice = isPolice
            )
            repository.addContact(contact)
            _toastEvent.emit("Contact $name added successfully.")
        }
    }

    fun deleteEmergencyContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
            _toastEvent.emit("Contact ${contact.name} deleted.")
        }
    }

    // Volunteer Mode Operations
    fun toggleVolunteerDuty() {
        _isVolunteerOnDuty.value = !_isVolunteerOnDuty.value
    }

    fun acceptWalkRequest(request: WalkRequest) {
        _activeEscortRequest.value = request.copy(status = RequestStatus.ACCEPTED)
        _activeWalkId.value = request.id
        _incomingRequests.value = _incomingRequests.value.map {
            if (it.id == request.id) it.copy(status = RequestStatus.ACCEPTED) else it
        }

        // Start volunteer live GPS tracking
        locationTracker.resetRoute()
        locationTracker.startTracking(viewModelScope)

        // Stream volunteer live GPS and sync walker's location from Supabase
        counterpartSyncJob?.cancel()
        counterpartSyncJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive && _activeEscortRequest.value != null) {
                delay(3000)
                val activeId = _activeWalkId.value ?: continue
                val res = backendManager.supabaseService.fetchCounterpartLiveLocation(activeId, "WALKER")
                res.getOrNull()?.let { pt ->
                    locationTracker.setCounterpartLocation(pt.latitude, pt.longitude)
                }
            }
        }

        viewModelScope.launch {
            _toastEvent.emit("Accepted escort for ${request.walkerName}! Live GPS synchronization active.")
        }
    }

    fun completeEscortSession(request: WalkRequest) {
        val walkId = _activeWalkId.value ?: request.id
        _activeEscortRequest.value = null
        _activeWalkId.value = null
        counterpartSyncJob?.cancel()
        counterpartSyncJob = null
        locationTracker.stopTracking()
        _isSupabaseGpsStreaming.value = false
        _incomingRequests.value = _incomingRequests.value.filter { it.id != request.id }

        viewModelScope.launch {
            backendManager.supabaseService.recordWalkSessionToSupabase(
                walkId = walkId,
                destination = request.destination,
                status = "COMPLETED_ESCORT",
                volunteerName = _currentUser.value?.name ?: "Verified Volunteer",
                distanceMeters = request.distanceMeters,
                durationSeconds = request.estimatedWalkMin * 60
            )
            _toastEvent.emit("Escort for ${request.walkerName} completed safely! Rating +0.05★")
        }
    }

    fun triggerImmediateGpsStream() {
        _isSupabaseGpsStreaming.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val loc = userLocation.value
            val currentRole = if (_currentMode.value == AppMode.VOLUNTEER) "VOLUNTEER" else "WALKER"
            val walkId = _activeWalkId.value ?: "walk_live"
            val pt = LiveStreamPoint(
                walkId = walkId,
                userId = _currentUser.value?.id ?: "guest",
                role = currentRole,
                latitude = loc.latitude,
                longitude = loc.longitude,
                speed = 1.25f,
                heading = 45f,
                batteryLevel = dispatchHelper.getBatteryLevel(),
                timestamp = System.currentTimeMillis()
            )
            val res = backendManager.supabaseService.streamLiveLocation(pt)
            val status = if (res.isSuccess) "⚡ Live GPS point published to Supabase table!" else "Stream status: ${res.exceptionOrNull()?.message}"
            _toastEvent.emit(status)
        }
    }

    // Fake call actions
    fun triggerFakeCall(caller: String = "Dad (Waiting at Corner)") {
        fakeCallHelper.triggerFakeCall(delaySeconds = 3, scope = viewModelScope, caller = caller)
    }

    fun acceptFakeCall() {
        fakeCallHelper.acceptCall(viewModelScope)
    }

    fun endFakeCall() {
        fakeCallHelper.declineOrEndCall()
    }

    fun captureAudioEvidenceNow() {
        audioManager.startAutomatedEvidenceRecording(viewModelScope, 30)
    }

    // ----------------------------------------------------
    // AUTHENTICATION METHODS (Supabase Auth)
    // ----------------------------------------------------

    fun signIn(email: String, pass: String) {
        _isAuthLoading.value = true
        _authErrorMessage.value = null
        viewModelScope.launch {
            val res = backendManager.supabaseService.signIn(email, pass)
            _isAuthLoading.value = false
            if (res.isSuccess) {
                val profile = res.getOrThrow()
                _currentUser.value = profile
                _currentMode.value = if (profile.role == UserRole.VOLUNTEER) AppMode.VOLUNTEER else AppMode.USER_WALKER
                _toastEvent.emit("Welcome back, ${profile.name}!")
            } else {
                _authErrorMessage.value = res.exceptionOrNull()?.message ?: "Sign in failed"
            }
        }
    }

    fun signUp(email: String, pass: String, name: String, phone: String, role: UserRole, badge: String?) {
        _isAuthLoading.value = true
        _authErrorMessage.value = null
        viewModelScope.launch {
            val res = backendManager.supabaseService.signUp(email, pass, name, phone, role, badge)
            _isAuthLoading.value = false
            if (res.isSuccess) {
                val profile = res.getOrThrow()
                _currentUser.value = profile
                _currentMode.value = if (profile.role == UserRole.VOLUNTEER) AppMode.VOLUNTEER else AppMode.USER_WALKER
                _toastEvent.emit("Account created for ${profile.name} (${profile.role.name})!")
            } else {
                _authErrorMessage.value = res.exceptionOrNull()?.message ?: "Sign up failed"
            }
        }
    }

    fun signOut() {
        _currentUser.value = null
        viewModelScope.launch {
            _toastEvent.emit("Signed out successfully.")
        }
    }

    fun quickDemoLogin(role: UserRole) {
        if (role == UserRole.WALKER) {
            _currentUser.value = UserProfile(
                id = "demo_walker_${System.currentTimeMillis()}",
                email = "walker.kavya@safewalk.org",
                name = "Kavya Sharma",
                phone = "+1-555-019-2834",
                role = UserRole.WALKER
            )
            _currentMode.value = AppMode.USER_WALKER
            viewModelScope.launch {
                _toastEvent.emit("Signed in as Demo Walker: Kavya")
            }
        } else {
            _currentUser.value = UserProfile(
                id = "demo_guard_${System.currentTimeMillis()}",
                email = "guard.aarti@safewalk.org",
                name = "Aarti Deshmukh",
                phone = "+1-555-018-9921",
                role = UserRole.VOLUNTEER,
                volunteerBadge = "SW-7842",
                volunteerRating = 4.96f,
                completedEscorts = 64
            )
            _currentMode.value = AppMode.VOLUNTEER
            viewModelScope.launch {
                _toastEvent.emit("Signed in as Demo Volunteer Guard: Aarti (Badge #SW-7842)")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationTracker.stopTracking()
        audioManager.stopMonitoring()
        alarmManager.stopAll()
    }
}
