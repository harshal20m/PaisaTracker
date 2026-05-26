package com.example.paisatracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for BankAccount operations.
 * Provides CRUD operations and balance tracking queries.
 */
@Dao
interface BankAccountDao {
    
    // ── Create ────────────────────────────────────────────────────────────────
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: BankAccount): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<BankAccount>)
    
    // ── Read ──────────────────────────────────────────────────────────────────
    
    /** Get all bank accounts ordered by creation date (newest first) */
    @Query("SELECT * FROM bank_accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<BankAccount>>
    
    /** Get only active accounts */
    @Query("SELECT * FROM bank_accounts WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveAccounts(): Flow<List<BankAccount>>
    
    /** Get a specific account by ID */
    @Query("SELECT * FROM bank_accounts WHERE id = :accountId LIMIT 1")
    fun getAccountById(accountId: Long): Flow<BankAccount?>
    
    /** Get account by ID (one-time fetch) */
    @Query("SELECT * FROM bank_accounts WHERE id = :accountId LIMIT 1")
    suspend fun getAccountByIdOnce(accountId: Long): BankAccount?
    
    /** Get accounts by type */
    @Query("SELECT * FROM bank_accounts WHERE accountType = :type AND isActive = 1 ORDER BY name ASC")
    fun getAccountsByType(type: String): Flow<List<BankAccount>>
    
    /** Get total balance across all active accounts */
    @Query("SELECT COALESCE(SUM(currentBalance), 0.0) FROM bank_accounts WHERE isActive = 1")
    fun getTotalBalance(): Flow<Double>
    
    /** Get count of active accounts */
    @Query("SELECT COUNT(*) FROM bank_accounts WHERE isActive = 1")
    fun getActiveAccountCount(): Flow<Int>
    
    /** Search accounts by name */
    @Query("""
        SELECT * FROM bank_accounts 
        WHERE name LIKE '%' || :query || '%' 
        OR bankName LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchAccounts(query: String): Flow<List<BankAccount>>
    
    // ── Update ────────────────────────────────────────────────────────────────
    
    @Update
    suspend fun update(account: BankAccount)
    
    /** Update account balance */
    @Query("UPDATE bank_accounts SET currentBalance = :newBalance, updatedAt = :timestamp WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, newBalance: Double, timestamp: Long = System.currentTimeMillis())
    
    /** Increment account balance (for income/refunds) */
    @Query("UPDATE bank_accounts SET currentBalance = currentBalance + :amount, updatedAt = :timestamp WHERE id = :accountId")
    suspend fun incrementBalance(accountId: Long, amount: Double, timestamp: Long = System.currentTimeMillis())
    
    /** Decrement account balance (for expenses) */
    @Query("UPDATE bank_accounts SET currentBalance = currentBalance - :amount, updatedAt = :timestamp WHERE id = :accountId")
    suspend fun decrementBalance(accountId: Long, amount: Double, timestamp: Long = System.currentTimeMillis())
    
    /** Toggle account active status */
    @Query("UPDATE bank_accounts SET isActive = :isActive, updatedAt = :timestamp WHERE id = :accountId")
    suspend fun setActiveStatus(accountId: Long, isActive: Boolean, timestamp: Long = System.currentTimeMillis())
    
    // ── Delete ────────────────────────────────────────────────────────────────
    
    @Delete
    suspend fun delete(account: BankAccount)
    
    @Query("DELETE FROM bank_accounts WHERE id = :accountId")
    suspend fun deleteById(accountId: Long)
    
    /** Soft delete - mark as inactive instead of deleting */
    @Query("UPDATE bank_accounts SET isActive = 0, updatedAt = :timestamp WHERE id = :accountId")
    suspend fun softDelete(accountId: Long, timestamp: Long = System.currentTimeMillis())
    
    /** Delete all accounts (use with caution) */
    @Query("DELETE FROM bank_accounts")
    suspend fun deleteAll()
    
    // ── Statistics ────────────────────────────────────────────────────────────
    
    /** Get account balance summary for dashboard */
    @Query("""
        SELECT 
            id as accountId,
            name as accountName,
            emoji as accountEmoji,
            currentBalance as balance,
            colorHex
        FROM bank_accounts 
        WHERE isActive = 1
        ORDER BY currentBalance DESC
    """)
    fun getAccountBalances(): Flow<List<AccountBalance>>
}

// Made with Bob
