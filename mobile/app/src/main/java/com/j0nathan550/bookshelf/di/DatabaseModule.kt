package com.j0nathan550.bookshelf.di

import android.content.Context
import androidx.room.Room
import com.j0nathan550.bookshelf.data.local.BookShelfDatabase
import com.j0nathan550.bookshelf.data.local.dao.BookDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BookShelfDatabase =
        Room.databaseBuilder(context, BookShelfDatabase::class.java, "bookshelf.db").build()

    @Provides
    fun provideBookDao(db: BookShelfDatabase): BookDao = db.bookDao()
}
