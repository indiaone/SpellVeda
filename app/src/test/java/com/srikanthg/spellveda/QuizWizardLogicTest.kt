package com.srikanthg.spellveda

import com.srikanthg.spellveda.ui.screens.initialQuestionCount
import com.srikanthg.spellveda.ui.screens.maxQuestionsForWordCount
import org.junit.Assert.assertEquals
import org.junit.Test

class QuizWizardLogicTest {
    @Test
    fun `empty category has no available questions`() {
        assertEquals(0, maxQuestionsForWordCount(0))
    }

    @Test
    fun `small category starts with one question`() {
        assertEquals(1, initialQuestionCount(1))
        assertEquals(5, initialQuestionCount(5))
    }

    @Test
    fun `large category is capped at one hundred questions`() {
        assertEquals(100, maxQuestionsForWordCount(250))
        assertEquals(10, initialQuestionCount(250))
    }
}
