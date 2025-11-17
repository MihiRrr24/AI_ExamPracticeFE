package com.example.exampratice.api

import com.example.exampratice.models.*
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://ai-exampracticebe.onrender.com/api/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()



    val userService: UserService = retrofit.create(UserService::class.java)
    val authService: AuthService = retrofit.create(AuthService::class.java)
    val quizService: QuizService = retrofit.create(QuizService::class.java)
}

interface UserService {
    @GET("user/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>
    
    @PUT("user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body updateRequest: UpdateProfileRequest
    ): Response<UpdateProfileResponse>
    
    @POST("user/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body changePasswordRequest: ChangePasswordRequest
    ): Response<MessageResponse>
    
    @GET("user/notifications")
    suspend fun getNotifications(@Header("Authorization") token: String): Response<NotificationSettings>
    
    @PUT("user/notifications")
    suspend fun updateNotifications(
        @Header("Authorization") token: String,
        @Body notificationSettings: NotificationSettings
    ): Response<UpdateNotificationResponse>
    
    @POST("user/toggle-2fa")
    suspend fun toggle2FA(@Header("Authorization") token: String): Response<MessageResponse>
}

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body verifyOtpRequest: VerifyOtpRequest): Response<AuthResponse>
}

interface QuizService {
    @POST("generate/questions")
    suspend fun generateQuestions(
        @Header("Authorization") token: String,
        @Body generateRequest: GenerateQuestionsRequest
    ): Response<QuestionSet>

    @POST("quiz/submit")
    suspend fun submitQuiz(
        @Header("Authorization") token: String,
        @Body submitRequest: QuizSubmission
    ): Response<QuizResult>

    @GET("quiz/recent")
    suspend fun getRecentQuizzes(@Header("Authorization") token: String): Response<List<QuizSession>>
    
    @GET("quiz/subjects")
    suspend fun getSubjects(@Header("Authorization") token: String): Response<List<SubjectData>>
    
    @GET("quiz/subjects/{subject}/topics")
    suspend fun getTopics(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("subject") subject: String
    ): Response<TopicsResponse>
    
    @POST("quiz/start")
    suspend fun startQuiz(
        @Header("Authorization") token: String,
        @Body startRequest: StartQuizRequest
    ): Response<QuizSession>
    
    @GET("quiz/analysis/{sessionId}")
    suspend fun getQuizAnalysis(
        @retrofit2.http.Path("sessionId") sessionId: String,
        @Header("Authorization") token: String
    ): Response<QuizAnalysisResponse>
    
    @GET("quiz/dashboard-stats")
    suspend fun getDashboardStats(@Header("Authorization") token: String): Response<DashboardStats>
}