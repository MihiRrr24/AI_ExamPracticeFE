package com.example.exampratice.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.exampratice.R
import com.example.exampratice.databinding.ItemQuestionAnalysisBinding
import com.example.exampratice.models.QuestionAnalysis

class QuestionAnalysisAdapter : RecyclerView.Adapter<QuestionAnalysisAdapter.QuestionAnalysisViewHolder>() {
    
    private var questions = listOf<QuestionAnalysis>()
    
    fun updateQuestions(newQuestions: List<QuestionAnalysis>) {
        questions = newQuestions
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionAnalysisViewHolder {
        val binding = ItemQuestionAnalysisBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return QuestionAnalysisViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: QuestionAnalysisViewHolder, position: Int) {
        holder.bind(questions[position], position + 1)
    }
    
    override fun getItemCount() = questions.size
    
    class QuestionAnalysisViewHolder(
        private val binding: ItemQuestionAnalysisBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(question: QuestionAnalysis, questionNumber: Int) {
            binding.apply {
                tvQuestionNumber.text = "Q$questionNumber"
                tvQuestion.text = question.question
                tvExplanation.text = question.explanation
                
                // Show correct answer
                tvCorrectAnswer.text = question.correctAnswer
                
                // Show user answer if provided
                if (question.userAnswer.isNotEmpty()) {
                    tvUserAnswer.text = question.userAnswer
                    layoutUserAnswer.visibility = View.VISIBLE
                } else {
                    tvUserAnswer.text = "Not answered"
                    layoutUserAnswer.visibility = View.VISIBLE
                }
                
                // Set status indicator
                if (question.isCorrect) {
                    ivStatus.setImageResource(R.drawable.ic_correct)
                    ivStatus.imageTintList = ContextCompat.getColorStateList(itemView.context, R.color.success)
                    cardQuestion.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.success_light)
                    )
                } else {
                    ivStatus.setImageResource(R.drawable.ic_incorrect)
                    ivStatus.imageTintList = ContextCompat.getColorStateList(itemView.context, R.color.error)
                    cardQuestion.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.error_light)
                    )
                }
                
                // Show options for MCQ questions
                when (question.type) {
                    "mcq" -> {
                        layoutOptions.visibility = View.VISIBLE
                        tvOptions.text = question.options?.joinToString("\n") { "• $it" } ?: ""
                    }
                    else -> {
                        layoutOptions.visibility = View.GONE
                    }
                }
                
                // Set difficulty badge
                val difficultyColor = when (question.difficulty) {
                    "easy" -> R.color.accent_green
                    "medium" -> R.color.accent_orange
                    "hard" -> R.color.accent_red
                    else -> R.color.accent_orange
                }
                
                tvDifficulty.text = question.difficulty.replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                }
                tvDifficulty.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, difficultyColor)
                )
            }
        }
    }
}