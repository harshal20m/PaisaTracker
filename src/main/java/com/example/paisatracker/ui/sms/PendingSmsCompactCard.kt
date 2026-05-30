package com.example.paisatracker.ui.sms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.paisatracker.data.BankNotificationEntity
import com.example.paisatracker.util.CurrentCurrency
import java.time.format.DateTimeFormatter

/**
 * Compact card showing pending SMS transactions on the home screen
 */
@Composable
fun PendingSmsCompactCard(
    viewModel: SmsTransactionViewModel,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingTransactions by viewModel.pendingTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    
    // Show confirmation sheet when user taps a transaction
    var selectedTransaction by remember { mutableStateOf<BankNotificationEntity?>(null) }
    
    AnimatedVisibility(
        visible = pendingTransactions.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Message,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Pending Transactions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "${pendingTransactions.size}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    TextButton(onClick = onViewAll) {
                        Text("View All", style = MaterialTheme.typography.labelMedium)
                    }
                }
                
                // Show first 2 pending transactions
                pendingTransactions.take(2).forEach { transaction ->
                    PendingTransactionCompactItem(
                        transaction = transaction,
                        onConfirm = {
                            selectedTransaction = transaction
                        },
                        onReject = {
                            viewModel.rejectTransaction(transaction.id)
                        }
                    )
                }
                
                if (pendingTransactions.size > 2) {
                    Text(
                        "+${pendingTransactions.size - 2} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
    
    // Show confirmation sheet when transaction is selected
    selectedTransaction?.let { transaction ->
        SmsTransactionConfirmationSheet(
            transaction = transaction,
            viewModel = viewModel,
            onDismiss = { selectedTransaction = null },
            onConfirm = { categoryId, projectId ->
                viewModel.confirmTransaction(
                    notificationId = transaction.id,
                    categoryId = categoryId,
                    projectId = projectId
                )
                selectedTransaction = null
            }
        )
    }
}

@Composable
private fun PendingTransactionCompactItem(
    transaction: BankNotificationEntity,
    onConfirm: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    transaction.merchant ?: transaction.bankName ?: "Transaction",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "₹${String.format("%.2f", transaction.amount ?: 0.0)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    transaction.bankName?.let {
                        Text(
                            "• $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    transaction.postedAt.format(DateTimeFormatter.ofPattern("MMM dd, hh:mm a")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onConfirm,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Confirm",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onReject,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Reject",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// Made with Bob