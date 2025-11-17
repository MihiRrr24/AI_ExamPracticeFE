package com.example.exampratice.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.exampratice.databinding.FragmentQuizBinding

class QuizFragment : Fragment() {
    
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // This fragment can be used for quiz selection/management
        // For now, it will show a simple message
        binding.tvMessage.text = "Quiz management coming soon!"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}