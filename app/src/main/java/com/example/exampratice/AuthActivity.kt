package com.example.exampratice

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.exampratice.auth.LoginFragment
import com.example.exampratice.auth.RegisterFragment

class AuthActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        
        if (savedInstanceState == null) {
            showLoginFragment()
        }
    }
    
    fun showLoginFragment() {
        replaceFragment(LoginFragment())
    }
    
    fun showRegisterFragment() {
        replaceFragment(RegisterFragment())
    }
    
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.authContainer, fragment)
            .commit()
    }
    
    fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}