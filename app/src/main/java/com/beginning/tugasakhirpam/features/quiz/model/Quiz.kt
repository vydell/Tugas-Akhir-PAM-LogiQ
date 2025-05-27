package com.beginning.tugasakhirpam.features.quiz.model

data class Quiz(
    var id: String,
    var title: String? = null,
    var description: String? = null,
    var status: QuizStatusEnum? = null,
    var openDate: String? = null,
    var closeDate: String? = null,
    var questions: Map<String, Question>? = null
)
