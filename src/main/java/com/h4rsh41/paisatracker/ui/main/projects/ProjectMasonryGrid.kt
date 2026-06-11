package com.h4rsh41.paisatracker.ui.main.projects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.h4rsh41.paisatracker.data.Project
import com.h4rsh41.paisatracker.data.ProjectWithTotal
import com.h4rsh41.paisatracker.ui.theme.PaisaTrackerTheme
import com.h4rsh41.paisatracker.util.CurrentCurrency
import com.h4rsh41.paisatracker.util.formatCurrency

/**
 * Masonry Grid Layout for Projects
 * Displays projects in a compact 2-column staggered grid
 */
@Composable
fun ProjectMasonryGrid(
    projects: List<ProjectWithTotal>,
    onProjectClick: (ProjectWithTotal) -> Unit,
    onEditClick: (ProjectWithTotal) -> Unit,
    onDeleteClick: (ProjectWithTotal) -> Unit,
    onCompleteToggleClick: (ProjectWithTotal) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyStaggeredGridState()

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp
    ) {
        items(projects, key = { it.project.id }) { projectWithTotal ->
            CompactProjectCard(
                projectWithTotal = projectWithTotal,
                onProjectClick = { onProjectClick(projectWithTotal) },
                onEditClick = { onEditClick(projectWithTotal) },
                onDeleteClick = { onDeleteClick(projectWithTotal) },
                onCompleteToggleClick = { onCompleteToggleClick(projectWithTotal) }
            )
        }
    }
}

/**
 * Compact Project Card for Masonry Grid
 * Optimized for space efficiency while maintaining readability
 */
@Composable
private fun CompactProjectCard(
    projectWithTotal: ProjectWithTotal,
    onProjectClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCompleteToggleClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var isAmountVisible by remember { mutableStateOf(false) }
    val currency = CurrentCurrency.get()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, hoveredElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header: Emoji + Name + Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(projectWithTotal.project.emoji, fontSize = 22.sp)
                        }
                        Text(
                            text = projectWithTotal.project.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 15.sp,
                            lineHeight = 18.sp
                        )
                    }
                    IconButton(
                        onClick = { menuExpanded = !menuExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            "Menu",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Expanded Menu
                AnimatedVisibility(
                    visible = menuExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                        CompactMenuButton(
                            icon = Icons.Default.Edit,
                            label = "Edit",
                            onClick = { onEditClick(); menuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        CompactMenuButton(
                            icon = if (projectWithTotal.project.isCompleted) Icons.Default.History else Icons.Default.CheckCircle,
                            label = if (projectWithTotal.project.isCompleted) "Reopen" else "Complete",
                            onClick = { onCompleteToggleClick(); menuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                        CompactMenuButton(
                            icon = Icons.Default.Delete,
                            label = "Delete",
                            onClick = { onDeleteClick(); menuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Stats (when menu is closed)
                AnimatedVisibility(
                    visible = !menuExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            thickness = 0.5.dp
                        )

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MiniStatChip(
                                emoji = "🏷️",
                                value = "${projectWithTotal.categoryCount}",
                                modifier = Modifier.weight(1f)
                            )
                            MiniStatChip(
                                emoji = "🧾",
                                value = "${projectWithTotal.expenseCount}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Amount Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        )
                                    )
                                )
                                .clickable { isAmountVisible = !isAmountVisible }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isAmountVisible) formatCurrency(projectWithTotal.totalAmount) else "${currency.symbol} ••••",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (isAmountVisible) "Tap to hide" else "Tap to show",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    fontSize = 9.sp
                                )
                            }
                        }

                        // Date Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MiniDateChip(com.h4rsh41.paisatracker.ui.main.projects.formatDateCompact(projectWithTotal.project.lastModified))
                            
                            // Explore Button
                            Button(
                                onClick = onProjectClick,
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    "Explore",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatChip(emoji: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 12.sp)
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun MiniDateChip(date: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun CompactMenuButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                fontSize = 12.sp
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun PreviewCompactProjectCard() {
    PaisaTrackerTheme {
        val sampleProject = ProjectWithTotal(
            project = Project(
                id = 1,
                name = "Home Renovation",
                emoji = "🏠",
                createdAt = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis(),
                isCompleted = false,
                includeInSalary = true
            ),
            totalAmount = 45000.0,
            categoryCount = 5,
            expenseCount = 23
        )
        
        Box(modifier = Modifier.padding(16.dp)) {
            CompactProjectCard(
                projectWithTotal = sampleProject,
                onProjectClick = {},
                onEditClick = {},
                onDeleteClick = {},
                onCompleteToggleClick = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun PreviewProjectMasonryGrid() {
    PaisaTrackerTheme {
        val sampleProjects = listOf(
            ProjectWithTotal(
                project = Project(1, "Home Renovation", "🏠", System.currentTimeMillis(), System.currentTimeMillis(), false, true),
                totalAmount = 45000.0,
                categoryCount = 5,
                expenseCount = 23
            ),
            ProjectWithTotal(
                project = Project(2, "Vacation", "✈️", System.currentTimeMillis(), System.currentTimeMillis(), false, false),
                totalAmount = 25000.0,
                categoryCount = 3,
                expenseCount = 15
            ),
            ProjectWithTotal(
                project = Project(3, "Car Maintenance", "🚗", System.currentTimeMillis(), System.currentTimeMillis(), false, true),
                totalAmount = 12000.0,
                categoryCount = 4,
                expenseCount = 8
            ),
            ProjectWithTotal(
                project = Project(4, "Education", "📚", System.currentTimeMillis(), System.currentTimeMillis(), false, true),
                totalAmount = 35000.0,
                categoryCount = 6,
                expenseCount = 18
            )
        )
        
        ProjectMasonryGrid(
            projects = sampleProjects,
            onProjectClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onCompleteToggleClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewCompactProjectCardDark() {
    PaisaTrackerTheme {
        val sampleProject = ProjectWithTotal(
            project = Project(
                id = 1,
                name = "Home Renovation",
                emoji = "🏠",
                createdAt = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis(),
                isCompleted = false,
                includeInSalary = true
            ),
            totalAmount = 45000.0,
            categoryCount = 5,
            expenseCount = 23
        )
        
        Box(modifier = Modifier.padding(16.dp)) {
            CompactProjectCard(
                projectWithTotal = sampleProject,
                onProjectClick = {},
                onEditClick = {},
                onDeleteClick = {},
                onCompleteToggleClick = {}
            )
        }
    }
}

// Made with Bob
