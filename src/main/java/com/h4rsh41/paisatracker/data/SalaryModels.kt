package com.h4rsh41.paisatracker.data

/**
 * Salary source types for categorization
 */
object SalarySourceType {
    const val PRIMARY = "PRIMARY"           // Main job salary
    const val FREELANCE = "FREELANCE"       // Freelance/contract work
    const val BONUS = "BONUS"               // One-time bonus
    const val PASSIVE = "PASSIVE"           // Rental, dividends, etc.
    const val OTHER = "OTHER"               // Other income
    
    fun getDisplayName(type: String): String = when (type) {
        PRIMARY -> "Primary Job"
        FREELANCE -> "Freelance"
        BONUS -> "Bonus"
        PASSIVE -> "Passive Income"
        OTHER -> "Other"
        else -> "Unknown"
    }
    
    fun getEmoji(type: String): String = when (type) {
        PRIMARY -> "💼"
        FREELANCE -> "🎨"
        BONUS -> "🎁"
        PASSIVE -> "📈"
        OTHER -> "💰"
        else -> "💵"
    }
}

/**
 * Aggregated view of all salaries for a month
 */
data class MonthlySalarySummary(
    val month: Int,
    val year: Int,
    val totalIncome: Double,
    val salaries: List<SalaryRecord>,
    val totalSpent: Double,
    val remainingBalance: Double,
    val spendPercentage: Float
)

/**
 * Salary with linked account details
 */
data class SalaryWithAccount(
    val salary: SalaryRecord,
    val account: BankAccount
)

/**
 * Per-salary spending breakdown
 */
data class SalarySpendingBreakdown(
    val salaryId: Long,
    val salaryAmount: Double,
    val sourceName: String,
    val sourceType: String,
    val accountName: String,
    val accountEmoji: String,
    val totalSpent: Double,
    val remainingBalance: Double,
    val spendPercentage: Float,
    val categoryBreakdown: List<CategorySpend>
)

// Made with Bob
