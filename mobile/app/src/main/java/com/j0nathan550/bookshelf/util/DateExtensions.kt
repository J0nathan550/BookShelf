package com.j0nathan550.bookshelf.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val ISO_PARSERS = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
    "yyyy-MM-dd'T'HH:mm:ss",
).map { pattern ->
    SimpleDateFormat(pattern, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
}

fun String.toReadableDate(): String = try {
    val date = ISO_PARSERS.firstNotNullOfOrNull { parser ->
        try { parser.parse(this) } catch (_: Exception) { null }
    }
    date?.let { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(it) } ?: this
} catch (_: Exception) {
    this
}
