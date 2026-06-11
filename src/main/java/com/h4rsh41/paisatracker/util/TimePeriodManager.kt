package com.h4rsh41.paisatracker.util

import com.h4rsh41.paisatracker.domain.models.DateRange
import com.h4rsh41.paisatracker.domain.models.TimePeriod
import java.util.Calendar

/**
 * Utility class for managing time periods and date ranges
 * Provides consistent date range calculations across the app
 */
object TimePeriodManager {
    
    /**
     * Get date range for the current week (Monday to Sunday)
     */
    fun getCurrentWeek(): DateRange {
        val calendar = Calendar.getInstance()
        
        // Set to start of week (Monday)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        // Set to end of week (Sunday)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        
        return DateRange(start, end, "This Week")
    }
    
    /**
     * Get date range for the current month
     */
    fun getCurrentMonth(): DateRange {
        val calendar = Calendar.getInstance()
        
        // Set to start of month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        // Set to end of month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        
        return DateRange(start, end, "This Month")
    }
    
    /**
     * Get date range for the current year
     */
    fun getCurrentYear(): DateRange {
        val calendar = Calendar.getInstance()
        
        // Set to start of year
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        // Set to end of year
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        
        return DateRange(start, end, "This Year")
    }
    
    /**
     * Get custom date range
     */
    fun getCustomRange(start: Long, end: Long, label: String = "Custom"): DateRange {
        return DateRange(start, end, label)
    }
    
    /**
     * Get all-time date range (from epoch to now)
     */
    fun getAllTime(): DateRange {
        val now = System.currentTimeMillis()
        return DateRange(0, now, "All Time")
    }
    
    /**
     * Get date range for last N months
     */
    fun getLastNMonths(n: Int): DateRange {
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, -n)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        return DateRange(start, end, "Last $n Months")
    }
    
    /**
     * Get date range for last N years
     */
    fun getLastNYears(n: Int): DateRange {
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis
        
        calendar.add(Calendar.YEAR, -n)
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        return DateRange(start, end, "Last $n Years")
    }
    
    /**
     * Get date range for a specific month and year
     */
    fun getMonthRange(month: Int, year: Int): DateRange {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1) // Calendar months are 0-based
        
        // Start of month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        // End of month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        
        val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault())
        return DateRange(start, end, "$monthName $year")
    }
    
    /**
     * Get date range for a specific year
     */
    fun getYearRange(year: Int): DateRange {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        
        // Start of year
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        // End of year
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        
        return DateRange(start, end, "Year $year")
    }
    
    /**
     * Get date range based on TimePeriod enum
     */
    fun getDateRange(period: TimePeriod, customStart: Long? = null, customEnd: Long? = null): DateRange {
        return when (period) {
            TimePeriod.THIS_WEEK -> getCurrentWeek()
            TimePeriod.THIS_MONTH -> getCurrentMonth()
            TimePeriod.THIS_YEAR -> getCurrentYear()
            TimePeriod.ALL_TIME -> getAllTime()
            TimePeriod.CUSTOM -> {
                if (customStart != null && customEnd != null) {
                    getCustomRange(customStart, customEnd)
                } else {
                    getCurrentMonth() // Fallback
                }
            }
        }
    }
    
    /**
     * Get list of available years from expenses (for year selector)
     */
    fun getAvailableYears(oldestTimestamp: Long): List<Int> {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        
        calendar.timeInMillis = oldestTimestamp
        val oldestYear = calendar.get(Calendar.YEAR)
        
        return (oldestYear..currentYear).toList().reversed()
    }
    
    /**
     * Get list of months for a year (for month selector)
     */
    fun getMonthsInYear(year: Int): List<Pair<Int, String>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        
        return (1..12).map { month ->
            calendar.set(Calendar.MONTH, month - 1)
            val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault()) ?: ""
            month to monthName
        }
    }
}

// Made with Bob
