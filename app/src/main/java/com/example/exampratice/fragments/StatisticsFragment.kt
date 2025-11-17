package com.example.exampratice.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.example.exampratice.R
import com.example.exampratice.api.ApiClient
import com.example.exampratice.databinding.FragmentStatisticsBinding
import com.example.exampratice.utils.PreferenceManager
import kotlinx.coroutines.launch

class StatisticsFragment : Fragment() {
    
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferenceManager = PreferenceManager(requireContext())
        
        setupViews()
        loadStatistics()
    }
    
    private fun setupViews() {
        // Setup chart
        binding.barChart.apply {
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            description.isEnabled = false
            setMaxVisibleValueCount(50)
            setPinchZoom(false)
            setDrawGridBackground(false)
        }
    }
    
    private fun loadStatistics() {
        // Load sample data for now
        loadSampleData()
        
        // Load real statistics from API
        lifecycleScope.launch {
            try {
                val token = preferenceManager.getToken()
                val response = ApiClient.userService.getProfile("Bearer $token")
                
                if (response.isSuccessful && response.body() != null) {
                    // Update UI with real statistics
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    private fun loadSampleData() {
        // Sample statistics
        binding.tvTotalQuizzes.text = "25"
        binding.tvAverageScore.text = "85%"
        binding.tvBestScore.text = "95%"
        binding.tvTotalTime.text = "12.5h"
        
        // Sample chart data
        val entries = listOf(
            BarEntry(0f, 80f),
            BarEntry(1f, 85f),
            BarEntry(2f, 90f),
            BarEntry(3f, 75f),
            BarEntry(4f, 95f),
            BarEntry(5f, 88f),
            BarEntry(6f, 92f)
        )
        
        val dataSet = BarDataSet(entries, "Quiz Scores")
        dataSet.color = resources.getColor(R.color.primary_blue, null)
        
        val barData = BarData(dataSet)
        binding.barChart.data = barData
        binding.barChart.invalidate()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}