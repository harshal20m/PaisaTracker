# PaisaTracker - Complete Project Analysis

## 📋 Executive Summary

**PaisaTracker** is a modern, offline-first Android expense tracking application built with Kotlin and Jetpack Compose. The app follows strict MVVM architecture with Material 3 design principles, providing users with a privacy-focused, feature-rich expense management solution.

---

## 🏗️ Architecture Overview

### Core Architecture Pattern: MVVM (Model-View-ViewModel)

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  - Screens, Components, Sheets, Dialogs                 │
│  - Material 3 Design System                             │
└─────────────────┬───────────────────────────────────────┘
                  │ collectAsStateWithLifecycle()
┌─────────────────▼───────────────────────────────────────┐
│                  ViewModel Layer                         │
│  - State Management (UiState pattern)                   │
│  - Business Logic Orchestration                         │
│  - Flow Transformations                                 │
└─────────────────┬───────────────────────────────────────┘
                  │ Kotlin Flows
┌─────────────────▼───────────────────────────────────────┐
│                 Repository Layer                         │
│  - Data Source Abstraction                              │
│  - Business Logic Implementation                        │
│  - Flow Operators (map, combine, flatMapLatest)        │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│                   Data Layer                             │
│  - Room Database (Local SQLite)                         │
│  - DataStore (Preferences)                              │
│  - DAOs (Data Access Objects)                           │
└─────────────────────────────────────────────────────────┘
```

---

## 📦 Tech Stack

### Core Technologies
- **Language:** 100% Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM with Repository Pattern
- **Concurrency:** Kotlin Coroutines + Flow
- **Database:** Room (SQLite)
- **Preferences:** DataStore
- **Navigation:** Type-safe Navigation Compose
- **Dependency Management:** Version Catalog (libs.versions.toml)

### Key Libraries
- **androidx.compose:** UI toolkit
- **androidx.room:** Local database
- **androidx.datastore:** Preferences storage
- **androidx.lifecycle:** ViewModel and lifecycle management
- **androidx.navigation:** Navigation component
- **kotlinx.coroutines:** Asynchronous programming
- **androidx.biometric:** Biometric authentication
- **androidx.work:** Background tasks (reminders)
- **androidx.glance:** Widget framework

---

## 🗂️ Project Structure

### 1. Data Layer (`src/main/java/com/example/paisatracker/data/`)

#### Core Entities
- **Project.kt** - Top-level container for expenses
- **Category.kt** - Expense categorization within projects
- **Expense.kt** - Individual expense records
- **Budget.kt** - Budget tracking per category
- **SalaryRecord.kt** - Monthly salary tracking
- **Asset.kt** - Payment method tracking
- **BackupMetadata.kt** - Backup history
- **ActionHistory.kt** - Undo/redo functionality
- **FlapData.kt** - Quick access notes/calculator

#### DAOs (Data Access Objects)
- **ProjectDao.kt** - Project CRUD + aggregations
- **CategoryDao.kt** - Category CRUD + spending queries
- **ExpenseDao.kt** - Expense CRUD + analytics queries
- **BudgetDao.kt** - Budget CRUD + progress tracking
- **SalaryRecordDao.kt** - Salary CRUD + monthly queries
- **AssetDao.kt** - Asset CRUD
- **BackupDao.kt** - Backup metadata management
- **ActionHistoryDao.kt** - History tracking
- **FlapDao.kt** - Quick access data

#### Database
- **PaisaTrackerDatabase.kt** - Room database configuration
  - Version: 1
  - Entities: 9 core entities
  - Type converters for Date, LocalDate, LocalDateTime
  - Migration strategy ready

#### Repository
- **PaisaTrackerRepository.kt** - Single source of truth
  - Aggregates all DAOs
  - Provides Flow-based reactive data
  - Implements business logic
  - Handles complex queries and transformations

#### Preferences
- **ThemePreferencesRepository.kt** - Theme settings (Light/Dark/System)
- **CurrencyPreferencesRepository.kt** - Currency selection
- **EmojiPreferencesRepository.kt** - Emoji preferences
- **AppLockPreferences.kt** - PIN/Biometric settings

#### Models
- **Currency.kt** - Currency definitions (₹, $, €, £, ¥, etc.)
- **AppTheme.kt** - Theme enum (LIGHT, DARK, SYSTEM)
- **ProjectWithTotal.kt** - Project with calculated totals
- **BudgetWithSpending.kt** - Budget with current spending
- **ExportRow.kt** - CSV export data structure
- **Converters.kt** - Room type converters

#### Utilities
- **DataSeeder.kt** - Initial data population
- **Emojidata.kt** - Emoji categories and data

---

### 2. Domain Layer (`src/main/java/com/example/paisatracker/domain/`)

#### Models
- **UiState.kt** - Generic state wrapper (Loading, Success, Error)
- **TimePeriod.kt** - Time period enum (DAILY, WEEKLY, MONTHLY, YEARLY, ALL_TIME)
- **DateRange.kt** - Date range data class
- **MonthlyTotal.kt** - Monthly aggregated data
- **YearlyTotal.kt** - Yearly aggregated data
- **CategorySpending.kt** - Category-wise spending breakdown
- **AnalyticsStatistics.kt** - Comprehensive analytics data
- **FinancialState.kt** - Current financial snapshot
- **BudgetProgress.kt** - Budget tracking with alerts

---

### 3. ViewModel Layer (`src/main/java/com/example/paisatracker/viewmodel/`)

#### Core ViewModels
- **PaisaTrackerViewModel.kt** - Main app state management
  - Project/Category/Expense CRUD
  - Asset management
  - Backup/Restore
  - App lock
  - Theme management
  - Undo/Redo functionality

- **AnalyticsViewModel.kt** - Analytics and insights
  - Time period filtering
  - Spending trends
  - Category breakdowns
  - Monthly/Yearly comparisons
  - Financial statistics

- **QuickAddViewModel.kt** - Quick expense entry
  - Simplified expense creation
  - Recent categories
  - Quick asset selection

- **SearchViewModel.kt** - Global search
  - Expense search across all projects
  - Filter by date, amount, category
  - Sort options

- **SalaryViewModel.kt** - Salary tracking
  - Monthly salary records
  - Salary vs expenses comparison
  - Savings calculation

- **SettingsViewModel.kt** - App settings
  - Theme management
  - Currency selection
  - Notification settings
  - Update checks

#### Factory
- **AnalyticsViewModelFactory.kt** - Factory for dependency injection

---

### 4. UI Layer (`src/main/java/com/example/paisatracker/ui/`)

#### Main Screens
- **main/MainApp.kt** - Root composable with navigation
- **main/home/HomeScreen.kt** - Dashboard with overview
- **main/projects/ProjectListScreen.kt** - Project list view

#### Feature Screens
- **details/ProjectDetailsScreen.kt** - Project detail view
- **details/Projectinsightsscreen.kt** - Project analytics
- **expense/ExpenseListScreen.kt** - Expense list
- **expense/ExpenseDetailScreen.kt** - Expense detail/edit
- **budget/BudgetScreen.kt** - Budget management
- **search/SearchScreen.kt** - Global search
- **export/ExportScreen.kt** - Data export (CSV/Backup)
- **settings/SettingsScreen.kt** - App settings
- **bin/BinScreen.kt** - Deleted items (soft delete)

#### Common Components
- **common/BottomNavigationBar.kt** - Bottom nav
- **common/ScreenHeader.kt** - Consistent headers
- **common/PieChart.kt** - Pie chart visualization
- **common/BarChart.kt** - Bar chart visualization
- **common/CalendarView.kt** - Calendar picker
- **common/WeeklyDashboardCalendar.kt** - Week view
- **common/EmojiPickerSheet.kt** - Emoji selection
- **common/DatePickerSheet.kt** - Date picker
- **common/DeleteConfirmationSheetContent.kt** - Delete confirmation
- **common/PaisaToast.kt** - Custom toast messages
- **common/ZoomableImageDialog.kt** - Image viewer
- **common/SortComponents.kt** - Sort UI components
- **common/Breadcrumbnavigation.kt** - Breadcrumb navigation

#### Analytics Components
- **components/LoadingState.kt** - Loading indicator
- **components/ErrorState.kt** - Error display
- **components/EmptyState.kt** - Empty state display
- **components/ConfirmationDialog.kt** - Confirmation dialogs

#### Bottom Sheets
- **details/category/Categorysheets.kt** - Category management sheets
- **main/projects/ProjectListSheets.kt** - Project creation/edit
- **budget/AddBudgetSheet.kt** - Budget creation
- **assets/AssetsBottomSheet.kt** - Asset management
- **applock/SetupPinSheet.kt** - PIN setup
- **applock/AppLockSettingsSheet.kt** - Lock settings
- **Quickadd/Quickaddsheet.kt** - Quick expense entry
- **settings/ThemeSelectionBottomSheet.kt** - Theme picker
- **settings/Currencyselectionbottomsheet.kt** - Currency picker
- **settings/NotificationSettingsBottomSheet.kt** - Notification settings
- **settings/AboutBottomSheet.kt** - About app
- **settings/Batteryoptimizationbottomsheet.kt** - Battery settings
- **setup/FirstTimeSetupSheet.kt** - First-time setup
- **tour/AppTourSheet.kt** - App tour

#### Special Features
- **flap/QuickAccessFlap.kt** - Quick access panel
- **flap/CalculatorTab.kt** - Built-in calculator
- **flap/NotesTab.kt** - Quick notes
- **recent/RecentExpensesSection.kt** - Recent transactions
- **main/home/RecentTransactionsSlider.kt** - Animated transaction slider
- **salary/SalaryTrackerSection.kt** - Salary tracking UI
- **applock/AppLockScreen.kt** - Lock screen

---

### 5. Utility Layer (`src/main/java/com/example/paisatracker/util/`)

- **BackupManager.kt** - Backup/restore logic (ZIP format)
- **BiometricHelper.kt** - Biometric authentication
- **ImageUtils.kt** - Image compression and management
- **AppUtils.kt** - General utilities
- **Currencyutils.kt** - Currency formatting
- **UpdateManager.kt** - App update checks
- **ExpenseReminderWorker.kt** - Background reminder notifications
- **ComposeFileProvider.kt** - File provider for sharing
- **TimePeriodManager.kt** - Time period calculations

---

### 6. Widget Layer (`src/main/java/com/example/paisatracker/widget/`)

#### Widgets
- **QuickBalanceWidget.kt** - Home screen balance widget
- **BudgetProgressWidget.kt** - Budget progress widget
- **RecentTransactionsWidget.kt** - Recent transactions widget
- **SalaryWidget.kt** - Salary tracking widget

#### Widget Receivers
- **QuickBalanceWidgetReceiver.kt**
- **BudgetProgressWidgetReceiver.kt**
- **RecentTransactionsWidgetReceiver.kt**
- **SalaryWidgetReceiver.kt**

#### Widget Utilities
- **WidgetColorScheme.kt** - Widget theming

---

### 7. Theme Layer (`src/main/java/com/example/paisatracker/ui/theme/`)

- **Theme.kt** - Material 3 theme configuration
- **Color.kt** - Color palette (Light/Dark)
- **Type.kt** - Typography system

---

### 8. Navigation (`src/main/java/com/example/paisatracker/navigation/`)

- **AppNavigation.kt** - Type-safe navigation graph
  - Routes: Home, Projects, ProjectDetails, ExpenseList, ExpenseDetail, Budget, Search, Export, Settings, Bin, Analytics, ProjectInsights

---

## 🔑 Key Features

### 1. **Project-Based Organization**
- Hierarchical structure: Projects → Categories → Expenses
- Each project can have multiple categories
- Each category can have multiple expenses
- Soft delete with bin functionality

### 2. **Expense Management**
- Quick add with recent categories
- Image attachments (compressed storage)
- Multiple payment methods (Assets)
- Date/time tracking
- Notes and descriptions
- Undo/Redo functionality

### 3. **Budget Tracking**
- Per-category budgets
- Real-time progress tracking
- Budget alerts (50%, 75%, 90%, 100%)
- Monthly budget cycles
- Budget vs actual spending comparison

### 4. **Salary Tracking**
- Monthly salary records
- Salary vs expenses comparison
- Savings calculation
- Monthly financial overview

### 5. **Analytics & Insights**
- Time period filtering (Daily, Weekly, Monthly, Yearly, All-Time)
- Spending trends
- Category breakdowns (Pie charts)
- Monthly comparisons (Bar charts)
- Top spending categories
- Average daily/weekly/monthly spending
- Financial statistics

### 6. **Search & Filter**
- Global expense search
- Filter by date range
- Filter by amount range
- Filter by category
- Sort by date, amount, category

### 7. **Data Export**
- CSV export with custom date ranges
- Full backup (ZIP format with database + images)
- Restore from backup
- Backup history tracking

### 8. **Security**
- App lock (PIN + Biometric)
- Offline-first (no cloud dependency)
- Local data storage only
- Privacy-focused design

### 9. **Customization**
- Theme selection (Light/Dark/System)
- Currency selection (₹, $, €, £, ¥, etc.)
- Emoji customization for projects/categories
- Notification preferences

### 10. **Widgets**
- Quick balance widget
- Budget progress widget
- Recent transactions widget
- Salary tracking widget

### 11. **Quick Access Features**
- Quick add sheet
- Built-in calculator
- Quick notes
- Recent transactions slider

### 12. **Reminders**
- Expense reminder notifications
- Background worker for scheduled reminders
- Customizable reminder settings

---

## 🔄 Data Flow Examples

### Example 1: Adding an Expense

```
User Input (UI)
    ↓
