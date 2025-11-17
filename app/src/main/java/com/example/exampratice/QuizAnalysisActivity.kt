package com.example.exampratice

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exampratice.adapters.QuestionAnalysisAdapter
import com.example.exampratice.api.ApiClient
import com.example.exampratice.databinding.ActivityQuizAnalysisBinding
import com.example.exampratice.models.QuizAnalysisResponse
import com.example.exampratice.utils.PreferenceManager
import kotlinx.coroutines.launch

class QuizAnalysisActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityQuizAnalysisBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var questionAnalysisAdapter: QuestionAnalysisAdapter
    
    private var sessionId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager(this)
        sessionId = intent.getStringExtra("sessionId") ?: ""
        
        if (sessionId.isEmpty()) {
            Toast.makeText(this, "Invalid quiz session", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupViews()
        loadQuizAnalysis()
    }
    
    private fun setupViews() {
        binding.btnBack.setOnClickListener { finish() }
        
        questionAnalysisAdapter = QuestionAnalysisAdapter()
        binding.rvQuestions.apply {
            adapter = questionAnalysisAdapter
            layoutManager = LinearLayoutManager(this@QuizAnalysisActivity)
        }
    }
    
    private fun loadQuizAnalysis() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val token = preferenceManager.getToken()
                val response = ApiClient.quizService.getQuizAnalysis(sessionId, "Bearer $token")
                
                if (response.isSuccessful && response.body() != null) {
                    val analysis = response.body()!!
                    updateUI(analysis)
                } else {
                    showError("Failed to load quiz analysis")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun updateUI(analysis: QuizAnalysisResponse) {
        binding.scrollView.visibility = View.VISIBLE
        
        // Update quiz overview
        val overview = analysis.quizOverview
        binding.apply {
            tvQuizTitle.text = "${overview.subject} Quiz"
            tvTopicName.text = overview.topic
            tvTotalTime.text = "${overview.totalTimeSpent} minutes"
            tvAverageTime.text = "${overview.averageTimePerQuestion}s per question"
            
            // Update score with animation
            tvScore.text = "${overview.accuracy}%"
            tvCorrectAnswers.text = "${overview.correctAnswers}/${overview.totalQuestions}"
            progressScore.progress = overview.accuracy
            
            // Set score color based on performance
            val scoreColor = when {
                overview.accuracy >= 80 -> resources.getColor(R.color.success, null)
                overview.accuracy >= 60 -> resources.getColor(R.color.warning, null)
                else -> resources.getColor(R.color.error, null)
            }
            tvScore.setTextColor(scoreColor)
            progressScore.progressTintList = android.content.res.ColorStateList.valueOf(scoreColor)
        }
        
        // Update difficulty breakdown
        updateDifficultyBreakdown(analysis.performanceAnalysis.difficultyBreakdown)
        
        // Update AI Analysis
        updateAIAnalysis(analysis.aiAnalysis)
        
        // Update questions list
        questionAnalysisAdapter.updateQuestions(analysis.questions)
    }
    
    private fun updateDifficultyBreakdown(breakdown: List<com.example.exampratice.models.DifficultyBreakdown>) {
        breakdown.forEach { diff ->
            when (diff.difficulty) {
                "easy" -> {
                    binding.tvEasyScore.text = "${diff.accuracy}%"
                    binding.progressEasy.progress = diff.accuracy
                    binding.tvEasyQuestions.text = "${diff.correct}/${diff.total}"
                }
                "medium" -> {
                    binding.tvMediumScore.text = "${diff.accuracy}%"
                    binding.progressMedium.progress = diff.accuracy
                    binding.tvMediumQuestions.text = "${diff.correct}/${diff.total}"
                }
                "hard" -> {
                    binding.tvHardScore.text = "${diff.accuracy}%"
                    binding.progressHard.progress = diff.accuracy
                    binding.tvHardQuestions.text = "${diff.correct}/${diff.total}"
                }
            }
        }
    }
    
    private fun updateAIAnalysis(analysis: com.example.exampratice.models.AIAnalysis) {
        binding.apply {
            tvOverallAssessment.text = analysis.overallAssessment
            
            // Update strengths
            if (analysis.strengths.isNotEmpty()) {
                tvStrengths.text = analysis.strengths.joinToString("\n• ", "• ")
                cardStrengths.visibility = View.VISIBLE
            } else {
                cardStrengths.visibility = View.GONE
            }
            
            // Update weaknesses
            if (analysis.weaknesses.isNotEmpty()) {
                tvWeaknesses.text = analysis.weaknesses.joinToString("\n• ", "• ")
                cardWeaknesses.visibility = View.VISIBLE
            } else {
                cardWeaknesses.visibility = View.GONE
            }
            
            // Update recommendations
            tvRecommendations.text = analysis.recommendations.joinToString("\n• ", "• ")
            
            // Update study plan
            tvStudyPlan.text = analysis.studyPlan.joinToString("\n", "")
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        binding.scrollView.visibility = View.VISIBLE
    }
}