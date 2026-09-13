package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookmarkEntity::class,
        MistakeEntity::class,
        UserProgressEntity::class,
        AIAnalysisCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SSKNDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun mistakeDao(): MistakeDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun aiAnalysisDao(): AIAnalysisDao

    companion object {
        @Volatile
        private var INSTANCE: SSKNDatabase? = null

        fun getDatabase(context: Context): SSKNDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SSKNDatabase::class.java,
                    "sskn_local_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
