package com.h4rsh41.paisatracker.ui.management

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.h4rsh41.paisatracker.PaisaTrackerViewModel
import com.h4rsh41.paisatracker.data.Category
import com.h4rsh41.paisatracker.data.ProjectWithTotal
import com.h4rsh41.paisatracker.ui.common.ScreenHeader
import com.h4rsh41.paisatracker.ui.common.ToastType
import com.h4rsh41.paisatracker.ui.components.ConfirmationDialog
import com.h4rsh41.paisatracker.util.formatCurrency
import kotlinx.coroutines.launch

enum class ManagementTab { PROJECTS, CATEGORIES }
enum class ViewMode { LIST, GRID }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementScreen(
    viewModel: PaisaTrackerViewModel,
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf(ManagementTab.PROJECTS) }
    var searchQuery by remember { mutableStateOf("") }
    var showArchived by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    
    // Confirmation dialogs
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showCompleteConfirmation by remember { mutableStateOf(false) }
    var projectToToggle by remember { mutableStateOf<ProjectWithTotal?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Data
    val allProjects by viewModel.getAllProjectsWithTotal().collectAsState(initial = emptyList())
    val completedProjects by viewModel.getCompletedProjectsWithTotal().collectAsState(initial = emptyList())
    val allCategories by viewModel.getAllCategories().collectAsState(initial = emptyList())
    
    val displayProjects = if (showArchived) completedProjects else allProjects
    val filteredProjects = remember(displayProjects, searchQuery) {
        if (searchQuery.isBlank()) displayProjects
        else displayProjects.filter { 
            it.project.name.contains(searchQuery, ignoreCase = true) 
        }
    }
    
    val filteredCategories = remember(allCategories, searchQuery) {
        if (searchQuery.isBlank()) allCategories
        else allCategories.filter { 
            it.name.contains(searchQuery, ignoreCase = true) 
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        ScreenHeader(
            title = "Management",
            subtitle = "Organize projects & categories",
            icon = Icons.Default.FolderOpen,
            onBackClick = { navController.popBackStack() }
        )

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            indicator = { }
        ) {
            Tab(
                selected = selectedTab == ManagementTab.PROJECTS,
                onClick = { 
                    selectedTab = ManagementTab.PROJECTS
                    selectionMode = false
                    selectedItems = emptySet()
                },
                text = { 
                    Text(
                        "Projects (${displayProjects.size})",
                        fontWeight = if (selectedTab == ManagementTab.PROJECTS) FontWeight.Bold else FontWeight.Normal
                    ) 
                }
            )
            Tab(
                selected = selectedTab == ManagementTab.CATEGORIES,
                onClick = { 
                    selectedTab = ManagementTab.CATEGORIES
                    selectionMode = false
                    selectedItems = emptySet()
                },
                text = { 
                    Text(
                        "Categories (${allCategories.size})",
                        fontWeight = if (selectedTab == ManagementTab.CATEGORIES) FontWeight.Bold else FontWeight.Normal
                    ) 
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search ${if (selectedTab == ManagementTab.PROJECTS) "projects" else "categories"}...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedTab == ManagementTab.PROJECTS) {
                    FilterChip(
                        selected = showArchived,
                        onClick = {
                            showArchived = !showArchived
                            selectionMode = false
                            selectedItems = emptySet()
                        },
                        label = { Text(if (showArchived) "Completed" else "Active") },
                        leadingIcon = {
                            Icon(
                                if (showArchived) Icons.Default.CheckCircle else Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                
                FilterChip(
                    selected = selectionMode,
                    onClick = {
                        selectionMode = !selectionMode
                        if (!selectionMode) selectedItems = emptySet()
                    },
                    label = { Text(if (selectionMode) "Cancel" else "Select") },
                    leadingIcon = {
                        Icon(
                            if (selectionMode) Icons.Default.Close else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // View mode toggle and bulk actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimatedVisibility(
                    visible = selectionMode && selectedItems.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "${selectedItems.size} selected",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        
                        IconButton(
                            onClick = { showDeleteConfirmation = true }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                
                // View mode toggle (only when not in selection mode)
                if (!selectionMode || selectedItems.isEmpty()) {
                    IconButton(
                        onClick = {
                            viewMode = if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
                        }
                    ) {
                        Icon(
                            if (viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = "Toggle view mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        when (selectedTab) {
            ManagementTab.PROJECTS -> {
                ProjectManagementList(
                    projects = filteredProjects,
                    selectionMode = selectionMode,
                    selectedItems = selectedItems,
                    viewMode = viewMode,
                    onItemClick = { project ->
                        if (selectionMode) {
                            selectedItems = if (selectedItems.contains(project.project.id)) {
                                selectedItems - project.project.id
                            } else {
                                selectedItems + project.project.id
                            }
                        } else {
                            navController.navigate("project_details/${project.project.id}")
                        }
                    },
                    onToggleComplete = { project ->
                        projectToToggle = project
                        showCompleteConfirmation = true
                    }
                )
            }
            ManagementTab.CATEGORIES -> {
                CategoryManagementList(
                    categories = filteredCategories,
                    projects = allProjects,
                    selectionMode = selectionMode,
                    selectedItems = selectedItems,
                    viewMode = viewMode,
                    onItemClick = { category ->
                        if (selectionMode) {
                            selectedItems = if (selectedItems.contains(category.id)) {
                                selectedItems - category.id
                            } else {
                                selectedItems + category.id
                            }
                        }
                    }
                )
            }
        }
    }
    
    // Confirmation Dialogs
    if (showDeleteConfirmation) {
        ConfirmationDialog(
            title = "Delete ${selectedItems.size} ${if (selectedTab == ManagementTab.PROJECTS) "projects" else "categories"}?",
            message = "This action cannot be undone. All associated data will be permanently deleted.",
            confirmText = "Delete",
            onConfirm = {
                scope.launch {
                    if (selectedTab == ManagementTab.PROJECTS) {
                        val projectsToDelete = filteredProjects.filter { selectedItems.contains(it.project.id) }
                        projectsToDelete.forEach { projectWithTotal ->
                            viewModel.deleteProject(projectWithTotal.project)
                        }
                        viewModel.showToast("${selectedItems.size} projects deleted", ToastType.SUCCESS)
                    } else {
                        val categoriesToDelete = filteredCategories.filter { selectedItems.contains(it.id) }
                        categoriesToDelete.forEach { category ->
                            viewModel.deleteCategory(category)
                        }
                        viewModel.showToast("${selectedItems.size} categories deleted", ToastType.SUCCESS)
                    }
                    selectedItems = emptySet()
                    selectionMode = false
                    showDeleteConfirmation = false
                }
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
    
    if (showCompleteConfirmation && projectToToggle != null) {
        val project = projectToToggle!!
        ConfirmationDialog(
            title = if (project.project.isCompleted) "Reactivate project?" else "Complete project?",
            message = if (project.project.isCompleted)
                "This will move the project back to active projects."
            else
                "This will mark the project as completed and move it to completed projects.",
            confirmText = if (project.project.isCompleted) "Reactivate" else "Complete",
            onConfirm = {
                scope.launch {
                    viewModel.updateProject(project.project.copy(isCompleted = !project.project.isCompleted))
                    viewModel.showToast(
                        if (project.project.isCompleted) "Project reactivated" else "Project completed",
                        ToastType.SUCCESS
                    )
                    showCompleteConfirmation = false
                    projectToToggle = null
                }
            },
            onDismiss = {
                showCompleteConfirmation = false
                projectToToggle = null
            }
        )
    }
}

@Composable
private fun ProjectManagementList(
    projects: List<ProjectWithTotal>,
    selectionMode: Boolean,
    selectedItems: Set<Long>,
    viewMode: ViewMode,
    onItemClick: (ProjectWithTotal) -> Unit,
    onToggleComplete: (ProjectWithTotal) -> Unit
) {
    if (projects.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.FolderOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No projects found",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        when (viewMode) {
            ViewMode.LIST -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projects, key = { it.project.id }) { projectWithTotal ->
                        ProjectManagementItem(
                            projectWithTotal = projectWithTotal,
                            isSelected = selectedItems.contains(projectWithTotal.project.id),
                            selectionMode = selectionMode,
                            onClick = { onItemClick(projectWithTotal) },
                            onToggleComplete = { onToggleComplete(projectWithTotal) }
                        )
                    }
                }
            }
            ViewMode.GRID -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projects, key = { it.project.id }) { projectWithTotal ->
                        ProjectManagementGridItem(
                            projectWithTotal = projectWithTotal,
                            isSelected = selectedItems.contains(projectWithTotal.project.id),
                            selectionMode = selectionMode,
                            onClick = { onItemClick(projectWithTotal) },
                            onToggleComplete = { onToggleComplete(projectWithTotal) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectManagementItem(
    projectWithTotal: ProjectWithTotal,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() }
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = projectWithTotal.project.emoji,
                        fontSize = 24.sp
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = projectWithTotal.project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatCurrency(projectWithTotal.totalAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (!selectionMode) {
                IconButton(onClick = onToggleComplete) {
                    Icon(
                        if (projectWithTotal.project.isCompleted) Icons.AutoMirrored.Filled.Undo else Icons.Default.CheckCircle,
                        contentDescription = if (projectWithTotal.project.isCompleted) "Reactivate" else "Complete",
                        tint = if (projectWithTotal.project.isCompleted) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryManagementList(
    categories: List<Category>,
    projects: List<ProjectWithTotal>,
    selectionMode: Boolean,
    selectedItems: Set<Long>,
    viewMode: ViewMode,
    onItemClick: (Category) -> Unit
) {
    if (categories.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No categories found",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        when (viewMode) {
            ViewMode.LIST -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        val project = projects.find { it.project.id == category.projectId }
                        CategoryManagementItem(
                            category = category,
                            projectName = project?.project?.name ?: "Unknown",
                            isSelected = selectedItems.contains(category.id),
                            selectionMode = selectionMode,
                            onClick = { onItemClick(category) }
                        )
                    }
                }
            }
            ViewMode.GRID -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        val project = projects.find { it.project.id == category.projectId }
                        CategoryManagementGridItem(
                            category = category,
                            projectName = project?.project?.name ?: "Unknown",
                            isSelected = selectedItems.contains(category.id),
                            selectionMode = selectionMode,
                            onClick = { onItemClick(category) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryManagementItem(
    category: Category,
    projectName: String,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() }
                )
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.emoji,
                    fontSize = 24.sp
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = projectName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// Made with Bob


@Composable
private fun ProjectManagementGridItem(
    projectWithTotal: ProjectWithTotal,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(20.dp))
                }
                
                if (!selectionMode) {
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            if (projectWithTotal.project.isCompleted) Icons.AutoMirrored.Filled.Undo else Icons.Default.CheckCircle,
                            contentDescription = if (projectWithTotal.project.isCompleted) "Reactivate" else "Complete",
                            tint = if (projectWithTotal.project.isCompleted) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = projectWithTotal.project.emoji,
                    fontSize = 28.sp
                )
            }
            
            Text(
                text = projectWithTotal.project.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = formatCurrency(projectWithTotal.totalAmount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CategoryManagementGridItem(
    category: Category,
    projectName: String,
    isSelected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectionMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.emoji,
                    fontSize = 28.sp
                )
            }
            
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = projectName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Made with Bob
