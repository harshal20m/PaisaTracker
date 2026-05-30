package com.example.paisatracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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

    @Query("DELETE FROM bank_notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bank_notifications")
    suspend fun deleteAllNotifications()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(notification: BankNotificationEntity): Long
}

// Made with Bob
