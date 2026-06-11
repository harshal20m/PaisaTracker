package com.h4rsh41.paisatracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

enum class SmsTransactionStatus {
    PENDING,    // Waiting for user confirmation
    CONFIRMED,  // User confirmed and expense created
    REJECTED,   // User rejected - moved to trash
    AUTO_CREATED, // Automatically created (when auto-mode is ON)
    CREDIT_PENDING  // Credit transaction waiting for user action
}

@Entity(
    tableName = "bank_notifications",
    indices = [
        Index(value = ["package_name", "message_hash"], unique = true),
        Index(value = ["status"]),
        Index(value = ["deletion_scheduled_at"])
    ]
)
data class BankNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "sender_alias")
    val senderAlias: String,

    @ColumnInfo(name = "message_body")
    val messageBody: String,

    @ColumnInfo(name = "message_hash")
    val messageHash: String,

    @ColumnInfo(name = "posted_at")
    val postedAt: LocalDateTime,

    @ColumnInfo(name = "processed")
    val processed: Boolean = false,

    @ColumnInfo(name = "transaction_id")
    val transactionId: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "status")
    val status: SmsTransactionStatus = SmsTransactionStatus.PENDING,

    // Parsed transaction details for display
    @ColumnInfo(name = "amount")
    val amount: Double? = null,

    @ColumnInfo(name = "merchant")
    val merchant: String? = null,

    @ColumnInfo(name = "bank_name")
    val bankName: String? = null,

    @ColumnInfo(name = "account_last4")
    val accountLast4: String? = null,
    
    // Transaction type (EXPENSE, INCOME, etc.)
    @ColumnInfo(name = "transaction_type")
    val transactionType: String? = null,

    // Trash-related fields
    @ColumnInfo(name = "rejected_at")
    val rejectedAt: LocalDateTime? = null,

    @ColumnInfo(name = "deletion_scheduled_at")
    val deletionScheduledAt: LocalDateTime? = null,

    @ColumnInfo(name = "trash_retention_days")
    val trashRetentionDays: Int? = null
)

// Made with Bob
