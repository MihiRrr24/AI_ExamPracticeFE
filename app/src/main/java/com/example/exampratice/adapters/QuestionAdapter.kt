package com.example.exampratice.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.exampratice.R
import com.example.exampratice.databinding.ItemOptionBinding
import com.example.exampratice.models.Question

class QuestionAdapter(
    private val onOptionSelected: (String) -> Unit
) : RecyclerView.Adapter<QuestionAdapter.OptionViewHolder>() {
    
    private var question: Question? = null
    private var selectedOption: String = ""
    
    fun setQuestion(question: Question, selectedOption: String) {
        this.question = question
        this.selectedOption = selectedOption
        notifyDataSetChanged()
    }
    
    fun getQuestionType(): String {
        val question = this.question ?: return "mcq"
        return when {
            question.options.isEmpty() -> "text"
            question.options.size == 2 && 
            (question.options.contains("True") && question.options.contains("False")) -> "true_false"
            else -> "mcq"
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OptionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        val option = question?.options?.get(position) ?: return
        holder.bind(option, option == selectedOption)
    }
    
    override fun getItemCount(): Int = question?.options?.size ?: 0
    
    inner class OptionViewHolder(private val binding: ItemOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(option: String, isSelected: Boolean) {
            binding.tvOption.text = option
            
            // Update appearance based on selection with green background
            if (isSelected) {
                binding.cardOption.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.accent_green)
                )
                binding.tvOption.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.white)
                )
                binding.cardOption.strokeWidth = 0
            } else {
                binding.cardOption.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.white)
                )
                binding.tvOption.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.text_primary)
                )
                binding.cardOption.strokeWidth = 2
                binding.cardOption.strokeColor = ContextCompat.getColor(binding.root.context, R.color.primary_blue_light)
            }
            
            binding.root.setOnClickListener {
                onOptionSelected(option)
            }
        }
    }
}