package com.rustam.quizapp.data

import android.content.Context
import androidx.room.withTransaction
import com.rustam.quizapp.data.db.AppDatabase
import com.rustam.quizapp.data.db.AppStateEntity
import com.rustam.quizapp.domain.AnswerReward
import com.rustam.quizapp.domain.QuizEventType
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
    val answerRewards: List<AnswerReward> = emptyList(),
    val selectedAnswer: Int? = null,
    val showExplanation: Boolean = false,
    val isTimeout: Boolean = false,
    val eventType: QuizEventType? = null,
    val questionTimeSeconds: Int = 10
)

class QuizProgressRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.appStateDao()
    private val json = Json { ignoreUnknownKeys = true }

    val savedProgress: Flow<SavedQuizProgress?> = dao.observe().map { state ->
        state?.savedQuizJson?.let { raw ->
            runCatching { json.decodeFromString<SavedQuizProgress>(raw) }.getOrNull()
        }
    }

    suspend fun save(progress: SavedQuizProgress) {
        val raw = json.encodeToString(progress)
        db.withTransaction {
            val current = dao.get() ?: AppStateEntity()
            dao.upsert(current.copy(savedQuizJson = raw))
        }
    }

    suspend fun clear() {
        db.withTransaction {
            val current = dao.get() ?: return@withTransaction
            dao.upsert(current.copy(savedQuizJson = null))
        }
    }
}
