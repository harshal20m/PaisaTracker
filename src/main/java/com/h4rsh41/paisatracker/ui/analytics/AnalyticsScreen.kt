package com.h4rsh41.paisatracker.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.h4rsh41.paisatracker.domain.models.AnalyticsStatistics
import com.h4rsh41.paisatracker.domain.models.CategorySpending
import com.h4rsh41.paisatracker.domain.models.FinancialState
import com.h4rsh41.paisatracker.domain.models.MonthlyTotal
import com.h4rsh41.paisatracker.domain.models.TimePeriod
import com.h4rsh41.paisatracker.domain.models.UiState
import com.h4rsh41.paisatracker.domain.models.YearlyTotal
import com.h4rsh41.paisatracker.ui.common.DateRangePickerSheet
import com.h4rsh41.paisatracker.ui.components.*
import com.h4rsh41.paisatracker.ui.theme.PaisaTrackerTheme
import com.h4rsh41.paisatracker.viewmodel.AnalyticsViewModel

/**
 * AnalyticsScreen - Main analytics dashboard showing comprehensive financial insights
 * 
 * Features:
 * - Time period selection (Week/Month/Year/Custom/All Time)
 * - Key statistics cards (Total Spent, Budget, Savings, Categories)
 * - Financial health indicator with score
 * - Monthly/Yearly trend charts
 * - Category distribution pie chart
 * - Detailed category spending list
 * - Pull-to-refresh support
 * - Loading/Error/Empty states
 * 
 * @param viewModel AnalyticsViewModel instance
 * @param onNavigateBack Callback for back navigation
 * @param onCategoryClick Callback when a category is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateBack: () -> Unit,
    onCategoryClick: (CategorySpending) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Collect state from ViewModel
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val monthlyTrends by viewModel.monthlyTrends.collectAsStateWithLifecycle()
    val yearlyTrends by viewModel.yearlyTrends.collectAsStateWithLifecycle()
    val categorySpending by viewModel.categorySpending.collectAsStateWithLifecycle()
    val financialState by viewModel.financialState.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    
    var showDatePicker by remember { mutableStateOf(false) }
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AnalyticsTopBar(
                onNavigateBack = onNavigateBack,
                onRefresh = { viewModel.refreshAnalytics() },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Show loading state if any critical data is loading
                financialState is UiState.Loading || statistics is UiState.Loading -> {
                    LoadingState(
                        message = "Loading analytics...",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Show error state if any critical data failed
                financialState is UiState.Error -> {
                    ErrorState(
                        message = (financialState as UiState.Error).message,
                        onRetry = { viewModel.refreshAnalytics() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Show empty state if no data available
                financialState is UiState.Empty -> {
                    EmptyState(
                        title = "No Financial Data",
                        description = "Start tracking your expenses to see analytics",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Show content
                else -> {
                    AnalyticsContent(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { viewModel.selectPeriod(it) },
                        onCustomRangeClick = { showDatePicker = true },
                        monthlyTrends = monthlyTrends,
                        yearlyTrends = yearlyTrends,
                        categorySpending = categorySpending,
                        financialState = financialState,
                        statistics = statistics,
                        onCategoryClick = onCategoryClick
                    )
                }
            }
        }
    }
    
    // Date range picker sheet
    if (showDatePicker) {
        DateRangePickerSheet(
            onDateRangeSelected = { startMillis, endMillis ->
                viewModel.selectCustomRange(startMillis, endMillis)
                viewModel.selectPeriod(TimePeriod.CUSTOM)
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * Top app bar for analytics screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsTopBar(
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

/**
 * Main content of analytics screen
 */
