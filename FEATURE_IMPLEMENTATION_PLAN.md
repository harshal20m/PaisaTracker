# PaisaTracker Feature Implementation Plan

## 📅 Date: 2026-05-08

---

## ✅ Completed Today

### 1. **Analytics Loading Issue Fix**
**Problem:** Analytics screen stuck on loading spinner
**Root Cause:** Flow blocking with `.collect{}` inside `map` and `combine` operators
**Solution:** 
- Fixed 3 locations using `firstOrNull()` instead of `.collect{}`
- `AnalyticsViewModel.kt` line 185
- `PaisaTrackerRepository.kt` getTotalBudgetForPeriod() 
- `PaisaTrackerRepository.kt` calculateSavingsForMonth()

**Files Modified:**
- `src/main/java/com/example/paisatracker/viewmodel/AnalyticsViewModel.kt`
- `src/main/java/com/example/paisatracker/data/PaisaTrackerRepository.kt`

---

### 2. **Analytics Screen Crash Fix**
**Problem:** App crashes when clicking "View Full Analytics"
**Error:** `IllegalStateException: Vertically scrollable component was measured with an infinity maximum height constraints`
**Root Cause:** `LazyColumn` nested inside scrollable `Column`
**Solution:** Replaced `LazyColumn` with regular `Column` in `CategorySpendingList.kt`

**Files Modified:**
- `src/main/java/com/example/paisatracker/ui/components/CategorySpendingList.kt`

---

### 3. **Analytics Preview Card UI Redesign** 🎨
**Status:** In Progress (Building)

**Changes Made:**
- **Modern Header:** Icon in colored box + "Financial Insights" title
- **Stat Cards:** Two prominent cards for Spent (💸) and Saved (💰)
- **Savings Rate:** Visual progress bar with color coding:
  - Green (≥30%): Excellent
  - Yellow (≥15%): Good
  - Red (<15%): Needs improvement
- **Top Categories:** Mini list showing top 3 categories with percentages
- **Better Button:** Filled button instead of outlined
- **Color Scheme:** Using `primaryContainer` for better visual hierarchy
- **Spacing:** Increased padding and spacing for better readability

**Files Modified:**
- `src/main/java/com/example/paisatracker/ui/main/home/AnalyticsPreviewCard.kt`

**New Components Added:**
- `ModernStatCard`: Emoji-based stat cards
- `MiniCategoryRow`: Compact category display

---

## 📋 Upcoming Features (In Order)

### 4. **Add "Include in Salary" Toggle to Projects**
**Estimated Time:** 2-3 hours

**Requirements:**
- Add `includeInSalary` boolean field to Project entity
- Database migration (version bump)
- UI toggle in Project creation/edit sheets
- Update salary calculations to exclude projects where `includeInSalary = false`
- Update FinancialState calculations

**Files to Modify:**
- `src/main/java/com/example/paisatracker/data/Project.kt`
- `src/main/java/com/example/paisatracker/data/PaisaTrackerDatabase.kt`
- `src/main/java/com/example/paisatracker/ui/main/projects/ProjectListSheets.kt`
- `src/main/java/com/example/paisatracker/data/PaisaTrackerRepository.kt`
- `src/main/java/com/example/paisatracker/viewmodel/AnalyticsViewModel.kt`

**Implementation Steps:**
1. Add field to Project data class
2. Create database migration
3. Add toggle to AddProjectSheet
4. Add toggle to EditProjectSheet
5. Update repository methods to filter by includeInSalary
6. Update ViewModel calculations
7. Test with existing data

---

### 5. **Bank Account Management System** 🏦
**Estimated Time:** Full day (8-10 hours)

**Requirements:**
- Create new `BankAccount` entity
- Support multiple accounts per user
- Track balance for each account
- Link expenses to specific accounts
- Update balance on expense creation/deletion
- Account selection during expense entry
- Account overview screen
- Transfer between accounts (optional)

**Database Schema:**
```kotlin
@Entity(tableName = "bank_accounts")
data class BankAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,              // e.g., "HDFC Savings"
    val accountNumber: String?,    // Optional, last 4 digits
    val bankName: String?,         // e.g., "HDFC Bank"
    val accountType: AccountType,  // SAVINGS, CURRENT, CREDIT_CARD, WALLET
    val initialBalance: Double,
    val currentBalance: Double,
    val emoji: String,             // 🏦, 💳, 💰, etc.
    val color: String,             // Hex color for UI
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AccountType {
    SAVINGS, CURRENT, CREDIT_CARD, WALLET, CASH
}
```

**Expense Entity Update:**
```kotlin
// Add to Expense entity
val accountId: Long?  // Foreign key to BankAccount
```

**Files to Create:**
- `src/main/java/com/example/paisatracker/data/BankAccount.kt`
- `src/main/java/com/example/paisatracker/data/BankAccountDao.kt`
- `src/main/java/com/example/paisatracker/ui/accounts/AccountsScreen.kt`
- `src/main/java/com/example/paisatracker/ui/accounts/AddAccountSheet.kt`
- `src/main/java/com/example/paisatracker/viewmodel/AccountsViewModel.kt`

