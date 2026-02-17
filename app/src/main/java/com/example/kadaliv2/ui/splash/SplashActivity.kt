package com.example.kadaliv2.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.kadaliv2.MainActivity
import com.example.kadaliv2.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Keep the splash screen visible for a bit if needed, or just let it transition to this layout.
        // The assignment says "Duration: 1.5 – 2.5 seconds maximum".
        // Use Handler to delay navigation.
        
        // Use Handler to delay navigation.
        
        val btnGetStarted = findViewById<android.widget.Button>(R.id.btn_get_started)
        btnGetStarted.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
