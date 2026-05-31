package com.example.paisatracker.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

private val Context.merchantRulesDataStore by preferencesDataStore(name = "merchant_rules_prefs")

class MerchantRuleRepository(
    private val dao: MerchantRuleDao,
    private val context: Context
) {
    
    companion object {
        private val DEFAULT_RULES_SEEDED = booleanPreferencesKey("default_rules_seeded")
    }
    
    /**
     * Check if default rules have been seeded
     */
    private suspend fun areDefaultRulesSeeded(): Boolean {
        return context.merchantRulesDataStore.data
            .map { preferences -> preferences[DEFAULT_RULES_SEEDED] ?: false }
            .first()
    }
    
    /**
     * Mark default rules as seeded
     */
    private suspend fun markDefaultRulesSeeded() {
        context.merchantRulesDataStore.edit { preferences ->
            preferences[DEFAULT_RULES_SEEDED] = true
        }
    }
    
    /**
     * Seed default merchant rules on first app launch
     * This should be called when the app starts
     */
    suspend fun seedDefaultRulesIfNeeded(categoryDao: CategoryDao) {
        if (areDefaultRulesSeeded()) {
            return // Already seeded
        }
        
        val defaultPresets = MerchantRulePresets.getDefaultActiveRules()
        val categories = categoryDao.getAllCategoriesList()
        
        // Create a map of category names to IDs
        val categoryMap = categories.associateBy { it.name.lowercase() }
        
        // Convert presets to entities and insert
        val entitiesToInsert = mutableListOf<MerchantRuleEntity>()
        
        defaultPresets.forEach { preset ->
            val category = categoryMap[preset.categoryName.lowercase()]
            if (category != null) {
                entitiesToInsert.add(
                    MerchantRuleEntity(
                        id = 0,
                        merchantPattern = preset.merchantPattern,
                        categoryId = category.id,
                        projectId = null,
                        priority = preset.priority,
                        isActive = true, // Active by default
                        matchCount = 0,
                        lastMatchedAt = null,
                        createdAt = LocalDateTime.now()
                    )
                )
            }
        }
        
        // Insert all default rules
        entitiesToInsert.forEach { entity ->
            try {
                insert(entity)
            } catch (e: Exception) {
                // Skip if already exists or error
            }
        }
        
        // Mark as seeded
        markDefaultRulesSeeded()
    }
    
    fun getActiveRules(): Flow<List<MerchantRuleEntity>> = dao.getActiveRules()
    
    fun getAllRules(): Flow<List<MerchantRuleEntity>> = dao.getAllRules()
    
    suspend fun getActiveRulesList(): List<MerchantRuleEntity> = dao.getActiveRulesList()
    
    suspend fun getById(id: Long): MerchantRuleEntity? = dao.getById(id)
    
    suspend fun insert(rule: MerchantRuleEntity): Long = dao.insert(rule)
    
    suspend fun update(rule: MerchantRuleEntity) = dao.update(rule)
    
    suspend fun delete(rule: MerchantRuleEntity) = dao.delete(rule)
    
    suspend fun toggleActive(id: Long, isActive: Boolean) = dao.toggleActive(id, isActive)
    
    suspend fun incrementMatchCount(id: Long) {
        dao.incrementMatchCount(id, LocalDateTime.now())
    }
    
    suspend fun deleteAll() = dao.deleteAll()
    
    suspend fun getActiveRuleCount(): Int = dao.getActiveRuleCount()
    
    /**
     * Find the first matching merchant rule for the given merchant name
     * Rules are checked in priority order (highest first)
     */
    suspend fun findMatchingRule(merchant: String?): MerchantRuleEntity? {
        if (merchant.isNullOrBlank()) return null
        
        val activeRules = getActiveRulesList()
        val merchantLower = merchant.lowercase()
        
        for (rule in activeRules) {
            if (merchantMatches(merchantLower, rule.merchantPattern)) {
                return rule
            }
        }
        
        return null
    }
    
    /**
     * Check if merchant name matches the pattern
     * Supports multiple patterns separated by |
     */
    private fun merchantMatches(merchantLower: String, pattern: String): Boolean {
        val patterns = pattern.split("|")
        return patterns.any { p ->
            val patternLower = p.trim().lowercase()
            merchantLower.contains(patternLower)
        }
    }
    
    /**
     * Import preset rules (bulk insert)
     */
    suspend fun importPresets(presets: List<MerchantRuleEntity>): Int {
        var count = 0
        presets.forEach { preset ->
            try {
                insert(preset)
                count++
            } catch (e: Exception) {
                // Skip duplicates or errors
            }
        }
        return count
    }
}

// Made with Bob
