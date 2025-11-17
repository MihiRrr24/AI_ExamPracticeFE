package com.example.exampratice.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.exampratice.AuthActivity
import com.example.exampratice.R
import com.example.exampratice.api.ApiClient
import com.example.exampratice.databinding.FragmentProfileBinding
import com.example.exampratice.models.*
import com.example.exampratice.utils.PreferenceManager
import com.example.exampratice.utils.ThemeManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var themeManager: ThemeManager
    private var allRecentQuizzes = listOf<RecentQuiz>()
    private var showingAllQuizzes = false
    private var isUpdatingUI = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferenceManager = PreferenceManager(requireContext())
        themeManager = ThemeManager(requireContext())
        
        setupViews()
        loadUserProfile()
        loadNotificationSettings()
        updateThemeUI()
    }
    
    private fun loadNotificationSettings() {
        lifecycleScope.launch {
            try {
                val token = preferenceManager.getToken()
                val response = ApiClient.userService.getNotifications("Bearer $token")
                
                if (response.isSuccessful && response.body() != null) {
                    // Store notification settings locally if needed
                    // You can store these in preferences if needed for offline access
                }
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }
    
    private fun updateThemeUI() {
        isUpdatingUI = true
        val isDarkMode = themeManager.isDarkMode()
        binding.switchTheme.isChecked = isDarkMode
        binding.tvThemeText.text = if (isDarkMode) "Dark Mode" else "Light Mode"
        binding.ivThemeIcon.setImageResource(
            if (isDarkMode) R.drawable.ic_dark_mode else R.drawable.ic_light_mode
        )
        isUpdatingUI = false
    }
    
    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        
        // Pre-fill current values
        etName.setText(binding.tvUserName.text)
        etEmail.setText(binding.tvUserEmail.text)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etName.text.toString().trim()
                val newEmail = etEmail.text.toString().trim()
                
                if (newName.isNotEmpty() && newEmail.isNotEmpty()) {
                    updateProfile(newName, newEmail)
                } else {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrentPassword = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = etCurrentPassword.text.toString()
                val newPassword = etNewPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()
                
                when {
                    currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                    newPassword != confirmPassword -> {
                        Toast.makeText(context, "New passwords don't match", Toast.LENGTH_SHORT).show()
                    }
                    newPassword.length < 6 -> {
                        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        changePassword(currentPassword, newPassword)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showNotificationSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_notification_settings, null)
        val switchQuizReminders = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchQuizReminders)
        val switchDailyGoals = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchDailyGoals)
        val switchWeeklyReports = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchWeeklyReports)
        val switchAchievements = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchAchievements)
        
        // Load current settings
        lifecycleScope.launch {
            try {
                val token = preferenceManager.getToken()
                val response = ApiClient.userService.getNotifications("Bearer $token")
                
                if (response.isSuccessful && response.body() != null) {
                    val settings = response.body()!!
                    switchQuizReminders.isChecked = settings.quizReminders
                    switchDailyGoals.isChecked = settings.dailyGoals
                    switchWeeklyReports.isChecked = settings.weeklyReports
                    switchAchievements.isChecked = settings.achievements
                }
            } catch (e: Exception) {
                // Use defaults
            }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Notification Settings")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newSettings = NotificationSettings(
                    quizReminders = switchQuizReminders.isChecked,
                    dailyGoals = switchDailyGoals.isChecked,
                    weeklyReports = switchWeeklyReports.isChecked,
                    achievements = switchAchievements.isChecked
                )
                updateNotificationSettings(newSettings)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun toggleTwoFactor() {
        lifecycleScope.launch {
            try {
                val token = preferenceManager.getToken()
                println("DEBUG: Toggling 2FA with token: ${token?.take(10)}...")
                
                val response = ApiClient.userService.toggle2FA("Bearer $token")
                println("DEBUG: 2FA toggle response code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val message = response.body()!!.message
                    println("DEBUG: 2FA toggle message: $message")
                    
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    
                    // Reload profile to get updated 2FA status
                    loadUserProfile()
                } else {
                    println("DEBUG: 2FA toggle failed: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Failed to toggle 2FA", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                println("DEBUG: 2FA toggle error: ${e.message}")
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun toggleTheme() {
        val oldMode = themeManager.isDarkMode()
        println("DEBUG: Current theme mode: $oldMode")
        
        val isDarkMode = themeManager.toggleDarkMode()
        println("DEBUG: New theme mode: $isDarkMode")
        
        updateThemeUI()
        
        // Show confirmation message
        val message = if (isDarkMode) "Dark mode enabled" else "Light mode enabled"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        
        // Apply theme change immediately by recreating activity
        requireActivity().recreate()
    }
    
    private fun updateProfile(name: String, email: String) {
        lifecycleScope.launch {
            try {
                val token = preferenceManager.getToken()
                val request = UpdateProfileRequest(name = name, email = email)
                val response = ApiClient.userService.updateProfile("Bearer $token", request)
                
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    
                    // Update UI
                    binding.tvUserName.text = name
                    binding.tvUserEmail.text = email
                    
                    // Update stored preferences
                    preferenceManager.saveUserName(name)
                    preferenceManager.saveUserEmail(email)
                } else {
                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun changePassword(currentPassword: String, newPassword: String) {
        lifecycleScope.launch {
            try {
                val token = preferenceManager.getToken()
                val request = ChangePasswordRequest(currentPassword, newPassword)
                val response = ApiClient.userService.changePassword("Bearer $token", request)
                
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to change password", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateNotificationSettings(settings: NotificationSettings) {
        lifecycleScope.launch {
            try {
                val token = preferenceManager.getToken()
                val response = ApiClient.userService.updateNotifications("Bearer $token", settings)
                
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to update notifications", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupViews() {
        binding.btnLogout.setOnClickListener {
            logout()
        }
        
        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
        
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
        
        binding.btnNotifications.setOnClickListener {
            showNotificationSettingsDialog()
        }
        
        binding.btnTwoFactor.setOnClickListener {
            toggleTwoFactor()
        }
        
        binding.btnTheme.setOnClickListener {
            toggleTheme()
        }
        
        // Allow switches to be clicked directly
        binding.switchTwoFactor.setOnCheckedChangeListener { _, _ ->
            if (!isUpdatingUI) {
                toggleTwoFactor()
            }
        }
        
        binding.switchTheme.setOnCheckedChangeListener { _, _ ->
            if (!isUpdatingUI) {
                toggleTheme()
            }
        }
    }
    
    private fun loadUserProfile() {
        val userName = preferenceManager.getUserName() ?: "Student"
        val userEmail = preferenceManager.getUserEmail() ?: "student@example.com"
        
        binding.tvUserName.text = userName
        binding.tvUserEmail.text = userEmail
        
        // Load additional profile data from API
        lifecycleScope.launch {
            try {
                val token = preferenceManager.getToken()
                val response = ApiClient.userService.getProfile("Bearer $token")
                
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    binding.tvUserName.text = user.name
                    binding.tvUserEmail.text = user.email
                    
                    // Update 2FA switch
                    isUpdatingUI = true
                    binding.switchTwoFactor.isChecked = user.twoFactorEnabled
                    isUpdatingUI = false
                    
                    // Update preferences
                    preferenceManager.saveUserName(user.name)
                    preferenceManager.saveUserEmail(user.email)
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    private fun logout() {
        preferenceManager.clearAll()
        
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}