package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class FakeCallState {
    IDLE,
    RINGING,
    CONNECTED,
    ENDED
}

class FakeCallHelper(private val context: Context) {

    private val _callState = MutableStateFlow(FakeCallState.IDLE)
    val callState: StateFlow<FakeCallState> = _callState.asStateFlow()

    private val _callerName = MutableStateFlow("Brother Alex (Live Tracking)")
    val callerName: StateFlow<String> = _callerName.asStateFlow()

    private val _callDurationSec = MutableStateFlow(0)
    val callDurationSec: StateFlow<Int> = _callDurationSec.asStateFlow()

    private var callTimerJob: Job? = null
    private var simulatedVoiceJob: Job? = null

    fun triggerFakeCall(delaySeconds: Int = 3, scope: CoroutineScope, caller: String = "Dad (Waiting at Corner)") {
        _callerName.value = caller
        scope.launch {
            delay(delaySeconds * 1000L)
            _callState.value = FakeCallState.RINGING
        }
    }

    fun acceptCall(scope: CoroutineScope) {
        _callState.value = FakeCallState.CONNECTED
        _callDurationSec.value = 0

        callTimerJob = scope.launch(Dispatchers.Default) {
            while (isActive && _callState.value == FakeCallState.CONNECTED) {
                delay(1000)
                _callDurationSec.value += 1
            }
        }
    }

    fun declineOrEndCall() {
        _callState.value = FakeCallState.ENDED
        callTimerJob?.cancel()
        callTimerJob = null
        simulatedVoiceJob?.cancel()
        simulatedVoiceJob = null
        Handler(Looper.getMainLooper()).postDelayed({
            _callState.value = FakeCallState.IDLE
        }, 1500)
    }
}
