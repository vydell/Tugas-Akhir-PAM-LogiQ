package com.beginning.tugasakhirpam.features.user.repository

import com.beginning.tugasakhirpam.features.user.model.QuizHistory
import com.beginning.tugasakhirpam.features.user.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class UserRepository@Inject constructor() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    private lateinit var userRepository: UserRepository
    fun getCurrentUser(): UserProfile? {
        val user = auth.currentUser ?: return null
        return UserProfile(
            userId = user.uid,
            email = user.email ?: "",
            username = user.displayName ?: "Anonymous",
            profileImageUrl = user.photoUrl?.toString().toString()
        )
    }

    fun initializeUserProfile(profile: UserProfile, onComplete: (Boolean) -> Unit) {
        val userRef = database.child(profile.userId)
        userRef.child("profile").setValue(profile)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun addQuizHistory(quizHistory: QuizHistory, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            onComplete(false)
            return
        }

        database.child(userId).child("quizHistory").child(quizHistory.quizId)
            .setValue(quizHistory)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun getUserQuizHistory(onSuccess: (Map<String, QuizHistory>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            onFailure(Exception("User not authenticated"))
            return
        }

        database.child(userId).child("quizHistory").get()
            .addOnSuccessListener { snapshot ->
                val history = snapshot.children.mapNotNull {
                    it.getValue(QuizHistory::class.java)?.let { history ->
                        it.key to history
                    }
                }.toMap()
                onSuccess(history as Map<String, QuizHistory>)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}