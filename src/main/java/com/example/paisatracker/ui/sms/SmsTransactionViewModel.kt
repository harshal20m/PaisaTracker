package com.example.paisatracker.ui.sms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.paisatracker.data.BankNotificationEntity
import com.example.paisatracker.data.BankNotificationRepository
import com.example.paisatracker.data.Category
import com.example.paisatracker.data.CategoryDao
import com.example.paisatracker.data.PaisaTrackerDatabase
import com.example.paisatracker.data.Project
import com.example.paisatracker.data.ProjectDao
import com.example.paisatracker.data.SmsPreferences
import com.example.paisatracker.manager.SmsTransactionProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmsTransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PaisaTrackerDatabase.getDatabase(application)
    private val bankNotificationRepository = BankNotificationRepository(database.bankNotificationDao())
    private val categoryDao: CategoryDao = database.categoryDao()
    private val projectDao: ProjectDao = database.projectDao()
    private val smsPreferences = SmsPreferences(application)
    
    private val smsTransactionProcessor = SmsTransactionProcessor(
        context = application,
        expenseDao = database.expenseDao(),
        categoryDao = categoryDao,
        bankNotificationRepository = bankNotificationRepository,
        unrecognizedSmsRepository = com.example.paisatracker.data.UnrecognizedSmsRepository(database.unrecognizedSmsDao()),
        smsPreferences = smsPreferences
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
                _categories.value = categoryDao.getAllCategoriesList()
                _projects.value = projectDao.getAllProjectsList()
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
                    _successMessage.value = "Transaction confirmed successfully"
                } else {
                    _errorMessage.value = result.reason ?: "Failed to confirm transaction"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error confirming transaction: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reject a pending transaction
     */
    fun rejectTransaction(notificationId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = smsTransactionProcessor.rejectPendingTransaction(notificationId)
                if (result.success) {
                    _successMessage.value = "Transaction rejected"
                } else {
                    _errorMessage.value = result.reason ?: "Failed to reject transaction"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error rejecting transaction: ${e.message}"
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
     * Clear messages
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}

// Made with Bob