**Files to Modify:**
- `src/main/java/com/example/paisatracker/data/Expense.kt`
- `src/main/java/com/example/paisatracker/data/PaisaTrackerDatabase.kt` (migration)
- `src/main/java/com/example/paisatracker/data/PaisaTrackerRepository.kt`
- `src/main/java/com/example/paisatracker/ui/Quickadd/Quickaddsheet.kt`
- `src/main/java/com/example/paisatracker/navigation/AppNavigation.kt`

**Implementation Steps:**
1. Create BankAccount entity and DAO
2. Create database migration (version bump)
3. Add accountId to Expense entity
4. Create AccountsViewModel
5. Create AccountsScreen UI
6. Create AddAccountSheet
7. Add account selection to QuickAddSheet
8. Update balance on expense operations
9. Add account filter to expense lists
10. Create account overview cards
11. Add account management to settings
12. Test thoroughly with migrations

---

### 6. **Management Screen for Projects/Categories**
**Estimated Time:** Half day (4-5 hours)

**Requirements:**
- Centralized screen to view all projects and categories
- Bulk operations (archive, delete, reorder)
- Search and filter
- Statistics per project/category
- Quick edit capabilities
- Drag-to-reorder (optional)

**Features:**
- Tab view: Projects | Categories
- Search bar at top
- Sort options (Name, Date, Amount, Count)
- Long-press for bulk selection
- Floating action button for bulk actions
- Swipe actions (Edit, Delete, Archive)
- Statistics cards (Total spent, Expense count, etc.)

**Files to Create:**
- `src/main/java/com/example/paisatracker/ui/management/ManagementScreen.kt`
- `src/main/java/com/example/paisatracker/ui/management/ProjectManagementTab.kt`
- `src/main/java/com/example/paisatracker/ui/management/CategoryManagementTab.kt`
- `src/main/java/com/example/paisatracker/viewmodel/ManagementViewModel.kt`

**Files to Modify:**
- `src/main/java/com/example/paisatracker/navigation/AppNavigation.kt`
- `src/main/java/com/example/paisatracker/ui/settings/SettingsScreen.kt` (add link)

---

### 7. **Flexible Salary Updates**
**Estimated Time:** 2-3 hours

**Requirements:**
- Allow salary updates at any time (not just monthly)
- Salary history tracking
- Multiple income sources
- Edit past salary records
- Income vs Expense comparison

**Current Limitation:**
- Salary can only be set once per month
- No history tracking
- No editing of past records

**Proposed Changes:**
- Add "Edit Salary" button to salary section
- Show salary history in a list
- Allow adding income at any time
- Support multiple income sources (Salary, Freelance, Investment, etc.)
- Track income date separately from month

**Files to Modify:**
- `src/main/java/com/example/paisatracker/ui/salary/SalaryTrackerSection.kt`
- `src/main/java/com/example/paisatracker/ui/salary/SalaryViewModel.kt`
- `src/main/java/com/example/paisatracker/data/SalaryRecord.kt` (add incomeType field)
- `src/main/java/com/example/paisatracker/data/SalaryRecordDao.kt`

---

## 🎯 Implementation Order

Based on user request: "go with flow one by one which i told"

1. ✅ Analytics loading fix
2. ✅ Analytics crash fix  
3. 🔄 Analytics Preview Card UI (in progress)
4. ⏭️ "Include in Salary" toggle for Projects
5. ⏭️ Bank Account Management System
6. ⏭️ Management Screen
7. ⏭️ Flexible Salary Updates

---

## 📊 Progress Tracking

| Feature | Status | Time Spent | Remaining |
|---------|--------|------------|-----------|
| Analytics Loading Fix | ✅ Complete | 1 hour | - |
| Analytics Crash Fix | ✅ Complete | 30 min | - |
| Analytics UI Redesign | 🔄 In Progress | 1 hour | Testing |
| Include in Salary Toggle | ⏭️ Pending | - | 2-3 hours |
| Bank Account System | ⏭️ Pending | - | 8-10 hours |
| Management Screen | ⏭️ Pending | - | 4-5 hours |
| Flexible Salary | ⏭️ Pending | - | 2-3 hours |

**Total Estimated Remaining:** ~17-21 hours

---

## 🔧 Technical Considerations

### Database Migrations
- Current version: 1
- Upcoming migrations needed:
  - Version 2: Add `includeInSalary` to Project
  - Version 3: Add BankAccount entity + accountId to Expense
  - Version 4: Update SalaryRecord with incomeType

### Testing Strategy
- Test each feature independently
- Test database migrations with existing data
- Test backward compatibility
- Performance testing with large datasets

### UI/UX Principles
- Maintain Material 3 design consistency
- Smooth animations and transitions
- Clear visual hierarchy
- Accessibility support
- Dark mode compatibility

---

## 📝 Notes

- All features follow MVVM architecture
- Using Kotlin Coroutines and Flow for reactive data
- Material 3 components only
- Offline-first approach maintained
- No cloud dependencies

---

**Last Updated:** 2026-05-08 19:57 IST  
**Next Review:** After Analytics UI completion