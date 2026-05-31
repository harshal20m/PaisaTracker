package com.example.paisatracker.data

import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class BankNotificationRepository(
    private val dao: BankNotificationDao
) {

    suspend fun insert(notification: BankNotificationEntity): Long {
        return dao.insert(notification)
    }

    suspend fun update(notification: BankNotificationEntity) {
        dao.update(notification)
    }

    suspend fun getById(id: Long): BankNotificationEntity? {
        return dao.getById(id)
    }

    suspend fun logNotification(
        packageName: String,
        senderAlias: String,
        messageBody: String,
        postedAtMillis: Long
    ): Long {
        val postedAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(postedAtMillis),
            ZoneId.systemDefault()
        )

        // Bucket timestamp to the nearest minute so identical recurring
        // notifications at different times produce distinct hashes.
        val timeBucket = postedAtMillis / 60_000
        val messageHash = hash("$packageName|$senderAlias|$timeBucket|$messageBody")

        val entity = BankNotificationEntity(
            packageName = packageName,
            senderAlias = senderAlias,
            messageBody = messageBody,
            messageHash = messageHash,
            postedAt = postedAt
        )

        return dao.insert(entity)
    }

    suspend fun markProcessed(id: Long, transactionId: Long?) {
        dao.updateStatus(id, processed = true, transactionId = transactionId)
    }

    suspend fun getUnprocessed(): List<BankNotificationEntity> =
        dao.getUnprocessed()

    fun getPendingTransactions(): Flow<List<BankNotificationEntity>> =
        dao.getPendingTransactions()

    suspend fun getPendingTransactionsList(): List<BankNotificationEntity> =
        dao.getPendingTransactionsList()

    fun getPendingCount(): Flow<Int> =
        dao.getPendingCount()

    // Trash-related methods
    fun getTrashedTransactions(): Flow<List<BankNotificationEntity>> =
        dao.getTrashedTransactions()

    suspend fun getTrashedTransactionsList(): List<BankNotificationEntity> =
        dao.getTrashedTransactionsList()

    fun getTrashedCount(): Flow<Int> =
        dao.getTrashedCount()

    suspend fun moveToTrash(
        id: Long,
        retentionDays: Int
    ) {
        val now = LocalDateTime.now()
        val deletionDate = now.plusDays(retentionDays.toLong())
        
        dao.moveToTrash(
            id = id,
            status = SmsTransactionStatus.REJECTED,
            rejectedAt = now,
            deletionScheduledAt = deletionDate,
            retentionDays = retentionDays
        )
    }

    suspend fun restoreTransaction(id: Long) {
        dao.restoreTransaction(id)
    }

    suspend fun emptyTrash(): Int {
        return dao.emptyTrash()
    }

    suspend fun deleteExpiredTransactions(): Int {
        val now = LocalDateTime.now()
        return dao.deleteExpiredTransactions(now)
    }

    suspend fun getExpiredTrashedTransactions(): List<BankNotificationEntity> {
        val now = LocalDateTime.now()
        return dao.getExpiredTrashedTransactions(now)
    }

    private fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

// Made with Bob
