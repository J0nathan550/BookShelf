package com.j0nathan550.bookshelf.ui.books.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.j0nathan550.bookshelf.ui.common.BookCoverImage
import com.j0nathan550.bookshelf.util.toReadableDate

private val readingStatuses = listOf("Want to Read", "Currently Reading", "Finished")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Int,
    onNavigateBack: () -> Unit,
    onEditBook: () -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }
    val isAdmin = viewModel.isAdmin
    val currentUserId = viewModel.currentUserId

    LaunchedEffect(bookId) { viewModel.loadBook(bookId) }

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
                title = { Text(state.book?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = onEditBook) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val book = state.book ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BookCoverImage(
                    coverImageUrl = book.coverImageUrl,
                    modifier = Modifier
                        .size(width = 100.dp, height = 140.dp)
                        .clip(MaterialTheme.shapes.medium),
                )
                Column {
                    Text(book.title, style = MaterialTheme.typography.headlineSmall)
                    Text(book.author, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    book.genre?.let { Text(it.name, style = MaterialTheme.typography.bodyMedium) }
                    book.format?.let { Text(it.name, style = MaterialTheme.typography.bodyMedium) }
                    book.pages?.let { Text("$it pages", style = MaterialTheme.typography.bodySmall) }
                }
            }

            HorizontalDivider()

            Text("Reading Status", style = MaterialTheme.typography.titleMedium)
            val displayReadingStatus = book.readingStatus?.status
                ?.takeIf { it in listOf("Want to Read", "Currently Reading", "Finished") }
            Box {
                androidx.compose.material3.OutlinedButton(
                    onClick = { showStatusDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(displayReadingStatus ?: "Not set")
                }
                DropdownMenu(expanded = showStatusDropdown, onDismissRequest = { showStatusDropdown = false }) {
                    readingStatuses.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = {
                                showStatusDropdown = false
                                viewModel.updateStatus(s, book.readingStatus?.rating, book.readingStatus?.completionDate)
                            },
                        )
                    }
                }
            }
            book.readingStatus?.rating?.let {
                Text("Rating: ${"★".repeat(it)}${"☆".repeat(5 - it)}", style = MaterialTheme.typography.bodyMedium)
            }

            HorizontalDivider()

            Text("Availability", style = MaterialTheme.typography.titleMedium)
            val lending = book.lendingRecord
            if (lending != null && !lending.isReturned) {
                Text("Lent to: ${lending.borrowerName}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Since: ${lending.lendingDate.toReadableDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isAdmin || currentUserId == lending.lenderUserId) {
                    Button(onClick = viewModel::showReturnDialog, modifier = Modifier.fillMaxWidth()) {
                        Text("Mark as Returned")
                    }
                }
            } else {
                Button(onClick = viewModel::showLendDialog, modifier = Modifier.fillMaxWidth()) {
                    Text("Lend This Book")
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Notes", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = viewModel::showAddNoteDialog) {
                    Icon(Icons.Default.Add, contentDescription = "Add note")
                }
            }
            if (book.notes.isEmpty()) {
                Text("No notes yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            book.notes.forEach { note ->
                Column {
                    if (isAdmin && note.authorName != null) {
                        Text(
                            text = note.authorName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(note.noteText, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = note.createdDate.toReadableDate() +
                            if (note.modifiedDate != null) " · edited" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row {
                        TextButton(onClick = { viewModel.showEditNoteDialog(note.id, note.noteText) }) { Text("Edit") }
                        TextButton(onClick = { viewModel.deleteNote(note.id) }) { Text("Delete") }
                    }
                }
            }

            if (isAdmin && book.lendingHistory.isNotEmpty()) {
                HorizontalDivider()
                Text("Lending History", style = MaterialTheme.typography.titleMedium)
                book.lendingHistory.forEach { record ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Lent to: ${record.borrowerName}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        record.lenderName?.let {
                            Text(
                                text = "By: $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = record.lendingDate.toReadableDate() +
                                if (record.isReturned && record.returnDate != null)
                                    " → returned ${record.returnDate.toReadableDate()}"
                                else " · not yet returned",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Delete confirm dialog (admin only)
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Book") },
            text = { Text("Delete \"${state.book?.title}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteBook { showDeleteConfirm = false; onNavigateBack() } },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }

    // Lend confirmation dialog — no fields, server auto-fills date and name
    if (state.showLendDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideLendDialog,
            title = { Text("Lend Book") },
            text = { Text("Record this book as lent out? Today's date and your account name will be saved automatically.") },
            confirmButton = { Button(onClick = viewModel::lendBook) { Text("Lend") } },
            dismissButton = { TextButton(onClick = viewModel::hideLendDialog) { Text("Cancel") } },
        )
    }

    // Return confirmation dialog — no fields, server auto-fills return date
    if (state.showReturnDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideReturnDialog,
            title = { Text("Mark as Returned") },
            text = { Text("Mark \"${state.book?.title}\" as returned today?") },
            confirmButton = { Button(onClick = viewModel::returnBook) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = viewModel::hideReturnDialog) { Text("Cancel") } },
        )
    }

    // Note dialog
    if (state.showNoteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideNoteDialog,
            title = { Text(if (state.editingNoteId == null) "Add Note" else "Edit Note") },
            text = {
                OutlinedTextField(
                    value = state.noteText,
                    onValueChange = viewModel::onNoteTextChange,
                    label = { Text("Note") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = { Button(onClick = viewModel::saveNote) { Text("Save") } },
            dismissButton = { TextButton(onClick = viewModel::hideNoteDialog) { Text("Cancel") } },
        )
    }
}
