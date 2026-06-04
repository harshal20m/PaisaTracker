package com.example.paisatracker.ui.expense

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paisatracker.data.Expense
import com.example.paisatracker.domain.models.MonthGroup
import com.example.paisatracker.domain.models.YearGroup
import com.example.paisatracker.ui.common.SortDropdown
import com.example.paisatracker.ui.common.SortOption
import com.example.paisatracker.util.formatCurrency

/**
 * Sticky Month Header with background for better visibility
 */
@Composable
fun StickyMonthHeader(
    monthGroup: MonthGroup,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Horizontal line (takes remaining space)
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            
            // Legend info with labels and better spacing
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transactions: ${monthGroup.expenseCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                
                Text(
                    text = monthGroup.monthName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                
                Text(
                    text = "Total: ${formatCurrency(monthGroup.totalAmount)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Horizontal line (takes remaining space)
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Collapsing Summary Header that animates based on scroll
 */
@Composable
fun CollapsingSummaryHeader(
    expenses: List<Expense>,
    isCollapsed: Boolean,
    modifier: Modifier = Modifier
) {
    val totalExpenses = expenses.filter { it.amount > 0 }.sumOf { it.amount }
    val totalCredits = expenses.filter { it.amount < 0 }.sumOf { kotlin.math.abs(it.amount) }
    val expenseCount = expenses.count { it.amount > 0 }
    val creditCount = expenses.count { it.amount < 0 }

    val cardHeight by animateDpAsState(
        targetValue = if (isCollapsed) 0.dp else 120.dp,
        animationSpec = tween(durationMillis = 300),
        label = "cardHeight"
    )
    
    val cardAlpha by animateFloatAsState(
        targetValue = if (isCollapsed) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "cardAlpha"
    )

    if (cardHeight > 0.dp) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(cardHeight)
                .padding(horizontal = 16.dp)
                .alpha(cardAlpha),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Spending",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(totalExpenses),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val countsText = buildString {
                        if (expenseCount > 0) append("$expenseCount expense${if (expenseCount != 1) "s" else ""}")
                        if (creditCount > 0) {
                            if (expenseCount > 0) append(" • ")
                            append("$creditCount credit${if (creditCount != 1) "s" else ""} (${formatCurrency(totalCredits)})")
                        }
                    }
                    Text(
                        text = countsText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${expenses.size}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Grouped Expense List View with Collapsing Header and Sticky Month Headers
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupedExpenseListViewWithCollapsingHeader(
    expenses: List<Expense>,
    yearGroups: List<YearGroup>,
    nextYearToLoad: Int?,
    isLoadingYear: Boolean,
    currencySymbol: String,
    currentViewType: ExpenseViewType,
    onViewTypeChange: (ExpenseViewType) -> Unit,
    expenseSortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    onAddExpenseClick: () -> Unit,
    onExpenseClick: (Expense) -> Unit,
    onEditClick: (Expense) -> Unit,
    onDeleteClick: (Expense) -> Unit,
    onLoadMoreClick: () -> Unit
) {
    val listState = rememberLazyListState()
    
    // Track if we should show collapsed header (when scrolled past first item)
    val isHeaderCollapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || 
            (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset > 100)
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Collapsing Summary Header
        CollapsingSummaryHeader(
            expenses = expenses,
            isCollapsed = isHeaderCollapsed,
            modifier = Modifier.padding(top = 16.dp)
        )
        
        // Always visible controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = if (isHeaderCollapsed) 2.dp else 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpenseViewTypeToggle(
                    currentViewType = currentViewType,
                    onViewTypeChange = onViewTypeChange
                )

                SortDropdown(current = expenseSortOption, onChange = onSortOptionChange)
            }
        }
        
        // Scrollable content with sticky headers
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add expense button at top
            item(key = "ADD_BUTTON") {
                AddExpenseListItem(onClick = onAddExpenseClick)
            }
            
            // Iterate through year groups
            yearGroups.forEachIndexed { yearIndex, yearGroup ->
                // Year separator (except for first year)
                if (yearIndex > 0) {
                    item(key = "YEAR_SEP_${yearGroup.year}") {
                        YearSeparator(year = yearGroup.year)
                    }
                }
                
                // Month groups within the year
                yearGroup.monthGroups.forEach { monthGroup ->
                    // Sticky Month header
                    stickyHeader(key = "MONTH_${yearGroup.year}_${monthGroup.month}") {
                        StickyMonthHeader(monthGroup = monthGroup)
                    }
                    
                    // Expenses in this month
                    items(
                        items = monthGroup.expenses,
                        key = { expense -> "EXP_${expense.id}" }
                    ) { expense ->
                        ExpenseListItem(
                            expense = expense,
                            onClick = { onExpenseClick(expense) },
                            onEditClick = { onEditClick(expense) },
                            onDeleteClick = { onDeleteClick(expense) }
                        )
                    }
                }
            }
            
            // Load more button
            if (nextYearToLoad != null) {
                item(key = "LOAD_MORE") {
                    LoadMoreYearButton(
                        year = nextYearToLoad,
                        isLoading = isLoadingYear,
                        onClick = onLoadMoreClick,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}

// Made with Bob
/**
 * Grouped Expense Grid View with Collapsing Header and Sticky Month Headers
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupedExpenseGridViewWithCollapsingHeader(
    expenses: List<Expense>,
    yearGroups: List<YearGroup>,
    nextYearToLoad: Int?,
    isLoadingYear: Boolean,
    currencySymbol: String,
    currentViewType: ExpenseViewType,
    onViewTypeChange: (ExpenseViewType) -> Unit,
    expenseSortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    onAddExpenseClick: () -> Unit,
    onExpenseClick: (Expense) -> Unit,
    onEditClick: (Expense) -> Unit,
    onDeleteClick: (Expense) -> Unit,
    onLoadMoreClick: () -> Unit
) {
    val listState = rememberLazyListState()
    
    // Track if we should show collapsed header
    val isHeaderCollapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || 
            (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset > 100)
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Collapsing Summary Header
        CollapsingSummaryHeader(
            expenses = expenses,
            isCollapsed = isHeaderCollapsed,
            modifier = Modifier.padding(top = 16.dp)
        )
        
        // Always visible controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = if (isHeaderCollapsed) 2.dp else 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpenseViewTypeToggle(
                    currentViewType = currentViewType,
                    onViewTypeChange = onViewTypeChange
                )

                SortDropdown(current = expenseSortOption, onChange = onSortOptionChange)
            }
        }
        
        // Scrollable grid content with sticky headers
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add expense button at top (in grid format)
            item(key = "ADD_BUTTON") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AddExpenseGridItem(
                        onClick = onAddExpenseClick,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            
            // Iterate through year groups
            yearGroups.forEachIndexed { yearIndex, yearGroup ->
                // Year separator (except for first year)
                if (yearIndex > 0) {
                    item(key = "YEAR_SEP_${yearGroup.year}") {
                        YearSeparator(year = yearGroup.year)
                    }
                }
                
                // Month groups within the year
                yearGroup.monthGroups.forEach { monthGroup ->
                    // Sticky Month header
                    stickyHeader(key = "MONTH_${yearGroup.year}_${monthGroup.month}") {
                        StickyMonthHeader(monthGroup = monthGroup)
                    }
                    
                    // Expenses in this month (in grid format - 2 columns)
                    val chunkedExpenses = monthGroup.expenses.chunked(2)
                    items(
                        items = chunkedExpenses,
                        key = { rowExpenses -> "ROW_${rowExpenses.firstOrNull()?.id ?: 0}" }
                    ) { rowExpenses ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowExpenses.forEach { expense ->
                                ExpenseGridItem(
                                    expense = expense,
                                    onClick = { onExpenseClick(expense) },
                                    onEditClick = { onEditClick(expense) },
                                    onDeleteClick = { onDeleteClick(expense) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Add spacer if only one item in row
                            if (rowExpenses.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            
            // Load more button
            if (nextYearToLoad != null) {
                item(key = "LOAD_MORE") {
                    LoadMoreYearButton(
                        year = nextYearToLoad,
                        isLoading = isLoadingYear,
                        onClick = onLoadMoreClick,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}
