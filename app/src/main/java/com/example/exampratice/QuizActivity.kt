package com.example.exampratice

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exampratice.adapters.QuestionAdapter
import com.example.exampratice.api.ApiClient
import com.example.exampratice.databinding.ActivityQuizBinding
import com.example.exampratice.models.GenerateQuestionsRequest
import com.example.exampratice.models.Question
import com.example.exampratice.models.QuizSubmission
import com.example.exampratice.utils.PreferenceManager
import kotlinx.coroutines.launch

class QuizActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityQuizBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var questionAdapter: QuestionAdapter
    
    private var questions = mutableListOf<Question>()
    private var currentQuestionIndex = 0
    private var userAnswers = mutableListOf<String>()
    private var startTime = 0L
    
    // Dual timer system
    private var examTimer: CountDownTimer? = null
    private var questionTimer: CountDownTimer? = null
    private var totalExamTimeSeconds = 0L
    private var timePerQuestionSeconds = 0L
    private var examTimeRemaining = 0L
    private var questionTimeRemaining = 0L
    
    private var selectedSubject = ""
    private var selectedTopic = ""
    private var selectedCount = 10
    private var selectedTimePerQuestion = 60
    private var selectedDifficulty = "medium"
    
    private var currentTextWatcher: TextWatcher? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager(this)
        
        // Get quiz configuration from intent
        selectedSubject = intent.getStringExtra("subject") ?: "General Knowledge"
        selectedTopic = intent.getStringExtra("topic") ?: ""
        selectedCount = intent.getIntExtra("count", 10)
        selectedTimePerQuestion = intent.getIntExtra("timePerQuestion", 60)
        selectedDifficulty = intent.getStringExtra("difficulty") ?: "medium"
        totalExamTimeSeconds = intent.getIntExtra("totalTime", 600).toLong()
        timePerQuestionSeconds = selectedTimePerQuestion.toLong()
        
        setupViews()
        loadQuestions(selectedSubject, selectedTopic, selectedCount, selectedTimePerQuestion, selectedDifficulty)
    }
    
    private fun setupViews() {
        binding.btnSubmit.setOnClickListener {
            submitQuiz()
        }
        
        binding.btnNext.setOnClickListener {
            nextQuestion()
        }
        
        binding.btnPrevious.setOnClickListener {
            previousQuestion()
        }
    }
    
    private fun loadQuestions(subject: String, topic: String, count: Int, timePerQuestion: Int, difficulty: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.quizContent.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val token = preferenceManager.getToken()
                val response = ApiClient.quizService.generateQuestions(
                    "Bearer $token",
                    GenerateQuestionsRequest(subject, topic, count, timePerQuestion, difficulty)
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val questionSet = response.body()!!
                    questions.addAll(questionSet.questions)
                    userAnswers = MutableList(questions.size) { "" }
                    
                    startQuiz()
                } else {
                    showError("Failed to load questions")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun startQuiz() {
        binding.quizContent.visibility = View.VISIBLE
        startTime = System.currentTimeMillis()
        
        // Initialize timer durations
        examTimeRemaining = totalExamTimeSeconds * 1000
        questionTimeRemaining = timePerQuestionSeconds * 1000
        
        startExamTimer()
        startQuestionTimer()
        
        setupQuestionAdapter()
        showCurrentQuestion()
    }
    
    private fun startExamTimer() {
        examTimer?.cancel()
        examTimer = object : CountDownTimer(examTimeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                examTimeRemaining = millisUntilFinished
                updateExamTimer(millisUntilFinished)
            }
            
            override fun onFinish() {
                submitQuiz()
            }
        }.start()
    }
    
    private fun startQuestionTimer() {
        questionTimer?.cancel()
        questionTimer = object : CountDownTimer(questionTimeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                questionTimeRemaining = millisUntilFinished
                updateQuestionTimer(millisUntilFinished)
            }
            
            override fun onFinish() {
                // Auto-move to next question when time runs out
                moveToNextQuestion()
            }
        }.start()
    }
    
    private fun setupQuestionAdapter() {
        questionAdapter = QuestionAdapter { selectedOption ->
            userAnswers[currentQuestionIndex] = selectedOption
            updateNavigationButtons()
        }
        
        binding.rvOptions.apply {
            adapter = questionAdapter
            layoutManager = LinearLayoutManager(this@QuizActivity)
        }
    }
    
    private fun showCurrentQuestion() {
        val question = questions[currentQuestionIndex]
        
        binding.tvQuestionNumber.text = "Question ${currentQuestionIndex + 1} of ${questions.size}"
        binding.tvQuestion.text = question.question
        binding.progressIndicator.progress = ((currentQuestionIndex + 1) * 100) / questions.size
        
        // Show difficulty level
        val difficultyIcon = when (question.difficulty) {
            "easy" -> "🟢"
            "hard" -> "🔴"
            else -> "🟡"
        }
        binding.tvDifficultyLevel.text = "$difficultyIcon ${question.difficulty.capitalize()} Level"
        
        // Determine question type and show appropriate UI
        val questionType = getQuestionType(question)
        
        when (questionType) {
            "text" -> {
                // Show text input for one-word answers
                binding.rvOptions.visibility = View.GONE
                binding.cardTextInput.visibility = View.VISIBLE
                
                // Remove previous text watcher
                currentTextWatcher?.let { binding.etAnswer.removeTextChangedListener(it) }
                
                // Set text without triggering listeners
                binding.etAnswer.setText(userAnswers[currentQuestionIndex])
                
                // Create and add new text watcher
                currentTextWatcher = object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        userAnswers[currentQuestionIndex] = s.toString().trim()
                    }
                }
                binding.etAnswer.addTextChangedListener(currentTextWatcher)
            }
            else -> {
                // Show RecyclerView for MCQ and True/False
                binding.rvOptions.visibility = View.VISIBLE
                binding.cardTextInput.visibility = View.GONE
                questionAdapter.setQuestion(question, userAnswers[currentQuestionIndex])
            }
        }
        
        updateNavigationButtons()
    }
    
    private fun getQuestionType(question: Question): String {
        return when {
            question.options.isEmpty() -> "text"
            question.options.size == 2 && 
            (question.options.contains("True") && question.options.contains("False")) -> "true_false"
            else -> "mcq"
        }
    }
    
    private fun updateNavigationButtons() {
        binding.btnPrevious.isEnabled = currentQuestionIndex > 0
        binding.btnNext.isEnabled = currentQuestionIndex < questions.size - 1
        
        // Show submit button on last question
        if (currentQuestionIndex == questions.size - 1) {
            binding.btnNext.visibility = View.GONE
            binding.btnSubmit.visibility = View.VISIBLE
        } else {
            binding.btnNext.visibility = View.VISIBLE
            binding.btnSubmit.visibility = View.GONE
        }
    }
    
    private fun nextQuestion() {
        saveCurrentAnswer()
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            showCurrentQuestion()
            resetQuestionTimer()
        }
    }
    
    private fun previousQuestion() {
        saveCurrentAnswer()
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--
            showCurrentQuestion()
            resetQuestionTimer()
        }
    }
    
    private fun saveCurrentAnswer() {
        // Save text input if visible
        if (binding.cardTextInput.visibility == View.VISIBLE) {
            userAnswers[currentQuestionIndex] = binding.etAnswer.text.toString().trim()
        }
    }
    
    private fun moveToNextQuestion() {
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            showCurrentQuestion()
            resetQuestionTimer()
        } else {
            submitQuiz()
        }
    }
    
    private fun resetQuestionTimer() {
        questionTimeRemaining = timePerQuestionSeconds * 1000
        startQuestionTimer()
    }
    
    private fun updateExamTimer(millisUntilFinished: Long) {
        val minutes = (millisUntilFinished / 1000) / 60
        val seconds = (millisUntilFinished / 1000) % 60
        binding.tvExamTimer.text = String.format("⏱️ %02d:%02d", minutes, seconds)
    }
    
    private fun updateQuestionTimer(millisUntilFinished: Long) {
        val seconds = millisUntilFinished / 1000
        binding.tvQuestionTimer.text = String.format("⏰ %d", seconds)
        
        // Change color based on remaining time
        when {
            seconds <= 10 -> binding.tvQuestionTimer.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            seconds <= 20 -> binding.tvQuestionTimer.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
            else -> binding.tvQuestionTimer.setTextColor(resources.getColor(R.color.accent_orange))
        }
    }
    
    private fun submitQuiz() {
        examTimer?.cancel()
        questionTimer?.cancel()
        
        val timeSpent = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        val subject = intent.getStringExtra("subject") ?: "General Knowledge"
        
        lifecycleScope.launch {
            try {
                val token = preferenceManager.getToken()
                val submission = QuizSubmission(subject, userAnswers, timeSpent)
                val response = ApiClient.quizService.submitQuiz("Bearer $token", submission)
                
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    showResult(result.session?.correctAnswers ?: 0, questions.size)
                } else {
                    showError("Failed to submit quiz")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            }
        }
    }
    
    private fun showResult(correctAnswers: Int, totalQuestions: Int) {
        val percentage = (correctAnswers * 100) / totalQuestions
        val message = "Quiz completed!\nScore: $correctAnswers/$totalQuestions ($percentage%)"
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        examTimer?.cancel()
        questionTimer?.cancel()
    }
}