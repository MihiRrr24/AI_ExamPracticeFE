package com.example.exampratice

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.exampratice.api.ApiClient
import com.example.exampratice.databinding.ActivityQuizConfigurationBinding
import com.example.exampratice.models.GenerateQuestionsRequest
import com.example.exampratice.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class QuizConfigurationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityQuizConfigurationBinding
    private lateinit var preferenceManager: PreferenceManager
    
    private var selectedSubject: String = ""
    private var selectedTopic: String = ""
    private var selectedCount: Int = 10
    private var selectedTimePerQuestion: Int = 60
    private var selectedDifficulty: String = "medium"
    private var isSubjectSelectable: Boolean = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager(this)
        selectedSubject = intent.getStringExtra("subject") ?: ""
        isSubjectSelectable = selectedSubject.isEmpty()
        
        if (isSubjectSelectable) {
            selectedSubject = "General Knowledge" // Default selection
        }
        
        setupViews()
    }
    
    private fun setupViews() {
        setupSubjectSelection()
        updateSubjectDisplay()
        
        // Set up text watchers for real-time updates
        binding.etQuestionCount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val count = s.toString().toIntOrNull()
                if (count != null && count > 0 && count <= 1000) {
                    selectedCount = count
                    updateTotalTime()
                } else if (count != null && count > 1000) {
                    binding.etQuestionCount.error = "Maximum 1000 questions allowed"
                } else if (s.toString().isNotEmpty()) {
                    binding.etQuestionCount.error = "Please enter a valid number"
                }
            }
        })
        
        binding.etTimePerQuestion.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val time = s.toString().toIntOrNull()
                if (time != null && time > 0 && time <= 600) {
                    selectedTimePerQuestion = time
                    updateTotalTime()
                } else if (time != null && time > 600) {
                    binding.etTimePerQuestion.error = "Maximum 600 seconds (10 minutes) per question"
                } else if (s.toString().isNotEmpty()) {
                    binding.etTimePerQuestion.error = "Please enter a valid number"
                }
            }
        })
        
        binding.etTopic.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                selectedTopic = s.toString().trim()
            }
        })
        
        // Set up difficulty buttons
        setupDifficultyButtons()
        
        binding.btnStartQuiz.setOnClickListener {
            startQuiz()
        }
        
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        updateTotalTime()
    }
    
    private fun setupSubjectSelection() {
        if (isSubjectSelectable) {
            // Show subject selection dropdown
            binding.spinnerSubject.visibility = View.VISIBLE
            binding.tvSelectedSubject.visibility = View.GONE
            
            // Set up subject spinner
            val subjects = arrayOf(
                "Mathematics",
                "Science", 
                "History",
                "Geography",
                "English",
                "Computer Science",
                "Physics",
                "Chemistry",
                "Biology",
                "General Knowledge"
            )
            
            val adapter = android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                subjects
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSubject.adapter = adapter
            
            // Set default selection
            binding.spinnerSubject.setSelection(subjects.indexOf(selectedSubject))
            
            // Handle subject selection
            binding.spinnerSubject.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    selectedSubject = subjects[position]
                    updateSubjectDisplay()
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        } else {
            // Hide subject selection dropdown
            binding.spinnerSubject.visibility = View.GONE
            binding.tvSelectedSubject.visibility = View.VISIBLE
        }
    }
    
    private fun updateSubjectDisplay() {
        if (!isSubjectSelectable) {
            binding.tvSelectedSubject.text = "Subject: $selectedSubject"
        }
    }
    
    private fun setupDifficultyButtons() {
        binding.btnEasy.setOnClickListener { selectDifficulty("easy") }
        binding.btnMedium.setOnClickListener { selectDifficulty("medium") }
        binding.btnHard.setOnClickListener { selectDifficulty("hard") }
        
        selectDifficulty("medium")
    }

    private fun selectDifficulty(difficulty: String) {
        selectedDifficulty = difficulty

        val newTimePerQuestion = when (difficulty) {
            "easy" -> 30
            "hard" -> 90
            else -> 60
        }

        selectedTimePerQuestion = newTimePerQuestion

        binding.etTimePerQuestion.setText(newTimePerQuestion.toString())

        resetButtonStyle(binding.btnEasy, R.color.accent_green)
        resetButtonStyle(binding.btnMedium, R.color.accent_orange)
        resetButtonStyle(binding.btnHard, R.color.accent_red)

        when (difficulty) {
            "easy" -> {
                setSelectedButtonStyle(binding.btnEasy, R.color.accent_green)
                binding.tvDifficultyDescription.text = "Easy: Simple and straightforward questions for beginners"
            }
            "medium" -> {
                setSelectedButtonStyle(binding.btnMedium, R.color.accent_orange)
                binding.tvDifficultyDescription.text = "Medium: Moderately challenging questions with standard concepts"
            }
            "hard" -> {
                setSelectedButtonStyle(binding.btnHard, R.color.accent_red)
                binding.tvDifficultyDescription.text = "Hard: Advanced and complex questions requiring deep understanding"
            }
        }

        updateTotalTime()
    }
    
    private fun updateTotalTime() {
        val totalTimeSeconds = selectedCount * selectedTimePerQuestion
        val totalTimeMinutes = totalTimeSeconds / 60
        val remainingSeconds = totalTimeSeconds % 60
        
        binding.tvTotalTime.text = "Total Exam Time: ${totalTimeMinutes}m ${remainingSeconds}s"
        binding.tvQuestionTime.text = "Per Question: ${selectedTimePerQuestion}s"
        binding.tvQuestionCount.text = "Total Questions: $selectedCount"
    }
    
    private fun startQuiz() {
        // Validate subject (when selectable)
        if (isSubjectSelectable && selectedSubject.isEmpty()) {
            Toast.makeText(this, "Please select a subject", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validate topic
        if (selectedTopic.isEmpty()) {
            binding.etTopic.error = "Please enter a topic"
            Toast.makeText(this, "Please enter a topic", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validate question count
        if (selectedCount <= 0 || selectedCount > 1000) {
            binding.etQuestionCount.error = "Please enter 1-1000 questions"
            Toast.makeText(this, "Please enter a valid number of questions (1-1000)", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validate time per question
        if (selectedTimePerQuestion <= 0 || selectedTimePerQuestion > 600) {
            binding.etTimePerQuestion.error = "Please enter 1-600 seconds"
            Toast.makeText(this, "Please enter valid time per question (1-600 seconds)", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra("subject", selectedSubject)
            putExtra("topic", selectedTopic)
            putExtra("count", selectedCount)
            putExtra("timePerQuestion", selectedTimePerQuestion)
            putExtra("difficulty", selectedDifficulty)
            putExtra("totalTime", selectedCount * selectedTimePerQuestion)
        }
        startActivity(intent)
        finish()
    }
    
    private fun resetButtonStyle(button: MaterialButton, colorRes: Int) {
        button.apply {
            // Reset to outlined style
            backgroundTintList = null
            strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@QuizConfigurationActivity, colorRes))
            strokeWidth = 2
            setTextColor(ContextCompat.getColor(this@QuizConfigurationActivity, colorRes))
        }
    }
    
    private fun setSelectedButtonStyle(button: MaterialButton, colorRes: Int) {
        button.apply {
            // Set to filled style
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@QuizConfigurationActivity, colorRes))
            strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@QuizConfigurationActivity, colorRes))
            strokeWidth = 0
            setTextColor(ContextCompat.getColor(this@QuizConfigurationActivity, R.color.white))
        }
    }
}