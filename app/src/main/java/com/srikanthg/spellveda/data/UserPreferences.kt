package com.srikanthg.spellveda.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {

    private object PreferencesKeys {
        val APP_MODE = androidx.datastore.preferences.core.stringPreferencesKey("app_mode")
        val VOICE_ID = androidx.datastore.preferences.core.stringPreferencesKey("voice_id")
        val SPEECH_RATE = androidx.datastore.preferences.core.floatPreferencesKey("speech_rate")
    }

    val appModeFlow: Flow<AppMode> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val modeName = preferences[PreferencesKeys.APP_MODE] ?: AppMode.QUIZ.name
            try {
                AppMode.valueOf(modeName)
            } catch (e: IllegalArgumentException) {
                AppMode.QUIZ
            }
        }

    val voiceIdFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.VOICE_ID]
        }

    val speechRateFlow: Flow<Float> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SPEECH_RATE] ?: 1.0f
        }

    suspend fun updateAppMode(mode: AppMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_MODE] = mode.name
        }
    }

    suspend fun updateVoiceId(voiceId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VOICE_ID] = voiceId
        }
    }

    suspend fun updateSpeechRate(rate: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SPEECH_RATE] = rate
        }
    }
}
