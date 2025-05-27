package com.beginning.tugasakhirpam.data

import com.beginning.tugasakhirpam.features.quiz.model.Quiz
import com.beginning.tugasakhirpam.features.user.model.User

data class QuizDatabase(
    val users: Map<String, User> = emptyMap(),
    val quizzes: Map<String, Quiz> = emptyMap()
)
