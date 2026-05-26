package com.example.paisatracker.ui.details

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.paisatracker.PaisaTrackerViewModel
import com.example.paisatracker.data.CategoryExpense
import com.example.paisatracker.ui.common.BarChart
import com.example.paisatracker.ui.common.PieChartWithLegend
import com.example.paisatracker.ui.common.ScreenHeader
import com.example.paisatracker.ui.components.getCategoryColor
import com.example.paisatracker.util.formatCurrency
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.delay

enum class ChartType { PIE, BAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectInsightsScreen(
    viewModel: PaisaTrackerViewModel,
    projectId: Long,
    navController: NavController
) {
    val categoryExpenses: List<CategoryExpense> by viewModel.getCategoryExpenses(projectId)
        .collectAsState(initial = emptyList())

    var currentChartType by remember { mutableStateOf(ChartType.PIE) }
    val totalSpent = categoryExpenses.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = "Insights",
                subtitle = "Spending analytics",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (categoryExpenses.isEmpty()) {
                item { EmptyState() }
            } else {
                // ── Hero total card ──────────────────────────────────────────
                item { HeroTotalCard(totalSpent) }

                // ── Mini stat row ────────────────────────────────────────────
                item {
                    val avg = if (categoryExpenses.isNotEmpty()) totalSpent / categoryExpenses.size else 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MiniStatCard(
                            icon = Icons.AutoMirrored.Outlined.TrendingUp,
                            value = formatCurrency(avg),
                            label = "Avg per category",
                            modifier = Modifier.weight(1f)
                        )
                        MiniStatCard(
                            icon = Icons.Outlined.Category,
                            value = "${categoryExpenses.size}",
                            label = "Categories",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ── Top category strip ───────────────────────────────────────
                item {
                    val top = categoryExpenses.maxByOrNull { it.totalAmount }
                    top?.let { TopCategoryStrip(it.categoryName, it.totalAmount, totalSpent) }
                }

                // ── Chart with tabs ──────────────────────────────────────────
                item { ChartSection(currentChartType, categoryExpenses, onTypeChange = { currentChartType = it }) }

                // ── Section header ───────────────────────────────────────────
                item {
                    Text(
                        text = "Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                // ── Category rows ─────────────────────────────────────────────
                val sorted = categoryExpenses.sortedByDescending { it.totalAmount }
                sorted.forEach { cat ->
                    item {
                        val percent = if (totalSpent > 0) (cat.totalAmount / totalSpent) * 100 else 0.0
                        CategoryExpenseRow(
                            name = cat.categoryName,
                            amount = cat.totalAmount,
                            percent = percent
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero total card with spring scale-in animation
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroTotalCard(totalSpent: Double) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "hero_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "TOTAL SPENDING",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                letterSpacing = 1.sp
            )
            CountingNumber(totalSpent)
        }
    }
}

@Composable
private fun CountingNumber(target: Double) {
    var current by remember { mutableStateOf(0.0) }
    LaunchedEffect(target) {
        val steps = 48
        val increment = target / steps
        repeat(steps) {
            current += increment
            delay(1000L / steps)
        }
        current = target
    }
    Text(
        text = formatCurrency(current),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Mini stat card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MiniStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    var tapped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (tapped) 360f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = { tapped = false },
        label = "icon_rotation"
    )

    Card(
        modifier = modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { tapped = true },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotation),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.65f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top category strip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopCategoryStrip(name: String, amount: Double, total: Double) {
    val pct = if (total > 0) ((amount / total) * 100).toInt() else 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("👑", fontSize = 22.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TOP CATEGORY",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatCurrency(amount),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$pct%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chart section — toggle tabs built into the card header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChartSection(
    current: ChartType,
    expenses: List<CategoryExpense>,
    onTypeChange: (ChartType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Tabs inside the card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ChartType.values().forEach { type ->
                        val selected = current == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onTypeChange(type) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 9.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (type) {
                                        ChartType.PIE -> Icons.Outlined.PieChart
                                        ChartType.BAR -> Icons.Outlined.BarChart
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = when (type) {
                                        ChartType.PIE -> "Pie"
                                        ChartType.BAR -> "Bar"
                                    },
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Chart content
            when (current) {
                ChartType.PIE -> {
                    val entries = expenses.map { PieEntry(it.totalAmount.toFloat(), it.categoryName) }
                    PieChartWithLegend(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        entries = entries,
                        description = ""
                    )
                }
                ChartType.BAR -> {
                    val entries = expenses.mapIndexed { i, e -> BarEntry(i.toFloat(), e.totalAmount.toFloat()) }
                    val labels = expenses.map { it.categoryName }
                    BarChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        entries = entries,
                        labels = labels,
                        description = ""
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category expense row — replaces the old masonry grid cards
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategoryExpenseRow(
    name: String,
    amount: Double,
    percent: Double,
    modifier: Modifier = Modifier
) {
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); ready = true }

    val animatedPct by animateFloatAsState(
        targetValue = if (ready) percent.toFloat() else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "row_pct"
    )
    val scale by animateFloatAsState(
        targetValue = if (ready) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "row_scale"
    )

    val categoryColor = getCategoryColor(name)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: name + progress bar
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Text(
                    text = formatCurrency(amount),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp
                )
                LinearProgressIndicator(
                    progress = { animatedPct / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = categoryColor,
                    trackColor = categoryColor.copy(alpha = 0.15f)
                )
            }

            // Right: percentage bubble
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = categoryColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${animatedPct.toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = categoryColor
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("📊", fontSize = 36.sp)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "No data yet",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = "Add expenses to see insights",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}