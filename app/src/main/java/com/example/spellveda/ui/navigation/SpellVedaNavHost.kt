package com.example.spellveda.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.spellveda.data.SpellingBeeDatabase
import com.example.spellveda.data.WordRepository
import com.example.spellveda.data.UserPreferences
import com.example.spellveda.ui.screens.*
import com.example.spellveda.ui.viewmodels.SettingsViewModel
import com.example.spellveda.ui.viewmodels.QuizViewModel
import com.example.spellveda.ui.viewmodels.QuizWizardViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import android.app.Application

@Composable
fun SpellVedaNavHost() {
    val backStack = rememberNavBackStack(Route.Splash)
    val context = LocalContext.current
    
    val repository = remember {
        val database = SpellingBeeDatabase.getDatabase(context)
        WordRepository(database.wordDao())
    }

    val userPreferences = remember { UserPreferences(context) }
    
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() }
    ) { route ->
        NavEntry(route) {
            when (val r = it) {
                is Route.Splash -> SplashScreen(
                    onSplashComplete = {
                        backStack.removeLastOrNull()
                        backStack.add(Route.CategorySelection)
                    }
                )
                is Route.CategorySelection -> CategorySelectionScreen(
                    onCategorySelected = { category ->
                        backStack.add(Route.QuizWizard(category))
                    },
                    onSettingsClick = { backStack.add(Route.Settings) },
                    onAboutClick = { backStack.add(Route.About) }
                )
                is Route.QuizWizard -> {
                    val wizardViewModel: QuizWizardViewModel = viewModel(
                        key = "Wizard_${r.category}",
                        factory = viewModelFactory {
                            initializer {
                                QuizWizardViewModel(repository, r.category)
                            }
                        }
                    )
                    QuizWizardScreen(
                        category = r.category,
                        viewModel = wizardViewModel,
                        onStartQuiz = { category, count ->
                            backStack.add(Route.Quiz(category, count))
                        },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                is Route.Quiz -> {
                    val quizViewModel: QuizViewModel = viewModel(
                        key = "Quiz_${r.category}_${r.questionsCount}",
                        factory = viewModelFactory {
                            initializer {
                                QuizViewModel(
                                    application = context.applicationContext as Application,
                                    repository = repository,
                                    userPreferences = userPreferences,
                                    category = r.category,
                                    questionsCount = r.questionsCount
                                )
                            }
                        }
                    )
                    QuizScreen(
                        viewModel = quizViewModel,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                is Route.Settings -> {
                    val settingsViewModel: SettingsViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer {
                                SettingsViewModel(
                                    application = context.applicationContext as Application,
                                    repository = repository,
                                    userPreferences = userPreferences
                                )
                            }
                        }
                    )
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                is Route.About -> AboutScreen(
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    }
}
