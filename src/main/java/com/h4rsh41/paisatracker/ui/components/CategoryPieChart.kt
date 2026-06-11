package com.h4rsh41.paisatracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.h4rsh41.paisatracker.util.formatCurrency
import com.h4rsh41.paisatracker.domain.models.CategorySpending
import com.h4rsh41.paisatracker.ui.theme.PaisaTrackerTheme

/**
 * CategoryPieChart - A Material 3 pie chart component for visualizing category distribution
 * 
 * Features:
 * - Animated pie chart with smooth transitions
 * - Color-coded category segments
 * - Interactive legend with percentages
 * - Center label showing total
 * - Material 3 styling
 * - Responsive layout
 * 
 * @param categories List of category spending data
 * @param modifier Modifier for the component
 * @param showLegend Whether to show the legend
 * @param currencySymbol Currency symbol for labels
 */
@Composable
fun CategoryPieChart(
    categories: List<CategorySpending>,
    modifier: Modifier = Modifier,
    showLegend: Boolean = true,
    currencySymbol: String = "₹"
) {
    if (categories.isEmpty()) {
        EmptyPieChartPlaceholder(modifier = modifier)
        return
    }
    
    val totalAmount = categories.sumOf { it.total }
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(categories) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Chart title
            Text(
                text = "Category Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Pie chart
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Draw pie chart
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasSize = size.minDimension
                    val radius = canvasSize / 2
                    val strokeWidth = radius * 0.3f
                    
                    var startAngle = -90f
                    
                    categories.forEachIndexed { index, category ->
                        val sweepAngle = (category.percentage / 100f * 360f * animationProgress.value).toFloat()
                        val color = getCategoryColor(index)
                        
                        // Draw arc
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(
                                (size.width - canvasSize) / 2,
                                (size.height - canvasSize) / 2
                            ),
                            size = Size(canvasSize, canvasSize),
                            style = Stroke(width = strokeWidth)
                        )
                        
                        startAngle += sweepAngle
                    }
                }
                
                // Center label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(totalAmount, currencySymbol),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Legend
            if (showLegend) {
                Spacer(modifier = Modifier.height(16.dp))
                
                CategoryLegend(
                    categories = categories,
                    currencySymbol = currencySymbol
                )
            }
        }
    }
}

/**
 * Compact pie chart without legend
 */
@Composable
fun CompactCategoryPieChart(
    categories: List<CategorySpending>,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 120.dp
) {
    if (categories.isEmpty()) return
    
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(categories) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }
    
    Canvas(
        modifier = modifier.size(size)
    ) {
        val canvasSize = this.size.minDimension
        val radius = canvasSize / 2
        val strokeWidth = radius * 0.4f
        
        var startAngle = -90f
        
        categories.forEachIndexed { index, category ->
            val sweepAngle = (category.percentage / 100f * 360f * animationProgress.value).toFloat()
            val color = getCategoryColor(index)
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    (this.size.width - canvasSize) / 2,
                    (this.size.height - canvasSize) / 2
                ),
                size = Size(canvasSize, canvasSize),
                style = Stroke(width = strokeWidth)
            )
            
            startAngle += sweepAngle
        }
    }
}

/**
 * Legend showing category colors and percentages
 */
@Composable
private fun CategoryLegend(
    categories: List<CategorySpending>,
    currencySymbol: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.take(5).forEachIndexed { index, category ->
            LegendItem(
                category = category,
                color = getCategoryColor(index),
                currencySymbol = currencySymbol
            )
        }
        
        if (categories.size > 5) {
            Text(
                text = "+ ${categories.size - 5} more categories",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 32.dp)
            )
        }
    }
}

/**
 * Individual legend item
 */
@Composable
private fun LegendItem(
    category: CategorySpending,
    color: Color,
    currencySymbol: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, CircleShape)
            )
            
            // Category name with emoji
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.categoryIcon,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Amount and percentage
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatCurrency(category.total, currencySymbol),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${String.format("%.1f", category.percentage)}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Empty pie chart placeholder
 */
@Composable
private fun EmptyPieChartPlaceholder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📊",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "No categories to display",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Donut chart variant with center hole
 */
@Composable
fun CategoryDonutChart(
    categories: List<CategorySpending>,
    modifier: Modifier = Modifier,
    centerContent: @Composable () -> Unit = {},
    currencySymbol: String = "₹"
) {
    if (categories.isEmpty()) {
        EmptyPieChartPlaceholder(modifier = modifier)
        return
    }
    
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(categories) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
    }
    
    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2
            val strokeWidth = radius * 0.25f
            
            var startAngle = -90f
            
            categories.forEachIndexed { index, category ->
                val sweepAngle = (category.percentage / 100f * 360f * animationProgress.value).toFloat()
                val color = getCategoryColor(index)
                
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        (size.width - canvasSize) / 2,
                        (size.height - canvasSize) / 2
                    ),
                    size = Size(canvasSize, canvasSize),
                    style = Stroke(width = strokeWidth)
                )
                
                startAngle += sweepAngle
            }
        }
        
        centerContent()
    }
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

private fun getCategoryColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFFEC4899), // Pink
        Color(0xFF10B981), // Green
        Color(0xFFF59E0B), // Amber
        Color(0xFF8B5CF6), // Purple
        Color(0xFF06B6D4), // Cyan
        Color(0xFFEF4444), // Red
        Color(0xFF14B8A6), // Teal
        Color(0xFFF97316), // Orange
        Color(0xFF84CC16)  // Lime
    )
    return colors[index % colors.size]
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun CategoryPieChartPreview() {
    PaisaTrackerTheme {
        Surface {
            CategoryPieChart(
                categories = listOf(
                    CategorySpending(
                        categoryId = 1,
                        categoryName = "Food",
                        categoryIcon = "🍔",
                        total = 15000.0,
                        count = 45,
                        percentage = 35.0
                    ),
                    CategorySpending(
                        categoryId = 2,
                        categoryName = "Transport",
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
                    ),
                    CategorySpending(
                        categoryId = 4,
                        categoryName = "Entertainment",
                        categoryIcon = "🎬",
                        total = 8000.0,
                        count = 10,
                        percentage = 18.5
                    )
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompactCategoryPieChartPreview() {
    PaisaTrackerTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CompactCategoryPieChart(
                    categories = listOf(
                        CategorySpending(1, "Food", "🍔", 15000.0, 45, 50.0),
                        CategorySpending(2, "Transport", "🚗", 8000.0, 20, 30.0),
                        CategorySpending(3, "Shopping", "🛍️", 5000.0, 15, 20.0)
                    )
                )
                
                CategoryDonutChart(
                    categories = listOf(
                        CategorySpending(1, "Food", "🍔", 15000.0, 45, 50.0),
                        CategorySpending(2, "Transport", "🚗", 8000.0, 20, 30.0),
                        CategorySpending(3, "Shopping", "🛍️", 5000.0, 15, 20.0)
                    ),
                    centerContent = {
                        Text(
                            text = "₹28K",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryPieChartDarkPreview() {
    PaisaTrackerTheme {
        Surface {
            CategoryPieChart(
                categories = listOf(
                    CategorySpending(1, "Food", "🍔", 15000.0, 45, 60.0),
                    CategorySpending(2, "Transport", "🚗", 10000.0, 20, 40.0)
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// Made with Bob
