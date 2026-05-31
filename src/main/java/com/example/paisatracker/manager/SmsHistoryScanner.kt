package com.example.paisatracker.manager

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.paisatracker.data.ScanSummary
import com.example.paisatracker.data.SmsHistoryScanConfig
import com.example.paisatracker.data.SmsHistoryScanState
import com.example.paisatracker.data.SmsScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.coroutines.coroutineContext

/**
 * Scans SMS inbox for historical bank transaction messages
 * Provides progress updates and handles deduplication
 */
class SmsHistoryScanner(
    private val context: Context,
    private val smsTransactionProcessor: SmsTransactionProcessor
) {
    companion object {
        private const val TAG = "SmsHistoryScanner"
        private const val SMS_INBOX_URI = "content://sms/inbox"
        private const val BATCH_SIZE = 50
    }

    /**
     * Scans SMS inbox for transactions within the specified date range
     * Emits progress updates as SmsHistoryScanState
     */
    fun scanHistory(config: SmsHistoryScanConfig): Flow<SmsHistoryScanState> = flow {
        val startTime = System.currentTimeMillis()
        
        try {
            emit(SmsHistoryScanState.Idle)
            
            // Convert dates to timestamps
            val startTimestamp = config.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endTimestamp = config.endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            Log.d(TAG, "Starting SMS scan from ${config.startDate} to ${config.endDate}")
            
            // Read SMS messages from inbox
            val messages = readSmsInbox(startTimestamp, endTimestamp)
            
            if (messages.isEmpty()) {
                emit(SmsHistoryScanState.Completed(
                    totalMessages = 0,
                    foundTransactions = 0,
                    createdExpenses = 0,
                    skippedDuplicates = 0,
                    failedMessages = 0,
                    scanDurationMs = System.currentTimeMillis() - startTime
                ))
                return@flow
            }
            
            Log.d(TAG, "Found ${messages.size} messages in date range")
            
            // Process messages in batches
            var processedCount = 0
            var foundTransactions = 0
            var createdExpenses = 0
            var skippedDuplicates = 0
            var failedMessages = 0
            val allResults = mutableListOf<SmsScanResult>()
            
            for (batch in messages.chunked(config.batchSize)) {
                // Check if coroutine is still active
                if (!coroutineContext.isActive) {
                    emit(SmsHistoryScanState.Cancelled)
                    return@flow
                }
                
                for (message in batch) {
                    val result = processSmsMessage(message, config.autoCreateExpenses)
                    allResults.add(result)
                    
                    processedCount++
                    
                    when {
                        result.success && !result.isDuplicate -> {
                            foundTransactions++
                            if (result.expenseId != null) {
                                createdExpenses++
                            }
                        }
                        result.isDuplicate -> {
                            skippedDuplicates++
                        }
                        !result.success -> {
                            failedMessages++
                        }
                    }
                    
                    // Emit progress update
                    if (processedCount % config.progressUpdateInterval == 0 || processedCount == messages.size) {
                        emit(SmsHistoryScanState.Scanning(
                            totalMessages = messages.size,
                            processedMessages = processedCount,
                            foundTransactions = foundTransactions,
                            createdExpenses = createdExpenses,
                            skippedDuplicates = skippedDuplicates,
                            currentSender = message.sender
                        ))
                    }
                }
            }
            
            // Emit completion state
            val scanDuration = System.currentTimeMillis() - startTime
            emit(SmsHistoryScanState.Completed(
                totalMessages = messages.size,
                foundTransactions = foundTransactions,
                createdExpenses = createdExpenses,
                skippedDuplicates = skippedDuplicates,
                failedMessages = failedMessages,
                scanDurationMs = scanDuration,
                scanResults = allResults
            ))
            
            Log.d(TAG, "Scan completed: $foundTransactions transactions found, $createdExpenses expenses created, $skippedDuplicates duplicates skipped")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during SMS scan", e)
            emit(SmsHistoryScanState.Error(e.message ?: "Unknown error", e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Reads SMS messages from inbox within the specified time range
     */
    private suspend fun readSmsInbox(startTimestamp: Long, endTimestamp: Long): List<SmsMessage> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<SmsMessage>()
        
        try {
            val uri = Uri.parse(SMS_INBOX_URI)
            val projection = arrayOf("_id", "address", "body", "date")
            val selection = "date >= ? AND date <= ?"
            val selectionArgs = arrayOf(startTimestamp.toString(), endTimestamp.toString())
            val sortOrder = "date DESC"
            
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            
            cursor?.use {
                val addressIndex = it.getColumnIndex("address")
                val bodyIndex = it.getColumnIndex("body")
                val dateIndex = it.getColumnIndex("date")
                
                while (it.moveToNext()) {
                    val address = it.getString(addressIndex) ?: continue
                    val body = it.getString(bodyIndex) ?: continue
                    val date = it.getLong(dateIndex)
                    
                    // Only include messages from potential financial senders
                    if (isFinancialSender(address)) {
                        messages.add(SmsMessage(address, body, date))
                    }
                }
            }
            
            Log.d(TAG, "Read ${messages.size} financial SMS messages from inbox")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to read SMS", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error reading SMS inbox", e)
            throw e
        }
        
        return@withContext messages
    }

    /**
     * Processes a single SMS message
     */
    private suspend fun processSmsMessage(
        message: SmsMessage,
        autoCreate: Boolean
    ): SmsScanResult {
        return try {
            val result = smsTransactionProcessor.processAndSaveTransaction(
                sender = message.sender,
                body = message.body,
                timestamp = message.timestamp
            )
            
            SmsScanResult(
                sender = message.sender,
                body = message.body,
                timestamp = message.timestamp,
                success = result.success,
                isDuplicate = result.reason == "Duplicate transaction",
                expenseId = result.expenseId,
                notificationId = result.notificationId,
                errorMessage = if (!result.success) result.reason else null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing message from ${message.sender}", e)
            SmsScanResult(
                sender = message.sender,
                body = message.body,
                timestamp = message.timestamp,
                success = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Checks if sender is likely from a financial institution
     */
    private fun isFinancialSender(sender: String): Boolean {
        // DLT-registered senders in India
        if (sender.endsWith("-T") || sender.endsWith("-S")) {
            return true
        }
        
        // 6-digit alphanumeric codes (common for banks)
        if (sender.length == 6 && sender.all { it.isLetterOrDigit() }) {
            return true
        }
        
        // Common bank sender patterns
        val bankPatterns = listOf(
            "HDFC", "ICICI", "SBI", "AXIS", "KOTAK", "PAYTM", "GPAY", "PHONEPE",
            "AMAZON", "FLIPKART", "BANK", "CARD", "CREDIT", "DEBIT"
        )
        
        return bankPatterns.any { sender.contains(it, ignoreCase = true) }
    }

    /**
     * Generates a scan summary from the completed state
     */
    fun generateSummary(
        completedState: SmsHistoryScanState.Completed,
        config: SmsHistoryScanConfig
    ): ScanSummary {
        val dateRange = "${config.startDate} to ${config.endDate}"
        
        return ScanSummary(
            totalMessagesScanned = completedState.totalMessages,
            financialMessagesFound = completedState.totalMessages,
            transactionsParsed = completedState.foundTransactions,
            expensesCreated = completedState.createdExpenses,
            duplicatesSkipped = completedState.skippedDuplicates,
            failedMessages = completedState.failedMessages,
            scanDurationMs = completedState.scanDurationMs,
            dateRange = dateRange
        )
    }

    /**
     * Data class representing an SMS message
     */
    private data class SmsMessage(
        val sender: String,
        val body: String,
        val timestamp: Long
    )
}

// Made with Bob