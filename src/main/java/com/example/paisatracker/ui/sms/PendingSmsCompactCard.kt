package com.example.paisatracker.ui.sms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.paisatracker.data.BankNotificationEntity
import java.time.format.DateTimeFormatter

@Composable
fun PendingSmsCompactCard(
    viewModel : SmsTransactionViewModel,
    onViewAll : () -> Unit,
    navController: androidx.navigation.NavController,
    modifier  : Modifier = Modifier
) {
    val pendingTransactions by viewModel.pendingTransactions
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedTransaction by remember { mutableStateOf<BankNotificationEntity?>(null) }

    AnimatedVisibility(
        visible  = pendingTransactions.isNotEmpty(),
        enter    = expandVertically() + fadeIn(),
        exit     = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape     = RoundedCornerShape(18.dp),
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border    = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Header
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Message,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(17.dp)
                            )
                        }
                        Column {
                            Text(
                                text       = "Pending from SMS",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text     = "Needs your attention",
                                fontSize = 11.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text       = "${pendingTransactions.size}",
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp
                )

                // Transaction rows
                pendingTransactions.take(2).forEach { txn ->
                    CompactTransactionRow(
                        transaction = txn,
                        onConfirm   = { selectedTransaction = txn },
                        onReject    = { viewModel.rejectTransaction(txn.id) }
                    )
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.5.dp
                    )
                }

                // Overflow label
                if (pendingTransactions.size > 2) {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text     = "+${pendingTransactions.size - 2} more",
                            fontSize = 12.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.5.dp
                    )
                }

                // View All
                TextButton(
                    onClick  = onViewAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text       = "View all pending  →",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    selectedTransaction?.let { txn ->
        SmsTransactionConfirmationSheet(
            transaction = txn,
            viewModel   = viewModel,
            onDismiss   = { selectedTransaction = null },
            onConfirm   = { catId, projId ->
                viewModel.confirmTransaction(
                    notificationId = txn.id,
                    categoryId     = catId,
                    projectId      = projId
                )
                selectedTransaction = null
            },
            navController = navController
        )
    }
}

@Composable
private fun CompactTransactionRow(
    transaction : BankNotificationEntity,
    onConfirm   : () -> Unit,
    onReject    : () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text       = transaction.merchant ?: transaction.bankName ?: "Transaction",
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text       = "₹${String.format("%.2f", transaction.amount ?: 0.0)}",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.error
                )
                transaction.bankName?.let {
                    Text(
                        text     = "· $it",
                        fontSize = 11.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Text(
                text     = transaction.postedAt.format(DateTimeFormatter.ofPattern("MMM dd, hh:mm a")),
                fontSize = 11.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
                onClick  = onConfirm,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Confirm",
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick  = onReject,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Reject",
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}