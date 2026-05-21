package com.j0nathan550.bookshelf.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun BookCoverImage(
    coverImageUrl: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    if (coverImageUrl.isNullOrBlank()) {
        BookCoverPlaceholder(modifier)
    } else {
        var loadFailed by remember(coverImageUrl) { mutableStateOf(false) }
        if (loadFailed) {
            BookCoverPlaceholder(modifier)
        } else {
            AsyncImage(
                model = coverImageUrl,
                contentDescription = null,
                contentScale = contentScale,
                modifier = modifier,
                onError = { loadFailed = true },
            )
        }
    }
}

@Composable
fun BookCoverPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.5f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
