package com.elendheim.chords.model

/**
 * Notes are identified by their MIDI number (60 = C4, 69 = A4).
 * Labels use the sharp spelling: C, C#, D, D#, E, F, F#, G, G#, A, A#, B.
 */
object Note {

    val PITCH_CLASSES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    fun label(midi: Int): String {
        val pc = PITCH_CLASSES[((midi % 12) + 12) % 12]
        val octave = midi / 12 - 1
        return "$pc$octave"
    }

    fun frequency(midi: Int): Double = 440.0 * Math.pow(2.0, (midi - 69) / 12.0)

    fun isBlack(midi: Int): Boolean = when (((midi % 12) + 12) % 12) {
        1, 3, 6, 8, 10 -> true
        else -> false
    }

    /** Formats a set of notes as the chord label, e.g. "A4+C5+E5". */
    fun chordLabel(midis: Collection<Int>): String =
        midis.sorted().joinToString("+") { label(it) }
}
