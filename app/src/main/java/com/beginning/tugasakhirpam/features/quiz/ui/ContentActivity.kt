package com.beginning.tugasakhirpam.features.quiz.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.beginning.tugasakhirpam.R
import com.beginning.tugasakhirpam.databinding.ActivityContentBinding
import com.beginning.tugasakhirpam.features.quiz.adapter.QuizContentAdapter
import com.beginning.tugasakhirpam.features.quiz.model.AnswerChoice
import com.beginning.tugasakhirpam.features.quiz.model.Question
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ContentActivity : AppCompatActivity() {

    private lateinit var contentBinding: ActivityContentBinding
    private lateinit var contentAdapter: QuizContentAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val questionsList = mutableListOf<Question>()

    companion object {
        const val EXTRA_USER = ""
        const val EXTRA_CONTENTS = "extra_contents"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentBinding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(contentBinding.root)

        // Get quiz ID from intent
        val quizId = intent.getStringExtra("QUIZ_ID") ?: ""

        contentAdapter = QuizContentAdapter()
        contentBinding.rvContent.adapter = contentAdapter

        loadQuestionsFromFirebase(quizId)
        showPageChange(questionsList)
    }

    private fun loadQuestionsFromFirebase(quizId: String) {
        val database = FirebaseDatabase.getInstance()
        val quizRef = database.getReference("quizzes").child(quizId).child("questions")

        quizRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                questionsList.clear()

                for (questionSnapshot in snapshot.children) {
                    // Get the question body
                    val body = questionSnapshot.child("body").getValue(String::class.java)

                    // Get answer choices
                    val answerChoicesMap = questionSnapshot.child("answerChoices").value as? Map<String, Map<String, Any>>
                    val answerChoices = mutableListOf<AnswerChoice>()

                    answerChoicesMap?.forEach { (choiceKey, choiceValue) ->
                        answerChoices.add(
                            AnswerChoice(
                                text = choiceValue["text"] as? String,
                                isCorrect = choiceValue["isCorrect"] as? Boolean,
                                isClick = false
                            )
                        )
                    }

                    questionsList.add(
                        Question(
                            body = body,
                            answerChoices = answerChoices
                        )
                    )
                }

                contentAdapter.setData(questionsList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                error.toException().printStackTrace()
            }
        })
    }

    private fun showPageChange(contents: List<Question>?) {
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val snapHelper = PagerSnapHelper()

        if (contents != null) {
            contentAdapter.setData(contents as MutableList<Question>)
        }

        snapHelper.attachToRecyclerView(contentBinding.rvContent)
        contentBinding.rvContent.layoutManager = layoutManager
        contentBinding.rvContent.adapter = contentAdapter

    }
}