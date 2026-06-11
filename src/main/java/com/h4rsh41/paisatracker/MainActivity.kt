package com.h4rsh41.paisatracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.h4rsh41.paisatracker.analytics.AnalyticsManager
import com.h4rsh41.paisatracker.data.AppLockPreferences
import com.h4rsh41.paisatracker.data.AppTheme
import com.h4rsh41.paisatracker.data.DataSeeder
import com.h4rsh41.paisatracker.data.EmojiPreferencesRepository
import com.h4rsh41.paisatracker.data.ThemePreferencesRepository
import com.h4rsh41.paisatracker.ui.applock.AppLockScreen
import com.h4rsh41.paisatracker.ui.main.MainApp
import com.h4rsh41.paisatracker.ui.setup.FirstTimeSetupSheet
import com.h4rsh41.paisatracker.ui.tour.AppTourSheet
import com.h4rsh41.paisatracker.ui.theme.PaisaTrackerTheme
import com.h4rsh41.paisatracker.util.CurrentCurrency
import com.h4rsh41.paisatracker.util.UpdateManager
import com.h4rsh41.paisatracker.receiver.SmsBroadcastReceiver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private val updateManager by lazy { UpdateManager(this) }
    private val analyticsManager by lazy { AnalyticsManager.getInstance(this) }

    private val viewModel: PaisaTrackerViewModel by viewModels {
        PaisaTrackerViewModelFactory(
            (application as PaisaTrackerApplication).repository,
            (application as PaisaTrackerApplication).currencyPreferencesRepository,
            EmojiPreferencesRepository.getInstance(this),
            updateManager
        )
    }

    private val appLockPrefs by lazy { AppLockPreferences.getInstance(this) }
    private val themePreferencesRepository by lazy { ThemePreferencesRepository.getInstance(this) }

    private val dataSeeder by lazy { DataSeeder.getInstance((application as PaisaTrackerApplication).repository) }
    private var isUnlocked by mutableStateOf(false)

    private var showAppTour by mutableStateOf(false)
    private var showFirstTimeSetup by mutableStateOf(false)
    private var isFinishing = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestBatteryOptimizationExemption()
        }
    }

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val receiveSmsGranted = permissions[Manifest.permission.RECEIVE_SMS] ?: false
        val readSmsGranted = permissions[Manifest.permission.READ_SMS] ?: false
        
        if (receiveSmsGranted && readSmsGranted) {
            Log.d("MainActivity", "SMS permissions granted")
        } else {
            Log.d("MainActivity", "SMS permissions denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Log app opened event
        analyticsManager.logAppOpened()

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                val isAppLockEnabled = appLockPrefs.isAppLockEnabled.first()
                isUnlocked = !isAppLockEnabled

                // Check if first time setup should be shown
                val needsSetup = dataSeeder.shouldShowFirstTimeSetup(this@MainActivity)
                if (needsSetup) {
                    // Seed default project BEFORE showing tour
                    dataSeeder.seedDefaultDailyExpensesProject(this@MainActivity)
                    showAppTour = true
                }
                
                // Update analytics user properties
                updateAnalyticsProperties()
            }
        }

        // Handle SMS notification intents
        handleIntent(intent)

        requestNotificationPermission()
        requestSmsPermissions()
        viewModel.checkForUpdates(isManual = false)

        setContent {
            val currentTheme by themePreferencesRepository.appTheme.collectAsStateWithLifecycle(initialValue = AppTheme.SYSTEM_DEFAULT)

            // Observe currency changes
            val currentCurrency by viewModel.currentCurrency.collectAsStateWithLifecycle()

            // Update CurrentCurrency singleton whenever currency changes
            LaunchedEffect(currentCurrency) {
                CurrentCurrency.set(currentCurrency)
            }

            PaisaTrackerTheme(appTheme = currentTheme) {
                // Main App Content
                AppContent()

                // Onboarding Sequence: Tour -> First Time Setup
                if (showAppTour && !isFinishing) {
                    AppTourSheet(
                        onComplete = {
                            showAppTour = false
                            showFirstTimeSetup = true
                            // Mark tour as shown so it doesn't appear again
                            dataSeeder.markTourAsShown(this@MainActivity)
                        },
                        onBankAccountAdded = { name, bankName, last4, type, balance ->
                            // Create bank account during tour
                            lifecycleScope.launch {
                                try {
                                    val account = com.h4rsh41.paisatracker.data.BankAccount(
                                        name = name,
                                        accountType = type,
                                        bankName = bankName,
                                        accountNumberLast4 = last4,
                                        initialBalance = balance,
                                        currentBalance = balance,
                                        emoji = when(type) {
                                            com.h4rsh41.paisatracker.data.AccountType.CREDIT_CARD -> "💳"
                                            else -> "🏦"
                                        },
                                        colorHex = "#2196F3",
                                        isActive = true
                                    )
                                    (application as PaisaTrackerApplication).repository.insertBankAccount(account)
                                    Log.d("MainActivity", "Bank account created during tour: $name")
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Failed to create bank account: ${e.message}", e)
                                }
                            }
                        }
                    )
                }

                if (showFirstTimeSetup && !isFinishing) {
                    FirstTimeSetupSheet(
                        viewModel = viewModel,
                        onSetupComplete = { shouldKeepDefault ->
                            lifecycleScope.launch {
                                if (shouldKeepDefault) {
                                    // User chose to keep default project - just mark as complete
                                    dataSeeder.markSetupComplete(this@MainActivity)
                                } else {
                                    // User chose "Start Fresh" - remove default project
                                    dataSeeder.removeDefaultProject(this@MainActivity)
                                    dataSeeder.markSetupComplete(this@MainActivity)
                                }
                                showFirstTimeSetup = false
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            SmsBroadcastReceiver.ACTION_VIEW_EXPENSE -> {
                val expenseId = intent.getLongExtra(SmsBroadcastReceiver.EXTRA_EXPENSE_ID, -1L)
                if (expenseId != -1L) {
                    Log.d("MainActivity", "Opening expense from SMS notification: $expenseId")
                    // TODO: Navigate to expense detail screen
                    // This will be implemented when UI screens are created
                }
            }
            SmsBroadcastReceiver.ACTION_VIEW_PENDING -> {
                val notificationId = intent.getLongExtra(SmsBroadcastReceiver.EXTRA_NOTIFICATION_ID, -1L)
                Log.d("MainActivity", "Opening pending SMS transactions from notification: $notificationId")
                // The navigation will happen automatically when the app opens
                // User can navigate to Settings -> SMS Transactions -> View Pending
            }
        }
    }

    private fun requestSmsPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECEIVE_SMS)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_SMS)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            smsPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("was_unlocked", isUnlocked)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isUnlocked = savedInstanceState.getBoolean("was_unlocked", false)
    }

    override fun onPause() {
        super.onPause()
        isFinishing = isFinishing()
    }

    @Composable
    private fun AppContent() {
        val isAppLockEnabled by appLockPrefs.isAppLockEnabled.collectAsStateWithLifecycle(initialValue = false)
        val isBiometricEnabled by appLockPrefs.isBiometricEnabled.collectAsStateWithLifecycle(initialValue = false)
        val pinCode by appLockPrefs.pinCode.collectAsStateWithLifecycle(initialValue = null)

        when {
            isAppLockEnabled && !isUnlocked && pinCode != null -> {
                AppLockScreen(
                    onUnlock = { isUnlocked = true },
                    correctPin = pinCode ?: "",
                    biometricEnabled = isBiometricEnabled
                )
            }
            else -> {
                MainApp(viewModel = viewModel)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    requestBatteryOptimizationExemption()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            requestBatteryOptimizationExemption()
        }
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:$packageName")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                // Silently fail
            }
        }
    }
    
    /**
     * Update analytics with aggregated user properties
     * This is called periodically to keep analytics data current
     */
    private fun updateAnalyticsProperties() {
        lifecycleScope.launch {
            try {
                val repository = (application as PaisaTrackerApplication).repository
                
                // Get aggregated counts (no personal data)
                val totalProjects = repository.getAllProjects().first().size
                val totalCategories = repository.getAllCategories().first().size
                val totalExpenses = repository.getAllExpenses().first().size
                val hasAppLock = appLockPrefs.isAppLockEnabled.first()
                
                // Widget count would need to be tracked separately
                // For now, we'll use 0 as placeholder
                val widgetsCount = 0
                
                analyticsManager.updateUserProperties(
                    totalProjects = totalProjects,
                    totalCategories = totalCategories,
                    totalExpenses = totalExpenses,
                    hasAppLock = hasAppLock,
                    widgetsCount = widgetsCount
                )
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to update analytics properties", e)
            }
        }
    }
}