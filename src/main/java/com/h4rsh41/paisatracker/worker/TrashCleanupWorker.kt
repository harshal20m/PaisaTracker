package com.h4rsh41.paisatracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.h4rsh41.paisatracker.data.BankNotificationRepository
import com.h4rsh41.paisatracker.data.PaisaTrackerDatabase
import com.h4rsh41.paisatracker.data.SmsPreferences

/**
 * Background worker that automatically deletes expired transactions from trash
 * Runs daily to clean up transactions that have exceeded their retention period
 */
class TrashCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "TrashCleanupWorker"
        const val WORK_NAME = "trash_cleanup_work"
        private const val NOTIFICATION_CHANNEL_ID = "trash_cleanup_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting trash cleanup...")

            val database = PaisaTrackerDatabase.getDatabase(applicationContext)
            val repository = BankNotificationRepository(database.bankNotificationDao())
            val preferences = SmsPreferences(applicationContext)

            // Get expired transactions
            val expiredTransactions = repository.getExpiredTrashedTransactions()
            
            if (expiredTransactions.isEmpty()) {
                Log.d(TAG, "No expired transactions to delete")
                return Result.success()
            }

            // Delete expired transactions
            val deletedCount = repository.deleteExpiredTransactions()
            
            Log.d(TAG, "Deleted $deletedCount expired transaction(s) from trash")

            // Show notification if enabled and items were deleted
            if (deletedCount > 0 && shouldShowNotification(preferences)) {
                showDeletionNotification(deletedCount)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during trash cleanup", e)
            // Retry on failure
            Result.retry()
        }
    }

    private fun shouldShowNotification(preferences: SmsPreferences): Boolean {
        // Only show notification if SMS notifications are enabled
        return preferences.getShowNotifications()
    }

    private fun showDeletionNotification(count: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Trash Cleanup",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for automatic trash cleanup"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open trash screen (you'll need to update this with actual intent)
        val intent = Intent().apply {
            // TODO: Add intent to open trash screen
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_delete) // TODO: Use app icon
            .setContentTitle("Trash Cleaned")
            .setContentText("$count transaction${if (count != 1) "s" else ""} automatically deleted from trash")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}

// Made with Bob