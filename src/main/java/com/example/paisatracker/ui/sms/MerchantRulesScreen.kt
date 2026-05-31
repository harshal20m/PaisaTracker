package com.example.paisatracker.ui.sms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.paisatracker.data.Category
import com.example.paisatracker.data.MerchantRuleEntity
import com.example.paisatracker.data.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantRulesScreen(
    navController: NavHostController,
    viewModel: MerchantRuleViewModel
) {
    val merchantRules by viewModel.merchantRules.collectAsStateWithLifecycle(initialValue = emptyList())
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<MerchantRuleEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<MerchantRuleEntity?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showGuidedSetup by remember { mutableStateOf(false) }

    // Show snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }
    
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Merchant Rules") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Help button
                    IconButton(
                        onClick = { showHelpDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Help,
                            contentDescription = "Help & Templates",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Import Presets button
                    IconButton(
                        onClick = { showImportDialog = true },
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Import Presets",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Rule")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (merchantRules.isEmpty()) {
                EnhancedEmptyState(
                    onAddClick = { showAddDialog = true },
                    onImportClick = { showImportDialog = true }
                )
            } else {
                MerchantRulesGridContent(
                    merchantRules = merchantRules,
                    getCategoryName = { viewModel.getCategoryName(it) ?: "Unknown" },
                    getProjectName = { viewModel.getProjectName(it) },
                    onToggleActive = { ruleId, isActive -> viewModel.toggleRuleActive(ruleId, isActive) },
                    onEdit = { editingRule = it },
                    onDelete = { showDeleteDialog = it },
                    onBulkDelete = { ruleIds -> viewModel.deleteRules(ruleIds) }
                )
            }
        }
    }

    // Help Dialog
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }
    
    // Import Presets Dialog
    if (showImportDialog) {
        val missingCategories = viewModel.getMissingCategories()
        ImportPresetsDialog(
            missingCategories = missingCategories,
            onDismiss = { showImportDialog = false },
            onImport = {
                viewModel.importPresetRules()
                showImportDialog = false
            },
            onGuidedSetup = {
                showImportDialog = false
                showGuidedSetup = true
            }
        )
    }
    
    // Guided Setup Dialog
    if (showGuidedSetup) {
        val missingCategories = viewModel.getMissingCategories()
        GuidedSetupDialog(
            missingCategories = missingCategories,
            existingProjects = projects,
            onComplete = { projectId, projectName, projectEmoji ->
                viewModel.createCategoriesAndImport(
                    projectId = projectId,
                    projectName = projectName,
                    projectEmoji = projectEmoji,
                    missingCategories = missingCategories
                )
                showGuidedSetup = false
            },
            onDismiss = { showGuidedSetup = false }
        )
    }

    // Add/Edit Dialog
    if (showAddDialog || editingRule != null) {
        AddEditRuleDialog(
            rule = editingRule,
            categories = categories,
            projects = projects,
            onDismiss = {
                showAddDialog = false
                editingRule = null
            },
            onSave = { pattern, categoryId, projectId, priority ->
                if (editingRule != null) {
                    viewModel.updateRule(
                        id = editingRule!!.id,
                        merchantPattern = pattern,
                        categoryId = categoryId,
                        projectId = projectId,
                        priority = priority
                    )
                } else {
                    viewModel.addRule(
                        merchantPattern = pattern,
                        categoryId = categoryId,
                        projectId = projectId,
                        priority = priority
                    )
                }
                showAddDialog = false
                editingRule = null
            },
            validatePattern = { viewModel.validateMerchantPattern(it) }
        )
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { rule ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Rule?") },
            text = { Text("Are you sure you want to delete the rule for \"${rule.merchantPattern}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRule(rule.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MerchantRuleCard(
    rule: MerchantRuleEntity,
    categoryName: String,
    projectName: String?,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isActive) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            }
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Merchant Pattern
                Text(
                    text = rule.merchantPattern,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = if (rule.isActive) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    }
                )

                // Active Toggle
                Switch(
                    checked = rule.isActive,
                    onCheckedChange = { onToggleActive() },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Category and Project
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (projectName != null) {
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = projectName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Stats and Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority and Match Count
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "P${rule.priority}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Match Count Badge
                    if (rule.matchCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${rule.matchCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Action Buttons
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Rule,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "No Merchant Rules Yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Create rules to automatically categorize transactions from specific merchants",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Your First Rule")
        }

        Spacer(Modifier.height(32.dp))

        // Tips Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Pattern Matching Tips",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(12.dp))

                TipItem("Use | to match multiple merchants")
                TipItem("Example: \"Amazon|Flipkart|Myntra\"")
                TipItem("Patterns are case-insensitive")
                TipItem("Higher priority rules are checked first")
            }
        }
    }
}

