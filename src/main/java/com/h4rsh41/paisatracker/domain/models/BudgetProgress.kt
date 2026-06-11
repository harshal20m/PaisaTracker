package com.h4rsh41.paisatracker.domain.models

import com.h4rsh41.paisatracker.data.Budget

/**
 * Represents the progress of a budget with spending information.
 * Used for displaying budget status and progress bars in the UI.
 *
 * @property budget The budget being tracked
 * @property spent Amount spent against this budget
 * @property remaining Amount remaining in the budget (can be negative if overspent)
 * @property percentage Percentage of budget used (0-100+)
 * @property isOverBudget Whether spending has exceeded the budget limit
 * @property daysRemaining Days remaining in the budget period (if applicable)
 */
data class BudgetProgress(
    val budget: Budget,
    val spent: Double,
    val remaining: Double,
    val percentage: Double,
    val isOverBudget: Boolean,
    val daysRemaining: Int? = null
) {
    /**
     * Get the status of the budget based on percentage used.
     */
    fun getStatus(): BudgetStatus {
        return when {
            percentage >= 100.0 -> BudgetStatus.EXCEEDED
            percentage >= 90.0 -> BudgetStatus.CRITICAL
            percentage >= 75.0 -> BudgetStatus.WARNING
            percentage >= 50.0 -> BudgetStatus.MODERATE
            else -> BudgetStatus.HEALTHY
        }
    }
    
    /**
     * Get a color indicator for the budget status.
     */
    fun getStatusColor(): String {
        return when (getStatus()) {
            BudgetStatus.HEALTHY -> "#4CAF50"    // Green
            BudgetStatus.MODERATE -> "#8BC34A"   // Light Green
            BudgetStatus.WARNING -> "#FFC107"    // Amber
            BudgetStatus.CRITICAL -> "#FF9800"   // Orange
            BudgetStatus.EXCEEDED -> "#F44336"   // Red
        }
    }
    
    companion object {
        /**
         * Create a BudgetProgress from a budget and spending amount.
         */
        fun from(budget: Budget, spent: Double, daysRemaining: Int? = null): BudgetProgress {
            val remaining = budget.limitAmount - spent
            val percentage = if (budget.limitAmount > 0) {
                (spent / budget.limitAmount) * 100.0
            } else {
                0.0
            }
            val isOverBudget = spent > budget.limitAmount
            
            return BudgetProgress(
                budget = budget,
                spent = spent,
                remaining = remaining,
                percentage = percentage,
                isOverBudget = isOverBudget,
                daysRemaining = daysRemaining
            )
        }
    }
}

/**
 * Budget status levels based on spending percentage.
 */
enum class BudgetStatus {
    HEALTHY,    // 0-50%
    MODERATE,   // 50-75%
    WARNING,    // 75-90%
    CRITICAL,   // 90-100%
    EXCEEDED    // 100%+
}

// Made with Bob