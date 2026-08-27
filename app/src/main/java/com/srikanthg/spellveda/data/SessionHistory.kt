package com.srikanthg.spellveda.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "session_history")
data class SessionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,
    val category: Int,
    val totalItems: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val completedAt: Long = System.currentTimeMillis()
) {
    val appMode: AppMode
        get() = runCatching { AppMode.valueOf(mode) }.getOrDefault(AppMode.QUIZ)
}

@Dao
interface SessionHistoryDao {
    @Query("SELECT * FROM session_history ORDER BY completedAt DESC")
    fun observeSessions(): Flow<List<SessionHistoryEntity>>

    @Insert
    suspend fun insert(session: SessionHistoryEntity)

    @Query("DELETE FROM session_history")
    suspend fun clear()
}

class SessionHistoryRepository(private val dao: SessionHistoryDao) {
    val sessions: Flow<List<SessionHistoryEntity>> = dao.observeSessions()

    suspend fun recordSession(
        mode: AppMode,
        category: Int,
        totalItems: Int,
        correctAnswers: Int,
        wrongAnswers: Int
    ) {
        dao.insert(
            SessionHistoryEntity(
                mode = mode.name,
                category = category,
                totalItems = totalItems,
                correctAnswers = correctAnswers,
                wrongAnswers = wrongAnswers
            )
        )
    }

    suspend fun clearHistory() {
        dao.clear()
    }
}

fun categoryDisplayName(category: Int): String = when (category) {
    1 -> "Class 1-2"
    2 -> "Class 3-4"
    3 -> "Class 5-7"
    4 -> "Class 8-10"
    else -> "Category $category"
}
