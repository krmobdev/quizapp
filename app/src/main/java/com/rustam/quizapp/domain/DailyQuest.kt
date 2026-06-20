package com.rustam.quizapp.domain

import com.rustam.quizapp.data.Category
import java.time.LocalDate

object DailyQuest {
    fun categoryForDay(categories: List<Category>, day: LocalDate = LocalDate.now()): Category? {
        if (categories.isEmpty()) return null
        val seed = day.year * 366L + day.dayOfYear
        return categories[(seed % categories.size).toInt()]
    }

    fun epochDay(day: LocalDate = LocalDate.now()): Long = day.toEpochDay()
}