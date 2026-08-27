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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val userPreferences: UserPreferences,
    private val historyRepository: SessionHistoryRepository
) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    private val _selectedCategory = MutableStateFlow(1)
    private val _availableVoices = MutableStateFlow<List<Voice>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _speechRate = MutableStateFlow(1.0f)
    private var tts: TextToSpeech? = null
    private var speechRateSaveJob: Job? = null

    val sessionHistory: StateFlow<List<SessionHistoryEntity>> = historyRepository.sessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        tts = TextToSpeech(application, this)
        
        // Keep the slider responsive while applying persisted settings whenever they change.
        viewModelScope.launch {
            userPreferences.speechRateFlow.collect { rate ->
                _speechRate.value = rate
                tts?.setSpeechRate(rate)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val pagedWords: Flow<PagingData<WordEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            repository.getPagedWords(query)
        }
        .cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: StateFlow<SettingsUiState> = combine(
        combine(_selectedCategory, userPreferences.voiceIdFlow, _speechRate) { c, v, s -> Triple(c, v, s) },
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
            
            // Restore the saved voice, or persist a sensible English default once.
            viewModelScope.launch {
                val savedVoiceId = userPreferences.voiceIdFlow.first()
                val selectedVoice = savedVoiceId?.let { id -> englishVoices.find { it.name == id } }
                val voice = selectedVoice ?: englishVoices.find { it.locale.country == "IN" } ?: englishVoices.firstOrNull()
                if (voice != null) {
                    tts?.voice = voice
                    if (savedVoiceId != voice.name) {
                        userPreferences.updateVoiceId(voice.name)
                    }
                }

                val currentRate = userPreferences.speechRateFlow.first()
                _speechRate.value = currentRate
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
        _availableVoices.value.find { it.name == voiceId }?.let { voice ->
            tts?.voice = voice
        }
        viewModelScope.launch {
            userPreferences.updateVoiceId(voiceId)
        }
    }

    fun setSpeechRate(rate: Float) {
        val normalizedRate = rate.coerceIn(0.5f, 2.0f)
        _speechRate.value = normalizedRate
        tts?.setSpeechRate(normalizedRate)

        speechRateSaveJob?.cancel()
        speechRateSaveJob = viewModelScope.launch {
            delay(250)
            userPreferences.updateSpeechRate(normalizedRate)
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
        speechRateSaveJob?.cancel()
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

    fun updateWord(original: WordEntity, updated: WordEntity) {
        viewModelScope.launch {
            repository.updateWord(original, updated)
        }
    }

    fun deleteWord(wordEntity: WordEntity) {
        viewModelScope.launch {
            repository.deleteWord(wordEntity)
        }
    }

    fun clearSessionHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }
}
