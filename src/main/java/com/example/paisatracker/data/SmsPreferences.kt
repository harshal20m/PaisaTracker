package com.example.paisatracker.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages SMS transaction detection preferences
 */
class SmsPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "sms_preferences",
        Context.MODE_PRIVATE
    )

    private val _autoCreateExpenses = MutableStateFlow(getAutoCreateExpenses())
    val autoCreateExpenses: Flow<Boolean> = _autoCreateExpenses.asStateFlow()
    val autoCreateExpensesFlow: Flow<Boolean> = autoCreateExpenses

    private val _showNotifications = MutableStateFlow(getShowNotifications())
    val showNotifications: Flow<Boolean> = _showNotifications.asStateFlow()
    val showNotificationsFlow: Flow<Boolean> = showNotifications

    private val _vibrateOnDetection = MutableStateFlow(getVibrateOnDetection())
    val vibrateOnDetection: Flow<Boolean> = _vibrateOnDetection.asStateFlow()
    val vibrateOnDetectionFlow: Flow<Boolean> = vibrateOnDetection

    private val _trashRetentionDays = MutableStateFlow(getTrashRetentionDays())
    val trashRetentionDays: Flow<Int> = _trashRetentionDays.asStateFlow()
    val trashRetentionDaysFlow: Flow<Int> = trashRetentionDays

    companion object {
        private const val KEY_AUTO_CREATE_EXPENSES = "auto_create_expenses"
        private const val KEY_SHOW_NOTIFICATIONS = "show_sms_notifications"
        private const val KEY_VIBRATE_ON_DETECTION = "vibrate_on_detection"
        private const val KEY_TRASH_RETENTION_DAYS = "trash_retention_days"
        private const val KEY_DEFAULT_CATEGORY_ID = "auto_create_default_category"
        private const val KEY_DEFAULT_PROJECT_ID = "auto_create_default_project"
        private const val KEY_USE_MERCHANT_RULES = "use_merchant_rules"
        private const val DEFAULT_TRASH_RETENTION_DAYS = 30
    }

    /**
     * Get whether to automatically create expenses from SMS
     * Default: false (manual confirmation required)
     */
    fun getAutoCreateExpenses(): Boolean {
        return prefs.getBoolean(KEY_AUTO_CREATE_EXPENSES, false)
    }

    /**
     * Set whether to automatically create expenses from SMS
     */
    fun setAutoCreateExpenses(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CREATE_EXPENSES, enabled).apply()
        _autoCreateExpenses.value = enabled
    }

    /**
     * Get whether to show notifications for detected SMS transactions
     * Default: true
     */
    fun getShowNotifications(): Boolean {
        return prefs.getBoolean(KEY_SHOW_NOTIFICATIONS, true)
    }

    /**
     * Set whether to show notifications for detected SMS transactions
     */
    fun setShowNotifications(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_NOTIFICATIONS, enabled).apply()
        _showNotifications.value = enabled
    }

    /**
     * Get whether to vibrate when SMS transaction is detected
     * Default: false
     */
    fun getVibrateOnDetection(): Boolean {
        return prefs.getBoolean(KEY_VIBRATE_ON_DETECTION, false)
    }

    /**
     * Set whether to vibrate when SMS transaction is detected
     */
    fun setVibrateOnDetection(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATE_ON_DETECTION, enabled).apply()
        _vibrateOnDetection.value = enabled
    }

    /**
     * Get trash retention period in days
     * Default: 30 days
     */
    fun getTrashRetentionDays(): Int {
        return prefs.getInt(KEY_TRASH_RETENTION_DAYS, DEFAULT_TRASH_RETENTION_DAYS)
    }

    /**
     * Set trash retention period in days
     * Valid values: 10, 30, 90, or -1 for never
     */
    fun setTrashRetentionDays(days: Int) {
        prefs.edit().putInt(KEY_TRASH_RETENTION_DAYS, days).apply()
        _trashRetentionDays.value = days
    }

    /**
     * Get default category ID for auto-created expenses
     * Returns null if not set
     */
    fun getDefaultCategoryId(): Long? {
        val id = prefs.getLong(KEY_DEFAULT_CATEGORY_ID, -1L)
        return if (id == -1L) null else id
    }

    /**
     * Set default category ID for auto-created expenses
     * Pass null to clear
     */
    fun setDefaultCategoryId(categoryId: Long?) {
        if (categoryId == null) {
            prefs.edit().remove(KEY_DEFAULT_CATEGORY_ID).apply()
        } else {
            prefs.edit().putLong(KEY_DEFAULT_CATEGORY_ID, categoryId).apply()
        }
    }

    /**
     * Get default project ID for auto-created expenses
     * Returns null if not set
     */
    fun getDefaultProjectId(): Long? {
        val id = prefs.getLong(KEY_DEFAULT_PROJECT_ID, -1L)
        return if (id == -1L) null else id
    }

    /**
     * Set default project ID for auto-created expenses
     * Pass null to clear
     */
    fun setDefaultProjectId(projectId: Long?) {
        if (projectId == null) {
            prefs.edit().remove(KEY_DEFAULT_PROJECT_ID).apply()
        } else {
            prefs.edit().putLong(KEY_DEFAULT_PROJECT_ID, projectId).apply()
        }
    }

    /**
     * Get whether to use merchant-specific rules
     * Default: true
     */
    fun getUseMerchantRules(): Boolean {
        return prefs.getBoolean(KEY_USE_MERCHANT_RULES, true)
    }

    /**
     * Set whether to use merchant-specific rules
     */
    fun setUseMerchantRules(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_MERCHANT_RULES, enabled).apply()
    }
}

// Made with Bob