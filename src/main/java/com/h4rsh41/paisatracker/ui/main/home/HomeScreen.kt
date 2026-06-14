package com.h4rsh41.paisatracker.ui.main.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.h4rsh41.paisatracker.PaisaTrackerApplication
import com.h4rsh41.paisatracker.PaisaTrackerViewModel
import com.h4rsh41.paisatracker.ui.assets.AssetsBottomSheet
import com.h4rsh41.paisatracker.ui.export.ExportBottomSheet
import com.h4rsh41.paisatracker.ui.common.HeaderActionButton
import com.h4rsh41.paisatracker.ui.common.ScreenHeader
import com.h4rsh41.paisatracker.ui.common.WeeklyDashboardCalendar
import com.h4rsh41.paisatracker.ui.main.projects.*
import com.h4rsh41.paisatracker.ui.quickadd.QuickAddSheet
import com.h4rsh41.paisatracker.ui.search.SearchBottomSheet
import com.h4rsh41.paisatracker.ui.search.SearchViewModel
import com.h4rsh41.paisatracker.ui.search.SearchViewModelFactory
import com.h4rsh41.paisatracker.ui.settings.UpdateRow
import com.h4rsh41.paisatracker.ui.bin.BinSheetContent
import com.h4rsh41.paisatracker.ui.sms.HistoryScanCard
import com.h4rsh41.paisatracker.ui.sms.PendingSmsCompactCard
import com.h4rsh41.paisatracker.ui.sms.SmsHistoryScanViewModel
import com.h4rsh41.paisatracker.ui.sms.SmsTransactionViewModel
import com.h4rsh41.paisatracker.ui.trash.TrashScreen
import com.h4rsh41.paisatracker.ui.trash.TrashViewModel
import com.h4rsh41.paisatracker.data.Project
import com.h4rsh41.paisatracker.util.CurrentCurrency
import com.h4rsh41.paisatracker.viewmodel.AnalyticsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun HomeScreen(viewModel: PaisaTrackerViewModel, navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as PaisaTrackerApplication
    val scope = rememberCoroutineScope()

    // ── Search ────────────────────────────────────────────────────────────────
    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(application.repository, context)
    )

    // ── SMS Transactions ──────────────────────────────────────────────────────
    val smsViewModel = remember {
        SmsTransactionViewModel(application, viewModel)
    }
    
    // ── SMS History Scan ──────────────────────────────────────────────────────
    val smsHistoryScanViewModel = remember {
        SmsHistoryScanViewModel(application)
    }
    
    // ── SMS Trash ─────────────────────────────────────────────────────────────
    val trashViewModel = remember {
        TrashViewModel(application)
    }
    val trashCount by trashViewModel.trashedTransactions.collectAsStateWithLifecycle(initialValue = emptyList())

    // ── Analytics ─────────────────────────────────────────────────────────────
    val analyticsViewModel = remember { AnalyticsViewModel(application.repository) }
    
    // Listen for project status changes and refresh analytics
    val projectStatusChanged by viewModel.projectStatusChanged.collectAsStateWithLifecycle()
    LaunchedEffect(projectStatusChanged) {
        if (projectStatusChanged > 0) {
            analyticsViewModel.refreshAnalytics()
        }
    }

    // ── Currency ──────────────────────────────────────────────────────────────
    val currency by viewModel.currentCurrency.collectAsStateWithLifecycle()
    LaunchedEffect(currency) { CurrentCurrency.set(currency) }

    // ── Panel toggle state ────────────────────────────────────────────────────
    var recentExpanded by remember { mutableStateOf(false) }

    // ── Totals ────────────────────────────────────────────────────────────────
    val activeProjects by viewModel.getAllProjectsWithTotal().collectAsStateWithLifecycle(initialValue = emptyList())
    val totalSpent = activeProjects.sumOf { it.totalAmount }
    val totalCategories = activeProjects.sumOf { it.categoryCount }
    val totalExpenses = activeProjects.sumOf { it.expenseCount }
    val recentExpenses by viewModel.recentExpenses.collectAsStateWithLifecycle(initialValue = emptyList())
    val updateAvailable by viewModel.updateAvailable.collectAsStateWithLifecycle()

    // ── Sheet state ───────────────────────────────────────────────────────────
    var showAssetsSheet by remember { mutableStateOf(false) }
    var showQuickAdd by remember { mutableStateOf(false) }
    var showBin by remember { mutableStateOf(false) }
    var showSmsTrash by remember { mutableStateOf(false) }
    var showDataManagement by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var showSummarySheet by remember { mutableStateOf(false) }
    val quickAddState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val binSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val smsTrashSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val searchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val summarySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── Sheet for Add Project ────────────────────────────────────────────────
    var showAddProjectSheet by remember { mutableStateOf(false) }
    val addProjectSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showAssetsSheet) {
        AssetsBottomSheet(viewModel = viewModel, onDismiss = { showAssetsSheet = false })
    }

    if (showDataManagement) {
        ExportBottomSheet(viewModel = viewModel, navController = navController, onDismiss = { showDataManagement = false })
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showQuickAdd = true
                    scope.launch { quickAddState.show() }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 96.dp) // Adjusted to be above bottom nav (84dp + 12dp)
                    .size(56.dp)
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = "Quick Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScreenHeader(
                title = "PaisaTracker",
                subtitle = "Expense Tracker Dashboard",
                painter = painterResource(
                    id = com.h4rsh41.paisatracker.R.drawable.expenses_5501391
                ),
                action = {
                    HeaderActionButton(
                        icon = Icons.Default.Add,
                        label = "Add Expense",
                        onClick = {
                            showQuickAdd = true
                            scope.launch { quickAddState.show() }
                        },
                        contentDescription = "Quick Add",
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            )

            LazyColumn(
                state = rememberLazyListState(),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 0.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AnimatedVisibility(
                        visible = updateAvailable != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        updateAvailable?.let { release ->
                            UpdateRow(
                                tagName = release.tag_name,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(release.html_url))
                                    context.startActivity(intent)
                                },
                                onDismiss = { viewModel.dismissUpdate() }
                            )
                        }
                    }
                }
                
                // Warning card when no projects exist
                item {
                    AnimatedVisibility(
                        visible = activeProjects.isEmpty(),
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        NoProjectWarningCard(
                            onClick = {
                                showAddProjectSheet = true
                                scope.launch { addProjectSheetState.show() }
                            }
                        )
                    }
                }

                // Search Card - Opens bottom sheet
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        onClick = {
                            showSearchSheet = true
                            scope.launch { searchSheetState.show() }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Search expenses...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        WeeklyDashboardCalendar(
                            expenses = recentExpenses,
                            onTransactionClick = { navController.navigate("expense_details/$it") }
                        )

                        // Recent Transactions Section - Moved here right after calendar
                        RecentTransactionsSlider(
                            expenses = recentExpenses,
                            onExpenseClick = { navController.navigate("expense_details/${it.id}") },
                            onMoreClick = {
                                recentExpanded = !recentExpanded
                            },
                            showMore = recentExpanded,
                            onLoadMore = { viewModel.loadMoreRecentExpenses() }
                        )

                        // Only show SMS cards if at least one project exists
                        if (activeProjects.isNotEmpty()) {
                            // SMS History Scan Card
                            HistoryScanCard(
                                viewModel = smsHistoryScanViewModel,
                                onScanClick = { navController.navigate("sms_history_scan") }
                            )
                            
                            // Pending SMS Transactions Card
                            PendingSmsCompactCard(
                                viewModel = smsViewModel,
                                onViewAll = { navController.navigate("pending_sms") },
                                navController = navController
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (activeProjects.isEmpty()) {
                            DummyCreateProjectCard(onClick = {
                                showAddProjectSheet = true
                                scope.launch { addProjectSheetState.show() }
                            })
                        }

                        HomeActionGrid(
                            onSummaryClick = {
                                showSummarySheet = true
                                scope.launch { summarySheetState.show() }
                            },
                            onAssetsClick = { showAssetsSheet = true },
                            onBinClick = {
                                showBin = true
                                scope.launch { binSheetState.show() }
                            },
                            onSmsTrashClick = {
                                showSmsTrash = true
                                scope.launch { smsTrashSheetState.show() }
                            },
                            smsTrashCount = trashCount.size
                        )

                        // Analytics Preview Card
                        Spacer(modifier = Modifier.height(8.dp))
                        AnalyticsPreviewCard(
                            viewModel = analyticsViewModel,
                            onViewFullAnalytics = { navController.navigate("analytics") },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAddProjectSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddProjectSheet = false },
            sheetState = addProjectSheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            AddProjectSheetContent(
                viewModel = viewModel,
                onCancel = { showAddProjectSheet = false },
                onConfirm = { name, emoji, includeInSalary ->
                    if (name.isNotBlank()) {
                        viewModel.insertProject(Project(name = name, emoji = emoji, includeInSalary = includeInSalary))
                        scope.launch { addProjectSheetState.hide() }.invokeOnCompletion {
                            showAddProjectSheet = false
                        }
                    }
                }
            )
        }
    }

    if (showQuickAdd) {
        ModalBottomSheet(
            onDismissRequest = { showQuickAdd = false },
            sheetState = quickAddState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            QuickAddSheet(
                onDismiss = {
                    scope.launch { quickAddState.hide() }.invokeOnCompletion { showQuickAdd = false }
                },
                viewModel = viewModel,
                currencySymbol = currency.symbol
            )
        }
    }

    if (showBin) {
        ModalBottomSheet(
            onDismissRequest = { showBin = false },
            sheetState = binSheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight()
        ) {
            BinSheetContent(
                viewModel = viewModel,
                onDismiss = {
                    scope.launch { binSheetState.hide() }.invokeOnCompletion { showBin = false }
                }
            )
        }
    }
    
    if (showSmsTrash) {
        ModalBottomSheet(
            onDismissRequest = { showSmsTrash = false },
            sheetState = smsTrashSheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight()
        ) {
            TrashScreen(
                viewModel = trashViewModel,
                onNavigateBack = {
                    scope.launch { smsTrashSheetState.hide() }.invokeOnCompletion { showSmsTrash = false }
                }
            )
        }
    }

    if (showSearchSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSearchSheet = false },
            sheetState = searchSheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            SearchBottomSheet(
                searchViewModel = searchViewModel,
                onDismiss = {
                    scope.launch { searchSheetState.hide() }.invokeOnCompletion { showSearchSheet = false }
                },
                onExpenseClick = { expense ->
                    navController.navigate("expense_details/${expense.id}")
                    scope.launch { searchSheetState.hide() }.invokeOnCompletion { showSearchSheet = false }
                }
            )
        }
    }

    if (showSummarySheet) {
        ModalBottomSheet(
            onDismissRequest = { showSummarySheet = false },
            sheetState = summarySheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            SummaryBottomSheet(
                analyticsViewModel = analyticsViewModel,
                totalSpent = totalSpent,
                totalProjects = activeProjects.size,
                totalCategories = totalCategories,
                totalExpenses = totalExpenses,
                onDismiss = {
                    scope.launch { summarySheetState.hide() }.invokeOnCompletion { showSummarySheet = false }
                }
            )
        }
    }
}


@Composable
private fun HomeActionGrid(
    onSummaryClick: () -> Unit,
    onAssetsClick: () -> Unit,
    onBinClick: () -> Unit,
    onSmsTrashClick: () -> Unit,
    smsTrashCount: Int = 0
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // First Row: Summary and Gallery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Summary
                OutlinedCard(
                    onClick = onSummaryClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(116.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📊", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Summary",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Gallery
                OutlinedCard(
                    onClick = onAssetsClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(116.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Collections,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Gallery",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Second Row: Bin and SMS Trash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bin
                OutlinedCard(
                    onClick = onBinClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(53.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Text(
                            "Bin",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // SMS Trash
                OutlinedCard(
                    onClick = onSmsTrashClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(53.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.RestoreFromTrash,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                            // Badge
                            if (smsTrashCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (smsTrashCount > 9) "9+" else smsTrashCount.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onError,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            "SMS Trash",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun NoProjectWarningCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Warning Icon
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Text Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "No Projects Created",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Create your first project to start tracking expenses and enable SMS transaction detection",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
            
            // Action Button
            FilledTonalButton(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Create",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
