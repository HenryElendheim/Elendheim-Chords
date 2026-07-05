package com.elendheim.chords

import com.elendheim.chords.audio.ChordSynth
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SynthTest {

    @Test
    fun rendersFullDurationAtSaneAmplitude() {
        val synth = ChordSynth()
        val pcm = synth.render(listOf(69, 72, 76), durationSeconds = 0.5)
        assertEquals((ChordSynth.SAMPLE_RATE * 0.5).toInt(), pcm.size)

        val peak = pcm.maxOf { abs(it.toInt()) }
        assertTrue("expected a healthy signal, peak was $peak", peak > 20000)
        assertTrue("expected headroom below clipping, peak was $peak", peak <= 28000)

        // Envelope must start and end near silence so playback never clicks.
        assertTrue(abs(pcm.first().toInt()) < 500)
        assertTrue(abs(pcm.last().toInt()) < 500)
    }

    @Test
    fun singleNoteRenderWorks() {
        val pcm = ChordSynth().render(listOf(60), durationSeconds = 0.25)
        assertTrue(pcm.any { it.toInt() != 0 })
    }
}
