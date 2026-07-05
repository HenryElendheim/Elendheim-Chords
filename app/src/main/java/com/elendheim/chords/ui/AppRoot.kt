package com.elendheim.chords.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Piano
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elendheim.chords.ChordsViewModel
import com.elendheim.chords.model.Note
import kotlinx.coroutines.launch

private const val TAB_BUILD = 0
private const val TAB_LIBRARY = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(viewModel: ChordsViewModel = viewModel()) {
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_BUILD) }
    val selectedNotes by viewModel.selectedNotes.collectAsState()
    val library by viewModel.library.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Elendheim Chords", fontWeight = FontWeight.SemiBold)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == TAB_BUILD,
                    onClick = { selectedTab = TAB_BUILD },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == TAB_BUILD) {
                                Icons.Filled.Piano
                            } else {
                                Icons.Outlined.Piano
                            },
                            contentDescription = null
                        )
                    },
                    label = { Text("Build") }
                )
                NavigationBarItem(
                    selected = selectedTab == TAB_LIBRARY,
                    onClick = { selectedTab = TAB_LIBRARY },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == TAB_LIBRARY) {
                                Icons.Filled.LibraryMusic
                            } else {
                                Icons.Outlined.LibraryMusic
                            },
                            contentDescription = null
                        )
                    },
                    label = { Text("Library") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            TAB_BUILD -> BuilderScreen(
                selectedNotes = selectedNotes,
                onKeyTap = viewModel::toggleNote,
                onRemoveNote = viewModel::removeNote,
                onPlay = viewModel::playSelection,
                onClear = viewModel::clearSelection,
                onSave = { name ->
                    val label = Note.chordLabel(selectedNotes)
                    if (viewModel.saveSelection(name)) {
                        val shownName = name.trim().ifEmpty { label }
                        scope.launch {
                            snackbarHostState.showSnackbar("Saved $shownName")
                        }
                    }
                },
                modifier = contentModifier
            )
            TAB_LIBRARY -> LibraryScreen(
                chords = library,
                onPlay = viewModel::playChord,
                onEdit = { chord ->
                    viewModel.loadIntoBuilder(chord)
                    selectedTab = TAB_BUILD
                },
                onDelete = { viewModel.deleteChord(it.id) },
                modifier = contentModifier
            )
        }
    }
}
