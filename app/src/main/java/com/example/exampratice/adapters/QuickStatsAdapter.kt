package com.example.exampratice.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.exampratice.databinding.ItemQuickStatBinding
import com.example.exampratice.models.QuickStat

class QuickStatsAdapter : ListAdapter<QuickStat, QuickStatsAdapter.QuickStatViewHolder>(QuickStatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickStatViewHolder {
        val binding = ItemQuickStatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuickStatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuickStatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class QuickStatViewHolder(private val binding: ItemQuickStatBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(quickStat: QuickStat) {
            binding.tvStatIcon.text = quickStat.icon
            binding.tvStatTitle.text = quickStat.title
            binding.tvStatValue.text = quickStat.value
            
            // Set colors if provided
            if (quickStat.backgroundColor != "#FFFFFF") {
                try {
                    val color = android.graphics.Color.parseColor(quickStat.backgroundColor)
                    binding.llStatBackground.setBackgroundColor(color)
                } catch (e: Exception) {
                    // Handle invalid color gracefully
                }
            }
            
            if (quickStat.textColor != "#000000") {
                try {
                    val color = android.graphics.Color.parseColor(quickStat.textColor)
                    binding.tvStatValue.setTextColor(color)
                } catch (e: Exception) {
                    // Handle invalid color gracefully
                }
            }
            
            // Show/hide progress indicator
            if (quickStat.showProgress) {
                binding.vProgressIndicator.visibility = android.view.View.VISIBLE
                // You could animate the progress here
            } else {
                binding.vProgressIndicator.visibility = android.view.View.GONE
            }
            
            // Add subtle animation based on trend
            when (quickStat.trend) {
                "up" -> {
                    // Maybe add a subtle green tint or animation
                    binding.tvStatValue.setTextColor(
                        binding.root.context.getColor(android.R.color.holo_green_dark)
                    )
                }
                "down" -> {
                    binding.tvStatValue.setTextColor(
                        binding.root.context.getColor(android.R.color.holo_red_dark)
                    )
                }
                else -> {
                    // Keep default color
                }
            }
        }
    }

    private class QuickStatDiffCallback : DiffUtil.ItemCallback<QuickStat>() {
        override fun areItemsTheSame(oldItem: QuickStat, newItem: QuickStat): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: QuickStat, newItem: QuickStat): Boolean {
            return oldItem == newItem
        }
    }
}