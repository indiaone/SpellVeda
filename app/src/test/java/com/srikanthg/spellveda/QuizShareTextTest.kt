package com.srikanthg.spellveda

import com.srikanthg.spellveda.data.AppMode
import com.srikanthg.spellveda.data.WordEntity
import com.srikanthg.spellveda.ui.screens.createSessionShareText
import com.srikanthg.spellveda.ui.viewmodels.QuizUiState
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class QuizShareTextTest {
    @Test
    fun `quiz share includes score and category`() {
        val state = QuizUiState(
            words = listOf(
                WordEntity(category = 1, word = "ALPHA", definition = null, exampleUsage = null),
                WordEntity(category = 1, word = "BETA", definition = null, exampleUsage = null),
                WordEntity(category = 1, word = "GAMMA", definition = null, exampleUsage = null),
                WordEntity(category = 1, word = "DELTA", definition = null, exampleUsage = null)
            ),
            correctCount = 3,
            wrongCount = 1,
            appMode = AppMode.QUIZ,
            category = 1,
            isFinished = true
        )

        val message = createSessionShareText(state)

        assertTrue(message.contains("completed a SpellVeda quiz in Class 1-2"))
        assertTrue(message.contains("Score: 75% (3/4 correct)."))
        assertFalse(message.contains("Words completed:"))
    }

    @Test
    fun `learning share includes completed words and no quiz score`() {
        val state = QuizUiState(
            words = listOf(WordEntity(category = 3, word = "ALPHA", definition = null, exampleUsage = null)),
            appMode = AppMode.LEARNING,
            category = 3,
            isFinished = true
        )

        val message = createSessionShareText(state)

        assertTrue(message.contains("completed a SpellVeda learning session in Class 5-7"))
        assertTrue(message.contains("Words completed: 1."))
        assertFalse(message.contains("Score:"))
        assertFalse(message.contains("correct"))
    }
}
