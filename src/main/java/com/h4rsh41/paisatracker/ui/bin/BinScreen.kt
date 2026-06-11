package com.h4rsh41.paisatracker.ui.bin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import com.h4rsh41.paisatracker.ui.common.ScreenHeader
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.h4rsh41.paisatracker.ui.common.DeleteConfirmationSheetContent
import com.h4rsh41.paisatracker.PaisaTrackerViewModel
import com.h4rsh41.paisatracker.data.ActionHistory
import com.h4rsh41.paisatracker.data.Expense
import com.h4rsh41.paisatracker.data.Project
import com.h4rsh41.paisatracker.data.Category
import com.h4rsh41.paisatracker.data.Budget
import com.h4rsh41.paisatracker.data.SalaryRecord
import com.h4rsh41.paisatracker.ui.theme.PaisaTrackerTheme
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinSheetContent(viewModel: PaisaTrackerViewModel, onDismiss: () -> Unit) {
    val history by viewModel.actionHistory.collectAsStateWithLifecycle()
    val deletedItems = history.filter { it.actionType == "DELETE" }
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Recycle Bin",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Items are deleted after 30 days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            if (deletedItems.isNotEmpty()) {
                TextButton(
                    onClick = { showClearConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All")
                }
            }
        }

        if (deletedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Your bin is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(deletedItems, key = { it.id }) { item ->
                    var showDetailsSheet by remember { mutableStateOf(false) }
                    
                    BinItemRow(
                        item = item,
                        onRestore = { viewModel.restoreAction(item) },
                        onDelete = { viewModel.deleteAction(item) },
                        onClick = { showDetailsSheet = true }
                    )
                    
                    if (showDetailsSheet) {
                        BinItemDetailsSheet(
                            item = item,
                            onDismiss = { showDetailsSheet = false },
                            onRestore = {
                                viewModel.restoreAction(item)
                                showDetailsSheet = false
                            },
                            onDelete = {
                                viewModel.deleteAction(item)
                                showDetailsSheet = false
                            }
                        )
                    }
                }
            }
        }
    }

    val clearSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showClearConfirm) {
        ModalBottomSheet(
            onDismissRequest = { showClearConfirm = false },
            sheetState = clearSheetState
        ) {
            DeleteConfirmationSheetContent(
                title = "Clear Bin?",
                message = "This will permanently delete all items in the recycle bin. This action cannot be undone.",
                onConfirm = {
                    viewModel.clearBin()
                    showClearConfirm = false
                },
                onDismiss = { showClearConfirm = false },
                confirmText = "Clear All"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinScreen(viewModel: PaisaTrackerViewModel, navController: NavController) {
    Scaffold(
        topBar = {
            ScreenHeader(
                title = "Recycle Bin",
                subtitle = "Recently deleted items",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            BinSheetContent(viewModel = viewModel, onDismiss = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinItemRow(
    item: ActionHistory,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit = {}
) {
    val daysRemaining = 30 - TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - item.timestamp)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val (title, subtitle, icon, emoji) = remember(item) {
        try {
            val gson = Gson()
            when (item.entityType) {
                "PROJECT" -> {
                    try {
                        val data = gson.fromJson(item.entityData, JsonObject::class.java)
                        if (data == null || !data.has("project")) {
                            return@remember Quadruple("Project", "Deleted project", Icons.Default.Folder, "📁")
                        }
                        
                        val projectObj = data.getAsJsonObject("project")
                        if (projectObj == null) {
                            return@remember Quadruple("Project", "Deleted project", Icons.Default.Folder, "📁")
                        }
                        
                        val project = gson.fromJson(projectObj, Project::class.java)
                        val children = data.getAsJsonArray("children")
                        val childCount = children?.size() ?: 0
                        
                        val totalExpenses = try {
                            children?.sumOf { childElement ->
                                try {
                                    val childObj = childElement?.asJsonObject
                                    val expenses = childObj?.getAsJsonArray("expenses")
                                    expenses?.size() ?: 0
                                } catch (e: Exception) {
                                    0
                                }
                            } ?: 0
                        } catch (e: Exception) {
                            0
                        }
                        
                        Quadruple(
                            project.name.takeIf { it.isNotBlank() } ?: "Unnamed Project",
                            "Project • $childCount categories, $totalExpenses expenses",
                            Icons.Default.Folder,
                            project.emoji.takeIf { it.isNotBlank() } ?: "📁"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Quadruple("Project", "Deleted project", Icons.Default.Folder, "📁")
                    }
                }
                "CATEGORY" -> {
                    try {
                        val data = gson.fromJson(item.entityData, JsonObject::class.java)
                        if (data == null || !data.has("category")) {
                            return@remember Quadruple("Category", "Deleted category", Icons.Default.Category, "📂")
                        }
                        
                        val categoryObj = data.getAsJsonObject("category")
                        if (categoryObj == null) {
                            return@remember Quadruple("Category", "Deleted category", Icons.Default.Category, "📂")
                        }
                        
                        val category = gson.fromJson(categoryObj, Category::class.java)
                        val expenses = data.getAsJsonArray("expenses")
                        val expCount = expenses?.size() ?: 0
                        
                        Quadruple(
                            category.name.takeIf { it.isNotBlank() } ?: "Unnamed Category",
                            "Category • $expCount expenses",
                            Icons.Default.Category,
                            category.emoji.takeIf { it.isNotBlank() } ?: "📂"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Quadruple("Category", "Deleted category", Icons.Default.Category, "📂")
                    }
                }
                "EXPENSE" -> {
                    try {
                        val expense = gson.fromJson(item.entityData, Expense::class.java)
                        if (expense == null) {
                            return@remember Quadruple("Expense", "Deleted expense", Icons.AutoMirrored.Filled.ReceiptLong, null)
                        }
                        
                        Quadruple(
                            expense.description.takeIf { it.isNotBlank() } ?: "Unlabeled Expense",
                            "Expense • ${String.format("%.2f", expense.amount)}",
                            Icons.AutoMirrored.Filled.ReceiptLong,
                            null
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Quadruple("Expense", "Deleted expense", Icons.AutoMirrored.Filled.ReceiptLong, null)
                    }
                }
                "BUDGET" -> {
                    try {
                        val budget = gson.fromJson(item.entityData, Budget::class.java)
                        if (budget == null) {
                            return@remember Quadruple("Budget", "Deleted budget", Icons.Default.AccountBalanceWallet, "💰")
                        }
                        
                        Quadruple(
                            budget.name.takeIf { it.isNotBlank() } ?: "Unnamed Budget",
                            "Budget • ${String.format("%.2f", budget.limitAmount)} (${budget.period.displayName})",
                            Icons.Default.AccountBalanceWallet,
                            budget.emoji.takeIf { it.isNotBlank() } ?: "💰"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Quadruple("Budget", "Deleted budget", Icons.Default.AccountBalanceWallet, "💰")
                    }
                }
                "SALARY_RECORD" -> {
                    try {
                        val record = gson.fromJson(item.entityData, SalaryRecord::class.java)
                        if (record == null) {
                            return@remember Quadruple("Salary", "Deleted salary record", Icons.Default.Payments, "💰")
                        }
                        
                        Quadruple(
                            "Salary: ${record.month}/${record.year}",
                            "Income • ${String.format("%.2f", record.amount)}",
                            Icons.Default.Payments,
                            "💰"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Quadruple("Salary", "Deleted salary record", Icons.Default.Payments, "💰")
                    }
                }
                else -> {
                    Quadruple(
                        item.entityType.takeIf { it.isNotBlank() } ?: "Unknown",
                        "Deleted item",
                        Icons.Default.Info,
                        "🗑️"
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Quadruple("Unknown", "Corrupted data", Icons.Default.Info, "⚠️")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (emoji != null) {
                        Text(text = emoji, fontSize = 20.sp)
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = subtitle.substringBefore("•").trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " • $daysRemaining days left",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Row {
                IconButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Restore",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Permanently",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showDeleteConfirm) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteConfirm = false },
            sheetState = deleteSheetState
        ) {
            DeleteConfirmationSheetContent(
                title = "Delete Permanently?",
                message = "Are you sure you want to delete this $title permanently? This cannot be undone.",
                onConfirm = {
                    onDelete()
                    showDeleteConfirm = false
                },
                onDismiss = { showDeleteConfirm = false }
            )
        }
    }
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinItemDetailsSheet(
    item: ActionHistory,
    onDismiss: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val daysRemaining = 30 - TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - item.timestamp)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header with icon and title
            val (title, icon, emoji) = remember(item) {
                try {
                    val gson = Gson()
                    when (item.entityType) {
                        "PROJECT" -> {
                            val data = gson.fromJson(item.entityData, JsonObject::class.java)
                            val projectObj = data?.getAsJsonObject("project")
                            val project = projectObj?.let { gson.fromJson(it, Project::class.java) }
                            Triple(
                                project?.name ?: "Project",
                                Icons.Default.Folder,
                                project?.emoji ?: "📁"
                            )
                        }
                        "CATEGORY" -> {
                            val data = gson.fromJson(item.entityData, JsonObject::class.java)
                            val categoryObj = data?.getAsJsonObject("category")
                            val category = categoryObj?.let { gson.fromJson(it, Category::class.java) }
                            Triple(
                                category?.name ?: "Category",
                                Icons.Default.Category,
                                category?.emoji ?: "📂"
                            )
                        }
                        "EXPENSE" -> {
                            val expense = gson.fromJson(item.entityData, Expense::class.java)
                            Triple(
                                expense?.description ?: "Expense",
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                null
                            )
                        }
                        "BUDGET" -> {
                            val budget = gson.fromJson(item.entityData, Budget::class.java)
                            Triple(
                                budget?.name ?: "Budget",
                                Icons.Default.AccountBalanceWallet,
                                budget?.emoji ?: "💰"
                            )
                        }
                        "SALARY_RECORD" -> {
                            val record = gson.fromJson(item.entityData, SalaryRecord::class.java)
                            Triple(
                                "Salary: ${record?.month}/${record?.year}",
                                Icons.Default.Payments,
                                "💰"
                            )
                        }
                        else -> Triple("Unknown Item", Icons.Default.Info, "🗑️")
                    }
                } catch (e: Exception) {
                    Triple("Unknown Item", Icons.Default.Info, "⚠️")
                }
            }
            
            // Icon and Title Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (emoji != null) {
                            Text(text = emoji, fontSize = 32.sp)
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.entityType.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Warning Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Will be permanently deleted in $daysRemaining days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Details Section
            when (item.entityType) {
                "PROJECT" -> ProjectDetailsContent(item)
                "CATEGORY" -> CategoryDetailsContent(item)
                "EXPENSE" -> ExpenseDetailsContent(item)
                "BUDGET" -> BudgetDetailsContent(item)
                "SALARY_RECORD" -> SalaryDetailsContent(item)
                else -> GenericDetailsContent(item)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Forever")
                }
                
                Button(
                    onClick = onRestore,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore")
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showDeleteConfirm = false },
            sheetState = deleteSheetState
        ) {
            DeleteConfirmationSheetContent(
                title = "Delete Permanently?",
                message = "This item will be permanently deleted and cannot be recovered. Are you sure?",
                onConfirm = {
                    onDelete()
                    showDeleteConfirm = false
                },
                onDismiss = { showDeleteConfirm = false }
            )
        }
    }
}

@Composable
private fun ProjectDetailsContent(item: ActionHistory) {
    val projectData = remember(item) {
        try {
            val gson = Gson()
            val data = gson.fromJson(item.entityData, JsonObject::class.java)
            val projectObj = data?.getAsJsonObject("project")
            val project = projectObj?.let { gson.fromJson(it, Project::class.java) }
            val children = data?.getAsJsonArray("children")
            val childCount = children?.size() ?: 0
            
            val totalExpenses = try {
                children?.sumOf { childElement ->
                    try {
                        val childObj = childElement?.asJsonObject
                        val expenses = childObj?.getAsJsonArray("expenses")
                        expenses?.size() ?: 0
                    } catch (e: Exception) { 0 }
                } ?: 0
            } catch (e: Exception) { 0 }
            
            Triple(project, childCount, totalExpenses)
        } catch (e: Exception) {
            null
        }
    }
    
    if (projectData == null) {
        ErrorDetailsContent()
    } else {
        val (project, childCount, totalExpenses) = projectData
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Project Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            DetailRow(label = "Name", value = project?.name ?: "Unknown")
            DetailRow(label = "Categories", value = "$childCount")
            DetailRow(label = "Total Expenses", value = "$totalExpenses")
            DetailRow(
                label = "Created",
                value = formatDate(project?.createdAt ?: item.timestamp)
            )
            DetailRow(
                label = "Deleted",
                value = formatDate(item.timestamp)
            )
        }
    }
}

@Composable
private fun CategoryDetailsContent(item: ActionHistory) {
    val categoryData = remember(item) {
        try {
            val gson = Gson()
            val data = gson.fromJson(item.entityData, JsonObject::class.java)
            val categoryObj = data?.getAsJsonObject("category")
            val category = categoryObj?.let { gson.fromJson(it, Category::class.java) }
            val expenses = data?.getAsJsonArray("expenses")
            val expCount = expenses?.size() ?: 0
            Pair(category, expCount)
        } catch (e: Exception) {
            null
        }
    }
    
    if (categoryData == null) {
        ErrorDetailsContent()
    } else {
        val (category, expCount) = categoryData
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Category Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            DetailRow(label = "Name", value = category?.name ?: "Unknown")
            DetailRow(label = "Expenses", value = "$expCount")
            DetailRow(
                label = "Created",
                value = formatDate(category?.createdAt ?: item.timestamp)
            )
            DetailRow(
                label = "Deleted",
                value = formatDate(item.timestamp)
            )
        }
    }
}

@Composable
private fun ExpenseDetailsContent(item: ActionHistory) {
    val expense = remember(item) {
        try {
            val gson = Gson()
            gson.fromJson(item.entityData, Expense::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    if (expense == null) {
        ErrorDetailsContent()
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Expense Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            DetailRow(
                label = "Description",
                value = expense.description?.takeIf { it.isNotBlank() } ?: "No description"
            )
            DetailRow(
                label = "Amount",
                value = "₹${String.format("%.2f", expense.amount)}"
            )
            DetailRow(
                label = "Date",
                value = formatDate(expense.date)
            )
            expense.paymentMethod?.let {
                DetailRow(label = "Payment Method", value = it)
            }
            expense.assetPath?.let {
                DetailRow(label = "Has Attachment", value = "Yes")
            }
            DetailRow(
                label = "Deleted",
                value = formatDate(item.timestamp)
            )
        }
    }
}

@Composable
private fun BudgetDetailsContent(item: ActionHistory) {
    val budget = remember(item) {
        try {
            val gson = Gson()
            gson.fromJson(item.entityData, Budget::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    if (budget == null) {
        ErrorDetailsContent()
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Budget Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            DetailRow(label = "Name", value = budget.name)
            DetailRow(
                label = "Limit",
                value = "₹${String.format("%.2f", budget.limitAmount)}"
            )
            DetailRow(
                label = "Period",
                value = budget.period.displayName
            )
            DetailRow(
                label = "Deleted",
                value = formatDate(item.timestamp)
            )
        }
    }
}

@Composable
private fun SalaryDetailsContent(item: ActionHistory) {
    val record = remember(item) {
        try {
            val gson = Gson()
            gson.fromJson(item.entityData, SalaryRecord::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    if (record == null) {
        ErrorDetailsContent()
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Salary Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            DetailRow(
                label = "Period",
                value = "${record.month}/${record.year}"
            )
            DetailRow(
                label = "Amount",
                value = "₹${String.format("%.2f", record.amount)}"
            )
            DetailRow(
                label = "Deleted",
                value = formatDate(item.timestamp)
            )
        }
    }
}

@Composable
private fun GenericDetailsContent(item: ActionHistory) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Item Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        DetailRow(label = "Type", value = item.entityType)
        DetailRow(label = "Deleted", value = formatDate(item.timestamp))
    }
}

@Composable
private fun ErrorDetailsContent() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Unable to load item details",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Preview
@Preview(showBackground = true)
@Composable
private fun DetailRowPreview() {
    PaisaTrackerTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailRow(label = "Name", value = "Monthly Groceries")
            DetailRow(label = "Amount", value = "₹5,000.00")
            DetailRow(label = "Date", value = "May 27, 2026 at 06:52 PM")
        }
    }
}
