package com.rustam.quizapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Aggregated statistics for a single quiz category. */
data class CategoryStats(
    val categoryId: String,
    val quizzesCompleted: Int,
    val correctAnswers: Int,
    val questionsAnswered: Int,
    val bestScorePercent: Int
)

/** Whole-app statistics, exposed to the UI. */
data class AppStats(
    val totalQuizzesCompleted: Int,
    val categories: List<CategoryStats>
)

// Shared across the module (e.g. SettingsRepository) so all preferences live in one
// DataStore instance. Declaring a second delegate with the same name would crash at runtime.
internal val Context.statsDataStore: DataStore<Preferences> by preferencesDataStore(name = "quiz_stats")

/**
 * Persists per-category quiz statistics in a [DataStore] backed by [Preferences].
 *
 * Each category keeps its counters under keys of the form `cat.<id>.<field>`; the
 * category set is reconstructed from those keys when reading, so categories appear
 * in the stats only once they have at least one completed quiz.
 */
class StatsRepository(context: Context) {

    private val dataStore = context.applicationContext.statsDataStore

    /**
     * Records the outcome of one finished quiz in [categoryId]: bumps the quiz count,
     * accumulates [correct] answers over [total] questions, and raises the best score
     * (in percent) if this run beat the previous best. No-op for an empty quiz.
     */
    suspend fun recordQuizResult(categoryId: String, correct: Int, total: Int) {
        if (total <= 0) return
        val scorePercent = correct * 100 / total
        dataStore.edit { prefs ->
            prefs[quizzesKey(categoryId)] = (prefs[quizzesKey(categoryId)] ?: 0) + 1
            prefs[correctKey(categoryId)] = (prefs[correctKey(categoryId)] ?: 0) + correct
            prefs[answeredKey(categoryId)] = (prefs[answeredKey(categoryId)] ?: 0) + total
            prefs[bestKey(categoryId)] = maxOf(prefs[bestKey(categoryId)] ?: 0, scorePercent)
            prefs[TOTAL_QUIZZES] = (prefs[TOTAL_QUIZZES] ?: 0) + 1
        }
    }

    /** Emits the full statistics for the UI whenever the stored data changes. */
    fun observeStats(): Flow<AppStats> = dataStore.data.map { prefs ->
        val categoryIds = prefs.asMap().keys
            .map { it.name }
            .filter { it.startsWith(CATEGORY_PREFIX) }
            .map { it.removePrefix(CATEGORY_PREFIX).substringBefore(FIELD_SEPARATOR) }
            .distinct()
            .sorted()

        val categories = categoryIds.map { id ->
            CategoryStats(
                categoryId = id,
                quizzesCompleted = prefs[quizzesKey(id)] ?: 0,
                correctAnswers = prefs[correctKey(id)] ?: 0,
                questionsAnswered = prefs[answeredKey(id)] ?: 0,
                bestScorePercent = prefs[bestKey(id)] ?: 0
            )
        }

        AppStats(
            totalQuizzesCompleted = prefs[TOTAL_QUIZZES] ?: 0,
            categories = categories
        )
    }

    private fun quizzesKey(id: String) = intPreferencesKey("$CATEGORY_PREFIX$id${FIELD_SEPARATOR}quizzes")
    private fun correctKey(id: String) = intPreferencesKey("$CATEGORY_PREFIX$id${FIELD_SEPARATOR}correct")
    private fun answeredKey(id: String) = intPreferencesKey("$CATEGORY_PREFIX$id${FIELD_SEPARATOR}answered")
    private fun bestKey(id: String) = intPreferencesKey("$CATEGORY_PREFIX$id${FIELD_SEPARATOR}best")

    private companion object {
        const val CATEGORY_PREFIX = "cat."
        const val FIELD_SEPARATOR = "."
        val TOTAL_QUIZZES = intPreferencesKey("total_quizzes")
    }
}
