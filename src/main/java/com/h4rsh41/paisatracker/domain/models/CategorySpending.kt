package com.h4rsh41.paisatracker.domain.models

/**
 * Represents aggregated spending data for a specific category.
 * Used for category-wise analytics and budget tracking.
 *
 * @property categoryId Category ID
 * @property categoryName Category name
 * @property categoryIcon Category emoji icon
 * @property total Total amount spent in the category
 * @property count Number of expenses in the category
 * @property percentage Percentage of total spending (0-100)
 */
data class CategorySpending(
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val total: Double,
    val count: Int,
    val percentage: Double = 0.0
) {
    /**
     * Calculate average spending per expense in this category.
     * @return Average amount per expense, or 0.0 if no expenses
     */
    fun getAveragePerExpense(): Double {
        return if (count > 0) total / count else 0.0
    }

    /**
     * Check if this category has any expenses.
     * @return true if count > 0
     */
    fun hasExpenses(): Boolean = count > 0

    /**
     * Compare with another category to determine which has higher spending.
     * @param other Another CategorySpending to compare with
     * @return true if this category has higher spending
     */
    fun hasHigherSpendingThan(other: CategorySpending): Boolean {
        return total > other.total
    }

    /**
     * Get a formatted summary string.
     * @return Summary like "Groceries: $500.00 (15 expenses)"
     */
    fun getSummary(): String {
        return "$categoryName: $${"%.2f".format(total)} ($count expenses)"
    }

    /**
     * Get a formatted percentage string.
     * @return Percentage like "25.5%"
     */
    fun getPercentageFormatted(): String {
        return "${"%.1f".format(percentage)}%"
    }

    /**
     * Check if this category represents a significant portion of spending.
     * @param threshold Threshold percentage (default 10%)
     * @return true if percentage >= threshold
     */
    fun isSignificant(threshold: Double = 10.0): Boolean {
        return percentage >= threshold
    }

    /**
     * Compare with budget to check if over budget.
     * @param budgetAmount Budget amount for this category
     * @return true if spending exceeds budget
     */
    fun isOverBudget(budgetAmount: Double): Boolean {
        return total > budgetAmount
    }

    /**
     * Calculate budget utilization percentage.
     * @param budgetAmount Budget amount for this category
     * @return Utilization percentage (can be > 100%)
     */
    fun getBudgetUtilization(budgetAmount: Double): Double {
        return if (budgetAmount > 0) (total / budgetAmount) * 100 else 0.0
    }

    /**
     * Calculate remaining budget.
     * @param budgetAmount Budget amount for this category
     * @return Remaining amount (negative if over budget)
     */
    fun getRemainingBudget(budgetAmount: Double): Double {
        return budgetAmount - total
    }

    companion object {
        /**
         * Calculate percentages for a list of category spending.
         * @param categories List of CategorySpending
         * @return List with updated percentages
         */
        fun calculatePercentages(categories: List<CategorySpending>): List<CategorySpending> {
            val totalSpending = categories.sumOf { it.total }
            if (totalSpending == 0.0) return categories

            return categories.map { category ->
                category.copy(
                    percentage = (category.total / totalSpending) * 100
                )
            }
        }

        /**
         * Sort categories by spending amount (descending).
         * @param categories List of CategorySpending
         * @return Sorted list
         */
        fun sortBySpending(categories: List<CategorySpending>): List<CategorySpending> {
            return categories.sortedByDescending { it.total }
        }

        /**
         * Get top N categories by spending.
         * @param categories List of CategorySpending
         * @param n Number of top categories to return
         * @return Top N categories
         */
        fun getTopCategories(categories: List<CategorySpending>, n: Int): List<CategorySpending> {
            return sortBySpending(categories).take(n)
        }

        /**
         * Filter categories with spending above a threshold.
         * @param categories List of CategorySpending
         * @param threshold Minimum spending amount
         * @return Filtered list
         */
        fun filterByMinimumSpending(
            categories: List<CategorySpending>,
            threshold: Double
        ): List<CategorySpending> {
            return categories.filter { it.total >= threshold }
        }
    }
}

// Made with Bob
