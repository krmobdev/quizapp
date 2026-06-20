package com.rustam.quizapp.data

import androidx.annotation.StringRes

data class Category(
    val id: String,
    @param:StringRes val titleRes: Int,
    val emoji: String
)