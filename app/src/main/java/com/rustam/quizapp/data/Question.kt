package com.rustam.quizapp.data

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    val category: String,
    val difficulty: Difficulty,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null
)

enum class Difficulty { EASY, MEDIUM, HARD }
