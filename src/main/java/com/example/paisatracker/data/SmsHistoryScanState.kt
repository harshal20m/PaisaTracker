package com.example.paisatracker.data

import java.time.LocalDate

/**
 * Represents the state of SMS history scanning
 */
sealed class SmsHistoryScanState {
    object Idle : SmsHistoryScanState()
    object SelectingDateRange : SmsHistoryScanState()
    data class Scanning(
        val totalMessages: Int,
        val processedMessages: Int,
        val foundTransactions: Int,
        val createdExpenses: Int,
        val skippedDuplicates: Int,
        val currentSender: String? = null
    ) : SmsHistoryScanState()
    data class Completed(
        val totalMessages: Int,
        val foundTransactions: Int,
        val createdExpenses: Int,
        val skippedDuplicates: Int,
        val failedMessages: Int,
        val scanDurationMs: Long,
        val scanResults: List<SmsScanResult> = emptyList()
    ) : SmsHistoryScanState()
    data class Error(val message: String, val throwable: Throwable? = null) : SmsHistoryScanState()
    object Cancelled : SmsHistoryScanState()
}

/**
 * Date range options for scanning
 */
enum class ScanDateRange(val days: Int, val displayName: String) {
    LAST_7_DAYS(7, "Last 7 days"),
    LAST_15_DAYS(15, "Last 15 days"),
    LAST_30_DAYS(30, "Last 30 days"),
    LAST_60_DAYS(60, "Last 60 days"),
    LAST_90_DAYS(90, "Last 90 days"),
    CUSTOM(0, "Custom range");

    fun getStartDate(): LocalDate {
        return if (this == CUSTOM) {
            LocalDate.now().minusDays(30) // Default for custom
        } else {
            LocalDate.now().minusDays(days.toLong())
        }
    }
}

/**
 * Result of scanning a single SMS message
 */
data class SmsScanResult(
    val sender: String,
    val body: String,
    val timestamp: Long,
    val success: Boolean,
    val isDuplicate: Boolean = false,
    val expenseId: Long? = null,
    val notificationId: Long? = null,
    val errorMessage: String? = null
)

/**
 * Configuration for SMS history scan
 */
data class SmsHistoryScanConfig(
    val startDate: LocalDate,
    val endDate: LocalDate = LocalDate.now(),
    val autoCreateExpenses: Boolean = true,
    val batchSize: Int = 50,
    val progressUpdateInterval: Int = 10
)

/**
 * Summary of scan results
 */
data class ScanSummary(
    val totalMessagesScanned: Int,
    val financialMessagesFound: Int,
    val transactionsParsed: Int,
    val expensesCreated: Int,
    val duplicatesSkipped: Int,
    val failedMessages: Int,
    val scanDurationMs: Long,
    val dateRange: String
) {
    val successRate: Float
        get() = if (financialMessagesFound > 0) {
            (transactionsParsed.toFloat() / financialMessagesFound.toFloat()) * 100f
        } else 0f

    val creationRate: Float
        get() = if (transactionsParsed > 0) {
            (expensesCreated.toFloat() / transactionsParsed.toFloat()) * 100f
        } else 0f
}

// Made with Bob