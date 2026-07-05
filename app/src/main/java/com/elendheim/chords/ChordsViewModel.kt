package com.elendheim.chords

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elendheim.chords.audio.ChordSynth
import com.elendheim.chords.data.ChordStore
import com.elendheim.chords.model.SavedChord
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChordsViewModel(application: Application) : AndroidViewModel(application) {

    private val store = ChordStore(application)
    private val synth = ChordSynth()

    private val _selectedNotes = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNotes: StateFlow<Set<Int>> = _selectedNotes.asStateFlow()

    private val _library = MutableStateFlow<List<SavedChord>>(emptyList())
    val library: StateFlow<List<SavedChord>> = _library.asStateFlow()

    // The progression: each bar is a chord, in the order they were set.
    private val _progression = MutableStateFlow<List<List<Int>>>(emptyList())
    val progression: StateFlow<List<List<Int>>> = _progression.asStateFlow()

    private var progressionJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _library.value = store.load()
        }
    }

    fun toggleNote(midi: Int) {
        val selected = _selectedNotes.value
        if (midi in selected) {
            _selectedNotes.value = selected - midi
        } else {
            _selectedNotes.value = selected + midi
            playNotes(listOf(midi), durationSeconds = 0.7)
        }
    }

    fun removeNote(midi: Int) {
        _selectedNotes.value = _selectedNotes.value - midi
    }

    fun clearSelection() {
        _selectedNotes.value = emptySet()
    }

    fun playSelection() {
        val notes = _selectedNotes.value.sorted()
        if (notes.isNotEmpty()) playNotes(notes)
    }

    fun playChord(chord: SavedChord) {
        if (chord.notes.isNotEmpty()) playNotes(chord.notes.sorted())
    }

    /** Appends the current selection to the progression as the next bar. */
    fun setBar() {
        val notes = _selectedNotes.value.sorted()
        if (notes.isEmpty()) return
        _progression.value = _progression.value + listOf(notes)
    }

    fun deleteBar(index: Int) {
        _progression.value = _progression.value.filterIndexed { i, _ -> i != index }
    }

    fun moveBar(from: Int, to: Int) {
        val bars = _progression.value.toMutableList()
        if (from !in bars.indices || to !in bars.indices || from == to) return
        val bar = bars.removeAt(from)
        bars.add(to, bar)
        _progression.value = bars
    }

    fun playBar(index: Int) {
        _progression.value.getOrNull(index)?.let { playNotes(it) }
    }

    /** Plays the progression bar by bar. */
    fun playProgression() {
        val bars = _progression.value
        if (bars.isEmpty()) return
        progressionJob?.cancel()
        progressionJob = viewModelScope.launch(Dispatchers.Default) {
            for (bar in bars) {
                val pcm = synth.render(bar, durationSeconds = 1.25)
                synth.play(pcm)
                delay(1100)
            }
        }
    }

    private fun playNotes(midis: List<Int>, durationSeconds: Double = 1.7) {
        progressionJob?.cancel()
        viewModelScope.launch(Dispatchers.Default) {
            val pcm = synth.render(midis, durationSeconds)
            synth.play(pcm)
        }
    }

    /** Saves the current selection under the given name. Returns false if there is nothing to save. */
    fun saveSelection(name: String): Boolean {
        val notes = _selectedNotes.value.sorted()
        if (notes.isEmpty()) return false
        val chord = SavedChord(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifEmpty { com.elendheim.chords.model.Note.chordLabel(notes) },
            notes = notes,
            createdAt = System.currentTimeMillis()
        )
        updateLibrary(listOf(chord) + _library.value)
        return true
    }

    fun deleteChord(id: String) {
        updateLibrary(_library.value.filterNot { it.id == id })
    }

    fun loadIntoBuilder(chord: SavedChord) {
        _selectedNotes.value = chord.notes.toSet()
    }

    private fun updateLibrary(chords: List<SavedChord>) {
        _library.value = chords
        viewModelScope.launch(Dispatchers.IO) {
            store.save(chords)
        }
    }

    override fun onCleared() {
        synth.stop()
    }
}
