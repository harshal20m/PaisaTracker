package com.example.paisatracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.paisatracker.domain.models.FinancialState
import com.example.paisatracker.ui.theme.PaisaTrackerTheme
import java.text.NumberFormat
import java.util.*

/**
 * FinancialHealthIndicator - A Material 3 component for displaying overall financial health
 * 
 * Features:
 * - Visual health score (0-100)
 * - Color-coded status (Excellent, Good, Fair, Poor)
 * - Circular progress indicator
 * - Key metrics display
 * - Actionable insights
 * - Material 3 styling
 * 
 * @param financialState The financial state data
 * @param modifier Modifier for the component
 * @param currencySymbol Currency symbol for labels
 */
@Composable
fun FinancialHealthIndicator(
    financialState: FinancialState,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹"
) {
    val healthScore = calculateHealthScore(financialState)
    val healthStatus = getHealthStatus(healthScore)
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(healthScore) {
        animationProgress.animateTo(
            targetValue = healthScore / 100f,
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Financial Health",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Circular health indicator
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 20f
                    val radius = (size.minDimension - strokeWidth) / 2
                    
                    // Background arc
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.1f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(
                            (size.width - radius * 2) / 2,
                            (size.height - radius * 2) / 2
                        ),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    
                    // Progress arc
                    drawArc(
                        color = healthStatus.color,
                        startAngle = 135f,
                        sweepAngle = 270f * animationProgress.value,
                        useCenter = false,
                        topLeft = Offset(
                            (size.width - radius * 2) / 2,
                            (size.height - radius * 2) / 2
                        ),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                
                // Center content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = healthStatus.icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = healthStatus.color
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${(healthScore * animationProgress.value).toInt()}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = healthStatus.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = healthStatus.color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Key metrics
            HealthMetrics(
                financialState = financialState,
                currencySymbol = currencySymbol
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Insights
            HealthInsights(
                financialState = financialState,
                healthStatus = healthStatus
            )
        }
    }
}

/**
 * Compact health indicator for smaller spaces
 */
@Composable
fun CompactFinancialHealthIndicator(
    financialState: FinancialState,
    modifier: Modifier = Modifier
) {
    val healthScore = calculateHealthScore(financialState)
    val healthStatus = getHealthStatus(healthScore)
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = healthStatus.icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = healthStatus.color
        )
        
        Column {
            Text(
                text = "Health Score",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$healthScore",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = healthStatus.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = healthStatus.color,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Health metrics display
 */
@Composable
private fun HealthMetrics(
    financialState: FinancialState,
    currencySymbol: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricRow(
            label = "Income",
            value = formatCurrency(financialState.totalIncome, currencySymbol),
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF4CAF50)
        )
        
        MetricRow(
            label = "Expenses",
            value = formatCurrency(financialState.totalExpenses, currencySymbol),
            icon = Icons.Default.TrendingDown,
            color = MaterialTheme.colorScheme.error
        )
        
        MetricRow(
            label = "Savings",
            value = formatCurrency(financialState.totalSavings, currencySymbol),
            icon = Icons.Default.Savings,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Individual metric row
 */
@Composable
private fun MetricRow(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = color
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Health insights and recommendations
 */
@Composable
private fun HealthInsights(
    financialState: FinancialState,
    healthStatus: HealthStatus
) {
    val insights = generateInsights(financialState, healthStatus)
    
    if (insights.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = healthStatus.color.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                insights.forEach { insight ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = healthStatus.color
                        )
                        
                        Text(
                            text = insight,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// DATA CLASSES & ENUMS
// ============================================================================

data class HealthStatus(
    val label: String,
    val color: Color,
    val icon: ImageVector,
    val range: IntRange
)

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

private fun calculateHealthScore(state: FinancialState): Int {
    var score = 50 // Base score
    
    // Savings rate (0-30 points)
    val savingsRate = if (state.totalIncome > 0) {
        (state.totalSavings / state.totalIncome * 100).toInt()
    } else 0
    score += (savingsRate * 0.3).toInt().coerceIn(0, 30)
    
    // Budget adherence (0-25 points)
    val budgetAdherence = if (state.totalBudget > 0) {
        ((1 - (state.totalExpenses / state.totalBudget).coerceIn(0.0, 1.5)) * 100).toInt()
    } else 0
    score += (budgetAdherence * 0.25).toInt().coerceIn(0, 25)
    
    // Expense control (0-25 points)
    val expenseRatio = if (state.totalIncome > 0) {
        (state.totalExpenses / state.totalIncome)
    } else 1.0
    score += when {
        expenseRatio < 0.5 -> 25
        expenseRatio < 0.7 -> 20
        expenseRatio < 0.9 -> 15
        else -> 5
    }
    
    return score.coerceIn(0, 100)
}

private fun getHealthStatus(score: Int): HealthStatus {
    return when {
        score >= 80 -> HealthStatus(
            "Excellent",
            Color(0xFF4CAF50),
            Icons.Default.CheckCircle,
            80..100
        )
        score >= 60 -> HealthStatus(
            "Good",
            Color(0xFF2196F3),
            Icons.Default.ThumbUp,
            60..79
        )
        score >= 40 -> HealthStatus(
            "Fair",
            Color(0xFFFFC107),
            Icons.Default.Warning,
            40..59
        )
        else -> HealthStatus(
            "Poor",
            Color(0xFFF44336),
            Icons.Default.Error,
            0..39
        )
    }
}

private fun generateInsights(state: FinancialState, status: HealthStatus): List<String> {
    val insights = mutableListOf<String>()
    
    val savingsRate = if (state.totalIncome > 0) {
        (state.totalSavings / state.totalIncome * 100).toInt()
    } else 0
    
    when (status.label) {
        "Excellent" -> {
            insights.add("Great job! You're managing your finances well.")
            if (savingsRate > 30) {
                insights.add("Your savings rate of $savingsRate% is outstanding!")
            }
        }
        "Good" -> {
            insights.add("You're on the right track. Keep it up!")
            if (savingsRate < 20) {
                insights.add("Try to increase your savings rate to 20% or more.")
            }
        }
        "Fair" -> {
            insights.add("There's room for improvement in your financial health.")
            if (state.totalExpenses > state.totalBudget) {
                insights.add("You're over budget. Review your spending habits.")
            }
        }
        "Poor" -> {
            insights.add("Your financial health needs attention.")
            if (state.totalExpenses > state.totalIncome) {
                insights.add("You're spending more than you earn. Create a budget.")
            }
        }
    }
    
    return insights
}

private fun formatCurrency(value: Double, symbol: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 0
    return "$symbol ${formatter.format(value)}"
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun FinancialHealthIndicatorPreview() {
    PaisaTrackerTheme {
        Surface {
            FinancialHealthIndicator(
                financialState = FinancialState(
                    totalIncome = 50000.0,
                    totalExpenses = 30000.0,
                    totalSavings = 20000.0,
                    totalBudget = 35000.0,
                    budgetUtilization = 85.7,
                    savingsRate = 40.0
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompactFinancialHealthIndicatorPreview() {
    PaisaTrackerTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CompactFinancialHealthIndicator(
                    financialState = FinancialState(
                        totalIncome = 50000.0,
                        totalExpenses = 30000.0,
                        totalSavings = 20000.0,
                        totalBudget = 35000.0,
                        budgetUtilization = 85.7,
                        savingsRate = 40.0
                    )
                )
                
                CompactFinancialHealthIndicator(
                    financialState = FinancialState(
                        totalIncome = 50000.0,
                        totalExpenses = 45000.0,
                        totalSavings = 5000.0,
                        totalBudget = 40000.0,
                        budgetUtilization = 112.5,
                        savingsRate = 10.0
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FinancialHealthIndicatorDarkPreview() {
    PaisaTrackerTheme {
        Surface {
            FinancialHealthIndicator(
                financialState = FinancialState(
                    totalIncome = 50000.0,
                    totalExpenses = 48000.0,
                    totalSavings = 2000.0,
                    totalBudget = 40000.0,
                    budgetUtilization = 120.0,
                    savingsRate = 4.0
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// Made with Bob
