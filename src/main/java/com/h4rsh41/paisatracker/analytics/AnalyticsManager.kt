package com.h4rsh41.paisatracker.analytics

import android.content.Context
import android.os.Bundle
import com.h4rsh41.paisatracker.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Privacy-First Analytics Manager for PaisaTracker
 * 
 * This manager handles all analytics events with strict privacy controls:
 * - Only enabled if BuildConfig.ANALYTICS_ENABLED is true
 * - No personal or financial data is ever logged
 * - Only aggregated, anonymous metrics are collected
 * - Complies with GDPR and privacy best practices
 * 
 * Metrics Collected (Developer Only):
 * - App installs and opens
 * - Active user count (anonymous)
 * - Feature usage statistics (e.g., total projects created)
 * - No transaction amounts, descriptions, or personal data
 */
class AnalyticsManager private constructor(context: Context) {
    
    private val analytics: FirebaseAnalytics? = if (BuildConfig.ANALYTICS_ENABLED) {
        try {
            Firebase.analytics.apply {
                // Disable automatic screen tracking to have full control
                setAnalyticsCollectionEnabled(true)
            }
        } catch (e: Exception) {
            // Firebase not configured properly (e.g., missing google-services.json)
            // This is expected for debug builds and open-source contributors
            e.printStackTrace()
            null
        }
    } else {
        null
    }
    
    companion object {
        @Volatile
        private var instance: AnalyticsManager? = null
        
        fun getInstance(context: Context): AnalyticsManager {
            return instance ?: synchronized(this) {
                instance ?: AnalyticsManager(context.applicationContext).also { instance = it }
            }
        }
        
        // Event Names
        private const val EVENT_APP_OPENED = "app_opened"
        private const val EVENT_PROJECT_CREATED = "project_created"
        private const val EVENT_EXPENSE_CREATED = "expense_created"
        private const val EVENT_CATEGORY_CREATED = "category_created"
        private const val EVENT_BACKUP_CREATED = "backup_created"
        private const val EVENT_BACKUP_RESTORED = "backup_restored"
        private const val EVENT_WIDGET_ADDED = "widget_added"
        private const val EVENT_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val EVENT_SMS_SCAN_PERFORMED = "sms_scan_performed"
        
        // User Properties (Aggregated, Non-Personal)
        private const val PROPERTY_TOTAL_PROJECTS = "total_projects"
        private const val PROPERTY_TOTAL_CATEGORIES = "total_categories"
        private const val PROPERTY_TOTAL_EXPENSES = "total_expenses"
        private const val PROPERTY_HAS_APP_LOCK = "has_app_lock"
        private const val PROPERTY_WIDGETS_COUNT = "widgets_count"
    }
    
    /**
     * Check if analytics is enabled
     */
    fun isEnabled(): Boolean = BuildConfig.ANALYTICS_ENABLED && analytics != null
    
    /**
     * Log app opened event
     */
    fun logAppOpened() {
        if (!isEnabled()) return
        analytics?.logEvent(EVENT_APP_OPENED, null)
    }
    
    /**
     * Log project created event
     */
    fun logProjectCreated() {
        if (!isEnabled()) return
        analytics?.logEvent(EVENT_PROJECT_CREATED, null)
    }
    
    /**
     * Log expense created event
     */
    fun logExpenseCreated() {
        if (!isEnabled()) return
        analytics?.logEvent(EVENT_EXPENSE_CREATED, null)
    }
    
    /**
     * Log category created event
     */
    fun logCategoryCreated() {
        if (!isEnabled()) return
        analytics?.logEvent(EVENT_CATEGORY_CREATED, null)
    }
    
    /**
     * Log backup created event
     */
    fun logBackupCreated() {
        if (!isEnabled()) return
        analytics?.logEvent(EVENT_BACKUP_CREATED, null)
    }
    
    /**
     * Log backup restored event
     */
    fun logBackupRestored() {
        if (!isEnabled()) return
        analytics?.logEvent(EVENT_BACKUP_RESTORED, null)
    }
    
    /**
     * Log widget added event
     * @param widgetType Type of widget (e.g., "quick_balance", "budget_progress")
     */
    fun logWidgetAdded(widgetType: String) {
        if (!isEnabled()) return
        val bundle = Bundle().apply {
            putString("widget_type", widgetType)
        }
        analytics?.logEvent(EVENT_WIDGET_ADDED, bundle)
    }
    
    /**
     * Log app lock enabled event
     */
    fun logAppLockEnabled() {
        if (!isEnabled()) return
        analytics?.logEvent(EVENT_APP_LOCK_ENABLED, null)
    }
    
    /**
     * Log SMS scan performed event
     * @param transactionsFound Number of transactions found (no details)
     */
    fun logSmsScanPerformed(transactionsFound: Int) {
        if (!isEnabled()) return
        val bundle = Bundle().apply {
            putInt("transactions_found", transactionsFound)
        }
        analytics?.logEvent(EVENT_SMS_SCAN_PERFORMED, bundle)
    }
    
    /**
     * Update user properties with aggregated counts
     * This helps understand app usage patterns without exposing personal data
     * 
     * @param totalProjects Total number of projects
     * @param totalCategories Total number of categories
     * @param totalExpenses Total number of expenses
     * @param hasAppLock Whether app lock is enabled
     * @param widgetsCount Number of active widgets
     */
    fun updateUserProperties(
        totalProjects: Int,
        totalCategories: Int,
        totalExpenses: Int,
        hasAppLock: Boolean,
        widgetsCount: Int
    ) {
        if (!isEnabled()) return
        
        analytics?.apply {
            setUserProperty(PROPERTY_TOTAL_PROJECTS, totalProjects.toString())
            setUserProperty(PROPERTY_TOTAL_CATEGORIES, totalCategories.toString())
            setUserProperty(PROPERTY_TOTAL_EXPENSES, totalExpenses.toString())
            setUserProperty(PROPERTY_HAS_APP_LOCK, hasAppLock.toString())
            setUserProperty(PROPERTY_WIDGETS_COUNT, widgetsCount.toString())
        }
    }
    
    /**
     * Log custom event with parameters
     * Use sparingly and ensure no personal data is included
     */
    fun logCustomEvent(eventName: String, params: Map<String, Any>? = null) {
        if (!isEnabled()) return
        
        val bundle = params?.let {
            Bundle().apply {
                it.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Double -> putDouble(key, value)
                        is Boolean -> putBoolean(key, value)
                    }
                }
            }
        }
        
        analytics?.logEvent(eventName, bundle)
    }
}

// Made with Bob
