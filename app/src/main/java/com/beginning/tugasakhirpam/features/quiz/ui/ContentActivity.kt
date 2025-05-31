package com.beginning.tugasakhirpam.features.quiz.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.beginning.tugasakhirpam.databinding.ActivityContentBinding
import com.beginning.tugasakhirpam.features.quiz.adapter.QuizContentAdapter
import com.beginning.tugasakhirpam.features.quiz.model.AnswerChoice
import com.beginning.tugasakhirpam.features.user.model.QuizHistory
import com.beginning.tugasakhirpam.features.quiz.model.Question
import com.beginning.tugasakhirpam.features.user.repository.UserRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.properties.Delegates

class ContentActivity : AppCompatActivity() {
    private lateinit var contentBinding: ActivityContentBinding
    private lateinit var contentAdapter: QuizContentAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val questionsList = mutableListOf<Question>()
    private var pages by Delegates.notNull<Int>()
    private var currPage by Delegates.notNull<Int>()

    companion object {
        const val EXTRA_CONTENTS = "extra_contents"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentBinding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(contentBinding.root)

        contentAdapter = QuizContentAdapter()
        contentBinding.rvContent.adapter = contentAdapter

        val userRepository = UserRepository()
        val quizId = intent.getStringExtra("QUIZ_ID") ?: ""
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val quizTitle = intent.getStringExtra("QUIZ_TITLE") ?: ""

        pages = 0;
        currPage = 1;
        loadQuestionsFromFirebase(quizId)
        showPageChange(questionsList)
        setupRecyclerViewScrollListener()

        contentBinding.btnSubmit.setOnClickListener {
            Log.d("QUIZ", "Submit clicked")
            val correctCount = countCorrectAnswers()
            val result = (correctCount.toFloat() / pages.toFloat() * 100).toInt()
            val answeredQuestions = countSelectedAnswers()

            Toast.makeText(this, "You got $correctCount correct!", Toast.LENGTH_LONG).show()

            val userQuizResult = createHistory(
                quizId = quizId,
                quizTitle = quizTitle,
                answeredQuestions = answeredQuestions,
                correctAnswers = correctCount,
                score = result,
                totalQuestions = pages
            )

            userRepository.addQuizHistory(userQuizResult) { success ->
                if (success) {
                    Toast.makeText(this, "History saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save history", Toast.LENGTH_SHORT).show()
                }
            }

            val intent = Intent(this, ScoreActivity::class.java).apply {
                putExtra("QUIZ_ID", quizId)
                putExtra("USER_ID", userId)
                putExtra("RESULT", result)
            }
            startActivity(intent)
            finish()

        }
    }

    private fun loadQuestionsFromFirebase(quizId: String) {
        val database = FirebaseDatabase.getInstance()
        val quizRef = database.getReference("quizzes").child(quizId).child("questions")

        quizRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                questionsList.clear()
                pages = 0;

                for (questionSnapshot in snapshot.children) {
                    val body = questionSnapshot.child("body").getValue(String::class.java)

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
                    pages++;
                }

                contentAdapter.setData(questionsList)
                contentBinding.tvQuizPage.text = "$currPage/$pages"
            }

            override fun onCancelled(error: DatabaseError) {
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

    private fun setupRecyclerViewScrollListener() {
        contentBinding.rvContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                currPage = firstVisibleItemPosition + 1
                contentBinding.tvQuizPage.setText("$currPage/$pages")
            }
        })
    }

    private fun countCorrectAnswers(): Int {
        return questionsList.count { question ->
            question.answerChoices?.any { it.isClick == true && it.isCorrect == true } == true
        }
    }

    private fun createHistory(quizId: String,
                              quizTitle: String,
                              answeredQuestions: Int,
                              correctAnswers: Int,
                              score: Int,
                              totalQuestions: Int
                              ): QuizHistory {
        val accuracy = score
        val completionRate = answeredQuestions/totalQuestions * 100
        return QuizHistory.create(accuracy, answeredQuestions, completionRate, correctAnswers, quizId, quizTitle, score, totalQuestions)
    }

    private fun countSelectedAnswers(): Int {
        return questionsList.count { question ->
            question.answerChoices?.any { it.isClick == true } == true
        }
    }

}