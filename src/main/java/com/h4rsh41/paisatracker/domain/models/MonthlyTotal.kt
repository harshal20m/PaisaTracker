package com.h4rsh41.paisatracker.domain.models

/**
 * Represents aggregated expense data for a specific month.
 * Used for monthly analytics and trend visualization.
 *
 * @property month Month in format "YYYY-MM" (e.g., "2024-01")
 * @property total Total amount spent in the month
 * @property count Number of expenses in the month
 * @property averagePerDay Average spending per day (total / days in month)
 */
data class MonthlyTotal(
    val month: String,
    val total: Double,
    val count: Int
) {
    /**
     * Get the year from the month string.
     * @return Year as Int (e.g., 2024)
     */
    fun getYear(): Int = month.substring(0, 4).toInt()

    /**
     * Get the month number from the month string.
     * @return Month as Int (1-12)
     */
    fun getMonthNumber(): Int = month.substring(5, 7).toInt()

    /**
     * Get a human-readable month name.
     * @return Month name (e.g., "January 2024")
     */
    fun getMonthName(): String {
        val monthNames = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthIndex = getMonthNumber() - 1
        return "${monthNames[monthIndex]} ${getYear()}"
    }

    /**
     * Calculate average spending per expense.
     * @return Average amount per expense, or 0.0 if no expenses
     */
    fun getAveragePerExpense(): Double {
        return if (count > 0) total / count else 0.0
    }

    /**
     * Calculate approximate average per day (assuming 30 days).
     * @return Average amount per day
     */
    fun getAveragePerDay(): Double {
        return total / 30.0
    }

    /**
     * Check if this month has any expenses.
     * @return true if count > 0
     */
    fun hasExpenses(): Boolean = count > 0

    /**
     * Compare with another month to calculate growth percentage.
     * @param previous Previous month's total
     * @return Growth percentage (positive = increase, negative = decrease)
     */
    fun calculateGrowth(previous: MonthlyTotal?): Double {
        if (previous == null || previous.total == 0.0) return 0.0
        return ((total - previous.total) / previous.total) * 100
    }
}

// Made with Bob
