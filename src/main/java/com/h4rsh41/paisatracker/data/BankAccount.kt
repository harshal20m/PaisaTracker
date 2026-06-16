package com.h4rsh41.paisatracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a bank account or payment source for tracking expenses.
 *
 * Features:
 * - Multiple account support (Bank accounts, Credit cards, Cash, Digital wallets)
 * - Balance tracking with initial balance
 * - Account type categorization
 * - Color coding for visual distinction
 * - Active/Inactive status
 * - SMS transaction matching via last 4 digits
 */
@Entity(tableName = "bank_accounts")
data class BankAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Display name (e.g., "HDFC Savings", "Cash Wallet", "ICICI Credit Card") */
    val name: String,
    
    /** Account type: "BANK", "CASH", "CREDIT_CARD", "DIGITAL_WALLET" */
    val accountType: String,
    
    /** Optional bank name (e.g., "HDFC Bank", "State Bank of India") */
    val bankName: String? = null,
    
    /** Last 4 digits of account number for SMS matching (optional) */
    val accountNumberLast4: String? = null,
    
    /** Initial balance when account was added */
    val initialBalance: Double = 0.0,
    
    /** Current balance (updated with each transaction) */
    val currentBalance: Double = 0.0,
    
    /** Emoji icon for visual identification (e.g., "🏦", "💳", "💰", "📱") */
    val emoji: String = "🏦",
    
    /** Color hex code for UI theming (e.g., "#FF6B6B") */
    val colorHex: String = "#2196F3",
    
    /** Whether this account is active and should be shown in lists */
    val isActive: Boolean = true,
    
    /** Account priority: PRIMARY or SECONDARY (for organizing accounts) */
    val priority: String = AccountPriority.SECONDARY,
    
    /** Timestamp when account was created */
    val createdAt: Long = System.currentTimeMillis(),
    
    /** Last updated timestamp */
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Account priority levels for organizing accounts
 */
object AccountPriority {
    const val PRIMARY = "PRIMARY"       // Main/primary accounts (shown first)
    const val SECONDARY = "SECONDARY"   // Secondary/backup accounts
}

/**
 * Predefined account types for consistency
 */
object AccountType {
    const val BANK = "BANK"
    const val CASH = "CASH"
    const val CREDIT_CARD = "CREDIT_CARD"
    const val DIGITAL_WALLET = "DIGITAL_WALLET"
}

/**
 * Data class for account balance summary
 */
data class AccountBalanceSummary(
    val totalBalance: Double,
    val activeAccountsCount: Int,
    val accountBalances: List<AccountBalance>
)

data class AccountBalance(
    val accountId: Long,
    val accountName: String,
    val accountEmoji: String,
    val balance: Double,
    val colorHex: String
)

// Made with Bob
