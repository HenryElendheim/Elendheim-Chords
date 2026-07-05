package com.elendheim.chords.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.elendheim.chords.model.Note
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * A tiny additive synth. Renders a warm, piano-ish pluck for one or more
 * notes and plays it through an AudioTrack. No audio assets required.
 */
class ChordSynth {

    private var current: AudioTrack? = null

    /** Renders PCM off the main thread, then plays it. Safe to call repeatedly. */
    fun render(midis: List<Int>, durationSeconds: Double = 1.7): ShortArray {
        val totalSamples = (SAMPLE_RATE * durationSeconds).toInt()
        val mix = DoubleArray(totalSamples)

        for (midi in midis) {
            val freq = Note.frequency(midi)
            for (h in HARMONICS.indices) {
                val hFreq = freq * (h + 1)
                if (hFreq > SAMPLE_RATE / 2.0) break
                val amp = HARMONICS[h]
                val omega = 2.0 * PI * hFreq / SAMPLE_RATE
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    mix[i] += amp * envelope(t, durationSeconds) * sin(omega * i)
                }
            }
        }

        var peak = 1e-9
        for (v in mix) {
            val a = if (v < 0) -v else v
            if (a > peak) peak = a
        }
        val scale = 0.82 * Short.MAX_VALUE / peak
        return ShortArray(totalSamples) { i ->
            (mix[i] * scale).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    /** Attack, gentle decay, and a fade-out so the tail never clicks. */
    private fun envelope(t: Double, duration: Double): Double {
        val attack = min(t / ATTACK_SECONDS, 1.0)
        val decay = exp(-2.6 * t)
        val remaining = duration - t
        val release = min(remaining / RELEASE_SECONDS, 1.0).coerceAtLeast(0.0)
        return attack * (0.25 + 0.75 * decay) * release
    }

    @Synchronized
    fun play(pcm: ShortArray) {
        stop()
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        track.write(pcm, 0, pcm.size)
        track.play()
        current = track
    }

    @Synchronized
    fun stop() {
        current?.let { track ->
            try {
                track.stop()
            } catch (e: IllegalStateException) {
                // Track was never started; nothing to stop.
            }
            track.release()
        }
        current = null
    }

    companion object {
        const val SAMPLE_RATE = 44100
        private const val ATTACK_SECONDS = 0.012
        private const val RELEASE_SECONDS = 0.20
        private val HARMONICS = doubleArrayOf(1.0, 0.42, 0.20, 0.10, 0.05)
    }
}
