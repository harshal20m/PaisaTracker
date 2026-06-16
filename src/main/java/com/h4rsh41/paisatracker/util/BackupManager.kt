package com.h4rsh41.paisatracker.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Room
import com.h4rsh41.paisatracker.data.BackupMetadata
import com.h4rsh41.paisatracker.data.PaisaTrackerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context) {

    private val database = PaisaTrackerDatabase.getDatabase(context)
    private val TAG = "BackupManager"

    /**
     * Create a full database backup (ZIP file containing database + assets)
     * Returns the BackupMetadata if successful, null otherwise
     */
    suspend fun createFullBackup(destinationUri: Uri): BackupMetadata? = withContext(Dispatchers.IO) {
        try {
            // Get database file - MUST match your actual database name
            val dbPath = context.getDatabasePath("paisa_tracker_database").absolutePath
            val dbFile = File(dbPath)

            if (!dbFile.exists()) {
                Log.e(TAG, "Database file not found: $dbPath")
                return@withContext null
            }

            // Get asset files directory
            val assetDir = File(context.filesDir, "expense_assets")

            // Get current database instance for stats (don't close it)
            val db = PaisaTrackerDatabase.getDatabase(context)
            val projectCount = db.projectDao().getProjectCount()
            val categoryCount = db.categoryDao().getCategoryCount()
            val expenseCount = db.expenseDao().getExpenseCount()
            val totalAmount = db.expenseDao().getTotalAmount() ?: 0.0

            // Create ZIP file (even though we use .backup extension, it's a zip internally)
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zipOut ->

                    // Checkpoint WAL file before copying (ensures data is written to main db file)
                    try {
                        db.query("PRAGMA wal_checkpoint(FULL)", null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to checkpoint WAL", e)
                    }

                    // Add database file to ZIP
                    FileInputStream(dbFile).use { fis ->
                        zipOut.putNextEntry(ZipEntry("database.db"))
                        fis.copyTo(zipOut)
                        zipOut.closeEntry()
                    }

                    // Add WAL file if exists
                    val walFile = File("${dbFile.absolutePath}-wal")
                    if (walFile.exists()) {
                        FileInputStream(walFile).use { fis ->
                            zipOut.putNextEntry(ZipEntry("database.db-wal"))
                            fis.copyTo(zipOut)
                            zipOut.closeEntry()
                        }
                    }

                    // Add SHM file if exists
                    val shmFile = File("${dbFile.absolutePath}-shm")
                    if (shmFile.exists()) {
                        FileInputStream(shmFile).use { fis ->
                            zipOut.putNextEntry(ZipEntry("database.db-shm"))
                            fis.copyTo(zipOut)
                            zipOut.closeEntry()
                        }
                    }

                    // Add asset files to ZIP if directory exists
                    if (assetDir.exists() && assetDir.isDirectory) {
                        assetDir.listFiles()?.forEach { assetFile ->
                            if (assetFile.isFile) {
                                FileInputStream(assetFile).use { fis ->
                                    zipOut.putNextEntry(ZipEntry("assets/${assetFile.name}"))
                                    fis.copyTo(zipOut)
                                    zipOut.closeEntry()
                                }
                            }
                        }
                    }

                    // Add schema version metadata
                    zipOut.putNextEntry(ZipEntry("schema_version.txt"))
                    zipOut.write("14".toByteArray()) // Current schema version
                    zipOut.closeEntry()
                }
            }

            // Get file size
            val fileSize = getFileSize(destinationUri)

            // Create metadata
            val timestamp = System.currentTimeMillis()
            val fileName = "PaisaTracker_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(timestamp))}.backup"

            val metadata = BackupMetadata(
                fileName = fileName,
                filePath = destinationUri.toString(),
                fileSize = fileSize,
                timestamp = timestamp,
                projectCount = projectCount,
                categoryCount = categoryCount,
                expenseCount = expenseCount,
                totalAmount = totalAmount
            )

            // Save metadata to database
            db.backupDao().insertBackup(metadata)

            Log.i(TAG, "Backup created successfully: $fileName")
            metadata

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create backup", e)
            null
        }
    }

    /**
     * Restore database from backup file with automatic migration support
     * Returns true if successful, false otherwise
     */
    suspend fun restoreFromBackup(sourceUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Starting restore from backup...")

            // Close current database
            database.close()

            val dbPath = context.getDatabasePath("paisa_tracker_database").absolutePath
            val dbFile = File(dbPath)
            val tempDbFile = File(dbPath + ".temp")
            val assetDir = File(context.filesDir, "expense_assets")

            // Create asset directory if it doesn't exist
            if (!assetDir.exists()) {
                assetDir.mkdirs()
            }

            // Extract ZIP file content from the backup file to temp location
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var entry: ZipEntry? = zipIn.nextEntry

                    while (entry != null) {
                        when {
                            entry.name == "database.db" -> {
                                // Restore database file to temp location
                                FileOutputStream(tempDbFile).use { fos ->
                                    zipIn.copyTo(fos)
                                }
                                Log.i(TAG, "Extracted database to temp location")
                            }
                            entry.name == "database.db-wal" -> {
                                // Skip WAL file - will be recreated
                                Log.i(TAG, "Skipping WAL file")
                            }
                            entry.name == "database.db-shm" -> {
                                // Skip SHM file - will be recreated
                                Log.i(TAG, "Skipping SHM file")
                            }
                            entry.name.startsWith("assets/") -> {
                                // Restore asset file
                                val fileName = entry.name.substringAfter("assets/")
                                val assetFile = File(assetDir, fileName)
                                FileOutputStream(assetFile).use { fos ->
                                    zipIn.copyTo(fos)
                                }
                                Log.i(TAG, "Restored asset: $fileName")
                            }
                            entry.name == "schema_version.txt" -> {
                                // Read schema version (for future use)
                                val version = zipIn.bufferedReader().readText()
                                Log.i(TAG, "Backup schema version: $version")
                            }
                        }

                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            }

            // Delete old WAL and SHM files
            File("${dbFile.absolutePath}-wal").delete()
            File("${dbFile.absolutePath}-shm").delete()

            // Move temp database to actual location
            if (tempDbFile.exists()) {
                dbFile.delete()
                tempDbFile.renameTo(dbFile)
                Log.i(TAG, "Moved temp database to final location")
            } else {
                Log.e(TAG, "Temp database file not found")
                return@withContext false
            }

            // Open database with migrations to upgrade old backups
            // Try with migrations first, fallback to manual extraction if migration fails
            try {
                val restoredDb = Room.databaseBuilder(
                    context.applicationContext,
                    PaisaTrackerDatabase::class.java,
                    "paisa_tracker_database"
                )
                    .addMigrations(
                        PaisaTrackerDatabase.MIGRATION_1_2,
                        PaisaTrackerDatabase.MIGRATION_2_3,
                        PaisaTrackerDatabase.MIGRATION_3_4,
                        PaisaTrackerDatabase.MIGRATION_4_5,
                        PaisaTrackerDatabase.MIGRATION_5_6,
                        PaisaTrackerDatabase.MIGRATION_6_7,
                        PaisaTrackerDatabase.MIGRATION_7_8,
                        PaisaTrackerDatabase.MIGRATION_8_9,
                        PaisaTrackerDatabase.MIGRATION_9_10,
                        PaisaTrackerDatabase.MIGRATION_10_11,
                        PaisaTrackerDatabase.MIGRATION_11_12,
                        PaisaTrackerDatabase.MIGRATION_12_13,
                        PaisaTrackerDatabase.MIGRATION_13_14,
                        PaisaTrackerDatabase.MIGRATION_14_15,
                        PaisaTrackerDatabase.MIGRATION_15_16
                    )
                    .build()

                // Force database to open and run migrations
                restoredDb.openHelper.writableDatabase

                // Verify database is accessible
                val expenseCount = restoredDb.expenseDao().getExpenseCount()
                Log.i(TAG, "Database restored successfully with $expenseCount expenses")

                restoredDb.close()
            } catch (migrationError: Exception) {
                Log.e(TAG, "Migration failed, attempting data extraction from old backup", migrationError)

                // If migration fails, try to extract data manually from old schema
                try {
                    val success = extractDataFromOldBackup(dbFile)
                    if (!success) {
                        Log.e(TAG, "Failed to extract data from old backup")
                        return@withContext false
                    }
                    Log.i(TAG, "Successfully extracted data from old backup format")
                } catch (extractError: Exception) {
                    Log.e(TAG, "Failed to extract data from old backup", extractError)
                    return@withContext false
                }
            }

            // Reopen database with normal instance
            PaisaTrackerDatabase.getDatabase(context)

            Log.i(TAG, "Restore completed successfully")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore backup", e)
            // Reopen database
            try {
                PaisaTrackerDatabase.getDatabase(context)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to reopen database after restore failure", ex)
            }
            false
        }
    }

    /**
     * Extract data from old backup format when migration fails.
     * This handles backups from unknown/very old schema versions by reading
     * the raw SQLite tables directly and re-inserting rows through the DAOs
     * of a fresh, current-schema database.
     */
    private suspend fun extractDataFromOldBackup(oldDbFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Attempting to extract data from old backup format...")

            // Create a new database with current schema
            val newDb = PaisaTrackerDatabase.getDatabase(context)

            // Open old database directly with SQLite
            val oldDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                oldDbFile.absolutePath,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READONLY
            )

            try {
                // Extract Projects (core table, should exist in all versions)
                val projectsCursor = oldDb.rawQuery(
                    "SELECT id, name, emoji, createdAt, lastModified, isCompleted FROM projects",
                    null
                )

                val projectIdMap = mutableMapOf<Long, Long>() // old ID -> new ID

                projectsCursor.use { cursor ->
                    while (cursor.moveToNext()) {
                        val oldId = cursor.getLong(0)
                        val name = cursor.getString(1)
                        val emoji = cursor.getStringOrNull(2) ?: "📁"
                        val createdAt = cursor.getLong(3)
                        val lastModified = cursor.getLong(4)
                        val isCompleted = cursor.getInt(5) == 1

                        val project = com.h4rsh41.paisatracker.data.Project(
                            id = 0, // Auto-generate new ID
                            name = name,
                            emoji = emoji,
                            createdAt = createdAt,
                            lastModified = lastModified,
                            isCompleted = isCompleted,
                            includeInSalary = true // Default for old backups
                        )

                        val newId = newDb.projectDao().insertProject(project)
                        projectIdMap[oldId] = newId
                        Log.d(TAG, "Extracted project: $name (old ID: $oldId -> new ID: $newId)")
                    }
                }

                // Extract Categories
                val categoriesCursor = oldDb.rawQuery(
                    "SELECT id, name, projectId, createdAt FROM categories",
                    null
                )

                val categoryIdMap = mutableMapOf<Long, Long>()

                categoriesCursor.use { cursor ->
                    while (cursor.moveToNext()) {
                        val oldId = cursor.getLong(0)
                        val name = cursor.getString(1)
                        val oldProjectId = cursor.getLong(2)
                        val createdAt = cursor.getLong(3)

                        val newProjectId = projectIdMap[oldProjectId] ?: continue

                        val category = com.h4rsh41.paisatracker.data.Category(
                            id = 0,
                            name = name,
                            projectId = newProjectId,
                            emoji = "▶️", // Default for old backups
                            createdAt = createdAt
                        )

                        val newId = newDb.categoryDao().insertCategory(category)
                        categoryIdMap[oldId] = newId
                        Log.d(TAG, "Extracted category: $name")
                    }
                }

                // Extract Expenses
                val expensesCursor = oldDb.rawQuery(
                    "SELECT id, amount, date, description, categoryId, assetPath FROM expenses",
                    null
                )

                var expenseCount = 0
                expensesCursor.use { cursor ->
                    while (cursor.moveToNext()) {
                        val amount = cursor.getDouble(1)
                        val date = cursor.getLong(2)
                        val description = cursor.getString(3)
                        val oldCategoryId = cursor.getLong(4)
                        val assetPath = cursor.getStringOrNull(5)

                        val newCategoryId = categoryIdMap[oldCategoryId] ?: continue

                        val expense = com.h4rsh41.paisatracker.data.Expense(
                            id = 0,
                            amount = amount,
                            date = date,
                            description = description,
                            categoryId = newCategoryId,
                            assetPath = assetPath,
                            paymentMethod = null, // New field, set to null
                            paymentIcon = null,
                            bankAccountId = null
                        )

                        newDb.expenseDao().insertExpense(expense)
                        expenseCount++
                    }
                }

                Log.i(TAG, "Successfully extracted $expenseCount expenses from old backup")

                // Try to extract budgets if table exists
                try {
                    val budgetsCursor = oldDb.rawQuery(
                        "SELECT name, emoji, limitAmount, period, categoryId, projectId, createdAt, trackingStartAt, isActive FROM budgets",
                        null
                    )

                    budgetsCursor.use { cursor ->
                        while (cursor.moveToNext()) {
                            val name = cursor.getString(0)
                            val emoji = cursor.getStringOrNull(1) ?: "💰"
                            val limitAmount = cursor.getDouble(2)
                            val period = cursor.getString(3)
                            val oldCategoryId = cursor.getLongOrNull(4)
                            val oldProjectId = cursor.getLongOrNull(5)
                            val createdAt = cursor.getLong(6)
                            val trackingStartAt = cursor.getLong(7)
                            val isActive = cursor.getInt(8) == 1

                            val newCategoryId = oldCategoryId?.let { categoryIdMap[it] }
                            val newProjectId = oldProjectId?.let { projectIdMap[it] }

                            val budget = com.h4rsh41.paisatracker.data.Budget(
                                id = 0,
                                name = name,
                                emoji = emoji,
                                limitAmount = limitAmount,
                                period = com.h4rsh41.paisatracker.data.BudgetPeriod.valueOf(period),
                                categoryId = newCategoryId,
                                projectId = newProjectId,
                                createdAt = createdAt,
                                trackingStartAt = trackingStartAt,
                                isActive = isActive
                            )

                            newDb.budgetDao().insertBudget(budget)
                        }
                    }
                    Log.i(TAG, "Extracted budgets from old backup")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not extract budgets (table may not exist in old backup)", e)
                }

                oldDb.close()
                return@withContext true

            } catch (e: Exception) {
                Log.e(TAG, "Error during data extraction", e)
                oldDb.close()
                return@withContext false
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to open old database for extraction", e)
            return@withContext false
        }
    }

    /**
     * Helper extension to get nullable string from cursor
     */
    private fun android.database.Cursor.getStringOrNull(columnIndex: Int): String? {
        return if (isNull(columnIndex)) null else getString(columnIndex)
    }

    /**
     * Helper extension to get nullable long from cursor
     */
    private fun android.database.Cursor.getLongOrNull(columnIndex: Int): Long? {
        return if (isNull(columnIndex)) null else getLong(columnIndex)
    }

    /**
     * Delete backup file from storage and database record.
     * Record is removed from database even if file deletion fails.
     */
    suspend fun deleteBackupFile(backup: BackupMetadata): Boolean = withContext(Dispatchers.IO) {
        // Attempt to delete the physical file
        try {
            val uri = Uri.parse(backup.filePath)
            context.contentResolver.delete(uri, null, null)
            Log.i(TAG, "Deleted backup file: ${backup.fileName}")
        } catch (e: Exception) {
            // Log file deletion error but proceed to remove record from DB
            Log.e(TAG, "Failed to delete backup file", e)
        }

        // Always remove from database
        try {
            database.backupDao().deleteBackup(backup)
            Log.i(TAG, "Removed backup metadata: ${backup.fileName}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove backup metadata", e)
            false
        }
    }

    /**
     * Get file size from URI
     */
    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.statSize
            } ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get file size", e)
            0L
        }
    }

    /**
     * Format file size for display
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }
}

// Made with Bob
