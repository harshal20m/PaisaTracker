package com.example.paisatracker.ui.finance

import com.example.paisatracker.data.SalaryRecord
import com.example.paisatracker.ui.bankaccount.EditSalarySheet
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paisatracker.PaisaTrackerApplication
import com.example.paisatracker.PaisaTrackerViewModel
import com.example.paisatracker.data.AccountType
import com.example.paisatracker.data.BankAccount
import com.example.paisatracker.data.Budget
import com.example.paisatracker.data.Currency
import com.example.paisatracker.ui.bankaccount.BankAccountViewModel
import com.example.paisatracker.ui.bankaccount.BankAccountViewModelFactory
import com.example.paisatracker.ui.bankaccount.AddBankAccountSheet
import com.example.paisatracker.ui.bankaccount.EditBankAccountSheet
import com.example.paisatracker.ui.bankaccount.AccountTransactionsSheet
import com.example.paisatracker.ui.bankaccount.AddMoneySheet
import com.example.paisatracker.ui.bankaccount.AddSalaryToAccountSheet
import com.example.paisatracker.ui.bankaccount.AddSalarySheet
import com.example.paisatracker.ui.budget.AddBudgetSheet
import com.example.paisatracker.ui.budget.BudgetEmptyState
import com.example.paisatracker.ui.common.DeleteConfirmationSheetContent
import com.example.paisatracker.ui.salary.SalaryViewModel
import com.example.paisatracker.ui.salary.SalaryViewModelFactory
import com.example.paisatracker.util.formatCurrency

