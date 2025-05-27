package com.beginning.tugasakhirpam.features.quiz.model

import com.google.gson.JsonDeserializationContext
import java.lang.reflect.Type
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException

enum class QuizStatusEnum {
    OPENED,
    CLOSED;

    companion object {
        fun fromString(value: String?): QuizStatusEnum {
            return when (value?.lowercase()) {
                "opened" -> OPENED
                "closed" -> CLOSED
                else -> CLOSED
            }
        }
    }
}