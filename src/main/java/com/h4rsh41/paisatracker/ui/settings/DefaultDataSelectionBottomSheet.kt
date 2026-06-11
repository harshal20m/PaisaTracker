package com.h4rsh41.paisatracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.h4rsh41.paisatracker.PaisaTrackerViewModel
import com.h4rsh41.paisatracker.data.Category
import com.h4rsh41.paisatracker.data.Project
import com.h4rsh41.paisatracker.ui.common.ToastType
import kotlinx.coroutines.launch

data class DefaultProjectTemplate(
    val name: String,
    val emoji: String,
    val categories: List<DefaultCategoryTemplate>
)

data class DefaultCategoryTemplate(
    val name: String,
    val emoji: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultDataSelectionBottomSheet(
    viewModel: PaisaTrackerViewModel,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    // Map of project name to set of selected category names
    var selectedCategories by remember { mutableStateOf<Map<String, Set<String>>>(emptyMap()) }
    var expandedProjects by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    val defaultTemplates = remember {
        listOf(
            DefaultProjectTemplate(
                name = "Daily Living",
                emoji = "🏠",
                categories = listOf(
                    DefaultCategoryTemplate("Groceries", "🛒"),
                    DefaultCategoryTemplate("Household Items", "🧹"),
                    DefaultCategoryTemplate("Personal Care", "🧴")
                )
            ),
            DefaultProjectTemplate(
                name = "Food & Dining",
                emoji = "🍔",
                categories = listOf(
                    DefaultCategoryTemplate("Restaurants", "🍽️"),
                    DefaultCategoryTemplate("Coffee & Snacks", "☕"),
                    DefaultCategoryTemplate("Takeout", "🥡")
                )
            ),
            DefaultProjectTemplate(
                name = "Transportation",
                emoji = "🚗",
                categories = listOf(
                    DefaultCategoryTemplate("Fuel", "⛽"),
                    DefaultCategoryTemplate("Public Transit", "🚌"),
                    DefaultCategoryTemplate("Ride Share", "🚕"),
                    DefaultCategoryTemplate("Parking", "🅿️")
                )
            ),
            DefaultProjectTemplate(
                name = "Shopping",
                emoji = "🛍️",
                categories = listOf(
                    DefaultCategoryTemplate("Clothing", "👕"),
                    DefaultCategoryTemplate("Electronics", "📱"),
                    DefaultCategoryTemplate("Gifts", "🎁")
                )
            ),
            DefaultProjectTemplate(
                name = "Entertainment",
                emoji = "🎬",
                categories = listOf(
                    DefaultCategoryTemplate("Movies", "🎬"),
                    DefaultCategoryTemplate("Streaming Services", "📺"),
                    DefaultCategoryTemplate("Games", "🎮"),
                    DefaultCategoryTemplate("Events & Concerts", "🎵")
                )
            ),
            DefaultProjectTemplate(
                name = "Bills & Utilities",
                emoji = "💡",
                categories = listOf(
                    DefaultCategoryTemplate("Electricity", "⚡"),
                    DefaultCategoryTemplate("Water", "💧"),
                    DefaultCategoryTemplate("Internet", "🌐"),
                    DefaultCategoryTemplate("Mobile", "📱"),
                    DefaultCategoryTemplate("Rent", "🏠")
                )
            ),
            DefaultProjectTemplate(
                name = "Health & Wellness",
                emoji = "💊",
                categories = listOf(
                    DefaultCategoryTemplate("Pharmacy", "💊"),
                    DefaultCategoryTemplate("Doctor Visits", "👨‍⚕️"),
                    DefaultCategoryTemplate("Gym", "🏋️"),
                    DefaultCategoryTemplate("Insurance", "🛡️")
                )
            ),
            DefaultProjectTemplate(
                name = "Education",
                emoji = "📚",
                categories = listOf(
                    DefaultCategoryTemplate("Books", "📖"),
                    DefaultCategoryTemplate("Courses", "🎓"),
                    DefaultCategoryTemplate("Supplies", "✏️")
                )
            )
        )
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "📊 Build Your Structure",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Select projects to add with their categories",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            // Select All / Deselect All
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedCategories = defaultTemplates.associate { template ->
                            template.name to template.categories.map { it.name }.toSet()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Select All")
                }
                OutlinedButton(
                    onClick = { selectedCategories = emptyMap() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All")
                }
            }
            
            // Selection Counter
            val totalSelected = selectedCategories.values.sumOf { it.size }
            if (totalSelected > 0) {
                Text(
                    text = "$totalSelected categories selected across ${selectedCategories.size} projects",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Project List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(defaultTemplates) { template ->
                    ProjectTemplateCard(
                        template = template,
                        selectedCategories = selectedCategories[template.name] ?: emptySet(),
                        isExpanded = expandedProjects.contains(template.name),
                        onToggleExpand = {
                            expandedProjects = if (expandedProjects.contains(template.name)) {
                                expandedProjects - template.name
                            } else {
                                expandedProjects + template.name
                            }
                        },
                        onToggleProject = { selectAll ->
                            selectedCategories = if (selectAll) {
                                selectedCategories + (template.name to template.categories.map { it.name }.toSet())
                            } else {
                                selectedCategories - template.name
                            }
                        },
                        onToggleCategory = { categoryName ->
                            val currentCategories = selectedCategories[template.name] ?: emptySet()
                            selectedCategories = if (currentCategories.contains(categoryName)) {
                                val newSet = currentCategories - categoryName
                                if (newSet.isEmpty()) {
                                    selectedCategories - template.name
                                } else {
                                    selectedCategories + (template.name to newSet)
                                }
                            } else {
                                selectedCategories + (template.name to (currentCategories + categoryName))
                            }
                        }
                    )
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                // Check for existing projects
                                val existingProjects = viewModel.getAllProjectsList()
                                val existingNames = existingProjects.map { it.name.lowercase() }.toSet()
                                
                                var addedCount = 0
                                var skippedCount = 0
                                
                                selectedCategories.forEach { (projectName, categoryNames) ->
                                    if (categoryNames.isEmpty()) return@forEach
                                    
                                    if (projectName.lowercase() !in existingNames) {
                                        // Find the template
                                        val template = defaultTemplates.find { it.name == projectName } ?: return@forEach
                                        
                                        // Create project with selected categories only
                                        val project = Project(
                                            name = template.name,
                                            emoji = template.emoji,
                                            createdAt = System.currentTimeMillis()
                                        )
                                        
                                        val categories = template.categories
                                            .filter { categoryNames.contains(it.name) }
                                            .map { categoryTemplate ->
                                                Category(
                                                    projectId = 0, // Will be set by insertProjectWithCategories
                                                    name = categoryTemplate.name,
                                                    emoji = categoryTemplate.emoji,
                                                    createdAt = System.currentTimeMillis()
                                                )
                                            }
                                        
                                        if (categories.isNotEmpty() && viewModel.insertProjectWithCategories(project, categories)) {
                                            addedCount++
                                        }
                                    } else {
                                        skippedCount++
                                    }
                                }
                                
                                val message = when {
                                    addedCount > 0 && skippedCount > 0 -> 
                                        "$addedCount projects added, $skippedCount skipped (already exist)"
                                    addedCount > 0 -> 
                                        "$addedCount projects added successfully!"
                                    skippedCount > 0 -> 
                                        "All selected projects already exist"
                                    else -> 
                                        "No projects selected"
                                }
                                
                                viewModel.showToast(
                                    message,
                                    if (addedCount > 0) ToastType.SUCCESS else ToastType.INFO
                                )
                                
                                onDismiss()
                            } catch (e: Exception) {
                                viewModel.showToast("Error adding projects: ${e.message}", ToastType.ERROR)
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && selectedCategories.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        val projectCount = selectedCategories.size
                        val categoryCount = selectedCategories.values.sumOf { it.size }
                        Text("Add $projectCount Projects ($categoryCount categories)")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectTemplateCard(
    template: DefaultProjectTemplate,
    selectedCategories: Set<String>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleProject: (Boolean) -> Unit,
    onToggleCategory: (String) -> Unit
) {
    val allSelected = selectedCategories.size == template.categories.size && selectedCategories.isNotEmpty()
    val someSelected = selectedCategories.isNotEmpty() && !allSelected
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (someSelected || allSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Project Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Project Checkbox
                Checkbox(
                    checked = allSelected,
                    onCheckedChange = { onToggleProject(it) },
                    modifier = Modifier.size(20.dp)
                )
                
                // Project Info
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = template.emoji, fontSize = 20.sp)
                    Column {
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${selectedCategories.size}/${template.categories.size} selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
                
                // Expand/Collapse Button
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Expandable Category List
            if (isExpanded) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isExpanded,
                    enter = androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        template.categories.forEach { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = selectedCategories.contains(category.name),
                                    onCheckedChange = { onToggleCategory(category.name) },
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(text = category.emoji, fontSize = 16.sp)
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Made with Bob
