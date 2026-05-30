# Database Migration Error Fix

## Error Description
```
Migration didn't properly handle: bank_notifications
Expected columns: status, amount, merchant, bank_name, account_last4
```

This error occurs when the app was already installed with database version 15, and the migration to version 16 needs to be applied.

## Solutions

### Solution 1: Uninstall and Reinstall (Recommended for Development)
This is the quickest solution during development:

1. **Uninstall the app completely:**
   ```bash
   adb uninstall com.example.paisatracker
   ```

2. **Reinstall the app:**
   ```bash
   cd PaisaTracker
   ./gradlew installDebug
   ```

3. **Or from Android Studio:**
   - Right-click on app in device list
   - Select "Uninstall"
   - Run the app again

**Note:** This will delete all existing data in the app.

### Solution 2: Clear App Data (Preserves APK)
If you want to keep the app installed but reset the database:

1. **On the device:**
   - Go to Settings → Apps → PaisaTracker
   - Tap "Storage"
   - Tap "Clear Data" or "Clear Storage"

2. **Or via ADB:**
   ```bash
   adb shell pm clear com.example.paisatracker
   ```

### Solution 3: Force Migration (For Production)
If you need to preserve user data, you can force a destructive migration temporarily:

1. **Modify PaisaTrackerDatabase.kt temporarily:**
   ```kotlin
   .fallbackToDestructiveMigration(true)  // Change false to true
   ```

2. **Run the app once** - This will recreate the database with the new schema

3. **Change it back:**
   ```kotlin
   .fallbackToDestructiveMigration(false)  // Change back to false
   ```

**Warning:** This will delete all user data!

### Solution 4: Manual Database Update (Advanced)
For production apps with user data, you would need to:

1. Export user data before migration
2. Apply migration
3. Re-import user data

This is complex and not needed for development.

## Why This Happened

The database was created at version 15 (before we added the new columns). When we:
1. Added new columns to `BankNotificationEntity`
2. Created `MIGRATION_15_16`
3. Changed database version to 16

The existing database on the device still has version 15 schema. Room tries to apply the migration but the app needs to be reinstalled or data cleared for a fresh start.

## Prevention for Future

For production releases:
1. Always test migrations on devices with existing data
2. Use Room's migration testing framework
3. Consider using `fallbackToDestructiveMigrationOnDowngrade()` for development builds
4. Export schema files and version them properly

## Verification

After applying any solution, verify the app starts successfully:
1. Check logcat for "Migration succeeded" messages
2. Navigate to Settings → SMS Transactions
3. Verify the pending SMS screen loads without errors

## Current Migration (v15 → v16)

The migration adds these columns to `bank_notifications`:
- `status` (TEXT, NOT NULL, DEFAULT 'PENDING')
- `amount` (REAL, nullable)
- `merchant` (TEXT, nullable)
- `bank_name` (TEXT, nullable)
- `account_last4` (TEXT, nullable)

And creates an index on the `status` column for efficient querying.