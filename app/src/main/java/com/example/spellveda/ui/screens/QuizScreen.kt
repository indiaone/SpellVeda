package com.example.spellveda.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spellveda.ui.viewmodels.QuizViewModel
import com.example.spellveda.ui.viewmodels.QuizUiState
import androidx.compose.ui.tooling.preview.Preview
import com.example.spellveda.ui.theme.SpellVedaTheme
import com.example.spellveda.data.WordEntity
import com.example.spellveda.data.AppMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    if (uiState.isFinished) {
        QuizSummaryScreen(uiState = uiState, onBack = onBack)
    } else {
        BackHandler {
            showExitDialog = true
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit Quiz") },
                text = { Text("Are you sure you want to end the quiz? Your progress will not be saved.") },
                confirmButton = {
                    TextButton(onClick = onBack) {
                        Text("End Quiz")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Continue")
                    }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Spelling Quiz") },
                    navigationIcon = {
                        IconButton(onClick = { showExitDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        ScoreDisplay(
                            correct = uiState.correctCount,
                            wrong = uiState.wrongCount
                        )
                    }
                )
            }
        ) { padding ->
            QuizContent(
                uiState = uiState,
                onUserInputChange = viewModel::onUserInputChange,
                onSubmit = viewModel::submitAnswer,
                onPlayWord = { viewModel.speakWord(it) },
                onShowDefinition = viewModel::showAndSpeakDefinition,
                onShowUsage = viewModel::showAndSpeakUsage,
                onNext = viewModel::nextQuestion,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun ScoreDisplay(correct: Int, wrong: Int) {
    val successColor = if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)
    Row(
        modifier = Modifier.padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
            Text(
                text = "✓ $correct",
                style = MaterialTheme.typography.labelLarge,
                color = successColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
            Text(
                text = "✗ $wrong",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun QuizScreenPreview() {
    SpellVedaTheme {
        QuizContent(
            uiState = QuizUiState(
                words = listOf(
                    WordEntity(category = 1, word = "EXAMPLE", definition = "An instance or illustration.", exampleUsage = "This is an example.")
                ),
                isLoading = false
            ),
            onUserInputChange = {},
            onSubmit = {},
            onPlayWord = {},
            onShowDefinition = {},
            onShowUsage = {},
            onNext = {}
        )
    }
}

@Composable
fun QuizContent(
    uiState: QuizUiState,
    onUserInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onPlayWord: (String) -> Unit,
    onShowDefinition: () -> Unit,
    onShowUsage: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.words.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No words found for this category.")
        }
    } else {
        val currentWord = uiState.words[uiState.currentIndex]

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { (uiState.currentIndex + 1).toFloat() / uiState.words.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                )

                val isLearningMode = uiState.appMode == AppMode.LEARNING

                if (isLearningMode) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text("Learning Mode") },
                        icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Text(
                    text = "Question ${uiState.currentIndex + 1} of ${uiState.words.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Word Display
                val wordText = if (isLearningMode || uiState.feedback != null) {
                    currentWord.word.uppercase()
                } else {
                    "????"
                }
                
                Text(
                    text = wordText,
                    style = if (isLearningMode) MaterialTheme.typography.displayLarge else MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = if (uiState.feedback != null) {
                        if (uiState.isCorrect == true) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                    } else if (isLearningMode) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (!isLearningMode) {
                    OutlinedTextField(
                        value = uiState.userInput,
                        onValueChange = onUserInputChange,
                        label = { Text("Type the word here") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = uiState.feedback == null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onSubmit()
                            keyboardController?.hide()
                        }),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuizActionButton(
                        onClick = { onPlayWord(currentWord.word) },
                        icon = Icons.Rounded.PlayArrow,
                        label = "Play",
                        enabled = uiState.feedback == null || isLearningMode
                    )
                    QuizActionButton(
                        onClick = onShowDefinition,
                        icon = Icons.Rounded.Info,
                        label = "Definition",
                        enabled = uiState.feedback == null || isLearningMode
                    )
                    QuizActionButton(
                        onClick = onShowUsage,
                        icon = Icons.Rounded.FormatQuote,
                        label = "Usage",
                        enabled = uiState.feedback == null || isLearningMode
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(visible = uiState.showDefinition || isLearningMode) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Definition:", fontWeight = FontWeight.Bold)
                            Text(currentWord.definition ?: "No definition available")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(visible = uiState.showUsage || isLearningMode) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Usage:", fontWeight = FontWeight.Bold)
                            Text(currentWord.exampleUsage ?: "No example usage available")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                if (uiState.feedback != null || isLearningMode) {
                    if (uiState.feedback != null) {
                        val feedbackColor = if (uiState.isCorrect == true) {
                            if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (uiState.isCorrect == true) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = feedbackColor,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.feedback,
                                style = MaterialTheme.typography.headlineSmall,
                                color = feedbackColor,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(if (uiState.currentIndex == uiState.words.size - 1) "Finish Quiz" else "Next Question")
                    }
                } else {
                    Button(
                        onClick = {
                            onSubmit()
                            keyboardController?.hide()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = uiState.userInput.isNotBlank()
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@Composable
fun QuizActionButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean = true
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(icon, contentDescription = label)
        }
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSummaryScreen(
    uiState: QuizUiState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quiz Summary") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Great Job!",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val successColor = if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)
                    SummaryRow("Total Questions", uiState.words.size.toString())
                    SummaryRow("Correct Answers", uiState.correctCount.toString(), successColor)
                    SummaryRow("Wrong Answers", uiState.wrongCount.toString(), MaterialTheme.colorScheme.error)
                    
                    val percentage = (uiState.correctCount.toFloat() / uiState.words.size * 100).toInt()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Score: $percentage%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Return Home")
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
