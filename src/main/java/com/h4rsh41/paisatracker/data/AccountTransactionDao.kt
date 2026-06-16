package com.h4rsh41.paisatracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Data Access Object for AccountTransaction operations.
 * Provides queries for transaction history and balance tracking.
 */
@Dao
interface AccountTransactionDao {
    
    // ── Create ────────────────────────────────────────────────────────────────
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: AccountTransaction): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<AccountTransaction>)
    
    // ── Read ──────────────────────────────────────────────────────────────────
    
    /** Get all transactions for an account ordered by timestamp (newest first) */
    @Query("SELECT * FROM account_transactions WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getAccountTransactions(accountId: Long): Flow<List<AccountTransaction>>
    
    /** Get transactions for an account in a specific month */
    @Query("""
        SELECT * FROM account_transactions 
        WHERE accountId = :accountId AND month = :month AND year = :year 
        ORDER BY timestamp DESC
    """)
    fun getAccountTransactionsForMonth(accountId: Long, month: Int, year: Int): Flow<List<AccountTransaction>>
    
    /** Get recent transactions (last N days) with lazy loading support */
    @Query("""
        SELECT * FROM account_transactions 
        WHERE accountId = :accountId AND timestamp >= :startTimestamp 
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun getRecentTransactions(accountId: Long, startTimestamp: Long, limit: Int = 50): List<AccountTransaction>
    
    /** Get transactions for current and last month (for lazy loading) */
    @Query("""
        SELECT * FROM account_transactions 
        WHERE accountId = :accountId 
        AND ((month = :currentMonth AND year = :currentYear) OR (month = :lastMonth AND year = :lastYear))
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun getTransactionsForLastTwoMonths(
        accountId: Long,
        currentMonth: Int,
        currentYear: Int,
        lastMonth: Int,
        lastYear: Int,
        limit: Int = 100
    ): List<AccountTransaction>
    
    /** Get credit transactions (salary, topup, refund) */
    @Query("""
        SELECT * FROM account_transactions 
        WHERE accountId = :accountId AND type IN ('CREDIT', 'SALARY', 'TOPUP', 'REFUND')
        ORDER BY timestamp DESC
    """)
    fun getCreditTransactions(accountId: Long): Flow<List<AccountTransaction>>
    
    /** Get debit transactions (expenses) */
    @Query("""
        SELECT * FROM account_transactions 
        WHERE accountId = :accountId AND type IN ('DEBIT', 'EXPENSE')
        ORDER BY timestamp DESC
    """)
    fun getDebitTransactions(accountId: Long): Flow<List<AccountTransaction>>
    
    /** Get salary history for an account */
    @Query("""
        SELECT * FROM account_transactions 
        WHERE accountId = :accountId AND type = 'SALARY'
        ORDER BY timestamp DESC
    """)
    fun getSalaryHistory(accountId: Long): Flow<List<AccountTransaction>>
    
    /** Get transaction by ID */
    @Query("SELECT * FROM account_transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionById(transactionId: Long): AccountTransaction?
    
    /** Get total credits for an account */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM account_transactions WHERE accountId = :accountId AND type IN ('CREDIT', 'SALARY', 'TOPUP', 'REFUND')")
    fun getTotalCredits(accountId: Long): Flow<Double>
    
    /** Get total debits for an account */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM account_transactions WHERE accountId = :accountId AND type IN ('DEBIT', 'EXPENSE')")
    fun getTotalDebits(accountId: Long): Flow<Double>
    
    /** Get transaction count for an account */
    @Query("SELECT COUNT(*) FROM account_transactions WHERE accountId = :accountId")
    fun getTransactionCount(accountId: Long): Flow<Int>
    
    // ── Update ────────────────────────────────────────────────────────────────
    
    @Update
    suspend fun update(transaction: AccountTransaction)
    
    // ── Delete ────────────────────────────────────────────────────────────────
    
    @Delete
    suspend fun delete(transaction: AccountTransaction)
    
    @Query("DELETE FROM account_transactions WHERE id = :transactionId")
    suspend fun deleteById(transactionId: Long)
    
    @Query("DELETE FROM account_transactions WHERE accountId = :accountId")
    suspend fun deleteAllForAccount(accountId: Long)
    
    /** Delete transactions older than specified timestamp */
    @Query("DELETE FROM account_transactions WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
    
    @Query("DELETE FROM account_transactions")
    suspend fun deleteAll()
}

// Made with Bob