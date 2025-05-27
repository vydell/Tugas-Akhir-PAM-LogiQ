package com.beginning.tugasakhirpam.features.quiz.model

data class Question(
    var body: String? = null,
    var answerChoices: List<AnswerChoice>? = null
) {
    constructor() : this(null, null)
}
