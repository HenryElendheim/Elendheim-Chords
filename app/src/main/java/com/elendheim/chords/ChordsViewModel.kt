package com.elendheim.chords

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elendheim.chords.audio.ChordSynth
import com.elendheim.chords.data.ChordStore
import com.elendheim.chords.midi.MidiWriter
import com.elendheim.chords.model.SavedChord
import com.elendheim.chords.model.SavedProgression
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val _progressionLibrary = MutableStateFlow<List<SavedProgression>>(emptyList())
    val progressionLibrary: StateFlow<List<SavedProgression>> = _progressionLibrary.asStateFlow()

    // Index of the bar currently being edited on the keyboard, if any.
    private val _editingBar = MutableStateFlow<Int?>(null)
    val editingBar: StateFlow<Int?> = _editingBar.asStateFlow()

    private var progressionJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _library.value = store.load()
            _progressionLibrary.value = store.loadProgressions()
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

    /**
     * Appends the current selection to the progression as the next bar, or,
     * while editing an existing bar, replaces that bar in place.
     */
    fun setBar() {
        val notes = _selectedNotes.value.sorted()
        if (notes.isEmpty()) return
        val editing = _editingBar.value
        if (editing != null && editing in _progression.value.indices) {
            _progression.value = _progression.value.mapIndexed { i, bar ->
                if (i == editing) notes else bar
            }
            _editingBar.value = null
        } else {
            _progression.value = _progression.value + listOf(notes)
        }
    }

    /** Loads the bar's notes onto the keyboard so Set becomes Update. */
    fun startEditingBar(index: Int) {
        val bar = _progression.value.getOrNull(index) ?: return
        _selectedNotes.value = bar.toSet()
        _editingBar.value = index
    }

    fun cancelEditingBar() {
        _editingBar.value = null
    }

    fun deleteBar(index: Int) {
        _progression.value = _progression.value.filterIndexed { i, _ -> i != index }
        _editingBar.value = _editingBar.value?.let { editing ->
            when {
                editing == index -> null
                editing > index -> editing - 1
                else -> editing
            }
        }
    }

    fun moveBar(from: Int, to: Int) {
        val bars = _progression.value.toMutableList()
        if (from !in bars.indices || to !in bars.indices || from == to) return
        val bar = bars.removeAt(from)
        bars.add(to, bar)
        _progression.value = bars
        _editingBar.value = _editingBar.value?.let { editing ->
            when {
                editing == from -> to
                from < editing && to >= editing -> editing - 1
                from > editing && to <= editing -> editing + 1
                else -> editing
            }
        }
    }

    fun playBar(index: Int) {
        _progression.value.getOrNull(index)?.let { playNotes(it) }
    }

    /** Plays the current progression bar by bar. */
    fun playProgression() {
        playBarsInSequence(_progression.value)
    }

    fun playSavedProgression(progression: SavedProgression) {
        playBarsInSequence(progression.bars)
    }

    private fun playBarsInSequence(bars: List<List<Int>>) {
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

    /** Saves the current progression under the given name. */
    fun saveProgression(name: String): Boolean {
        val bars = _progression.value
        if (bars.isEmpty()) return false
        val progression = SavedProgression(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifEmpty { "Progression ${_progressionLibrary.value.size + 1}" },
            bars = bars,
            createdAt = System.currentTimeMillis()
        )
        updateProgressionLibrary(listOf(progression) + _progressionLibrary.value)
        return true
    }

    fun deleteProgression(id: String) {
        updateProgressionLibrary(_progressionLibrary.value.filterNot { it.id == id })
    }

    fun loadProgressionIntoBuilder(progression: SavedProgression) {
        _progression.value = progression.bars
        _editingBar.value = null
    }

    private fun updateProgressionLibrary(progressions: List<SavedProgression>) {
        _progressionLibrary.value = progressions
        viewModelScope.launch(Dispatchers.IO) {
            store.saveProgressions(progressions)
        }
    }

    /** Writes the current progression to the given document as a MIDI file. */
    suspend fun exportMidi(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val bars = _progression.value
        if (bars.isEmpty()) return@withContext false
        try {
            val resolver = getApplication<Application>().contentResolver
            resolver.openOutputStream(uri)?.use { stream ->
                stream.write(MidiWriter.fromBars(bars))
                true
            } ?: false
        } catch (e: Exception) {
            false
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
