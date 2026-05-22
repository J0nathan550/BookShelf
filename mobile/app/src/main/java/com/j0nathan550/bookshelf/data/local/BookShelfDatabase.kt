package com.j0nathan550.bookshelf.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.j0nathan550.bookshelf.data.local.converter.Converters
import com.j0nathan550.bookshelf.data.local.dao.BookDao
import com.j0nathan550.bookshelf.data.local.dao.PendingOperationDao
import com.j0nathan550.bookshelf.data.local.entity.BookEntity
import com.j0nathan550.bookshelf.data.local.entity.PendingOperationEntity

@Database(
    entities = [BookEntity::class, PendingOperationEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class BookShelfDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun pendingOperationDao(): PendingOperationDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pending_operations` " +
                        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`operationType` TEXT NOT NULL, " +
                        "`payload` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)",
                )
            }
        }
    }
}
