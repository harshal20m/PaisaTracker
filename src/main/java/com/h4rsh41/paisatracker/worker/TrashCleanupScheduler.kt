package com.h4rsh41.paisatracker.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Utility class to schedule and manage the trash cleanup worker
 */
object TrashCleanupScheduler {
    private const val TAG = "TrashCleanupScheduler"

    /**
     * Schedule periodic trash cleanup work
     * Runs once daily to delete expired transactions
     */
    fun scheduleCleanup(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) // Only run when battery is not low
            .setRequiresDeviceIdle(false) // Can run while device is active
            .build()

        val cleanupRequest = PeriodicWorkRequestBuilder<TrashCleanupWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS) // Wait 1 hour before first run
            .addTag("trash_cleanup")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TrashCleanupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            cleanupRequest
        )

        Log.d(TAG, "Trash cleanup work scheduled (runs daily)")
    }

    /**
     * Cancel scheduled trash cleanup work
     */
    fun cancelCleanup(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(TrashCleanupWorker.WORK_NAME)
        
        Log.d(TAG, "Trash cleanup work cancelled")
    }

    /**
     * Trigger immediate cleanup (for testing or manual trigger)
     */
    fun triggerImmediateCleanup(context: Context) {
        val constraints = Constraints.Builder()
            .build()

        val cleanupRequest = OneTimeWorkRequestBuilder<TrashCleanupWorker>()
            .setConstraints(constraints)
            .addTag("trash_cleanup_immediate")
            .build()

        WorkManager.getInstance(context).enqueue(cleanupRequest)
        
        Log.d(TAG, "Immediate trash cleanup triggered")
    }

    /**
     * Check if cleanup work is scheduled
     */
    fun isCleanupScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(TrashCleanupWorker.WORK_NAME)
            .get()

        return workInfos.any { workInfo ->
            workInfo.state == WorkInfo.State.ENQUEUED || 
            workInfo.state == WorkInfo.State.RUNNING
        }
    }
}

// Made with Bob