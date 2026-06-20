package com.rustam.quizapp.ui.localization

import androidx.annotation.StringRes
import com.rustam.quizapp.R
import com.rustam.quizapp.data.AppLanguage
import com.rustam.quizapp.data.ThemeMode
import com.rustam.quizapp.ui.screens.home.DifficultyFilter

@get:StringRes
val DifficultyFilter.labelRes: Int
    get() = when (this) {
        DifficultyFilter.EASY -> R.string.difficulty_easy_label
        DifficultyFilter.MEDIUM -> R.string.difficulty_medium_label
        DifficultyFilter.HARD -> R.string.difficulty_hard_label
        DifficultyFilter.ANY -> R.string.difficulty_any_label
    }

@get:StringRes
val DifficultyFilter.subtitleRes: Int
    get() = when (this) {
        DifficultyFilter.EASY -> R.string.difficulty_easy_subtitle
        DifficultyFilter.MEDIUM -> R.string.difficulty_medium_subtitle
        DifficultyFilter.HARD -> R.string.difficulty_hard_subtitle
        DifficultyFilter.ANY -> R.string.difficulty_any_subtitle
    }

@get:StringRes
val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    }

@get:StringRes
val AppLanguage.labelRes: Int
    get() = when (this) {
        AppLanguage.RU -> R.string.language_russian
        AppLanguage.EN -> R.string.language_english
    }