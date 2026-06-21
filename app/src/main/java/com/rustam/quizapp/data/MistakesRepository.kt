package com.rustam.quizapp.data

import android.content.Context
import androidx.room.withTransaction
import com.rustam.quizapp.data.db.AppDatabase
import com.rustam.quizapp.data.db.AppStateEntity
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

    private val db = AppDatabase.getInstance(context)
    private val dao = db.appStateDao()
    private val json = Json { ignoreUnknownKeys = true }

    val mistakes: Flow<List<Question>> = dao.observe().map { state ->
        decode(state?.mistakesJson)
    }

    val count: Flow<Int> = mistakes.map { it.size }

    /** Adds [questions] to the front of the pool, replacing any existing entries with the same id. */
    suspend fun addMistakes(questions: List<Question>) {
        if (questions.isEmpty()) return
        db.withTransaction {
            val current = decode(dao.get()?.mistakesJson)
            val newIds = questions.map { it.id }.toSet()
            val merged = (questions + current.filter { it.id !in newIds }).take(MAX_MISTAKES)
            writeMistakes(merged)
        }
    }

    /** Removes the questions with the given [ids] from the pool (e.g. solved during practice). */
    suspend fun removeSolved(ids: Collection<String>) {
        if (ids.isEmpty()) return
        db.withTransaction {
            val current = decode(dao.get()?.mistakesJson)
            if (current.isEmpty()) return@withTransaction
            writeMistakes(current.filter { it.id !in ids })
        }
    }

    private suspend fun writeMistakes(questions: List<Question>) {
        val state = dao.get() ?: AppStateEntity()
        val raw = if (questions.isEmpty()) null else json.encodeToString(questions)
        dao.upsert(state.copy(mistakesJson = raw))
    }

    private fun decode(raw: String?): List<Question> =
        raw?.let { runCatching { json.decodeFromString<List<Question>>(it) }.getOrNull() } ?: emptyList()

    private companion object {
        const val MAX_MISTAKES = 60
    }
}
