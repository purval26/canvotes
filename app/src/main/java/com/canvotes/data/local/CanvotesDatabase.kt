package com.canvotes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.canvotes.data.local.dao.NotebookDao
import com.canvotes.data.local.dao.PageDao
import com.canvotes.data.local.entity.NotebookEntity
import com.canvotes.data.local.entity.PageEntity

@Database(
    entities = [NotebookEntity::class, PageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CanvotesDatabase : RoomDatabase() {
    abstract fun notebookDao(): NotebookDao
    abstract fun pageDao(): PageDao

    companion object {
        @Volatile
        private var INSTANCE: CanvotesDatabase? = null

        fun getInstance(context: Context): CanvotesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CanvotesDatabase::class.java,
                    "canvotes.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
