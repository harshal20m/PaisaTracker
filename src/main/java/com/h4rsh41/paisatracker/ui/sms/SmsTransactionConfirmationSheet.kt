package com.h4rsh41.paisatracker.ui.sms

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.h4rsh41.paisatracker.data.BankNotificationEntity
import com.h4rsh41.paisatracker.data.Category
import com.h4rsh41.paisatracker.data.Project
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsTransactionConfirmationSheet(
    transaction : BankNotificationEntity,
    viewModel   : SmsTransactionViewModel,
    onDismiss   : () -> Unit,
    onConfirm   : (categoryId: Long?, projectId: Long?) -> Unit,
    navController: androidx.navigation.NavController
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val projects   by viewModel.projects.collectAsStateWithLifecycle()

    var selectedCategory   by remember { mutableStateOf<Category?>(null) }
    var selectedProject    by remember { mutableStateOf<Project?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showProjectPicker  by remember { mutableStateOf(false) }
    var isDetecting        by remember { mutableStateOf(false) }
    var showRulesDisabledDialog by remember { mutableStateOf(false) }
    var showNoRuleFoundDialog by remember { mutableStateOf(false) }
    var noRuleMerchantName by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.colorScheme.surface,
        dragHandle       = {
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text       = "Confirm Transaction",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text     = "Review details before adding to expenses",
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            // Transaction detail card
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SheetDetailRow(
                        icon       = Icons.Default.AttachMoney,
                        label      = "Amount",
                        value      = "₹${String.format("%.2f", transaction.amount ?: 0.0)}",
                        valueColor = MaterialTheme.colorScheme.error
                    )
                    RowDivider()
                    SheetDetailRow(
                        icon  = Icons.Default.Store,
                        label = "Merchant",
                        value = transaction.merchant ?: "Unknown"
                    )
                    RowDivider()
                    SheetDetailRow(
                        icon  = Icons.Default.AccountBalance,
                        label = "Bank",
                        value = transaction.bankName ?: "Unknown"
                    )
                    RowDivider()
                    SheetDetailRow(
                        icon  = Icons.Default.CalendarToday,
                        label = "Date",
                        value = transaction.postedAt.format(
                            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        )
                    )
                    if (transaction.accountLast4 != null) {
                        RowDivider()
                        SheetDetailRow(
                            icon  = Icons.Default.CreditCard,
                            label = "Account",
                            value = "••••${transaction.accountLast4}"
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Category
            PickerSectionLabel("Category")
            Spacer(Modifier.height(8.dp))
            PickerSelectorCard(
                icon     = Icons.Default.Category,
                label    = selectedCategory?.name ?: "Select Category",
                emoji    = selectedCategory?.emoji,
                selected = selectedCategory != null,
                onClick  = { showCategoryPicker = true }
            )
            
            // Recent categories grid
            if (categories.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Recent",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(6.dp))
                
                // Show top 6 recent categories in a grid
                val recentCategories = categories.take(6)
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    recentCategories.chunked(3).forEach { rowCategories ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowCategories.forEach { category ->
                                CategoryPillChip(
                                    emoji = category.emoji,
                                    name = category.name,
                                    isSelected = selectedCategory?.id == category.id,
                                    onClick = { selectedCategory = category },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space if row is not complete
                            repeat(3 - rowCategories.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Project
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PickerSectionLabel("Project")
                Text(
                    text     = "— optional",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(8.dp))
            PickerSelectorCard(
                icon     = Icons.Default.Folder,
                label    = selectedProject?.name ?: "Select Project",
                emoji    = selectedProject?.emoji,
                selected = selectedProject != null,
                onClick  = { showProjectPicker = true }
            )

            Spacer(Modifier.height(24.dp))

            // Actions
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick  = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick  = { onConfirm(selectedCategory?.id, selectedProject?.id) },
                    modifier = Modifier.weight(2f),
                    shape    = RoundedCornerShape(12.dp),
                    enabled  = selectedCategory != null,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onPrimary,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Confirm",
                        color      = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showCategoryPicker) {
        DarkPickerDialog(
            title     = "Select Category",
            items     = categories,
            onDismiss = { showCategoryPicker = false },
            showDetectOption = transaction.merchant != null,
            isDetecting = isDetecting,
            onDetectClick = {
                Log.d("SmsConfirmationSheet", "=== DETECT BUTTON CLICKED ===")
                Log.d("SmsConfirmationSheet", "Transaction merchant: ${transaction.merchant}")
                
                // Check if merchant rules are enabled first
                scope.launch {
                    val rulesEnabled = viewModel.smsPreferences.getUseMerchantRules()
                    Log.d("SmsConfirmationSheet", "Merchant rules enabled: $rulesEnabled")
                    
                    if (!rulesEnabled) {
                        Log.d("SmsConfirmationSheet", "Rules disabled, showing dialog")
                        showRulesDisabledDialog = true
                        return@launch
                    }
                    
                    Log.d("SmsConfirmationSheet", "Setting isDetecting to true")
                    isDetecting = true
                    
                    Log.d("SmsConfirmationSheet", "Coroutine launched, calling detectUsingRules...")
                    val result = viewModel.detectUsingRules(transaction.merchant)
                    Log.d("SmsConfirmationSheet", "Detection result received:")
                    Log.d("SmsConfirmationSheet", "  - Rule matched: ${result.ruleMatched}")
                    Log.d("SmsConfirmationSheet", "  - Category: ${result.category?.name}")
                    Log.d("SmsConfirmationSheet", "  - Project: ${result.project?.name}")
                    
                    isDetecting = false
                    Log.d("SmsConfirmationSheet", "Setting isDetecting to false")
                    
                    if (result.ruleMatched && result.category != null) {
                        Log.d("SmsConfirmationSheet", "✓ Applying detected category and project")
                        selectedCategory = result.category
                        selectedProject = result.project
                        showCategoryPicker = false
                        Log.d("SmsConfirmationSheet", "Category picker closed")
                    } else {
                        Log.d("SmsConfirmationSheet", "✗ No rule matched, showing dialog")
                        noRuleMerchantName = transaction.merchant ?: "Unknown"
                        showNoRuleFoundDialog = true
                    }
                }
            },
            navController = navController,
            itemContent = { category ->
                PickerDialogRow(
                    emoji    = category.emoji,
                    name     = category.name,
                    selected = selectedCategory?.id == category.id,
                    onClick  = { selectedCategory = category; showCategoryPicker = false }
                )
            }
        )
    }

    if (showProjectPicker) {
        DarkPickerDialog(
            title     = "Select Project",
            items     = projects,
            onDismiss = { showProjectPicker = false },
            navController = navController,
            itemContent = { project ->
                PickerDialogRow(
                    emoji    = project.emoji,
                    name     = project.name,
                    selected = selectedProject?.id == project.id,
                    onClick  = { selectedProject = project; showProjectPicker = false }
                )
            }
        )
    }
    
    // Rules Disabled Dialog
    if (showRulesDisabledDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDisabledDialog = false },
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Merchant Rules Disabled",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Merchant rules are currently disabled. Enable them in SMS Settings to automatically detect categories based on merchant names.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("SmsConfirmationSheet", "Go to Settings clicked")
                        showRulesDisabledDialog = false
                        navController.navigate("sms_settings")
                        Log.d("SmsConfirmationSheet", "Navigation to sms_settings triggered")
                        onDismiss()
                    }
                ) {
                    Text("Go to Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRulesDisabledDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // No Rule Found Dialog
    if (showNoRuleFoundDialog) {
        AlertDialog(
            onDismissRequest = { showNoRuleFoundDialog = false },
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "No Rule Found",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "No merchant rule found for:",
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "\"$noRuleMerchantName\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Create a rule in Merchant Rules to automatically categorize transactions from this merchant.",
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("SmsConfirmationSheet", "Create Rule clicked for merchant: $noRuleMerchantName")
                        showNoRuleFoundDialog = false
                        navController.navigate("merchant_rules")
                        Log.d("SmsConfirmationSheet", "Navigation to merchant_rules triggered")
                        onDismiss()
                    }
                ) {
                    Text("Create Rule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoRuleFoundDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SheetDetailRow(
    icon       : ImageVector,
    label      : String,
    value      : String,
    valueColor : androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(17.dp)
            )
            Text(
                text     = label,
                fontSize = 12.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text       = value,
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color      = valueColor
        )
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = 14.dp),
        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp
    )
}

@Composable
private fun PickerSectionLabel(text: String) {
    Text(
        text          = text.uppercase(),
        fontSize      = 11.sp,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        color         = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
private fun PickerSelectorCard(
    icon     : ImageVector,
    label    : String,
    emoji    : String?,
    selected : Boolean,
    onClick  : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (emoji != null) {
            Text(emoji, fontSize = 26.sp)
        } else {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text     = label,
            fontSize = 14.sp,
            color    = if (selected) MaterialTheme.colorScheme.onSurface
                       else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DarkPickerDialog(
    title            : String,
    items            : List<T>,
    onDismiss        : () -> Unit,
    showDetectOption : Boolean = false,
    isDetecting      : Boolean = false,
    onDetectClick    : () -> Unit = {},
    navController    : androidx.navigation.NavController? = null,
    itemContent      : @Composable (T) -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape  = RoundedCornerShape(20.dp),
            color  = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with title and close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = title,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp
                )
                
                // Detect using rules option
                if (showDetectOption) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isDetecting, onClick = onDetectClick)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isDetecting) "Detecting..." else "Detect using rules",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isDetecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.5.dp
                    )
                }
                
                if (items.isEmpty()) {
                    // Empty state - guide user to create project
                    EmptyStateContent(
                        title = title,
                        onDismiss = onDismiss,
                        navController = navController
                    )
                } else {
                    // Grid layout for categories
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items.chunked(2)) { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { item ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        itemContent(item)
                                    }
                                }
                                // Fill remaining space if row is not complete
                                if (rowItems.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateContent(
    title: String,
    onDismiss: () -> Unit,
    navController: androidx.navigation.NavController?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (title.contains("Category", ignoreCase = true)) Icons.Default.Category else Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        
        // Title
        Text(
            text = if (title.contains("Category", ignoreCase = true))
                "No Categories Yet"
            else
                "No Projects Yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Description
        Text(
            text = if (title.contains("Category", ignoreCase = true))
                "Categories are created within Projects. You need to create a Project first, then add Categories to organize your expenses."
            else
                "Projects help you organize your expenses. Create your first project to get started!",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Action button
        Button(
            onClick = {
                onDismiss()
                navController?.navigate("projects") {
                    // Pop up to home to avoid deep navigation stack
                    popUpTo("home") {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Go to Projects",
                fontWeight = FontWeight.Bold
            )
        }
        
        // Helper text
        Text(
            text = "Tip: Tap the + button to create your first project",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun PickerDialogRow(
    emoji    : String,
    name     : String,
    selected : Boolean,
    onClick  : () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 4.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
// Category Pill Chip for quick selection
@Composable
private fun CategoryPillChip(
    emoji: String,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 14.sp
            )
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}