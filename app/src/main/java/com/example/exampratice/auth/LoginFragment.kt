package com.example.exampratice.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.exampratice.AuthActivity
import com.example.exampratice.R
import com.example.exampratice.api.ApiClient
import com.example.exampratice.databinding.FragmentLoginBinding
import com.example.exampratice.models.LoginRequest
import com.example.exampratice.models.VerifyOtpRequest
import com.example.exampratice.utils.PreferenceManager
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener { performLogin() }
        binding.tvRegisterLink.setOnClickListener {
            (activity as AuthActivity).showRegisterFragment()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (!validateInput(email, password)) return

        loginUser(email, password)
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.tilEmail.error = "Email is required"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Enter valid email"
                false
            }
            password.isEmpty() -> {
                binding.tilPassword.error = "Password is required"
                false
            }
            else -> {
                binding.tilEmail.error = null
                binding.tilPassword.error = null
                true
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = ApiClient.authService.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    val body = response.body()

                    when {
                        body?.token != null -> {
                            saveLoginData(body.token, body.user?.name ?: "", email)
                            (activity as AuthActivity).navigateToMain()
                        }

                        body?.message?.contains("OTP sent", ignoreCase = true) == true -> {
                            showOtpDialog(email)
                        }

                        else -> showError(body?.message ?: "Login failed")
                    }
                } else showError("Invalid credentials")

            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
            }
        }
    }

    private fun showOtpDialog(email: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_otp_verification, null)
        val etOtp = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etOtp)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("OTP Verification")
            .setMessage("Enter the 6-digit OTP sent to your email")
            .setView(dialogView)
            .setPositiveButton("Verify", null)
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            val btn = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val otp = etOtp.text.toString().trim()
                if (otp.length == 6) verifyOtp(email, otp, dialog)
                else etOtp.error = "Enter valid 6-digit OTP"
            }
        }

        dialog.show()
    }

    private fun verifyOtp(email: String, otp: String, dialog: androidx.appcompat.app.AlertDialog) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.authService.verifyOtp(VerifyOtpRequest(email, otp))

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.token != null) {
                        saveLoginData(body.token, body.user?.name ?: "", email)
                        dialog.dismiss()
                        (activity as AuthActivity).navigateToMain()
                    } else showError("OTP verification failed")

                } else showError("Invalid OTP")

            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            }
        }
    }

    private fun saveLoginData(token: String, name: String, email: String) {
        preferenceManager.saveAuthToken(token)
        preferenceManager.saveUserName(name)
        preferenceManager.saveUserEmail(email)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
