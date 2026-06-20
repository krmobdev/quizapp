package com.rustam.quizapp.data

enum class ThemeMode(val label: String) {
    SYSTEM("Системная"),
    LIGHT("Светлая"),
    DARK("Тёмная");

    companion object {
        fun fromStored(value: String?): ThemeMode =
            entries.find { it.name == value } ?: SYSTEM
    }
}