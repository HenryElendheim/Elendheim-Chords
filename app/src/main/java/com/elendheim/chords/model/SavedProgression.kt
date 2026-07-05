package com.elendheim.chords.model

data class SavedProgression(
    val id: String,
    val name: String,
    val bars: List<List<Int>>,
    val createdAt: Long
)
