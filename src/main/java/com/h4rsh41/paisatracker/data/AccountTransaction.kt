package com.h4rsh41.paisatracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a transaction (credit/debit) for a bank account.
 * This tracks all money movements including:
 * - Salary credits
 * - Manual top-ups
 * - Expense debits
 * - Refunds
 *
 * This provides a complete history of account balance changes.
 */
@Entity(
    tableName = "account_transactions",
    foreignKeys = [
        ForeignKey(
            entity = BankAccount::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["timestamp"]),
        Index(value = ["type"]),
        Index(value = ["month", "year"])
    ]
)
data class AccountTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Bank account ID this transaction belongs to */
    val accountId: Long,
    
    /** Transaction type: CREDIT, DEBIT, SALARY, TOPUP, REFUND */
    val type: String,
    
    /** Transaction amount (always positive, type determines direction) */
    val amount: Double,
    
    /** Balance after this transaction */
    val balanceAfter: Double,
    
    /** Description/note for this transaction */
    val description: String = "",
    
    /** Reference to related entity (expenseId, salaryId, etc.) */
    val referenceId: Long? = null,
    
    /** Reference type: EXPENSE, SALARY, MANUAL, etc. */
    val referenceType: String? = null,
    
    /** Timestamp when transaction occurred */
    val timestamp: Long = System.currentTimeMillis(),
    
    /** Month (1-12) for grouping */
    val month: Int,
    
    /** Year for grouping */
    val year: Int
)

/**
 * Transaction types
 */
object TransactionType {
    const val CREDIT = "CREDIT"           // Generic credit
    const val DEBIT = "DEBIT"             // Generic debit
    const val SALARY = "SALARY"           // Salary credit
    const val TOPUP = "TOPUP"             // Manual top-up
    const val EXPENSE = "EXPENSE"         // Expense debit
    const val REFUND = "REFUND"           // Refund credit
}

/**
 * Reference types for linking transactions
 */
object TransactionReferenceType {
    const val EXPENSE = "EXPENSE"
    const val SALARY = "SALARY"
    const val MANUAL = "MANUAL"
}

// Made with Bob