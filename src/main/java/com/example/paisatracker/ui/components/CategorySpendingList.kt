package com.example.paisatracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.paisatracker.domain.models.CategorySpending
import com.example.paisatracker.ui.theme.PaisaTrackerTheme
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)

/**
 * CategorySpendingList - A Material 3 component for displaying category-wise spending breakdown
 * 
 * Features:
 * - List of categories with spending amounts and percentages
 * - Visual progress bars for each category
 * - Color-coded categories
 * - Expandable details (budget comparison, transaction count)
 * - Sort options (by amount, by name, by percentage)
 * - Material 3 styling
 * 
 * @param categories List of category spending data
 * @param onCategoryClick Callback when a category is clicked
 * @param showBudgetComparison Whether to show budget comparison
 * @param currencySymbol Currency symbol to display
 * @param modifier Modifier for the component
 */
@Composable
fun CategorySpendingList(
    categories: List<CategorySpending>,
    onCategoryClick: (CategorySpending) -> Unit = {},
    showBudgetComparison: Boolean = true,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    var sortOption by remember { mutableStateOf(CategorySortOption.BY_AMOUNT) }
    var isGridView by remember { mutableStateOf(true) } // Default to grid view
    
    val sortedCategories = remember(categories, sortOption) {
        when (sortOption) {
            CategorySortOption.BY_AMOUNT -> categories.sortedByDescending { it.total }
            CategorySortOption.BY_NAME -> categories.sortedBy { it.categoryName }
            CategorySortOption.BY_PERCENTAGE -> categories.sortedByDescending { it.percentage }
        }
    }
    
    Column(modifier = modifier) {
        // Sort options and view toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategorySortChips(
                selectedOption = sortOption,
                onOptionSelected = { sortOption = it },
                modifier = Modifier.weight(1f)
            )
            
            // View toggle button
            IconButton(
                onClick = { isGridView = !isGridView },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Category list or grid
        if (isGridView) {
            // Grid layout using FlowRow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 3
            ) {
                sortedCategories.forEach { category ->
                    CategoryGridItem(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        currencySymbol = currencySymbol,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // List layout
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sortedCategories.forEach { category ->
                    CategorySpendingItem(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        showBudgetComparison = showBudgetComparison,
                        currencySymbol = currencySymbol
                    )
                }
            }
        }
    }
}

/**
 * Individual category spending item
 */
@Composable
private fun CategorySpendingItem(
    category: CategorySpending,
    onClick: () -> Unit,
    showBudgetComparison: Boolean,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Main content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category emoji/icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(getCategoryColor(category.categoryName)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.categoryIcon,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Column {
                        Text(
                            text = category.categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "${category.count} transaction${if (category.count != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Amount and percentage
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatCurrency(category.total, currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "${String.format("%.1f", category.percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Expand icon
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            CategoryProgressBar(
                percentage = category.percentage,
                color = getCategoryColor(category.categoryName)
            )
            
            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider()
                    
                    // Budget comparison removed - not available in CategorySpending
                    // Will be added in Sprint 7 when Budget integration is complete
                    
                    // Average per transaction
                    DetailRow(
                        label = "Avg per transaction",
                        value = formatCurrency(category.getAveragePerExpense(), currencySymbol)
                    )
                    
                    // Date range removed - not available in CategorySpending
                    // Will be added in Sprint 7 when enhanced analytics are implemented
                }
            }
        }
    }
}

/**
 * Compact grid item for category spending
 */
@Composable
fun CategoryGridItem(
    category: CategorySpending,
    onClick: () -> Unit,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category icon with background
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(category.categoryName)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.categoryIcon,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            // Category name
            Text(
                text = category.categoryName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            
            // Amount
            Text(
                text = formatCurrency(category.total, currencySymbol),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Percentage and transaction count
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "${String.format("%.1f", category.percentage)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "${category.count} txn",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Progress bar
            CategoryProgressBar(
                percentage = category.percentage,
                color = getCategoryColor(category.categoryName),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


/**
 * Progress bar showing category percentage
 */
@Composable
private fun CategoryProgressBar(
    percentage: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (percentage / 100f).toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "progress_animation"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}

/**
 * Budget comparison row
 */
@Composable
private fun BudgetComparisonRow(
    spent: Double,
    budget: Double,
    currencySymbol: String
) {
    val remaining = budget - spent
    val isOverBudget = remaining < 0
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Budget",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatCurrency(budget, currencySymbol),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "${if (isOverBudget) "Over" else "Remaining"}: ${formatCurrency(Math.abs(remaining), currencySymbol)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isOverBudget) {
                    MaterialTheme.colorScheme.error
                } else {
                    Color(0xFF4CAF50)
                }
            )
        }
    }
}

/**
 * Generic detail row
 */
@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Sort chips for category list
 */
@Composable
private fun CategorySortChips(
    selectedOption: CategorySortOption,
    onOptionSelected: (CategorySortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategorySortOption.entries.forEach { option ->
            FilterChip(
                selected = selectedOption == option,
                onClick = { onOptionSelected(option) },
                label = {
                    Text(
                        text = when (option) {
                            CategorySortOption.BY_AMOUNT -> "Amount"
                            CategorySortOption.BY_NAME -> "Name"
                            CategorySortOption.BY_PERCENTAGE -> "Percentage"
                        }
                    )
                }
            )
        }
    }
}

// ============================================================================
// DATA CLASSES & ENUMS
// ============================================================================

enum class CategorySortOption {
    BY_AMOUNT,
    BY_NAME,
    BY_PERCENTAGE
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

private fun formatCurrency(value: Double, symbol: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 2
    return "$symbol ${formatter.format(value)}"
}

private fun formatDateRange(start: Long, end: Long): String {
    val dateFormat = java.text.SimpleDateFormat("MMM dd", Locale.getDefault())
    return "${dateFormat.format(Date(start))} - ${dateFormat.format(Date(end))}"
}

private fun getCategoryColor(categoryName: String): Color {
    // Generate consistent color based on category name
    val colors = listOf(
        Color(0xFFE57373), Color(0xFFBA68C8), Color(0xFF64B5F6),
        Color(0xFF4DB6AC), Color(0xFFAED581), Color(0xFFFFD54F),
        Color(0xFFFF8A65), Color(0xFF90A4AE)
    )
    val index = categoryName.hashCode().mod(colors.size)
    return colors[index].copy(alpha = 0.3f)
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun CategorySpendingListPreview() {
    PaisaTrackerTheme {
        Surface {
            CategorySpendingList(
                categories = listOf(
                    CategorySpending(
                        categoryId = 1,
                        categoryName = "Food & Dining",
                        categoryIcon = "🍔",
                        total = 15000.0,
                        count = 45,
                        percentage = 35.0
                    ),
                    CategorySpending(
                        categoryId = 2,
                        categoryName = "Transportation",
                        categoryIcon = "🚗",
                        total = 8000.0,
                        count = 20,
                        percentage = 18.6
                    ),
                    CategorySpending(
                        categoryId = 3,
                        categoryName = "Shopping",
                        categoryIcon = "🛍️",
                        total = 12000.0,
                        count = 15,
                        percentage = 27.9
                    )
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategorySpendingListDarkPreview() {
    PaisaTrackerTheme {
        Surface {
            CategorySpendingList(
                categories = listOf(
                    CategorySpending(
                        categoryId = 1,
                        categoryName = "Entertainment",
                        categoryIcon = "🎬",
                        total = 5000.0,
                        count = 10,
                        percentage = 45.0
                    )
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// Made with Bob
