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
import com.j0nathan550.bookshelf.BuildConfig

@Composable
fun BookCoverImage(
    coverImageUrl: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    if (coverImageUrl.isNullOrBlank()) {
        BookCoverPlaceholder(modifier)
    } else {
        // Relative paths (e.g. /covers/guid.jpg) come from locally-uploaded covers.
        // Prepend the configured base URL so the correct LAN host is used.
        val resolvedUrl = if (coverImageUrl.startsWith("/")) {
            BuildConfig.BASE_URL.trimEnd('/') + coverImageUrl
        } else {
            coverImageUrl
        }
        var loadFailed by remember(resolvedUrl) { mutableStateOf(false) }
        if (loadFailed) {
            BookCoverPlaceholder(modifier)
        } else {
            AsyncImage(
                model = resolvedUrl,
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
