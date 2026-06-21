package com.rustam.quizapp.data

import android.content.Context
import androidx.room.withTransaction
import com.rustam.quizapp.data.db.AppDatabase
import com.rustam.quizapp.data.db.AppStateEntity
import com.rustam.quizapp.data.db.CategoryStatsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

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

/**
 * Persists per-category quiz statistics in Room. Categories appear in the stats only
 * once they have at least one recorded quiz (a row is created on the first result).
 */
class StatsRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val categoryDao = db.categoryStatsDao()
    private val appStateDao = db.appStateDao()

    /**
     * Records the outcome of one finished quiz in [categoryId]: bumps the quiz count,
     * accumulates [correct] answers over [total] questions, and raises the best score
     * (in percent) if this run beat the previous best. No-op for an empty quiz.
     */
    suspend fun recordQuizResult(categoryId: String, correct: Int, total: Int) {
        if (total <= 0) return
        val scorePercent = correct * 100 / total
        db.withTransaction {
            val existing = categoryDao.get(categoryId) ?: CategoryStatsEntity(categoryId)
            categoryDao.upsert(
                existing.copy(
                    quizzesCompleted = existing.quizzesCompleted + 1,
                    correctAnswers = existing.correctAnswers + correct,
                    questionsAnswered = existing.questionsAnswered + total,
                    bestScorePercent = maxOf(existing.bestScorePercent, scorePercent)
                )
            )
            val state = appStateDao.get() ?: AppStateEntity()
            appStateDao.upsert(state.copy(totalQuizzes = state.totalQuizzes + 1))
        }
    }

    /** Emits the full statistics for the UI whenever the stored data changes. */
    fun observeStats(): Flow<AppStats> =
        combine(categoryDao.observeAll(), appStateDao.observe()) { categories, state ->
            AppStats(
                totalQuizzesCompleted = state?.totalQuizzes ?: 0,
                categories = categories.map {
                    CategoryStats(
                        categoryId = it.categoryId,
                        quizzesCompleted = it.quizzesCompleted,
                        correctAnswers = it.correctAnswers,
                        questionsAnswered = it.questionsAnswered,
                        bestScorePercent = it.bestScorePercent
                    )
                }
            )
        }
}
