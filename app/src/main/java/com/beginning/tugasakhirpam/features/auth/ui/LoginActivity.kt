package com.beginning.tugasakhirpam.features.auth.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beginning.tugasakhirpam.MainActivity
import com.beginning.tugasakhirpam.R
import com.beginning.tugasakhirpam.features.homepage.ui.HomepageFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val btnLogin: Button = findViewById(R.id.btnLogin)
        val etEmail: EditText = findViewById(R.id.etEmail)
        val etPassword: EditText = findViewById(R.id.etPassword)
        val tvRegister: TextView = findViewById(R.id.tvRegister)

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val userRef = FirebaseDatabase.getInstance(
                            "https://projek-akhir-pam-66797-default-rtdb.asia-southeast1.firebasedatabase.app"
                        ).getReference("users").child(userId)

                        userRef.get().addOnSuccessListener { snapshot ->
                            val userName = snapshot.child("profile").child("username").getValue(String::class.java)
                            val userEmail = snapshot.child("profile").child("email").getValue(String::class.java)

                            val intent = Intent(this, MainActivity::class.java).apply {
                                putExtra("USER_EMAIL", userEmail)
                                putExtra("USER_ID", userId)
                                putExtra("USER_NAME", userName)
                            }
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to get user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}