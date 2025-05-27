package com.beginning.tugasakhirpam.features.homepage.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beginning.tugasakhirpam.R
import com.beginning.tugasakhirpam.features.homepage.adapter.HomepageQuizAdapter
import com.beginning.tugasakhirpam.features.quiz.model.Quiz
import com.beginning.tugasakhirpam.features.quiz.model.QuizStatusEnum
import com.beginning.tugasakhirpam.features.quiz.ui.QuizActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomepageActivity : Fragment(R.layout.activity_homepage) {

    private lateinit var quizRecyclerView: RecyclerView
    private lateinit var quizAdapter: HomepageQuizAdapter
    private lateinit var searchEditText: EditText

    private var quizList = mutableListOf<Quiz>()
    private var filteredQuizList = mutableListOf<Quiz>()

    private lateinit var database: DatabaseReference

    companion object {
        private const val TAG = "HomepageFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_homepage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        quizRecyclerView = view.findViewById(R.id.quizRecyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)

        setupRecyclerView()
        setupSearchFunctionality()

        database = FirebaseDatabase.getInstance(
            "https://projek-akhir-pam-66797-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("quizzes")

        loadQuizDataFromFirebase()
    }

    private fun setupRecyclerView() {
        quizAdapter = HomepageQuizAdapter(filteredQuizList) { quiz -> onQuizItemClicked(quiz) }
        quizRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quizAdapter
        }
    }

    private fun setupSearchFunctionality() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterQuizList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    @SuppressLint("RestrictedApi")
    private fun loadQuizDataFromFirebase() {
        Log.d(TAG, "Loading quizzes...")
        Toast.makeText(requireContext(), "Loading quizzes...", Toast.LENGTH_SHORT).show()

        database.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Database error: ${error.message}", Toast.LENGTH_LONG).show()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                quizList.clear()
                filteredQuizList.clear()

                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(), "No quiz data found", Toast.LENGTH_SHORT).show()
                    return
                }

                for (quizSnapshot in snapshot.children) {
                    try {
                        val quizMap = quizSnapshot.value as? Map<*, *> ?: continue
                        val id = quizSnapshot.key ?: continue

                        val statusString = quizMap["status"] as? String
                        val statusEnum = when (statusString?.uppercase()) {
                            "OPEN", "OPENED" -> QuizStatusEnum.OPENED
                            "CLOSED" -> QuizStatusEnum.CLOSED
                            else -> QuizStatusEnum.CLOSED
                        }

                        val quiz = Quiz(
                            id = id,
                            title = quizMap["title"] as? String ?: "Untitled Quiz",
                            description = quizMap["description"] as? String ?: "", // Load description
                            openDate = quizMap["openDate"] as? String,
                            closeDate = quizMap["closeDate"] as? String,
                            status = statusEnum
                        )
                        quizList.add(quiz)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing quiz: ${e.message}")
                    }
                }

                filteredQuizList.clear()
                filteredQuizList.addAll(quizList)
                quizAdapter.notifyDataSetChanged()
                Log.d(TAG, "Loaded ${quizList.size} quizzes from Firebase")
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterQuizList(searchText: String) {
        filteredQuizList.clear()

        filteredQuizList.addAll(
            if (searchText.isEmpty()) {
                quizList
            } else {
                quizList.filter {
                    it.title?.lowercase()?.contains(searchText.lowercase()) == true
                }
            }
        )
        quizAdapter.notifyDataSetChanged()
    }

    private fun onQuizItemClicked(quiz: Quiz) {
        when (quiz.status) {
            QuizStatusEnum.OPENED -> {
                Toast.makeText(requireContext(), "Opening quiz: ${quiz.title}", Toast.LENGTH_SHORT).show()
                // Navigate to ContentActivity with the quiz ID
                val intent = Intent(requireContext(), QuizActivity::class.java).apply {
                    putExtra("QUIZ_ID", quiz.id)
                    putExtra("QUIZ_TITLE", quiz.title)
                    putExtra("QUIZ_DESCRIPTION", quiz.description)
                }
                startActivity(intent)
            }
            QuizStatusEnum.CLOSED -> {
                Toast.makeText(requireContext(), "Quiz is closed: ${quiz.title}", Toast.LENGTH_SHORT).show()
            }
            null -> {
                Toast.makeText(requireContext(), "Quiz status unknown", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

