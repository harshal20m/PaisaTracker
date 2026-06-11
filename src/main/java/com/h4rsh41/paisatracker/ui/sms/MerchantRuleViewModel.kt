package com.h4rsh41.paisatracker.ui.sms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.h4rsh41.paisatracker.data.Category
import com.h4rsh41.paisatracker.data.CategoryDao
import com.h4rsh41.paisatracker.data.MerchantRuleEntity
import com.h4rsh41.paisatracker.data.MerchantRuleRepository
import com.h4rsh41.paisatracker.data.PaisaTrackerDatabase
import com.h4rsh41.paisatracker.data.Project
import com.h4rsh41.paisatracker.data.ProjectDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MerchantRuleViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PaisaTrackerDatabase.getDatabase(application)
    private val merchantRuleRepository = MerchantRuleRepository(
        database.merchantRuleDao(),
        application.applicationContext
    )
    private val categoryDao: CategoryDao = database.categoryDao()
    private val projectDao: ProjectDao = database.projectDao()

    // Merchant rules
    val merchantRules = merchantRuleRepository.getAllRules()

    // Categories and projects for selection
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadCategoriesAndProjects()
        seedDefaultRules()
    }
    
    /**
     * Seed default merchant rules on first app launch
     */
    private fun seedDefaultRules() {
        viewModelScope.launch {
            try {
                merchantRuleRepository.seedDefaultRulesIfNeeded(categoryDao)
            } catch (e: Exception) {
                // Silently fail - not critical
            }
        }
    }

    private fun loadCategoriesAndProjects() {
        viewModelScope.launch {
            try {
                _categories.value = categoryDao.getAllCategoriesList()
                _projects.value = projectDao.getAllProjectsList()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load categories and projects: ${e.message}"
            }
        }
    }

    /**
     * Add a new merchant rule
     */
    fun addRule(
        merchantPattern: String,
        categoryId: Long,
        projectId: Long?,
        priority: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val rule = MerchantRuleEntity(
                    merchantPattern = merchantPattern.trim(),
                    categoryId = categoryId,
                    projectId = projectId,
                    priority = priority,
                    isActive = true,
                    matchCount = 0,
                    lastMatchedAt = null,
                    createdAt = LocalDateTime.now()
                )
                merchantRuleRepository.insert(rule)
                _successMessage.value = "Rule added successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add rule: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing merchant rule
     */
    fun updateRule(
        id: Long,
        merchantPattern: String,
        categoryId: Long,
        projectId: Long?,
        priority: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val existingRule = merchantRuleRepository.getById(id)
                if (existingRule != null) {
                    val updatedRule = existingRule.copy(
                        merchantPattern = merchantPattern.trim(),
                        categoryId = categoryId,
                        projectId = projectId,
                        priority = priority
                    )
                    merchantRuleRepository.update(updatedRule)
                    _successMessage.value = "Rule updated successfully"
                } else {
                    _errorMessage.value = "Rule not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update rule: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a merchant rule
     */
    fun deleteRule(ruleId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val rule = merchantRuleRepository.getById(ruleId)
                if (rule != null) {
                    merchantRuleRepository.delete(rule)
                }
                _successMessage.value = "Rule deleted"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete rule: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete multiple merchant rules (bulk delete)
     */
    fun deleteRules(ruleIds: List<Long>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                ruleIds.forEach { ruleId ->
                    val rule = merchantRuleRepository.getById(ruleId)
                    if (rule != null) {
                        merchantRuleRepository.delete(rule)
                    }
                }
                _successMessage.value = "${ruleIds.size} rules deleted"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete rules: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggle rule active status
     */
    fun toggleRuleActive(ruleId: Long, isActive: Boolean) {
        viewModelScope.launch {
            try {
                merchantRuleRepository.toggleActive(ruleId, isActive)
                _successMessage.value = if (isActive) "Rule activated" else "Rule deactivated"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to toggle rule: ${e.message}"
            }
        }
    }

    /**
     * Update rule priority
     */
    fun updateRulePriority(ruleId: Long, priority: Int) {
        viewModelScope.launch {
            try {
                val existingRule = merchantRuleRepository.getById(ruleId)
                if (existingRule != null) {
                    val updatedRule = existingRule.copy(priority = priority)
                    merchantRuleRepository.update(updatedRule)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update priority: ${e.message}"
            }
        }
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Validate merchant pattern
     */
    fun validateMerchantPattern(pattern: String): String? {
        if (pattern.isBlank()) {
            return "Merchant pattern cannot be empty"
        }
        
        // Check for valid pipe-separated patterns
        val patterns = pattern.split("|")
        if (patterns.any { it.trim().isEmpty() }) {
            return "Invalid pattern: empty values not allowed"
        }
        
        // Check pattern length
        if (pattern.length > 200) {
            return "Pattern too long (max 200 characters)"
        }
        
        return null
    }

    /**
     * Get category name by ID
     */
    fun getCategoryName(categoryId: Long): String? {
        return _categories.value.find { it.id == categoryId }?.name
    }

    /**
     * Get project name by ID
     */
    fun getProjectName(projectId: Long?): String? {
        if (projectId == null) return null
        return _projects.value.find { it.id == projectId }?.name
    }
    
    /**
     * Import preset merchant rules
     * Creates categories if they don't exist and imports matching rules
     */
    fun importPresetRules() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Get all existing categories
                val existingCategories = categoryDao.getAllCategoriesList()
                val categoryMap = existingCategories.associate { it.name to it.id }
                
                // Get required categories from presets
                val requiredCategories = com.h4rsh41.paisatracker.data.MerchantRulePresets.getRequiredCategories()
                
                // Create missing categories
                val updatedCategoryMap = categoryMap.toMutableMap()
                val defaultProject = projectDao.getAllProjectsList().firstOrNull()
                
                if (defaultProject == null) {
                    _errorMessage.value = "Please create a project first before importing presets"
                    _isLoading.value = false
                    return@launch
                }
                
                for (categoryName in requiredCategories) {
                    if (!updatedCategoryMap.containsKey(categoryName)) {
                        // Create new category
                        val newCategory = com.h4rsh41.paisatracker.data.Category(
                            name = categoryName,
                            projectId = defaultProject.id,
                            emoji = getCategoryEmojiForName(categoryName)
                        )
                        val categoryId = categoryDao.insert(newCategory)
                        updatedCategoryMap[categoryName] = categoryId
                    }
                }
                
                // Convert presets to entities
                val presetEntities = com.h4rsh41.paisatracker.data.MerchantRulePresets.convertToEntities(
                    categoryMap = updatedCategoryMap,
                    defaultProjectId = null
                )
                
                // Import rules
                val importedCount = merchantRuleRepository.importPresets(presetEntities)
                
                _successMessage.value = "Imported $importedCount preset rules successfully"
                _categories.value = categoryDao.getAllCategoriesList() // Refresh categories
            } catch (e: Exception) {
                _errorMessage.value = "Failed to import presets: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get emoji for category name
     */
    fun getCategoryEmojiForName(categoryName: String): String {
        return when (categoryName.lowercase()) {
            "shopping" -> "🛍️"
            "food & dining", "food" -> "🍽️"
            "groceries" -> "🛒"
            "transportation", "transport" -> "🚗"
            "fuel" -> "⛽"
            "entertainment" -> "🎬"
            "utilities" -> "💡"
            "health" -> "🏥"
            "education" -> "📚"
            else -> "💰"
        }
    }
    
    /**
     * Get list of missing categories required for preset rules
     */
    fun getMissingCategories(): List<String> {
        val requiredCategories = com.h4rsh41.paisatracker.data.MerchantRulePresets.getRequiredCategories()
        val existingCategories = _categories.value.map { it.name.lowercase() }
        return requiredCategories.filter { required ->
            !existingCategories.contains(required.lowercase())
        }
    }
    
    /**
     * Create categories and import preset rules with guided setup
     */
    fun createCategoriesAndImport(
        projectId: Long?,
        projectName: String?,
        projectEmoji: String?,
        missingCategories: List<String>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Step 1: Create project if needed
                val finalProjectId = if (projectId == null && !projectName.isNullOrBlank()) {
                    val newProject = Project(
                        name = projectName,
                        emoji = projectEmoji ?: "📊"
                    )
                    projectDao.insertProject(newProject)
                } else {
                    projectId ?: projectDao.getAllProjectsList().firstOrNull()?.id
                }
                
                if (finalProjectId == null) {
                    _errorMessage.value = "No project available. Please create a project first."
                    _isLoading.value = false
                    return@launch
                }
                
                // Step 2: Create missing categories
                val createdCount = missingCategories.size
                missingCategories.forEach { categoryName ->
                    val emoji = getCategoryEmojiForName(categoryName)
                    val newCategory = Category(
                        name = categoryName,
                        emoji = emoji,
                        projectId = finalProjectId
                    )
                    categoryDao.insert(newCategory)
                }
                
                // Step 3: Reload categories and projects
                _categories.value = categoryDao.getAllCategoriesList()
                _projects.value = projectDao.getAllProjectsList()
                
                // Step 4: Import preset rules
                importPresetRules()
                
                _successMessage.value = "Created $createdCount categories and imported preset rules!"
            } catch (e: Exception) {
                _errorMessage.value = "Setup failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// Made with Bob
