package com.beginning.tugasakhirpam.features.history.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beginning.tugasakhirpam.R
import com.beginning.tugasakhirpam.features.history.adapter.HistoryAdapter
import com.beginning.tugasakhirpam.features.user.model.QuizHistory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryFragment : Fragment(R.layout.activity_history) {

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var totalQuizText: TextView
    private lateinit var totalScoreText: TextView

    private var historyList = mutableListOf<QuizHistory>()
    private lateinit var database: DatabaseReference

    companion object {
        private const val TAG = "HistoryFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getString("USER_ID")
        if (userId != null) {
            setupFirebase(userId)
            loadHistoryFromFirebase()
        } else {
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
        }

        initViews(view)
        setupRecyclerView()
        loadHistoryFromFirebase()
        Log.d(TAG, "Loading history for user: $userId")
    }

    private fun initViews(view: View) {
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView)
        totalQuizText = view.findViewById(R.id.totalQuizText)
        totalScoreText = view.findViewById(R.id.totalScoreText)
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(historyList) { history -> onHistoryItemClicked(history) }
        historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun setupFirebase(userId: String) {
        database = FirebaseDatabase.getInstance(
            "https://projek-akhir-pam-66797-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("users").child(userId).child("quizHistory")
    }

    private fun loadHistoryFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                historyList.clear()

                if (!snapshot.exists()) {
                    updateSummary()
                    historyAdapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "No quiz history found", Toast.LENGTH_LONG)
                        .show()
                    return
                }

                for (historySnapshot in snapshot.children) {
                    try {
                        val historyMap = historySnapshot.value as? Map<*, *> ?: continue

                        val history = QuizHistory(
                            id = parseIntValue(historyMap["id"]),
                            quizId = historyMap["quizId"].toString(),
                            quizTitle = historyMap["quizTitle"]?.toString() ?: "Unknown Quiz",
                            score = parseIntValue(historyMap["score"]),
                            accuracy = parseIntValue(historyMap["accuracy"]),
                            completionRate = parseIntValue(historyMap["completionRate"]),
                            submittedDate = historyMap["submittedDate"]?.toString() ?: "",
                            totalQuestions = parseIntValue(historyMap["totalQuestions"]),
                            correctAnswers = parseIntValue(historyMap["correctAnswers"]),
                            answeredQuestions = parseIntValue(historyMap["answeredQuestions"])
                        )

                        historyList.add(history)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing quiz history: ${e.message}", e)
                    }
                }

                historyList.sortByDescending { it.submittedDate }
                updateSummary()
                historyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
                Toast.makeText(
                    requireContext(),
                    "Database error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun parseIntValue(value: Any?): Int {
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
    }

    private fun updateSummary() {
        totalQuizText.text = historyList.size.toString()
        totalScoreText.text = historyList.sumOf { it.score }.toString()
    }

    private fun onHistoryItemClicked(history: QuizHistory) {
        val message = """
            Quiz: ${history.quizTitle}
            Score: ${history.score}
            Accuracy: ${history.accuracy}%
            Completion: ${history.completionRate}%
            Correct: ${history.correctAnswers}/${history.totalQuestions}
            Date: ${history.submittedDate}
        """.trimIndent()
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}