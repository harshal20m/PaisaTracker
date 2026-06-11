package com.example.paisatracker.ui.sms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.paisatracker.data.BankNotificationEntity
import com.example.paisatracker.ui.common.ScreenHeader
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsNotificationDetailScreen(
    notificationId: Long,
    viewModel: SmsTransactionViewModel,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var notification by remember { mutableStateOf<BankNotificationEntity?>(null) }
    var showConfirmationSheet by remember { mutableStateOf(false) }
    
    // Load notification details
    LaunchedEffect(notificationId) {
        notification = viewModel.getNotificationById(notificationId)
    }
    
    Scaffold(
        topBar = {
            ScreenHeader(
                title = "Transaction Details",
                subtitle = "Review SMS transaction",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        notification?.let { txn ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Badge
                val isCredit = txn.transactionType?.contains("CREDIT", ignoreCase = true) == true ||
                               txn.transactionType?.contains("INCOME", ignoreCase = true) == true
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCredit) Color(0xFF4CAF50).copy(alpha = 0.15f) 
                            else MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isCredit) "Credit Transaction" else "Debit Transaction",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isCredit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Pending confirmation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "₹${String.format("%.2f", txn.amount ?: 0.0)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCredit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // Parsed Transaction Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Parsed Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp).padding(bottom = 0.dp)
                        )
                        
                        DetailRow(
                            icon = Icons.Default.Store,
                            label = "Merchant",
                            value = txn.merchant ?: "Unknown"
                        )
                        DetailDivider()
                        DetailRow(
                            icon = Icons.Default.AccountBalance,
                            label = "Bank",
                            value = txn.bankName ?: "Unknown"
                        )
                        DetailDivider()
                        DetailRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Date & Time",
                            value = txn.postedAt.format(
                                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                            )
                        )
                        if (txn.accountLast4 != null) {
                            DetailDivider()
                            DetailRow(
                                icon = Icons.Default.CreditCard,
                                label = "Account",
                                value = "••••${txn.accountLast4}"
                            )
                        }
                        if (txn.transactionType != null) {
                            DetailDivider()
                            DetailRow(
                                icon = Icons.Default.SwapHoriz,
                                label = "Type",
                                value = txn.transactionType
                            )
                        }
                    }
                }
                
                // Original SMS Message Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Message,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Original SMS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "From: ${txn.senderAlias}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                                Text(
                                    text = txn.messageBody,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.rejectTransaction(txn.id)
                            navController.navigateUp()
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp, MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Reject",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Button(
                        onClick = { showConfirmationSheet = true },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Confirm",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(Modifier.height(32.dp))
            }
            
            // Confirmation Sheet
            if (showConfirmationSheet) {
                SmsTransactionConfirmationSheet(
                    transaction = txn,
                    viewModel = viewModel,
                    onDismiss = { showConfirmationSheet = false },
                    onConfirm = { catId, projId ->
                        viewModel.confirmTransaction(
                            notificationId = txn.id,
                            categoryId = catId,
                            projectId = projId
                        )
                        showConfirmationSheet = false
                        navController.navigateUp()
                    },
                    navController = navController
                )
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp
    )
}

// Made with Bob