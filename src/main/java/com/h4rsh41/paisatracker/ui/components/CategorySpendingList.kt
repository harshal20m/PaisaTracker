package com.h4rsh41.paisatracker.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.h4rsh41.paisatracker.domain.models.CategorySpending
import com.h4rsh41.paisatracker.ui.theme.PaisaTrackerTheme
import com.h4rsh41.paisatracker.util.formatCurrency

/**
 * CategorySpendingList — Modern dashboard list with:
 * - Grid view (default): 2-column transparent thin-bordered cards
 * - List view: full-width rows with inline progress bar
 * - Sort chips (amount / name / percentage)
 * - Animated progress bars and percentage pills
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
    var isGridView by remember { mutableStateOf(true) } // Grid is default

    val sortedCategories = remember(categories, sortOption) {
        when (sortOption) {
            CategorySortOption.BY_AMOUNT -> categories.sortedByDescending { it.total }
            CategorySortOption.BY_NAME   -> categories.sortedBy { it.categoryName }
            CategorySortOption.BY_PERCENTAGE -> categories.sortedByDescending { it.percentage }
        }
    }

    Column(modifier = modifier) {
        // ── Controls row ─────────────────────────────────────────────────────
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
            IconButton(onClick = { isGridView = !isGridView }) {
                Icon(
                    imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                    contentDescription = if (isGridView) "Switch to list view" else "Switch to grid view",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // ── Grid or List ──────────────────────────────────────────────────────
        if (isGridView) {
            CategoryGrid(
                categories = sortedCategories,
                onCategoryClick = onCategoryClick,
                currencySymbol = currencySymbol
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sortedCategories.forEach { category ->
                    CategorySpendingRow(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        currencySymbol = currencySymbol
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Grid layout — 2-column, transparent cards with thin border
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategoryGrid(
    categories: List<CategorySpending>,
    onCategoryClick: (CategorySpending) -> Unit,
    currencySymbol: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { category ->
                    CategoryGridCard(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        currencySymbol = currencySymbol,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Pad the last row if odd number of items
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Transparent thin-bordered grid card
 */
@Composable
private fun CategoryGridCard(
    category: CategorySpending,
    onClick: () -> Unit,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (category.percentage / 100f).toFloat(),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "grid_progress"
    )
    val categoryColor = getCategoryColor(category.categoryName)

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon + percentage pill on the same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = category.categoryIcon, fontSize = 18.sp)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = categoryColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${String.format("%.1f", category.percentage)}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Category name
            Text(
                text = category.categoryName,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Amount
            Text(
                text = formatCurrency(category.total, currencySymbol),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Transaction count
            Text(
                text = "${category.count} txn${if (category.count != 1) "s" else ""}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Thin progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(categoryColor)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// List row — full width with icon, progress bar, amount + pill
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategorySpendingRow(
    category: CategorySpending,
    onClick: () -> Unit,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (category.percentage / 100f).toFloat(),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "row_progress"
    )
    val categoryColor = getCategoryColor(category.categoryName)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon bubble
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = category.categoryIcon, fontSize = 20.sp)
            }

            // Name + bar + count
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(categoryColor)
                    )
                }
                Text(
                    text = "${category.count} transaction${if (category.count != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount + pill
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatCurrency(category.total, currencySymbol),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = categoryColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${String.format("%.1f", category.percentage)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sort chips
// ─────────────────────────────────────────────────────────────────────────────

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
                        },
                        fontSize = 11.sp
                    )
                }
            )
        }
    }
}

// ============================================================================
// ENUMS
// ============================================================================

enum class CategorySortOption {
    BY_AMOUNT,
    BY_NAME,
    BY_PERCENTAGE
}

// ============================================================================
// UTILITY
// ============================================================================

internal fun getCategoryColor(categoryName: String): Color {
    val colors = listOf(
        Color(0xFFE57373),
        Color(0xFFBA68C8),
        Color(0xFF64B5F6),
        Color(0xFF4DB6AC),
        Color(0xFFAED581),
        Color(0xFFFFD54F),
        Color(0xFFFF8A65),
        Color(0xFF90A4AE)
    )
    val index = Math.floorMod(categoryName.hashCode(), colors.size)
    return colors[index]
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun CategorySpendingListGridPreview() {
    PaisaTrackerTheme {
        Surface {
            CategorySpendingList(
                categories = listOf(
                    CategorySpending(categoryId = 1, categoryName = "Food & Dining", categoryIcon = "🍔", total = 15000.0, count = 45, percentage = 35.0),
                    CategorySpending(categoryId = 2, categoryName = "Transportation", categoryIcon = "🚗", total = 8000.0, count = 20, percentage = 18.6),
                    CategorySpending(categoryId = 3, categoryName = "Shopping", categoryIcon = "🛍️", total = 12000.0, count = 15, percentage = 27.9),
                    CategorySpending(categoryId = 4, categoryName = "Entertainment", categoryIcon = "🎬", total = 5000.0, count = 10, percentage = 11.6)
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
                    CategorySpending(categoryId = 1, categoryName = "Entertainment", categoryIcon = "🎬", total = 5000.0, count = 10, percentage = 45.0)
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}