/**
 * Unified Finance Screen
 * Combines Bank Accounts and Budgets in a beautiful, aesthetic layout
 * Features:
 * - Tab-based navigation between Accounts and Budgets
 * - Clean Material 3 design
 * - Integrated financial overview
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FinanceScreen(
    mainViewModel: PaisaTrackerViewModel,
    onNavigateBack: () -> Unit,
    onOpenProject: (Long) -> Unit = {},
    onOpenCategory: (Long) -> Unit = {},
    onOpenBankAccount: (Long) -> Unit = {},
    currencySymbol: String = "₹"
) {
    val app = LocalContext.current.applicationContext as PaisaTrackerApplication
    val bankAccountViewModel = viewModel<BankAccountViewModel>(
        factory = BankAccountViewModelFactory(app.repository, mainViewModel)
    )

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Accounts", "Budgets")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Finance",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> AccountsTab(
                    viewModel = bankAccountViewModel,
                    mainViewModel = mainViewModel,
                    onAccountClick = onOpenBankAccount,
                    currencySymbol = currencySymbol
                )
                1 -> BudgetsTab(
                    viewModel = mainViewModel,
                    onOpenProject = onOpenProject,
                    onOpenCategory = onOpenCategory,
                    currencySymbol = currencySymbol
                )
            }
        }
    }
}

/**
 * Accounts Tab - Beautiful bank account management
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountsTab(
    viewModel: BankAccountViewModel,
    mainViewModel: PaisaTrackerViewModel,
    onAccountClick: (Long) -> Unit,
    currencySymbol: String
) {
    val accounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val activeAccountCount by viewModel.activeAccountCount.collectAsStateWithLifecycle()
    val app = LocalContext.current.applicationContext as PaisaTrackerApplication
    val salaryViewModel = viewModel<SalaryViewModel>(
        factory = SalaryViewModelFactory(app.repository, mainViewModel)
    )
    // Multi-salary support
    val currentMonthSalaries by salaryViewModel.currentMonthSalaries.collectAsStateWithLifecycle()
    val totalIncome by salaryViewModel.totalMonthlyIncome.collectAsStateWithLifecycle()
    val totalSpent by salaryViewModel.totalSpentThisMonth.collectAsStateWithLifecycle()
    val remaining by salaryViewModel.remainingBalance.collectAsStateWithLifecycle()
    val spendPercentage by salaryViewModel.spendPercentage.collectAsStateWithLifecycle()
    
    // Legacy support (for backward compatibility)
    val currentSalary by salaryViewModel.currentSalary.collectAsStateWithLifecycle()

    var showAddAccountSheet by remember { mutableStateOf(false) }
    var showAddSalarySheet by remember { mutableStateOf(false) }
    var salaryToEdit by remember { mutableStateOf<com.example.paisatracker.data.SalaryRecord?>(null) }
    var accountToEdit by remember { mutableStateOf<BankAccount?>(null) }
    var accountToDelete by remember { mutableStateOf<BankAccount?>(null) }
    var accountToShowTransactions by remember { mutableStateOf<BankAccount?>(null) }
    var accountToAddMoney by remember { mutableStateOf<BankAccount?>(null) }
    var accountToAddSalary by remember { mutableStateOf<BankAccount?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (accounts.isEmpty()) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "No Accounts Yet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Add your first bank account to start tracking your finances",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { showAddAccountSheet = true },
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Account")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Financial Overview Card
                item {
                    FinancialOverviewCard(
                        totalBalance = totalBalance,
                        accountCount = activeAccountCount,
                        currencySymbol = currencySymbol
                    )
                }
                
                // Salary Management Card (Multi-Salary)
                item {
                    SalaryManagementCard(
                        salaries = currentMonthSalaries,
                        totalIncome = totalIncome,
                        totalSpent = totalSpent,
                        spendPercentage = spendPercentage,
                        onAddSalary = { showAddSalarySheet = true },
                        onEditSalary = { salary -> salaryToEdit = salary },
                        currencySymbol = currencySymbol
                    )
                }

                // Accounts by Type
                val accountsByType = accounts.groupBy { it.accountType }
                
                accountsByType.forEach { (type, accountList) ->
                    item {
                        AccountTypeSection(
                            accountType = type,
                            accounts = accountList,
                            currencySymbol = currencySymbol,
                            currentSalary = currentSalary?.amount ?: 0.0,
                            totalSpent = totalSpent,
                            remaining = remaining,
                            salaryViewModel = salaryViewModel,
                            onAccountClick = { selectedAccount ->
                                accountToShowTransactions = selectedAccount
                            },
                            onAddMoneyClick = { accountToAddMoney = it },
                            onAddSalaryClick = { accountToAddSalary = it },
                            onEditClick = { accountToEdit = it },
                            onDeleteClick = { accountToDelete = it }
                        )
                    }
                }

                // Bottom spacing for FAB and navigation bar
                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }

        // Floating Action Button
        ExtendedFloatingActionButton(
            onClick = { showAddAccountSheet = true },
            icon = { Icon(Icons.Default.Add, "Add Account") },
            text = { Text("Add Account") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 96.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    // Add Account Sheet
    if (showAddAccountSheet) {
        AddBankAccountSheet(
            onDismiss = { showAddAccountSheet = false },
            onConfirm = { name, type, bankName, balance, emoji, color ->
                viewModel.createAccount(
                    name = name,
                    accountType = type,
                    bankName = bankName,
                    accountNumberLast4 = null,
                    initialBalance = balance,
                    emoji = emoji,
                    colorHex = color,
                    onSuccess = { showAddAccountSheet = false }
                )
            }
        )
    }

    // Add Salary Sheet (Multi-Salary with Account Selector)
    if (showAddSalarySheet) {
        AddSalarySheet(
            accounts = accounts,
            onDismiss = { showAddSalarySheet = false },
            onConfirm = { accountId, amount, sourceName, sourceType, note, isRecurring ->
                salaryViewModel.addSalary(
                    amount = amount,
                    linkedAccountId = accountId,
                    sourceName = sourceName,
                    sourceType = sourceType,
                    note = note,
                    isRecurring = isRecurring
                )
                showAddSalarySheet = false
            }
        )
    }

    // Edit Salary Sheet
    salaryToEdit?.let { salary ->
        EditSalarySheet(
            salary = salary,
            accounts = accounts,
            onDismiss = { salaryToEdit = null },
            onConfirm = { accountId, amount, sourceName, sourceType, note, isRecurring ->
                val updatedSalary = salary.copy(
                    linkedAccountId = accountId,
                    amount = amount,
                    sourceName = sourceName,
                    sourceType = sourceType,
                    note = note,
                    isRecurring = isRecurring
                )
                salaryViewModel.updateSalary(updatedSalary)
                salaryToEdit = null
            },
            onDelete = {
                salaryViewModel.deleteSalary(salary)
                salaryToEdit = null
            }
        )
    }

    // Add Money Sheet
    accountToAddMoney?.let { account ->
        AddMoneySheet(
            account = account,
            onDismiss = { accountToAddMoney = null },
            onConfirm = { amount, _ ->
                viewModel.addMoneyToAccount(
                    accountId = account.id,
                    amount = amount,
                    onSuccess = { accountToAddMoney = null }
                )
            }
        )
    }

    accountToAddSalary?.let { account ->
        AddSalaryToAccountSheet(
            account = account,
            onDismiss = { accountToAddSalary = null },
            onConfirm = { amount, note, isRecurring ->
                salaryViewModel.addSalary(
                    amount = amount,
                    linkedAccountId = account.id,
                    sourceName = account.name,
                    sourceType = com.example.paisatracker.data.SalarySourceType.PRIMARY,
                    note = note,
                    isRecurring = isRecurring
                )
                accountToAddSalary = null
            }
        )
    }

    // Edit Account Sheet
    accountToEdit?.let { account ->
        EditBankAccountSheet(
            account = account,
            onDismiss = { accountToEdit = null },
            onConfirm = { updatedAccount ->
                viewModel.updateAccount(
                    account = updatedAccount,
                    onSuccess = { accountToEdit = null }
                )
            }
        )
    }

    // Delete Confirmation
    accountToDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Account?") },
            text = { Text("Are you sure you want to delete '${account.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(account.id) {
                            accountToDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Transaction History Sheet
    accountToShowTransactions?.let { account ->
        AccountTransactionsSheet(
            account = account,
            viewModel = viewModel,
            onDismiss = { accountToShowTransactions = null },
            onTransactionClick = { expense ->
                // Navigate to expense detail
                accountToShowTransactions = null
                // TODO: Add navigation to expense detail if needed
            },
            currencySymbol = currencySymbol
        )
    }
}

/**
 * Budgets Tab - Budget management with salary tracker
 * Reuses the existing BudgetScreen content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetsTab(
    viewModel: PaisaTrackerViewModel,
    onOpenProject: (Long) -> Unit,
    onOpenCategory: (Long) -> Unit,
    currencySymbol: String
) {
    // Simply embed the existing BudgetScreen content without the Scaffold
    val allBudgets by viewModel.budgetsWithSpending.collectAsStateWithLifecycle()
    val activeBudgets = allBudgets.filter { it.budget.isActive }
    val inactiveBudgets = allBudgets.filter { !it.budget.isActive }
    
    var showAddSheet by remember { mutableStateOf(false) }
    var budgetToEdit by remember { mutableStateOf<Budget?>(null) }
    var budgetToDelete by remember { mutableStateOf<Budget?>(null) }
    var selectedBudgetDetail by remember { mutableStateOf<com.example.paisatracker.data.BudgetWithSpending?>(null) }
    var showPaused by remember { mutableStateOf(false) }
    val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                FinanceBudgetHeading(
                    activeCount = activeBudgets.size,
                    inactiveCount = inactiveBudgets.size
                )
            }

            if (activeBudgets.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FinanceBudgetOverviewCard(
                        budgets = activeBudgets,
                        currencySymbol = currencySymbol
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    FinanceBudgetSectionLabel(
                        text = "Active Budgets",
                        count = activeBudgets.size,
                        dotColor = MaterialTheme.colorScheme.primary
                    )
                }

                items(activeBudgets, key = { it.budget.id }) { budgetWithSpending ->
                    FinanceBudgetCard(
                        bws = budgetWithSpending,
                        currencySymbol = currencySymbol,
                        onOpenDetails = { selectedBudgetDetail = budgetWithSpending },
                        onEdit = { budgetToEdit = budgetWithSpending.budget },
                        onDelete = { budgetToDelete = budgetWithSpending.budget },
                        onToggle = {
                            viewModel.toggleBudgetActive(
                                budgetWithSpending.budget.id,
                                !budgetWithSpending.budget.isActive
                            )
                        }
                    )
                }
            }

            if (inactiveBudgets.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FinanceBudgetPausedHeader(
                        count = inactiveBudgets.size,
                        expanded = showPaused,
                        onClick = { showPaused = !showPaused }
                    )
                }
            }

            if (inactiveBudgets.isNotEmpty() && showPaused) {
                items(inactiveBudgets, key = { it.budget.id }) { budgetWithSpending ->
                    FinanceBudgetCard(
                        bws = budgetWithSpending,
                        currencySymbol = currencySymbol,
                        isPaused = true,
                        onOpenDetails = { selectedBudgetDetail = budgetWithSpending },
                        onEdit = { budgetToEdit = budgetWithSpending.budget },
                        onDelete = { budgetToDelete = budgetWithSpending.budget },
                        onToggle = {
                            viewModel.toggleBudgetActive(
                                budgetWithSpending.budget.id,
                                !budgetWithSpending.budget.isActive
                            )
                        }
                    )
                }
            }

            if (allBudgets.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BudgetEmptyState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        onCreateClick = { showAddSheet = true }
                    )
                }
            }

            // Bottom spacing
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(120.dp))
            }
        }

        // Floating Action Button
        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            icon = { Icon(Icons.Default.Add, "Add Budget") },
            text = { Text("Add Budget") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 96.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    // Add Budget Sheet
    if (showAddSheet) {
        AddBudgetSheet(
            viewModel = viewModel,
            onDismiss = { showAddSheet = false },
            currencySymbol = currencySymbol
        )
    }

    // Edit Budget Sheet
    budgetToEdit?.let { budget ->
        AddBudgetSheet(
            viewModel = viewModel,
            onDismiss = { budgetToEdit = null },
            currencySymbol = currencySymbol,
            budgetToEdit = budget
        )
    }

    selectedBudgetDetail?.let { budgetWithSpending ->
        ModalBottomSheet(
            onDismissRequest = { selectedBudgetDetail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            FinanceBudgetDetailSheet(
                bws = budgetWithSpending,
                currencySymbol = currencySymbol,
                onDismiss = { selectedBudgetDetail = null },
                onEdit = {
                    selectedBudgetDetail = null
                    budgetToEdit = budgetWithSpending.budget
                },
                onDelete = {
                    selectedBudgetDetail = null
                    budgetToDelete = budgetWithSpending.budget
                },
                onToggle = {
                    viewModel.toggleBudgetActive(
                        budgetWithSpending.budget.id,
                        !budgetWithSpending.budget.isActive
                    )
                    selectedBudgetDetail = null
                }
            )
        }
    }

    // Delete Confirmation
    budgetToDelete?.let { budget ->
        ModalBottomSheet(
            onDismissRequest = { budgetToDelete = null },
            sheetState = deleteSheetState
        ) {
            DeleteConfirmationSheetContent(
                title = "Delete \"${budget.name}\"?",
                message = "This budget and its history will be permanently removed.",
                onConfirm = {
                    viewModel.deleteBudget(budget)
                    budgetToDelete = null
                },
                onDismiss = { budgetToDelete = null }
            )
        }
    }
}

/**
 * Financial Overview Card - Beautiful summary of total balance
 */
