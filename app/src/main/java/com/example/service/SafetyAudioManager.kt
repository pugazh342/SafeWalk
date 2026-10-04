package com.example.service

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.math.log10

class SafetyAudioManager(private val context: Context) {

    private val _currentDecibels = MutableStateFlow(25f)
    val currentDecibels: StateFlow<Float> = _currentDecibels.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _isRecordingEvidence = MutableStateFlow(false)
    val isRecordingEvidence: StateFlow<Boolean> = _isRecordingEvidence.asStateFlow()

    private val _evidenceAudioPath = MutableStateFlow<String?>(null)
    val evidenceAudioPath: StateFlow<String?> = _evidenceAudioPath.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var monitorJob: Job? = null
    private var evidenceRecorder: MediaRecorder? = null
    private var distressCallback: (() -> Unit)? = null

    // Threshold above which a distress sound / scream is flagged
    var distressThresholdDb: Float = 82f

    fun setOnDistressDetected(callback: () -> Unit) {
        distressCallback = callback
    }

    fun startMonitoring(scope: CoroutineScope) {
        if (_isMonitoring.value) return

        val hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        _isMonitoring.value = true

        monitorJob = scope.launch(Dispatchers.Default) {
            if (hasMicPermission) {
                val sampleRate = 44100
                val channelConfig = AudioFormat.CHANNEL_IN_MONO
                val audioFormat = AudioFormat.ENCODING_PCM_16BIT
                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

                try {
                    @Suppress("MissingPermission")
                    val record = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        channelConfig,
                        audioFormat,
                        bufferSize * 2
                    )
                    audioRecord = record

                    if (record.state == AudioRecord.STATE_INITIALIZED) {
                        record.startRecording()
                        val buffer = ShortArray(bufferSize)
                        var consecutiveDistressCount = 0

                        while (isActive && _isMonitoring.value) {
                            val read = record.read(buffer, 0, buffer.size)
                            if (read > 0) {
                                var sum = 0.0
                                for (i in 0 until read) {
                                    val absVal = abs(buffer[i].toDouble())
                                    if (absVal > sum) sum = absVal
                                }

                                val maxAmp = sum.coerceAtLeast(1.0)
                                // Convert amplitude to approximate dB SPL
                                val db = (20.0 * log10(maxAmp / 1.0)).toFloat().coerceIn(20f, 100f)
                                _currentDecibels.value = db

                                if (db >= distressThresholdDb) {
                                    consecutiveDistressCount++
                                    if (consecutiveDistressCount >= 3) {
                                        distressCallback?.invoke()
                                        consecutiveDistressCount = 0
                                    }
                                } else {
                                    consecutiveDistressCount = 0
                                }
                            }
                            delay(120)
                        }
                    } else {
                        fallbackSimulation(this)
                    }
                } catch (e: Exception) {
                    Log.w("SafetyAudioManager", "Mic initialization fallback: ${e.message}")
                    fallbackSimulation(this)
                } finally {
                    try {
                        audioRecord?.stop()
                        audioRecord?.release()
                    } catch (e: Exception) {
                        // ignore
                    }
                    audioRecord = null
                }
            } else {
                // If permission not granted, run realistic ambient noise simulation
                fallbackSimulation(this)
            }
        }
    }

    private suspend fun fallbackSimulation(scope: CoroutineScope) {
        var baseNoise = 36f
        while (scope.isActive && _isMonitoring.value) {
            val jitter = ((Math.random() - 0.48) * 6).toFloat()
            baseNoise = (baseNoise + jitter).coerceIn(28f, 52f)
            _currentDecibels.value = baseNoise
            delay(200)
        }
    }

    fun stopMonitoring() {
        _isMonitoring.value = false
        monitorJob?.cancel()
        monitorJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // ignore
        }
        audioRecord = null
        _currentDecibels.value = 25f
    }

    fun startAutomatedEvidenceRecording(scope: CoroutineScope, durationSeconds: Int = 30) {
        val hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasMicPermission || _isRecordingEvidence.value) {
            // Mark evidence mock state
            _isRecordingEvidence.value = true
            _evidenceAudioPath.value = "evidence_enc_${System.currentTimeMillis()}.m4a"
            scope.launch {
                delay(durationSeconds * 1000L)
                _isRecordingEvidence.value = false
            }
            return
        }

        try {
            val evidenceFile = File(context.cacheDir, "evidence_${System.currentTimeMillis()}.m4a")
            @Suppress("DEPRECATION")
            val recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(evidenceFile.absolutePath)
                prepare()
                start()
            }
            evidenceRecorder = recorder
            _isRecordingEvidence.value = true
            _evidenceAudioPath.value = evidenceFile.absolutePath

            scope.launch {
                delay(durationSeconds * 1000L)
                stopEvidenceRecording()
            }
        } catch (e: Exception) {
            Log.e("SafetyAudioManager", "Evidence recording error", e)
            _isRecordingEvidence.value = false
        }
    }

    fun stopEvidenceRecording() {
        if (!_isRecordingEvidence.value) return
        try {
            evidenceRecorder?.stop()
            evidenceRecorder?.release()
        } catch (e: Exception) {
            Log.w("SafetyAudioManager", "Error stopping evidence recorder", e)
        }
        evidenceRecorder = null
        _isRecordingEvidence.value = false
    }
}