@Composable
private fun AnalyticsContent(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    onCustomRangeClick: () -> Unit,
    monthlyTrends: UiState<List<MonthlyTotal>>,
    yearlyTrends: UiState<List<YearlyTotal>>,
    categorySpending: UiState<List<CategorySpending>>,
    financialState: UiState<FinancialState>,
    statistics: UiState<AnalyticsStatistics>,
    onCategoryClick: (CategorySpending) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Time Period Selector
        TimePeriodSelector(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected,
            onCustomRangeClick = onCustomRangeClick,
            customRangeText = getCustomRangeText(selectedPeriod)
        )
        
        // Key Statistics Grid
        when (statistics) {
            is UiState.Success -> {
                StatisticsSection(statistics.data)
            }
            is UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            else -> { /* Skip if error or empty */ }
        }
        
        // Financial Health Indicator
        when (financialState) {
            is UiState.Success -> {
                FinancialHealthIndicator(
                    financialState = financialState.data
                )
            }
            else -> { /* Skip if loading, error, or empty */ }
        }
        
        // Trend Charts Section
        TrendChartsSection(
            selectedPeriod = selectedPeriod,
            monthlyTrends = monthlyTrends,
            yearlyTrends = yearlyTrends
        )
        
        // Category Distribution Section
        CategoryDistributionSection(
            categorySpending = categorySpending,
            onCategoryClick = onCategoryClick
        )
    }
}

/**
 * Statistics cards section
 */
@Composable
private fun StatisticsSection(statistics: AnalyticsStatistics) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        StatisticsGrid(
            statistics = listOf(
                StatisticItem(
                    title = "Total Spent",
                    value = statistics.totalSpending,
                    icon = Icons.AutoMirrored.Filled.TrendingDown
                ),
                StatisticItem(
                    title = "Avg/Day",
                    value = statistics.dailyAverage,
                    icon = Icons.Default.CalendarToday
                ),
                StatisticItem(
                    title = "Transactions",
                    value = statistics.expenseCount.toDouble(),
                    icon = Icons.Default.Receipt
                ),
                StatisticItem(
                    title = "Avg/Expense",
                    value = statistics.averagePerExpense,
                    icon = Icons.Default.AttachMoney
                )
            )
        )
    }
}

/**
 * Trend charts section
 */
@Composable
private fun TrendChartsSection(
    selectedPeriod: TimePeriod,
    monthlyTrends: UiState<List<MonthlyTotal>>,
    yearlyTrends: UiState<List<YearlyTotal>>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Spending Trends",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Show monthly trends for shorter periods, yearly for longer
        when {
            selectedPeriod == TimePeriod.THIS_YEAR || selectedPeriod == TimePeriod.ALL_TIME -> {
                when (yearlyTrends) {
                    is UiState.Success -> {
                        TrendChart(
                            dataPoints = yearlyTrends.data.map { it.toTrendDataPoint() }
                        )
                    }
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Empty -> {
                        TrendChart(dataPoints = emptyList())
                    }
                    else -> { /* Skip error state */ }
                }
            }
            else -> {
                when (monthlyTrends) {
                    is UiState.Success -> {
                        TrendChart(
                            dataPoints = monthlyTrends.data.map { it.toTrendDataPoint() }
                        )
                    }
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Empty -> {
                        TrendChart(dataPoints = emptyList())
                    }
                    else -> { /* Skip error state */ }
                }
            }
        }
    }
}

/**
 * Category distribution section
 */
@Composable
private fun CategoryDistributionSection(
    categorySpending: UiState<List<CategorySpending>>,
    onCategoryClick: (CategorySpending) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Category Breakdown",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        when (categorySpending) {
            is UiState.Success -> {
                if (categorySpending.data.isNotEmpty()) {
                    // Pie chart
                    CategoryPieChart(
                        categories = categorySpending.data,
                        showLegend = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Detailed list
                    CategorySpendingList(
                        categories = categorySpending.data,
                        onCategoryClick = onCategoryClick,
                        showBudgetComparison = true
                    )
                } else {
                    EmptyState(
                        title = "No Categories",
                        description = "Start categorizing your expenses"
                    )
                }
            }
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Empty -> {
                EmptyState(
                    title = "No Categories",
                    description = "Start categorizing your expenses"
                )
            }
            is UiState.Error -> {
                ErrorState(
                    message = categorySpending.message,
                    onRetry = { /* Handled by parent */ }
                )
            }
        }
    }
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

private fun getCustomRangeText(period: TimePeriod): String? {
    return if (period == TimePeriod.CUSTOM) {
        // This would be populated from ViewModel's custom range
        "Custom Range"
    } else {
        null
    }
}

// ============================================================================
// PREVIEW
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun AnalyticsScreenPreview() {
    PaisaTrackerTheme {
        // Preview with mock data would go here
        // Requires ViewModel instance which is complex for preview
        Surface {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Analytics Screen Preview")
            }
        }
    }
}

// Made with Bob
