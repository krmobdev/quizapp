package com.rustam.quizapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Reserved category id used to launch a "work on mistakes" practice run. The quiz
 * flow recognises this id and pulls its questions from [MistakesRepository] instead
 * of the regular per-category question bank.
 */
const val MISTAKES_CATEGORY_ID = "__mistakes__"

/**
 * Persists the pool of questions the player has answered incorrectly, so they can be
 * replayed later in a dedicated practice mode. Questions are stored newest-first,
 * de-duplicated by id and capped at [MAX_MISTAKES].
 */
class MistakesRepository(context: Context) {

    private val dataStore = context.applicationContext.statsDataStore
    private val json = Json { ignoreUnknownKeys = true }

    val mistakes: Flow<List<Question>> = dataStore.data.map { prefs ->
        prefs[MISTAKES_JSON]?.let { raw ->
            runCatching { json.decodeFromString<List<Question>>(raw) }.getOrNull()
        } ?: emptyList()
    }

    val count: Flow<Int> = mistakes.map { it.size }

    /** Adds [questions] to the front of the pool, replacing any existing entries with the same id. */
    suspend fun addMistakes(questions: List<Question>) {
        if (questions.isEmpty()) return
        dataStore.edit { prefs ->
            val current = decode(prefs[MISTAKES_JSON])
            val newIds = questions.map { it.id }.toSet()
            val merged = (questions + current.filter { it.id !in newIds }).take(MAX_MISTAKES)
            prefs[MISTAKES_JSON] = json.encodeToString(merged)
        }
    }

    /** Removes the questions with the given [ids] from the pool (e.g. solved during practice). */
    suspend fun removeSolved(ids: Collection<String>) {
        if (ids.isEmpty()) return
        dataStore.edit { prefs ->
            val current = decode(prefs[MISTAKES_JSON])
            if (current.isEmpty()) return@edit
            val remaining = current.filter { it.id !in ids }
            if (remaining.isEmpty()) {
                prefs.remove(MISTAKES_JSON)
            } else {
                prefs[MISTAKES_JSON] = json.encodeToString(remaining)
            }
        }
    }

    private fun decode(raw: String?): List<Question> =
        raw?.let { runCatching { json.decodeFromString<List<Question>>(it) }.getOrNull() } ?: emptyList()

    private companion object {
        const val MAX_MISTAKES = 60
        val MISTAKES_JSON = stringPreferencesKey("mistakes_json")
    }
}
