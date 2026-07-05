package com.elendheim.chords.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elendheim.chords.model.Note

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuilderScreen(
    selectedNotes: Set<Int>,
    progression: List<List<Int>>,
    onKeyTap: (Int) -> Unit,
    onRemoveNote: (Int) -> Unit,
    onPlay: () -> Unit,
    onClear: () -> Unit,
    onSave: (String) -> Unit,
    onSetBar: () -> Unit,
    onPlayBar: (Int) -> Unit,
    onPlayProgression: () -> Unit,
    onDeleteBar: (Int) -> Unit,
    onMoveBar: (Int, Int) -> Unit,
    onSaveProgression: (String) -> Unit,
    onExportMidi: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var showSaveProgressionDialog by rememberSaveable { mutableStateOf(false) }
    var selectedBar by rememberSaveable { mutableStateOf<Int?>(null) }
    val sortedNotes = selectedNotes.sorted()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("audio/midi")
    ) { uri ->
        if (uri != null) onExportMidi(uri)
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (sortedNotes.isEmpty()) "Your chord" else Note.chordLabel(sortedNotes),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (sortedNotes.isEmpty()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (sortedNotes.isNotEmpty()) {
                        TextButton(onClick = { showSaveDialog = true }) {
                            Text("Save")
                        }
                        TextButton(onClick = onClear) {
                            Text("Clear")
                        }
                    }
                }
                if (sortedNotes.isEmpty()) {
                    Text(
                        text = "Tap keys below to add notes. Tap a note again to take it out.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (midi in sortedNotes) {
                            AssistChip(
                                onClick = { onRemoveNote(midi) },
                                label = { Text(Note.label(midi)) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove ${Note.label(midi)}",
                                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPlay,
                enabled = sortedNotes.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text("Play", modifier = Modifier.padding(start = 4.dp))
            }
            FilledTonalButton(
                onClick = onSetBar,
                enabled = sortedNotes.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Set")
            }
        }

        Keyboard(
            selectedNotes = selectedNotes,
            onKeyTap = onKeyTap,
            modifier = Modifier
                .weight(3f)
                .padding(top = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progression",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onPlayProgression,
                enabled = progression.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play the progression"
                )
            }
            IconButton(
                onClick = { showSaveProgressionDialog = true },
                enabled = progression.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = "Save the progression"
                )
            }
            IconButton(
                onClick = { exportLauncher.launch("progression.mid") },
                enabled = progression.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = "Export as MIDI file"
                )
            }
            IconButton(
                onClick = {
                    selectedBar?.let(onDeleteBar)
                    selectedBar = null
                },
                enabled = selectedBar != null
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete selected bar"
                )
            }
        }

        PianoRoll(
            bars = progression,
            selectedBar = selectedBar,
            onBarTap = { index ->
                selectedBar = index
                onPlayBar(index)
            },
            onMoveBar = { from, to ->
                onMoveBar(from, to)
                selectedBar = selectedBar?.let { sel ->
                    when {
                        sel == from -> to
                        from < sel && to >= sel -> sel - 1
                        from > sel && to <= sel -> sel + 1
                        else -> sel
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 16.dp, end = 16.dp, bottom = 6.dp)
        )
    }

    if (showSaveDialog) {
        NameDialog(
            title = "Name this chord",
            subtitle = Note.chordLabel(sortedNotes),
            placeholder = "Chorus pad",
            onConfirm = { name ->
                showSaveDialog = false
                onSave(name)
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    if (showSaveProgressionDialog) {
        NameDialog(
            title = "Name this progression",
            subtitle = "${progression.size} bars",
            placeholder = "Verse loop",
            onConfirm = { name ->
                showSaveProgressionDialog = false
                onSaveProgression(name)
            },
            onDismiss = { showSaveProgressionDialog = false }
        )
    }
}

@Composable
private fun NameDialog(
    title: String,
    subtitle: String,
    placeholder: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text(placeholder) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
