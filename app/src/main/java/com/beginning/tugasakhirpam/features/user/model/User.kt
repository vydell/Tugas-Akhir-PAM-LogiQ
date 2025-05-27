package com.beginning.tugasakhirpam.features.user.model

import com.beginning.tugasakhirpam.features.user.model.QuizHistory

data class User(
    val profile: UserProfile,
    val quizHistory: Map<String, QuizHistory> = emptyMap()
)
