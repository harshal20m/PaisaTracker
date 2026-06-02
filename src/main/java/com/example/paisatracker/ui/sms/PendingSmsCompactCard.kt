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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.paisatracker.data.BankNotificationEntity
import java.time.format.DateTimeFormatter

@Composable
fun PendingSmsCompactCard(
    viewModel     : SmsTransactionViewModel,
    onViewAll     : () -> Unit,
    navController : androidx.navigation.NavController,
    modifier      : Modifier = Modifier
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
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape     = RoundedCornerShape(16.dp),
            // Use surface — always solid, always readable on any theme
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // ── Coloured top stripe ───────────────────────────────────────
                // Gives the card a strong visual identity without relying on
                // a background colour that might clash with the theme
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Message,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text       = "Pending from SMS",
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text     = "Tap ✓ to confirm or ✕ to dismiss",
                                    fontSize = 11.sp,
                                    color    = MaterialTheme.colorScheme.onPrimaryContainer
                                        .copy(alpha = 0.65f)
                                )
                            }
                        }

                        // Count badge
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
                }

                // ── Transaction rows (max 2) ───────────────────────────────────
                pendingTransactions.take(2).forEachIndexed { index, txn ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(horizontal = 16.dp),
                            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                    }
                    CompactTransactionRow(
                        transaction = txn,
                        onConfirm   = { selectedTransaction = txn },
                        onReject    = { viewModel.rejectTransaction(txn.id) }
                    )
                }

                // ── Overflow indicator ────────────────────────────────────────
                if (pendingTransactions.size > 2) {
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 16.dp),
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text     = "+${pendingTransactions.size - 2} more transaction${if (pendingTransactions.size - 2 != 1) "s" else ""}",
                            fontSize = 12.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // ── View All button ───────────────────────────────────────────
                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
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

    // ── Confirmation sheet ────────────────────────────────────────────────────
    selectedTransaction?.let { txn ->
        SmsTransactionConfirmationSheet(
            transaction   = txn,
            viewModel     = viewModel,
            onDismiss     = { selectedTransaction = null },
            onConfirm     = { catId, projId ->
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

// ── Single compact row ────────────────────────────────────────────────────────
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
        // Left: merchant + amount + time
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text       = transaction.merchant ?: transaction.bankName ?: "Transaction",
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Detect if credit or debit
                val isCredit = transaction.transactionType?.contains("CREDIT", ignoreCase = true) == true ||
                               transaction.transactionType?.contains("INCOME", ignoreCase = true) == true
                
                // Amount chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isCredit) Color(0xFF4CAF50).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text       = "₹${String.format("%.2f", transaction.amount ?: 0.0)}",
                        modifier   = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isCredit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
                transaction.bankName?.let {
                    Text(
                        text     = it,
                        fontSize = 11.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Text(
                text     = transaction.postedAt.format(DateTimeFormatter.ofPattern("MMM dd, hh:mm a")),
                fontSize = 11.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        Spacer(Modifier.width(8.dp))

        // Right: action buttons
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Confirm
            FilledIconButton(
                onClick  = onConfirm,
                modifier = Modifier.size(36.dp),
                colors   = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor   = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Confirm",
                    modifier           = Modifier.size(20.dp)
                )
            }
            // Reject
            FilledIconButton(
                onClick  = onReject,
                modifier = Modifier.size(36.dp),
                colors   = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor   = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Reject",
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
}