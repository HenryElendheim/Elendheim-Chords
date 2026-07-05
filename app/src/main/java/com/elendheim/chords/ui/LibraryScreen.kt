package com.elendheim.chords.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elendheim.chords.model.SavedChord
import com.elendheim.chords.model.SavedProgression

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    chords: List<SavedChord>,
    progressions: List<SavedProgression>,
    onPlayChord: (SavedChord) -> Unit,
    onEditChord: (SavedChord) -> Unit,
    onDeleteChord: (SavedChord) -> Unit,
    onPlayProgression: (SavedProgression) -> Unit,
    onEditProgression: (SavedProgression) -> Unit,
    onDeleteProgression: (SavedProgression) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingDeleteChord by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteProgression by rememberSaveable { mutableStateOf<String?>(null) }

    if (chords.isEmpty() && progressions.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Nothing saved yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Build a chord or a progression on the keyboard\nand save it. It will live here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (progressions.isNotEmpty()) {
            item(key = "header-progressions") {
                SectionHeader("Progressions")
            }
            items(progressions, key = { "p-${it.id}" }) { progression ->
                LibraryCard(
                    title = progression.name,
                    detail = if (progression.bars.size == 1) "1 bar" else "${progression.bars.size} bars",
                    onPlay = { onPlayProgression(progression) },
                    onEdit = { onEditProgression(progression) },
                    onDelete = { pendingDeleteProgression = progression.id }
                )
            }
        }
        if (chords.isNotEmpty()) {
            item(key = "header-chords") {
                SectionHeader("Chords")
            }
            items(chords, key = { "c-${it.id}" }) { chord ->
                LibraryCard(
                    title = chord.name,
                    detail = chord.notesLabel,
                    onPlay = { onPlayChord(chord) },
                    onEdit = { onEditChord(chord) },
                    onDelete = { pendingDeleteChord = chord.id }
                )
            }
        }
    }

    val chordToDelete = chords.firstOrNull { it.id == pendingDeleteChord }
    if (chordToDelete != null) {
        DeleteDialog(
            title = "Delete ${chordToDelete.name}?",
            text = "${chordToDelete.notesLabel} will be removed from your library.",
            onConfirm = {
                pendingDeleteChord = null
                onDeleteChord(chordToDelete)
            },
            onDismiss = { pendingDeleteChord = null }
        )
    }

    val progressionToDelete = progressions.firstOrNull { it.id == pendingDeleteProgression }
    if (progressionToDelete != null) {
        DeleteDialog(
            title = "Delete ${progressionToDelete.name}?",
            text = "The progression and its ${progressionToDelete.bars.size} bars will be removed from your library.",
            onConfirm = {
                pendingDeleteProgression = null
                onDeleteProgression(progressionToDelete)
            },
            onDismiss = { pendingDeleteProgression = null }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryCard(
    title: String,
    detail: String,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        onClick = onPlay,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            IconButton(onClick = onPlay) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play $title"
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Open $title on the keyboard"
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete $title"
                )
            }
        }
    }
}

@Composable
private fun DeleteDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
