package com.beginning.tugasakhirpam.features.user.model

data class QuizHistory(
    val id: Int,
    val accuracy: Int,
    val answeredQuestions: Int,
    val completionRate: Int,
    val correctAnswers: Int,
    val quizId: Int,
    val quizTitle: String,
    val score: Int,
    val submittedDate: String,
    val totalQuestions: Int
)
