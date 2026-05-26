package com.example.paisatracker.ui.bankaccount

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.paisatracker.data.AccountType
import com.example.paisatracker.data.BankAccount
import com.example.paisatracker.util.formatCurrency

/**
 * Bank Account Management Screen
 * Features:
 * - Account list with balance display
 * - Total balance overview
 * - Add/Edit/Delete accounts
 * - Filter by account type
 * - Search functionality
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BankAccountScreen(
    viewModel: BankAccountViewModel,
    onNavigateBack: () -> Unit,
    onAccountClick: (Long) -> Unit = {}
) {
    val accounts by viewModel.filteredAccounts.collectAsStateWithLifecycle()
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val activeAccountCount by viewModel.activeAccountCount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedAccountType.collectAsStateWithLifecycle()

    var showAddAccountSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<BankAccount?>(null) }
    var accountToDelete by remember { mutableStateOf<BankAccount?>(null) }
    var accountToAddMoney by remember { mutableStateOf<BankAccount?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Accounts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Search toggle
                    IconButton(onClick = { /* Toggle search */ }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                    // Filter
                    IconButton(onClick = { showFilterSheet = true }) {
                        Badge(
                            containerColor = if (selectedType != null) MaterialTheme.colorScheme.primary 
                                else Color.Transparent
                        ) {
                            Icon(Icons.Default.FilterList, "Filter")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddAccountSheet = true },
                icon = { Icon(Icons.Default.Add, "Add Account") },
                text = { Text("Add Account") },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Balance Overview Card
            BalanceOverviewCard(
                totalBalance = totalBalance,
                accountCount = activeAccountCount,
                modifier = Modifier.padding(16.dp)
            )

            // Account Type Filter Chips
            if (selectedType != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.clearFilters() },
                        label = { Text(selectedType ?: "") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear filter",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            // Account List
            if (accounts.isEmpty()) {
                EmptyAccountsState(
                    onAddAccount = { showAddAccountSheet = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(accounts, key = { it.id }) { account ->
                        BankAccountCard(
                            account = account,
                            onClick = { onAccountClick(account.id) },
                            onAddMoney = { accountToAddMoney = account },
                            onEdit = { accountToEdit = account },
                            onDelete = { accountToDelete = account },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
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
                    initialBalance = balance,
                    emoji = emoji,
                    colorHex = color,
                    onSuccess = { showAddAccountSheet = false }
                )
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

    // Filter Sheet
    if (showFilterSheet) {
        AccountTypeFilterSheet(
            selectedType = selectedType,
            onDismiss = { showFilterSheet = false },
            onSelectType = { type ->
                viewModel.setSelectedAccountType(type)
                showFilterSheet = false
            }
        )
    }
}

@Composable
private fun BalanceOverviewCard(
    totalBalance: Double,
    accountCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(totalBalance),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                Text(
                    text = "$accountCount Active Account${if (accountCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankAccountCard(
    account: BankAccount,
    onClick: () -> Unit,
    onAddMoney: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Account Icon
                Surface(
                    shape = CircleShape,
                    color = Color(android.graphics.Color.parseColor(account.colorHex)).copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = account.emoji,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
                
                // Account Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (account.bankName != null) {
                        Text(
                            text = account.bankName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(account.currentBalance),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (account.currentBalance >= 0) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // More Options
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "More options")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add Money") },
                        onClick = {
                            showMenu = false
                            onAddMoney()
                        },
                        leadingIcon = { Icon(Icons.Default.AddCard, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
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
}

@Composable
private fun EmptyAccountsState(
    onAddAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.AccountBalance,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Bank Accounts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your first bank account to start tracking your finances",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddAccount,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Account")
        }
    }
}

// Made with Bob
