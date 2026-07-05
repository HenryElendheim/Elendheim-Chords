package com.elendheim.chords

import com.elendheim.chords.midi.MidiWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MidiWriterTest {

    @Test
    fun writesValidFormatZeroFile() {
        val bytes = MidiWriter.fromBars(listOf(listOf(60, 64, 67), listOf(65, 69, 72)))

        assertEquals("MThd", String(bytes, 0, 4, Charsets.US_ASCII))
        assertEquals(6, readInt32(bytes, 4))
        assertEquals(0, readInt16(bytes, 8)) // format 0
        assertEquals(1, readInt16(bytes, 10)) // one track
        assertEquals(MidiWriter.TICKS_PER_BEAT, readInt16(bytes, 12))
        assertEquals("MTrk", String(bytes, 14, 4, Charsets.US_ASCII))
        // Declared track length must match the remaining bytes exactly.
        assertEquals(bytes.size - 22, readInt32(bytes, 18))
        // Ends with the end-of-track meta event.
        assertEquals(0xFF, bytes[bytes.size - 3].toInt() and 0xFF)
        assertEquals(0x2F, bytes[bytes.size - 2].toInt() and 0xFF)
        assertEquals(0x00, bytes[bytes.size - 1].toInt() and 0xFF)
    }

    @Test
    fun eachNoteGetsOnAndOffEvents()  {
        val bytes = MidiWriter.fromBars(listOf(listOf(60, 64, 67), listOf(65, 69, 72)))
        val ons = bytes.count { it.toInt() and 0xFF == 0x90 }
        val offs = bytes.count { it.toInt() and 0xFF == 0x80 }
        // Six notes on; note-off count includes the 0x80 in the bar-length
        // delta time (0x83 0x60 encodes 480), so only assert the ons here
        // and that offs are at least the ons.
        assertEquals(6, ons)
        assertTrue(offs >= 6)
    }

    @Test
    fun skipsEmptyBarsAndOutOfRangeNotes() {
        val bytes = MidiWriter.fromBars(listOf(emptyList(), listOf(200, -3)))
        val ons = bytes.count { it.toInt() and 0xFF == 0x90 }
        assertEquals(0, ons)
    }

    private fun readInt32(b: ByteArray, at: Int): Int =
        ((b[at].toInt() and 0xFF) shl 24) or ((b[at + 1].toInt() and 0xFF) shl 16) or
            ((b[at + 2].toInt() and 0xFF) shl 8) or (b[at + 3].toInt() and 0xFF)

    private fun readInt16(b: ByteArray, at: Int): Int =
        ((b[at].toInt() and 0xFF) shl 8) or (b[at + 1].toInt() and 0xFF)
}
