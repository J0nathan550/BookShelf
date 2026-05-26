package com.j0nathan550.bookshelf.ui.books.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.j0nathan550.bookshelf.data.remote.dto.BookDto
import com.j0nathan550.bookshelf.ui.common.BookCoverImage

private val statusFilters = listOf("All", "Available", "Want to Read", "Currently Reading", "Finished", "Lent Out")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    onBookClick: (Int) -> Unit,
    onAddBook: () -> Unit,
    onNavigateToLent: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToAccount: () -> Unit,
    viewModel: BookListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadBooks() }

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
                title = { Text("My Library") },
                actions = {
                    IconButton(onClick = onNavigateToAccount) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Account")
                    }
                },
            )
        },
        bottomBar = {
            BookShelfNavBar(
                currentRoute = "library",
                isAdmin = state.isAdmin,
                onLibrary = {},
                onLent = onNavigateToLent,
                onStats = onNavigateToStats,
                onAdmin = onNavigateToAdmin,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBook) {
                Icon(Icons.Default.Add, contentDescription = "Add book")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (state.pendingCount > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                ) {
                    Text(
                        text = "${state.pendingCount} change(s) saved offline — will sync when back online",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE65100),
                    )
                }
            }

            PrimaryTabRow(selectedTabIndex = state.selectedTab) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { Text("Library") },
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text("Pending") },
                )
            }

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search by title or author…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (state.selectedTab == 0) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(statusFilters) { filter ->
                        FilterChip(
                            selected = state.statusFilter == filter,
                            onClick = { viewModel.onStatusFilterChange(filter) },
                            label = { Text(filter) },
                        )
                    }
                }
            }

            if (state.selectedTab == 1) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Text(
                        text = "Your submissions are awaiting admin review and will appear in the Library once approved.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (state.isLoading && state.books.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val displayed = viewModel.filteredBooks()
                if (displayed.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = when {
                                state.searchQuery.isNotBlank() -> "No books match your search"
                                state.selectedTab == 1 -> "No pending submissions"
                                else -> "No books yet — tap + to add one"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(displayed, key = { it.id }) { book ->
                            BookCard(book = book, onClick = { onBookClick(book.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BookShelfNavBar(
    currentRoute: String,
    isAdmin: Boolean,
    onLibrary: () -> Unit,
    onLent: () -> Unit,
    onStats: () -> Unit,
    onAdmin: () -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "library",
            onClick = onLibrary,
            icon = { Icon(Icons.Default.Book, contentDescription = null) },
            label = { Text("Library") },
        )
        NavigationBarItem(
            selected = currentRoute == "lent",
            onClick = onLent,
            icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
            label = { Text("Lent") },
        )
        NavigationBarItem(
            selected = currentRoute == "stats",
            onClick = onStats,
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text("Stats") },
        )
        if (isAdmin) {
            NavigationBarItem(
                selected = currentRoute == "admin",
                onClick = onAdmin,
                icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                label = { Text("Admin") },
            )
        }
    }
}

@Composable
fun BookCard(book: BookDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            BookCoverImage(
                coverImageUrl = book.coverImageUrl,
                modifier = Modifier
                    .size(width = 60.dp, height = 80.dp)
                    .clip(MaterialTheme.shapes.small),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                book.genre?.let {
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!book.isApproved) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Pending Review", style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                    val readingStatus = book.readingStatus?.status
                    if (readingStatus != null && readingStatus in listOf("Want to Read", "Currently Reading", "Finished")) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(readingStatus, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                    if (book.lendingRecord != null && !book.lendingRecord.isReturned) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Lent Out", style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LentBooksScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onBookClick: (Int) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    viewModel: BookListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lent Books") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            BookShelfNavBar(
                currentRoute = "lent",
                isAdmin = state.isAdmin,
                onLibrary = onNavigateToLibrary,
                onLent = {},
                onStats = onNavigateToStats,
                onAdmin = onNavigateToAdmin,
            )
        },
    ) { padding ->
        val lentBooks = state.books.filter { it.lendingRecord != null && !it.lendingRecord.isReturned }
        if (lentBooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("No books are currently lent out", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(lentBooks, key = { it.id }) { book ->
                    BookCard(book = book, onClick = { onBookClick(book.id) })
                }
            }
        }
    }
}