@Composable
private fun TipItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            modifier = Modifier.padding(end = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditRuleDialog(
    rule: MerchantRuleEntity?,
    categories: List<Category>,
    projects: List<Project>,
    onDismiss: () -> Unit,
    onSave: (pattern: String, categoryId: Long, projectId: Long?, priority: Int) -> Unit,
    validatePattern: (String) -> String?
) {
    var merchantPattern by remember { mutableStateOf(rule?.merchantPattern ?: "") }
    var selectedCategoryId by remember { mutableStateOf(rule?.categoryId ?: (categories.firstOrNull()?.id ?: 0L)) }
    var selectedProjectId by remember { mutableStateOf<Long?>(rule?.projectId) }
    var priority by remember { mutableStateOf(rule?.priority ?: 50) }
    var patternError by remember { mutableStateOf<String?>(null) }

    var showCategoryPicker by remember { mutableStateOf(false) }
    var showProjectPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Title
                Text(
                    text = if (rule == null) "Add Merchant Rule" else "Edit Merchant Rule",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(20.dp))

                // Merchant Pattern Input
                OutlinedTextField(
                    value = merchantPattern,
                    onValueChange = {
                        merchantPattern = it
                        patternError = validatePattern(it)
                    },
                    label = { Text("Merchant Pattern *") },
                    supportingText = {
                        Text(
                            if (patternError != null) patternError!!
                            else "Use | to match multiple (e.g., Amazon|Flipkart)",
                            color = if (patternError != null) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError = patternError != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(Modifier.height(16.dp))

                // Category Picker
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryPicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Category *",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            val selectedCategory = categories.find { it.id == selectedCategoryId }
                            Text(
                                text = selectedCategory?.let { "${it.emoji} ${it.name}" } ?: "Select Category",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Project Picker
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProjectPicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Project (Optional)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            val selectedProject = projects.find { it.id == selectedProjectId }
                            Text(
                                text = selectedProject?.let { "${it.emoji} ${it.name}" } ?: "None",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Priority Slider
                Column {
                    Text(
                        text = "Priority (0 = highest)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = priority.toFloat(),
                        onValueChange = { priority = it.toInt() },
                        valueRange = 0f..100f,
                        steps = 19
                    )
                    Text(
                        text = "Current: $priority",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (patternError == null && merchantPattern.isNotBlank() && selectedCategoryId != 0L) {
                                onSave(merchantPattern, selectedCategoryId, selectedProjectId, priority)
                            }
                        },
                        enabled = patternError == null && merchantPattern.isNotBlank() && selectedCategoryId != 0L
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    // Category Picker Dialog
    if (showCategoryPicker) {
        PickerDialog(
            title = "Select Category",
            items = categories,
            selectedId = selectedCategoryId,
            onDismiss = { showCategoryPicker = false },
            onSelect = {
                selectedCategoryId = it ?: 0L
                showCategoryPicker = false
            },
            itemContent = { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedCategoryId = category?.id ?: 0L
                            showCategoryPicker = false
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category?.emoji ?: "",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = category?.name ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        )
    }

    // Project Picker Dialog
    if (showProjectPicker) {
        PickerDialog(
            title = "Select Project",
            items = listOf(null) + projects,
            selectedId = selectedProjectId,
            onDismiss = { showProjectPicker = false },
            onSelect = { 
                selectedProjectId = it
                showProjectPicker = false
            },
            itemContent = { project ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedProjectId = project?.id
                            showProjectPicker = false
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (project == null) {
                        Text(
                            text = "None",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = project.emoji,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun <T> PickerDialog(
    title: String,
    items: List<T?>,
    selectedId: Long?,
    onDismiss: () -> Unit,
    onSelect: (Long?) -> Unit,
    itemContent: @Composable (T?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(items) { item ->
                        itemContent(item)
                    }
                }
            }
        }
    }
}

// Made with Bob


@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Quick Start", "Templates", "Examples")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "How to Create Merchant Rules",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    0 -> QuickStartContent()
                    1 -> TemplatesContent()
                    2 -> ExamplesContent()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It!")
            }
        }
    )
}

@Composable
private fun QuickStartContent() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            "3 Simple Steps:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        StepItem(
            number = "1",
            title = "Enter Merchant Pattern",
            description = "Type merchant names separated by | (pipe). Example: Swiggy|Zomato"
        )
        
        StepItem(
            number = "2",
            title = "Select Category",
            description = "Choose where expenses should be categorized (e.g., Food & Dining)"
        )
        
        StepItem(
            number = "3",
            title = "Set Priority (Optional)",
            description = "Lower number = higher priority. Default is 50."
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Pro Tip",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Start by importing preset rules, then customize them for your needs!",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun TemplatesContent() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            "Copy & Customize These Templates:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        TemplateCard(
            title = "🍔 Food Delivery",
            pattern = "Swiggy|Zomato|Uber Eats|Domino's",
            category = "Food & Dining",
            priority = "10"
        )
        
        TemplateCard(
            title = "🛒 Online Shopping",
            pattern = "Amazon|Flipkart|Myntra|Meesho",
            category = "Shopping",
            priority = "20"
        )
        
        TemplateCard(
            title = "🚗 Transportation",
            pattern = "Uber|Ola|Rapido|IRCTC",
            category = "Transportation",
            priority = "15"
        )
        
        TemplateCard(
            title = "⛽ Fuel Stations",
            pattern = "Indian Oil|HP|BPCL|Shell",
            category = "Fuel",
            priority = "10"
        )
        
        TemplateCard(
            title = "🎬 Entertainment",
            pattern = "Netflix|Prime Video|Hotstar|Spotify",
            category = "Entertainment",
            priority = "30"
        )
        
        TemplateCard(
            title = "🥬 Groceries",
            pattern = "BigBasket|Blinkit|Zepto|DMart",
            category = "Groceries",
            priority = "15"
        )
        
        TemplateCard(
            title = "💊 Healthcare",
            pattern = "PharmEasy|1mg|Apollo|Netmeds",
            category = "Health",
            priority = "5"
        )
        
        TemplateCard(
            title = "📱 Mobile Recharge",
            pattern = "Jio|Airtel|Vi|Vodafone",
            category = "Utilities",
            priority = "25"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "How to Use Templates:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "1. Copy the pattern you need\n" +
                    "2. Tap + button to add new rule\n" +
                    "3. Paste pattern in Merchant Pattern field\n" +
                    "4. Select matching category\n" +
                    "5. Adjust priority if needed\n" +
                    "6. Save!",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ExamplesContent() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            "Real-World Examples:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        ExampleCard(
            scenario = "Track All Food Expenses",
            pattern = "Swiggy|Zomato|McDonald's|KFC|Starbucks|Domino's",
            category = "Food & Dining",
            priority = "10",
            explanation = "Catches all food delivery and restaurant transactions in one category"
        )
        
        ExampleCard(
            scenario = "Separate Business Travel",
            pattern = "Uber|Ola|IRCTC",
            category = "Business Expenses",
            priority = "5",
            explanation = "High priority (5) ensures business travel is categorized correctly before personal expenses"
        )
        
        ExampleCard(
            scenario = "Track Subscriptions",
            pattern = "Netflix|Prime|Spotify|Hotstar",
            category = "Entertainment",
            priority = "30",
            explanation = "Lower priority for recurring subscriptions"
        )
        
        ExampleCard(
            scenario = "Grocery Shopping",
            pattern = "BigBasket|Blinkit|Zepto|DMart|More",
            category = "Groceries",
            priority = "15",
            explanation = "Medium priority for regular grocery purchases"
        )
        
        ExampleCard(
            scenario = "Fuel Expenses",
            pattern = "Indian Oil|HP|BPCL|Shell|Reliance",
            category = "Fuel",
            priority = "10",
            explanation = "Track all fuel station transactions automatically"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Understanding Priority",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• 0-10: Critical (Business, Healthcare)\n" +
                    "• 11-20: High (Food, Transport, Fuel)\n" +
                    "• 21-40: Medium (Shopping, Entertainment)\n" +
                    "• 41-100: Low (Miscellaneous)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StepItem(number: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TemplateCard(
    title: String,
    pattern: String,
    category: String,
    priority: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(label = "Pattern:", value = pattern)
            InfoRow(label = "Category:", value = category)
            InfoRow(label = "Priority:", value = priority)
        }
    }
}

@Composable
private fun ExampleCard(
    scenario: String,
    pattern: String,
    category: String,
    priority: String,
    explanation: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                scenario,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(label = "Pattern:", value = pattern)
            InfoRow(label = "Category:", value = category)
            InfoRow(label = "Priority:", value = priority)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "💡 $explanation",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun ImportPresetsDialog(
    missingCategories: List<String>,
    onDismiss: () -> Unit,
    onImport: () -> Unit,
    onGuidedSetup: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Preset Rules") },
        text = {
            Column {
                if (missingCategories.isNotEmpty()) {
                    Text(
                        "⚠️ Missing Categories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "The following categories are required for preset rules:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    missingCategories.forEach { category ->
                        Text(
                            "• $category",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Would you like to create them automatically?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text("Import 50+ preset merchant rules for popular merchants?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This will add rules for Amazon, Swiggy, Zomato, Uber, and many more.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Row {
                if (missingCategories.isNotEmpty()) {
                    TextButton(onClick = onGuidedSetup) {
                        Text("Guided Setup")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TextButton(onClick = onImport) {
                    Text(if (missingCategories.isNotEmpty()) "Import Anyway" else "Import")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun GuidedSetupDialog(
    missingCategories: List<String>,
    existingProjects: List<Project>,
    onComplete: (projectId: Long?, projectName: String?, projectEmoji: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1=Project, 2=Confirm
    var selectedOption by remember { mutableStateOf(if (existingProjects.isEmpty()) "new" else "existing") }
    var newProjectName by remember { mutableStateOf("") }
    var newProjectEmoji by remember { mutableStateOf("📊") }
    var selectedProject by remember { mutableStateOf<Project?>(existingProjects.firstOrNull()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                when (step) {
                    1 -> "Step 1: Project Setup"
                    else -> "Step 2: Confirm"
                }
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                when (step) {
                    1 -> {
                        Text(
                            "Choose how to organize your categories:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Option 1: Create New Project
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedOption = "new" }
                                .background(
                                    if (selectedOption == "new") 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOption == "new",
                                onClick = { selectedOption = "new" }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Create New Project",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Organize categories under a new project",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        if (selectedOption == "new") {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newProjectName,
                                onValueChange = { newProjectName = it },
                                label = { Text("Project Name") },
                                placeholder = { Text("e.g., Personal Expenses") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newProjectEmoji,
                                onValueChange = { if (it.length <= 2) newProjectEmoji = it },
                                label = { Text("Emoji (Optional)") },
                                placeholder = { Text("📊") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        if (existingProjects.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Option 2: Use Existing Project
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedOption = "existing" }
                                    .background(
                                        if (selectedOption == "existing") 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedOption == "existing",
                                    onClick = { selectedOption = "existing" }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "Use Existing Project",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Add categories to an existing project",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            
                            if (selectedOption == "existing") {
                                Spacer(modifier = Modifier.height(8.dp))
                                existingProjects.forEach { project ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { selectedProject = project }
                                            .background(
                                                if (selectedProject?.id == project.id)
                                                    MaterialTheme.colorScheme.secondaryContainer
                                                else
                                                    Color.Transparent
                                            )
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedProject?.id == project.id,
                                            onClick = { selectedProject = project }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("${project.emoji} ${project.name}")
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        Text(
                            "Review & Confirm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Project:",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    if (selectedOption == "new") 
                                        "$newProjectEmoji $newProjectName (New)"
                                    else 
                                        "${selectedProject?.emoji} ${selectedProject?.name}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            "Categories to Create (${missingCategories.size}):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        missingCategories.forEach { category ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(category, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "50+ preset merchant rules will be imported automatically",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (step == 1) {
                        step = 2
                    } else {
                        val projectId = if (selectedOption == "existing") selectedProject?.id else null
                        val projectName = if (selectedOption == "new") newProjectName else null
                        val projectEmoji = if (selectedOption == "new") newProjectEmoji else null
                        onComplete(projectId, projectName, projectEmoji)
                    }
                },
                enabled = when {
                    step == 1 && selectedOption == "new" -> newProjectName.isNotBlank()
                    step == 1 && selectedOption == "existing" -> selectedProject != null
                    else -> true
                }
            ) {
                Text(if (step == 1) "Next" else "Create & Import")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (step == 1) {
                        onDismiss()
                    } else {
                        step = 1
                    }
                }
            ) {
                Text(if (step == 1) "Cancel" else "Back")
            }
        }
    )
}
