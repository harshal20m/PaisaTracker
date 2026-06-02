package com.example.paisatracker.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Project::class,
        Category::class,
        Expense::class,
        Asset::class,
        BackupMetadata::class,
        Budget::class,
        FlapData::class,
        SalaryRecord::class,
        ActionHistory::class,
        BankAccount::class,
        BankNotificationEntity::class,
        UnrecognizedSmsEntity::class,
        MerchantRuleEntity::class,
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PaisaTrackerDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun assetDao(): AssetDao
    abstract fun backupDao(): BackupDao
    abstract fun budgetDao(): BudgetDao
    abstract fun flapDao(): FlapDao
    abstract fun salaryRecordDao(): SalaryRecordDao
    abstract fun actionHistoryDao(): ActionHistoryDao
    abstract fun bankAccountDao(): BankAccountDao
    abstract fun bankNotificationDao(): BankNotificationDao
    abstract fun unrecognizedSmsDao(): UnrecognizedSmsDao
    abstract fun merchantRuleDao(): MerchantRuleDao
    
    companion object {
        @Volatile
        private var INSTANCE: PaisaTrackerDatabase? = null
        
        fun getDatabase(context: Context): PaisaTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PaisaTrackerDatabase::class.java,
                    "paisa_tracker_database_v2"  // New database name for fresh start
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()  // Allow destructive migration for fresh start
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        // ============================================================================
        // MIGRATIONS
        // ============================================================================
        
        /**
         * Migration from version 1 to 2: Add transaction_type field to bank_notifications
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add transaction_type column to bank_notifications table
                database.execSQL(
                    "ALTER TABLE bank_notifications ADD COLUMN transaction_type TEXT"
                )
            }
        }
        
        /**
         * Migration from version 2 to 3: Add accountNumberLast4 field to bank_accounts
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add accountNumberLast4 column to bank_accounts table
                database.execSQL(
                    "ALTER TABLE bank_accounts ADD COLUMN accountNumberLast4 TEXT"
                )
            }
        }
        
        // ============================================================================
        // PREVIOUS MIGRATIONS REMOVED - STARTED FRESH WITH VERSION 1
        // ============================================================================
        // 
        // ⚠️ WARNING: This will cause data loss for existing users!
        // 
        // This approach is ONLY suitable if:
        // 1. App is not yet released to production
        // 2. You can afford to lose all test data
        // 3. No users depend on the app
        // 4. You're in active development phase
        //
        // When users update the app:
        // - Room will detect schema mismatch
        // - fallbackToDestructiveMigration() will DROP all tables
        // - All data will be PERMANENTLY DELETED
        // - Fresh database will be created with version 1 schema
        //
        // Database name changed from "paisa_tracker_database_v1_3" to 
        // "paisa_tracker_database_v2" to force fresh start on all devices.
        //
        // Previous migrations (1-19) have been removed to simplify codebase.
        // ============================================================================
    }
}

// Made with Bob
