package com.example.exampratice.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exampratice.AuthActivity
import com.example.exampratice.QuizAnalysisActivity
import com.example.exampratice.QuizConfigurationActivity
import com.example.exampratice.adapters.RecentQuizAdapter
import com.example.exampratice.adapters.SubjectAdapter
import com.example.exampratice.api.ApiClient
import com.example.exampratice.databinding.FragmentDashboardBinding
import com.example.exampratice.models.RecentQuiz
import com.example.exampratice.models.Subject
import com.example.exampratice.models.User
import com.example.exampratice.utils.PreferenceManager
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var subjectAdapter: SubjectAdapter
    private lateinit var recentQuizAdapter: RecentQuizAdapter
    private var allRecentQuizzes = listOf<RecentQuiz>()
    private var showingAllQuizzes = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferenceManager = PreferenceManager(requireContext())
        
        setupViews()
        loadDashboardData()
    }
    
    private fun setupViews() {
        // Setup greeting
        val userName = preferenceManager.getUserName() ?: "Student"
        binding.tvGreeting.text = "Hello, $userName!"
        
        // Setup subjects recycler view
        subjectAdapter = SubjectAdapter { subject ->
            startQuiz(subject.name)
        }
        binding.rvSubjects.apply {
            adapter = subjectAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
        
        // Setup recent quizzes recycler view
        recentQuizAdapter = RecentQuizAdapter { quiz ->
            viewQuizDetails(quiz)
        }
        binding.rvRecentQuizzes.apply {
            adapter = recentQuizAdapter
            layoutManager = LinearLayoutManager(context)
        }
        
        // Setup click listeners
        binding.btnStartQuiz.setOnClickListener {
            showSubjectSelection()
        }
        
        binding.tvViewMore.setOnClickListener {
            toggleViewMore()
        }
    }

    private fun loadDashboardData() {
        // Load subjects
        loadSubjects()

        // Load recent quizzes
        loadRecentQuizzes()
    }

    private fun loadSubjects() {
        lifecycleScope.launch {
            try {
                val token = preferenceManager.getToken()
                val response = ApiClient.quizService.getSubjects("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val subjects = response.body()!!.map { subjectData ->
                        Subject(subjectData.name, subjectData.icon, subjectData.color)
                    }
                    subjectAdapter.submitList(subjects)
                } else {
                    showDefaultSubjects()
                }
            } catch (e: Exception) {
                showDefaultSubjects()
            }
        }
    }

    private fun showDefaultSubjects() {
        val subjects = listOf(
            Subject("Mathematics", "📊", "#FF6B6B"),
            Subject("Science", "🔬", "#4ECDC4"),
            Subject("History", "📚", "#45B7D1"),
            Subject("English", "📝", "#96CEB4"),
            Subject("Programming", "💻", "#FCEA2B"),
            Subject("General Knowledge", "🧠", "#FF9FF3")
        )
        subjectAdapter.submitList(subjects)
    }

    private fun loadRecentQuizzes() {
        lifecycleScope.launch {
            try {
                val token = preferenceManager.getToken()
                println("DEBUG: Loading recent quizzes with token: ${token?.take(10)}...")

                val response = ApiClient.quizService.getRecentQuizzes("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val quizSessions = response.body()!!
                    println("DEBUG: Retrieved ${quizSessions.size} quiz sessions from API")

                    if (quizSessions.isNotEmpty()) {
                        val recentQuizzes = quizSessions.map { quizSession ->
                            RecentQuiz(
                                id = quizSession.id,
                                subject = quizSession.subject,
                                score = "${quizSession.correctAnswers}/${quizSession.totalQuestions}",
                                date = quizSession.createdAt,
                                percentage = if (quizSession.totalQuestions > 0) {
                                    (quizSession.correctAnswers * 100 / quizSession.totalQuestions)
                                } else 0
                            )
                        }
                        allRecentQuizzes = recentQuizzes
                        displayRecentQuizzes()
                        println("DEBUG: Displayed ${recentQuizzes.size} recent quizzes")
                    } else {
                        println("DEBUG: No quiz sessions found, showing empty state message")
                        showNoQuizzesMessage()
                    }
                } else {
                    println("DEBUG: API call failed with code: ${response.code()}, message: ${response.message()}")
                    if (response.code() == 401) {
                        // Token expired, redirect to auth
                        startActivity(Intent(requireContext(), AuthActivity::class.java))
                        requireActivity().finish()
                    } else {
                        showSampleRecentQuizzes()
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Exception loading recent quizzes: ${e.message}")
                e.printStackTrace()
                showSampleRecentQuizzes()
            }
        }
    }

    private fun displayRecentQuizzes() {
        val quizzesToShow = if (showingAllQuizzes) {
            allRecentQuizzes
        } else {
            allRecentQuizzes.take(3)
        }
        recentQuizAdapter.submitList(quizzesToShow)
        
        // Show "View More" text if there are more than 3 quizzes and we're not showing all
        binding.tvViewMore.visibility = if (allRecentQuizzes.size > 3 && !showingAllQuizzes) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun toggleViewMore() {
        showingAllQuizzes = !showingAllQuizzes
        binding.tvViewMore.text = if (showingAllQuizzes) "View Less" else "View More"
        displayRecentQuizzes()
    }

    private fun showNoQuizzesMessage() {
        // Show empty list and a message that no quizzes have been taken yet
        allRecentQuizzes = emptyList()
        recentQuizAdapter.submitList(emptyList())
        binding.tvViewMore.visibility = View.GONE
        showingAllQuizzes = false

        // Show a toast to indicate no quizzes available
        Toast.makeText(requireContext(), "No recent quizzes found. Take a quiz to see your history!", Toast.LENGTH_LONG).show()
    }

    private fun showSampleRecentQuizzes() {
        val sampleQuizzes = listOf(
            RecentQuiz("sample1", "Mathematics", "8/10", "2024-01-15", 80),
            RecentQuiz("sample2", "Science", "9/10", "2024-01-14", 90),
            RecentQuiz("sample3", "History", "7/10", "2024-01-13", 70)
        )
        allRecentQuizzes = sampleQuizzes
        displayRecentQuizzes()
    }

    private fun startQuiz(subject: String) {
        val intent = Intent(requireContext(), QuizConfigurationActivity::class.java)
        intent.putExtra("subject", subject)
        startActivity(intent)
    }

    private fun showSubjectSelection() {
        // Launch QuizConfigurationActivity without subject (user will select subject)
        val intent = Intent(context, QuizConfigurationActivity::class.java)
        // Don't pass subject - this will trigger subject selection mode
        startActivity(intent)
    }

    private fun viewQuizDetails(quiz: RecentQuiz) {
        val intent = Intent(context, QuizAnalysisActivity::class.java)
        intent.putExtra("sessionId", quiz.id)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}