package com.srikanthg.spellveda.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WordEntity::class, SessionHistoryEntity::class], version = 2, exportSchema = false)
abstract class SpellingBeeDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun sessionHistoryDao(): SessionHistoryDao

    companion object {
        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS session_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        mode TEXT NOT NULL,
                        category INTEGER NOT NULL,
                        totalItems INTEGER NOT NULL,
                        correctAnswers INTEGER NOT NULL,
                        wrongAnswers INTEGER NOT NULL,
                        completedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: SpellingBeeDatabase? = null

        fun getDatabase(context: Context): SpellingBeeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpellingBeeDatabase::class.java,
                    "spelling_bee_database"
                )
                    .createFromAsset("words.db")
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
