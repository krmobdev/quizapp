package com.rustam.quizapp.domain

import com.rustam.quizapp.data.Category
import com.rustam.quizapp.data.Difficulty
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields
import kotlinx.serialization.Serializable

@Serializable
enum class QuizEventType {
    DAILY,
    WEEKLY,
    WEEKEND_BLITZ,
    MARATHON
}

data class QuizEvent(
    val type: QuizEventType,
    val category: Category,
    val difficulty: Difficulty?,
    val questionCount: Int,
    val questionTimeSeconds: Int,
    val coinMultiplier: Int,
    val bonusPoints: Int,
    val maxCompletions: Int
)

data class QuizEventProgress(
    val event: QuizEvent,
    val completions: Int,
    val available: Boolean
)

object QuizEvents {
    const val WEEKLY_MAX_COMPLETIONS = 10
    const val WEEKEND_BLITZ_MAX_COMPLETIONS = 5
    const val MARATHON_MAX_COMPLETIONS = 3

    fun epochDay(day: LocalDate = LocalDate.now()): Long = day.toEpochDay()

    fun epochWeek(day: LocalDate = LocalDate.now()): Long {
        val fields = WeekFields.ISO
        val year = day.get(fields.weekBasedYear())
        val week = day.get(fields.weekOfWeekBasedYear())
        return year * 100L + week
    }

    fun epochWeekend(day: LocalDate = LocalDate.now()): Long {
        val saturday = day.with(DayOfWeek.SATURDAY)
        return saturday.toEpochDay() / 7
    }

    fun categoryForDay(categories: List<Category>, day: LocalDate = LocalDate.now()): Category? {
        if (categories.isEmpty()) return null
        val seed = day.year * 366L + day.dayOfYear
        return categories[(seed % categories.size).toInt()]
    }

    fun categoryForWeek(categories: List<Category>, day: LocalDate = LocalDate.now()): Category? {
        if (categories.isEmpty()) return null
        val seed = epochWeek(day) * 17 + 3
        return categories[(seed % categories.size).toInt()]
    }

    fun categoryForWeekend(categories: List<Category>, day: LocalDate = LocalDate.now()): Category? {
        if (categories.isEmpty()) return null
        val seed = epochWeekend(day) * 31 + 7
        return categories[(seed % categories.size).toInt()]
    }

    fun categoryForMarathon(categories: List<Category>, day: LocalDate = LocalDate.now()): Category? {
        if (categories.isEmpty()) return null
        val seed = day.year * 53L + day.get(WeekFields.ISO.weekOfWeekBasedYear()) * 2 + 1
        return categories[(seed % categories.size).toInt()]
    }

    fun isWeekend(day: LocalDate = LocalDate.now()): Boolean {
        val dow = day.dayOfWeek
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
    }

    fun eventFor(type: QuizEventType, categories: List<Category>, day: LocalDate = LocalDate.now()): QuizEvent? {
        return when (type) {
            QuizEventType.DAILY -> categoryForDay(categories, day)?.let { category ->
                QuizEvent(
                    type = type,
                    category = category,
                    difficulty = null,
                    questionCount = 10,
                    questionTimeSeconds = 10,
                    coinMultiplier = 3,
                    bonusPoints = 100,
                    maxCompletions = 1
                )
            }
            QuizEventType.WEEKLY -> categoryForWeek(categories, day)?.let { category ->
                QuizEvent(
                    type = type,
                    category = category,
                    difficulty = Difficulty.MEDIUM,
                    questionCount = 10,
                    questionTimeSeconds = 10,
                    coinMultiplier = 3,
                    bonusPoints = 75,
                    maxCompletions = WEEKLY_MAX_COMPLETIONS
                )
            }
            QuizEventType.WEEKEND_BLITZ -> {
                if (!isWeekend(day)) return null
                categoryForWeekend(categories, day)?.let { category ->
                    QuizEvent(
                        type = type,
                        category = category,
                        difficulty = Difficulty.EASY,
                        questionCount = 10,
                        questionTimeSeconds = 5,
                        coinMultiplier = 2,
                        bonusPoints = 50,
                        maxCompletions = WEEKEND_BLITZ_MAX_COMPLETIONS
                    )
                }
            }
            QuizEventType.MARATHON -> categoryForMarathon(categories, day)?.let { category ->
                QuizEvent(
                    type = type,
                    category = category,
                    difficulty = Difficulty.HARD,
                    questionCount = 15,
                    questionTimeSeconds = 10,
                    coinMultiplier = 2,
                    bonusPoints = 60,
                    maxCompletions = MARATHON_MAX_COMPLETIONS
                )
            }
        }
    }

    fun activeEvents(
        categories: List<Category>,
        progress: EventProgressSnapshot,
        day: LocalDate = LocalDate.now()
    ): List<QuizEventProgress> {
        return QuizEventType.entries.mapNotNull { type ->
            val event = eventFor(type, categories, day) ?: return@mapNotNull null
            val completions = progress.completionsFor(type, day)
            val available = completions < event.maxCompletions
            QuizEventProgress(event = event, completions = completions, available = available)
        }
    }
}

data class EventProgressSnapshot(
    val dailyCompletedDay: Long = -1,
    val weeklyEpoch: Long = -1,
    val weeklyCompletions: Int = 0,
    val weekendEpoch: Long = -1,
    val weekendCompletions: Int = 0,
    val marathonDay: Long = -1,
    val marathonCompletions: Int = 0
) {
    fun completionsFor(type: QuizEventType, day: LocalDate = LocalDate.now()): Int = when (type) {
        QuizEventType.DAILY ->
            if (dailyCompletedDay == QuizEvents.epochDay(day)) 1 else 0
        QuizEventType.WEEKLY ->
            if (weeklyEpoch == QuizEvents.epochWeek(day)) weeklyCompletions else 0
        QuizEventType.WEEKEND_BLITZ ->
            if (weekendEpoch == QuizEvents.epochWeekend(day)) weekendCompletions else 0
        QuizEventType.MARATHON ->
            if (marathonDay == QuizEvents.epochDay(day)) marathonCompletions else 0
    }
}