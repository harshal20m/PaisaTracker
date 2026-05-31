package com.example.paisatracker.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.paisatracker.data.BackupMetadata
import com.example.paisatracker.data.PaisaTrackerDatabase
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
            val dbPath = context.getDatabasePath("paisa_tracker_database_v2").absolutePath
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
                    zipOut.write("1".toByteArray()) // Current schema version
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

            val dbPath = context.getDatabasePath("paisa_tracker_database_v2").absolutePath
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

            // Open database - no migrations needed since we're at version 1
            try {
                val restoredDb = Room.databaseBuilder(
                    context.applicationContext,
                    PaisaTrackerDatabase::class.java,
                    "paisa_tracker_database_v2"
                )
                    .fallbackToDestructiveMigration() // Allow destructive migration for fresh start
                    .build()

                // Force database to open and run migrations
                restoredDb.openHelper.writableDatabase
                
                // Verify database is accessible
                val expenseCount = restoredDb.expenseDao().getExpenseCount()
                Log.i(TAG, "Database restored successfully with $expenseCount expenses")
                
                restoredDb.close()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open restored database", e)
                return@withContext false
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
