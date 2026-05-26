package com.example.paisatracker.domain.models

/**
 * Represents a date range with start and end timestamps
 * Used for filtering expenses and analytics by time period
 */
data class DateRange(
    val start: Long,
    val end: Long,
    val label: String
) {
    /**
     * Check if a timestamp falls within this date range
     */
    fun contains(timestamp: Long): Boolean {
        return timestamp in start..end
    }
    
    /**
     * Get the duration in milliseconds
     */
    fun durationMillis(): Long {
        return end - start
    }
    
    /**
     * Get the duration in days
     */
    fun durationDays(): Int {
        return ((end - start) / (24 * 60 * 60 * 1000)).toInt()
    }
}

// Made with Bob
