package com.example.paisatracker.ui.sms

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.paisatracker.data.ScanDateRange
import com.example.paisatracker.data.SmsHistoryScanState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * Screen for scanning SMS history for bank transactions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsHistoryScanScreen(
    navController: NavHostController,
    viewModel: SmsHistoryScanViewModel = viewModel()
) {
    val context = LocalContext.current
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val selectedDateRange by viewModel.selectedDateRange.collectAsStateWithLifecycle()
    val customStartDate by viewModel.customStartDate.collectAsStateWithLifecycle()
    val customEndDate by viewModel.customEndDate.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val scanSummary by viewModel.scanSummary.collectAsStateWithLifecycle()
    
    // Check permission on launch
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setPermissionGranted(granted)
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setPermissionGranted(granted)
        if (granted) {
            viewModel.startScan()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan SMS History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (scanState) {
                is SmsHistoryScanState.Idle,
                is SmsHistoryScanState.SelectingDateRange -> {
                    DateRangeSelectionContent(
                        selectedDateRange = selectedDateRange,
                        customStartDate = customStartDate,
                        customEndDate = customEndDate,
                        hasPermission = hasPermission,
                        estimatedCount = viewModel.getEstimatedMessageCount(),
                        onDateRangeSelected = { viewModel.setDateRange(it) },
                        onCustomStartDateSelected = { viewModel.setCustomStartDate(it) },
                        onCustomEndDateSelected = { viewModel.setCustomEndDate(it) },
                        onStartScan = {
                            if (hasPermission) {
                                viewModel.startScan()
                            } else {
                                permissionLauncher.launch(Manifest.permission.READ_SMS)
                            }
                        }
                    )
                }
                
                is SmsHistoryScanState.Scanning -> {
                    ScanningProgressContent(
                        state = scanState as SmsHistoryScanState.Scanning,
                        progress = viewModel.getScanProgress(),
                        onCancel = { viewModel.cancelScan() }
                    )
                }
                
                is SmsHistoryScanState.Completed -> {
                    val completedState = scanState as SmsHistoryScanState.Completed
                    ScanCompletedContent(
                        summary = scanSummary,
                        onDone = { navController.popBackStack() },
                        onScanAgain = { viewModel.resetScan() },
                        scanResults = completedState.scanResults
                    )
                }
                
                is SmsHistoryScanState.Error -> {
                    ScanErrorContent(
                        error = (scanState as SmsHistoryScanState.Error).message,
                        onRetry = { viewModel.resetScan() },
                        onDismiss = { navController.popBackStack() }
                    )
                }
                
                is SmsHistoryScanState.Cancelled -> {
                    ScanCancelledContent(
                        onDismiss = { navController.popBackStack() },
                        onScanAgain = { viewModel.resetScan() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeSelectionContent(
    selectedDateRange: ScanDateRange,
    customStartDate: LocalDate,
    customEndDate: LocalDate,
    hasPermission: Boolean,
    estimatedCount: Int,
    onDateRangeSelected: (ScanDateRange) -> Unit,
    onCustomStartDateSelected: (LocalDate) -> Unit,
    onCustomEndDateSelected: (LocalDate) -> Unit,
    onStartScan: () -> Unit
) {
    // Date picker states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Text(
            text = "📱 Scan Old Messages",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Import your past bank transaction SMS messages to track historical expenses. Choose how far back you want to scan.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Date Range Options
        Text(
            text = "Select Time Period",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        ScanDateRange.entries.forEach { range ->
            DateRangeOption(
                range = range,
                isSelected = selectedDateRange == range,
                onClick = { onDateRangeSelected(range) }
            )
        }
        
        // Custom date picker (shown when CUSTOM is selected)
        AnimatedVisibility(visible = selectedDateRange == ScanDateRange.CUSTOM) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Custom Date Range",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start Date
                        OutlinedCard(
                            modifier = Modifier.weight(1f),
                            onClick = { showStartDatePicker = true }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "From",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = customStartDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // End Date
                        OutlinedCard(
                            modifier = Modifier.weight(1f),
                            onClick = { showEndDatePicker = true }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "To",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = customEndDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Estimated: ~$estimatedCount messages",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Duplicates will be automatically skipped",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Permission warning
        if (!hasPermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "SMS read permission required to scan messages",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // Start Scan Button
        Button(
            onClick = onStartScan,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (hasPermission) "Start Scanning" else "Grant Permission & Scan",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    
    // Date Picker Dialogs
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = customStartDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            onCustomStartDateSelected(selectedDate)
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = customEndDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            onCustomEndDateSelected(selectedDate)
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DateRangeOption(
    range: ScanDateRange,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
            )
        } else null
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
                    text = range.displayName,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                if (range != ScanDateRange.CUSTOM) {
                    Text(
                        text = "${range.getStartDate().format(DateTimeFormatter.ofPattern("dd MMM"))} - Today",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ScanningProgressContent(
    state: SmsHistoryScanState.Scanning,
    progress: Float,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = progress,
        targetValue = progress,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Scanning Messages...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = state.currentSender ?: "Processing...",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Progress Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "${state.processedMessages} of ${state.totalMessages} messages",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Stats Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Found",
                value = state.foundTransactions.toString(),
                icon = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.primary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Created",
                value = state.createdExpenses.toString(),
                icon = Icons.Default.Add,
                color = MaterialTheme.colorScheme.tertiary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Skipped",
                value = state.skippedDuplicates.toString(),
                icon = Icons.Default.Close,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Cancel Button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel Scan")
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScanCompletedContent(
    summary: com.example.paisatracker.data.ScanSummary?,
    onDone: () -> Unit,
    onScanAgain: () -> Unit,
    scanResults: List<com.example.paisatracker.data.SmsScanResult> = emptyList()
) {
    var showDetails by remember { mutableStateOf(false) }
    
    if (showDetails) {
        ScanResultsDetailScreen(
            scanResults = scanResults,
            onBack = { showDetails = false }
        )
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Success Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Scan Complete!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (summary != null) {
            Text(
                text = "${summary.expensesCreated} expenses added to your tracker",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Summary Cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryRow("Messages Scanned", summary.totalMessagesScanned.toString())
                    SummaryRow("Transactions Found", summary.transactionsParsed.toString())
                    SummaryRow("Expenses Created", summary.expensesCreated.toString(), highlight = true)
                    SummaryRow("Duplicates Skipped", summary.duplicatesSkipped.toString())
                    if (summary.failedMessages > 0) {
                        SummaryRow("Failed", summary.failedMessages.toString(), isError = true)
                    }
                    
                    Divider()
                    
                    SummaryRow(
                        "Success Rate",
                        "${summary.successRate.roundToInt()}%",
                        highlight = true
                    )
                    SummaryRow(
                        "Scan Duration",
                        "${(summary.scanDurationMs / 1000.0).roundToInt()}s"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action Buttons
        if (scanResults.isNotEmpty()) {
            OutlinedButton(
                onClick = { showDetails = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Detailed Results (${scanResults.size} SMS)")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Done", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onScanAgain,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Scan Again")
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    highlight: Boolean = false,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = if (highlight) 18.sp else 14.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isError -> MaterialTheme.colorScheme.error
                highlight -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun ScanErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Scan Failed",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try Again")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        TextButton(onClick = onDismiss) {
            Text("Dismiss")
        }
    }
}

@Composable
private fun ScanCancelledContent(
    onDismiss: () -> Unit,
    onScanAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Scan Cancelled",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "The scan was cancelled before completion",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onScanAgain,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start New Scan")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        TextButton(onClick = onDismiss) {
            Text("Go Back")
        }
    }
}
@Composable
private fun ScanResultsDetailScreen(
    scanResults: List<com.example.paisatracker.data.SmsScanResult>,
    onBack: () -> Unit
) {
    var filterType by remember { mutableStateOf("All") }
    
    val filteredResults = remember(scanResults, filterType) {
        when (filterType) {
            "Success" -> scanResults.filter { it.success && !it.isDuplicate }
            "Failed" -> scanResults.filter { !it.success }
            "Duplicate" -> scanResults.filter { it.isDuplicate }
            else -> scanResults
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Scan Results Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredResults.size} of ${scanResults.size} messages",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterType == "All",
                onClick = { filterType = "All" },
                label = { Text("All (${scanResults.size})") }
            )
            FilterChip(
                selected = filterType == "Success",
                onClick = { filterType = "Success" },
                label = { Text("Success (${scanResults.count { it.success && !it.isDuplicate }})") },
                leadingIcon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF4CAF50)
                    )
                }
            )
            FilterChip(
                selected = filterType == "Failed",
                onClick = { filterType = "Failed" },
                label = { Text("Failed (${scanResults.count { !it.success }})") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
            FilterChip(
                selected = filterType == "Duplicate",
                onClick = { filterType = "Duplicate" },
                label = { Text("Duplicate (${scanResults.count { it.isDuplicate }})") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Results List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredResults) { result ->
                SmsResultCard(result)
            }
        }
    }
}

@Composable
private fun SmsResultCard(result: com.example.paisatracker.data.SmsScanResult) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = when {
                result.isDuplicate -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                result.success -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = when {
                            result.isDuplicate -> Icons.Default.Info
                            result.success -> Icons.Default.CheckCircle
                            else -> Icons.Default.Close
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = when {
                            result.isDuplicate -> MaterialTheme.colorScheme.primary
                            result.success -> Color(0xFF4CAF50)
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = result.sender,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(result.timestamp)),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                    result.isDuplicate -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    result.success -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                }
            ) {
                Text(
                    text = when {
                        result.isDuplicate -> "Duplicate"
                        result.success -> "Parsed Successfully"
                        else -> "Failed to Parse"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        result.isDuplicate -> MaterialTheme.colorScheme.primary
                        result.success -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
            
            // Expanded Content
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "SMS Content:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.body,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                if (result.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Error:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.errorMessage,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}


// Made with Bob