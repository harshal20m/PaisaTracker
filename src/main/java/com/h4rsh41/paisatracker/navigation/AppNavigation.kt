package com.h4rsh41.paisatracker.navigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.h4rsh41.paisatracker.PaisaTrackerApplication
import com.h4rsh41.paisatracker.PaisaTrackerViewModel
import com.h4rsh41.paisatracker.ui.analytics.AnalyticsScreen
import com.h4rsh41.paisatracker.ui.bankaccount.BankAccountTransactionScreen
import com.h4rsh41.paisatracker.ui.common.CalendarTransactionView
import com.h4rsh41.paisatracker.ui.details.ProjectDetailsScreen
import com.h4rsh41.paisatracker.ui.details.ProjectInsightsScreen
import com.h4rsh41.paisatracker.ui.expense.ExpenseDetailScreen
import com.h4rsh41.paisatracker.ui.expense.ExpenseListScreen
import com.h4rsh41.paisatracker.ui.budget.BudgetScreen
import com.h4rsh41.paisatracker.ui.finance.FinanceScreen
import com.h4rsh41.paisatracker.ui.main.home.HomeScreen
import com.h4rsh41.paisatracker.ui.main.projects.ProjectListScreen
import com.h4rsh41.paisatracker.ui.management.ManagementScreen
import com.h4rsh41.paisatracker.ui.settings.SettingsScreen
import com.h4rsh41.paisatracker.ui.sms.AutoCreateConfigScreen
import com.h4rsh41.paisatracker.ui.bin.BinScreen
import com.h4rsh41.paisatracker.ui.sms.MerchantRulesScreen
import com.h4rsh41.paisatracker.ui.sms.MerchantRuleViewModel
import com.h4rsh41.paisatracker.ui.sms.PendingSmsTransactionsScreen
import com.h4rsh41.paisatracker.ui.sms.SmsHistoryScanScreen
import com.h4rsh41.paisatracker.ui.sms.SmsSettingsScreen
import com.h4rsh41.paisatracker.ui.trash.TrashScreen
import com.h4rsh41.paisatracker.ui.trash.TrashViewModel
import com.h4rsh41.paisatracker.viewmodel.AnalyticsViewModel
import com.h4rsh41.paisatracker.viewmodel.AnalyticsViewModelFactory

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: PaisaTrackerViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(navController, startDestination = "home", modifier = modifier) {
        composable("home") {
            HomeScreen(viewModel = viewModel, navController = navController)
        }
        composable("projects") {
            ProjectListScreen(viewModel = viewModel, navController = navController)
        }
        composable("calendar") {
            val expenses by viewModel.getAllExpensesWithDetails().collectAsState(initial = emptyList())
            CalendarTransactionView(
                expenses = expenses,
                onTransactionClick = { expenseId ->
                    navController.navigate("expense_details/$expenseId")
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(viewModel = viewModel, navController = navController)
        }
        composable("project_details/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull() ?: 0L
            ProjectDetailsScreen(viewModel = viewModel, projectId = projectId, navController = navController)
        }
        composable("expense_details/{expenseId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("expenseId")?.toLongOrNull() ?: return@composable
            ExpenseDetailScreen(viewModel = viewModel, expenseId = id, navController = navController)
        }
        composable("expense_list/{categoryId}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")?.toLongOrNull() ?: return@composable
            ExpenseListScreen(viewModel = viewModel, categoryId = categoryId, navController = navController)
        }
        composable("project_insights/{projectId}") { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull()
            if (projectId != null) {
                ProjectInsightsScreen(viewModel = viewModel, projectId = projectId, navController = navController)
            }
        }
        composable("budget") {
            BudgetScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenProject = { projectId ->
                    navController.navigate("project_details/$projectId")
                },
                onOpenCategory = { categoryId ->
                    navController.navigate("expense_list/$categoryId")
                },
                currencySymbol = "₹"
            )
        }
        composable("bin") {
            BinScreen(viewModel = viewModel, navController = navController)
        }
        composable("analytics") {
            val analyticsViewModel = viewModel<AnalyticsViewModel>(
                factory = AnalyticsViewModelFactory(
                    (LocalContext.current.applicationContext as PaisaTrackerApplication).repository
                )
            )
            
            // Listen for project status changes and refresh analytics
            val projectStatusChanged by viewModel.projectStatusChanged.collectAsStateWithLifecycle()
            LaunchedEffect(projectStatusChanged) {
                if (projectStatusChanged > 0) {
                    analyticsViewModel.refreshAnalytics()
                }
            }
            
            AnalyticsScreen(
                viewModel = analyticsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onCategoryClick = { category ->
                    // Navigate to expense list for this category
                    navController.navigate("expense_list/${category.categoryId}")
                }
            )
        }
        composable("bank_accounts") {
            FinanceScreen(
                mainViewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenProject = { projectId ->
                    navController.navigate("project_details/$projectId")
                },
                onOpenCategory = { categoryId ->
                    navController.navigate("expense_list/$categoryId")
                },
                onOpenBankAccount = { bankAccountId ->
                    navController.navigate("bank_account_transactions/$bankAccountId")
                },
                currencySymbol = "₹"
            )
        }
        composable("bank_account_transactions/{bankAccountId}") { backStackEntry ->
            val bankAccountId = backStackEntry.arguments?.getString("bankAccountId")?.toLongOrNull() ?: 0L
            BankAccountTransactionScreen(
                bankAccountId = bankAccountId,
                viewModel = viewModel,
                navController = navController
            )
        }
        composable("management") {
            ManagementScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable("pending_sms") { backStackEntry ->
            val context = LocalContext.current
            val application = context.applicationContext as PaisaTrackerApplication
            val smsViewModel = remember(backStackEntry) {
                com.h4rsh41.paisatracker.ui.sms.SmsTransactionViewModel(application, viewModel)
            }
            PendingSmsTransactionsScreen(
                navController = navController,
                viewModel = smsViewModel
            )
        }
        composable("sms_trash") { backStackEntry ->
            val context = LocalContext.current
            val application = context.applicationContext as PaisaTrackerApplication
            val trashViewModel = remember(backStackEntry) {
                TrashViewModel(application)
            }
            TrashScreen(
                viewModel = trashViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("sms_settings") { backStackEntry ->
            val context = LocalContext.current
            val application = context.applicationContext as PaisaTrackerApplication
            val smsViewModel = remember(backStackEntry) {
                com.h4rsh41.paisatracker.ui.sms.SmsTransactionViewModel(application, viewModel)
            }
            SmsSettingsScreen(
                navController = navController,
                viewModel = smsViewModel
            )
        }
        composable("auto_create_config") { backStackEntry ->
            val context = LocalContext.current
            val application = context.applicationContext as PaisaTrackerApplication
            val smsViewModel = remember(backStackEntry) {
                com.h4rsh41.paisatracker.ui.sms.SmsTransactionViewModel(application, viewModel)
            }
            AutoCreateConfigScreen(
                navController = navController,
                smsViewModel = smsViewModel
            )
        }
        composable("merchant_rules") { backStackEntry ->
            val context = LocalContext.current
            val application = context.applicationContext as PaisaTrackerApplication
            val merchantRuleViewModel = remember(backStackEntry) {
                MerchantRuleViewModel(application)
            }
            MerchantRulesScreen(
                navController = navController,
                viewModel = merchantRuleViewModel
            )
        }
        composable("sms_history_scan") { backStackEntry ->
            val context = LocalContext.current
            val application = context.applicationContext as PaisaTrackerApplication
            val smsHistoryScanViewModel = remember(backStackEntry) {
                com.h4rsh41.paisatracker.ui.sms.SmsHistoryScanViewModel(application)
            }
            SmsHistoryScanScreen(
                navController = navController,
                viewModel = smsHistoryScanViewModel
            )
        }
        composable("sms_notification_detail/{notificationId}") { backStackEntry ->
            val notificationId = backStackEntry.arguments?.getString("notificationId")?.toLongOrNull() ?: 0L
            val context = LocalContext.current
            val application = context.applicationContext as PaisaTrackerApplication
            val smsViewModel = remember(backStackEntry) {
                com.h4rsh41.paisatracker.ui.sms.SmsTransactionViewModel(application, viewModel)
            }
            com.h4rsh41.paisatracker.ui.sms.SmsNotificationDetailScreen(
                notificationId = notificationId,
                viewModel = smsViewModel,
                navController = navController
            )
        }
    }
}