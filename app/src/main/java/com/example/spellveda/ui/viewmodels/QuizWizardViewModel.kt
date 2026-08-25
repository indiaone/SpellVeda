package com.example.spellveda.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spellveda.data.WordRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class QuizWizardViewModel(
    repository: WordRepository,
    category: Int
) : ViewModel() {
    val wordCount: StateFlow<Int?> = repository.getWordCountByCategoryFlow(category)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
