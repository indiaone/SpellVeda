package com.srikanthg.spellveda.data

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "words", primaryKeys = ["category", "word"])
data class WordEntity(
    val category: Int, // 1-4
    val word: String,
    val definition: String?,
    @ColumnInfo(name = "example_usage")
    val exampleUsage: String?
)
