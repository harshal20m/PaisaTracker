package com.h4rsh41.paisatracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.h4rsh41.paisatracker.data.PaisaTrackerRepository
import com.h4rsh41.paisatracker.domain.models.CategorySpending
import com.h4rsh41.paisatracker.domain.models.DateRange
import com.h4rsh41.paisatracker.domain.models.FinancialState
import com.h4rsh41.paisatracker.domain.models.MonthlyTotal
import com.h4rsh41.paisatracker.domain.models.TimePeriod
import com.h4rsh41.paisatracker.domain.models.UiState
import com.h4rsh41.paisatracker.domain.models.YearlyTotal
import com.h4rsh41.paisatracker.util.TimePeriodManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for Analytics features.
 * Provides time-period based analytics, trends, and financial insights.
 *
 * Features:
 * - Time period selection (Week, Month, Year, Custom, All Time)
 * - Monthly and yearly spending trends
 * - Category-wise spending breakdown
 * - Financial health status
 * - Statistical analysis
 *
 * Uses UiState pattern for consistent state management.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AnalyticsViewModel(
    private val repository: PaisaTrackerRepository
) : ViewModel() {

    // ============================================================================
    // Time Period Selection
    // ============================================================================

    private val _selectedPeriod = MutableStateFlow(TimePeriod.THIS_MONTH)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    private val _customDateRange = MutableStateFlow<DateRange?>(null)
    val customDateRange: StateFlow<DateRange?> = _customDateRange.asStateFlow()

    /**
     * Refresh trigger for analytics data.
     * Incremented whenever analytics need to be refreshed (e.g., project status change).
     */
    private val _refreshTrigger = MutableStateFlow(0L)

    /**
     * Get the current date range based on selected period.
     */
    val currentDateRange: StateFlow<DateRange> = combine(
        _selectedPeriod,
        _customDateRange
    ) { period, customRange ->
        when (period) {
            TimePeriod.THIS_WEEK -> TimePeriodManager.getCurrentWeek()
            TimePeriod.THIS_MONTH -> TimePeriodManager.getCurrentMonth()
            TimePeriod.THIS_YEAR -> TimePeriodManager.getCurrentYear()
            TimePeriod.CUSTOM -> customRange ?: TimePeriodManager.getCurrentMonth()
            TimePeriod.ALL_TIME -> TimePeriodManager.getAllTime()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimePeriodManager.getCurrentMonth())

    // ============================================================================
    // Monthly Trends
    // ============================================================================

    /**
     * Get monthly spending trends for the last 12 months.
     * Returns UiState with loading, success, error, or empty states.
     */
    val monthlyTrends: StateFlow<UiState<List<MonthlyTotal>>> = _refreshTrigger
        .flatMapLatest {
            repository.getMonthlyTotals(12)
                .map { totals ->
                    if (totals.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(totals)
                    }
                }
                .catch { e ->
                    emit(UiState.Error(e.message ?: "Failed to load monthly trends", e))
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    // ============================================================================
    // Yearly Trends
    // ============================================================================

    /**
     * Get yearly spending trends for all years with data.
     * Returns UiState with loading, success, error, or empty states.
     */
    val yearlyTrends: StateFlow<UiState<List<YearlyTotal>>> = _refreshTrigger
        .flatMapLatest {
            repository.getYearlyTotals()
                .map { totals ->
                    if (totals.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(totals)
                    }
                }
                .catch { e ->
                    emit(UiState.Error(e.message ?: "Failed to load yearly trends", e))
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    // ============================================================================
    // Category Spending
    // ============================================================================

    /**
     * Get category-wise spending for the current selected period.
     * Automatically calculates percentages.
     */
    val categorySpending: StateFlow<UiState<List<CategorySpending>>> = combine(
        currentDateRange,
        _refreshTrigger
    ) { range, _ -> range }
        .flatMapLatest { range ->
            repository.getCategorySpendingByDateRange(range.start, range.end)
                .map { categories ->
                    if (categories.isEmpty()) {
                        UiState.Empty
                    } else {
                        // Calculate percentages
                        val withPercentages = CategorySpending.calculatePercentages(categories)
                        UiState.Success(withPercentages)
                    }
                }
                .catch { e ->
                    emit(UiState.Error(e.message ?: "Failed to load category spending", e))
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    /**
     * Get top N categories by spending for the current period.
     */
    fun getTopCategories(limit: Int = 5): StateFlow<UiState<List<CategorySpending>>> {
        return currentDateRange
            .flatMapLatest { range ->
                repository.getTopCategoriesByDateRange(range.start, range.end, limit)
                    .map { categories ->
                        if (categories.isEmpty()) {
                            UiState.Empty
                        } else {
                            val withPercentages = CategorySpending.calculatePercentages(categories)
                            UiState.Success(withPercentages)
                        }
                    }
                    .catch { e ->
                        emit(UiState.Error(e.message ?: "Failed to load top categories", e))
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)
    }

    // ============================================================================
    // Financial State (Unified View)
    // ============================================================================

    /**
     * Get unified financial state for the current period.
     * Combines income (salary), expenses, and budget data.
     *
     * ENHANCED in Sprint 7: Now uses real salary and budget data
     */
    val financialState: StateFlow<UiState<FinancialState>> = combine(
        currentDateRange,
        _selectedPeriod
    ) { range, period ->
        try {
            val totalExpenses = repository.getTotalByDateRange(range.start, range.end)
            
            // Get salary for the current month (if period is THIS_MONTH)
            val totalIncome = if (period == TimePeriod.THIS_MONTH) {
                val calendar = java.util.Calendar.getInstance()
                val month = calendar.get(java.util.Calendar.MONTH) + 1
                val year = calendar.get(java.util.Calendar.YEAR)
                
                // Use firstOrNull() instead of collect to avoid blocking
                val salaryRecord = repository.getCurrentMonthSalary(month, year).firstOrNull()
                salaryRecord?.amount ?: 0.0
            } else {
                0.0 // For other periods, we don't have salary tracking yet
            }
            
            // Get total budget for the period
            val totalBudget = repository.getTotalBudgetForPeriod(range.start, range.end)
            
            // Calculate savings (income - expenses)
            val totalSavings = totalIncome - totalExpenses
            
            val state = FinancialState(
                period = period,
                totalIncome = totalIncome,
                totalExpenses = totalExpenses,
                totalBudget = totalBudget,
                totalSavings = totalSavings
            )
            
            UiState.Success(state)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to load financial state", e)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    // ============================================================================
    // Statistics
    // ============================================================================

    /**
     * Get statistical analysis for the current period.
     */
    val statistics: StateFlow<UiState<com.h4rsh41.paisatracker.domain.models.AnalyticsStatistics>> = combine(
        currentDateRange,
        _refreshTrigger
    ) { range, _ -> range }
        .map { range ->
            try {
                val total = repository.getTotalByDateRange(range.start, range.end)
                val count = repository.getCountByDateRange(range.start, range.end)
                val dailyAverage = repository.getAverageDailySpending(range.start, range.end)
                
                val stats = com.h4rsh41.paisatracker.domain.models.AnalyticsStatistics(
                    totalSpending = total,
                    expenseCount = count,
                    averagePerExpense = if (count > 0) total / count else 0.0,
                    dailyAverage = dailyAverage,
                    period = _selectedPeriod.value,
                    dateRange = range
                )
                
                UiState.Success(stats)
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to load statistics", e)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    // ============================================================================
    // Budget Progress Tracking - Added in Sprint 7
    // ============================================================================

    /**
     * Get budget progress for all active budgets in the current period.
     * Shows how much has been spent against each budget.
     */
    val budgetProgress: StateFlow<UiState<List<com.h4rsh41.paisatracker.domain.models.BudgetProgress>>> =
        currentDateRange.flatMapLatest { range ->
            repository.getAllActiveBudgets().map { budgets ->
                try {
                    val progressList = budgets.map { budget ->
                        val spent = repository.getSpendingForBudget(budget, range.start, range.end)
                        
                        // Calculate days remaining for monthly budgets
                        val daysRemaining = if (budget.period == com.h4rsh41.paisatracker.data.BudgetPeriod.MONTHLY) {
                            val calendar = java.util.Calendar.getInstance()
                            val lastDay = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                            lastDay - currentDay
                        } else {
                            null
                        }
                        
                        com.h4rsh41.paisatracker.domain.models.BudgetProgress.from(
                            budget = budget,
                            spent = spent,
                            daysRemaining = daysRemaining
                        )
                    }
                    
                    if (progressList.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(progressList)
                    }
                } catch (e: Exception) {
                    UiState.Error(e.message ?: "Failed to load budget progress", e)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    // ============================================================================
    // Actions
    // ============================================================================

    /**
     * Select a time period for analytics.
     * Automatically updates all analytics data.
     */
    fun selectPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
        if (period != TimePeriod.CUSTOM) {
            _customDateRange.value = null
        }
    }

    /**
     * Select a custom date range.
     * Automatically switches to CUSTOM period.
     */
    fun selectCustomRange(startDate: Long, endDate: Long) {
        _customDateRange.value = TimePeriodManager.getCustomRange(startDate, endDate)
        _selectedPeriod.value = TimePeriod.CUSTOM
    }

    /**
     * Refresh all analytics data.
     * Useful for pull-to-refresh functionality or when project status changes.
     *
     * This method should be called when:
     * - User pulls to refresh
     * - Project is closed/reopened
     * - Category is added/removed
     * - Any change that affects analytics data
     */
    fun refreshAnalytics() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    /**
     * Get monthly totals for a specific year.
     */
    fun getMonthlyTotalsForYear(year: Int): StateFlow<UiState<List<MonthlyTotal>>> {
        return repository.getMonthlyTotalsForYear(year.toString())
            .map { totals ->
                if (totals.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(totals)
                }
            }
            .catch { e ->
                emit(UiState.Error(e.message ?: "Failed to load monthly data for year", e))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)
    }
}


// Made with Bob
