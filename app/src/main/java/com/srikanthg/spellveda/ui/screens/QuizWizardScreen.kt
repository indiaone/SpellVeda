package com.srikanthg.spellveda.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srikanthg.spellveda.ui.viewmodels.QuizWizardViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizWizardScreen(
    category: Int,
    viewModel: QuizWizardViewModel,
    onStartQuiz: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val wordCount by viewModel.wordCount.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var questionCount by remember(wordCount) {
        mutableIntStateOf(wordCount?.let(::initialQuestionCount) ?: DEFAULT_QUESTION_COUNT)
    }

    val categoryName = when (category) {
        1 -> "Class 1-2"
        2 -> "Class 3-4"
        3 -> "Class 5-7"
        4 -> "Class 8-10"
        else -> "Level $category"
    }
    val maxQuestions = wordCount?.let(::maxQuestionsForWordCount) ?: 0
    val hasWords = maxQuestions > 0
    val sliderMax = maxQuestions.coerceAtLeast(1)
    val sliderMin = minOf(MIN_QUESTION_COUNT, sliderMax)
    val safeQuestionCount = questionCount.coerceIn(sliderMin, sliderMax)

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
                    when (val count = wordCount) {
                        null -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
                                Text("Checking available words...")
                            }
                        }
                        0 -> Text(
                            "No words are available in this category.",
                            color = MaterialTheme.colorScheme.error
                        )
                        else -> Text(
                            "$count words available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (hasWords) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Number of Questions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "$safeQuestionCount",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Choose between $sliderMin and $sliderMax questions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (sliderMin < sliderMax) {
                        Slider(
                            value = safeQuestionCount.toFloat(),
                            onValueChange = { value ->
                                questionCount = value.roundToInt().coerceIn(sliderMin, sliderMax)
                            },
                            valueRange = sliderMin.toFloat()..sliderMax.toFloat(),
                            steps = (sliderMax - sliderMin - 1).coerceAtLeast(0),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$sliderMin", style = MaterialTheme.typography.labelMedium)
                        Text("$sliderMax", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val requestedCount = safeQuestionCount.coerceIn(1, maxQuestions)
                    if (wordCount == null || requestedCount > maxQuestions) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Please wait until the word count is available.",
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
                enabled = hasWords
            ) {
                Text("START QUIZ", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
