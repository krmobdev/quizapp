package com.rustam.quizapp.data

import java.util.Locale

enum class AppLanguage(val tag: String) {
    RU("ru"),
    EN("en");

    companion object {
        fun fromStored(value: String?): AppLanguage =
            entries.find { it.name == value } ?: defaultForSystem()

        fun defaultForSystem(): AppLanguage =
            if (Locale.getDefault().language == "en") EN else RU
    }
}