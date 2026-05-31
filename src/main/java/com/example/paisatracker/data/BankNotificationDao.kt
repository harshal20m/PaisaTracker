package com.example.paisatracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface BankNotificationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: BankNotificationEntity): Long

    @Update
    suspend fun update(notification: BankNotificationEntity)

    @Query(
        """
        UPDATE bank_notifications
        SET processed = :processed, transaction_id = :transactionId
        WHERE id = :id
        """
    )
    suspend fun updateStatus(id: Long, processed: Boolean, transactionId: Long?)

    @Query(
        """
        UPDATE bank_notifications
        SET status = :status, transaction_id = :transactionId
        WHERE id = :id
        """
    )
    suspend fun updateTransactionStatus(id: Long, status: SmsTransactionStatus, transactionId: Long?)

    @Query("SELECT * FROM bank_notifications WHERE processed = 0 ORDER BY posted_at DESC")
    suspend fun getUnprocessed(): List<BankNotificationEntity>

    @Query("SELECT * FROM bank_notifications WHERE status = 'PENDING' ORDER BY posted_at DESC")
    fun getPendingTransactions(): Flow<List<BankNotificationEntity>>

    @Query("SELECT * FROM bank_notifications WHERE status = 'PENDING' ORDER BY posted_at DESC")
    suspend fun getPendingTransactionsList(): List<BankNotificationEntity>

    @Query("SELECT COUNT(*) FROM bank_notifications WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT * FROM bank_notifications ORDER BY posted_at DESC")
    fun getAllNotifications(): Flow<List<BankNotificationEntity>>

    @Query("SELECT * FROM bank_notifications WHERE id = :id")
    suspend fun getById(id: Long): BankNotificationEntity?

    @Query("SELECT * FROM bank_notifications WHERE message_hash = :hash LIMIT 1")
    suspend fun getByHash(hash: String): BankNotificationEntity?

    @Query("SELECT * FROM bank_notifications WHERE transaction_id = :expenseId LIMIT 1")
    suspend fun getByTransactionId(expenseId: Long): BankNotificationEntity?

    @Query("""
        UPDATE bank_notifications
        SET transaction_id = NULL,
            status = 'PENDING'
        WHERE transaction_id = :expenseId
    """)
    suspend fun resetTransactionLink(expenseId: Long)

    @Query("DELETE FROM bank_notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bank_notifications")
    suspend fun deleteAllNotifications()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(notification: BankNotificationEntity): Long

    // Trash-related methods
    @Query("SELECT * FROM bank_notifications WHERE status = 'REJECTED' ORDER BY rejected_at DESC")
    fun getTrashedTransactions(): Flow<List<BankNotificationEntity>>

    @Query("SELECT * FROM bank_notifications WHERE status = 'REJECTED' ORDER BY rejected_at DESC")
    suspend fun getTrashedTransactionsList(): List<BankNotificationEntity>

    @Query("SELECT COUNT(*) FROM bank_notifications WHERE status = 'REJECTED'")
    fun getTrashedCount(): Flow<Int>

    @Query("""
        SELECT * FROM bank_notifications
        WHERE status = 'REJECTED'
        AND deletion_scheduled_at <= :currentTime
    """)
    suspend fun getExpiredTrashedTransactions(currentTime: LocalDateTime): List<BankNotificationEntity>

    @Query("""
        UPDATE bank_notifications
        SET status = 'PENDING',
            rejected_at = NULL,
            deletion_scheduled_at = NULL,
            trash_retention_days = NULL
        WHERE id = :id
    """)
    suspend fun restoreTransaction(id: Long)

    @Query("DELETE FROM bank_notifications WHERE status = 'REJECTED'")
    suspend fun emptyTrash(): Int

    @Query("""
        DELETE FROM bank_notifications
        WHERE status = 'REJECTED'
        AND deletion_scheduled_at <= :currentTime
    """)
    suspend fun deleteExpiredTransactions(currentTime: LocalDateTime): Int

    @Query("""
        UPDATE bank_notifications
        SET status = :status,
            rejected_at = :rejectedAt,
            deletion_scheduled_at = :deletionScheduledAt,
            trash_retention_days = :retentionDays
        WHERE id = :id
    """)
    suspend fun moveToTrash(
        id: Long,
        status: SmsTransactionStatus,
        rejectedAt: LocalDateTime,
        deletionScheduledAt: LocalDateTime,
        retentionDays: Int
    )
}

// Made with Bob
