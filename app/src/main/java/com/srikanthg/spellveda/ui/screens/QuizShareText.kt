package com.srikanthg.spellveda.ui.screens

import com.srikanthg.spellveda.data.AppMode
import com.srikanthg.spellveda.data.categoryDisplayName
import com.srikanthg.spellveda.ui.viewmodels.QuizUiState

internal fun createSessionShareText(uiState: QuizUiState): String {
    val isQuiz = uiState.appMode == AppMode.QUIZ
    val percentage = if (uiState.words.isNotEmpty()) {
        uiState.correctCount * 100 / uiState.words.size
    } else {
        0
    }

    return buildString {
        append("I completed a SpellVeda ")
        append(if (isQuiz) "quiz" else "learning session")
        append(" in ${categoryDisplayName(uiState.category)}.\n")
        if (isQuiz) {
            append("Score: $percentage% (${uiState.correctCount}/${uiState.words.size} correct).\n")
        } else {
            append("Words completed: ${uiState.words.size}.\n")
        }
        append("Keep learning with SpellVeda!")
    }
}
