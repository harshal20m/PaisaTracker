package com.h4rsh41.paisatracker.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import com.h4rsh41.paisatracker.MainActivity
import com.h4rsh41.paisatracker.R
import com.h4rsh41.paisatracker.data.PaisaTrackerDatabase
import com.h4rsh41.paisatracker.data.BankNotificationRepository
import com.h4rsh41.paisatracker.data.SmsPreferences
import com.h4rsh41.paisatracker.data.UnrecognizedSmsRepository
import com.h4rsh41.paisatracker.manager.SmsTransactionProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that intercepts incoming SMS messages in real-time
 * and processes them for transaction data.
 */
class SmsBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsBroadcastReceiver"
        const val ACTION_VIEW_EXPENSE = "com.example.paisatracker.ACTION_VIEW_EXPENSE"
        const val ACTION_VIEW_PENDING = "com.example.paisatracker.ACTION_VIEW_PENDING"
        const val EXTRA_EXPENSE_ID = "expense_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val CHANNEL_ID = "sms_transaction_notifications"
        const val CHANNEL_NAME = "SMS Transaction Notifications"
    }

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) {
            return
        }

        // Combine multi-part SMS messages with their timestamps
        data class SmsData(val body: StringBuilder, var timestamp: Long)
        val smsMap = mutableMapOf<String, SmsData>()
        for (message in messages) {
            val sender = message.originatingAddress ?: continue
            val body = message.messageBody ?: continue
            val timestamp = message.timestampMillis

            val existing = smsMap.getOrPut(sender) { SmsData(StringBuilder(), timestamp) }
            existing.body.append(body)
            // Use the earliest timestamp for multi-part messages
            if (timestamp < existing.timestamp) {
                existing.timestamp = timestamp
            }
        }

        // Get database and create processor
        val database = PaisaTrackerDatabase.getDatabase(context)
        val bankNotificationRepo = BankNotificationRepository(database.bankNotificationDao())
        val unrecognizedSmsRepo = UnrecognizedSmsRepository(database.unrecognizedSmsDao())
        val merchantRuleRepo = com.h4rsh41.paisatracker.data.MerchantRuleRepository(
            database.merchantRuleDao(),
            context
        )
        val smsPreferences = SmsPreferences(context)
        val repository = com.h4rsh41.paisatracker.data.PaisaTrackerRepository(
            projectDao = database.projectDao(),
            categoryDao = database.categoryDao(),
            expenseDao = database.expenseDao(),
            assetDao = database.assetDao(),
            backupDao = database.backupDao(),
            budgetDao = database.budgetDao(),
            flapDao = database.flapDao(),
            salaryRecordDao = database.salaryRecordDao(),
            actionHistoryDao = database.actionHistoryDao(),
            bankAccountDao = database.bankAccountDao(),
            bankNotificationDao = database.bankNotificationDao(),
            accountTransactionDao = database.accountTransactionDao()
        )
        
        val processor = SmsTransactionProcessor(
            context = context,
            expenseDao = database.expenseDao(),
            categoryDao = database.categoryDao(),
            bankNotificationRepository = bankNotificationRepo,
            unrecognizedSmsRepository = unrecognizedSmsRepo,
            smsPreferences = smsPreferences,
            merchantRuleRepository = merchantRuleRepo,
            repository = repository
        )

        // Process each unique SMS
        for ((sender, smsData) in smsMap) {
            val body = smsData.body.toString()
            val timestamp = smsData.timestamp
            Log.d(TAG, "Received SMS from: $sender at timestamp: $timestamp")

            processIncomingSms(context, processor, sender, body, timestamp)
        }
    }

    private fun processIncomingSms(
        context: Context,
        processor: SmsTransactionProcessor,
        sender: String,
        body: String,
        timestamp: Long
    ) {
        receiverScope.launch {
            try {
                // Use the processor to parse and save the transaction
                val result = processor.processAndSaveTransaction(sender, body, timestamp)

                if (result.success) {
                    if (result.isPending && result.notificationId != null) {
                        Log.d(TAG, "Pending transaction saved with ID: ${result.notificationId}")
                        // Show notification for pending transaction
                        showPendingTransactionNotification(
                            context = context,
                            notificationId = result.notificationId,
                            sender = sender,
                            body = body
                        )
                    } else if (result.expenseId != null) {
                        Log.d(TAG, "Expense auto-created with ID: ${result.expenseId}")
                        // Show notification for auto-created expense
                        showTransactionNotification(
                            context = context,
                            expenseId = result.expenseId,
                            sender = sender,
                            body = body
                        )
                    }
                } else {
                    Log.d(TAG, "Transaction not saved: ${result.reason}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing SMS", e)
            }
        }
    }

    private fun showPendingTransactionNotification(
        context: Context,
        notificationId: Long,
        sender: String,
        body: String
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create notification channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for SMS-detected transactions"
            }
            notificationManager.createNotificationChannel(channel)

            // Create intent to open pending transactions screen
            val intent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_VIEW_PENDING
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Parse transaction details from SMS for clean notification
            val parser = com.pennywiseai.parser.core.bank.BankParserFactory.getParser(sender)
            val parsedTransaction = parser?.parse(body, sender, System.currentTimeMillis())
            
            val notificationText = if (parsedTransaction != null) {
                buildString {
                    append("₹${String.format("%.2f", parsedTransaction.amount)}")
                    parsedTransaction.merchant?.let { append(" at $it") }
                    append(" • ${parsedTransaction.bankName}")
                    append(" • ${if (parsedTransaction.type == com.pennywiseai.parser.core.TransactionType.EXPENSE) "Debited" else "Credited"}")
                }
            } else {
                "Tap to review transaction from $sender"
            }
            
            val expandedText = if (parsedTransaction != null) {
                buildString {
                    append("Amount: ₹${String.format("%.2f", parsedTransaction.amount)}\n")
                    parsedTransaction.merchant?.let { append("Merchant: $it\n") }
                    append("Bank: ${parsedTransaction.bankName}\n")
                    append("Type: ${if (parsedTransaction.type == com.pennywiseai.parser.core.TransactionType.EXPENSE) "Debited" else "Credited"}\n")
                    parsedTransaction.accountLast4?.let { append("Account: ****$it\n") }
                    append("\nTap to confirm this transaction")
                }
            } else {
                notificationText
            }

            // Build notification
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("💳 Transaction Detected - Confirm?")
                .setContentText(notificationText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(notificationId.toInt(), notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing pending notification", e)
        }
    }

    private fun showTransactionNotification(
        context: Context,
        expenseId: Long,
        sender: String,
        body: String
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create notification channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for SMS-detected transactions"
            }
            notificationManager.createNotificationChannel(channel)

            // Create intent to open expense detail
            val intent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_VIEW_EXPENSE
                putExtra(EXTRA_EXPENSE_ID, expenseId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                expenseId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Parse transaction details from SMS for clean notification
            val parser = com.pennywiseai.parser.core.bank.BankParserFactory.getParser(sender)
            val parsedTransaction = parser?.parse(body, sender, System.currentTimeMillis())
            
            val notificationText = if (parsedTransaction != null) {
                buildString {
                    append("₹${String.format("%.2f", parsedTransaction.amount)}")
                    parsedTransaction.merchant?.let { append(" at $it") }
                    append(" • ${parsedTransaction.bankName}")
                    append(" • ${if (parsedTransaction.type == com.pennywiseai.parser.core.TransactionType.EXPENSE) "Debited" else "Credited"}")
                }
            } else {
                "Transaction from $sender"
            }
            
            val expandedText = if (parsedTransaction != null) {
                buildString {
                    append("Amount: ₹${String.format("%.2f", parsedTransaction.amount)}\n")
                    parsedTransaction.merchant?.let { append("Merchant: $it\n") }
                    append("Bank: ${parsedTransaction.bankName}\n")
                    append("Type: ${if (parsedTransaction.type == com.pennywiseai.parser.core.TransactionType.EXPENSE) "Debited" else "Credited"}\n")
                    parsedTransaction.accountLast4?.let { append("Account: ****$it\n") }
                    append("\nExpense automatically created")
                }
            } else {
                notificationText
            }

            // Build notification
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("💸 Transaction Auto-Created")
                .setContentText(notificationText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(expenseId.toInt(), notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }
}

// Made with Bob
