package com.example.paisatracker.ui.sms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.paisatracker.data.MerchantRuleEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantRulesGridContent(
    merchantRules: List<MerchantRuleEntity>,
    getCategoryName: (Long) -> String?,
    getProjectName: (Long?) -> String?,
    onToggleActive: (Long, Boolean) -> Unit,
    onEdit: (MerchantRuleEntity) -> Unit,
    onDelete: (MerchantRuleEntity) -> Unit,
    onBulkDelete: (List<Long>) -> Unit
) {
    // Selection state for bulk delete
    var selectionMode by remember { mutableStateOf(false) }
    var selectedRuleIds by remember { mutableStateOf(setOf<Long>()) }
    
    // Group rules by category
    val groupedRules = merchantRules.groupBy { it.categoryId }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Selection toolbar
        if (selectionMode) {
            SelectionToolbar(
                selectedCount = selectedRuleIds.size,
                onCancelSelection = {
                    selectionMode = false
                    selectedRuleIds = emptySet()
                },
                onSelectAll = {
                    selectedRuleIds = merchantRules.map { it.id }.toSet()
                },
                onDeleteSelected = {
                    onBulkDelete(selectedRuleIds.toList())
                    selectionMode = false
                    selectedRuleIds = emptySet()
                }
            )
        }
        
        // Rules list grouped by category
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedRules.forEach { (categoryId, rules) ->
                item {
                    // Category header
                    CategoryHeader(
                        categoryName = getCategoryName(categoryId) ?: "Unknown",
                        ruleCount = rules.size,
                        onLongPress = {
                            if (!selectionMode) {
                                selectionMode = true
                            }
                        }
                    )
                }
                
                item {
                    // 2-column grid for rules in this category
                    val gridHeight = ((rules.size + 1) / 2) * 180
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(gridHeight.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        userScrollEnabled = false
                    ) {
                        items(rules) { rule ->
                            CompactRuleCard(
                                rule = rule,
                                categoryName = getCategoryName(rule.categoryId) ?: "Unknown",
                                projectName = getProjectName(rule.projectId),
                                isSelected = selectedRuleIds.contains(rule.id),
                                selectionMode = selectionMode,
                                onToggleActive = { onToggleActive(rule.id, !rule.isActive) },
                                onEdit = { onEdit(rule) },
                                onDelete = { onDelete(rule) },
                                onLongPress = {
                                    if (!selectionMode) {
                                        selectionMode = true
                                        selectedRuleIds = setOf(rule.id)
                                    }
                                },
                                onSelect = {
                                    if (selectionMode) {
                                        selectedRuleIds = if (selectedRuleIds.contains(rule.id)) {
                                            selectedRuleIds - rule.id
                                        } else {
                                            selectedRuleIds + rule.id
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionToolbar(
    selectedCount: Int,
    onCancelSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCancelSelection) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$selectedCount selected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Row {
                TextButton(onClick = onSelectAll) {
                    Text("Select All")
                }
                Spacer(Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onDeleteSelected,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    categoryName: String,
    ruleCount: Int,
    onLongPress: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = "$ruleCount rules",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactRuleCard(
    rule: MerchantRuleEntity,
    categoryName: String,
    projectName: String?,
    isSelected: Boolean,
    selectionMode: Boolean,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLongPress: () -> Unit,
    onSelect: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clickable {
                if (selectionMode) {
                    onSelect()
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                rule.isActive -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Header with checkbox/switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onSelect() },
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Switch(
                            checked = rule.isActive,
                            onCheckedChange = { onToggleActive() },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    
                    // Menu button
                    if (!selectionMode) {
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        showMenu = false
                                        onEdit()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Merchant pattern
                Text(
                    text = rule.merchantPattern,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (rule.isActive) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    }
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Project (if exists)
                if (projectName != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = projectName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(Modifier.weight(1f))
                
                // Footer with priority and match count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            rule.priority < 25 -> MaterialTheme.colorScheme.errorContainer
                            rule.priority < 50 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = "P${rule.priority}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Match count
                    if (rule.matchCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${rule.matchCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// Made with Bob
