# PaisaTracker 💰

<p align="center">
  <img src="https://github.com/harshal20m/PaisaTracker/blob/master/src/main/res/drawable/expenses_5501391.png" width="120" alt="PaisaTracker Icon">
</p>

<p align="center">
  <strong>Modern. Private. Comprehensive.</strong><br>
  A sophisticated personal finance ecosystem built with <b>Kotlin</b> and <b>Jetpack Compose</b>.
</p>

<p align="center">
  <a href="https://github.com/harshal20m/PaisaTracker/releases/latest">
    <img src="https://img.shields.io/badge/Download-Latest_Release-green?style=for-the-badge&logo=android" alt="Download Latest Release">
  </a>
</p>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Core Features](#-core-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Database Schema](#-database-schema)
- [Key Components](#-key-components)
- [Widgets](#-home-screen-widgets)
- [Security Features](#-security-features)
- [Data Management](#-data-management)
- [Analytics & Privacy](#-analytics--privacy)
- [Installation](#-installation)
- [Building from Source](#-building-from-source)
- [Project Structure](#-project-structure)
- [Version History](#-version-history)
- [Contributing](#-contributing)
- [Author](#-author)

---

## 🌟 Overview

**PaisaTracker** is a feature-rich, offline-first personal finance management application for Android. Built with modern Android development practices, it offers a complete financial tracking ecosystem with zero cloud dependencies, ensuring your financial data remains private and secure on your device.

### Key Highlights

- **100% Offline**: No internet required for core functionality
- **Privacy-First**: Your financial data never leaves your device
- **Transparent Analytics**: Optional, developer-only analytics (disabled by default for users)
- **Modern UI**: Material 3 design with dynamic theming
- **Multi-Project Support**: Organize expenses across different life areas
- **Bank Account Integration**: Track balances across multiple accounts
- **Smart Budgeting**: Set limits and get visual warnings
- **Salary Tracking**: Multi-source income management
- **Rich Analytics**: Visual insights with charts and trends
- **Home Screen Widgets**: 4 customizable widgets for quick access
- **Backup & Restore**: Full database + assets backup in ZIP format

---

## ✨ Core Features

### 📂 Advanced Organization System

#### **Multi-Project Architecture**
- Create unlimited projects (e.g., "Home", "Business", "Travel")
- Each project contains its own categories and expenses
- Mark projects as completed to archive them
- Include/exclude projects from salary calculations
- Project-level insights and analytics

#### **Hierarchical Categorization**
- Categories belong to specific projects
- Customizable emoji icons for visual identification
- Category-wise spending analytics
- Budget tracking per category
- Quick category search and filtering

#### **Expense Management**
- Detailed expense tracking with:
  - Amount and date
  - Description/notes
  - Category assignment
  - Payment method (UPI, Cash, Card, etc.)
  - Receipt image attachment
  - Bank account linking
- Edit and delete with undo functionality
- Global search across all expenses
- Filter by date range, amount, category, or project
- Calendar view for date-based browsing

### 💳 Bank Account Management

#### **Multi-Account Support**
- Track multiple accounts simultaneously:
  - Bank accounts (Savings, Current)
  - Credit cards
  - Cash wallets
  - Digital wallets (PhonePe, GPay, Paytm)
- Real-time balance tracking
- Initial balance configuration
- Automatic balance updates with transactions
- Color-coded accounts for easy identification
- Custom emoji icons per account

#### **Account Features**
- Active/Inactive status management
- Account type categorization
- Transaction history per account
- Balance summary dashboard
- Account-wise expense filtering

### 💰 Budgeting System

#### **Flexible Budget Creation**
- Set budgets at multiple levels:
  - Global (all expenses)
  - Project-specific
  - Category-specific
- Multiple time periods:
  - Daily
  - Weekly
  - Monthly
  - Yearly
- Custom tracking start dates
- Active/Inactive budget toggling

#### **Budget Monitoring**
- Real-time spending progress
- Visual progress indicators
- Color-coded warnings (green → yellow → red)
- Percentage-based alerts
- Budget vs. actual spending comparison
- Historical budget performance

### 💵 Salary & Income Tracking

#### **Multi-Source Income Support**
- Track multiple income sources per month:
  - Primary salary
  - Freelance income
  - Bonuses
  - Passive income
  - Other sources
- Link salaries to specific bank accounts
- Automatic account crediting
- Recurring salary auto-generation

#### **Salary Features**
- Monthly income summary
- Income vs. spending analysis
- Remaining balance calculation
- Spending percentage tracking
- Category-wise spending breakdown
- Historical salary records
- Custom notes per salary entry

### 📊 Analytics & Insights

#### **Time-Based Analytics**
- Multiple time period views:
  - This Week
  - This Month
  - This Year
  - Custom Date Range
- Monthly spending trends (last 12 months)
- Yearly comparison charts
- Daily average calculations

#### **Category Analytics**
- Pie chart visualization
- Top spending categories
- Category-wise percentage breakdown
- Spending trends over time
- Category comparison across periods

#### **Financial Statistics**
- Total expenses
- Average daily spending
- Expense count
- Budget utilization
- Savings calculation
- Payment method distribution

### 🎛️ Quick Access Flap

A unique side drawer providing instant access to utilities:

#### **Built-in Calculator**
- Full-featured calculator
- Calculation history
- Copy results
- Quick expense amount entry

#### **Financial Notes**
- Quick note-taking
- Timestamp tracking
- Note management (add/edit/delete)
- Persistent storage

### 🗂️ Assets Gallery

Centralized management for receipt images and financial documents:
- View all attached receipts
- Independent asset storage
- Image zoom and preview
- Asset deletion with cleanup
- Expense-linked assets
- Standalone document storage

### 🔄 Data Management

#### **Backup System**
- **Full Backup**: Creates a ZIP file containing:
  - Complete SQLite database
  - All receipt images
  - Database metadata
- **Backup Metadata Tracking**:
  - File size
  - Creation timestamp
  - Project/Category/Expense counts
  - Total amount snapshot
- **Restore Functionality**:
  - One-tap restore from backup
  - Automatic database migration
  - Asset restoration

#### **Export Options**
- **CSV Export**:
  - Project-specific or all data
  - Includes: Date, Project, Category, Amount, Description, Payment Method
  - Compatible with Excel/Google Sheets
- **PDF Export** (via iText7):
  - Formatted financial reports
  - Custom date ranges
  - Professional layout

#### **Import Functionality**
- CSV import with validation
- Automatic project/category creation
- Duplicate detection
- Error handling and reporting

### 🔒 Security & Privacy

#### **App Lock System**
- **PIN Protection**:
  - 4-6 digit PIN setup
  - PIN change functionality
  - Failed attempt tracking
- **Biometric Authentication**:
  - Fingerprint support
  - Face recognition (device-dependent)
  - Fallback to PIN
- **Lock Behavior**:
  - Locks on app background
  - Configurable timeout
  - Secure state management

#### **Privacy Features**
- No internet permission for core features
- No analytics or tracking
- No cloud sync (by design)
- Local-only data storage
- Secure file handling

### 🔔 Smart Notifications

#### **Daily Expense Reminders**
- Configurable reminder time
- Smart notification scheduling
- Battery-optimized delivery
- Customizable notification content
- Enable/disable toggle

#### **Update Notifications**
- Automatic update checking
- GitHub release integration
- Version comparison
- Direct download links
- Manual check option

### 🎨 Customization

#### **Theme System**
- **Theme Options**:
  - System Default (follows device)
  - Light Mode
  - Dark Mode
- Material 3 dynamic colors
- Consistent theming across app

#### **Currency Support**
- Multiple currency options
- Symbol and code display
- Locale-aware formatting
- Persistent currency selection

#### **Visual Customization**
- Custom emoji selection for:
  - Projects
  - Categories
  - Bank accounts
  - Budgets
- Color coding for accounts
- Icon customization

### 🗑️ Bin (Recycle Bin)

- Temporary storage for deleted items
- Restore deleted expenses
- Permanent deletion option
- Action history tracking
- Undo functionality

### 📱 Additional Features

- **First-Time Setup**:
  - Interactive app tour
  - Sample data seeding option
  - Currency selection
  - Initial configuration
- **Search Functionality**:
  - Global expense search
  - Filter by description
  - Amount range filtering
  - Date range search
  - Category/Project filtering
- **Calendar View**:
  - Month-based transaction view
  - Date-wise expense grouping
  - Quick navigation
  - Transaction details on tap

---

## 🏛️ Architecture

PaisaTracker follows a strict **MVVM (Model-View-ViewModel)** architecture with a clean separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer (Compose)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Screens    │  │   Sheets     │  │   Widgets    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────────────────┬────────────────────────────────────┘
                         │ State Flow / LiveData
┌────────────────────────▼────────────────────────────────────┐
│                     ViewModel Layer                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  PaisaTrackerViewModel, AnalyticsViewModel, etc.    │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │ Repository Pattern
┌────────────────────────▼────────────────────────────────────┐
│                   Repository Layer                           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         PaisaTrackerRepository (Single Source)       │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │ DAO Interfaces
┌────────────────────────▼────────────────────────────────────┐
│                      Data Layer                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │   Room   │  │DataStore │  │  Files   │  │  Utils   │   │
│  │ Database │  │  Prefs   │  │  System  │  │          │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Architecture Principles

1. **Single Source of Truth**: Repository pattern ensures data consistency
2. **Unidirectional Data Flow**: UI observes state, ViewModels handle business logic
3. **Separation of Concerns**: Clear boundaries between layers
4. **Reactive Programming**: Kotlin Flow for asynchronous data streams
5. **Dependency Injection**: Manual DI via Application class

---

## 🛠️ Tech Stack

### Core Technologies

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Kotlin | 2.1.0 |
| **UI Framework** | Jetpack Compose | 2025.01.00 BOM |
| **Material Design** | Material 3 | Latest |
| **Build System** | Gradle (Kotlin DSL) | 8.13.2 |
| **Min SDK** | Android 9.0 (API 28) | - |
| **Target SDK** | Android 15 (API 36) | - |
| **Compile SDK** | Android 15 (API 36) | - |

### Jetpack Libraries

| Library | Purpose | Version |
|---------|---------|---------|
| **Room** | Local database (SQLite) | 2.7.0-alpha13 |
| **DataStore** | Preferences storage | 1.1.2 |
| **Navigation Compose** | Type-safe navigation | 2.8.5 |
| **Lifecycle** | Lifecycle-aware components | 2.8.7 |
| **WorkManager** | Background task scheduling | 2.10.0 |
| **Glance** | Home screen widgets | 1.1.1 |
| **Biometric** | Fingerprint/Face authentication | 1.2.0-alpha05 |

### Third-Party Libraries

| Library | Purpose | Version |
|---------|---------|---------|
| **Retrofit** | HTTP client (update checks) | 2.11.0 |
| **Gson** | JSON serialization | Latest |
| **Coil** | Image loading | 2.6.0 |
| **MPAndroidChart** | Chart visualization | 3.1.0 |
| **OpenCSV** | CSV parsing | 5.9 |
| **iText7** | PDF generation | 7.2.6 |

### Development Tools

- **KSP (Kotlin Symbol Processing)**: 2.1.0-1.0.29
- **ProGuard**: Code shrinking and obfuscation
- **Version Catalog**: Centralized dependency management

---

## 🗄️ Database Schema

PaisaTracker uses **Room** with SQLite as the local database. Current schema version: **14**

### Entity Relationships

```
Projects (1) ──────< (N) Categories (1) ──────< (N) Expenses
                                                        │
                                                        │ (N)
                                                        │
                                                        ▼ (0..1)
                                              BankAccounts (1) ──────< (N) SalaryRecords
                                                        │
                                                        │ (N)
                                                        │
                                                        ▼ (0..1)
                                                    Budgets
```

### Core Entities

#### **Project**
```kotlin
@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "📁",
    val createdAt: Long,
    val lastModified: Long,
    val isCompleted: Boolean = false,
    val includeInSalary: Boolean = true
)
```

#### **Category**
```kotlin
@Entity(
    tableName = "categories",
    foreignKeys = [ForeignKey(
        entity = Project::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val projectId: Long,
    val emoji: String = "▶️",
    val createdAt: Long
)
```

#### **Expense**
```kotlin
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BankAccount::class,
            parentColumns = ["id"],
            childColumns = ["bankAccountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val date: Long,
    val description: String,
    val categoryId: Long,
    val assetPath: String? = null,
    val paymentMethod: String? = null,
    val paymentIcon: String? = null,
    val bankAccountId: Long? = null
)
```

#### **BankAccount**
```kotlin
@Entity(tableName = "bank_accounts")
data class BankAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val accountType: String, // BANK, CASH, CREDIT_CARD, DIGITAL_WALLET
    val bankName: String? = null,
    val initialBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val emoji: String = "🏦",
    val colorHex: String = "#2196F3",
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)
```

#### **Budget**
```kotlin
@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "💰",
    val limitAmount: Double,
    val period: BudgetPeriod, // DAILY, WEEKLY, MONTHLY, YEARLY
    val categoryId: Long? = null,
    val projectId: Long? = null,
    val createdAt: Long,
    val trackingStartAt: Long,
    val isActive: Boolean = true
)
```

#### **SalaryRecord**
```kotlin
@Entity(tableName = "salary_records")
data class SalaryRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val receivedAt: Long,
    val month: Int, // 1-12
    val year: Int,
    val note: String = "",
    val currency: String = "INR",
    val isRecurring: Boolean = false,
    val linkedAccountId: Long = 0,
    val sourceType: String, // PRIMARY, FREELANCE, BONUS, PASSIVE, OTHER
    val sourceName: String = "",
    val autoGenerated: Boolean = false,
    val isActive: Boolean = true
)
```

### Supporting Entities

- **Asset**: Receipt images and documents
- **BackupMetadata**: Backup file information
- **FlapData**: Calculator and notes state
- **ActionHistory**: Undo/redo tracking

### Database Migrations

The app includes **13 migrations** (v1 → v14) ensuring seamless upgrades:
- Migration 1→2: Backup metadata
- Migration 2→3: Emojis, payment methods, budgets
- Migration 3→4: Salary records
- Migration 4→5: UPI transactions (later removed)
- Migration 5→6: Data seeding
- Migration 6→7: Cleanup
- Migration 7→8: Budget tracking dates
- Migration 8→9: Project completion status
- Migration 9→10: Action history
- Migration 10→11: Salary-project linking
- Migration 11→12: Bank accounts
- Migration 12→13: Recurring salaries
- Migration 13→14: Multi-salary support

---

## 🔑 Key Components

### ViewModels

#### **PaisaTrackerViewModel**
Main ViewModel managing:
- Project CRUD operations
- Category management
- Expense tracking
- Budget operations
- Salary records
- Bank account management
- Search and filtering
- State management

#### **AnalyticsViewModel**
Dedicated to analytics:
- Time period selection
- Category spending analysis
- Monthly/yearly trends
- Statistical calculations
- Chart data preparation

#### **BankAccountViewModel**
Bank account specific:
- Account CRUD
- Balance management
- Transaction history
- Account filtering

#### **SalaryViewModel**
Salary management:
- Multi-source income tracking
- Monthly summaries
- Recurring salary generation
- Spending analysis

### Repositories

#### **PaisaTrackerRepository**
Single source of truth providing:
- 636 lines of data access methods
- Flow-based reactive queries
- Suspend functions for write operations
- Complex joins and aggregations
- Transaction management

#### **Preference Repositories**
- **ThemePreferencesRepository**: Theme selection
- **CurrencyPreferencesRepository**: Currency management
- **EmojiPreferencesRepository**: Emoji preferences
- **AppLockPreferences**: Security settings

### Utilities

#### **BackupManager**
- Full database backup to ZIP
- Asset file inclusion
- WAL checkpoint handling
- Restore functionality
- Metadata tracking

#### **UpdateManager**
- GitHub API integration
- Version comparison
- Update notifications
- Manual update checks

#### **BiometricHelper**
- Biometric authentication
- Fallback to PIN
- Device capability detection

#### **ImageUtils**
- Image compression
- File management
- URI handling
- Storage cleanup

#### **CurrencyUtils**
- Currency formatting
- Locale support
- Symbol display

#### **TimePeriodManager**
- Date range calculations
- Period definitions
- Calendar utilities

---

## 📱 Home Screen Widgets

PaisaTracker provides **4 Glance-powered widgets** for quick access:

### 1. Quick Balance Widget
- **Size**: 180x160dp, 280x180dp (responsive)
- **Features**:
  - Today's spending
  - Month's spending
  - Budget progress bar
  - Color-coded status
  - Over-budget warnings

### 2. Recent Transactions Widget
- **Features**:
  - Last 5 transactions
  - Amount and category
  - Date display
  - Quick expense overview

### 3. Budget Progress Widget
- **Features**:
  - Active budgets list
  - Progress indicators
  - Percentage used
  - Color-coded alerts

### 4. Salary Widget
- **Features**:
  - Current month salary
  - Total spending
  - Remaining balance
  - Spend percentage
  - Multi-salary support

All widgets use:
- Material 3 theming
- Automatic updates
- Tap to open app
- Battery-optimized refresh

---

## 🔒 Security Features

### App Lock Implementation

1. **PIN Setup**:
   - 4-6 digit PIN
   - Confirmation required
   - Stored in encrypted DataStore

2. **Biometric Integration**:
   - Android Biometric API
   - Fingerprint support
   - Face recognition (device-dependent)
   - Fallback to PIN

3. **Lock Behavior**:
   - Locks on app pause
   - State persistence
   - Secure unlock flow

### Data Security

- **Local Storage**: All data stored on device
- **No Cloud Sync**: Zero network data transmission
- **Encrypted Preferences**: DataStore encryption
- **Secure File Handling**: Private app directory
- **ProGuard**: Code obfuscation in release builds

---

## 💾 Data Management

### Backup Format

Backup files (`.backup` extension) are ZIP archives containing:

```
PaisaTracker_Backup_YYYYMMDD_HHMMSS.backup
├── database.db              # Main SQLite database
├── database.db-wal          # Write-Ahead Log (if exists)
├── database.db-shm          # Shared memory (if exists)
└── assets/                  # Receipt images
    ├── image1.jpg
    ├── image2.jpg
    └── ...
```

### Export Formats

#### CSV Structure
```csv
Date,Project,Category,Amount,Description,Payment Method
2024-01-15,Home,Groceries,1500.00,Weekly shopping,Cash
2024-01-16,Business,Office,3000.00,Supplies,Card
```

#### PDF Reports
- Professional formatting
- Date range headers
- Category summaries
- Total calculations
- iText7 generation

---

## 📥 Installation

### Download Options

1. **GitHub Releases** (Recommended):
   - Visit [Releases Page](https://github.com/harshal20m/PaisaTracker/releases/latest)
   - Download `paisatracker.apk`
   - Install on Android device

2. **Build from Source**: See [Building from Source](#-building-from-source)

### System Requirements

- **Android Version**: 9.0 (Pie) or higher (API 28+)
- **Storage**: ~50 MB for app + data
- **Permissions**:
  - Camera (optional, for receipt photos)
  - Storage (for backups and exports)
  - Notifications (for reminders)
  - Biometric (optional, for app lock)

---

## 🔨 Building from Source

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: 11 or higher
- **Gradle**: 8.13.2 (included via wrapper)
- **Git**: For cloning repository

### Build Steps

1. **Clone Repository**:
   ```bash
   git clone https://github.com/harshal20m/PaisaTracker.git
   cd PaisaTracker
   ```

2. **Open in Android Studio**:
   - File → Open → Select project directory
   - Wait for Gradle sync

3. **Configure Signing (Optional)**:
   Create `keystore.properties` in root:
   ```properties
   storeFile=/path/to/keystore.jks
   storePassword=your_store_password
   keyAlias=your_key_alias
   keyPassword=your_key_password
   ```

4. **Build APK**:
   ```bash
   # Debug build
   ./gradlew assembleDebug
   
   # Release build (requires keystore)
   ./gradlew assembleRelease
   ```

5. **Install on Device**:
   ```bash
   ./gradlew installDebug
   ```

### Build Variants

- **Debug**: Development build with debugging enabled
- **Release**: Optimized build with ProGuard enabled
  - Code shrinking
  - Resource shrinking
  - Obfuscation

### Version Configuration

Current version (in `build.gradle.kts`):
```kotlin
versionCode = 3
versionName = "v3.0.0"
```

---

## 📁 Project Structure

```
PaisaTracker/
├── src/
│   ├── main/
│   │   ├── java/com/example/paisatracker/
│   │   │   ├── data/                    # Data layer
│   │   │   │   ├── *Dao.kt             # Room DAOs
│   │   │   │   ├── *.kt                # Entity models
│   │   │   │   ├── PaisaTrackerDatabase.kt
│   │   │   │   └── PaisaTrackerRepository.kt
│   │   │   ├── domain/                  # Domain models
│   │   │   │   └── models/
│   │   │   ├── navigation/              # Navigation
│   │   │   │   └── AppNavigation.kt
│   │   │   ├── ui/                      # UI layer
│   │   │   │   ├── analytics/          # Analytics screen
│   │   │   │   ├── applock/            # App lock UI
│   │   │   │   ├── assets/             # Assets gallery
│   │   │   │   ├── bankaccount/        # Bank accounts
│   │   │   │   ├── bin/                # Recycle bin
│   │   │   │   ├── budget/             # Budget management
│   │   │   │   ├── common/             # Reusable components
│   │   │   │   ├── expense/            # Expense screens
│   │   │   │   ├── export/             # Export functionality
│   │   │   │   ├── finance/            # Finance dashboard
│   │   │   │   ├── flap/               # Quick access flap
│   │   │   │   ├── main/               # Main screens
│   │   │   │   ├── management/         # Data management
│   │   │   │   ├── Quickadd/           # Quick add sheet
│   │   │   │   ├── recent/             # Recent expenses
│   │   │   │   ├── salary/             # Salary tracking
│   │   │   │   ├── settings/           # Settings screens
│   │   │   │   ├── setup/              # First-time setup
│   │   │   │   └── tour/               # App tour
│   │   │   ├── util/                    # Utilities
│   │   │   │   ├── BackupManager.kt
│   │   │   │   ├── UpdateManager.kt
│   │   │   │   ├── BiometricHelper.kt
│   │   │   │   └── ...
│   │   │   ├── viewmodel/               # ViewModels
│   │   │   ├── widget/                  # Home screen widgets
│   │   │   ├── MainActivity.kt
│   │   │   └── PaisaTrackerApplication.kt
│   │   ├── res/                         # Resources
│   │   │   ├── drawable/               # Icons and images
│   │   │   ├── mipmap/                 # App icons
│   │   │   ├── values/                 # Strings, colors, themes
│   │   │   └── xml/                    # Widget configs
│   │   └── AndroidManifest.xml
│   ├── androidTest/                     # Instrumented tests
│   └── test/                            # Unit tests
├── gradle/
│   ├── libs.versions.toml              # Version catalog
│   └── wrapper/
├── build.gradle.kts                     # App build config
├── settings.gradle.kts                  # Project settings
├── proguard-rules.pro                   # ProGuard rules
├── keystore.properties                  # Signing config (gitignored)
├── AGENTS.md                            # AI agent instructions
└── README.md                            # This file
```

---

## 📜 Version History

### v3.0.0 (Current)
- Multi-salary support with source types
- Bank account integration
- Enhanced analytics with time periods
- 4 home screen widgets
- Improved backup system
- Material 3 updates
- Performance optimizations

### v2.x
- Budget tracking system
- Salary tracker
- Quick access flap
- App lock with biometric
- Export/Import functionality

### v1.x
- Initial release
- Basic expense tracking
- Project and category management
- Local database

---

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

### Reporting Issues

1. Check existing issues first
2. Provide detailed description
3. Include steps to reproduce
4. Attach screenshots if applicable
5. Mention Android version and device

### Submitting Pull Requests

1. Fork the repository
2. Create a feature branch
3. Follow coding standards (see `AGENTS.md`)
4. Write clear commit messages
5. Test thoroughly
6. Submit PR with description

### Coding Standards

- **Language**: 100% Kotlin
- **UI**: Pure Jetpack Compose (no XML)
- **Architecture**: Strict MVVM
- **Style**: Material 3 components only
- **State**: Use `collectAsStateWithLifecycle()`
- **Previews**: Every `@Composable` must have `@Preview`
- **Documentation**: KDoc for public APIs

---

## 📊 Analytics & Privacy

### Privacy-First Analytics

PaisaTracker includes **optional, developer-only analytics** to help understand app usage patterns. This is implemented with strict privacy controls:

#### What We Collect (Developer Only)
- ✅ App opens and active user count (anonymous)
- ✅ Aggregated feature usage (e.g., total projects created)
- ✅ Widget usage statistics
- ✅ Backup/restore events

#### What We DON'T Collect
- ❌ **No transaction amounts**
- ❌ **No expense descriptions**
- ❌ **No category or project names**
- ❌ **No personal information**
- ❌ **No financial data**
- ❌ **No location data**

### For End Users
- Analytics is **completely invisible** to end users
- No data collection prompts or dialogs
- Your financial data **never leaves your device**
- The app works identically with or without analytics

### For Contributors & Open Source Builds
Analytics is **disabled by default** for contributors:

1. **Default Configuration**: `analytics.enabled=false` in `local.properties`
2. **Dummy Config Included**: Project includes dummy `google-services.json` for building
3. **No Firebase Required**: Build and run without any Firebase setup
4. **Zero Overhead**: When disabled, analytics code has no runtime impact

### Setup Instructions

#### Building Without Analytics (Default)
```bash
# Clone the repository
git clone https://github.com/harshal20m/PaisaTracker.git
cd PaisaTracker

# Build normally - analytics is disabled by default
./gradlew assembleDebug
```

#### Enabling Analytics (Developer Only)
See [ANALYTICS_SETUP.md](ANALYTICS_SETUP.md) for detailed instructions on:
- Setting up Firebase project
- Configuring `google-services.json`
- Enabling analytics in `local.properties`
- Viewing analytics dashboard

### Transparency Commitment
- All analytics code is open source and auditable
- Analytics implementation is documented in `AnalyticsManager.kt`
- No third-party tracking SDKs beyond Firebase Analytics
- Complies with GDPR and privacy best practices

---

## 👨‍💻 Author

**Harshal Mali**

- GitHub: [@harshal20m](https://github.com/harshal20m)
- Email: [Contact via GitHub]

---

## 📄 License

This project is open source. Please check the repository for license details.

---

## 🙏 Acknowledgments

- **Jetpack Compose Team**: For the modern UI toolkit
- **Material Design**: For design guidelines
- **Open Source Community**: For libraries and inspiration

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/harshal20m/PaisaTracker/issues)
- **Discussions**: [GitHub Discussions](https://github.com/harshal20m/PaisaTracker/discussions)
- **Updates**: [GitHub Releases](https://github.com/harshal20m/PaisaTracker/releases)

---

<p align="center">
  <strong>Made with ❤️ by Harshal Mali</strong><br>
  <sub>A privacy-first approach to personal finance</sub>
</p>

<p align="center">
  <sub>⭐ Star this repo if you find it useful!</sub>
</p>
