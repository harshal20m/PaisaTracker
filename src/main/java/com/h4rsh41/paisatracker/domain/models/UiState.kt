package com.h4rsh41.paisatracker.domain.models

/**
 * Sealed class representing different UI states for data loading
 * Provides consistent state management across the app
 * 
 * Usage:
 * ```
 * val uiState: StateFlow<UiState<List<Expense>>> = ...
 * 
 * when (uiState.value) {
 *     is UiState.Loading -> ShowLoadingIndicator()
 *     is UiState.Success -> ShowData(uiState.value.data)
 *     is UiState.Error -> ShowError(uiState.value.message)
 *     is UiState.Empty -> ShowEmptyState()
 * }
 * ```
 */
sealed class UiState<out T> {
    /**
     * Initial loading state
     */
    object Loading : UiState<Nothing>()
    
    /**
     * Success state with data
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * Error state with message and optional retry action
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : UiState<Nothing>()
    
    /**
     * Empty state when no data is available
     */
    object Empty : UiState<Nothing>()
}

/**
 * Extension function to check if state is loading
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

/**
 * Extension function to check if state is success
 */
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success

/**
 * Extension function to check if state is error
 */
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error

/**
 * Extension function to check if state is empty
 */
fun <T> UiState<T>.isEmpty(): Boolean = this is UiState.Empty

/**
 * Extension function to get data if success, null otherwise
 */
fun <T> UiState<T>.getDataOrNull(): T? {
    return if (this is UiState.Success) data else null
}

// Made with Bob
