# 🏦 PaisaTracker Architecture & Use-Case Flow

PaisaTracker is a modern, offline-first expense manager designed for precision tracking and privacy. This document outlines the core data structures, application flows, and architectural principles.

---

## 🏗 Data Schema (Core Elements)

The application follows a hierarchical data model implemented using **Room Database**.

### 1. **Project** (The Root)
*   **Purpose:** High-level buckets for tracking different life areas.
*   **Actual Fields:** `id`, `name`, `emoji`, `createdAt`, `lastModified`, `isCompleted`, `includeInSalary`.
*   **Relationship:** One Project has many Categories.

### 2. **Category** (The Label)
*   **Purpose:** Specific classifications within a project.
*   **Actual Fields:** `id`, `name`, `projectId`, `emoji`, `createdAt`.
*   **Relationship:** Belongs to a Project; has many Expenses.

### 3. **Expense** (The Transaction)
*   **Purpose:** Individual monetary records.
*   **Actual Fields:** `id`, `amount`, `date`, `description`, `categoryId`, `assetPath`, `paymentMethod`, `paymentIcon`, `bankAccountId`.
*   **Relationship:** Belongs to a Category. Optionally linked to a Bank Account.

### 4. **Asset** (The Proof)
*   **Purpose:** Local image storage for receipts.
*   **Actual Fields:** `id`, `imagePath`, `title`, `description`, `timestamp`, `expenseId`.
*   **Relationship:** Can be independent or linked to an Expense via `expenseId`.

### 5. **Budget** (The Constraint)
*   **Purpose:** Defining spending limits for specific periods.
*   **Actual Fields:** `id`, `name`, `emoji`, `limitAmount`, `period` (Daily/Weekly/Monthly/Yearly), `categoryId`, `projectId`, `createdAt`, `trackingStartAt`, `isActive`.
*   **Relationship:** Can be global (null IDs) or scoped to a specific Project or Category.

---

## 🛤 Complete User Flow

### Phase 1: Onboarding & Configuration
1.  **Launch:** User starts the app.
2.  **App Tour:** A visual introduction to the core value propositions.
3.  **Setup:** Selecting base currency and seeding initial sample data (optional).

### Phase 2: Structural Setup
1.  **Create Project:** User uses the "Create Project" header action in the **Projects Screen**.
2.  **Add Categories:** Inside a project, the user defines categories with custom emojis.

### Phase 3: Daily Operation (The Loop)
1.  **Log Expense:**
    *   **Quick Add:** Use the global **Lightning FAB** to quickly log an expense with project/category auto-detection.
    *   **Direct Entry:** Navigate into a specific category and add a transaction.
2.  **Attach Receipts:** Capture or upload images to store them locally as **Assets**.

### Phase 4: Monitoring & Analysis
1.  **Dashboard:** View the **Weekly Calendar** and recent transactions on the Home screen.
2.  **Project Overview:** Track progress and spending in the project list.
3.  **Insights:** Detailed charts and spending breakdowns per project.

### Phase 5: Data Governance
1.  **Recycle Bin:** Deleted items are moved to the **Bin** for 30 days before permanent removal.
2.  **System Backup:** Create encrypted full backups (Database + Assets) to local storage.
3.  **Export:**
    *   **CSV:** Raw data for spreadsheet manipulation with smart project mapping.
    *   **PDF:** Structured, printable reports for professional record-keeping.

---

## 🛠 Technical Principles

*   **MVVM Architecture:** Clean separation between UI (Compose), State (ViewModel), and Data (Repository/DAO).
*   **Offline First:** All data is processed via `Dispatchers.IO` and stored locally in Room. No external network calls for private data.
*   **State Management:** Reactive UI updates using `StateFlow` and `collectAsStateWithLifecycle`.
*   **Material 3:** Modern design system with dynamic coloring and high-fidelity animations.

---
*Document Version: 1.1 (Aligned with PaisaTracker v3.0 Data Models)*
