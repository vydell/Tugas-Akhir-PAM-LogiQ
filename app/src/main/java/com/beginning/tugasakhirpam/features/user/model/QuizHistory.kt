package com.beginning.tugasakhirpam.features.user.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class QuizHistory(
    val id: Int,
    val accuracy: Int,
    val answeredQuestions: Int,
    val completionRate: Int,
    val correctAnswers: Int,
    val quizId: String,
    val quizTitle: String,
    val score: Int,
    val submittedDate: String,
    val totalQuestions: Int
) {
    companion object {
        fun create(
            accuracy: Int,
            answeredQuestions: Int,
            completionRate: Int,
            correctAnswers: Int,
            quizId: String,
            quizTitle: String,
            score: Int,
            totalQuestions: Int
        ): QuizHistory {
            val generatedId = System.currentTimeMillis().toInt()
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            return QuizHistory(
                id = generatedId,
                accuracy = accuracy,
                answeredQuestions = answeredQuestions,
                completionRate = completionRate,
                correctAnswers = correctAnswers,
                quizId = quizId,
                quizTitle = quizTitle,
                score = score,
                submittedDate = today,
                totalQuestions = totalQuestions
            )
        }
    }
}
