package com.h4rsh41.paisatracker.data

/**
 * Sealed class representing unified search results.
 * Supports both expense and category search results with proper hierarchy information.
 */
sealed class SearchResult {
    /**
     * Represents an expense search result with full context.
     */
    data class ExpenseResult(
        val expense: RecentExpense
    ) : SearchResult()

    /**
     * Represents a category search result with project context.
     */
    data class CategoryResult(
        val categoryId: Long,
        val categoryName: String,
        val categoryEmoji: String,
        val projectId: Long,
        val projectName: String,
        val projectEmoji: String,
        val expenseCount: Int,
        val totalAmount: Double
    ) : SearchResult()
}

/**
 * Data class for category search results from database.
 */
data class CategorySearchResult(
    val categoryId: Long,
    val categoryName: String,
    val categoryEmoji: String,
    val projectId: Long,
    val projectName: String,
    val projectEmoji: String,
    val expenseCount: Int,
    val totalAmount: Double
)

// Made with Bob
