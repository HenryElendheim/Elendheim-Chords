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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    chords: List<SavedChord>,
    onPlay: (SavedChord) -> Unit,
    onEdit: (SavedChord) -> Unit,
    onDelete: (SavedChord) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingDelete by rememberSaveable { mutableStateOf<String?>(null) }

    if (chords.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No chords yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Build a chord on the keyboard and save it.\nIt will live here.",
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
        items(chords, key = { it.id }) { chord ->
            ElevatedCard(
                onClick = { onPlay(chord) },
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
                            text = chord.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = chord.notesLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    IconButton(onClick = { onPlay(chord) }) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play ${chord.name}"
                        )
                    }
                    IconButton(onClick = { onEdit(chord) }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Open ${chord.name} on the keyboard"
                        )
                    }
                    IconButton(onClick = { pendingDelete = chord.id }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete ${chord.name}"
                        )
                    }
                }
            }
        }
    }

    val deleteTarget = chords.firstOrNull { it.id == pendingDelete }
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ${deleteTarget.name}?") },
            text = { Text("${deleteTarget.notesLabel} will be removed from your library.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        onDelete(deleteTarget)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
