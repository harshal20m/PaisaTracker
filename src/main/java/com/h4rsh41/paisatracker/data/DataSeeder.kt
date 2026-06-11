// DataSeeder.kt
package com.h4rsh41.paisatracker.data

import android.content.Context
import androidx.core.content.edit

class DataSeeder(private val repository: PaisaTrackerRepository) {

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_DATA_SEEDED = "data_seeded"
        private const val KEY_USER_CHOICE = "user_choice"
        private const val KEY_TOUR_SHOWN = "tour_shown"
        private const val KEY_DEFAULT_PROJECT_SEEDED = "default_project_seeded"

        @Volatile
        private var INSTANCE: DataSeeder? = null

        fun getInstance(repository: PaisaTrackerRepository): DataSeeder {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataSeeder(repository).also { INSTANCE = it }
            }
        }
    }

    fun shouldShowFirstTimeSetup(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Show tour if it hasn't been shown yet
        return !prefs.getBoolean(KEY_TOUR_SHOWN, false)
    }
    
    fun markTourAsShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_TOUR_SHOWN, true)
        }
    }

    suspend fun seedInitialDataIfUserAccepts(context: Context, shouldSeed: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (shouldSeed) {
            seedProjects()
            seedCategories()
        }

        // Mark as seeded regardless of choice (so dialog doesn't show again)
        prefs.edit {
            putBoolean(KEY_DATA_SEEDED, true)
            putBoolean(KEY_USER_CHOICE, shouldSeed)
        }
    }

    /**
     * Seeds a default "Daily Expenses" project with common categories
     * that match SMS transaction patterns for automatic detection
     */
    suspend fun seedDefaultDailyExpensesProject(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val alreadySeeded = prefs.getBoolean(KEY_DEFAULT_PROJECT_SEEDED, false)
        
        // Don't seed if already done
        if (alreadySeeded) return
        
        try {
            // Create Daily Expenses project
            val project = Project(
                name = "Daily Expenses",
                emoji = "💰",
                createdAt = System.currentTimeMillis(),
                includeInSalary = true
            )
            repository.insertProject(project)
            
            // Get the inserted project to get its ID
            val insertedProject = repository.getAllProjectsList().find {
                it.name == "Daily Expenses" && it.emoji == "💰"
            }
            
            insertedProject?.let { proj ->
                // Create categories with SMS-matching names
                val categories = listOf(
                    Category(projectId = proj.id, name = "Groceries", emoji = "🛒", createdAt = System.currentTimeMillis()),
                    Category(projectId = proj.id, name = "Food & Dining", emoji = "🍽️", createdAt = System.currentTimeMillis()),
                    Category(projectId = proj.id, name = "Fuel", emoji = "⛽", createdAt = System.currentTimeMillis()),
                    Category(projectId = proj.id, name = "Transportation", emoji = "🚕", createdAt = System.currentTimeMillis()),
                    Category(projectId = proj.id, name = "Shopping", emoji = "🛍️", createdAt = System.currentTimeMillis()),
                    Category(projectId = proj.id, name = "Healthcare", emoji = "💊", createdAt = System.currentTimeMillis()),
                    Category(projectId = proj.id, name = "Utilities", emoji = "💡", createdAt = System.currentTimeMillis()),
                    Category(projectId = proj.id, name = "Mobile & Recharge", emoji = "📱", createdAt = System.currentTimeMillis()),
                    Category(projectId = proj.id, name = "Entertainment", emoji = "🎬", createdAt = System.currentTimeMillis()),
                    Category(projectId = proj.id, name = "Others", emoji = "💳", createdAt = System.currentTimeMillis())
                )
                
                categories.forEach { category ->
                    repository.insertCategory(category)
                }
            }
            
            // Mark as seeded
            prefs.edit {
                putBoolean(KEY_DEFAULT_PROJECT_SEEDED, true)
            }
        } catch (e: Exception) {
            // Log error but don't crash the app
            e.printStackTrace()
        }
    }
    
    /**
     * Removes the default project if user chooses "Start Fresh"
     */
    suspend fun removeDefaultProject(context: Context) {
        try {
            val projects = repository.getAllProjectsList()
            val defaultProject = projects.find { it.name == "Daily Expenses" && it.emoji == "💰" }
            
            defaultProject?.let {
                repository.deleteProject(it)
            }
            
            // Reset the flag so it can be seeded again if needed
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                putBoolean(KEY_DEFAULT_PROJECT_SEEDED, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Marks the setup as complete
     */
    fun markSetupComplete(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_DATA_SEEDED, true)
        }
    }

    private suspend fun seedProjects() {

        val existingProjects = repository.getAllProjectsList()
        val existingProjectNames = existingProjects.map { it.name }.toSet()

        val defaultProjects = listOf(
            Project(name = "Daily Living", emoji = "🏠", createdAt = System.currentTimeMillis()),
            Project(name = "Food & Dining", emoji = "🍔", createdAt = System.currentTimeMillis()),
            Project(name = "Transportation", emoji = "🚗", createdAt = System.currentTimeMillis()),
            Project(name = "Shopping", emoji = "🛍️", createdAt = System.currentTimeMillis()),
            Project(name = "Entertainment", emoji = "🎬", createdAt = System.currentTimeMillis()),
            Project(name = "Bills & Utilities", emoji = "💡", createdAt = System.currentTimeMillis()),
            Project(name = "Health & Wellness", emoji = "💊", createdAt = System.currentTimeMillis()),
            Project(name = "Education", emoji = "📚", createdAt = System.currentTimeMillis())
        )

        // Only insert projects that don't already exist
        defaultProjects.forEach { project ->
            if (project.name !in existingProjectNames) {
                repository.insertProject(project)
            }
        }
    }

    private suspend fun seedCategories() {
        // Get all projects (you'll need to add a suspend function to get all projects)
        val projects = repository.getAllProjectsList()
        val projectMap = projects.associateBy { it.name }

        // Get existing categories to avoid duplicates
        val existingCategories = mutableSetOf<String>()
        projects.forEach { project ->
            val categories = repository.getCategoriesForProjectList(project.id)
            existingCategories.addAll(categories.map { "${project.name}:${it.name}" })
        }
        val defaultCategories = mutableListOf<Category>()

        // Helper to add category if not exists
        fun addCategoryIfNotExists(projectName: String, categoryName: String, emoji: String) {
            val key = "$projectName:$categoryName"
            if (key !in existingCategories) {
                projectMap[projectName]?.let { project ->
                    defaultCategories.add(
                        Category(
                            projectId = project.id,
                            name = categoryName,
                            emoji = emoji,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        // Daily Living categories
        addCategoryIfNotExists("Daily Living", "Groceries", "🛒")
        addCategoryIfNotExists("Daily Living", "Household Items", "🧹")
        addCategoryIfNotExists("Daily Living", "Personal Care", "🧴")

        // Food & Dining categories
        addCategoryIfNotExists("Food & Dining", "Restaurants", "🍽️")
        addCategoryIfNotExists("Food & Dining", "Coffee & Snacks", "☕")
        addCategoryIfNotExists("Food & Dining", "Takeout", "🥡")

        // Transportation categories
        addCategoryIfNotExists("Transportation", "Fuel", "⛽")
        addCategoryIfNotExists("Transportation", "Public Transit", "🚌")
        addCategoryIfNotExists("Transportation", "Ride Share", "🚕")
        addCategoryIfNotExists("Transportation", "Parking", "🅿️")

        // Shopping categories
        addCategoryIfNotExists("Shopping", "Clothing", "👕")
        addCategoryIfNotExists("Shopping", "Electronics", "📱")
        addCategoryIfNotExists("Shopping", "Gifts", "🎁")

        // Entertainment categories
        addCategoryIfNotExists("Entertainment", "Movies", "🎬")
        addCategoryIfNotExists("Entertainment", "Streaming Services", "📺")
        addCategoryIfNotExists("Entertainment", "Games", "🎮")
        addCategoryIfNotExists("Entertainment", "Events & Concerts", "🎵")

        // Bills & Utilities categories
        addCategoryIfNotExists("Bills & Utilities", "Electricity", "⚡")
        addCategoryIfNotExists("Bills & Utilities", "Water", "💧")
        addCategoryIfNotExists("Bills & Utilities", "Internet", "🌐")
        addCategoryIfNotExists("Bills & Utilities", "Mobile", "📱")
        addCategoryIfNotExists("Bills & Utilities", "Rent", "🏠")

        // Health & Wellness categories
        addCategoryIfNotExists("Health & Wellness", "Pharmacy", "💊")
        addCategoryIfNotExists("Health & Wellness", "Doctor Visits", "👨‍⚕️")
        addCategoryIfNotExists("Health & Wellness", "Gym", "🏋️")
        addCategoryIfNotExists("Health & Wellness", "Insurance", "🛡️")

        // Education categories
        addCategoryIfNotExists("Education", "Books", "📖")
        addCategoryIfNotExists("Education", "Courses", "🎓")
        addCategoryIfNotExists("Education", "Supplies", "✏️")

        defaultCategories.forEach { category ->
            repository.insertCategory(category)
        }
    }
}