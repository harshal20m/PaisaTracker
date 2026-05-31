package com.example.paisatracker.ui.sms

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.paisatracker.data.BankNotificationEntity
import com.example.paisatracker.ui.components.EmptyState
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
                onBackClick = { navController.popBackStack() }
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
            }
        )
    }
}

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun PendingScreenHeader(count: Int, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        IconButton(
            onClick  = onBackClick,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

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

// ── Transaction Card ──────────────────────────────────────────────────────────
@Composable
fun PendingTransactionCard(
    transaction  : BankNotificationEntity,
    accentColors : List<Color>,
    onConfirm    : () -> Unit,
    onReject     : () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(14.dp))

            // Action buttons
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