package com.example.exampratice

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.exampratice.utils.PreferenceManager

class SplashActivity : AppCompatActivity() {
    
    private companion object {
        const val SPLASH_DELAY = 2000L
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserLoginStatus()
        }, SPLASH_DELAY)
    }
    
    private fun checkUserLoginStatus() {
        val preferenceManager = PreferenceManager(this)
        val intent = if (preferenceManager.isLoggedIn()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, AuthActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}