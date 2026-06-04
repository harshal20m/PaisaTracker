package com.example.paisatracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class CategoryWithTotal(
    @Embedded
    val category: Category,
    val totalAmount: Double,
    val expenseCount: Int,
    val creditCount: Int = 0,
    val latestExpenseTime: Long? = null
)

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE projectId = :projectId ORDER BY name ASC")
    fun getCategoriesForProject(projectId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE projectId = :projectId ORDER BY name ASC")
    suspend fun getCategoriesForProjectList(projectId: Long): List<Category>


    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryById(categoryId: Long): Flow<Category>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryByIdSync(categoryId: Long): Category?

    @Query("SELECT * FROM categories WHERE name = :name AND projectId = :projectId LIMIT 1")
    suspend fun getCategoryByName(name: String, projectId: Long): Category?

    @Query("""
    SELECT
        c.*,
        COALESCE(SUM(e.amount), 0.0) AS totalAmount,
        COUNT(CASE WHEN e.amount > 0 THEN e.id END) AS expenseCount,
        COUNT(CASE WHEN e.amount < 0 THEN e.id END) AS creditCount,
        MAX(e.date) AS latestExpenseTime
    FROM categories c
    LEFT JOIN expenses e ON c.id = e.categoryId
    WHERE c.projectId = :projectId
    GROUP BY c.id
    ORDER BY c.name ASC
""")
    fun getCategoriesWithTotalForProject(projectId: Long): Flow<List<CategoryWithTotal>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategoriesList(): List<Category>

    /**
     * Get category by name (without project filter).
     * Used by SMS transaction processor to find existing categories.
     *
     * @param name Category name
     * @return Category if found, null otherwise
     */
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?

    /**
     * Insert a new category and return its ID.
     * Used by SMS transaction processor to create new categories.
     *
     * @param category Category to insert
     * @return ID of inserted category
     */
    @Insert
    suspend fun insert(category: Category): Long

    /**
     * Get the default project (first active project).
     * Used by SMS transaction processor when creating categories.
     *
     * @return First active project or null
     */
    @Query("SELECT * FROM projects WHERE isCompleted = 0 ORDER BY id ASC LIMIT 1")
    suspend fun getDefaultProject(): Project?
}