# ⚠️ IMPORTANT: Revert Destructive Migration Setting

## Current Status
The database is currently set to use **destructive migration** to fix the v15→v16 migration issue.

**File:** `PaisaTrackerDatabase.kt`
**Line 68:** `.fallbackToDestructiveMigration(true)`

## What This Means
- The app will **delete and recreate** the database if migration fails
- **All user data will be lost** if there's a migration problem
- This is **ONLY for development/testing**

## ✅ After Testing Successfully

**REVERT THIS CHANGE** before releasing to production:

```kotlin
.fallbackToDestructiveMigration(false)  // Change back to false
```

## When to Revert

Revert after you've confirmed:
1. ✅ App launches without crashes
2. ✅ SMS settings screen loads
3. ✅ Pending SMS transactions screen works
4. ✅ Transaction confirmation works
5. ✅ Database schema is correct

## How to Revert

1. Open `PaisaTrackerDatabase.kt`
2. Find line 68
3. Change `true` back to `false`
4. Rebuild the app

```kotlin
// BEFORE (current - temporary fix)
.fallbackToDestructiveMigration(true)

// AFTER (production-ready)
.fallbackToDestructiveMigration(false)
```

## Why This Matters

In production:
- Users have valuable expense data
- Losing data = bad user experience
- Proper migrations preserve user data
- Destructive migration should only be used as last resort

## For Future Migrations

Always test migrations properly:
1. Create migration code
2. Test on device with old schema
3. Verify data is preserved
4. Use Room's migration testing framework

---

**Remember:** This is a temporary fix for development. Don't forget to revert!