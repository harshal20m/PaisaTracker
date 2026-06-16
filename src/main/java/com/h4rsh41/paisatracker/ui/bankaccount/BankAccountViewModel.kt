package com.h4rsh41.paisatracker.ui.bankaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.h4rsh41.paisatracker.PaisaTrackerViewModel
import com.h4rsh41.paisatracker.data.AccountBalance
import com.h4rsh41.paisatracker.data.BankAccount
import com.h4rsh41.paisatracker.data.PaisaTrackerRepository
import com.h4rsh41.paisatracker.ui.common.ToastType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Bank Account Management
 * Handles CRUD operations and balance tracking for bank accounts
 */
class BankAccountViewModel(
    private val repository: PaisaTrackerRepository,
    private val globalViewModel: PaisaTrackerViewModel
) : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────────

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedAccountType = MutableStateFlow<String?>(null)
    val selectedAccountType: StateFlow<String?> = _selectedAccountType

    // ── Data Flows ────────────────────────────────────────────────────────────

    /** All bank accounts */
    val allAccounts: StateFlow<List<BankAccount>> = repository.getAllBankAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Active bank accounts only */
    val activeAccounts: StateFlow<List<BankAccount>> = repository.getActiveBankAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Filtered accounts based on search and type */
    val filteredAccounts: StateFlow<List<BankAccount>> = combine(
        allAccounts,
        searchQuery,
        selectedAccountType
    ) { accounts, query, type ->
        accounts.filter { account ->
            val matchesSearch = query.isBlank() || 
                account.name.contains(query, ignoreCase = true) ||
                account.bankName?.contains(query, ignoreCase = true) == true
            
            val matchesType = type == null || account.accountType == type
            
            matchesSearch && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Total balance across all active accounts */
    val totalBalance: StateFlow<Double> = repository.getTotalBankBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /** Active account count */
    val activeAccountCount: StateFlow<Int> = repository.getActiveBankAccountCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Account balances for dashboard */
    val accountBalances: StateFlow<List<AccountBalance>> = repository.getAccountBalances()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Get transactions for a specific bank account */
    fun getAccountTransactions(accountId: Long): StateFlow<List<com.h4rsh41.paisatracker.data.RecentExpense>> {
        return repository.getExpensesByBankAccount(accountId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedAccountType(type: String?) {
        _selectedAccountType.value = type
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedAccountType.value = null
    }

    /** Create a new bank account */
    fun createAccount(
        name: String,
        accountType: String,
        bankName: String?,
        accountNumberLast4: String?,
        initialBalance: Double,
        emoji: String,
        colorHex: String,
        priority: String = "SECONDARY",
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val account = BankAccount(
                    name = name,
                    accountType = accountType,
                    bankName = bankName,
                    accountNumberLast4 = accountNumberLast4,
                    initialBalance = initialBalance,
                    currentBalance = initialBalance,
                    emoji = emoji,
                    colorHex = colorHex,
                    priority = priority,
                    isActive = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                val id = repository.insertBankAccount(account)
                globalViewModel.showToast("Account created successfully", ToastType.SUCCESS)
                onSuccess(id)
            } catch (e: Exception) {
                globalViewModel.showToast("Failed to create account: ${e.message}", ToastType.ERROR)
            }
        }
    }

    /** Update an existing bank account */
    fun updateAccount(
        account: BankAccount,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.updateBankAccount(account.copy(updatedAt = System.currentTimeMillis()))
                globalViewModel.showToast("Account updated successfully", ToastType.SUCCESS)
                onSuccess()
            } catch (e: Exception) {
                globalViewModel.showToast("Failed to update account: ${e.message}", ToastType.ERROR)
            }
        }
    }

    /** Update account balance */
    fun updateAccountBalance(
        accountId: Long,
        newBalance: Double,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.updateBankAccountBalance(accountId, newBalance)
                globalViewModel.showToast("Balance updated", ToastType.SUCCESS)
                onSuccess()
            } catch (e: Exception) {
                globalViewModel.showToast("Failed to update balance: ${e.message}", ToastType.ERROR)
            }
        }
    }

    /** Add money to account */
    fun addMoneyToAccount(
        accountId: Long,
        amount: Double,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.incrementBankAccountBalance(accountId, amount)
                globalViewModel.showToast("Money added successfully", ToastType.SUCCESS)
                onSuccess()
            } catch (e: Exception) {
                globalViewModel.showToast("Failed to add money: ${e.message}", ToastType.ERROR)
            }
        }
    }

    /** Toggle account active status */
    fun toggleAccountStatus(
        accountId: Long,
        isActive: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.setBankAccountActiveStatus(accountId, isActive)
                val status = if (isActive) "activated" else "deactivated"
                globalViewModel.showToast("Account $status", ToastType.SUCCESS)
                onSuccess()
            } catch (e: Exception) {
                globalViewModel.showToast("Failed to update status: ${e.message}", ToastType.ERROR)
            }
        }
    }

    /** Delete account (soft delete) */
    fun deleteAccount(
        accountId: Long,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.softDeleteBankAccount(accountId)
                globalViewModel.showToast("Account deleted", ToastType.SUCCESS)
                onSuccess()
            } catch (e: Exception) {
                globalViewModel.showToast("Failed to delete account: ${e.message}", ToastType.ERROR)
            }
        }
    }

    /** Permanently delete account */
    fun permanentlyDeleteAccount(
        accountId: Long,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.deleteBankAccountById(accountId)
                globalViewModel.showToast("Account permanently deleted", ToastType.SUCCESS)
                onSuccess()
            } catch (e: Exception) {
                globalViewModel.showToast("Failed to delete account: ${e.message}", ToastType.ERROR)
            }
        }
    }

    /** Get account by ID (one-time fetch) */
    suspend fun getAccountById(accountId: Long): BankAccount? {
        return try {
            repository.getBankAccountByIdOnce(accountId)
        } catch (e: Exception) {
            globalViewModel.showToast("Failed to load account: ${e.message}", ToastType.ERROR)
            null
        }
    }

    /** Get transactions for last two months (for lazy loading) */
    suspend fun getTransactionsForLastTwoMonths(
        accountId: Long,
        currentMonth: Int,
        currentYear: Int,
        lastMonth: Int,
        lastYear: Int,
        limit: Int = 100
    ): List<com.h4rsh41.paisatracker.data.AccountTransaction> {
        return try {
            repository.getTransactionsForLastTwoMonths(
                accountId, currentMonth, currentYear, lastMonth, lastYear, limit
            )
        } catch (e: Exception) {
            globalViewModel.showToast("Failed to load transactions: ${e.message}", ToastType.ERROR)
            emptyList()
        }
    }

    /** Record a transaction (credit or debit) */
    fun recordTransaction(
        accountId: Long,
        type: String,
        amount: Double,
        description: String = "",
        referenceId: Long? = null,
        referenceType: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.recordAccountTransaction(
                    accountId = accountId,
                    type = type,
                    amount = amount,
                    description = description,
                    referenceId = referenceId,
                    referenceType = referenceType
                )
                onSuccess()
            } catch (e: Exception) {
                globalViewModel.showToast("Failed to record transaction: ${e.message}", ToastType.ERROR)
            }
        }
    }
}

/**
 * Factory for creating BankAccountViewModel with dependencies
 */
class BankAccountViewModelFactory(
    private val repository: PaisaTrackerRepository,
    private val globalViewModel: PaisaTrackerViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BankAccountViewModel::class.java)) {
            return BankAccountViewModel(repository, globalViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Made with Bob
