package com.j0nathan550.bookshelf.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.j0nathan550.bookshelf.data.remote.dto.BookNoteDto

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromNoteList(notes: List<BookNoteDto>): String = gson.toJson(notes)

    @TypeConverter
    fun toNoteList(json: String): List<BookNoteDto> {
        val type = object : TypeToken<List<BookNoteDto>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
