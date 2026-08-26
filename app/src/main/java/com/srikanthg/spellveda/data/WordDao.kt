package com.srikanthg.spellveda.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordEntity): Long

    @Update
    suspend fun update(word: WordEntity)

    @Delete
    suspend fun delete(word: WordEntity)

    @Query("SELECT * FROM words WHERE category = :category")
    fun getWordsByCategoryFlow(category: Int): kotlinx.coroutines.flow.Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE category = :category")
    suspend fun getWordsByCategory(category: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE category = :category ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomWordsByCategory(category: Int, limit: Int): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    @Query("SELECT COUNT(*) FROM words WHERE category = :category")
    fun getWordCountByCategoryFlow(category: Int): kotlinx.coroutines.flow.Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE category = :category")
    suspend fun getWordCountByCategory(category: Int): Int

    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' ORDER BY word ASC")
    fun searchWordsPaged(query: String): PagingSource<Int, WordEntity>

    @Query("SELECT * FROM words ORDER BY word ASC")
    fun getAllWordsPaged(): PagingSource<Int, WordEntity>
}
