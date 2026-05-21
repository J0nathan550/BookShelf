package com.j0nathan550.bookshelf.ui.books.addedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookScreen(
    bookId: Int?,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddEditBookViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showGenreDropdown by remember { mutableStateOf(false) }
    var showFormatDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.init(bookId) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (bookId == null) "Add Book" else "Edit Book") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.author,
                onValueChange = viewModel::onAuthorChange,
                label = { Text("Author *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Genre picker
            val selectedGenreName = state.genres.find { it.id == state.selectedGenreId }?.name ?: "Select genre *"
            OutlinedButton(onClick = { showGenreDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedGenreName)
                DropdownMenu(expanded = showGenreDropdown, onDismissRequest = { showGenreDropdown = false }) {
                    state.genres.forEach { genre ->
                        DropdownMenuItem(
                            text = { Text(genre.name) },
                            onClick = {
                                viewModel.onGenreSelected(genre.id)
                                showGenreDropdown = false
                            },
                        )
                    }
                }
            }

            // Format picker
            val selectedFormatName = state.formats.find { it.id == state.selectedFormatId }?.name ?: "Select format *"
            OutlinedButton(onClick = { showFormatDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedFormatName)
                DropdownMenu(expanded = showFormatDropdown, onDismissRequest = { showFormatDropdown = false }) {
                    state.formats.forEach { format ->
                        DropdownMenuItem(
                            text = { Text(format.name) },
                            onClick = {
                                viewModel.onFormatSelected(format.id)
                                showFormatDropdown = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.pages,
                onValueChange = viewModel::onPagesChange,
                label = { Text("Pages *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            OutlinedTextField(
                value = state.coverImageUrl,
                onValueChange = viewModel::onCoverUrlChange,
                label = { Text("Cover image URL (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = { viewModel.save(bookId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text(if (bookId == null) "Add Book" else "Save Changes")
                }
            }
        }
    }
}
