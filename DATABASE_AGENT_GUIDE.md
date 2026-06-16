# Database Management Guide for AI Agents

## 🤖 CRITICAL: Read This Before Making ANY Database Changes

This document provides **mandatory guidelines** for AI agents working on PaisaTracker's database. Following these rules ensures user data is **never lost**.

---

## 📍 Quick Reference

**Current Database Version:** 15  
**Database File:** `src/main/java/com/h4rsh41/paisatracker/data/PaisaTrackerDatabase.kt`  
**Migration Strategy:** Sequential migrations with full data preservation  
**Search Keywords in Code:** `@AI_AGENT_DATABASE_CHECKPOINT`

---

## 🚨 GOLDEN RULES (NEVER BREAK THESE)

### Rule #1: NEVER Use Destructive Migration
```kotlin
// ❌ FORBIDDEN - This deletes all user data
.fallbackToDestructiveMigration()

// ✅ CORRECT - Always provide proper migrations
.addMigrations(MIGRATION_X_Y)
```

### Rule #2: ALWAYS Increment Database Version
```kotlin
// When adding new fields/tables:
@Database(
    entities = [...],
    version = 16,  // ← Increment this
    exportSchema = true
)
```

### Rule #3: ALWAYS Create Migration for Version Change
```kotlin
// If version changes from 15 → 16, create:
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Your migration code here
    }
}

// And add it to the builder:
.addMigrations(
    // ... existing migrations ...
    MIGRATION_15_16  // ← Add new migration
)
```

### Rule #4: Make Migrations Idempotent
```kotlin
// ✅ CORRECT - Safe to run multiple times
try {
    database.execSQL("ALTER TABLE expenses ADD COLUMN newField TEXT")
} catch (e: Exception) {
    // Column already exists, safe to ignore
}

// OR use IF NOT EXISTS
database.execSQL("CREATE TABLE IF NOT EXISTS new_table (...)")
database.execSQL("CREATE INDEX IF NOT EXISTS index_name ON table(column)")
```

### Rule #5: Match Entity Definitions to Database Schema
```kotlin
// Entity field names MUST match database column names
@Entity(tableName = "expenses")
data class Expense(
    @ColumnInfo(name = "categoryId")  // ← Matches DB column
    val categoryId: Long,
    
    // Foreign keys and indices MUST match database order
    foreignKeys = [
        ForeignKey(entity = BankAccount::class, ...),  // Order matters!
        ForeignKey(entity = Category::class, ...)
    ],
    indices = [
        Index(value = ["bankAccountId"]),  // Order matters!
        Index(value = ["categoryId"]),
        Index(value = ["date"])
    ]
)
```

---

## 📋 Step-by-Step: Adding a New Field

### Example: Adding `notes` field to `expenses` table

#### Step 1: Update Entity
```kotlin
@Entity(tableName = "expenses")
data class Expense(
    // ... existing fields ...
    val notes: String? = null  // ← Add new field with default value
)
```

#### Step 2: Increment Database Version
```kotlin
@Database(
    entities = [...],
    version = 16,  // ← Changed from 15 to 16
    exportSchema = true
)
```

#### Step 3: Create Migration
```kotlin
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        android.util.Log.d("Migration_15_16", "Adding notes field to expenses")
        
        try {
            database.execSQL("ALTER TABLE expenses ADD COLUMN notes TEXT")
            android.util.Log.d("Migration_15_16", "Successfully added notes column")
        } catch (e: Exception) {
            android.util.Log.w("Migration_15_16", "Column already exists: ${e.message}")
        }
    }
}
```

#### Step 4: Register Migration
```kotlin
.addMigrations(
    MIGRATION_1_2,
    // ... all existing migrations ...
    MIGRATION_14_15,
    MIGRATION_15_16  // ← Add new migration
)
```

#### Step 5: Update Documentation
Update `DATABASE_MIGRATION_GUIDE.md` with the new migration details.

---

## 📋 Step-by-Step: Adding a New Table

### Example: Adding `tags` table

#### Step 1: Create Entity
```kotlin
@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)]
)
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#2196F3",
    val createdAt: Long = System.currentTimeMillis()
)
```

#### Step 2: Create DAO
```kotlin
@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long
}
```

#### Step 3: Update Database
```kotlin
@Database(
    entities = [
        // ... existing entities ...
        Tag::class  // ← Add new entity
    ],
    version = 16,  // ← Increment version
    exportSchema = true
)
abstract class PaisaTrackerDatabase : RoomDatabase() {
    // ... existing DAOs ...
    abstract fun tagDao(): TagDao  // ← Add new DAO
}
```

#### Step 4: Create Migration
```kotlin
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        android.util.Log.d("Migration_15_16", "Creating tags table")
        
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS tags (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                colorHex TEXT NOT NULL DEFAULT '#2196F3',
                createdAt INTEGER NOT NULL
            )
        """)
        
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_tags_name ON tags(name)"
        )
        
        android.util.Log.d("Migration_15_16", "Tags table created successfully")
    }
}
```

