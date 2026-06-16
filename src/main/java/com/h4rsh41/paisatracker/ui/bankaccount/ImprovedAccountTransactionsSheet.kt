package com.h4rsh41.paisatracker.ui.bankaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.h4rsh41.paisatracker.data.AccountTransaction
import com.h4rsh41.paisatracker.data.BankAccount
import com.h4rsh41.paisatracker.data.TransactionType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Improved Account Transactions Sheet with:
 * - Grid layout for compact display
 * - Smaller fonts
 * - Lazy loading (last 1 month + current month)
 * - Salary/credit history prominently displayed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImprovedAccountTransactionsSheet(
    account: BankAccount,
    viewModel: BankAccountViewModel,
    onDismiss: () -> Unit,
    currencySymbol: String = "₹"
) {
    // Get transactions for last 2 months with lazy loading
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH) + 1
    val currentYear = calendar.get(Calendar.YEAR)
    
    calendar.add(Calendar.MONTH, -1)
    val lastMonth = calendar.get(Calendar.MONTH) + 1
    val lastYear = calendar.get(Calendar.YEAR)
    
    var transactions by remember { mutableStateOf<List<AccountTransaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(account.id) {
        // Load transactions for current and last month
        transactions = viewModel.getTransactionsForLastTwoMonths(
            accountId = account.id,
            currentMonth = currentMonth,
            currentYear = currentYear,
            lastMonth = lastMonth,
            lastYear = lastYear
        )
        isLoading = false
    }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Group transactions by type
    val credits = transactions.filter { 
        it.type in listOf(TransactionType.CREDIT, TransactionType.SALARY, TransactionType.TOPUP, TransactionType.REFUND)
    }
    val debits = transactions.filter { 
        it.type in listOf(TransactionType.DEBIT, TransactionType.EXPENSE)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Compact Header
            CompactTransactionHeader(
                account = account,
                totalCredits = credits.sumOf { it.amount },
                totalDebits = debits.sumOf { it.amount },
                transactionCount = transactions.size,
                currencySymbol = currencySymbol
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (transactions.isEmpty()) {
                EmptyTransactionsCompactState()
            } else {
                // Grid Layout with smaller fonts
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Credits Section Header
                    if (credits.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            SectionHeader(
                                title = "Credits",
                                count = credits.size,
                                icon = Icons.Default.TrendingUp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        
                        items(credits) { transaction ->
                            CompactTransactionCard(
                                transaction = transaction,
                                currencySymbol = currencySymbol,
                                isCredit = true
                            )
                        }
                    }
                    
                    // Debits Section Header
                    if (debits.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionHeader(
                                title = "Debits",
                                count = debits.size,
                                icon = Icons.Default.TrendingDown,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        items(debits) { transaction ->
                            CompactTransactionCard(
                                transaction = transaction,
                                currencySymbol = currencySymbol,
                                isCredit = false
                            )
                        }
                    }
                    
                    // Bottom spacing
                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactTransactionHeader(
    account: BankAccount,
    totalCredits: Double,
    totalDebits: Double,
    transactionCount: Int,
    currencySymbol: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Account Info - Compact
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = account.emoji, fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (!account.bankName.isNullOrBlank()) {
                    Text(
                        text = account.bankName,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Transaction count badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "$transactionCount txns",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Cards - Compact
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompactSummaryCard(
                label = "Credits",
                value = "$currencySymbol${String.format("%.0f", totalCredits)}",
                icon = Icons.Default.Add,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            CompactSummaryCard(
                label = "Debits",
                value = "$currencySymbol${String.format("%.0f", totalDebits)}",
                icon = Icons.Default.Remove,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompactSummaryCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompactTransactionCard(
    transaction: AccountTransaction,
    currencySymbol: String,
    isCredit: Boolean
) {
    val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCredit) 
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Type badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isCredit) 
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                else 
                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            ) {
                Text(
                    text = transaction.type,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
            }
            
            // Amount
            Text(
                text = "$currencySymbol${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCredit) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
            )
            
            // Description
            if (transaction.description.isNotBlank()) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Date and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeFormat.format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyTransactionsCompactState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No Recent Transactions",
            style = MaterialTheme.typography.titleMedium,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Transactions from the last 2 months will appear here",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Made with Bob