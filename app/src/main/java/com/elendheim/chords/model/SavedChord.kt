package com.elendheim.chords.model

data class SavedChord(
    val id: String,
    val name: String,
    val notes: List<Int>,
    val createdAt: Long
) {
    val notesLabel: String get() = Note.chordLabel(notes)
}
