package com.beginning.tugasakhirpam.features.quiz.model

data class AnswerChoice(
    var text: String? = null,
    var isCorrect: Boolean? = null,
    var isClick: Boolean?
) {
    constructor() : this(null, null, null)
}
