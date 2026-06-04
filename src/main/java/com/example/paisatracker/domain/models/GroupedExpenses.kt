package com.example.paisatracker.domain.models

import com.example.paisatracker.data.Expense
import java.util.Calendar

/**
 * Represents expenses grouped by month
 */
data class MonthGroup(
    val year: Int,
    val month: Int, // 0-11 (Calendar.JANUARY to Calendar.DECEMBER)
    val expenses: List<Expense>,
    val totalAmount: Double
) {
    val monthName: String
        get() {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, month)
            return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault()) ?: ""
        }
    
    val expenseCount: Int
        get() = expenses.size
}

/**
 * Represents expenses grouped by year with monthly breakdown
 */
data class YearGroup(
    val year: Int,
    val monthGroups: List<MonthGroup>,
    val totalAmount: Double,
    val expenseCount: Int
)

/**
 * Helper function to group expenses by year and month
 */
fun List<Expense>.groupByYearAndMonth(): List<YearGroup> {
    val calendar = Calendar.getInstance()
    
    // Group by year first
    val byYear = this.groupBy { expense ->
        calendar.timeInMillis = expense.date
        calendar.get(Calendar.YEAR)
    }
    
    return byYear.map { (year, yearExpenses) ->
        // Group by month within each year
        val byMonth = yearExpenses.groupBy { expense ->
            calendar.timeInMillis = expense.date
            calendar.get(Calendar.MONTH)
        }
        
        val monthGroups = byMonth.map { (month, monthExpenses) ->
            MonthGroup(
                year = year,
                month = month,
                expenses = monthExpenses.sortedByDescending { it.date },
                totalAmount = monthExpenses.filter { it.amount > 0 }.sumOf { it.amount }
            )
        }.sortedByDescending { it.month } // Sort months descending (newest first)
        
        YearGroup(
            year = year,
            monthGroups = monthGroups,
            totalAmount = yearExpenses.filter { it.amount > 0 }.sumOf { it.amount },
            expenseCount = yearExpenses.size
        )
    }.sortedByDescending { it.year } // Sort years descending (newest first)
}

// Made with Bob
