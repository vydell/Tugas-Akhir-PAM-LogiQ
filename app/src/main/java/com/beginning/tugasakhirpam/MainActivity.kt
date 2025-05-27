package com.beginning.tugasakhirpam

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.beginning.tugasakhirpam.features.history.ui.HistoryActivity
import com.beginning.tugasakhirpam.features.homepage.ui.HomepageActivity
import com.beginning.tugasakhirpam.features.user.ui.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Load default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomepageActivity())
                .commit()

            bottomNavigation.selectedItemId = R.id.nav_home
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomepageActivity()
                R.id.nav_profile -> ProfileActivity()
                R.id.nav_history -> HistoryActivity()
                else -> null
            }
            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, it as Fragment)
                    .commit()
                true
            } == true
        }
    }
}