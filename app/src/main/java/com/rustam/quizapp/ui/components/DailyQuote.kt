package com.rustam.quizapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.rustam.quizapp.R
import java.time.LocalDate

@Composable
fun rememberDailyQuote(): String {
    val resources = LocalContext.current.resources
    return remember(resources.configuration) {
        val quotes = resources.getStringArray(R.array.daily_quotes)
        if (quotes.isEmpty()) return@remember ""
        val today = LocalDate.now()
        val seed = today.year * 366L + today.dayOfYear
        quotes[(seed % quotes.size).toInt()]
    }
}