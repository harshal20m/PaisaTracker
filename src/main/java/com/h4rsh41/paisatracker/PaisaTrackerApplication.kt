package com.h4rsh41.paisatracker

import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.h4rsh41.paisatracker.analytics.AnalyticsManager
import com.h4rsh41.paisatracker.data.CurrencyPreferencesRepository
import com.h4rsh41.paisatracker.data.MerchantRuleRepository
import com.h4rsh41.paisatracker.data.PaisaTrackerDatabase
import com.h4rsh41.paisatracker.data.PaisaTrackerRepository
import com.h4rsh41.paisatracker.data.ThemePreferencesRepository
import com.h4rsh41.paisatracker.util.CurrentCurrency
import com.h4rsh41.paisatracker.util.ExpenseReminderWorker
import com.h4rsh41.paisatracker.util.UpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PaisaTrackerApplication : Application() {
    
    // Analytics Manager (Lazy initialization)
    val analyticsManager: AnalyticsManager by lazy {
        AnalyticsManager.getInstance(this)
    }
    val database: PaisaTrackerDatabase by lazy { PaisaTrackerDatabase.getDatabase(this) }
    val repository: PaisaTrackerRepository by lazy {
        PaisaTrackerRepository(
            database.projectDao(),
            database.categoryDao(),
            database.expenseDao(),
            database.assetDao(),
            database.backupDao(),
            database.budgetDao(),
            database.flapDao(),
            database.salaryRecordDao(),
            database.actionHistoryDao(),
            database.bankAccountDao(),
            database.bankNotificationDao(),
            database.accountTransactionDao()
        )
    }

    val themePreferencesRepository: ThemePreferencesRepository by lazy {
        ThemePreferencesRepository.getInstance(applicationContext)
    }

    val currencyPreferencesRepository: CurrencyPreferencesRepository by lazy {
        CurrencyPreferencesRepository(applicationContext)
    }

    val updateManager: UpdateManager by lazy {
        UpdateManager(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Analytics Manager
        analyticsManager.logAppOpened()
        
        scheduleDailyReminder()

        // Initialize currency from preferences
        CoroutineScope(Dispatchers.IO).launch {
            currencyPreferencesRepository.selectedCurrency.collect { currency ->
                CurrentCurrency.set(currency)
            }
        }
        
        // Seed default merchant rules on first app launch
        seedDefaultMerchantRules()
        
        // Update user properties for analytics
        updateAnalyticsUserProperties()
        
        // Check for version updates (shows star repo card if updated)
        checkForVersionUpdate()
    }
    
    private fun checkForVersionUpdate() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateManager.checkForVersionUpdate()
            } catch (e: Exception) {
                // Silently fail - not critical for app functionality
                e.printStackTrace()
            }
        }
    }
    
    private fun updateAnalyticsUserProperties() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val projects = repository.getAllProjects().firstOrNull() ?: emptyList()
                val categories = repository.getAllCategories().firstOrNull() ?: emptyList()
                val expenses = repository.getAllExpenses().firstOrNull() ?: emptyList()
                
                val sharedPrefs = getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)
                val hasAppLock = sharedPrefs.getBoolean("app_lock_enabled", false)
                
                // Widget count would need to be tracked separately
                // For now, we'll use 0 as a placeholder
                analyticsManager.updateUserProperties(
                    totalProjects = projects.size,
                    totalCategories = categories.size,
                    totalExpenses = expenses.size,
                    hasAppLock = hasAppLock,
                    widgetsCount = 0
                )
            } catch (e: Exception) {
                // Silently fail - analytics should never crash the app
                e.printStackTrace()
            }
        }
    }
    
    private fun seedDefaultMerchantRules() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val merchantRuleRepository = MerchantRuleRepository(
                    database.merchantRuleDao(),
                    applicationContext
                )
                merchantRuleRepository.seedDefaultRulesIfNeeded(database.categoryDao())
            } catch (e: Exception) {
                // Silently fail - not critical for app functionality
                e.printStackTrace()
            }
        }
    }

    private fun scheduleDailyReminder() {
        val sharedPrefs = getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val isEnabled = sharedPrefs.getBoolean("notification_enabled", true)
        val notificationHour = sharedPrefs.getInt("notification_hour", 20)

        val workManager = WorkManager.getInstance(applicationContext)

        if (!isEnabled) {
            workManager.cancelUniqueWork("daily_expense_reminder")
            return
        }

        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, notificationHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (currentTime.after(targetTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val reminderRequest = PeriodicWorkRequestBuilder<ExpenseReminderWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .addTag("expense_reminder")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_expense_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderRequest
        )
    }
}