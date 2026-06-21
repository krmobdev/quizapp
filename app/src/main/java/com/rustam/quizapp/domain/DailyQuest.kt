package com.rustam.quizapp.domain

import com.rustam.quizapp.data.Category
import java.time.LocalDate

/** @deprecated Use [QuizEvents] instead. Kept for backward compatibility. */
object DailyQuest {
    fun categoryForDay(categories: List<Category>, day: LocalDate = LocalDate.now()): Category? =
        QuizEvents.categoryForDay(categories, 0, day)

    fun epochDay(day: LocalDate = LocalDate.now()): Long = QuizEvents.epochDay(day)
}