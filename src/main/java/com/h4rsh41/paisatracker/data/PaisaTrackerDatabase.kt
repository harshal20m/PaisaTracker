package com.h4rsh41.paisatracker.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// ============================================================================
// @AI_AGENT_DATABASE_CHECKPOINT
// ============================================================================
// CRITICAL: Before modifying this file, read DATABASE_AGENT_GUIDE.md
//
// Key Rules:
// 1. NEVER use fallbackToDestructiveMigration() - it deletes user data
// 2. ALWAYS increment version when changing schema
// 3. ALWAYS create migration for version changes
// 4. ALWAYS make migrations idempotent (safe to run multiple times)
// 5. ALWAYS match Entity definitions to database schema exactly
//
// Current Version: 15
// Migration Path: v1 → v2 → v3 → ... → v15
// Total Tables: 13
//
// See DATABASE_AGENT_GUIDE.md for complete guidelines
// ============================================================================

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
        AccountTransaction::class,
    ],
    version = 16,  // @AI_AGENT_DATABASE_CHECKPOINT - Increment this when changing schema
    exportSchema = true  // @AI_AGENT_DATABASE_CHECKPOINT - Keep this enabled for tracking
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
    abstract fun accountTransactionDao(): AccountTransactionDao
    
    companion object {
        @Volatile
        private var INSTANCE: PaisaTrackerDatabase? = null
        
        fun getDatabase(context: Context): PaisaTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PaisaTrackerDatabase::class.java,
                    "paisa_tracker_database"
                )
                    .addMigrations(
                        // @AI_AGENT_DATABASE_CHECKPOINT - Add new migrations here
                        // When incrementing version, create MIGRATION_X_Y and add it to this list
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_14_15,
                        MIGRATION_15_16
                        // Add new migrations here: MIGRATION_16_17, etc.
                    )
                    // @AI_AGENT_DATABASE_CHECKPOINT - NEVER uncomment this line!
                    // FORBIDDEN: .fallbackToDestructiveMigration() - This deletes ALL user data!
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        // ============================================================================
        // @AI_AGENT_DATABASE_CHECKPOINT - MIGRATIONS SECTION
        // ============================================================================
        // CRITICAL: All migrations must preserve user data
        //
        // Migration Checklist:
        // ✅ Use try-catch for ALTER TABLE (idempotent)
        // ✅ Use IF NOT EXISTS for CREATE TABLE/INDEX
        // ✅ Add android.util.Log statements for debugging
        // ✅ Test migration on device with existing data
        // ✅ Update DATABASE_MIGRATION_GUIDE.md
        //
        // Example Migration Template:
        // val MIGRATION_X_Y = object : Migration(X, Y) {
        //     override fun migrate(database: SupportSQLiteDatabase) {
        //         android.util.Log.d("Migration_X_Y", "Starting migration")
        //         try {
        //             database.execSQL("ALTER TABLE table_name ADD COLUMN new_field TEXT")
        //         } catch (e: Exception) {
        //             android.util.Log.w("Migration_X_Y", "Column exists: ${e.message}")
        //         }
        //     }
        // }
        // ============================================================================
        
        /**
         * Migration 1→2: Add transaction_type to bank_notifications
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL(
                        "ALTER TABLE bank_notifications ADD COLUMN transaction_type TEXT"
                    )
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
            }
        }
        
        /**
         * Migration 2→3: Add accountNumberLast4 to bank_accounts
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL(
                        "ALTER TABLE bank_accounts ADD COLUMN accountNumberLast4 TEXT"
                    )
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
            }
        }
        
        /**
         * Migration 3→4: Add payment method fields to expenses
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("ALTER TABLE expenses ADD COLUMN paymentMethod TEXT")
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
                try {
                    database.execSQL("ALTER TABLE expenses ADD COLUMN paymentIcon TEXT")
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
            }
        }
        
        /**
         * Migration 4→5: Add bankAccountId to expenses with foreign key
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add column
                try {
                    database.execSQL("ALTER TABLE expenses ADD COLUMN bankAccountId INTEGER")
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
                
                // Create index for foreign key (IF NOT EXISTS is safe)
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_expenses_bankAccountId ON expenses(bankAccountId)"
                )
            }
        }
        
        /**
         * Migration 5→6: Add includeInSalary to projects
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL(
                        "ALTER TABLE projects ADD COLUMN includeInSalary INTEGER NOT NULL DEFAULT 1"
                    )
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
            }
        }
        
        /**
         * Migration 6→7: Add emoji to categories
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL(
                        "ALTER TABLE categories ADD COLUMN emoji TEXT NOT NULL DEFAULT '▶️'"
                    )
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
            }
        }
        
        /**
         * Migration 7→8: Add expenseId to assets
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("ALTER TABLE assets ADD COLUMN expenseId INTEGER")
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
            }
        }
        
        /**
         * Migration 8→9: Add new fields to salary_records
         */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL(
                        "ALTER TABLE salary_records ADD COLUMN linkedAccountId INTEGER NOT NULL DEFAULT 0"
                    )
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
                try {
                    database.execSQL(
                        "ALTER TABLE salary_records ADD COLUMN sourceType TEXT NOT NULL DEFAULT 'PRIMARY'"
                    )
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
                try {
                    database.execSQL(
                        "ALTER TABLE salary_records ADD COLUMN sourceName TEXT NOT NULL DEFAULT ''"
                    )
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
                try {
                    database.execSQL(
                        "ALTER TABLE salary_records ADD COLUMN autoGenerated INTEGER NOT NULL DEFAULT 0"
                    )
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
                try {
                    database.execSQL(
                        "ALTER TABLE salary_records ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1"
                    )
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
                try {
                    database.execSQL(
                        "ALTER TABLE salary_records ADD COLUMN recurringAccountId INTEGER"
                    )
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
            }
        }
        
        /**
         * Migration 9→10: Add trash-related fields to bank_notifications
         */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("ALTER TABLE bank_notifications ADD COLUMN rejectedAt TEXT")
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
                try {
                    database.execSQL("ALTER TABLE bank_notifications ADD COLUMN deletion_scheduled_at TEXT")
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
                try {
                    database.execSQL("ALTER TABLE bank_notifications ADD COLUMN trash_retention_days INTEGER")
                } catch (e: Exception) {
                    // Column already exists, safe to ignore
                }
                
                // Create index for deletion_scheduled_at (IF NOT EXISTS is safe)
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_bank_notifications_deletion_scheduled_at ON bank_notifications(deletion_scheduled_at)"
                )
            }
        }
        
        /**
         * Migration 10→11: Create unrecognized_sms table
         */
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS unrecognized_sms (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sender TEXT NOT NULL,
                        sms_body TEXT NOT NULL,
                        received_at TEXT NOT NULL,
                        reported INTEGER NOT NULL DEFAULT 0,
                        is_deleted INTEGER NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL
                    )
                """)
                
                // Create unique index
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_unrecognized_sms_sender_sms_body ON unrecognized_sms(sender, sms_body)"
                )
            }
        }
        
        /**
         * Migration 11→12: Create merchant_rules table
         */
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS merchant_rules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        merchant_pattern TEXT NOT NULL,
                        category_id INTEGER NOT NULL,
                        project_id INTEGER,
                        priority INTEGER NOT NULL DEFAULT 0,
                        is_active INTEGER NOT NULL DEFAULT 1,
                        match_count INTEGER NOT NULL DEFAULT 0,
                        last_matched_at TEXT,
                        created_at TEXT NOT NULL
                    )
                """)
                
                // Create indices
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_merchant_rules_merchant_pattern ON merchant_rules(merchant_pattern)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_merchant_rules_priority ON merchant_rules(priority)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_merchant_rules_is_active ON merchant_rules(is_active)"
                )
            }
        }
        
        /**
         * Migration 12→13: Ensure all bank_accounts fields exist
         * (In case table was created in older version without all fields)
         */
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Check and add missing columns if they don't exist
                // SQLite doesn't have IF NOT EXISTS for ALTER TABLE, so we use try-catch approach
                try {
                    database.execSQL("ALTER TABLE bank_accounts ADD COLUMN accountNumberLast4 TEXT")
                } catch (e: Exception) {
                    // Column already exists, ignore
                }
            }
        }
        
        /**
         * Migration 13→14: Final validation and index optimization
         */
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Ensure all critical indices exist for performance
                
                // Expenses indices
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_expenses_categoryId ON expenses(categoryId)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_expenses_bankAccountId ON expenses(bankAccountId)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_expenses_date ON expenses(date)"
                )
                
                // Categories index
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_categories_projectId ON categories(projectId)"
                )
                
                // Bank notifications indices
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_bank_notifications_status ON bank_notifications(status)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_bank_notifications_deletion_scheduled_at ON bank_notifications(deletion_scheduled_at)"
                )
                
                // Ensure unique constraint on bank_notifications
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_bank_notifications_package_name_message_hash ON bank_notifications(package_name, message_hash)"
                )
            }
        }
        
        /**
         * Migration 14→15: Clean up duplicate rejectedAt column in bank_notifications
         *
         * ISSUE: Some databases have both 'rejectedAt' (old camelCase) and 'rejected_at' (correct snake_case)
         * SOLUTION: Check if duplicate exists, then recreate table with correct schema
         *
         * This migration is SAFE because:
         * 1. Checks for column existence before acting
         * 2. Preserves all data using COALESCE
         * 3. Recreates all indices
         * 4. Logs all steps for debugging
         */
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                android.util.Log.d("Migration_14_15", "Starting migration to fix bank_notifications schema")
                
                // Check if the duplicate rejectedAt column exists
                val cursor = database.query("PRAGMA table_info(bank_notifications)")
                val columnNames = mutableListOf<String>()
                
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    columnNames.add(columnName)
                }
                cursor.close()
                
                android.util.Log.d("Migration_14_15", "Found columns: ${columnNames.joinToString(", ")}")
                
                val hasRejectedAtCamelCase = columnNames.contains("rejectedAt")
                val hasRejectedAtSnakeCase = columnNames.contains("rejected_at")
                
                android.util.Log.d("Migration_14_15", "hasRejectedAtCamelCase: $hasRejectedAtCamelCase, hasRejectedAtSnakeCase: $hasRejectedAtSnakeCase")
                
                // Only proceed if we have the duplicate column issue
                if (hasRejectedAtCamelCase) {
                    android.util.Log.d("Migration_14_15", "Duplicate rejectedAt column found - recreating table")
                    
                    // Create new table with correct schema (without rejectedAt camelCase column)
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS bank_notifications_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            package_name TEXT NOT NULL,
                            sender_alias TEXT NOT NULL,
                            message_body TEXT NOT NULL,
                            message_hash TEXT NOT NULL,
                            posted_at TEXT NOT NULL,
                            processed INTEGER NOT NULL,
                            transaction_id INTEGER,
                            created_at TEXT NOT NULL,
                            status TEXT NOT NULL,
                            amount REAL,
                            merchant TEXT,
                            bank_name TEXT,
                            account_last4 TEXT,
                            transaction_type TEXT,
                            rejected_at TEXT,
                            deletion_scheduled_at TEXT,
                            trash_retention_days INTEGER
                        )
                    """)
                    
                    android.util.Log.d("Migration_14_15", "Created new table")
                    
                    // Build the SELECT statement dynamically based on which columns exist
                    val selectRejectedAt = when {
                        hasRejectedAtSnakeCase && hasRejectedAtCamelCase ->
                            "COALESCE(rejected_at, rejectedAt) as rejected_at"
                        hasRejectedAtSnakeCase ->
                            "rejected_at"
                        hasRejectedAtCamelCase ->
                            "rejectedAt as rejected_at"
                        else ->
                            "NULL as rejected_at"
                    }
                    
                    // Copy data from old table to new table
                    database.execSQL("""
                        INSERT INTO bank_notifications_new
                        SELECT
                            id,
                            package_name,
                            sender_alias,
                            message_body,
                            message_hash,
                            posted_at,
                            processed,
                            transaction_id,
                            created_at,
                            status,
                            amount,
                            merchant,
                            bank_name,
                            account_last4,
                            transaction_type,
                            $selectRejectedAt,
                            deletion_scheduled_at,
                            trash_retention_days
                        FROM bank_notifications
                    """)
                    
                    android.util.Log.d("Migration_14_15", "Copied data to new table")
                    
                    // Drop old table
                    database.execSQL("DROP TABLE bank_notifications")
                    android.util.Log.d("Migration_14_15", "Dropped old table")
                    
                    // Rename new table to original name
                    database.execSQL("ALTER TABLE bank_notifications_new RENAME TO bank_notifications")
                    android.util.Log.d("Migration_14_15", "Renamed new table")
                    
                    // Recreate indices
                    database.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS index_bank_notifications_package_name_message_hash ON bank_notifications(package_name, message_hash)"
                    )
                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_bank_notifications_status ON bank_notifications(status)"
                    )
                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_bank_notifications_deletion_scheduled_at ON bank_notifications(deletion_scheduled_at)"
                    )
                    
                    android.util.Log.d("Migration_14_15", "Recreated indices - migration complete")
                } else {
                    android.util.Log.d("Migration_14_15", "No duplicate column found - schema already correct, skipping migration")
                }
                
                android.util.Log.d("Migration_14_15", "Migration 14→15 completed successfully")
            }
        }
        
        /**
         * Migration 15→16: Add priority field to bank_accounts and create account_transactions table
         * 
         * Changes:
         * 1. Add priority column to bank_accounts (PRIMARY/SECONDARY)
         * 2. Create account_transactions table for tracking credits/debits history
         * 
         * This migration is SAFE because:
         * - Uses try-catch for ALTER TABLE (idempotent)
         * - Uses IF NOT EXISTS for CREATE TABLE
         * - Provides default value for new column
         * - Logs all steps for debugging
         */
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                android.util.Log.d("Migration_15_16", "Starting migration to add account priority and transactions")
                
                // Add priority column to bank_accounts
                try {
                    database.execSQL(
                        "ALTER TABLE bank_accounts ADD COLUMN priority TEXT NOT NULL DEFAULT 'SECONDARY'"
                    )
                    android.util.Log.d("Migration_15_16", "Added priority column to bank_accounts")
                } catch (e: Exception) {
                    android.util.Log.w("Migration_15_16", "Priority column already exists: ${e.message}")
                }
                
                // Create account_transactions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS account_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        accountId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        amount REAL NOT NULL,
                        balanceAfter REAL NOT NULL,
                        description TEXT NOT NULL,
                        referenceId INTEGER,
                        referenceType TEXT,
                        timestamp INTEGER NOT NULL,
                        month INTEGER NOT NULL,
                        year INTEGER NOT NULL,
                        FOREIGN KEY(accountId) REFERENCES bank_accounts(id) ON DELETE CASCADE
                    )
                """)
                android.util.Log.d("Migration_15_16", "Created account_transactions table")
                
                // Create indices for performance
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_account_transactions_accountId ON account_transactions(accountId)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_account_transactions_timestamp ON account_transactions(timestamp)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_account_transactions_type ON account_transactions(type)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_account_transactions_month_year ON account_transactions(month, year)"
                )
                
                android.util.Log.d("Migration_15_16", "Migration 15→16 completed successfully")
            }
        }
        
        // ============================================================================
        // @AI_AGENT_DATABASE_CHECKPOINT - MIGRATION SUMMARY
        // ============================================================================
        //
        // Current Status:
        // ✅ Database Version: 15
        // ✅ Total Migrations: 14 (v1→v15)
        // ✅ All migrations preserve user data
        // ✅ Schema export enabled for tracking
        // ✅ Destructive migration REMOVED (NEVER add it back!)
        // ✅ Complete migration path from v1 to v15
        //
        // Migration Strategy:
        // - Each migration is atomic and handles one logical change
        // - Default values provided for new NOT NULL columns
        // - Indices created for foreign keys and frequently queried columns
        // - Backward compatible with existing data
        // - All migrations are idempotent (safe to run multiple times)
        //
        // How It Works:
        // - New Users: Database created at version 15 directly (no migrations run)
        // - Existing Users: Migrations run sequentially from their version to v15
        // - Example: User at v3 → runs v3→4, v4→5, ..., v14→15
        //
        // Testing Checklist:
        // - Test upgrade path from each version to next
        // - Verify data integrity after migration
        // - Check foreign key constraints work correctly
        // - Validate indices improve query performance
        // - Test on device with real user data
        //
        // Documentation:
        // - DATABASE_AGENT_GUIDE.md - Guidelines for AI agents
        // - DATABASE_MIGRATION_GUIDE.md - Detailed migration history
        // - DATABASE_SCHEMA_ANALYSIS.md - Complete schema analysis
        //
        // @AI_AGENT_DATABASE_CHECKPOINT - Read DATABASE_AGENT_GUIDE.md before changes!
        // ============================================================================
    }
}

// Made with Bob
