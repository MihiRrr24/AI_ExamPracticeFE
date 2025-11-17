package com.example.exampratice.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.exampratice.R
import com.example.exampratice.databinding.ItemRecentQuizBinding
import com.example.exampratice.models.RecentQuiz

class RecentQuizAdapter(
    private val onQuizClick: (RecentQuiz) -> Unit
) : ListAdapter<RecentQuiz, RecentQuizAdapter.RecentQuizViewHolder>(RecentQuizDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentQuizViewHolder {
        val binding = ItemRecentQuizBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentQuizViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentQuizViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecentQuizViewHolder(private val binding: ItemRecentQuizBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(recentQuiz: RecentQuiz) {
            binding.tvSubject.text = recentQuiz.subject
            binding.tvScore.text = recentQuiz.score
            binding.tvDate.text = recentQuiz.date
            binding.progressBar.progress = recentQuiz.percentage
            binding.tvPercentage.text = "${recentQuiz.percentage}%"
            
            // Set progress bar color based on percentage
            val color = when {
                recentQuiz.percentage >= 80 -> ContextCompat.getColor(binding.root.context, R.color.success)
                recentQuiz.percentage >= 60 -> ContextCompat.getColor(binding.root.context, R.color.warning)
                else -> ContextCompat.getColor(binding.root.context, R.color.error)
            }
            binding.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(color)
            
            binding.root.setOnClickListener {
                onQuizClick(recentQuiz)
            }
        }
    }

    private class RecentQuizDiffCallback : DiffUtil.ItemCallback<RecentQuiz>() {
        override fun areItemsTheSame(oldItem: RecentQuiz, newItem: RecentQuiz): Boolean {
            return oldItem.subject == newItem.subject && oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: RecentQuiz, newItem: RecentQuiz): Boolean {
            return oldItem == newItem
        }
    }
}