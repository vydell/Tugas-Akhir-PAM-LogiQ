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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        quizId = intent.getStringExtra("QUIZ_ID") ?: ""
        val quizDescription = intent.getStringExtra("QUIZ_DESCRIPTION") ?: ""

        // Set up UI
        binding.apply {
            tvQuizDesc.text = quizDescription
        }

        binding.btnPlay.setOnClickListener {
            // Navigate to ContentActivity when Play is clicked
            val intent = Intent(this, ContentActivity::class.java).apply {
                putExtra("QUIZ_ID", quizId)
            }
            startActivity(intent)
            finish()
        }
    }
}