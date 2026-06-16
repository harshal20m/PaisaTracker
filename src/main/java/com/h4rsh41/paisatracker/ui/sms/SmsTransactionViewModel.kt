package com.h4rsh41.paisatracker.ui.sms

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.h4rsh41.paisatracker.data.BankNotificationEntity
import com.h4rsh41.paisatracker.data.BankNotificationRepository
import com.h4rsh41.paisatracker.data.Category
import com.h4rsh41.paisatracker.data.CategoryDao
import com.h4rsh41.paisatracker.data.PaisaTrackerDatabase
import com.h4rsh41.paisatracker.data.Project
import com.h4rsh41.paisatracker.data.ProjectDao
import com.h4rsh41.paisatracker.data.SmsPreferences
import com.h4rsh41.paisatracker.manager.SmsTransactionProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Result of detecting category/project using merchant rules
 */
data class RuleDetectionResult(
    val category: Category? = null,
    val project: Project? = null,
    val ruleMatched: Boolean = false
)

class SmsTransactionViewModel(
    application: Application,
    private val globalViewModel: com.h4rsh41.paisatracker.PaisaTrackerViewModel
) : AndroidViewModel(application) {
    private val database = PaisaTrackerDatabase.getDatabase(application)
    private val bankNotificationRepository = BankNotificationRepository(database.bankNotificationDao())
    private val categoryDao: CategoryDao = database.categoryDao()
    private val projectDao: ProjectDao = database.projectDao()
    val smsPreferences = SmsPreferences(application)
    private val merchantRuleRepository = com.h4rsh41.paisatracker.data.MerchantRuleRepository(
        database.merchantRuleDao(),
        application.applicationContext
    )
    private val repository = com.h4rsh41.paisatracker.data.PaisaTrackerRepository(
        projectDao = database.projectDao(),
        categoryDao = database.categoryDao(),
        expenseDao = database.expenseDao(),
        assetDao = database.assetDao(),
        backupDao = database.backupDao(),
        budgetDao = database.budgetDao(),
        flapDao = database.flapDao(),
        salaryRecordDao = database.salaryRecordDao(),
        actionHistoryDao = database.actionHistoryDao(),
        bankAccountDao = database.bankAccountDao(),
        bankNotificationDao = database.bankNotificationDao(),
        accountTransactionDao = database.accountTransactionDao()
    )
    
    private val smsTransactionProcessor = SmsTransactionProcessor(
        context = application,
        expenseDao = database.expenseDao(),
        categoryDao = categoryDao,
        bankNotificationRepository = bankNotificationRepository,
        unrecognizedSmsRepository = com.h4rsh41.paisatracker.data.UnrecognizedSmsRepository(database.unrecognizedSmsDao()),
        smsPreferences = smsPreferences,
        merchantRuleRepository = merchantRuleRepository,
        repository = repository
    )

    // Pending transactions
    val pendingTransactions = bankNotificationRepository.getPendingTransactions()
    val pendingCount = bankNotificationRepository.getPendingCount()

    // SMS preferences
    val autoCreateExpenses = smsPreferences.autoCreateExpensesFlow
    val showNotifications = smsPreferences.showNotificationsFlow
    val vibrateOnDetection = smsPreferences.vibrateOnDetectionFlow

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Categories and projects for selection
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    init {
        loadCategoriesAndProjects()
    }

    private fun loadCategoriesAndProjects() {
        viewModelScope.launch {
            try {
                // Load categories sorted by recent usage, excluding completed projects
                launch {
                    categoryDao.getCategoriesByRecentUsage().collect { categoriesList ->
                        _categories.value = categoriesList
                    }
                }
                
                // Load only active (non-completed) projects using Flow
                launch {
                    projectDao.getActiveProjects().collect { projectsList ->
                        _projects.value = projectsList
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load categories and projects: ${e.message}"
            }
        }
    }

    /**
     * Confirm a pending transaction and create an expense
     */
    fun confirmTransaction(
        notificationId: Long,
        categoryId: Long? = null,
        projectId: Long? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = smsTransactionProcessor.confirmPendingTransaction(
                    notificationId = notificationId,
                    categoryId = categoryId,
                    projectId = projectId
                )
                if (result.success) {
                    globalViewModel.showToast("Transaction confirmed successfully", com.h4rsh41.paisatracker.ui.common.ToastType.SUCCESS)
                } else {
                    globalViewModel.showToast(result.reason ?: "Failed to confirm transaction", com.h4rsh41.paisatracker.ui.common.ToastType.ERROR)
                }
            } catch (e: Exception) {
                globalViewModel.showToast("Error confirming transaction: ${e.message}", com.h4rsh41.paisatracker.ui.common.ToastType.ERROR)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reject a pending transaction and move to trash
     */
    fun rejectTransaction(notificationId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = smsTransactionProcessor.rejectPendingTransaction(notificationId)
                if (result.success) {
                    val retentionDays = smsPreferences.getTrashRetentionDays()
                    globalViewModel.showToast("Transaction moved to trash (auto-deletes in $retentionDays days)", com.h4rsh41.paisatracker.ui.common.ToastType.INFO)
                } else {
                    globalViewModel.showToast(result.reason ?: "Failed to reject transaction", com.h4rsh41.paisatracker.ui.common.ToastType.ERROR)
                }
            } catch (e: Exception) {
                globalViewModel.showToast("Error rejecting transaction: ${e.message}", com.h4rsh41.paisatracker.ui.common.ToastType.ERROR)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Restore a transaction from trash
     */
    fun restoreTransaction(notificationId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = smsTransactionProcessor.restoreTransaction(notificationId)
                if (result.success) {
                    globalViewModel.showToast("Transaction restored to pending", com.h4rsh41.paisatracker.ui.common.ToastType.SUCCESS)
                } else {
                    globalViewModel.showToast(result.reason ?: "Failed to restore transaction", com.h4rsh41.paisatracker.ui.common.ToastType.ERROR)
                }
            } catch (e: Exception) {
                globalViewModel.showToast("Error restoring transaction: ${e.message}", com.h4rsh41.paisatracker.ui.common.ToastType.ERROR)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update SMS preferences
     */
    fun setAutoCreateExpenses(enabled: Boolean) {
        smsPreferences.setAutoCreateExpenses(enabled)
    }

    fun setShowNotifications(enabled: Boolean) {
        smsPreferences.setShowNotifications(enabled)
    }

    fun setVibrateOnDetection(enabled: Boolean) {
        smsPreferences.setVibrateOnDetection(enabled)
    }

    /**
     * Clear all SMS scan history
     * Deletes all bank notifications (which will cascade delete associated expenses)
     */
    fun clearScanHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                bankNotificationRepository.deleteAllNotifications()
                globalViewModel.showToast("Scan history cleared successfully", com.h4rsh41.paisatracker.ui.common.ToastType.SUCCESS)
            } catch (e: Exception) {
                globalViewModel.showToast("Error clearing scan history: ${e.message}", com.h4rsh41.paisatracker.ui.common.ToastType.ERROR)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Bulk confirm multiple pending transactions with the same category and project
     */
    fun bulkConfirmTransactions(
        notificationIds: List<Long>,
        categoryId: Long? = null,
        projectId: Long? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                var successCount = 0
                var failCount = 0
                
                notificationIds.forEach { notificationId ->
                    val result = smsTransactionProcessor.confirmPendingTransaction(
                        notificationId = notificationId,
                        categoryId = categoryId,
                        projectId = projectId
                    )
                    if (result.success) {
                        successCount++
                    } else {
                        failCount++
                    }
                }
                
                if (failCount == 0) {
                    globalViewModel.showToast("Successfully confirmed $successCount transaction${if (successCount != 1) "s" else ""}", com.h4rsh41.paisatracker.ui.common.ToastType.SUCCESS)
                } else {
                    globalViewModel.showToast("Confirmed $successCount, failed $failCount transaction${if (failCount != 1) "s" else ""}", com.h4rsh41.paisatracker.ui.common.ToastType.WARNING)
                }
            } catch (e: Exception) {
                globalViewModel.showToast("Error in bulk confirmation: ${e.message}", com.h4rsh41.paisatracker.ui.common.ToastType.ERROR)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear messages
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Detect category and project using merchant rules
     * Returns the detected category and project if a rule matches
     */
    suspend fun detectUsingRules(merchant: String?): RuleDetectionResult {
        return try {
            Log.d(TAG, "=== DETECT USING RULES STARTED ===")
            Log.d(TAG, "Merchant name: $merchant")
            
            if (merchant.isNullOrBlank()) {
                Log.d(TAG, "Merchant is null or blank, returning no match")
                return RuleDetectionResult(ruleMatched = false)
            }

            // Check if merchant rules are enabled
            val useMerchantRules = smsPreferences.getUseMerchantRules()
            Log.d(TAG, "Merchant rules enabled in preferences: $useMerchantRules")
            
            if (!useMerchantRules) {
                Log.d(TAG, "Merchant rules disabled, returning no match")
                globalViewModel.showToast("Merchant rules are disabled. Enable them in SMS settings.", com.h4rsh41.paisatracker.ui.common.ToastType.INFO)
                return RuleDetectionResult(ruleMatched = false)
            }

            // Find matching rule
            Log.d(TAG, "Searching for matching rule...")
            val matchingRule = merchantRuleRepository.findMatchingRule(merchant)
            
            if (matchingRule != null) {
                Log.d(TAG, "✓ Found matching rule: ${matchingRule.merchantPattern}")
                Log.d(TAG, "  Category ID: ${matchingRule.categoryId}")
                Log.d(TAG, "  Project ID: ${matchingRule.projectId}")
                
                val category = categoryDao.getCategoryByIdSync(matchingRule.categoryId)
                Log.d(TAG, "  Category found: ${category?.name ?: "NULL"}")
                
                val project = matchingRule.projectId?.let { projectDao.getProjectByIdSync(it) }
                Log.d(TAG, "  Project found: ${project?.name ?: "NULL"}")
                
                globalViewModel.showToast("Detected: ${category?.name ?: "Unknown"}", com.h4rsh41.paisatracker.ui.common.ToastType.SUCCESS)
                
                return RuleDetectionResult(
                    category = category,
                    project = project,
                    ruleMatched = true
                )
            }

            Log.d(TAG, "✗ No matching rule found for merchant: $merchant")
            globalViewModel.showToast("No rule found for '$merchant'. Create a rule in Merchant Rules.", com.h4rsh41.paisatracker.ui.common.ToastType.INFO)
            return RuleDetectionResult(ruleMatched = false)
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting category: ${e.message}", e)
            globalViewModel.showToast("Error detecting category: ${e.message}", com.h4rsh41.paisatracker.ui.common.ToastType.ERROR)
            return RuleDetectionResult(ruleMatched = false)
        }
    }
    
    /**
     * Get a notification by ID
     */
    suspend fun getNotificationById(notificationId: Long): BankNotificationEntity? {
        return bankNotificationRepository.getById(notificationId)
    }
    
    companion object {
        private const val TAG = "SmsTransactionViewModel"
    }
}

// Made with Bob
