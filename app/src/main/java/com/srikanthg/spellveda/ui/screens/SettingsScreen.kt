package com.srikanthg.spellveda.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.srikanthg.spellveda.data.AppMode
import com.srikanthg.spellveda.data.WordEntity
import com.srikanthg.spellveda.ui.viewmodels.SettingsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessionHistory by viewModel.sessionHistory.collectAsStateWithLifecycle()
    val pagedWords = viewModel.pagedWords.collectAsLazyPagingItems()
    var showAddDialog by remember { mutableStateOf(false) }
    var wordToEdit by remember { mutableStateOf<WordEntity?>(null) }
    var wordToDelete by remember { mutableStateOf<WordEntity?>(null) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Word")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // App Mode Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("App Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = AppMode.entries.size),
                                onClick = { viewModel.setAppMode(mode) },
                                selected = uiState.appMode == mode
                            ) {
                                Text(mode.name)
                            }
                        }
                    }
                    
                    Text(
                        text = if (uiState.appMode == AppMode.QUIZ) 
                            "Quiz Mode: Definitions and usage are hidden during spelling."
                        else 
                            "Learning Mode: Definitions and usage are shown to help you learn.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Voice Settings Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Voice Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        val selectedVoice = uiState.availableVoices.find { it.name == uiState.voiceId } ?: uiState.availableVoices.firstOrNull()

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedVoice?.name ?: "Default",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Voice") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                uiState.availableVoices.forEach { voice ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(voice.name)
                                                Text(voice.locale.displayName, style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        onClick = {
                                            viewModel.setVoiceId(voice.name)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { viewModel.previewVoice() },
                            enabled = selectedVoice != null
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Preview Voice")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Speech Rate: ${String.format("%.1fx", uiState.speechRate)}", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = uiState.speechRate,
                        onValueChange = { viewModel.setSpeechRate(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 14 // 0.1 increments
                    )
                }
            }


            SessionHistorySection(
                sessions = sessionHistory,
                onClearHistory = { showClearHistoryDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search words...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Word CRUD Section
            Text(
                if (uiState.searchQuery.isBlank()) "Manage Words" else "Matching Words",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.isLoading || pagedWords.loadState.refresh is LoadState.Loading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (pagedWords.itemCount == 0) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No words found matching \"${uiState.searchQuery}\"")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = pagedWords.itemCount,
                        key = pagedWords.itemKey { "${it.category}_${it.word}" },
                        contentType = pagedWords.itemContentType { "word" }
                    ) { index ->
                        pagedWords[index]?.let { word ->
                            WordItem(
                                word = word,
                                onEdit = { wordToEdit = it },
                                onDelete = { wordToDelete = it }
                            )
                        }
                    }

                    when (val state = pagedWords.loadState.append) {
                        is LoadState.Loading -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                                }
                            }
                        }
                        is LoadState.Error -> {
                            item {
                                Text(
                                    "Error loading more words: ${state.error.message}",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        WordDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { word, def, usage, cat ->
                viewModel.addWord(word, def, usage, cat)
                showAddDialog = false
            },
            initialCategory = uiState.selectedCategory
        )
    }

    wordToEdit?.let { word ->
        WordDialog(
            wordToEdit = word,
            onDismiss = { wordToEdit = null },
            onConfirm = { w, d, u, c ->
                viewModel.updateWord(word, word.copy(word = w.trim(), definition = d.trim(), exampleUsage = u.trim(), category = c))
                wordToEdit = null
            }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear session history?") },
            text = { Text("This removes all saved quiz and learning sessions from this device. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearSessionHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) { Text("Cancel") }
            }
        )
    }

    wordToDelete?.let { word ->
        AlertDialog(
            onDismissRequest = { wordToDelete = null },
            title = { Text("Delete word?") },
            text = { Text("Delete \"${word.word}\" from the word list? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWord(word)
                        wordToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { wordToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SessionHistorySection(
    sessions: List<com.srikanthg.spellveda.data.SessionHistoryEntity>,
    onClearHistory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Session History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (sessions.isNotEmpty()) {
                    TextButton(onClick = onClearHistory) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }

            if (sessions.isEmpty()) {
                Text(
                    "Completed quizzes and learning sessions will appear here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                sessions.take(8).forEach { session ->
                    SessionHistoryRow(session)
                }
                if (sessions.size > 8) {
                    Text(
                        "Showing the 8 most recent sessions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryRow(session: com.srikanthg.spellveda.data.SessionHistoryEntity) {
    val dateFormatter = remember { java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT) }
    val modeLabel = if (session.appMode == AppMode.QUIZ) "Quiz" else "Learning"
    val score = if (session.appMode == AppMode.QUIZ && session.totalItems > 0) {
        "${(session.correctAnswers * 100 / session.totalItems)}%"
    } else {
        "${session.totalItems} words"
    }

    ListItem(
        headlineContent = {
            Text("$modeLabel · ${com.srikanthg.spellveda.data.categoryDisplayName(session.category)}")
        },
        supportingContent = {
            Text("$score · ${dateFormatter.format(java.util.Date(session.completedAt))}")
        },
        leadingContent = {
            Icon(
                if (session.appMode == AppMode.QUIZ) Icons.Default.CheckCircle else Icons.Default.School,
                contentDescription = null
            )
        },
        tonalElevation = 0.dp
    )
}

@Composable
fun WordItem(
    word: WordEntity,
    onEdit: (WordEntity) -> Unit,
    onDelete: (WordEntity) -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(word.word, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(word.definition ?: "No definition", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { onEdit(word) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { onDelete(word) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDialog(
    wordToEdit: WordEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int) -> Unit,
    initialCategory: Int = 1
) {
    var word by remember { mutableStateOf(wordToEdit?.word ?: "") }
    var definition by remember { mutableStateOf(wordToEdit?.definition ?: "") }
    var usage by remember { mutableStateOf(wordToEdit?.exampleUsage ?: "") }
    var category by remember { mutableIntStateOf(wordToEdit?.category ?: initialCategory) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (wordToEdit == null) "Add Word" else "Edit Word") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text("Word") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = definition,
                    onValueChange = { definition = it },
                    label = { Text("Definition") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = usage,
                    onValueChange = { usage = it },
                    label = { Text("Usage") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..4).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text("$cat") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(word.trim(), definition.trim(), usage.trim(), category) },
                enabled = word.isNotBlank() && definition.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
