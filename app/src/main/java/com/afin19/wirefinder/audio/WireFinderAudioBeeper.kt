package com.afin19.wirefinder.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Metal detector / wire finder acoustic audio synthesizer.
 * Generates dynamic pitch beeps / clicks proportional to detected signal level.
 * Pitch goes from ~450 Hz at threshold up to ~1800 Hz at high proximity,
 * with pulse intervals decreasing from 650ms down to 70ms (Geiger counter / metal detector style).
 */
class WireFinderAudioBeeper(
    private val scope: CoroutineScope
) {
    private val sampleRate = 22050
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null

    @Volatile
    var isEnabled: Boolean = true

    @Volatile
    private var currentLevel: Float = 0f

    @Volatile
    private var isPlaying: Boolean = false

    fun start() {
        if (isPlaying) return
        isPlaying = true

        playbackJob = scope.launch(Dispatchers.Default) {
            try {
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(sampleRate / 10)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
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
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack = track
                track.play()

                while (isActive && isPlaying) {
                    val level = currentLevel
                    if (isEnabled && level >= 25f) {
                        // Level: 25..100%
                        val normalized = ((level - 25f) / 75f).coerceIn(0f, 1f)

                        // Pitch increases from 480 Hz to 1850 Hz
                        val frequency = 480.0 + (normalized * 1370.0)

                        // Duration of click/beep: 20ms at low levels, 35ms at high levels
                        val beepDurationMs = 22 + (normalized * 15).toInt()
                        val pulseIntervalMs = (600.0 - (normalized * 530.0)).toLong().coerceIn(60L, 600L)

                        // Generate short sine wave pulse with smooth envelope (prevents clicking artifacts)
                        val numSamples = (sampleRate * (beepDurationMs / 1000.0)).toInt()
                        val pcmBuffer = ShortArray(numSamples)

                        for (i in 0 until numSamples) {
                            // Hann window envelope to avoid popping
                            val window = 0.5 * (1.0 - kotlin.math.cos(2.0 * Math.PI * i / numSamples))
                            val angle = 2.0 * Math.PI * i / (sampleRate / frequency)
                            val sample = (sin(angle) * window * Short.MAX_VALUE * 0.45).toInt()
                            pcmBuffer[i] = sample.toShort()
                        }

                        track.write(pcmBuffer, 0, pcmBuffer.size)

                        // Pause between pulses according to proximity
                        val pauseMs = (pulseIntervalMs - beepDurationMs).coerceAtLeast(20L)
                        kotlinx.coroutines.delay(pauseMs)
                    } else {
                        // Sleep briefly when no signal detected
                        kotlinx.coroutines.delay(40L)
                    }
                }
            } catch (_: Exception) {
                // Ignore audio track initialization or write errors
            } finally {
                releaseTrack()
            }
        }
    }

    fun updateLevel(level: Float) {
        currentLevel = level
    }

    fun stop() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        releaseTrack()
    }

    private fun releaseTrack() {
        try {
            audioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {
        } finally {
            audioTrack = null
        }
    }
}