QuickAddViewModel.addExpense()
    ↓
PaisaTrackerRepository.insertExpense()
    ↓
ExpenseDao.insert()
    ↓
Room Database
    ↓
Flow<List<Expense>> emits new data
    ↓
ViewModel collects and transforms
    ↓
UI updates automatically (collectAsStateWithLifecycle)
```

### Example 2: Analytics Calculation

```
User selects time period (UI)
    ↓
AnalyticsViewModel.updateTimePeriod()
    ↓
_uiState Flow combines multiple sources:
  - repository.getAllExpenses()
  - repository.getCategorySpending()
  - repository.getCurrentMonthSalary()
    ↓
Flow operators (map, combine, flatMapLatest)
    ↓
Calculate statistics, trends, breakdowns
    ↓
Emit UiState.Success(AnalyticsStatistics)
    ↓
UI renders charts and statistics
```

---

## 🎨 UI/UX Highlights

### Material 3 Design
- Dynamic color theming
- Consistent typography system
- Elevation and shadows
- Smooth animations and transitions

### Animations
- Entrance animations (fade-in, slide-up)
- Press feedback (scale, elevation)
- Infinite transitions (pulsing effects)
- Spring animations for natural feel
- Content size animations

### Interaction Patterns
- Bottom sheets for forms
- Swipe gestures
- Pull-to-refresh
- Long-press actions
- Confirmation dialogs

### Accessibility
- Semantic content descriptions
- Keyboard navigation support
- Screen reader compatibility
- High contrast support

---

## 📊 Database Schema

### Core Tables

#### projects
- id (PK)
- name
- emoji
- createdAt
- isDeleted

#### categories
- id (PK)
- projectId (FK → projects)
- name
- emoji
- createdAt
- isDeleted

#### expenses
- id (PK)
- categoryId (FK → categories)
- amount
- description
- date
- imagePath
- assetId (FK → assets)
- createdAt
- isDeleted

#### budgets
- id (PK)
- categoryId (FK → categories)
- amount
- month
- year
- createdAt

#### salary_records
- id (PK)
- amount
- month
- year
- createdAt

#### assets
- id (PK)
- name
- emoji
- createdAt

#### backup_metadata
- id (PK)
- fileName
- filePath
- createdAt
- fileSize

#### action_history
- id (PK)
- actionType
- entityType
- entityId
- entityData
- timestamp

#### flap_data
- id (PK)
- notes
- calculatorHistory
- lastUpdated

---

## 🔐 Security & Privacy

### Data Storage
- **100% Local:** All data stored on device
- **No Cloud Sync:** No mandatory cloud services
- **Encrypted Preferences:** DataStore for sensitive settings
- **File System:** Images stored in app-private directory

### App Lock
- **PIN Protection:** 4-6 digit PIN
- **Biometric:** Fingerprint/Face unlock
- **Auto-lock:** Configurable timeout
- **Lock on background:** Optional immediate lock

### Permissions
- **Storage:** For backup/restore and image attachments
- **Biometric:** For biometric authentication
- **Notifications:** For expense reminders
- **No Network:** No internet permission required

---

## 🚀 Performance Optimizations

### Database
- Indexed columns for fast queries
- Efficient Room queries with Flow
- Lazy loading for large datasets
- Pagination support

### Images
- Automatic compression on upload
- Thumbnail generation
- Efficient file cleanup on delete
- Glide/Coil for image loading

### UI
- Lazy columns/rows for lists
- Remember and derivedStateOf for recomposition optimization
- Stable keys for list items
- Minimal recomposition scope

### Background Work
- WorkManager for scheduled tasks
- Efficient worker constraints
- Battery optimization awareness

---

## 🧪 Testing Strategy

### Unit Tests
- ViewModel logic testing
- Repository testing with fake DAOs
- Utility function testing
- Date/time calculation testing

### Integration Tests
- Database migration testing
- Repository + DAO integration
- Flow transformation testing

### UI Tests (Instrumented)
- Navigation testing
- User interaction testing
- State management testing

---

## 📱 Build Configuration

### Gradle Setup
- **Version Catalog:** Centralized dependency management
- **Build Types:** Debug, Release
- **Flavors:** None (single flavor)
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34

### ProGuard
- Enabled for release builds
- R8 optimization
- Code shrinking and obfuscation

### Signing
- Debug keystore for development
- Release keystore (keystore.properties)

---

## 🔧 Development Workflow

### Code Style
- Kotlin coding conventions
- Functional programming preferred
- Immutable data classes
- Extension functions for utilities

### Git Workflow
- Feature branches
- Descriptive commit messages
- Pull request reviews
- Version tagging

### Release Process
1. Update version in build.gradle.kts
2. Run tests
3. Build release APK/AAB
4. Test on multiple devices
5. Create GitHub release
6. Update changelog

---

## 📈 Future Enhancements (Potential)

### Features
- [ ] Recurring expenses
- [ ] Split expenses
- [ ] Multi-currency support
- [ ] Receipt OCR scanning
- [ ] Advanced analytics (ML predictions)
- [ ] Export to PDF
- [ ] Cloud backup (optional)
- [ ] Wear OS companion app

### Technical
- [ ] Jetpack Compose Multiplatform (iOS support)
- [ ] Baseline profiles for performance
- [ ] Compose compiler metrics
- [ ] Automated UI testing
- [ ] CI/CD pipeline

---

## 🐛 Known Issues & Limitations

### Current Limitations
- Single currency per app (no multi-currency)
- No cloud sync (by design)
- No recurring expense automation
- No receipt scanning
- No expense splitting

### Fixed Issues
- ✅ Analytics loading state bug (Flow blocking)
- ✅ Compilation errors after Sprint 6
- ✅ Import conflicts in ViewModels
- ✅ Missing data classes

---

## 📚 Documentation

### Available Documentation
- **AGENTS.md** - AI agent instructions
- **REFACTORING_PLAN.md** - 8-sprint refactoring plan
- **SPRINT1_SUMMARY.md** - Foundation sprint summary
- **SPRINT2_SUMMARY.md** - Analytics sprint summary
- **SPRINT3_SUMMARY.md** - Advanced analytics summary
- **SPRINT4_PLAN.md** - Sprint 4 planning
- **PROJECT_PROGRESS_SUMMARY.md** - Overall progress
- **PROJECT_ANALYSIS.md** - This document

### Code Documentation
- KDoc comments on public APIs
- Inline comments for complex logic
- README.md for project overview

---

## 🎯 Project Status

### Completed Sprints
- ✅ Sprint 1: Foundation & Domain Models
- ✅ Sprint 2: Analytics Infrastructure
- ✅ Sprint 3: Advanced Analytics
- ✅ Sprint 4: Analytics UI Components
- ✅ Sprint 5: Analytics Integration
- ✅ Sprint 6: Analytics Testing & Refinement
- ✅ Sprint 7: Budget-Salary Integration
- ✅ Sprint 8: Polish & Optimization

### Recent Enhancements
- ✅ Recent Transactions Slider with animations
- ✅ Analytics loading state fix
- ✅ Build verification successful

### Current Status
**✅ BUILD SUCCESSFUL** - Ready for production

---

## 🤝 Contributing Guidelines

### Code Standards
1. Follow MVVM architecture strictly
2. Use Material 3 components only
3. Implement UiState pattern for state management
4. Add @Preview for all @Composable functions
5. Use collectAsStateWithLifecycle() in UI layer
6. Prefer functional programming
7. Write unit tests for business logic

### Pull Request Process
1. Create feature branch
2. Implement changes with tests
3. Update documentation
4. Ensure build passes
5. Submit PR with description
6. Address review comments

---

## 📞 Contact & Support

### Developer
- **Name:** Harshal Mali
- **GitHub:** [Link in About screen]
- **Email:** [Link in About screen]

### Social Links
- Discord
- Instagram
- LinkedIn
- Telegram
- WhatsApp
- Gmail

---

## 📄 License

[License information to be added]

---

## 🙏 Acknowledgments

- Material Design team for Material 3
- Jetpack Compose team
- Android community
- Open source contributors

---

**Last Updated:** 2026-05-08  
**Version:** 2.4  
**Build Status:** ✅ SUCCESSFUL