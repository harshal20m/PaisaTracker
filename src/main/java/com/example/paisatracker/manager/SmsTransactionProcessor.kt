package com.example.paisatracker.manager

import android.content.Context
import android.util.Log
import com.example.paisatracker.data.BankNotificationEntity
import com.example.paisatracker.data.BankNotificationRepository
import com.example.paisatracker.data.Category
import com.example.paisatracker.data.CategoryDao
import com.example.paisatracker.data.Expense
import com.example.paisatracker.data.ExpenseDao
import com.example.paisatracker.data.SmsPreferences
import com.example.paisatracker.data.SmsTransactionStatus
import com.example.paisatracker.data.UnrecognizedSmsEntity
import com.example.paisatracker.data.UnrecognizedSmsRepository
import com.pennywiseai.parser.core.ParsedTransaction
import com.pennywiseai.parser.core.TransactionType
import com.pennywiseai.parser.core.bank.BankParserFactory
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Simplified SMS transaction processor for PaisaTracker.
 * Processes incoming SMS messages and creates expenses from bank notifications.
 */
class SmsTransactionProcessor(
    private val context: Context,
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val bankNotificationRepository: BankNotificationRepository,
    private val unrecognizedSmsRepository: UnrecognizedSmsRepository,
    private val smsPreferences: SmsPreferences
) {
    companion object {
        private const val TAG = "SmsTransactionProcessor"
        private const val DEFAULT_CATEGORY_NAME = "Others"
    }

    /**
     * Result of processing an SMS message
     */
    data class ProcessingResult(
        val success: Boolean,
        val expenseId: Long? = null,
        val notificationId: Long? = null,
        val isPending: Boolean = false,
        val reason: String? = null
    )

    /**
     * Parses and saves a transaction from an SMS message.
     *
     * @param sender SMS sender address
     * @param body SMS body text
     * @param timestamp SMS timestamp in milliseconds
     * @return ProcessingResult indicating success/failure and expense ID
     */
    suspend fun processAndSaveTransaction(
        sender: String,
        body: String,
        timestamp: Long
    ): ProcessingResult {
        try {
            // Get the appropriate parser for this sender
            val parser = BankParserFactory.getParser(sender)
            if (parser == null) {
                Log.d(TAG, "No parser found for sender: $sender")
                // Store as unrecognized if it looks like a financial SMS
                if (isFinancialSms(sender)) {
                    storeUnrecognizedSms(sender, body, timestamp)
                }
                return ProcessingResult(false, reason = "No parser found for sender: $sender")
            }

            // Parse the SMS
            val parsedTransaction = parser.parse(body, sender, timestamp)
            if (parsedTransaction == null) {
                Log.d(TAG, "Could not parse transaction from SMS")
                // Store as unrecognized
                if (isFinancialSms(sender)) {
                    storeUnrecognizedSms(sender, body, timestamp)
                }
                return ProcessingResult(false, reason = "Could not parse transaction from SMS")
            }

            Log.d(TAG, "Parsed transaction: ${parsedTransaction.amount} from ${parsedTransaction.bankName}")

            // Save the transaction as expense
            return saveParsedTransaction(parsedTransaction, body, timestamp)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing SMS", e)
            return ProcessingResult(false, reason = e.message)
        }
    }

    /**
     * Saves a parsed transaction - either as pending or auto-creates expense based on settings
     */
    private suspend fun saveParsedTransaction(
        parsedTransaction: ParsedTransaction,
        smsBody: String,
        timestamp: Long
    ): ProcessingResult {
        return try {
            // Check for duplicates using hash
            val transactionHash = generateTransactionHash(parsedTransaction, timestamp)
            val existingExpense = expenseDao.getExpenseByHash(transactionHash)
            
            if (existingExpense != null) {
                Log.d(TAG, "Transaction already exists: $transactionHash")
                return ProcessingResult(false, reason = "Duplicate transaction")
            }

            // Only process EXPENSE type transactions for now
            // Income and other types can be added later
            if (parsedTransaction.type != TransactionType.EXPENSE) {
                Log.d(TAG, "Skipping non-expense transaction: ${parsedTransaction.type}")
                return ProcessingResult(false, reason = "Only expense transactions are supported")
            }

            // Check if auto-create is enabled
            val autoCreate = smsPreferences.getAutoCreateExpenses()
            
            if (autoCreate) {
                // Auto-create mode: Create expense immediately
                return createExpenseFromTransaction(parsedTransaction, smsBody, timestamp, SmsTransactionStatus.AUTO_CREATED)
            } else {
                // Manual mode: Save as pending for user confirmation
                return savePendingTransaction(parsedTransaction, smsBody, timestamp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving transaction: ${e.message}", e)
            return ProcessingResult(false, reason = e.message)
        }
    }

    /**
     * Saves transaction as pending for manual confirmation
     */
    private suspend fun savePendingTransaction(
        parsedTransaction: ParsedTransaction,
        smsBody: String,
        timestamp: Long
    ): ProcessingResult {
        return try {
            val transactionHash = generateTransactionHash(parsedTransaction, timestamp)
            
            // Create notification entity with parsed details
            val notification = BankNotificationEntity(
                packageName = "SMS",
                senderAlias = parsedTransaction.sender,
                messageBody = smsBody,
                messageHash = transactionHash,
                postedAt = Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime(),
                processed = false,
                status = SmsTransactionStatus.PENDING,
                amount = parsedTransaction.amount.toDouble(),
                merchant = parsedTransaction.merchant,
                bankName = parsedTransaction.bankName,
                accountLast4 = parsedTransaction.accountLast4
            )
            
            val notificationId = bankNotificationRepository.insert(notification)
            
            if (notificationId != -1L) {
                Log.d(TAG, "Saved pending transaction with ID: $notificationId")
                return ProcessingResult(true, isPending = true, notificationId = notificationId)
            } else {
                Log.d(TAG, "Failed to save pending transaction")
                return ProcessingResult(false, reason = "Failed to save pending transaction")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving pending transaction: ${e.message}", e)
            return ProcessingResult(false, reason = e.message)
        }
    }

    /**
     * Creates an expense from a parsed transaction
     */
    private suspend fun createExpenseFromTransaction(
        parsedTransaction: ParsedTransaction,
        smsBody: String,
        timestamp: Long,
        status: SmsTransactionStatus
    ): ProcessingResult {
        return try {
            // Get or create category based on merchant name
            val categoryName = parsedTransaction.merchant ?: "Other"
            val category = getOrCreateCategory(categoryName)
            
            // Create expense
            val expense = Expense(
                amount = parsedTransaction.amount.toDouble(),
                date = timestamp,
                description = parsedTransaction.merchant ?: "SMS Transaction",
                categoryId = category.id,
                paymentMethod = parsedTransaction.bankName,
                paymentIcon = "🏦"
            )

            val expenseId = expenseDao.insert(expense)
            
            if (expenseId != -1L) {
                Log.d(TAG, "Created expense with ID: $expenseId")
                
                // Log the notification with transaction ID
                val transactionHash = generateTransactionHash(parsedTransaction, timestamp)
                val notification = BankNotificationEntity(
                    packageName = "SMS",
                    senderAlias = parsedTransaction.sender,
                    messageBody = smsBody,
                    messageHash = transactionHash,
                    postedAt = Instant.ofEpochMilli(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime(),
                    processed = true,
                    transactionId = expenseId,
                    status = status,
                    amount = parsedTransaction.amount.toDouble(),
                    merchant = parsedTransaction.merchant,
                    bankName = parsedTransaction.bankName,
                    accountLast4 = parsedTransaction.accountLast4
                )
                
                bankNotificationRepository.insert(notification)
                
                return ProcessingResult(true, expenseId = expenseId)
            } else {
                Log.d(TAG, "Failed to create expense")
                return ProcessingResult(false, reason = "Failed to create expense")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating expense: ${e.message}", e)
            return ProcessingResult(false, reason = e.message)
        }
    }

    /**
     * Get or create a category for the transaction
     */
    private suspend fun getOrCreateCategory(categoryName: String?): Category {
        val name = categoryName ?: DEFAULT_CATEGORY_NAME
        
        // Try to find existing category by name
        val existingCategory = categoryDao.getCategoryByName(name)
        if (existingCategory != null) {
            return existingCategory
        }

        // Get default project (first project or create one)
        val defaultProject = categoryDao.getDefaultProject() 
            ?: throw IllegalStateException("No default project found. Please create a project first.")

        // Create new category
        val newCategory = Category(
            name = name,
            projectId = defaultProject.id,
            emoji = getCategoryEmoji(name)
        )
        
        val categoryId = categoryDao.insert(newCategory)
        return newCategory.copy(id = categoryId)
    }

    /**
     * Get emoji for category based on name
     */
    private fun getCategoryEmoji(categoryName: String): String {
        return when (categoryName.lowercase()) {
            "food", "food & dining", "restaurant" -> "🍽️"
            "shopping", "retail" -> "🛍️"
            "transport", "transportation", "travel" -> "🚗"
            "entertainment" -> "🎬"
            "groceries", "grocery" -> "🛒"
            "health", "medical" -> "🏥"
            "utilities", "bills" -> "💡"
            "education" -> "📚"
            "fuel", "petrol" -> "⛽"
            "others" -> "📦"
            else -> "💰"
        }
    }

    /**
     * Generate a unique hash for the transaction to detect duplicates
     */
    private fun generateTransactionHash(transaction: ParsedTransaction, timestamp: Long): String {
        val data = "${transaction.bankName}|${transaction.amount}|${transaction.merchant}|${timestamp / 60000}"
        return hash(data)
    }

    /**
     * Check if SMS is from a financial institution
     */
    private fun isFinancialSms(sender: String): Boolean {
        // DLT-registered senders end with specific suffixes
        return sender.endsWith("-T") || // Transaction messages
               sender.endsWith("-S") || // Service messages  
               sender.length == 6 && sender.all { it.isLetterOrDigit() } // 6-digit alphanumeric
    }

    /**
     * Store unrecognized SMS for later review
     */
    private suspend fun storeUnrecognizedSms(sender: String, body: String, timestamp: Long) {
        try {
            // Check if already exists
            if (unrecognizedSmsRepository.exists(sender, body)) {
                return
            }

            val entity = UnrecognizedSmsEntity(
                sender = sender,
                smsBody = body,
                receivedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp),
                    ZoneId.systemDefault()
                )
            )
            
            unrecognizedSmsRepository.insert(entity)
            Log.d(TAG, "Stored unrecognized SMS from: $sender")
        } catch (e: Exception) {
            Log.e(TAG, "Error storing unrecognized SMS", e)
        }
    }


    /**
     * Confirms a pending transaction and creates an expense
     * @param notificationId ID of the pending notification
     * @param categoryId Optional category ID to override auto-detected category
     * @param projectId Optional project ID to override default project
     */
    suspend fun confirmPendingTransaction(
        notificationId: Long,
        categoryId: Long? = null,
        projectId: Long? = null
    ): ProcessingResult {
        return try {
            val notification = bankNotificationRepository.getById(notificationId)
                ?: return ProcessingResult(false, reason = "Notification not found")

            if (notification.status != SmsTransactionStatus.PENDING) {
                return ProcessingResult(false, reason = "Transaction is not pending")
            }

            // Get or create category
            val category = if (categoryId != null) {
                categoryDao.getCategoryByIdSync(categoryId)
                    ?: return ProcessingResult(false, reason = "Category not found")
            } else {
                getOrCreateCategory(notification.merchant)
            }

            // Create expense
            val expense = Expense(
                amount = notification.amount ?: 0.0,
                date = notification.postedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                description = notification.merchant ?: "SMS Transaction",
                categoryId = category.id,
                paymentMethod = notification.bankName,
                paymentIcon = "🏦"
            )

            val expenseId = expenseDao.insert(expense)

            if (expenseId != -1L) {
                // Update notification status
                val updatedNotification = notification.copy(
                    status = SmsTransactionStatus.CONFIRMED,
                    processed = true,
                    transactionId = expenseId
                )
                bankNotificationRepository.update(updatedNotification)

                Log.d(TAG, "Confirmed pending transaction $notificationId, created expense $expenseId")
                return ProcessingResult(true, expenseId = expenseId)
            } else {
                return ProcessingResult(false, reason = "Failed to create expense")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error confirming transaction: ${e.message}", e)
            return ProcessingResult(false, reason = e.message)
        }
    }

    /**
     * Rejects a pending transaction
     * @param notificationId ID of the pending notification
     */
    suspend fun rejectPendingTransaction(notificationId: Long): ProcessingResult {
        return try {
            val notification = bankNotificationRepository.getById(notificationId)
                ?: return ProcessingResult(false, reason = "Notification not found")

            if (notification.status != SmsTransactionStatus.PENDING) {
                return ProcessingResult(false, reason = "Transaction is not pending")
            }

            // Update notification status to rejected
            val updatedNotification = notification.copy(
                status = SmsTransactionStatus.REJECTED,
                processed = true
            )
            bankNotificationRepository.update(updatedNotification)

            Log.d(TAG, "Rejected pending transaction $notificationId")
            return ProcessingResult(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting transaction: ${e.message}", e)
            return ProcessingResult(false, reason = e.message)
        }
    }
    private fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

// Made with Bob
