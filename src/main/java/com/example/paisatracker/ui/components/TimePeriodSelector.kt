package com.example.paisatracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.paisatracker.domain.models.TimePeriod
import com.example.paisatracker.ui.theme.PaisaTrackerTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * TimePeriodSelector - A Material 3 component for selecting time periods
 * 
 * Features:
 * - Dropdown menu with all time period options
 * - Custom date range picker integration
 * - Material 3 styling with proper theming
 * - Smooth animations
 * - Accessibility support
 * 
 * @param selectedPeriod Currently selected time period
 * @param onPeriodSelected Callback when a period is selected
 * @param onCustomRangeClick Callback when custom range is clicked (opens date picker)
 * @param customRangeText Optional text to display for custom range (e.g., "Jan 1 - Jan 31")
 * @param modifier Modifier for the component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    onCustomRangeClick: () -> Unit,
    customRangeText: String? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Display text for the selected period
    val displayText = when (selectedPeriod) {
        TimePeriod.THIS_WEEK -> "This Week"
        TimePeriod.THIS_MONTH -> "This Month"
        TimePeriod.THIS_YEAR -> "This Year"
        TimePeriod.CUSTOM -> customRangeText ?: "Custom Range"
        TimePeriod.ALL_TIME -> "All Time"
    }
    
    Column(modifier = modifier) {
        // Main selector button
        OutlinedCard(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (selectedPeriod == TimePeriod.CUSTOM) {
                            Icons.Default.DateRange
                        } else {
                            Icons.Default.CalendarMonth
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Column {
                        Text(
                            text = "Time Period",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            TimePeriod.entries.forEach { period ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = when (period) {
                                TimePeriod.THIS_WEEK -> "This Week"
                                TimePeriod.THIS_MONTH -> "This Month"
                                TimePeriod.THIS_YEAR -> "This Year"
                                TimePeriod.CUSTOM -> "Custom Range"
                                TimePeriod.ALL_TIME -> "All Time"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (period == selectedPeriod) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    },
                    onClick = {
                        if (period == TimePeriod.CUSTOM) {
                            onCustomRangeClick()
                        } else {
                            onPeriodSelected(period)
                        }
                        expanded = false
                    },
                    leadingIcon = {
                        if (period == selectedPeriod) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = if (period == selectedPeriod) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                )
            }
        }
    }
}

/**
 * Compact version of TimePeriodSelector for use in smaller spaces
 */
@Composable
fun CompactTimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        FilterChip(
            selected = true,
            onClick = { expanded = !expanded },
            label = {
                Text(
                    text = when (selectedPeriod) {
                        TimePeriod.THIS_WEEK -> "Week"
                        TimePeriod.THIS_MONTH -> "Month"
                        TimePeriod.THIS_YEAR -> "Year"
                        TimePeriod.CUSTOM -> "Custom"
                        TimePeriod.ALL_TIME -> "All"
                    }
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TimePeriod.entries.forEach { period ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = when (period) {
                                TimePeriod.THIS_WEEK -> "This Week"
                                TimePeriod.THIS_MONTH -> "This Month"
                                TimePeriod.THIS_YEAR -> "This Year"
                                TimePeriod.CUSTOM -> "Custom Range"
                                TimePeriod.ALL_TIME -> "All Time"
                            }
                        )
                    },
                    onClick = {
                        onPeriodSelected(period)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun TimePeriodSelectorPreview() {
    PaisaTrackerTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimePeriodSelector(
                    selectedPeriod = TimePeriod.THIS_MONTH,
                    onPeriodSelected = {},
                    onCustomRangeClick = {}
                )
                
                TimePeriodSelector(
                    selectedPeriod = TimePeriod.CUSTOM,
                    onPeriodSelected = {},
                    onCustomRangeClick = {},
                    customRangeText = "Jan 1 - Jan 31, 2024"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompactTimePeriodSelectorPreview() {
    PaisaTrackerTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactTimePeriodSelector(
                    selectedPeriod = TimePeriod.THIS_MONTH,
                    onPeriodSelected = {}
                )
                
                CompactTimePeriodSelector(
                    selectedPeriod = TimePeriod.THIS_YEAR,
                    onPeriodSelected = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TimePeriodSelectorDarkPreview() {
    PaisaTrackerTheme {
        Surface {
            TimePeriodSelector(
                selectedPeriod = TimePeriod.THIS_WEEK,
                onPeriodSelected = {},
                onCustomRangeClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// Made with Bob
