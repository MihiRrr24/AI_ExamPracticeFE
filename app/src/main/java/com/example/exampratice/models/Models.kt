package com.example.exampratice.models

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: User? = null,
    val requiresOtp: Boolean = false,
    val email: String? = null
)


data class User(
    val _id: String,
    val name: String,
    val email: String,
    val createdAt: String? = null,
    val twoFactorEnabled: Boolean = false,
    val notifications: NotificationSettings? = null,
    val stats: UserStats? = null
)

data class NotificationSettings(
    val quizReminders: Boolean = true,
    val dailyGoals: Boolean = true,
    val weeklyReports: Boolean = true,
    val achievements: Boolean = true
)

data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null
)

data class UpdateProfileResponse(
    val message: String,
    val user: User?
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class UpdateNotificationResponse(
    val message: String,
    val notifications: NotificationSettings
)

data class MessageResponse(
    val message: String
)

data class UserStats(
    val totalQuizzes: Int = 0,
    val averageScore: Int = 0,
    val bestSubject: String = "N/A",
    val streak: Int = 0,
    val subjectStats: Map<String, Any>? = null
)

data class Subject(
    val name: String,
    val icon: String,
    val color: String
)

data class QuickStat(
    val title: String,
    val value: String,
    val icon: String,
    val backgroundColor: String = "#FFFFFF",
    val textColor: String = "#000000",
    val showProgress: Boolean = false,
    val progressValue: Int = 0, // 0-100
    val trend: String = "neutral" // up, down, neutral
)

data class DashboardStats(
    val totalQuizzes: Int,
    val averageScore: Int,
    val totalTimeSpent: Int, // in minutes
    val strongestSubject: String,
    val weakestSubject: String,
    val weeklyProgress: Int,
    val accuracy: Int,
    val currentStreak: Int
)

data class RecentQuiz(
    val id: String,
    val subject: String,
    val score: String,
    val date: String,
    val percentage: Int
)

data class QuizSession(
    val id: String,
    val subject: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Double,
    val duration: Int,
    val createdAt: String
) {
    // Provide _id for backward compatibility
    val _id: String get() = id
}

data class Question(
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val difficulty: String = "medium",
    val explanation: String? = null
)

data class QuestionSet(
    val _id: String? = null,
    val subject: String,
    val topic: String? = null,
    val questions: List<Question>,
    val settings: QuizSettings? = null,
    val createdAt: String? = null
)

data class GenerateQuestionsRequest(
    val subject: String,
    val topic: String? = null,
    val count: Int = 10,
    val timePerQuestion: Int = 60, // seconds
    val difficulty: String = "medium" // easy, medium, hard
)

data class QuizSubmission(
    val subject: String,
    val answers: List<String>,
    val timeSpent: Int
)

data class QuizResult(
    val success: Boolean,
    val message: String,
    val session: QuizSession? = null,
    val results: List<QuestionResult>? = null
)

data class QuestionResult(
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val explanation: String? = null
)

data class SubjectData(
    val name: String,
    val icon: String,
    val color: String,
    val topics: List<String> = emptyList()
)

data class TopicsResponse(
    val subject: String,
    val topics: List<String>
)

data class QuizSettings(
    val count: Int = 10,
    val timePerQuestion: Int = 60, // seconds
    val totalTime: Int = 600 // total time in seconds
)

data class StartQuizRequest(
    val questionSetId: String
)

// Quiz Analysis Models
data class QuizAnalysisResponse(
    val quizOverview: QuizOverview,
    val performanceAnalysis: PerformanceAnalysis,
    val aiAnalysis: AIAnalysis,
    val questions: List<QuestionAnalysis>
)

data class QuizOverview(
    val subject: String,
    val topic: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracy: Int,
    val totalTimeSpent: Int,
    val averageTimePerQuestion: Int,
    val completedAt: String,
    val difficulty: String
)

data class PerformanceAnalysis(
    val overallAccuracy: Int,
    val difficultyBreakdown: List<DifficultyBreakdown>
)

data class DifficultyBreakdown(
    val difficulty: String,
    val total: Int,
    val correct: Int,
    val accuracy: Int
)

data class AIAnalysis(
    val overallAssessment: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val recommendations: List<String>,
    val studyPlan: List<String>
)

data class QuestionAnalysis(
    val type: String,
    val question: String,
    val options: List<String>? = null,
    val correctAnswer: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val explanation: String,
    val difficulty: String
)