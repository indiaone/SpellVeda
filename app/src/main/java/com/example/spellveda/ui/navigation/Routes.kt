package com.example.spellveda.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable data object Splash : Route
    @Serializable data object CategorySelection : Route
    @Serializable data class QuizWizard(val category: Int) : Route
    @Serializable data class Quiz(val category: Int, val questionsCount: Int) : Route
    @Serializable data object Settings : Route
    @Serializable data object About : Route
}
