package com.rustam.quizapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Serialized snapshot of an in-progress quiz, restored after the user leaves mid-run. */
@Serializable
data class SavedQuizProgress(
    val categoryId: String,
    val difficulty: Difficulty?,
    val language: AppLanguage = AppLanguage.RU,
    val questions: List<Question>,
    val currentIndex: Int,
    val correctCount: Int,
    val penaltyCount: Int,
    val mistakes: List<Question>,
    val selectedAnswer: Int? = null,
    val showExplanation: Boolean = false,
    val isTimeout: Boolean = false
)

class QuizProgressRepository(context: Context) {

    private val dataStore = context.applicationContext.statsDataStore
    private val json = Json { ignoreUnknownKeys = true }

    val savedProgress: Flow<SavedQuizProgress?> = dataStore.data.map { prefs ->
        prefs[SAVED_QUIZ_JSON]?.let { raw ->
            runCatching { json.decodeFromString<SavedQuizProgress>(raw) }.getOrNull()
        }
    }

    suspend fun save(progress: SavedQuizProgress) {
        dataStore.edit { prefs ->
            prefs[SAVED_QUIZ_JSON] = json.encodeToString(progress)
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs -> prefs.remove(SAVED_QUIZ_JSON) }
    }

    private companion object {
        val SAVED_QUIZ_JSON = stringPreferencesKey("saved_quiz_json")
    }
}