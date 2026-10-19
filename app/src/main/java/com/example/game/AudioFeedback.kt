package com.example.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural low-latency tactical audio synthesizer using native Android AudioTrack.
 * Generates crisp military UI beeps, scanner radar chirps, alarm sweeps, and chimes
 * without requiring external sound files or internet connection.
 */
class AudioFeedback(
    private val scope: CoroutineScope
) {
    private val sampleRate = 22050
    var isEnabled: Boolean = true

    fun playScannerPing() {
        if (!isEnabled) return
        scope.launch(Dispatchers.Default) {
            val durationMs = 180
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = 600f + (t * 800f) // Rising chirp from 600Hz to 1400Hz
                val envelope = if (t < 0.1f) t / 0.1f else (1f - t) // Fade out
                val angle = 2.0 * PI * i * freq / sampleRate
                buffer[i] = (sin(angle) * envelope * 24000).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playInteractBeep() {
        if (!isEnabled) return
        scope.launch(Dispatchers.Default) {
            val durationMs = 120
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = if (t < 0.5f) 880f else 1320f // Tactical double-tone
                val envelope = 1f - t
                val angle = 2.0 * PI * i * freq / sampleRate
                buffer[i] = (sin(angle) * envelope * 22000).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playAlertAlarm() {
        if (!isEnabled) return
        scope.launch(Dispatchers.Default) {
            val durationMs = 260
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = if ((i / 400) % 2 == 0) 520f else 780f // Urgent alternate tone
                val angle = 2.0 * PI * i * freq / sampleRate
                buffer[i] = (sin(angle) * 26000).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    fun playObjectiveComplete() {
        if (!isEnabled) return
        scope.launch(Dispatchers.Default) {
            val durationMs = 380
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            // Triad chord progression: C5 -> E5 -> G5
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = when {
                    t < 0.33f -> 523.25f
                    t < 0.66f -> 659.25f
                    else -> 783.99f
                }
                val envelope = 1f - (t * 0.7f)
                val angle = 2.0 * PI * i * freq / sampleRate
                buffer[i] = (sin(angle) * envelope * 25000).toInt().toShort()
            }
            playPcmBuffer(buffer)
        }
    }

    private fun playPcmBuffer(buffer: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Release after playing
            Thread.sleep((buffer.size * 1000L / sampleRate) + 50)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // AudioTrack failure non-blocking fallback
        }
    }
}
