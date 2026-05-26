package com.example.paisatracker.domain.models

/**
 * Represents statistical analysis data for a specific time period.
 * Used for displaying key metrics and insights in analytics screens.
 *
 * @property totalSpending Total amount spent in the period
 * @property expenseCount Number of expenses in the period
 * @property averagePerExpense Average amount per expense
 * @property dailyAverage Average daily spending
 * @property period The time period these statistics represent
 * @property dateRange The exact date range for these statistics
 */
data class AnalyticsStatistics(
    val totalSpending: Double,
    val expenseCount: Int,
    val averagePerExpense: Double,
    val dailyAverage: Double,
    val period: TimePeriod,
    val dateRange: DateRange
) {
    /**
     * Check if there are any expenses in this period.
     */
    fun hasExpenses(): Boolean = expenseCount > 0
    
    /**
     * Get the number of days in the period.
     */
    fun getDaysInPeriod(): Int {
        val diffInMillis = dateRange.end - dateRange.start
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
    }
    
    /**
     * Calculate projected total for the full period based on current rate.
     * Useful for partial periods (e.g., mid-month projections).
     */
    fun getProjectedTotal(daysElapsed: Int): Double {
        if (daysElapsed == 0) return totalSpending
        val totalDays = getDaysInPeriod()
        return (totalSpending / daysElapsed) * totalDays
    }
    
    /**
     * Get a formatted summary string.
     */
    fun getSummary(): String {
        return "$expenseCount expenses totaling $${"%.2f".format(totalSpending)}"
    }
}

// Made with Bob