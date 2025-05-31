package com.beginning.tugasakhirpam.features.quiz.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.beginning.tugasakhirpam.R
import com.beginning.tugasakhirpam.databinding.ActivityContentBinding
import com.beginning.tugasakhirpam.databinding.ActivityQuizBinding

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var quizId: String
    private lateinit var quizTitle: String
    private lateinit var quizDescription: String
    private lateinit var userId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        quizId = intent.getStringExtra("QUIZ_ID") ?: ""
        quizDescription = intent.getStringExtra("QUIZ_DESCRIPTION") ?: ""
        userId = intent.getStringExtra("USER_ID") ?: ""
        quizTitle = intent.getStringExtra("QUIZ_TITLE") ?: ""

        binding.apply {
            tvQuizDesc.text = quizDescription
        }

        binding.btnPlay.setOnClickListener {
            val intent = Intent(this, ContentActivity::class.java).apply {
                putExtra("QUIZ_ID", quizId)
                putExtra("USER_ID", userId)
                putExtra("QUIZ_TITLE", quizTitle)
            }
            startActivity(intent)
            finish()
        }
    }
}