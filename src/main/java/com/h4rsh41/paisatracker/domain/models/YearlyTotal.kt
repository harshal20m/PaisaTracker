package com.h4rsh41.paisatracker.domain.models

/**
 * Represents aggregated expense data for a specific year.
 * Used for yearly analytics and long-term trend visualization.
 *
 * @property year Year as string (e.g., "2024")
 * @property total Total amount spent in the year
 * @property count Number of expenses in the year
 */
data class YearlyTotal(
    val year: String,
    val total: Double,
    val count: Int
) {
    /**
     * Get the year as an integer.
     * @return Year as Int (e.g., 2024)
     */
    fun getYearInt(): Int = year.toInt()

    /**
     * Calculate average spending per expense.
     * @return Average amount per expense, or 0.0 if no expenses
     */
    fun getAveragePerExpense(): Double {
        return if (count > 0) total / count else 0.0
    }

    /**
     * Calculate average spending per month.
     * @return Average amount per month
     */
    fun getAveragePerMonth(): Double {
        return total / 12.0
    }

    /**
     * Calculate approximate average per day (assuming 365 days).
     * @return Average amount per day
     */
    fun getAveragePerDay(): Double {
        return total / 365.0
    }

    /**
     * Check if this year has any expenses.
     * @return true if count > 0
     */
    fun hasExpenses(): Boolean = count > 0

    /**
     * Compare with another year to calculate growth percentage.
     * @param previous Previous year's total
     * @return Growth percentage (positive = increase, negative = decrease)
     */
    fun calculateGrowth(previous: YearlyTotal?): Double {
        if (previous == null || previous.total == 0.0) return 0.0
        return ((total - previous.total) / previous.total) * 100
    }

    /**
     * Calculate year-over-year change in expense count.
     * @param previous Previous year's total
     * @return Change in number of expenses
     */
    fun calculateCountChange(previous: YearlyTotal?): Int {
        return if (previous != null) count - previous.count else 0
    }

    /**
     * Check if spending increased compared to previous year.
     * @param previous Previous year's total
     * @return true if current year spending is higher
     */
    fun isIncreaseFrom(previous: YearlyTotal?): Boolean {
        return previous != null && total > previous.total
    }

    /**
     * Get a summary string for the year.
     * @return Summary like "2024: $50,000 (150 expenses)"
     */
    fun getSummary(): String {
        return "$year: $${"%.2f".format(total)} ($count expenses)"
    }
}

// Made with Bob
