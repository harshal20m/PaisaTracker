package com.example.paisatracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.paisatracker.ui.theme.PaisaTrackerTheme
import java.text.NumberFormat
import java.util.*

/**
 * StatisticsCard - A Material 3 card component for displaying key financial metrics
 * 
 * Features:
 * - Displays a single statistic with icon, label, and value
 * - Optional trend indicator (up/down with percentage)
 * - Color-coded based on metric type
 * - Animated value changes
 * - Material 3 styling
 * 
 * @param title The label for the statistic (e.g., "Total Spent")
 * @param value The numeric value to display
 * @param icon Icon to display
 * @param trend Optional trend percentage (positive = increase, negative = decrease)
 * @param trendLabel Optional label for trend (e.g., "vs last month")
 * @param cardColor Color scheme for the card
 * @param currencySymbol Currency symbol to display (default: ₹)
 * @param modifier Modifier for the component
 */
@Composable
fun StatisticsCard(
    title: String,
    value: Double,
    icon: ImageVector,
    trend: Double? = null,
    trendLabel: String? = null,
    cardColor: StatisticsCardColor = StatisticsCardColor.Primary,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val colors = when (cardColor) {
        StatisticsCardColor.Primary -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        StatisticsCardColor.Secondary -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        StatisticsCardColor.Tertiary -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        StatisticsCardColor.Success -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        StatisticsCardColor.Warning -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with icon and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Value with animation
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "value_animation"
            ) { animatedValue ->
                Text(
                    text = formatCurrency(animatedValue, currencySymbol),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Trend indicator (if provided)
            if (trend != null) {
                TrendIndicator(
                    trend = trend,
                    label = trendLabel
                )
            }
        }
    }
}

/**
 * Compact version of StatisticsCard for grid layouts
 */
@Composable
fun CompactStatisticsCard(
    title: String,
    value: Double,
    icon: ImageVector,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = formatCurrency(value, currencySymbol),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Trend indicator component showing percentage change
 */
@Composable
private fun TrendIndicator(
    trend: Double,
    label: String?,
    modifier: Modifier = Modifier
) {
    val isPositive = trend >= 0
    val trendColor = if (isPositive) {
        MaterialTheme.colorScheme.error // Red for increase in spending
    } else {
        Color(0xFF4CAF50) // Green for decrease in spending
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = trendColor
        )
        
        Text(
            text = "${if (isPositive) "+" else ""}${String.format("%.1f", trend)}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = trendColor
        )
        
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Grid layout for multiple statistics cards
 */
@Composable
fun StatisticsGrid(
    statistics: List<StatisticItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        statistics.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    CompactStatisticsCard(
                        title = item.title,
                        value = item.value,
                        icon = item.icon,
                        currencySymbol = item.currencySymbol,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Fill remaining space if odd number of items
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================

/**
 * Color scheme options for StatisticsCard
 */
enum class StatisticsCardColor {
    Primary,
    Secondary,
    Tertiary,
    Success,
    Warning
}

/**
 * Data class for statistics items in grid
 */
data class StatisticItem(
    val title: String,
    val value: Double,
    val icon: ImageVector,
    val currencySymbol: String = "₹"
)

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

/**
 * Format currency value with proper formatting
 */
private fun formatCurrency(value: Double, symbol: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 2
    return "$symbol ${formatter.format(value)}"
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun StatisticsCardPreview() {
    PaisaTrackerTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatisticsCard(
                    title = "Total Spent",
                    value = 45678.50,
                    icon = Icons.Default.TrendingUp,
                    trend = 12.5,
                    trendLabel = "vs last month",
                    cardColor = StatisticsCardColor.Primary
                )
                
                StatisticsCard(
                    title = "Budget Remaining",
                    value = 15000.00,
                    icon = Icons.Default.AccountBalance,
                    trend = -8.3,
                    trendLabel = "saved",
                    cardColor = StatisticsCardColor.Success
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompactStatisticsCardPreview() {
    PaisaTrackerTheme {
        Surface {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactStatisticsCard(
                    title = "Income",
                    value = 50000.0,
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                
                CompactStatisticsCard(
                    title = "Expenses",
                    value = 35000.0,
                    icon = Icons.Default.TrendingDown,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatisticsGridPreview() {
    PaisaTrackerTheme {
        Surface {
            StatisticsGrid(
                statistics = listOf(
                    StatisticItem("Total Spent", 45678.50, Icons.Default.ShoppingCart),
                    StatisticItem("Budget", 60000.0, Icons.Default.AccountBalance),
                    StatisticItem("Savings", 14321.50, Icons.Default.Savings),
                    StatisticItem("Categories", 12.0, Icons.Default.Category)
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatisticsCardDarkPreview() {
    PaisaTrackerTheme {
        Surface {
            StatisticsCard(
                title = "Total Spent",
                value = 45678.50,
                icon = Icons.Default.TrendingUp,
                trend = 12.5,
                trendLabel = "vs last month",
                cardColor = StatisticsCardColor.Warning,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// Made with Bob
