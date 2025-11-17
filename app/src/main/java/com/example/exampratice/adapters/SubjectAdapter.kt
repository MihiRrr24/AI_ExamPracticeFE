package com.example.exampratice.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.exampratice.databinding.ItemSubjectBinding
import com.example.exampratice.models.Subject

class SubjectAdapter(
    private val onSubjectClick: (Subject) -> Unit
) : ListAdapter<Subject, SubjectAdapter.SubjectViewHolder>(SubjectDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubjectViewHolder(private val binding: ItemSubjectBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(subject: Subject) {
            binding.tvSubjectName.text = subject.name
            binding.tvSubjectIcon.text = subject.icon
            
            try {
                binding.cardSubject.setCardBackgroundColor(Color.parseColor(subject.color))
            } catch (e: Exception) {
                // Use default color if parsing fails
            }
            
            binding.root.setOnClickListener {
                onSubjectClick(subject)
            }
        }
    }

    private class SubjectDiffCallback : DiffUtil.ItemCallback<Subject>() {
        override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean {
            return oldItem == newItem
        }
    }
}