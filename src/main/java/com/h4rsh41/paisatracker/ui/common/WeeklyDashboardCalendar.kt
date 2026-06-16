package com.h4rsh41.paisatracker.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.h4rsh41.paisatracker.data.RecentExpense
import com.h4rsh41.paisatracker.util.formatCurrency
import java.text.SimpleDateFormat
import androidx.compose.ui.text.style.TextAlign
import java.util.*
import kotlin.math.max

@Composable
fun WeeklyDashboardCalendar(
    expenses: List<RecentExpense>,
    onTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { Calendar.getInstance() }
    var weekOffset by remember { mutableStateOf(0) } // Track which week we're viewing
    var selectedDate by remember { mutableStateOf(today) }

    val startOfWeek = remember(weekOffset) {
        Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.SUNDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.WEEK_OF_YEAR, weekOffset)
        }
    }

    val weekDates = remember(startOfWeek) {
        (0..6).map { i -> (startOfWeek.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, i) } }
    }

    val endOfWeek = remember(startOfWeek) {
        (startOfWeek.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 7) }
    }
    
    val isCurrentWeek = weekOffset == 0

    val weeklyExpenses = remember(expenses, startOfWeek, endOfWeek) {
        expenses.filter { it.date >= startOfWeek.timeInMillis && it.date < endOfWeek.timeInMillis }
    }

    val weeklyTotal = remember(weeklyExpenses) { weeklyExpenses.sumOf { it.amount } }

    val expensesByDate = remember(weeklyExpenses) {
        weeklyExpenses.groupBy {
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
        }
    }
    
    // Smart date selection based on week context
    LaunchedEffect(weekOffset) {
        selectedDate = when {
            // Current week: select today
            weekOffset == 0 -> today.clone() as Calendar
            // Past weeks: select Sunday (first day of week)
            else -> weekDates.first()
        }
    }

    val selectedDateKey = "${selectedDate.get(Calendar.YEAR)}-${selectedDate.get(Calendar.MONTH)}-${selectedDate.get(Calendar.DAY_OF_MONTH)}"
    val selectedExpenses = expensesByDate[selectedDateKey] ?: emptyList()
    
    // Calculate daily totals for the graph
    val dailyTotals = remember(weekDates, expensesByDate) {
        weekDates.map { date ->
            val dateKey = "${date.get(Calendar.YEAR)}-${date.get(Calendar.MONTH)}-${date.get(Calendar.DAY_OF_MONTH)}"
            expensesByDate[dateKey]?.sumOf { it.amount } ?: 0.0
        }
    }

    // Calendar Card with Graph inside
    Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // --- HEADER WITH NAVIGATION (Single Line) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Arrow
                    IconButton(
                        onClick = { weekOffset -= 1 },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Previous Week",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Title and Date Range in one line
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (isCurrentWeek) "This Week" else "Week",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "• ${formatDateToDayMonth(startOfWeek)} - ${formatDateToDayMonth(weekDates.last())}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    formatCurrency(weeklyTotal),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Right Arrow (disabled if current week)
                    IconButton(
                        onClick = { weekOffset += 1 },
                        enabled = !isCurrentWeek,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Next Week",
                            tint = if (isCurrentWeek)
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

            Spacer(modifier = Modifier.height(8.dp))

            // --- WEEK DAYS ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
                weekDates.forEachIndexed { index, date ->
                    val isSelected = isSameDay(date, selectedDate)
                    val isToday = isSameDay(date, today)
                    val dateKey = "${date.get(Calendar.YEAR)}-${date.get(Calendar.MONTH)}-${date.get(Calendar.DAY_OF_MONTH)}"

                    val bgColor by animateColorAsState(when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else -> Color.Transparent
                    }, label = "bg")

                    val contentColor by animateColorAsState(when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }, label = "txt")

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable { selectedDate = date }
                            .padding(vertical = 4.dp)
                            .weight(1f)
                    ) {
                        Text(dayLabels[index], style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = if (isSelected) contentColor.copy(0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(date.get(Calendar.DAY_OF_MONTH).toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = contentColor, fontSize = 13.sp)
                        Box(modifier = Modifier.size(2.dp).clip(CircleShape).background(if (expensesByDate.containsKey(dateKey)) contentColor else Color.Transparent))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

            // --- TRANSACTIONS GRID (2 in a row) ---
            AnimatedContent(
                targetState = selectedExpenses,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "grid"
            ) { expensesForDay ->
                if (expensesForDay.isNotEmpty()) {
                    Box(modifier = Modifier.heightIn(max = 150.dp).padding(top = 6.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(expensesForDay.chunked(2)) { rowItems ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    rowItems.forEach { expense ->
                                        GridTransactionItem(expense = expense, onClick = { onTransactionClick(expense.id) }, modifier = Modifier.weight(1f))
                                    }
                                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    Text("No transactions", modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            // Weekly Spending Graph at the bottom
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            WeeklySpendingGraphCard(
                dailyTotals = dailyTotals,
                weekDates = weekDates,
                modifier = Modifier.fillMaxWidth()
            )

        }
    }
}

@Composable
private fun GridTransactionItem(
    expense: RecentExpense,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(expense.categoryEmoji, fontSize = 12.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Show credits in green with + prefix, debits in red
                val isCredit = expense.amount < 0
                val displayAmount = kotlin.math.abs(expense.amount)
                val prefix = if (isCredit) "+" else ""
                
                Text(
                    text = "$prefix${formatCurrency(displayAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCredit) {
                        Color(0xFF4CAF50)  // Green for credits
                    } else {
                        MaterialTheme.colorScheme.error  // Red for debits
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WeeklySpendingGraphCard(
    dailyTotals: List<Double>,
    weekDates: List<Calendar>,
    modifier: Modifier = Modifier
) {
    val maxValue = dailyTotals.maxOrNull() ?: 0.0
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val today = remember { Calendar.getInstance() }

    val graphHeight = 120.dp

    // Round the max value to a "nice" number so gridlines read cleanly
    val axisMax = remember(maxValue) {
        if (maxValue <= 0) {
            100.0
        } else {
            val magnitude = Math.pow(10.0, Math.floor(Math.log10(maxValue)))
            val normalized = maxValue / magnitude
            val nice = when {
                normalized <= 1.0 -> 1.0
                normalized <= 2.0 -> 2.0
                normalized <= 5.0 -> 5.0
                else -> 10.0
            }
            nice * magnitude
        }
    }

    // Graph content without card wrapper (now inside main card)
    Row(modifier = modifier.fillMaxWidth()) {
                // Y-Axis Labels
                Column(
                    modifier = Modifier
                        .width(32.dp)
                        .height(graphHeight),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(axisMax, axisMax / 2, 0.0).forEach { value ->
                        Text(
                            text = formatAxisValue(value),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Graph + X-Axis labels
                Column(modifier = Modifier.weight(1f)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(graphHeight)
                    ) {
                        val w = size.width
                        val h = size.height

                        // Gridlines for max, mid, zero — match the y-axis labels
                        listOf(0f, h / 2f, h).forEach { y ->
                            drawLine(
                                color = gridColor.copy(alpha = 0.35f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val slotWidth = w / dailyTotals.size
                        val points = dailyTotals.mapIndexed { index, value ->
                            // Center each point under its day's slot, not on the edges
                            val x = (index + 0.5f) * slotWidth
                            val ratio = (value / axisMax).coerceIn(0.0, 1.0)
                            val y = h - (ratio * h).toFloat()
                            Offset(x, y)
                        }

                        // Shaded area under the line for a "glass" look
                        val fillPath = Path().apply {
                            moveTo(points.first().x, h)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, h)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.28f), Color.Transparent)
                            )
                        )

                        // Line connecting daily totals
                        val linePath = Path().apply {
                            points.forEachIndexed { index, point ->
                                if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = primaryColor,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Points - highlight today's dot in a different accent
                        points.forEachIndexed { index, point ->
                            if (dailyTotals[index] > 0) {
                                val isToday = isSameDay(weekDates[index], today)
                                drawCircle(
                                    color = if (isToday) tertiaryColor else primaryColor,
                                    radius = 4.dp.toPx(),
                                    center = point
                                )
                                drawCircle(color = surfaceColor, radius = 2.dp.toPx(), center = point)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // X-Axis Day Labels
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
                        weekDates.forEachIndexed { index, date ->
                            val isToday = isSameDay(date, today)
                            Text(
                                text = dayLabels[index],
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) tertiaryColor else onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
}

private fun formatAxisValue(value: Double): String {
    if (value <= 0) return "0"
    return if (value >= 1000) {
        val k = value / 1000
        if (k == k.toInt().toDouble()) "${k.toInt()}k" else String.format("%.1fk", k)
    } else {
        value.toInt().toString()
    }
}


private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean =
    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)

private fun formatDateToDayMonth(calendar: Calendar): String =
    SimpleDateFormat("MMM d", Locale.getDefault()).format(calendar.time)