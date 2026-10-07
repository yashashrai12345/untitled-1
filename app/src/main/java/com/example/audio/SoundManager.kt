package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.data.persistence.GamePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val preferences: GamePreferences) {

    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null

    companion object {
        @Volatile
        private var INSTANCE: SoundManager? = null

        fun getInstance(preferences: GamePreferences): SoundManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SoundManager(preferences).also { INSTANCE = it }
            }
        }
    }

    /**
     * Play a soft clean click when tapping an arrow or button.
     */
    fun playTap() {
        if (!preferences.isSoundEnabled) return
        scope.launch {
            val samples = generateTone(freq = 700.0, durationMs = 25, attackMs = 2, decayMs = 20)
            playPcm(samples)
        }
    }

    /**
     * Play a crisp positive ascending chime when an arrow escapes.
     */
    fun playArrowEscape() {
        if (!preferences.isSoundEnabled) return
        scope.launch {
            val part1 = generateTone(freq = 587.33, durationMs = 60, attackMs = 5, decayMs = 40) // D5
            val part2 = generateTone(freq = 880.0, durationMs = 90, attackMs = 5, decayMs = 70)  // A5
            playPcm(part1 + part2)
        }
    }

    /**
     * Play a gentle error buzz when an arrow is blocked.
     */
    fun playBlocked() {
        if (!preferences.isSoundEnabled) return
        scope.launch {
            val samples = generateSawBuzz(freq = 150.0, durationMs = 120)
            playPcm(samples)
        }
    }

    /**
     * Play a descending tone when a heart is lost.
     */
    fun playLifeLost() {
        if (!preferences.isSoundEnabled) return
        scope.launch {
            val part1 = generateTone(freq = 440.0, durationMs = 70, attackMs = 5, decayMs = 50)
            val part2 = generateTone(freq = 329.63, durationMs = 110, attackMs = 5, decayMs = 90)
            playPcm(part1 + part2)
        }
    }

    /**
     * Play a sparkle chime when a hint is requested.
     */
    fun playHint() {
        if (!preferences.isSoundEnabled) return
        scope.launch {
            val n1 = generateTone(freq = 523.25, durationMs = 50, attackMs = 3, decayMs = 35)
            val n2 = generateTone(freq = 659.25, durationMs = 50, attackMs = 3, decayMs = 35)
            val n3 = generateTone(freq = 783.99, durationMs = 90, attackMs = 3, decayMs = 70)
            playPcm(n1 + n2 + n3)
        }
    }

    /**
     * Play triumphant level complete arpeggio.
     */
    fun playLevelComplete() {
        if (!preferences.isSoundEnabled) return
        scope.launch {
            val c5 = generateTone(freq = 523.25, durationMs = 70, attackMs = 5, decayMs = 50)
            val e5 = generateTone(freq = 659.25, durationMs = 70, attackMs = 5, decayMs = 50)
            val g5 = generateTone(freq = 783.99, durationMs = 70, attackMs = 5, decayMs = 50)
            val c6 = generateTone(freq = 1046.50, durationMs = 180, attackMs = 5, decayMs = 150)
            playPcm(c5 + e5 + g5 + c6)
        }
    }

    fun playButtonClick() {
        playTap()
    }

    /**
     * Ambient soothing background chords (Cmaj7 -> Fmaj7 soft pads).
     */
    fun updateMusicState() {
        if (preferences.isMusicEnabled) {
            startAmbientMusic()
        } else {
            stopAmbientMusic()
        }
    }

    private fun startAmbientMusic() {
        if (musicJob?.isActive == true) return
        musicJob = scope.launch {
            val chords = listOf(
                listOf(261.63, 329.63, 392.0, 493.88), // Cmaj7
                listOf(220.0, 261.63, 329.63, 392.0),  // Am7
                listOf(174.61, 220.0, 261.63, 329.63), // Fmaj7
                listOf(196.0, 246.94, 293.66, 392.0)   // G6
            )
            var index = 0
            while (isActive && preferences.isMusicEnabled) {
                val freqs = chords[index % chords.size]
                val chordSamples = generateChord(freqs, durationMs = 2800, volume = 0.08)
                playPcm(chordSamples)
                delay(200)
                index++
            }
        }
    }

    private fun stopAmbientMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    private fun generateTone(
        freq: Double,
        durationMs: Int,
        attackMs: Int = 5,
        decayMs: Int = 20,
        volume: Double = 0.35
    ): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val attackSamples = (sampleRate * attackMs) / 1000
        val decaySamples = (sampleRate * decayMs) / 1000
        val sustainSamples = (totalSamples - attackSamples - decaySamples).coerceAtLeast(0)

        val buffer = ShortArray(totalSamples)
        val twoPiF = 2.0 * Math.PI * freq

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = when {
                i < attackSamples -> i.toDouble() / attackSamples
                i < attackSamples + sustainSamples -> 1.0
                else -> {
                    val decayStep = i - (attackSamples + sustainSamples)
                    (1.0 - (decayStep.toDouble() / decaySamples)).coerceAtLeast(0.0)
                }
            }
            val sample = sin(twoPiF * t) * envelope * volume * Short.MAX_VALUE
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateChord(freqs: List<Double>, durationMs: Int, volume: Double): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val attackSamples = (sampleRate * 400) / 1000
        val decaySamples = (sampleRate * 600) / 1000
        val sustainSamples = (totalSamples - attackSamples - decaySamples).coerceAtLeast(0)

        val buffer = ShortArray(totalSamples)
        val numNotes = freqs.size

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = when {
                i < attackSamples -> i.toDouble() / attackSamples
                i < attackSamples + sustainSamples -> 1.0
                else -> {
                    val decayStep = i - (attackSamples + sustainSamples)
                    (1.0 - (decayStep.toDouble() / decaySamples)).coerceAtLeast(0.0)
                }
            }
            var sum = 0.0
            for (f in freqs) {
                sum += sin(2.0 * Math.PI * f * t)
            }
            val sample = (sum / numNotes) * envelope * volume * Short.MAX_VALUE
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateSawBuzz(freq: Double, durationMs: Int): ShortArray {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)
        val period = sampleRate / freq

        for (i in 0 until totalSamples) {
            val progress = 1.0 - (i.toDouble() / totalSamples)
            val posInPeriod = (i % period) / period
            val saw = (2.0 * posInPeriod - 1.0)
            val sample = saw * progress * 0.3 * Short.MAX_VALUE
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun playPcm(samples: ShortArray) {
        if (samples.isEmpty()) return
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
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            // Release after playback completes
            scope.launch {
                val durationMs = (samples.size * 1000L) / sampleRate
                delay(durationMs + 50)
                audioTrack.stop()
                audioTrack.release()
            }
        } catch (e: Exception) {
            // Graceful ignore
        }
    }
}
