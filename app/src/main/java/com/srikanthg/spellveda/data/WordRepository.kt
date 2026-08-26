package com.srikanthg.spellveda.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WordRepository(private val wordDao: WordDao) {
    fun getPagedWords(query: String = ""): Flow<PagingData<WordEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false,
                initialLoadSize = 50
            ),
            pagingSourceFactory = {
                if (query.isBlank()) {
                    wordDao.getAllWordsPaged()
                } else {
                    wordDao.searchWordsPaged(query)
                }
            }
        ).flow
    }

    fun getWordsByCategoryFlow(category: Int): Flow<List<WordEntity>> {
        return wordDao.getWordsByCategoryFlow(category)
    }

    suspend fun getWordsByCategory(category: Int): List<WordEntity> {
        return wordDao.getWordsByCategory(category)
    }

    suspend fun getRandomWordsByCategory(category: Int, limit: Int): List<WordEntity> {
        return wordDao.getRandomWordsByCategory(category, limit)
    }

    fun getWordCountByCategoryFlow(category: Int): Flow<Int> {
        return wordDao.getWordCountByCategoryFlow(category)
    }

    suspend fun getWordCountByCategory(category: Int): Int {
        return wordDao.getWordCountByCategory(category)
    }

    suspend fun insertWord(word: WordEntity) {
        wordDao.insert(word)
    }

    suspend fun updateWord(word: WordEntity) {
        wordDao.update(word)
    }

    suspend fun deleteWord(word: WordEntity) {
        wordDao.delete(word)
    }
}
