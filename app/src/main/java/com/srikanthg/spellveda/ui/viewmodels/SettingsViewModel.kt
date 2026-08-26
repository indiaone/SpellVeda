package com.srikanthg.spellveda.ui.viewmodels

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.srikanthg.spellveda.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

data class SettingsUiState(
    val selectedCategory: Int = 1,
    val voiceId: String? = null,
    val speechRate: Float = 1.0f,
    val appMode: AppMode = AppMode.QUIZ,
    val availableVoices: List<Voice> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

class SettingsViewModel(
    application: Application,
    private val repository: WordRepository,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    private val _selectedCategory = MutableStateFlow(1)
    private val _availableVoices = MutableStateFlow<List<Voice>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(application, this)
        
        // Collect preferences and apply to TTS
        viewModelScope.launch {
            userPreferences.speechRateFlow.collect { rate ->
                tts?.setSpeechRate(rate)
            }
        }
        
        viewModelScope.launch {
            userPreferences.voiceIdFlow.collect { voiceId ->
                val voice = _availableVoices.value.find { it.name == voiceId }
                if (voice != null) {
                    tts?.voice = voice
                }
            }
        }
    }

    val pagedWords: Flow<PagingData<WordEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            repository.getPagedWords(query)
        }
        .cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SettingsUiState> = combine(
        combine(_selectedCategory, userPreferences.voiceIdFlow, userPreferences.speechRateFlow) { c, v, s -> Triple(c, v, s) },
        combine(userPreferences.appModeFlow, _availableVoices, _searchQuery) { a, v, q -> Triple(a, v, q) }
    ) { (category, voiceId, speechRate), (appMode, voices, searchQuery) ->
        SettingsUiState(category, voiceId, speechRate, appMode, voices, false, searchQuery)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(isLoading = true)
    )

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val voices = tts?.voices ?: emptyList()
            // Filter English voices, prioritize en-IN
            val englishVoices = voices.filter { 
                it.locale.language == "en"
            }.sortedWith(compareByDescending<Voice> { it.locale.country == "IN" }.thenBy { it.name })
            
            _availableVoices.value = englishVoices
            
            // Set default if none selected
            viewModelScope.launch {
                if (userPreferences.voiceIdFlow.first() == null && englishVoices.isNotEmpty()) {
                    val defaultVoice = englishVoices.find { it.locale.country == "IN" } ?: englishVoices.first()
                    userPreferences.updateVoiceId(defaultVoice.name)
                }
                
                // Initialize speech rate
                val currentRate = userPreferences.speechRateFlow.first()
                tts?.setSpeechRate(currentRate)
            }
        }
    }

    fun previewVoice() {
        val currentVoiceId = uiState.value.voiceId
        val voice = _availableVoices.value.find { it.name == currentVoiceId }
        if (voice != null) {
            tts?.voice = voice
        }
        tts?.setSpeechRate(uiState.value.speechRate)
        tts?.speak("This is a preview of the selected voice in Spell Veda.", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun setVoiceId(voiceId: String) {
        viewModelScope.launch {
            userPreferences.updateVoiceId(voiceId)
        }
    }

    fun setSpeechRate(rate: Float) {
        viewModelScope.launch {
            userPreferences.updateSpeechRate(rate)
            tts?.setSpeechRate(rate)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAppMode(mode: AppMode) {
        viewModelScope.launch {
            userPreferences.updateAppMode(mode)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }


    fun selectCategory(category: Int) {
        _selectedCategory.value = category
    }

    fun addWord(word: String, definition: String, usage: String, category: Int) {
        viewModelScope.launch {
            repository.insertWord(WordEntity(word = word, definition = definition, exampleUsage = usage, category = category))
        }
    }

    fun updateWord(wordEntity: WordEntity) {
        viewModelScope.launch {
            repository.updateWord(wordEntity)
        }
    }

    fun deleteWord(wordEntity: WordEntity) {
        viewModelScope.launch {
            repository.deleteWord(wordEntity)
        }
    }
}
