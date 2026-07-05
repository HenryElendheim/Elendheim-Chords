package com.elendheim.chords.midi

import java.io.ByteArrayOutputStream

/**
 * Writes a progression as a standard MIDI file (format 0, one track).
 * Each bar becomes one chord held for [BEATS_PER_BAR] beats.
 */
object MidiWriter {

    const val TICKS_PER_BEAT = 480
    const val BEATS_PER_BAR = 2
    const val TEMPO_BPM = 110
    private const val VELOCITY = 96

    fun fromBars(bars: List<List<Int>>): ByteArray {
        val track = ByteArrayOutputStream()

        // Tempo meta event.
        writeVarLen(track, 0)
        track.write(0xFF)
        track.write(0x51)
        track.write(0x03)
        val microsPerBeat = 60_000_000 / TEMPO_BPM
        track.write((microsPerBeat shr 16) and 0xFF)
        track.write((microsPerBeat shr 8) and 0xFF)
        track.write(microsPerBeat and 0xFF)

        val barTicks = TICKS_PER_BEAT * BEATS_PER_BAR
        for (bar in bars) {
            val notes = bar.filter { it in 0..127 }.sorted()
            if (notes.isEmpty()) continue
            for (note in notes) {
                writeVarLen(track, 0)
                track.write(0x90)
                track.write(note)
                track.write(VELOCITY)
            }
            notes.forEachIndexed { i, note ->
                writeVarLen(track, if (i == 0) barTicks else 0)
                track.write(0x80)
                track.write(note)
                track.write(0)
            }
        }

        // End of track.
        writeVarLen(track, 0)
        track.write(0xFF)
        track.write(0x2F)
        track.write(0x00)

        val trackBytes = track.toByteArray()
        val out = ByteArrayOutputStream()
        out.write("MThd".toByteArray(Charsets.US_ASCII))
        writeInt32(out, 6)
        writeInt16(out, 0) // format 0
        writeInt16(out, 1) // one track
        writeInt16(out, TICKS_PER_BEAT)
        out.write("MTrk".toByteArray(Charsets.US_ASCII))
        writeInt32(out, trackBytes.size)
        out.write(trackBytes)
        return out.toByteArray()
    }

    /** Standard MIDI variable-length quantity. */
    private fun writeVarLen(out: ByteArrayOutputStream, value: Int) {
        var buffer = value and 0x7F
        var v = value ushr 7
        while (v > 0) {
            buffer = (buffer shl 8) or 0x80 or (v and 0x7F)
            v = v ushr 7
        }
        while (true) {
            out.write(buffer and 0xFF)
            if (buffer and 0x80 != 0) buffer = buffer ushr 8 else break
        }
    }

    private fun writeInt32(out: ByteArrayOutputStream, value: Int) {
        out.write((value shr 24) and 0xFF)
        out.write((value shr 16) and 0xFF)
        out.write((value shr 8) and 0xFF)
        out.write(value and 0xFF)
    }

    private fun writeInt16(out: ByteArrayOutputStream, value: Int) {
        out.write((value shr 8) and 0xFF)
        out.write(value and 0xFF)
    }
}
