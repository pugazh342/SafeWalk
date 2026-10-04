package com.example.service

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class HardwareAlarmManager(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var cameraId: String? = null
    private var strobeJob: Job? = null
    private var sirenJob: Job? = null
    private var audioTrack: AudioTrack? = null

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id
                    return@forEach
                }
            }
        } catch (e: Exception) {
            Log.e("HardwareAlarmManager", "Error detecting camera flash", e)
        }
    }

    fun startStrobeFlash(scope: CoroutineScope, intervalMs: Long = 100L) {
        val id = cameraId ?: return
        if (strobeJob?.isActive == true) return

        strobeJob = scope.launch(Dispatchers.Default) {
            var flashOn = false
            try {
                while (isActive) {
                    flashOn = !flashOn
                    try {
                        cameraManager?.setTorchMode(id, flashOn)
                    } catch (e: CameraAccessException) {
                        Log.w("HardwareAlarmManager", "Torch mode error", e)
                        break
                    }
                    delay(intervalMs)
                }
            } finally {
                try {
                    cameraManager?.setTorchMode(id, false)
                } catch (e: Exception) {
                    Log.w("HardwareAlarmManager", "Error resetting torch", e)
                }
            }
        }
    }

    fun stopStrobeFlash() {
        strobeJob?.cancel()
        strobeJob = null
        val id = cameraId ?: return
        try {
            cameraManager?.setTorchMode(id, false)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun startSirenSound(scope: CoroutineScope) {
        if (sirenJob?.isActive == true) return

        sirenJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = track
            track.play()

            val buffer = ShortArray(1024)
            var currentFreq = 700.0
            var freqGoingUp = true
            var phase = 0.0

            try {
                while (isActive) {
                    // Sweeping siren tone between 700Hz and 1600Hz
                    if (freqGoingUp) {
                        currentFreq += 18.0
                        if (currentFreq >= 1600.0) freqGoingUp = false
                    } else {
                        currentFreq -= 18.0
                        if (currentFreq <= 700.0) freqGoingUp = true
                    }

                    for (i in buffer.indices) {
                        phase += 2.0 * Math.PI * currentFreq / sampleRate
                        if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                        buffer[i] = (sin(phase) * Short.MAX_VALUE * 0.95).toInt().toShort()
                    }
                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                Log.e("HardwareAlarmManager", "Siren generation error", e)
            } finally {
                try {
                    track.stop()
                    track.release()
                } catch (e: Exception) {
                    // ignore
                }
                audioTrack = null
            }
        }
    }

    fun stopSirenSound() {
        sirenJob?.cancel()
        sirenJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            // ignore
        }
    }

    fun triggerSosVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // SOS Morse pattern: 3 short, 3 long, 3 short (... --- ...)
                val timings = longArrayOf(0, 150, 100, 150, 100, 150, 200, 400, 100, 400, 100, 400, 200, 150, 100, 150, 100, 150)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 200, 100, 200, 100, 200, 300, 500, 100, 500), -1)
            }
        } catch (e: Exception) {
            Log.w("HardwareAlarmManager", "Vibrate error", e)
        }
    }

    fun stopAll() {
        stopStrobeFlash()
        stopSirenSound()
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // ignore
        }
    }
}