---

## 🔍 How New Users vs Existing Users Work

### **New Users (Fresh Install)**
```
1. App installs → Room creates database at version 15 directly
2. No migrations run (database is already at latest version)
3. All tables created with current schema
4. ✅ Works perfectly
```

### **Existing Users (App Update)**
```
1. User has database at version X (e.g., version 3)
2. App updates → Room detects version mismatch
3. Room runs migrations sequentially: 3→4→5→...→15
4. Each migration preserves user data
5. ✅ All data intact, database now at version 15
```

### **Why This Works:**
- Room's `@Database(version = X)` tells Room the **target version**
- New users: Room creates database at target version (no migrations needed)
- Existing users: Room runs migrations from their current version to target version
- Migrations are **only for upgrading**, not for fresh installs

---

## 🔍 Finding Database Code (Search Keywords)

Use these keywords to find critical database code:

```kotlin
// In PaisaTrackerDatabase.kt
@AI_AGENT_DATABASE_CHECKPOINT  // Marks critical sections

// Search for:
"@Database"           // Database definition
"version ="           // Current version number
"Migration"           // All migrations
"addMigrations"       // Migration registration
"@Entity"             // All table definitions
```

---

## ✅ Pre-Deployment Checklist

Before committing database changes:

- [ ] Database version incremented?
- [ ] Migration created for version change?
- [ ] Migration registered in `.addMigrations()`?
- [ ] Migration is idempotent (uses try-catch or IF NOT EXISTS)?
- [ ] Migration includes logging statements?
- [ ] Entity definition matches database schema?
- [ ] Foreign keys and indices in correct order?
- [ ] Build successful (`./gradlew assembleDebug`)?
- [ ] Documentation updated?
- [ ] Tested on device with existing data?

---

## 🚨 Common Mistakes to Avoid

### ❌ Mistake #1: Forgetting to Increment Version
```kotlin
// Added new field but forgot to increment version
@Database(entities = [...], version = 15)  // ← Still 15!
```
**Result:** Room doesn't know schema changed, app crashes

### ❌ Mistake #2: Not Creating Migration
```kotlin
// Incremented version but no migration
@Database(entities = [...], version = 16)  // ← Changed to 16
// But no MIGRATION_15_16 created!
```
**Result:** Room can't upgrade database, app crashes

### ❌ Mistake #3: Non-Idempotent Migration
```kotlin
// Will crash if run twice
database.execSQL("ALTER TABLE expenses ADD COLUMN notes TEXT")
```
**Result:** If migration runs twice, crashes with "duplicate column"

### ❌ Mistake #4: Wrong Foreign Key Order
```kotlin
// Entity has FK order: [Category, BankAccount]
// But database has: [BankAccount, Category]
```
**Result:** Schema validation fails, app crashes

---

## 📊 Current Database State (Version 15)

### Tables (13 total):
1. projects (7 columns)
2. categories (5 columns)
3. expenses (11 columns)
4. bank_accounts (12 columns)
5. bank_notifications (18 columns)
6. assets (5 columns)
7. budgets (10 columns)
8. flap_data (6 columns)
9. salary_records (13 columns)
10. action_history (5 columns)
11. backup_metadata (8 columns)
12. unrecognized_sms (7 columns)
13. merchant_rules (9 columns)

### Migration Path:
```
v1 → v2 → v3 → v4 → v5 → v6 → v7 → v8 → v9 → v10 → v11 → v12 → v13 → v14 → v15
```

---

## 📚 Related Documentation

- `DATABASE_MIGRATION_GUIDE.md` - Detailed migration history
- `DATABASE_SCHEMA_ANALYSIS.md` - Complete schema analysis
- `PaisaTrackerDatabase.kt` - Source of truth for database

---

## 🆘 Troubleshooting

### Problem: "Migration didn't properly handle: [table_name]"
**Solution:** Entity definition doesn't match database schema
1. Check column names match exactly
2. Check foreign key order matches database
3. Check index order matches database
4. Use `PRAGMA table_info(table_name)` to inspect actual schema

### Problem: "duplicate column name: [column]"
**Solution:** Migration not idempotent
1. Wrap ALTER TABLE in try-catch
2. Or check if column exists before adding

### Problem: Build fails after database change
**Solution:** 
1. Clean build: `./gradlew clean`
2. Rebuild: `./gradlew assembleDebug`
3. Check error messages for schema validation issues

---

## 🎯 Summary for AI Agents

**When modifying database:**
1. ✅ Update Entity
2. ✅ Increment version
3. ✅ Create migration
4. ✅ Register migration
5. ✅ Make migration idempotent
6. ✅ Add logging
7. ✅ Test build
8. ✅ Update docs

**Remember:** User data is sacred. When in doubt, ask for clarification rather than risk data loss.

---

*This guide ensures PaisaTracker's database remains robust and user data stays safe across all updates.*

*Last Updated: 2026-06-16 | Database Version: 15*