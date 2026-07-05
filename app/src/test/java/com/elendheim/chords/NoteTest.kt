package com.elendheim.chords

import com.elendheim.chords.model.Note
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteTest {

    @Test
    fun labelsMatchNoteNameFormat() {
        assertEquals("C4", Note.label(60))
        assertEquals("A4", Note.label(69))
        assertEquals("C#4", Note.label(61))
        assertEquals("B3", Note.label(59))
        assertEquals("C2", Note.label(36))
        assertEquals("C7", Note.label(96))
    }

    @Test
    fun chordLabelSortsLowToHigh() {
        // The example chorus pad from the app brief: A4+C5+E5.
        val chord = setOf(76, 69, 72) // E5, A4, C5 out of order
        assertEquals("A4+C5+E5", Note.chordLabel(chord))
    }

    @Test
    fun a4Is440Hz() {
        assertTrue(abs(Note.frequency(69) - 440.0) < 1e-9)
        assertTrue(abs(Note.frequency(57) - 220.0) < 1e-9)
        assertTrue(abs(Note.frequency(60) - 261.6255653) < 1e-3)
    }

    @Test
    fun blackKeysAreTheFiveAccidentals() {
        val blacksInOctave4 = (60..71).filter { Note.isBlack(it) }.map { Note.label(it) }
        assertEquals(listOf("C#4", "D#4", "F#4", "G#4", "A#4"), blacksInOctave4)
    }
}
