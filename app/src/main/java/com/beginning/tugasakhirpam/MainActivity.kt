package com.beginning.tugasakhirpam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.beginning.tugasakhirpam.features.history.ui.HistoryFragment
import com.beginning.tugasakhirpam.features.homepage.ui.HomepageFragment
import com.beginning.tugasakhirpam.features.user.ui.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userEmail = intent.getStringExtra("USER_EMAIL")
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val userName = intent.getStringExtra("USER_NAME")

        bottomNavigation = findViewById(R.id.bottomNavigation)

        val bundle = Bundle().apply {
            putString("USER_NAME", userName)
            putString("USER_EMAIL", userEmail)
            putString("USER_ID", userId)
        }

        if (savedInstanceState == null) {
            val homepageFragment = HomepageFragment().apply {
                arguments = bundle
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, homepageFragment)
                .commit()
            bottomNavigation.selectedItemId = R.id.nav_home
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomepageFragment()
                R.id.nav_profile -> ProfileFragment()
                R.id.nav_history -> HistoryFragment()
                else -> null
            }

            fragment?.arguments = bundle

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, it)
                    .commit()
                true
            } ?: false
        }
    }
}