@Composable
private fun FinancialOverviewCard(
    totalBalance: Double,
    accountCount: Int,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Total Balance",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$currencySymbol${String.format("%.2f", totalBalance)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.16f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "Active Accounts",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            accountCount.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.14f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "Status",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            if (accountCount > 0) "Healthy" else "Start Adding",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Account Type Section - Groups accounts by type with beautiful cards
 */
@Composable
private fun AccountTypeSection(
    accountType: String,
    accounts: List<BankAccount>,
    currencySymbol: String,
    currentSalary: Double,
    totalSpent: Double,
    remaining: Double,
    salaryViewModel: SalaryViewModel,
    onAccountClick: (BankAccount) -> Unit,
    onAddMoneyClick: (BankAccount) -> Unit,
    onAddSalaryClick: (BankAccount) -> Unit,
    onEditClick: (BankAccount) -> Unit,
    onDeleteClick: (BankAccount) -> Unit
) {
    val calendar = remember { java.util.Calendar.getInstance() }
    val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
    val currentYear = calendar.get(java.util.Calendar.YEAR)
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when (accountType) {
                                AccountType.BANK -> Icons.Default.AccountBalance
                                AccountType.CASH -> Icons.Default.Money
                                AccountType.CREDIT_CARD -> Icons.Default.CreditCard
                                AccountType.DIGITAL_WALLET -> Icons.Default.Wallet
                                else -> Icons.Default.AccountBalanceWallet
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column {
                    Text(
                        accountType,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${accounts.size} account${if (accounts.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                )
            ) {
                Text(
                    accounts.size.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        accounts.forEach { account ->
            val accountSalaries by salaryViewModel.getSalariesForAccount(account.id)
                .collectAsState(initial = emptyList())
            
            AccountCard(
                account = account,
                currencySymbol = currencySymbol,
                currentSalary = currentSalary,
                totalSpent = totalSpent,
                remaining = remaining,
                accountSalaries = accountSalaries,
                onClick = { onAccountClick(account) },
                onAddMoney = { onAddMoneyClick(account) },
                onAddSalary = { onAddSalaryClick(account) },
                onEdit = { onEditClick(account) },
                onDelete = { onDeleteClick(account) }
            )
        }
    }
}

/**
 * Account Card - Beautiful individual account display
 */
@Composable
private fun AccountCard(
    account: BankAccount,
    currencySymbol: String,
    currentSalary: Double,
    totalSpent: Double,
    remaining: Double,
    accountSalaries: List<SalaryRecord>,
    onClick: () -> Unit,
    onAddMoney: () -> Unit,
    onAddSalary: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        ),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(android.graphics.Color.parseColor(account.colorHex)).copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            account.emoji,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        account.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!account.bankName.isNullOrBlank()) {
                        Text(
                            account.bankName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.14f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "$currencySymbol${String.format("%.2f", account.currentBalance)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (account.currentBalance >= 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Account Type
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.14f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            account.accountType.replace("_", " "),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (account.isActive) {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.14f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.14f)
                    },
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Text(
                        text = if (account.isActive) "Active" else "Inactive",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (account.isActive) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
            
            // Credits/Deposits Section (if any salaries exist for this account)
            if (accountSalaries.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Credits This Month",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$currencySymbol${String.format("%.2f", accountSalaries.sumOf { it.amount })}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Show individual salary sources
                    accountSalaries.forEach { salary ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.12f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = salary.sourceName.ifBlank { "Income" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = salary.sourceType.replace("_", " "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "$currencySymbol${String.format("%.2f", salary.amount)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
            
            // Action buttons row
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddMoney,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.AddCard,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Money", style = MaterialTheme.typography.labelMedium)
                    }
                    
                    OutlinedButton(
                        onClick = onAddSalary,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Salary", style = MaterialTheme.typography.labelMedium)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Edit", style = MaterialTheme.typography.labelMedium)
                    }
                    
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Delete", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceBudgetHeading(
    activeCount: Int,
    inactiveCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Budget Planner",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (activeCount + inactiveCount > 0) {
                        "$activeCount active • $inactiveCount paused"
                    } else {
                        "Track limits across projects and categories"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = (activeCount + inactiveCount).toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    }
}

@Composable
private fun FinanceBudgetOverviewCard(
    budgets: List<com.example.paisatracker.data.BudgetWithSpending>,
    currencySymbol: String
) {
    val totalLimit = budgets.sumOf { it.budget.limitAmount }
    val totalSpent = budgets.sumOf { it.spent }
    val progress = if (totalLimit > 0) {
        (totalSpent / totalLimit).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    val overCount = budgets.count { it.isOverBudget }
    val nearCount = budgets.count { it.isNearLimit }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Budget Usage",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${String.format("%.2f", totalSpent)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "of $currencySymbol${String.format("%.2f", totalLimit)} planned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = when {
                    progress >= 1f -> MaterialTheme.colorScheme.error
                    progress >= 0.8f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.16f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Healthy",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            (budgets.size - nearCount - overCount).coerceAtLeast(0).toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.16f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Attention",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            nearCount.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.16f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Exceeded",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            overCount.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceBudgetSectionLabel(
    text: String,
    count: Int,
    dotColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp, start = 4.dp, end = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun FinanceBudgetPausedHeader(
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.64f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Paused Budgets",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FinanceBudgetCard(
    bws: com.example.paisatracker.data.BudgetWithSpending,
    currencySymbol: String,
    isPaused: Boolean = false,
    onOpenDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    val budget = bws.budget
    val progress by animateFloatAsState(
        targetValue = bws.percentUsed,
        animationSpec = tween(700),
        label = "finance_budget_progress"
    )

    val progressColor = when {
        bws.isOverBudget -> MaterialTheme.colorScheme.error
        bws.isNearLimit -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetails),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaused) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
            }
        ),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = budget.emoji,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budget.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            append(budget.period.displayName)
                            bws.categoryName?.let { append(" • $it") }
                            bws.projectName?.let { append(" • $it") }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Spent",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currencySymbol${String.format("%.2f", bws.spent)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = progressColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Limit",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currencySymbol${String.format("%.2f", budget.limitAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )

                    Text(
                        text = if (bws.isOverBudget) {
                            "Over by $currencySymbol${String.format("%.2f", -bws.remaining)}"
                        } else {
                            "$currencySymbol${String.format("%.2f", bws.remaining)} remaining"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (bws.isOverBudget) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FinanceBudgetActionButton(
                        label = "Edit",
                        icon = Icons.Default.Edit,
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    )
                    FinanceBudgetActionButton(
                        label = "Delete",
                        icon = Icons.Default.DeleteOutline,
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        contentColor = MaterialTheme.colorScheme.error
                    )
                }

                FinanceBudgetActionButton(
                    label = if (isPaused) "Resume Budget" else "Pause Budget",
                    icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    onClick = onToggle,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FinanceBudgetActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FinanceBudgetDetailSheet(
    bws: com.example.paisatracker.data.BudgetWithSpending,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    val budget = bws.budget
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Budget details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = budget.emoji,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = budget.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = buildString {
                                append(budget.period.displayName)
                                bws.categoryName?.let { append(" • $it") }
                                bws.projectName?.let { append(" • $it") }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                FinanceBudgetDetailRow("Spent", "$currencySymbol${String.format("%.2f", bws.spent)}")
                FinanceBudgetDetailRow("Limit", "$currencySymbol${String.format("%.2f", budget.limitAmount)}")
                FinanceBudgetDetailRow(
                    if (bws.isOverBudget) "Over by" else "Remaining",
                    "$currencySymbol${String.format("%.2f", kotlin.math.abs(bws.remaining))}"
                )
                FinanceBudgetDetailRow(
                    "Status",
                    when {
                        bws.isOverBudget -> "Exceeded"
                        bws.isNearLimit -> "Near limit"
                        else -> "Healthy"
                    }
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinanceBudgetActionButton(
                    label = "Edit",
                    icon = Icons.Default.Edit,
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )
                FinanceBudgetActionButton(
                    label = "Delete",
                    icon = Icons.Default.DeleteOutline,
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    contentColor = MaterialTheme.colorScheme.error
                )
            }

            FinanceBudgetActionButton(
                label = if (budget.isActive) "Pause Budget" else "Resume Budget",
                icon = if (budget.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FinanceBudgetDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Made with Bob


/**
 * Salary Management Card - Compact card for managing monthly salary
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalaryManagementCard(
    salaries: List<com.example.paisatracker.data.SalaryRecord>,
    totalIncome: Double,
    totalSpent: Double,
    spendPercentage: Float,
    onAddSalary: () -> Unit,
    onEditSalary: (com.example.paisatracker.data.SalaryRecord) -> Unit,
    currencySymbol: String
) {
    val remaining = totalIncome - totalSpent
    val isOverBudget = remaining < 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Monthly Income",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(onClick = onAddSalary) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Salary",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (salaries.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "No salary added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Button(onClick = onAddSalary) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Salary")
                    }
                }
            } else {
                // Total income display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Total Income",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            "$currencySymbol${String.format("%,.2f", totalIncome)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Edit button for single salary
                        if (salaries.size == 1) {
                            IconButton(
                                onClick = { onEditSalary(salaries.first()) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Salary",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        if (salaries.size > 1) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    "${salaries.size} sources",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Progress bar
                val progressColor = when {
                    spendPercentage < 0.7f -> MaterialTheme.colorScheme.tertiary
                    spendPercentage < 0.9f -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.error
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Spent: $currencySymbol${String.format("%,.2f", totalSpent)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            "${(spendPercentage * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = progressColor
                        )
                    }

                    LinearProgressIndicator(
                        progress = { spendPercentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    Text(
                        "Remaining: $currencySymbol${String.format("%,.2f", remaining)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = if (isOverBudget) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Individual salary items (show if multiple)
                if (salaries.size > 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    Text(
                        "Income Sources",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    salaries.forEach { salary ->
                        SalaryItemRow(
                            salary = salary,
                            currencySymbol = currencySymbol,
                            onEdit = { onEditSalary(salary) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SalaryItemRow(
    salary: com.example.paisatracker.data.SalaryRecord,
    currencySymbol: String,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    com.example.paisatracker.data.SalarySourceType.getEmoji(salary.sourceType),
                    style = MaterialTheme.typography.titleLarge
                )
                Column {
                    Text(
                        salary.sourceName.ifEmpty { com.example.paisatracker.data.SalarySourceType.getDisplayName(salary.sourceType) },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            com.example.paisatracker.data.SalarySourceType.getDisplayName(salary.sourceType),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (salary.isRecurring) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    "Recurring",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            Text(
                "$currencySymbol${String.format("%,.0f", salary.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Legacy SalaryManagementCard for backward compatibility (deprecated)
@Deprecated("Use new multi-salary version")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegacySalaryManagementCard(
    currentSalary: com.example.paisatracker.data.SalaryRecord?,
    totalSpent: Double,
    remaining: Double,
    salaryViewModel: com.example.paisatracker.ui.salary.SalaryViewModel,
    currencySymbol: String
) {
    var showEditSheet by remember { mutableStateOf(false) }
    val activeAccounts by salaryViewModel.activeBankAccounts.collectAsStateWithLifecycle()
    
    val spendPercentage = if (currentSalary != null && currentSalary.amount > 0) {
        (totalSpent / currentSalary.amount).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    
    val isOverBudget = remaining < 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            "Monthly Salary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (currentSalary != null) {
                            Text(
                                "$currencySymbol${String.format("%.0f", currentSalary.amount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                OutlinedButton(
                    onClick = { showEditSheet = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        if (currentSalary != null) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (currentSalary != null) "Edit" else "Setup",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            if (currentSalary != null) {
                // Progress bar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${(spendPercentage * 100).toInt()}% spent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (isOverBudget) {
                            Text(
                                "Over budget!",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    LinearProgressIndicator(
                        progress = { spendPercentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            isOverBudget -> MaterialTheme.colorScheme.error
                            spendPercentage > 0.8f -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.primary
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Spent: $currencySymbol${String.format("%.0f", totalSpent)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Left: $currencySymbol${String.format("%.0f", remaining)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            } else {
                // Empty state
                Text(
                    "Set your monthly salary to track spending and manage your budget effectively.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
    
    // Edit/Add Salary Sheet
    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            com.example.paisatracker.ui.bankaccount.AddSalaryToAccountSheet(
                account = activeAccounts.firstOrNull() ?: com.example.paisatracker.data.BankAccount(
                    id = 0,
                    name = "Default",
                    accountType = com.example.paisatracker.data.AccountType.BANK,
                    currentBalance = 0.0,
                    emoji = "🏦",
                    colorHex = "#2196F3"
                ),
                onDismiss = { showEditSheet = false },
                onConfirm = { amount, note, isRecurring ->
                    if (currentSalary != null) {
                        salaryViewModel.updateSalary(
                            currentSalary.copy(
                                amount = amount,
                                note = note,
                                isRecurring = isRecurring
                            )
                        )
                    } else {
                        salaryViewModel.addSalary(
                            amount = amount,
                            linkedAccountId = activeAccounts.firstOrNull()?.id ?: 0,
                            sourceName = activeAccounts.firstOrNull()?.name ?: "Salary",
                            sourceType = com.example.paisatracker.data.SalarySourceType.PRIMARY,
                            note = note,
                            isRecurring = isRecurring
                        )
                    }
                    showEditSheet = false
                }
            )
        }
    }
}
