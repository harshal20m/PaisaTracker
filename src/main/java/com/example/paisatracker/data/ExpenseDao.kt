package com.example.paisatracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Add this data class to your data package
data class RecentExpense(
    val id: Long,
    val amount: Double,
    val description: String,
    val date: Long,
    val paymentMethod: String?,
    val paymentIcon: String?,
    val projectId: Long,
    val projectName: String,
    val projectEmoji: String,
    val categoryId: Long,
    val categoryName: String,
    val categoryEmoji: String
)

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExpense(expense: Expense)

    @Insert
    suspend fun insert(expense: Expense): Long


    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    fun getExpenseById(id: Long): Flow<Expense?>
    
    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseByIdOnce(id: Long): Expense?

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)


     @Query("DELETE FROM expenses WHERE id = :id")
     suspend fun deleteById(id: Long)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getExpensesForCategory(categoryId: Long): Flow<List<Expense>>

    @Query("""
        SELECT * FROM expenses
        WHERE categoryId = :categoryId
        AND date >= :startOfYear
        AND date < :endOfYear
        ORDER BY date DESC
    """)
    suspend fun getExpensesForCategoryByYear(
        categoryId: Long,
        startOfYear: Long,
        endOfYear: Long
    ): List<Expense>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY date DESC")
    suspend fun getExpensesForCategoryList(categoryId: Long): List<Expense>

    @Query("""
    SELECT
        p.name AS projectName,
        p.emoji AS projectEmoji,
        c.name AS categoryName,
        c.emoji AS categoryEmoji,
        e.description AS description,
        e.amount AS amount,
        e.date AS date,
        e.paymentMethod AS paymentMethod,
        e.paymentIcon AS paymentMethodEmoji
    FROM expenses e
    INNER JOIN categories c ON e.categoryId = c.id
    INNER JOIN projects p ON c.projectId = p.id
    WHERE (:projectId IS NULL OR p.id = :projectId)
    ORDER BY p.name, c.name, e.date
""")
    suspend fun getExportRows(projectId: Long? = null): List<ExportRow>

    @Query("SELECT COUNT(*) FROM expenses WHERE amount > 0")
    suspend fun getExpenseCount(): Int

    @Query("SELECT COALESCE(SUM(CASE WHEN amount > 0 THEN amount ELSE 0 END), 0.0) FROM expenses")
    suspend fun getTotalAmount(): Double?

    @Query("""
    SELECT
        e.id as id,
        e.amount as amount,
        e.description as description,
        e.date as date,
        e.paymentMethod as paymentMethod,
        e.paymentIcon as paymentIcon,
        c.projectId as projectId,
        p.name as projectName,
        p.emoji as projectEmoji,
        c.id as categoryId,
        c.name as categoryName,
        c.emoji as categoryEmoji
    FROM expenses e
    INNER JOIN categories c ON e.categoryId = c.id
    INNER JOIN projects p ON c.projectId = p.id
    ORDER BY e.date DESC
    LIMIT :limit
""")
    fun getRecentExpensesWithDetails(limit: Int): Flow<List<RecentExpense>>

    @Query("""
    SELECT
        e.id as id,
        e.amount as amount,
        e.description as description,
        e.date as date,
        e.paymentMethod as paymentMethod,
        e.paymentIcon as paymentIcon,
        c.projectId as projectId,
        p.name as projectName,
        p.emoji as projectEmoji,
        c.id as categoryId,
        c.name as categoryName,
        c.emoji as categoryEmoji
    FROM expenses e
    INNER JOIN categories c ON e.categoryId = c.id
    INNER JOIN projects p ON c.projectId = p.id
    ORDER BY e.date DESC
""")
    fun getAllExpensesWithDetails(): Flow<List<RecentExpense>>

    // New search queries

    @Query("""
        SELECT
            e.id as id,
            e.amount as amount,
            e.description as description,
            e.date as date,
            e.paymentMethod as paymentMethod,
            e.paymentIcon as paymentIcon,
            c.projectId as projectId,
            p.name as projectName,
            p.emoji as projectEmoji,
            c.id as categoryId,
            c.name as categoryName,
            c.emoji as categoryEmoji
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE (:query IS NULL OR e.description LIKE '%' || :query || '%')
        AND (:projectId IS NULL OR p.id = :projectId)
        ORDER BY e.date DESC
    """)
    fun searchExpensesByDescription(query: String?, projectId: Long?): Flow<List<RecentExpense>>

    @Query("""
        SELECT
            e.id as id,
            e.amount as amount,
            e.description as description,
            e.date as date,
            e.paymentMethod as paymentMethod,
            e.paymentIcon as paymentIcon,
            c.projectId as projectId,
            p.name as projectName,
            p.emoji as projectEmoji,
            c.id as categoryId,
            c.name as categoryName,
            c.emoji as categoryEmoji
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE e.bankAccountId = :bankAccountId
        ORDER BY e.date DESC
    """)
    fun getExpensesByBankAccount(bankAccountId: Long): Flow<List<RecentExpense>>

    @Query("""
        SELECT
            e.id as id,
            e.amount as amount,
            e.description as description,
            e.date as date,
            e.paymentMethod as paymentMethod,
            e.paymentIcon as paymentIcon,
            c.projectId as projectId,
            p.name as projectName,
            p.emoji as projectEmoji,
            c.id as categoryId,
            c.name as categoryName,
            c.emoji as categoryEmoji
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE (:minAmount IS NULL OR e.amount >= :minAmount)
        AND (:maxAmount IS NULL OR e.amount <= :maxAmount)
        AND (:projectId IS NULL OR p.id = :projectId)
        ORDER BY e.date DESC
    """)
    fun searchExpensesByAmount(minAmount: Double?, maxAmount: Double?, projectId: Long?): Flow<List<RecentExpense>>

    @Query("""
        SELECT
            e.id as id,
            e.amount as amount,
            e.description as description,
            e.date as date,
            e.paymentMethod as paymentMethod,
            e.paymentIcon as paymentIcon,
            c.projectId as projectId,
            p.name as projectName,
            p.emoji as projectEmoji,
            c.id as categoryId,
            c.name as categoryName,
            c.emoji as categoryEmoji
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE (:startDate IS NULL OR e.date >= :startDate)
        AND (:endDate IS NULL OR e.date <= :endDate)
        AND (:projectId IS NULL OR p.id = :projectId)
        ORDER BY e.date DESC
    """)
    fun searchExpensesByDateRange(startDate: Long?, endDate: Long?, projectId: Long?): Flow<List<RecentExpense>>

    // ============================================================================
    // NEW ANALYTICS QUERIES - Added for Sprint 2 (Time-based Analytics)
    // These queries support the new TimePeriod and analytics features
    // ============================================================================

    /**
     * Get expenses within a specific date range.
     * Used for filtering expenses by time period (week, month, year, custom).
     *
     * @param startDate Start timestamp (inclusive)
     * @param endDate End timestamp (inclusive)
     * @return Flow of expenses in the date range
     */
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>

    /**
     * Get monthly aggregated totals for the last N months.
     * Returns month in format "YYYY-MM", total amount, and count of expenses.
     * Only counts positive amounts (debits/expenses), excludes negative amounts (credits).
     *
     * @param months Number of months to retrieve (default 12)
     * @return Flow of monthly totals
     */
    @Query("""
        SELECT
            strftime('%Y-%m', date/1000, 'unixepoch') as month,
            SUM(CASE WHEN e.amount > 0 THEN e.amount ELSE 0 END) as total,
            COUNT(CASE WHEN e.amount > 0 THEN 1 END) as count
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE p.isCompleted = 0
        GROUP BY month
        ORDER BY month DESC
        LIMIT :months
    """)
    fun getMonthlyTotals(months: Int = 12): Flow<List<com.example.paisatracker.domain.models.MonthlyTotal>>

    /**
     * Get yearly aggregated totals for all years with expenses.
     * Returns year, total amount, and count of expenses.
     * Only counts positive amounts (debits/expenses), excludes negative amounts (credits).
     *
     * @return Flow of yearly totals
     */
    @Query("""
        SELECT
            strftime('%Y', date/1000, 'unixepoch') as year,
            SUM(CASE WHEN e.amount > 0 THEN e.amount ELSE 0 END) as total,
            COUNT(CASE WHEN e.amount > 0 THEN 1 END) as count
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE p.isCompleted = 0
        GROUP BY year
        ORDER BY year DESC
    """)
    fun getYearlyTotals(): Flow<List<com.example.paisatracker.domain.models.YearlyTotal>>

    /**
     * Get category-wise spending for a specific date range.
     * Includes category details and aggregated amounts.
     * Only counts positive amounts (debits/expenses), excludes negative amounts (credits).
     *
     * @param startDate Start timestamp (inclusive)
     * @param endDate End timestamp (inclusive)
     * @return Flow of category spending data
     */
    @Query("""
        SELECT
            c.id as categoryId,
            c.name as categoryName,
            c.emoji as categoryIcon,
            SUM(CASE WHEN e.amount > 0 THEN e.amount ELSE 0 END) as total,
            COUNT(CASE WHEN e.amount > 0 THEN 1 END) as count,
            0.0 as percentage
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE e.date BETWEEN :startDate AND :endDate
        AND p.isCompleted = 0
        GROUP BY c.id, c.name, c.emoji
        HAVING total > 0
        ORDER BY total DESC
    """)
    fun getCategorySpendingByDateRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<com.example.paisatracker.domain.models.CategorySpending>>

    /**
     * Get total spending for a specific date range.
     * Used for calculating overall spending in a time period.
     * Only sums positive amounts (debits/expenses), excludes negative amounts (credits).
     *
     * @param startDate Start timestamp (inclusive)
     * @param endDate End timestamp (inclusive)
     * @return Total amount spent, or 0.0 if no expenses
     */
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN e.amount > 0 THEN e.amount ELSE 0 END), 0.0)
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE e.date BETWEEN :startDate AND :endDate
        AND p.isCompleted = 0
    """)
    suspend fun getTotalByDateRange(startDate: Long, endDate: Long): Double

    /**
     * Get expense count for a specific date range.
     * Used for analytics and statistics.
     * Only counts positive amounts (debits/expenses), excludes negative amounts (credits).
     *
     * @param startDate Start timestamp (inclusive)
     * @param endDate End timestamp (inclusive)
     * @return Number of expenses in the date range
     */
    @Query("""
        SELECT COUNT(*)
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE e.date BETWEEN :startDate AND :endDate
        AND e.amount > 0
        AND p.isCompleted = 0
    """)
    suspend fun getCountByDateRange(startDate: Long, endDate: Long): Int

    /**
     * Get monthly totals for a specific year.
     * Returns all 12 months with their totals (0 if no expenses).
     * Only counts positive amounts (debits/expenses), excludes negative amounts (credits).
     *
     * @param year Year to query (e.g., 2024)
     * @return Flow of monthly totals for the year
     */
    @Query("""
        SELECT
            strftime('%Y-%m', date/1000, 'unixepoch') as month,
            SUM(CASE WHEN e.amount > 0 THEN e.amount ELSE 0 END) as total,
            COUNT(CASE WHEN e.amount > 0 THEN 1 END) as count
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE strftime('%Y', date/1000, 'unixepoch') = :year
        AND p.isCompleted = 0
        GROUP BY month
        ORDER BY month ASC
    """)
    fun getMonthlyTotalsForYear(year: String): Flow<List<com.example.paisatracker.domain.models.MonthlyTotal>>

    /**
     * Get top N categories by spending in a date range.
     * Useful for "Top Spending Categories" analytics.
     * Only counts positive amounts (debits/expenses), excludes negative amounts (credits).
     *
     * @param startDate Start timestamp (inclusive)
     * @param endDate End timestamp (inclusive)
     * @param limit Number of top categories to return
     * @return Flow of top category spending data
     */
    @Query("""
        SELECT
            c.id as categoryId,
            c.name as categoryName,
            c.emoji as categoryIcon,
            SUM(CASE WHEN e.amount > 0 THEN e.amount ELSE 0 END) as total,
            COUNT(CASE WHEN e.amount > 0 THEN 1 END) as count,
            0.0 as percentage
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE e.date BETWEEN :startDate AND :endDate
        AND p.isCompleted = 0
        GROUP BY c.id, c.name, c.emoji
        HAVING total > 0
        ORDER BY total DESC
        LIMIT :limit
    """)
    fun getTopCategoriesByDateRange(
        startDate: Long,
        endDate: Long,
        limit: Int = 5
    ): Flow<List<com.example.paisatracker.domain.models.CategorySpending>>

    /**
     * Get average daily spending for a date range.
     * Useful for "Daily Average" analytics.
     * Only counts positive amounts (debits/expenses), excludes negative amounts (credits).
     *
     * @param startDate Start timestamp (inclusive)
     * @param endDate End timestamp (inclusive)
     * @return Average spending per day
     */
    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN e.amount > 0 THEN e.amount ELSE 0 END) /
                (CAST(((:endDate - :startDate) / 86400000) AS REAL) + 1), 0.0)
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE e.date BETWEEN :startDate AND :endDate
        AND p.isCompleted = 0
    """)
    suspend fun getAverageDailySpending(startDate: Long, endDate: Long): Double

    // ============================================================================
    // BUDGET-SALARY INTEGRATION QUERIES - Added for Sprint 7
    // These queries support budget tracking and financial health calculations
    // ============================================================================

    /**
     * Get total spending for a specific category within a date range.
     * Used for category-specific budget tracking.
     * Only sums positive amounts (debits/expenses), excludes negative amounts (credits).
     *
     * @param categoryId Category ID to filter by
     * @param startDate Start timestamp (inclusive)
     * @param endDate End timestamp (inclusive)
     * @return Total amount spent in the category
     */
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN e.amount > 0 THEN e.amount ELSE 0 END), 0.0)
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE e.categoryId = :categoryId
        AND e.date BETWEEN :startDate AND :endDate
        AND p.isCompleted = 0
    """)
    suspend fun getTotalByCategoryAndDateRange(
        categoryId: Long,
        startDate: Long,
        endDate: Long
    ): Double

    /**
     * Get total spending for a specific project within a date range.
     * Used for project-specific budget tracking.
     * Only sums positive amounts (debits/expenses), excludes negative amounts (credits).
     *
     * @param projectId Project ID to filter by
     * @param startDate Start timestamp (inclusive)
     * @param endDate End timestamp (inclusive)
     * @return Total amount spent in the project
     */
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN e.amount > 0 THEN e.amount ELSE 0 END), 0.0)
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE c.projectId = :projectId
        AND e.date BETWEEN :startDate AND :endDate
        AND p.isCompleted = 0
    """)
    suspend fun getTotalByProjectAndDateRange(
        projectId: Long,
        startDate: Long,
        endDate: Long
    ): Double

    /**
     * Get expense by transaction hash for duplicate detection.
     * Used by SMS transaction processor to avoid duplicate entries.
     *
     * @param hash Transaction hash
     * @return Expense if found, null otherwise
     */
    @Query("SELECT * FROM expenses WHERE description LIKE '%' || :hash || '%' LIMIT 1")
    suspend fun getExpenseByHash(hash: String): Expense?

    /**
     * Find similar expense by amount, date range, and description similarity
     * Used for deduplication during SMS history scan
     *
     * @param amount Transaction amount
     * @param startTime Start of time window (transaction time - 5 minutes)
     * @param endTime End of time window (transaction time + 5 minutes)
     * @param description Transaction description (for LIKE search)
     * @return Expense if found, null otherwise
     */
    @Query("""
        SELECT * FROM expenses
        WHERE ABS(amount - :amount) < 0.01
        AND date >= :startTime
        AND date <= :endTime
        AND description LIKE '%' || :description || '%'
        LIMIT 1
    """)
    suspend fun findSimilarExpense(
        amount: Double,
        startTime: Long,
        endTime: Long,
        description: String
    ): Expense?

    /**
     * Get related expenses based on similar description or amount
     * Used in ExpenseDetailScreen to show similar transactions
     *
     * @param expenseId Current expense ID to exclude
     * @param description Description to match (merchant name)
     * @param amount Amount to find similar transactions
     * @param amountTolerance Tolerance for amount matching (default 10%)
     * @param limit Maximum number of related expenses to return
     * @return Flow of related expenses with category and project info
     */
    @Query("""
        SELECT
            e.id,
            e.amount,
            e.description,
            e.date,
            e.paymentMethod,
            e.paymentIcon,
            p.id as projectId,
            p.name as projectName,
            p.emoji as projectEmoji,
            c.id as categoryId,
            c.name as categoryName,
            c.emoji as categoryEmoji
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        INNER JOIN projects p ON c.projectId = p.id
        WHERE e.id != :expenseId
        AND p.isCompleted = 0
        AND (
            e.description LIKE '%' || :description || '%'
            OR ABS(e.amount - :amount) <= (:amount * :amountTolerance / 100.0)
        )
        ORDER BY
            CASE
                WHEN e.description LIKE '%' || :description || '%' THEN 0
                ELSE 1
            END,
            ABS(e.amount - :amount) ASC,
            e.date DESC
        LIMIT :limit
    """)
    fun getRelatedExpenses(
        expenseId: Long,
        description: String,
        amount: Double,
        amountTolerance: Double = 10.0,
        limit: Int = 5
    ): Flow<List<RecentExpense>>
}