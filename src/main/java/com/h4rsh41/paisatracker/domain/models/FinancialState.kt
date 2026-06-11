package com.h4rsh41.paisatracker.domain.models

/**
 * Represents the overall financial state for a given time period
 * Combines income, expenses, and budget data for unified view
 */
data class FinancialState(
    val period: TimePeriod = TimePeriod.THIS_MONTH,
    val dateRange: DateRange? = null,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalBudget: Double = 0.0,
    val balance: Double = 0.0,
    val totalSavings: Double = 0.0,
    val savingsRate: Double = 0.0,
    val budgetUtilization: Double = 0.0
) {
    /**
     * Calculate if user is over budget
     */
    fun isOverBudget(): Boolean = totalExpenses > totalBudget && totalBudget > 0
    
    /**
     * Calculate if user is in deficit (expenses > income)
     */
    fun isInDeficit(): Boolean = totalExpenses > totalIncome && totalIncome > 0
    
    /**
     * Get financial health status
     */
    fun getHealthStatus(): HealthStatus {
        return when {
            isInDeficit() -> HealthStatus.CRITICAL
            budgetUtilization > 0.9 -> HealthStatus.WARNING
            budgetUtilization > 0.7 -> HealthStatus.MODERATE
            else -> HealthStatus.HEALTHY
        }
    }
    
    /**
     * Calculate projected month-end balance based on current spending rate
     */
    fun getProjectedBalance(daysInPeriod: Int, daysElapsed: Int): Double {
        if (daysElapsed == 0 || daysInPeriod == 0) return balance
        val dailySpendingRate = totalExpenses / daysElapsed
        val projectedTotalExpenses = dailySpendingRate * daysInPeriod
        return totalIncome - projectedTotalExpenses
    }
}

/**
 * Health status for financial state
 */
enum class HealthStatus {
    HEALTHY,    // Under 70% budget utilization
    MODERATE,   // 70-90% budget utilization
    WARNING,    // Over 90% budget utilization
    CRITICAL    // Over budget or in deficit
}

// Made with Bob
