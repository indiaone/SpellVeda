package com.srikanthg.spellveda.ui.viewmodels

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srikanthg.spellveda.data.AppMode
import com.srikanthg.spellveda.data.WordEntity
import com.srikanthg.spellveda.data.WordRepository
import com.srikanthg.spellveda.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

data class QuizUiState(
    val words: List<WordEntity> = emptyList(),
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val userInput: String = "",
    val feedback: String? = null,
    val isCorrect: Boolean? = null,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true,
    val showDefinition: Boolean = false,
    val showUsage: Boolean = false,
    val appMode: AppMode = AppMode.QUIZ
)

class QuizViewModel(
    application: Application,
    private val repository: WordRepository,
    private val userPreferences: UserPreferences,
    private val category: Int,
    private val questionsCount: Int
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    init {
        tts = TextToSpeech(application, this)
        loadQuizWords()
    }

    private fun loadQuizWords() {
        viewModelScope.launch {
            try {
                val appMode = userPreferences.appModeFlow.first()
                val isLearningMode = appMode == AppMode.LEARNING
                val words = repository.getRandomWordsByCategory(category, questionsCount)
                _uiState.value = _uiState.value.copy(
                    words = words, 
                    isLoading = false,
                    appMode = appMode,
                    // In learning mode, show definition and usage by default
                    showDefinition = isLearningMode,
                    showUsage = isLearningMode
                )
                if (words.isNotEmpty() && isTtsInitialized) {
                    speakWord(words[0].word)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, feedback = "Error loading words: ${e.message}")
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val preferredLocale = Locale.Builder().setLanguage("en").setRegion("IN").build()
            val preferredResult = tts?.setLanguage(preferredLocale) ?: TextToSpeech.ERROR
            val result = if (
                preferredResult == TextToSpeech.LANG_MISSING_DATA ||
                preferredResult == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                tts?.setLanguage(Locale.ENGLISH) ?: TextToSpeech.ERROR
            } else {
                preferredResult
            }
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED && result != TextToSpeech.ERROR) {
                // Apply voice preference
                applyVoicePreference()
                
                isTtsInitialized = true
                // Speak the first word if it was loaded while TTS was initializing
                val currentState = _uiState.value
                if (!currentState.isLoading && currentState.words.isNotEmpty() && currentState.currentIndex == 0 && currentState.feedback == null) {
                    speakWord(currentState.words[0].word)
                }
            }
        }
    }

    private fun applyVoicePreference() {
        viewModelScope.launch {
            val voiceId = userPreferences.voiceIdFlow.first()
            val speechRate = userPreferences.speechRateFlow.first()
            val voices = tts?.voices ?: return@launch
            val preferredVoice = voices.find { it.name == voiceId }
            
            preferredVoice?.let { tts?.voice = it }
            tts?.setSpeechRate(speechRate)
        }
    }

    fun onUserInputChange(input: String) {
        _uiState.value = _uiState.value.copy(userInput = input)
    }

    fun submitAnswer() {
        val currentState = _uiState.value
        if (currentState.isFinished || currentState.feedback != null) return

        val currentWord = currentState.words.getOrNull(currentState.currentIndex) ?: return
        val isCorrect = currentState.userInput.trim().equals(currentWord.word, ignoreCase = true)

        if (isCorrect) {
            _uiState.value = currentState.copy(
                correctCount = currentState.correctCount + 1,
                feedback = "Correct!",
                isCorrect = true
            )
            speakFeedback("Correct")
        } else {
            _uiState.value = currentState.copy(
                wrongCount = currentState.wrongCount + 1,
                feedback = "Wrong! The word was ${currentWord.word.uppercase()}",
                isCorrect = false
            )
            playFeedbackTone(false)
        }
    }

    private fun speakFeedback(text: String) {
        if (isTtsInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun playFeedbackTone(isCorrect: Boolean) {
        if (isCorrect) return
        val tone = ToneGenerator.TONE_PROP_NACK
        toneGenerator.startTone(tone, 200)
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentIndex + 1
        val isLearningMode = currentState.appMode == AppMode.LEARNING
        if (nextIndex < currentState.words.size) {
            _uiState.value = currentState.copy(
                currentIndex = nextIndex,
                userInput = "",
                feedback = null,
                isCorrect = null,
                showDefinition = isLearningMode,
                showUsage = isLearningMode
            )
            speakWord(currentState.words[nextIndex].word)
        } else {
            _uiState.value = currentState.copy(isFinished = true)
        }
    }

    fun speakWord(word: String) {
        if (isTtsInitialized) {
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun showAndSpeakDefinition() {
        val currentWord = _uiState.value.words.getOrNull(_uiState.value.currentIndex) ?: return
        _uiState.value = _uiState.value.copy(showDefinition = true)
        if (isTtsInitialized) {
            tts?.speak(currentWord.definition ?: "", TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun showAndSpeakUsage() {
        val currentWord = _uiState.value.words.getOrNull(_uiState.value.currentIndex) ?: return
        _uiState.value = _uiState.value.copy(showUsage = true)
        if (isTtsInitialized) {
            tts?.speak(currentWord.exampleUsage ?: "", TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        toneGenerator.release()
    }
}
