package com.j0nathan550.bookshelf.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.j0nathan550.bookshelf.data.local.converter.Converters
import com.j0nathan550.bookshelf.data.local.dao.BookDao
import com.j0nathan550.bookshelf.data.local.entity.BookEntity

@Database(entities = [BookEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class BookShelfDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}
