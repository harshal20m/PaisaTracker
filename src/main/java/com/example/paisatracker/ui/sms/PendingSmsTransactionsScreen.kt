package com.example.paisatracker.ui.sms

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.paisatracker.data.BankNotificationEntity
import com.example.paisatracker.data.Category
import com.example.paisatracker.data.Project
import com.example.paisatracker.ui.components.EmptyState
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// Accent gradients per card slot — purely decorative, not from theme
private val cardAccents = listOf(
    listOf(Color(0xFFFFB347), Color(0xFFFF5C6C)),
    listOf(Color(0xFF7C6EF0), Color(0xFFD946A8)),
    listOf(Color(0xFF00B4D8), Color(0xFF0077B6)),
    listOf(Color(0xFF00E5C3), Color(0xFF0EA5E9)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingSmsTransactionsScreen(
    navController: NavHostController,
    viewModel: SmsTransactionViewModel = viewModel()
) {
    val pendingTransactions by viewModel.pendingTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoading           by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage        by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage      by viewModel.successMessage.collectAsStateWithLifecycle()

    var selectedTransaction   by remember { mutableStateOf<BankNotificationEntity?>(null) }
    var showConfirmationSheet by remember { mutableStateOf(false) }
    
    // Bulk selection state
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedTransactionIds by remember { mutableStateOf(setOf<Long>()) }
    var showBulkConfirmSheet by remember { mutableStateOf(false) }
    
    val snackbarHostState     = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearErrorMessage()
        }
    }
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost   = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PendingScreenHeader(
                count       = pendingTransactions.size,
                onBackClick = {
                    if (isSelectionMode) {
                        isSelectionMode = false
                        selectedTransactionIds = setOf()
                    } else {
                        navController.popBackStack()
                    }
                },
                isSelectionMode = isSelectionMode,
                selectedCount = selectedTransactionIds.size,
                onToggleSelectionMode = {
                    isSelectionMode = !isSelectionMode
                    if (!isSelectionMode) {
                        selectedTransactionIds = setOf()
                    }
                },
                onSelectAll = {
                    selectedTransactionIds = pendingTransactions.map { it.id }.toSet()
                },
                onDeselectAll = {
                    selectedTransactionIds = setOf()
                },
                onBulkConfirm = {
                    if (selectedTransactionIds.isNotEmpty()) {
                        showBulkConfirmSheet = true
                    }
                }
            )

            when {
                isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                pendingTransactions.isEmpty() -> EmptyState(
                    icon        = Icons.Default.CheckCircle,
                    title       = "All Caught Up!",
                    description = "No pending SMS transactions to review",
                    modifier    = Modifier.fillMaxSize()
                )
                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pendingTransactions, key = { it.id }) { txn ->
                        PendingTransactionCard(
                            transaction  = txn,
                            accentColors = cardAccents[pendingTransactions.indexOf(txn) % cardAccents.size],
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedTransactionIds.contains(txn.id),
                            onSelectionToggle = {
                                selectedTransactionIds = if (selectedTransactionIds.contains(txn.id)) {
                                    selectedTransactionIds - txn.id
                                } else {
                                    selectedTransactionIds + txn.id
                                }
                            },
                            onConfirm    = { selectedTransaction = txn; showConfirmationSheet = true },
                            onReject     = { viewModel.rejectTransaction(txn.id) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showConfirmationSheet && selectedTransaction != null) {
        SmsTransactionConfirmationSheet(
            transaction = selectedTransaction!!,
            viewModel   = viewModel,
            onDismiss   = { showConfirmationSheet = false; selectedTransaction = null },
            onConfirm   = { catId, projId ->
                viewModel.confirmTransaction(
                    notificationId = selectedTransaction!!.id,
                    categoryId     = catId,
                    projectId      = projId
                )
                showConfirmationSheet = false
                selectedTransaction   = null
            },
            navController = navController
        )
    }
    
    // Bulk confirmation sheet
    if (showBulkConfirmSheet) {
        BulkConfirmationSheet(
            transactionCount = selectedTransactionIds.size,
            viewModel = viewModel,
            onDismiss = { showBulkConfirmSheet = false },
            onConfirm = { categoryId, projectId ->
                viewModel.bulkConfirmTransactions(
                    notificationIds = selectedTransactionIds.toList(),
                    categoryId = categoryId,
                    projectId = projectId
                )
                showBulkConfirmSheet = false
                isSelectionMode = false
                selectedTransactionIds = setOf()
            },
            navController = navController
        )
    }
}

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun PendingScreenHeader(
    count: Int,
    onBackClick: () -> Unit,
    isSelectionMode: Boolean,
    selectedCount: Int,
    onToggleSelectionMode: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onBulkConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick  = onBackClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    if (isSelectionMode) Icons.Default.Close else Icons.Default.ArrowBack,
                    contentDescription = if (isSelectionMode) "Cancel" else "Back",
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.dp)
                )
            }
            
            if (!isSelectionMode) {
                IconButton(
                    onClick  = onToggleSelectionMode,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Select Multiple",
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isSelectionMode) {
            Text(
                text          = "SELECT TRANSACTIONS",
                fontSize      = 11.sp,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 1.6.sp,
                color         = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))
            
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text       = "$selectedCount Selected",
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 34.sp
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Select All / Deselect All button
                    OutlinedButton(
                        onClick = if (selectedCount == count) onDeselectAll else onSelectAll,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            if (selectedCount == count) Icons.Default.Clear else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (selectedCount == count) "Clear" else "All",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Confirm button
                    if (selectedCount > 0) {
                        Button(
                            onClick = onBulkConfirm,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Confirm", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(4.dp))
            Text(
                text     = "Select transactions to confirm together",
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text          = "REVIEW REQUIRED",
                fontSize      = 11.sp,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 1.6.sp,
                color         = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Pending\nTransactions",
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 34.sp
                )
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text       = "$count new",
                        modifier   = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text     = "$count transaction${if (count != 1) "s" else ""} awaiting your review",
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Transaction Card ──────────────────────────────────────────────────────────
@Composable
fun PendingTransactionCard(
    transaction  : BankNotificationEntity,
    accentColors : List<Color>,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectionToggle: () -> Unit = {},
    onConfirm    : () -> Unit,
    onReject     : () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelectionMode) {
                    Modifier.clickable(onClick = onSelectionToggle)
                } else {
                    Modifier
                }
            ),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Gradient accent bar — decorative only, fine to hardcode
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Brush.horizontalGradient(accentColors))
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Selection mode indicator
            if (isSelectionMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = 2.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            
            // Bank + amount row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text       = transaction.bankName ?: "Bank",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text     = transaction.postedAt.format(
                                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                            ),
                            fontSize = 11.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                // Amount pill
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text          = "DEBIT",
                        fontSize      = 9.sp,
                        fontWeight    = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color         = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text       = "₹${String.format("%.2f", transaction.amount ?: 0.0)}",
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Merchant chip
            if (transaction.merchant != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(16.dp)
                    )
                    Text(
                        text       = transaction.merchant!!,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            // Meta tags
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (transaction.accountLast4 != null) MetaTag("••••${transaction.accountLast4}")
                MetaTag("UPI")
            }

            // Action buttons (only show in normal mode)
            if (!isSelectionMode) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick  = onReject,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Reject", fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick  = onConfirm,
                        modifier = Modifier.weight(2f),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onPrimary,
                            modifier           = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Confirm", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaTag(text: String) {
    Surface(
        shape  = RoundedCornerShape(100.dp),
        color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text     = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            fontSize = 11.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Bulk Confirmation Sheet ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkConfirmationSheet(
    transactionCount: Int,
    viewModel: SmsTransactionViewModel,
    onDismiss: () -> Unit,
    onConfirm: (categoryId: Long?, projectId: Long?) -> Unit,
    navController: androidx.navigation.NavController
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showProjectPicker by remember { mutableStateOf(false) }
    var isDetecting by remember { mutableStateOf(false) }
    var showRulesDisabledDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
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
                text = "Bulk Confirm Transactions",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Apply category and project to $transactionCount transaction${if (transactionCount != 1) "s" else ""}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "All selected transactions will be confirmed with the same category and project",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Category
            BulkPickerSectionLabel("Category")
            Spacer(Modifier.height(8.dp))
            BulkPickerSelectorCard(
                icon = Icons.Default.Category,
                label = selectedCategory?.name ?: "Select Category",
                emoji = selectedCategory?.emoji,
                selected = selectedCategory != null,
                onClick = { showCategoryPicker = true }
            )

            Spacer(Modifier.height(14.dp))

            // Project
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BulkPickerSectionLabel("Project")
                Text(
                    text = "— optional",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(8.dp))
            BulkPickerSelectorCard(
                icon = Icons.Default.Folder,
                label = selectedProject?.name ?: "Select Project",
                emoji = selectedProject?.emoji,
                selected = selectedProject != null,
                onClick = { showProjectPicker = true }
            )

            Spacer(Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onConfirm(selectedCategory?.id, selectedProject?.id) },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedCategory != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Confirm All",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Category picker with detect option
    if (showCategoryPicker) {
        BulkPickerDialog(
            title = "Select Category",
            items = categories,
            onDismiss = { showCategoryPicker = false },
            showDetectOption = false, // No merchant context in bulk mode
            isDetecting = isDetecting,
            onDetectClick = {
                // Not applicable for bulk - would need to check all merchants
                scope.launch {
                    val rulesEnabled = viewModel.smsPreferences.getUseMerchantRules()
                    if (!rulesEnabled) {
                        showRulesDisabledDialog = true
                    }
                }
            },
            itemContent = { category ->
                BulkPickerDialogRow(
                    emoji = category.emoji,
                    name = category.name,
                    selected = selectedCategory?.id == category.id,
                    onClick = { selectedCategory = category; showCategoryPicker = false }
                )
            }
        )
    }

    if (showProjectPicker) {
        BulkPickerDialog(
            title = "Select Project",
            items = projects,
            onDismiss = { showProjectPicker = false },
            itemContent = { project ->
                BulkPickerDialogRow(
                    emoji = project.emoji,
                    name = project.name,
                    selected = selectedProject?.id == project.id,
                    onClick = { selectedProject = project; showProjectPicker = false }
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
                    text = "Merchant rules are currently disabled. Enable them in SMS Settings to automatically detect categories.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRulesDisabledDialog = false
                        navController.navigate("sms_settings")
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
}

@Composable
private fun BulkPickerSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
private fun BulkPickerSelectorCard(
    icon: ImageVector,
    label: String,
    emoji: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (selected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> BulkPickerDialog(
    title: String,
    items: List<T>,
    onDismiss: () -> Unit,
    showDetectOption: Boolean = false,
    isDetecting: Boolean = false,
    onDetectClick: () -> Unit = {},
    itemContent: @Composable (T) -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp
                )

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(items) { item -> itemContent(item) }
                }
            }
        }
    }
}

@Composable
private fun BulkPickerDialogRow(
    emoji: String,
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else androidx.compose.ui.graphics.Color.Transparent
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 24.sp)
        Text(
            text = name,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}