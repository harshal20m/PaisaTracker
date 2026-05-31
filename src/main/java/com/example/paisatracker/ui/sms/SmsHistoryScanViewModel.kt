package com.example.paisatracker.ui.sms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.paisatracker.data.BankNotificationRepository
import com.example.paisatracker.data.PaisaTrackerDatabase
import com.example.paisatracker.data.ScanDateRange
import com.example.paisatracker.data.ScanSummary
import com.example.paisatracker.data.SmsHistoryScanConfig
import com.example.paisatracker.data.SmsHistoryScanState
import com.example.paisatracker.data.SmsPreferences
import com.example.paisatracker.data.UnrecognizedSmsRepository
import com.example.paisatracker.manager.SmsHistoryScanner
import com.example.paisatracker.manager.SmsTransactionProcessor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel for SMS History Scan feature
 * Manages scanning state, date range selection, and scan execution
 */
class SmsHistoryScanViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = PaisaTrackerDatabase.getDatabase(application)
    private val smsPreferences = SmsPreferences(application)
    
    // Initialize scanner
    private val smsTransactionProcessor = SmsTransactionProcessor(
        context = application,
        expenseDao = database.expenseDao(),
        categoryDao = database.categoryDao(),
        bankNotificationRepository = BankNotificationRepository(database.bankNotificationDao()),
        unrecognizedSmsRepository = UnrecognizedSmsRepository(database.unrecognizedSmsDao()),
        smsPreferences = smsPreferences,
        merchantRuleRepository = com.example.paisatracker.data.MerchantRuleRepository(
            database.merchantRuleDao(),
            application
        )
    )
    
    private val smsHistoryScanner = SmsHistoryScanner(
        context = application,
        smsTransactionProcessor = smsTransactionProcessor
    )
    
    // State flows
    private val _scanState = MutableStateFlow<SmsHistoryScanState>(SmsHistoryScanState.Idle)
    val scanState: StateFlow<SmsHistoryScanState> = _scanState.asStateFlow()
    
    private val _selectedDateRange = MutableStateFlow(ScanDateRange.LAST_30_DAYS)
    val selectedDateRange: StateFlow<ScanDateRange> = _selectedDateRange.asStateFlow()
    
    private val _customStartDate = MutableStateFlow(LocalDate.now().minusDays(30))
    val customStartDate: StateFlow<LocalDate> = _customStartDate.asStateFlow()
    
    private val _customEndDate = MutableStateFlow(LocalDate.now())
    val customEndDate: StateFlow<LocalDate> = _customEndDate.asStateFlow()
    
    private val _scanSummary = MutableStateFlow<ScanSummary?>(null)
    val scanSummary: StateFlow<ScanSummary?> = _scanSummary.asStateFlow()
    
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()
    
    private val _lastScanDate = MutableStateFlow<LocalDate?>(getLastScanDate())
    val lastScanDate: StateFlow<LocalDate?> = _lastScanDate.asStateFlow()
    
    private var scanJob: Job? = null
    
    /**
     * Update selected date range
     */
    fun setDateRange(range: ScanDateRange) {
        _selectedDateRange.value = range
        if (range != ScanDateRange.CUSTOM) {
            _customStartDate.value = range.getStartDate()
            _customEndDate.value = LocalDate.now()
        }
    }
    
    /**
     * Update custom start date
     */
    fun setCustomStartDate(date: LocalDate) {
        _customStartDate.value = date
        _selectedDateRange.value = ScanDateRange.CUSTOM
    }
    
    /**
     * Update custom end date
     */
    fun setCustomEndDate(date: LocalDate) {
        _customEndDate.value = date
        _selectedDateRange.value = ScanDateRange.CUSTOM
    }
    
    /**
     * Update permission status
     */
    fun setPermissionGranted(granted: Boolean) {
        _hasPermission.value = granted
    }
    
    /**
     * Start scanning SMS history
     */
    fun startScan() {
        // Cancel any existing scan
        scanJob?.cancel()
        
        val config = SmsHistoryScanConfig(
            startDate = _customStartDate.value,
            endDate = _customEndDate.value,
            autoCreateExpenses = smsPreferences.getAutoCreateExpenses(),
            batchSize = 50,
            progressUpdateInterval = 10
        )
        
        scanJob = viewModelScope.launch {
            smsHistoryScanner.scanHistory(config).collect { state ->
                _scanState.value = state
                
                // Generate summary when completed
                if (state is SmsHistoryScanState.Completed) {
                    _scanSummary.value = smsHistoryScanner.generateSummary(state, config)
                    saveLastScanDate()
                }
            }
        }
    }
    
    /**
     * Cancel ongoing scan
     */
    fun cancelScan() {
        scanJob?.cancel()
        _scanState.value = SmsHistoryScanState.Cancelled
    }
    
    /**
     * Reset scan state to idle
     */
    fun resetScan() {
        _scanState.value = SmsHistoryScanState.Idle
        _scanSummary.value = null
    }
    
    /**
     * Check if user should be prompted to scan
     * Returns true if never scanned or last scan was more than 30 days ago
     */
    fun shouldPromptScan(): Boolean {
        val lastScan = _lastScanDate.value ?: return true
        val daysSinceLastScan = java.time.temporal.ChronoUnit.DAYS.between(lastScan, LocalDate.now())
        return daysSinceLastScan > 30
    }
    
    /**
     * Get estimated message count for selected date range
     * This is a rough estimate based on typical SMS volume
     */
    fun getEstimatedMessageCount(): Int {
        val days = when (_selectedDateRange.value) {
            ScanDateRange.CUSTOM -> {
                java.time.temporal.ChronoUnit.DAYS.between(_customStartDate.value, _customEndDate.value).toInt()
            }
            else -> _selectedDateRange.value.days
        }
        
        // Estimate: ~2-5 financial SMS per day on average
        return (days * 3).coerceAtMost(1000) // Cap at 1000 for display
    }
    
    /**
     * Save last scan date to preferences
     */
    private fun saveLastScanDate() {
        val prefs = getApplication<Application>().getSharedPreferences("sms_scan_prefs", 0)
        prefs.edit().putLong("last_scan_date", System.currentTimeMillis()).apply()
        _lastScanDate.value = LocalDate.now()
    }
    
    /**
     * Get last scan date from preferences
     */
    private fun getLastScanDate(): LocalDate? {
        val prefs = getApplication<Application>().getSharedPreferences("sms_scan_prefs", 0)
        val timestamp = prefs.getLong("last_scan_date", -1L)
        return if (timestamp != -1L) {
            LocalDate.ofEpochDay(timestamp / (24 * 60 * 60 * 1000))
        } else {
            null
        }
    }
    
    /**
     * Get scan progress percentage
     */
    fun getScanProgress(): Float {
        return when (val state = _scanState.value) {
            is SmsHistoryScanState.Scanning -> {
                if (state.totalMessages > 0) {
                    state.processedMessages.toFloat() / state.totalMessages.toFloat()
                } else {
                    0f
                }
            }
            is SmsHistoryScanState.Completed -> 1f
            else -> 0f
        }
    }
    
    /**
     * Check if scan is in progress
     */
    fun isScanningInProgress(): Boolean {
        return _scanState.value is SmsHistoryScanState.Scanning
    }
    
    /**
     * Check if scan is completed
     */
    fun isScanCompleted(): Boolean {
        return _scanState.value is SmsHistoryScanState.Completed
    }
    
    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}

// Made with Bob