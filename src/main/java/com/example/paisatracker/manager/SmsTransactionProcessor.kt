package com.example.paisatracker.manager

import android.content.Context
import android.util.Log
import com.example.paisatracker.data.BankAccount
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
    private val smsPreferences: SmsPreferences,
    private val merchantRuleRepository: com.example.paisatracker.data.MerchantRuleRepository,
    private val repository: com.example.paisatracker.data.PaisaTrackerRepository
) {
    companion object {
        private const val TAG = "SmsTransactionProcessor"
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
            return saveParsedTransaction(parsedTransaction, sender, body, timestamp)
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
        sender: String,
        smsBody: String,
        timestamp: Long
    ): ProcessingResult {
        return try {
            // Generate message hash from SMS content (sender + body + timestamp)
            val messageHash = generateMessageHash(sender, smsBody, timestamp)
            
            // Check 1: Check if this SMS was already processed (via bank_notifications)
            val existingNotification = bankNotificationRepository.getByHash(messageHash)
            if (existingNotification != null) {
                Log.d(TAG, "SMS already processed (notification exists): $messageHash")
                return ProcessingResult(false, reason = "Duplicate transaction")
            }
            
            // Check 2: Check for similar expense by amount, date, and merchant
            val existingExpense = expenseDao.findSimilarExpense(
                amount = parsedTransaction.amount.toDouble(),
                startTime = timestamp - 300000, // 5 minutes before
                endTime = timestamp + 300000,   // 5 minutes after
                description = parsedTransaction.merchant ?: "SMS Transaction"
            )
            
            if (existingExpense != null) {
                Log.d(TAG, "Similar expense already exists: ${existingExpense.id}")
                return ProcessingResult(false, reason = "Duplicate transaction")
            }

            // Handle different transaction types
            when (parsedTransaction.type) {
                TransactionType.EXPENSE -> {
                    // Check if auto-create is enabled
                    val autoCreate = smsPreferences.getAutoCreateExpenses()
                    
                    if (autoCreate) {
                        // Check if default category/project is set OR merchant rules are enabled
                        val hasDefaultCategory = smsPreferences.getDefaultCategoryId() != null
                        val useMerchantRules = smsPreferences.getUseMerchantRules()
                        
                        // Only auto-create if we have a valid destination (default or merchant rule)
                        if (hasDefaultCategory || useMerchantRules) {
                            // Auto-create mode: Create expense immediately
                            return createExpenseFromTransaction(parsedTransaction, sender, smsBody, timestamp, SmsTransactionStatus.AUTO_CREATED)
                        } else {
                            // No default set - save as pending instead
                            Log.d(TAG, "Auto-create enabled but no default category set. Saving as pending.")
                            return savePendingTransaction(parsedTransaction, sender, smsBody, timestamp)
                        }
                    } else {
                        // Manual mode: Save as pending for user confirmation
                        return savePendingTransaction(parsedTransaction, sender, smsBody, timestamp)
                    }
                }
                TransactionType.INCOME, TransactionType.CREDIT -> {
                    // Try to auto-process credit if bank account exists
                    Log.d(TAG, "Credit transaction detected: ${parsedTransaction.amount}")
                    return processAutomaticCredit(parsedTransaction, sender, smsBody, timestamp)
                }
                else -> {
                    Log.d(TAG, "Unsupported transaction type: ${parsedTransaction.type}")
                    return ProcessingResult(false, reason = "Unsupported transaction type: ${parsedTransaction.type}")
                }
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
        sender: String,
        smsBody: String,
        timestamp: Long
    ): ProcessingResult {
        return try {
            val messageHash = generateMessageHash(sender, smsBody, timestamp)
            
            // Create notification entity with parsed details
            val notification = BankNotificationEntity(
                packageName = "SMS",
                senderAlias = parsedTransaction.sender,
                messageBody = smsBody,
                messageHash = messageHash,
                postedAt = Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime(),
                processed = false,
                status = SmsTransactionStatus.PENDING,
                amount = parsedTransaction.amount.toDouble(),
                merchant = parsedTransaction.merchant,
                bankName = parsedTransaction.bankName,
                accountLast4 = parsedTransaction.accountLast4,
                transactionType = parsedTransaction.type.name
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
     * Saves credit/income transaction as pending for user review
     */
    private suspend fun savePendingCreditTransaction(
        parsedTransaction: ParsedTransaction,
        sender: String,
        smsBody: String,
        timestamp: Long
    ): ProcessingResult {
        return try {
            val messageHash = generateMessageHash(sender, smsBody, timestamp)
            
            // Create notification entity with parsed details
            val notification = BankNotificationEntity(
                packageName = "SMS",
                senderAlias = parsedTransaction.sender,
                messageBody = smsBody,
                messageHash = messageHash,
                postedAt = Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime(),
                processed = false,
                status = SmsTransactionStatus.CREDIT_PENDING,
                amount = parsedTransaction.amount.toDouble(),
                merchant = parsedTransaction.merchant ?: "Credit",
                bankName = parsedTransaction.bankName,
                accountLast4 = parsedTransaction.accountLast4,
                transactionType = parsedTransaction.type.name
            )
            
            val notificationId = bankNotificationRepository.insert(notification)
            
            if (notificationId != -1L) {
                Log.d(TAG, "Saved pending credit transaction with ID: $notificationId")
                return ProcessingResult(true, isPending = true, notificationId = notificationId)
            } else {
                Log.d(TAG, "Failed to save pending credit transaction")
                return ProcessingResult(false, reason = "Failed to save pending credit transaction")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving pending credit transaction: ${e.message}", e)
            return ProcessingResult(false, reason = e.message)
        }
    }

    /**
     * Automatically process credit transaction by updating bank account balance
     */
    private suspend fun processAutomaticCredit(
        parsedTransaction: ParsedTransaction,
        sender: String,
        smsBody: String,
        timestamp: Long
    ): ProcessingResult {
        return try {
            val accountLast4 = parsedTransaction.accountLast4
            
            // Try to find matching bank account
            val bankAccount = if (accountLast4 != null) {
                repository.findBankAccountByLast4(accountLast4)
            } else {
                null
            }
            
            if (bankAccount != null) {
                // Update bank balance automatically
                val newBalance = bankAccount.currentBalance + parsedTransaction.amount.toDouble()
                repository.updateBankAccountBalance(bankAccount.id, newBalance)
                
                Log.d(TAG, "Auto-processed credit: ${parsedTransaction.amount} to account ${bankAccount.name}")
                
                // Save notification as auto-processed
                val messageHash = generateMessageHash(sender, smsBody, timestamp)
                val notification = BankNotificationEntity(
                    packageName = "SMS",
                    senderAlias = parsedTransaction.sender,
                    messageBody = smsBody,
                    messageHash = messageHash,
                    postedAt = Instant.ofEpochMilli(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime(),
                    processed = true,
                    status = SmsTransactionStatus.AUTO_CREATED,
                    amount = parsedTransaction.amount.toDouble(),
                    merchant = parsedTransaction.merchant ?: "Credit",
                    bankName = parsedTransaction.bankName,
                    accountLast4 = parsedTransaction.accountLast4,
                    transactionType = parsedTransaction.type.name
                )
                
                val notificationId = bankNotificationRepository.insert(notification)
                return ProcessingResult(true, notificationId = notificationId)
            } else {
                // No matching account - save as pending
                Log.d(TAG, "No matching bank account found for credit, saving as pending")
                return savePendingCreditTransaction(parsedTransaction, sender, smsBody, timestamp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing automatic credit: ${e.message}", e)
            return ProcessingResult(false, reason = e.message)
        }
    }

    /**
     * Creates an expense from a parsed transaction
     * Now supports merchant rules for automatic categorization
     */
    private suspend fun createExpenseFromTransaction(
        parsedTransaction: ParsedTransaction,
        sender: String,
        smsBody: String,
        timestamp: Long,
        status: SmsTransactionStatus
    ): ProcessingResult {
        return try {
            // Check if merchant rules are enabled
            val useMerchantRules = smsPreferences.getUseMerchantRules()
            
            var categoryId: Long? = null
            var projectId: Long? = null
            
            // Try to find matching merchant rule if enabled
            if (useMerchantRules) {
                val matchingRule = merchantRuleRepository.findMatchingRule(parsedTransaction.merchant)
                if (matchingRule != null) {
                    categoryId = matchingRule.categoryId
                    projectId = matchingRule.projectId
                    // Increment match count for this rule
                    merchantRuleRepository.incrementMatchCount(matchingRule.id)
                    Log.d(TAG, "Applied merchant rule: ${matchingRule.merchantPattern} -> Category ID: $categoryId")
                }
            }
            
            // If no rule matched, use default category/project from settings
            if (categoryId == null) {
                categoryId = smsPreferences.getDefaultCategoryId()
                projectId = smsPreferences.getDefaultProjectId()
            }
            
            // Get the category - must exist (either from rule or default)
            val category = if (categoryId != null && categoryId != 0L) {
                categoryDao.getCategoryByIdSync(categoryId)
                    ?: throw IllegalStateException("Category with ID $categoryId not found")
            } else {
                // This should never happen due to the check in saveParsedTransaction
                throw IllegalStateException("No category ID available for auto-created expense")
            }
            
            // Try to find matching bank account for automatic debit
            val accountLast4 = parsedTransaction.accountLast4
            val bankAccount = if (accountLast4 != null) {
                repository.findBankAccountByLast4(accountLast4)
            } else {
                null
            }
            
            // Create expense with bank account link
            val expense = Expense(
                amount = parsedTransaction.amount.toDouble(),
                date = timestamp,
                description = parsedTransaction.merchant ?: "SMS Transaction",
                categoryId = category.id,
                paymentMethod = parsedTransaction.bankName,
                paymentIcon = "🏦",
                bankAccountId = bankAccount?.id
            )

            val expenseId = expenseDao.insert(expense)
            
            // Automatically deduct from bank account if found
            if (bankAccount != null && expenseId != -1L) {
                try {
                    repository.decrementBankAccountBalance(bankAccount.id, parsedTransaction.amount.toDouble())
                    Log.d(TAG, "Auto-debited ${parsedTransaction.amount} from account ${bankAccount.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to auto-debit from bank account: ${e.message}", e)
                }
            }
            
            if (expenseId != -1L) {
                Log.d(TAG, "Created expense with ID: $expenseId")
                
                // Log the notification with transaction ID using message hash
                val messageHash = generateMessageHash(sender, smsBody, timestamp)
                val notification = BankNotificationEntity(
                    packageName = "SMS",
                    senderAlias = parsedTransaction.sender,
                    messageBody = smsBody,
                    messageHash = messageHash,
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

            // Allow both PENDING and CREDIT_PENDING transactions to be confirmed
            if (notification.status != SmsTransactionStatus.PENDING &&
                notification.status != SmsTransactionStatus.CREDIT_PENDING) {
                return ProcessingResult(false, reason = "Transaction is not pending")
            }

            val amount = notification.amount ?: 0.0
            val isCredit = notification.status == SmsTransactionStatus.CREDIT_PENDING

            // Get category - must be provided by user when confirming
            val category = if (categoryId != null) {
                categoryDao.getCategoryByIdSync(categoryId)
                    ?: return ProcessingResult(false, reason = "Category not found")
            } else {
                return ProcessingResult(false, reason = "Category must be selected when confirming transaction")
            }

            // Create expense - use negative amount for credits (income)
            val expense = Expense(
                amount = if (isCredit) -amount else amount,  // Negative for credits
                date = notification.postedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                description = notification.merchant ?: "SMS Transaction",
                categoryId = category.id,
                paymentMethod = notification.bankName,
                paymentIcon = "🏦"
            )

            val expenseId = expenseDao.insert(expense)

            if (expenseId != -1L) {
                // Update bank account balance if account is found
                var bankAccount: BankAccount? = null
                
                // Try to find bank account by last4 digits first
                notification.accountLast4?.let { last4 ->
                    bankAccount = repository.findBankAccountByLast4(last4)
                    if (bankAccount != null) {
                        Log.d(TAG, "Found bank account by last4: ${bankAccount?.name}")
                    } else {
                        Log.d(TAG, "Bank account not found for last4: $last4")
                    }
                }
                
                // If not found by last4, try to find by bank name
                if (bankAccount == null && notification.bankName != null) {
                    val allAccounts = repository.getAllBankAccountsList()
                    bankAccount = allAccounts.firstOrNull {
                        it.bankName.equals(notification.bankName, ignoreCase = true)
                    }
                    if (bankAccount != null) {
                        Log.d(TAG, "Found bank account by bank name: ${bankAccount?.name}")
                    } else {
                        Log.d(TAG, "Bank account not found for bank name: ${notification.bankName}")
                    }
                }
                
                // Update balance if bank account found
                if (bankAccount != null) {
                    if (isCredit) {
                        // Credit: Add to bank account balance
                        repository.incrementBankAccountBalance(bankAccount.id, amount)
                        Log.d(TAG, "✅ Credited ₹$amount to bank account ${bankAccount.name} (New balance will be updated)")
                    } else {
                        // Debit: Subtract from bank account balance
                        repository.decrementBankAccountBalance(bankAccount.id, amount)
                        Log.d(TAG, "✅ Debited ₹$amount from bank account ${bankAccount.name} (New balance will be updated)")
                    }
                } else {
                    Log.w(TAG, "⚠️ Could not find bank account to update balance. accountLast4=${notification.accountLast4}, bankName=${notification.bankName}")
                }

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
     * Rejects a pending transaction and moves it to trash
     * @param notificationId ID of the pending notification
     */
    suspend fun rejectPendingTransaction(notificationId: Long): ProcessingResult {
        return try {
            val notification = bankNotificationRepository.getById(notificationId)
                ?: return ProcessingResult(false, reason = "Notification not found")

            if (notification.status != SmsTransactionStatus.PENDING) {
                return ProcessingResult(false, reason = "Transaction is not pending")
            }

            // Get trash retention days from preferences
            val retentionDays = smsPreferences.getTrashRetentionDays()

            // Move to trash with retention period
            bankNotificationRepository.moveToTrash(
                id = notificationId,
                retentionDays = retentionDays
            )

            Log.d(TAG, "Moved transaction $notificationId to trash (retention: $retentionDays days)")
            return ProcessingResult(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting transaction: ${e.message}", e)
            return ProcessingResult(false, reason = e.message)
        }
    }

    /**
     * Restores a transaction from trash back to pending
     * @param notificationId ID of the trashed notification
     */
    suspend fun restoreTransaction(notificationId: Long): ProcessingResult {
        return try {
            val notification = bankNotificationRepository.getById(notificationId)
                ?: return ProcessingResult(false, reason = "Notification not found")

            if (notification.status != SmsTransactionStatus.REJECTED) {
                return ProcessingResult(false, reason = "Transaction is not in trash")
            }

            // Restore to pending status
            bankNotificationRepository.restoreTransaction(notificationId)

            Log.d(TAG, "Restored transaction $notificationId from trash")
            return ProcessingResult(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring transaction: ${e.message}", e)
            return ProcessingResult(false, reason = e.message)
        }
    }
    /**
     * Generate a unique hash for the SMS message to detect duplicates
     * Uses sender + body + timestamp to create a unique identifier
     */
    private fun generateMessageHash(sender: String, body: String, timestamp: Long): String {
        val data = "$sender|$body|$timestamp"
        return hash(data)
    }

    private fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

// Made with Bob
