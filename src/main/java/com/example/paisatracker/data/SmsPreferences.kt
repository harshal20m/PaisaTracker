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

    companion object {
        private const val KEY_AUTO_CREATE_EXPENSES = "auto_create_expenses"
        private const val KEY_SHOW_NOTIFICATIONS = "show_sms_notifications"
        private const val KEY_VIBRATE_ON_DETECTION = "vibrate_on_detection"
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
}

// Made with Bob