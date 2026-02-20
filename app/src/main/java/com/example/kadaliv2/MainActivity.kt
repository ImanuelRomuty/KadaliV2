package com.example.kadaliv2

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // Removed bottom padding for nav bar
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as androidx.navigation.fragment.NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)

        // Remove default Material3 active indicator and ripple
        bottomNav.isItemActiveIndicatorEnabled = false
        bottomNav.itemRippleColor = ColorStateList.valueOf(Color.TRANSPARENT)
        
        setSupportActionBar(toolbar)

        // Setup AppBarConfiguration with top-level destinations
        val appBarConfiguration = androidx.navigation.ui.AppBarConfiguration(
            setOf(R.id.dashboardFragment, R.id.simulationFragment, R.id.settingsFragment)
        )
        
        androidx.navigation.ui.NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)
        androidx.navigation.ui.NavigationUI.setupWithNavController(bottomNav, navController)
        
        // Hide bottom nav on specific fragments if needed
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.dashboardFragment, R.id.simulationFragment, R.id.settingsFragment -> {
                    bottomNav.visibility = android.view.View.VISIBLE
                }
                else -> {
                    bottomNav.visibility = android.view.View.GONE
                }
            }
        }
    }
}