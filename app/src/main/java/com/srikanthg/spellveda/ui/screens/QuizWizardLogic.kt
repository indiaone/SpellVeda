package com.srikanthg.spellveda.ui.screens

internal const val DEFAULT_QUESTION_COUNT = 10
internal const val MIN_QUESTION_COUNT = 1
internal const val MAX_QUESTION_COUNT = 100

internal fun maxQuestionsForWordCount(wordCount: Int): Int =
    wordCount.coerceIn(0, MAX_QUESTION_COUNT)

internal fun initialQuestionCount(wordCount: Int): Int =
    minOf(DEFAULT_QUESTION_COUNT, maxQuestionsForWordCount(wordCount)).coerceAtLeast(MIN_QUESTION_COUNT)
