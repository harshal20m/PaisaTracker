package com.h4rsh41.paisatracker.ui.trash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.h4rsh41.paisatracker.data.BankNotificationEntity
import com.h4rsh41.paisatracker.data.BankNotificationRepository
import com.h4rsh41.paisatracker.data.PaisaTrackerDatabase
import com.h4rsh41.paisatracker.data.SmsPreferences
import com.h4rsh41.paisatracker.manager.SmsTransactionProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TrashViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PaisaTrackerDatabase.getDatabase(application)
    private val bankNotificationRepository = BankNotificationRepository(database.bankNotificationDao())
    private val smsPreferences = SmsPreferences(application)
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
        categoryDao = database.categoryDao(),
        bankNotificationRepository = bankNotificationRepository,
        unrecognizedSmsRepository = com.h4rsh41.paisatracker.data.UnrecognizedSmsRepository(database.unrecognizedSmsDao()),
        smsPreferences = smsPreferences,
        merchantRuleRepository = merchantRuleRepository,
        repository = repository
    )

    // Trashed transactions
    val trashedTransactions = bankNotificationRepository.getTrashedTransactions()
    val trashedCount = bankNotificationRepository.getTrashedCount()

    // Trash retention settings
    val trashRetentionDays = smsPreferences.trashRetentionDaysFlow

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

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
                    _successMessage.value = "Transaction restored to pending"
                } else {
                    _errorMessage.value = result.reason ?: "Failed to restore transaction"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error restoring transaction: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a single transaction permanently
     */
    fun deleteTransaction(notificationId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                database.bankNotificationDao().deleteById(notificationId)
                _successMessage.value = "Transaction deleted permanently"
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting transaction: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Empty trash - delete all trashed transactions
     */
    fun emptyTrash() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val count = bankNotificationRepository.emptyTrash()
                _successMessage.value = "Deleted $count transaction(s) from trash"
            } catch (e: Exception) {
                _errorMessage.value = "Error emptying trash: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Calculate days remaining until auto-deletion
     */
    fun getDaysRemaining(transaction: BankNotificationEntity): Int {
        val scheduledDate = transaction.deletionScheduledAt ?: return (transaction.trashRetentionDays ?: 30)
        
        val now = LocalDateTime.now()
        val daysRemaining = ChronoUnit.DAYS.between(now, scheduledDate).toInt()
        
        return maxOf(0, daysRemaining)
    }

    /**
     * Get color for days remaining indicator
     */
    fun getDaysRemainingColor(daysRemaining: Int): TrashItemColor {
        return when {
            daysRemaining > 20 -> TrashItemColor.GREEN
            daysRemaining > 10 -> TrashItemColor.YELLOW
            else -> TrashItemColor.RED
        }
    }

    /**
     * Update trash retention period
     */
    fun setTrashRetentionDays(days: Int) {
        smsPreferences.setTrashRetentionDays(days)
        _successMessage.value = "Trash retention period updated to $days days"
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
}

enum class TrashItemColor {
    GREEN, YELLOW, RED
}

// Made with Bob