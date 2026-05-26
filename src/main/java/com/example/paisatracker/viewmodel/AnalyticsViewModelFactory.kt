package com.example.paisatracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.paisatracker.data.PaisaTrackerRepository

/**
 * Factory for creating AnalyticsViewModel instances.
 * Required because AnalyticsViewModel has constructor parameters.
 *
 * Usage:
 * ```kotlin
 * val viewModel: AnalyticsViewModel by viewModels {
 *     AnalyticsViewModelFactory(repository)
 * }
 * ```
 */
class AnalyticsViewModelFactory(
    private val repository: PaisaTrackerRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            return AnalyticsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

// Made with Bob
