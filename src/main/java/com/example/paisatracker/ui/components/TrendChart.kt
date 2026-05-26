package com.example.paisatracker.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.paisatracker.domain.models.MonthlyTotal
import com.example.paisatracker.domain.models.YearlyTotal
import com.example.paisatracker.ui.theme.PaisaTrackerTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import com.example.paisatracker.util.formatCurrency

/**
 * TrendChart - A Material 3 line chart component for visualizing spending trends
 * 
 * Features:
 * - Smooth animated line chart
 * - Gradient fill under the line
 * - Interactive data points
 * - Grid lines for better readability
 * - Responsive to different data ranges
 * - Material 3 styling
 * 
 * @param dataPoints List of data points to display
 * @param modifier Modifier for the component
 * @param lineColor Color of the trend line
 * @param showGrid Whether to show grid lines
 * @param currencySymbol Currency symbol for labels
 */
@Composable
fun TrendChart(
    dataPoints: List<TrendDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    showGrid: Boolean = true,
    currencySymbol: String = "₹"
) {
    if (dataPoints.isEmpty()) {
        EmptyChartPlaceholder(modifier = modifier)
        return
    }
    
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(dataPoints) {
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
                .padding(16.dp)
        ) {
            // Chart title and legend
            ChartHeader(
                title = "Spending Trend",
                maxValue = dataPoints.maxOf { it.value },
                currencySymbol = currencySymbol
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    val padding = 40f
                    
                    val maxValue = dataPoints.maxOf { it.value }
                    val minValue = 0.0
                    val valueRange = maxValue - minValue
                    
                    // Draw grid lines
                    if (showGrid) {
                        val gridColor = Color.Gray.copy(alpha = 0.2f)
                        val gridCount = 4
                        
                        for (i in 0..gridCount) {
                            val y = padding + (height - 2 * padding) * i / gridCount
                            drawLine(
                                color = gridColor,
                                start = Offset(padding, y),
                                end = Offset(width - padding, y),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                            )
                        }
                    }
                    
                    // Calculate points
                    val points = dataPoints.mapIndexed { index, point ->
                        val x = padding + (width - 2 * padding) * index / (dataPoints.size - 1).coerceAtLeast(1)
                        val normalizedValue = if (valueRange > 0) {
                            ((point.value - minValue) / valueRange).toFloat()
                        } else {
                            0.5f
                        }
                        val y = height - padding - (height - 2 * padding) * normalizedValue
                        Offset(x, y)
                    }
                    
                    // Draw gradient fill
                    val gradientPath = Path().apply {
                        if (points.isNotEmpty()) {
                            moveTo(points.first().x, height - padding)
                            points.forEach { point ->
                                lineTo(point.x * animationProgress.value, point.y)
                            }
                            lineTo(points.last().x * animationProgress.value, height - padding)
                            close()
                        }
                    }
                    
                    drawPath(
                        path = gradientPath,
                        color = lineColor.copy(alpha = 0.2f)
                    )
                    
                    // Draw line
                    if (points.size > 1) {
                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val animatedX = points[i].x * animationProgress.value
                                lineTo(animatedX, points[i].y)
                            }
                        }
                        
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(
                                width = 4f,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                    
                    // Draw data points
                    points.forEachIndexed { index, point ->
                        val animatedX = point.x * animationProgress.value
                        if (animatedX > 0) {
                            drawCircle(
                                color = lineColor,
                                radius = 6f,
                                center = Offset(animatedX, point.y)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3f,
                                center = Offset(animatedX, point.y)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // X-axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dataPoints.take(6).forEach { point ->
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Chart header with title and max value
 */
@Composable
private fun ChartHeader(
    title: String,
    maxValue: Double,
    currencySymbol: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Peak",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatCurrency(maxValue, currencySymbol),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Empty chart placeholder
 */
@Composable
private fun EmptyChartPlaceholder(modifier: Modifier = Modifier) {
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
                .height(250.dp),
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
                    text = "No data available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Compact trend indicator (mini chart)
 */
@Composable
fun CompactTrendIndicator(
    dataPoints: List<TrendDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (dataPoints.isEmpty()) return
    
    Canvas(
        modifier = modifier
            .width(60.dp)
            .height(30.dp)
    ) {
        val width = size.width
        val height = size.height
        
        val maxValue = dataPoints.maxOf { it.value }
        val minValue = dataPoints.minOf { it.value }
        val valueRange = maxValue - minValue
        
        val points = dataPoints.mapIndexed { index, point ->
            val x = width * index / (dataPoints.size - 1).coerceAtLeast(1)
            val normalizedValue = if (valueRange > 0) {
                ((point.value - minValue) / valueRange).toFloat()
            } else {
                0.5f
            }
            val y = height - height * normalizedValue
            Offset(x, y)
        }
        
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
            }
            
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
        }
    }
}

// ============================================================================
// EXTENSION FUNCTIONS
// ============================================================================

/**
 * Convert MonthlyTotal to TrendDataPoint
 */
fun MonthlyTotal.toTrendDataPoint(): TrendDataPoint {
    val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, getYear())
        set(Calendar.MONTH, getMonthNumber() - 1)
    }
    
    return TrendDataPoint(
        label = dateFormat.format(calendar.time),
        value = total,
        metadata = mapOf(
            "year" to getYear().toString(),
            "month" to getMonthNumber().toString(),
            "count" to count.toString()
        )
    )
}

/**
 * Convert YearlyTotal to TrendDataPoint
 */
fun YearlyTotal.toTrendDataPoint(): TrendDataPoint {
    return TrendDataPoint(
        label = year.toString(),
        value = total,
        metadata = mapOf(
            "year" to year.toString(),
            "count" to count.toString()
        )
    )
}

// ============================================================================
// DATA CLASSES
// ============================================================================

/**
 * Generic data point for trend charts
 */
data class TrendDataPoint(
    val label: String,
    val value: Double,
    val metadata: Map<String, String> = emptyMap()
)

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun TrendChartPreview() {
    PaisaTrackerTheme {
        Surface {
            TrendChart(
                dataPoints = listOf(
                    TrendDataPoint("Jan", 25000.0),
                    TrendDataPoint("Feb", 32000.0),
                    TrendDataPoint("Mar", 28000.0),
                    TrendDataPoint("Apr", 35000.0),
                    TrendDataPoint("May", 30000.0),
                    TrendDataPoint("Jun", 38000.0)
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyTrendChartPreview() {
    PaisaTrackerTheme {
        Surface {
            TrendChart(
                dataPoints = emptyList(),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompactTrendIndicatorPreview() {
    PaisaTrackerTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CompactTrendIndicator(
                    dataPoints = listOf(
                        TrendDataPoint("1", 100.0),
                        TrendDataPoint("2", 150.0),
                        TrendDataPoint("3", 120.0),
                        TrendDataPoint("4", 180.0),
                        TrendDataPoint("5", 160.0)
                    ),
                    lineColor = Color(0xFF4CAF50)
                )
                
                CompactTrendIndicator(
                    dataPoints = listOf(
                        TrendDataPoint("1", 180.0),
                        TrendDataPoint("2", 160.0),
                        TrendDataPoint("3", 140.0),
                        TrendDataPoint("4", 120.0),
                        TrendDataPoint("5", 100.0)
                    ),
                    lineColor = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TrendChartDarkPreview() {
    PaisaTrackerTheme {
        Surface {
            TrendChart(
                dataPoints = listOf(
                    TrendDataPoint("Jan", 25000.0),
                    TrendDataPoint("Feb", 32000.0),
                    TrendDataPoint("Mar", 28000.0),
                    TrendDataPoint("Apr", 35000.0),
                    TrendDataPoint("May", 30000.0),
                    TrendDataPoint("Jun", 38000.0)
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// Made with Bob
