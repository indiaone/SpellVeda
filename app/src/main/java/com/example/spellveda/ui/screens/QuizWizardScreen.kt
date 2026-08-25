package com.example.spellveda.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spellveda.ui.viewmodels.QuizWizardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizWizardScreen(
    category: Int,
    viewModel: QuizWizardViewModel,
    onStartQuiz: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val wordCount by viewModel.wordCount.collectAsState()
    var questionCount by remember(wordCount) { 
        mutableFloatStateOf(if (wordCount != null) minOf(10f, wordCount!!.toFloat()) else 10f) 
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val categoryName = when(category) {
        1 -> "Class 1-2"
        2 -> "Class 3-4"
        3 -> "Class 5-7"
        4 -> "Class 8-10"
        else -> "Level $category"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Quiz Setup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Selected Category",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        categoryName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    wordCount?.let {
                        Text(
                            "$it words available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Number of Questions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "${questionCount.toInt()}",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                val maxQuestionsPossible = (wordCount ?: 20).toFloat()
                val maxQuestions = minOf(100f, maxQuestionsPossible)
                val minQuestions = if (maxQuestionsPossible < 10f) maxQuestionsPossible else 10f

                Slider(
                    value = questionCount.coerceIn(minQuestions, maxQuestions),
                    onValueChange = { 
                        val stepped = Math.round(it / 10.0) * 10.0
                        questionCount = stepped.toFloat().coerceIn(minQuestions, maxQuestions)
                    },
                    valueRange = minQuestions..maxQuestions,
                    steps = if (maxQuestions > minQuestions) ((maxQuestions - minQuestions) / 10).toInt() - 1 else 0,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("10", style = MaterialTheme.typography.labelMedium)
                    if (maxQuestions > 50) Text("50", style = MaterialTheme.typography.labelMedium)
                    Text("${maxQuestions.toInt()}", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val requestedCount = questionCount.toInt()
                    if (wordCount != null && wordCount!! < requestedCount) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Not enough words in this category. Only $wordCount available.",
                                duration = SnackbarDuration.Short
                            )
                        }
                    } else {
                        onStartQuiz(category, requestedCount)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = wordCount != null && wordCount!! > 0
            ) {
                Text("START QUIZ", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
