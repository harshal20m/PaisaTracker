package com.example.paisatracker.ui.main.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paisatracker.data.RecentExpense
import com.example.paisatracker.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@Composable
fun RecentTransactionsSlider(
    expenses      : List<RecentExpense>,
    onExpenseClick: (RecentExpense) -> Unit,
    onMoreClick   : () -> Unit,
    showMore      : Boolean = false,
    onLoadMore    : () -> Unit = {}
) {
    val display = expenses.take(if (showMore) expenses.size else 10)
    if (display.isEmpty()) return

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(400)) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec  = tween(400, easing = FastOutSlowInEasing)
        )
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Recent Transactions",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "${display.size} transaction${if (display.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick          = onMoreClick,
                    modifier         = Modifier.height(36.dp),
                    shape            = RoundedCornerShape(18.dp),
                    border           = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    colors           = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor   = MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding   = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Text(
                        if (showMore) "View Less" else "View All",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (showMore) Icons.Default.KeyboardArrowUp
                        else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }

            if (!showMore) {
                // ── Horizontal scroll row ─────────────────────────────────────
                val listState = rememberLazyListState()

                LazyRow(
                    state               = listState,
                    contentPadding      = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    items(display, key = { it.id }) { expense ->
                        AnimatedTransactionCard(
                            expense = expense,
                            onClick = { onExpenseClick(expense) }
                        )
                    }
                    item { AnimatedMoreCard(onClick = onMoreClick) }
                }

                // Scroll indicator dots
                if (display.size > 2) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(minOf(display.size, 5)) { index ->
                            val isActive = remember {
                                derivedStateOf { listState.firstVisibleItemIndex == index }
                            }
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .animateContentSize(
                                        spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness    = Spring.StiffnessLow
                                        )
                                    )
                                    .size(
                                        width  = if (isActive.value) 16.dp else 6.dp,
                                        height = 6.dp
                                    )
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isActive.value) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                        }
                    }
                }

            } else {
                // ── Grid view (expanded) ──────────────────────────────────────
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    display.chunked(2).forEach { row ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { expense ->
                                RecentTransactionGridItem(
                                    expense = expense,
                                    onClick = { onExpenseClick(expense) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }

                    OutlinedButton(
                        onClick        = onLoadMore,
                        modifier       = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(top = 4.dp),
                        shape          = RoundedCornerShape(12.dp),
                        border         = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        colors         = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor   = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Load More", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// ── Transaction card (horizontal scroll) ─────────────────────────────────────
@Composable
private fun AnimatedTransactionCard(
    expense : RecentExpense,
    onClick : () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue  = if (isPressed) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label        = "scale"
    )
    val elevation by animateDpAsState(
        targetValue  = if (isPressed) 0.dp else 2.dp,
        animationSpec = tween(150),
        label        = "elevation"
    )

    Card(
        onClick           = onClick,
        modifier          = Modifier
            .width(160.dp)
            .height(110.dp)
            .scale(scale),
        shape             = RoundedCornerShape(18.dp),
        // Solid surface — no alpha blending that bleeds on light themes
        colors            = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation         = CardDefaults.cardElevation(defaultElevation = elevation),
        interactionSource = interactionSource
    ) {
        Column(
            modifier            = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: emoji + date
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        // primaryContainer is fine here — small chip, doesn't dominate
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(expense.categoryEmoji.ifBlank { "💸" }, fontSize = 16.sp)
                }
                Text(
                    SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(expense.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Middle: description + category
            Column {
                Text(
                    expense.description,
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    expense.categoryName.ifBlank { "Other" },
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom: amount
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    formatCurrency(expense.amount),
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ── Grid item (expanded view) ─────────────────────────────────────────────────
@Composable
private fun RecentTransactionGridItem(
    expense  : RecentExpense,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier
) {
    Card(
        onClick   = onClick,
        modifier  = modifier.height(100.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier            = Modifier.padding(10.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(expense.categoryEmoji.ifBlank { "💸" }, fontSize = 14.sp)
                }
                Text(
                    SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(expense.date)),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            Column {
                Text(
                    expense.description,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    expense.categoryName.ifBlank { "Other" },
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = RoundedCornerShape(5.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    formatCurrency(expense.amount),
                    modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ── "View All" card at end of horizontal scroll ───────────────────────────────
@Composable
private fun AnimatedMoreCard(onClick: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue  = if (isPressed) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label        = "more_scale"
    )

    // Subtle pulse on the inner icon circle only
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.08f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        onClick           = onClick,
        modifier          = Modifier
            .width(120.dp)
            .height(110.dp)
            .scale(scale),
        shape             = RoundedCornerShape(18.dp),
        // Solid primaryContainer — intentional, contained, readable
        colors            = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation         = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 0.dp else 2.dp
        ),
        interactionSource = interactionSource
    ) {
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View all transactions",
                        tint               = MaterialTheme.colorScheme.onPrimary,
                        modifier           = Modifier.size(22.dp)
                    )
                }
                Text(
                    "View All",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}