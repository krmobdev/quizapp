package com.rustam.quizapp.data

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromStored(value: String?): ThemeMode =
            entries.find { it.name == value } ?: SYSTEM
    }
}