package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ScanRecordEntity
import com.example.model.*
import com.example.util.DeviceScanner
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CleanerTab(val label: String) {
    DASHBOARD("Home"),
    STORAGE("Storage"),
    THERMAL_BATTERY("Battery & Heat"),
    LAG_GAMING("Lag & Gaming"),
    SMART_REPAIR("Smart Repair"),
    APP_MANAGER("Apps"),
    HISTORY_ABOUT("History & Info")
}

data class CleanerUiState(
    val currentTab: CleanerTab = CleanerTab.DASHBOARD,
    val storageInfo: StorageInfo = StorageInfo(),
    val memoryStatus: MemoryStatus = MemoryStatus(),
    val batteryStatus: BatteryStatus = BatteryStatus(),
    val healthScore: DeviceHealthScore = DeviceHealthScore(),
    val scanState: ScanState = ScanState.IDLE,
    val scanProgress: Float = 0f,
    val scanStepText: String = "",
    val junkItems: List<JunkItem> = emptyList(),
    val storageBreakdown: StorageCategoryBreakdown = StorageCategoryBreakdown(),
    val installedApps: List<AppInfoItem> = emptyList(),
    val appSearchQuery: String = "",
    val showSystemApps: Boolean = false,
    val scanHistory: List<ScanRecordEntity> = emptyList(),
    val showCleanConfirmationDialog: Boolean = false,
    val lastCleanedBytes: Long = 0L,
    val lastCleanedCount: Int = 0,
    val hasStoragePermission: Boolean = false
) {
    val selectedJunkBytes: Long
        get() = junkItems.filter { it.isChecked }.sumOf { it.sizeBytes }

    val totalDetectedJunkBytes: Long
        get() = junkItems.sumOf { it.sizeBytes }

    val filteredApps: List<AppInfoItem>
        get() = installedApps.filter { app ->
            (showSystemApps || !app.isSystemApp) &&
                    (appSearchQuery.isBlank() || app.appName.contains(appSearchQuery, ignoreCase = true) ||
                            app.packageName.contains(appSearchQuery, ignoreCase = true))
        }
}

class CleanerViewModel(application: Application) : AndroidViewModel(application) {

    private val scanner = DeviceScanner(application)
    private val database = AppDatabase.getDatabase(application)
    private val historyDao = database.scanHistoryDao()

    private val _uiState = MutableStateFlow(CleanerUiState())
    val uiState: StateFlow<CleanerUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        refreshHardwareStats()
        observeHistory()
        loadInstalledApps()
        loadStorageBreakdown()
    }

    fun setTab(tab: CleanerTab) {
        _uiState.update { it.copy(currentTab = tab) }
        if (tab == CleanerTab.STORAGE) {
            loadStorageBreakdown()
        } else if (tab == CleanerTab.APP_MANAGER && _uiState.value.installedApps.isEmpty()) {
            loadInstalledApps()
        }
    }

    fun updatePermissionStatus(granted: Boolean) {
        _uiState.update { it.copy(hasStoragePermission = granted) }
        if (granted) {
            loadStorageBreakdown()
        }
    }

    fun refreshHardwareStats() {
        val storage = scanner.getStorageInfo()
        val memory = scanner.getMemoryStatus()
        val battery = scanner.getBatteryStatus()
        val junkBytes = _uiState.value.totalDetectedJunkBytes
        val health = scanner.calculateHealthScore(storage, memory, battery, junkBytes)

        _uiState.update {
            it.copy(
                storageInfo = storage,
                memoryStatus = memory,
                batteryStatus = battery,
                healthScore = health
            )
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            historyDao.getAllRecords().collect { records ->
                _uiState.update { it.copy(scanHistory = records) }
            }
        }
    }

    fun startScan() {
        if (_uiState.value.scanState == ScanState.SCANNING) return

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    scanState = ScanState.SCANNING,
                    scanProgress = 0f,
                    scanStepText = "Initializing smart scan engine..."
                )
            }

            val items = scanner.scanJunkItems { progress, step ->
                _uiState.update {
                    it.copy(
                        scanProgress = progress / 100f,
                        scanStepText = step
                    )
                }
            }

            delay(300) // Brief animation settle

            val storage = scanner.getStorageInfo()
            val memory = scanner.getMemoryStatus()
            val battery = scanner.getBatteryStatus()
            val totalJunk = items.sumOf { it.sizeBytes }
            val health = scanner.calculateHealthScore(storage, memory, battery, totalJunk)

            _uiState.update {
                it.copy(
                    scanState = ScanState.SCANNED,
                    scanProgress = 1f,
                    scanStepText = "Scan Complete",
                    junkItems = items,
                    healthScore = health
                )
            }
        }
    }

    fun toggleJunkItem(itemId: String) {
        _uiState.update { state ->
            val updated = state.junkItems.map { item ->
                if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
            }
            state.copy(junkItems = updated)
        }
    }

    fun toggleCategory(category: JunkCategory, selectAll: Boolean) {
        _uiState.update { state ->
            val updated = state.junkItems.map { item ->
                if (item.category == category) {
                    // Only check dangerous/large items if user explicitly toggled it
                    if (item.isSafeToDelete || selectAll) {
                        item.copy(isChecked = selectAll)
                    } else {
                        item.copy(isChecked = false)
                    }
                } else item
            }
            state.copy(junkItems = updated)
        }
    }

    fun selectAllSafeItems() {
        _uiState.update { state ->
            val updated = state.junkItems.map { item ->
                item.copy(isChecked = item.isSafeToDelete)
            }
            state.copy(junkItems = updated)
        }
    }

    fun requestCleanConfirmation() {
        if (_uiState.value.selectedJunkBytes > 0) {
            _uiState.update { it.copy(showCleanConfirmationDialog = true) }
        }
    }

    fun dismissCleanConfirmation() {
        _uiState.update { it.copy(showCleanConfirmationDialog = false) }
    }

    fun confirmAndClean() {
        _uiState.update { it.copy(showCleanConfirmationDialog = false, scanState = ScanState.CLEANING) }

        viewModelScope.launch {
            val itemsToClean = _uiState.value.junkItems.filter { it.isChecked }
            val (freedBytes, count) = scanner.cleanItems(itemsToClean)

            // Remaining items that were unchecked or failed deletion
            val remainingItems = _uiState.value.junkItems.filter { !it.isChecked }

            // Insert into history
            val notes = if (count > 0) "Safely cleared $count items (${freedBytes / (1024 * 1024)} MB)" else "No items deleted"
            val record = ScanRecordEntity(
                junkFoundBytes = _uiState.value.totalDetectedJunkBytes,
                junkCleanedBytes = freedBytes,
                healthScore = _uiState.value.healthScore.score,
                itemsCleanedCount = count,
                summaryNotes = notes
            )
            historyDao.insertRecord(record)

            refreshHardwareStats()

            _uiState.update {
                it.copy(
                    scanState = ScanState.CLEANED,
                    junkItems = remainingItems,
                    lastCleanedBytes = freedBytes,
                    lastCleanedCount = count
                )
            }
        }
    }

    fun resetToIdle() {
        _uiState.update { it.copy(scanState = ScanState.IDLE) }
        refreshHardwareStats()
    }

    fun loadStorageBreakdown() {
        viewModelScope.launch {
            val breakdown = scanner.getStorageCategoryBreakdown()
            _uiState.update { it.copy(storageBreakdown = breakdown) }
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = scanner.getInstalledAppsList()
            _uiState.update { it.copy(installedApps = apps) }
        }
    }

    fun setAppSearchQuery(query: String) {
        _uiState.update { it.copy(appSearchQuery = query) }
    }

    fun toggleShowSystemApps() {
        _uiState.update { it.copy(showSystemApps = !it.showSystemApps) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyDao.clearHistory()
        }
    }

    // System navigation helpers
    fun openAppDetails(packageName: String) = scanner.openAppDetailsSettings(packageName)
    fun openStorageSettings() = scanner.openStorageSettings()
    fun openBatterySettings() = scanner.openBatterySettings()
    fun openDisplaySettings() = scanner.openDisplaySettings()
    fun openZenModeSettings() = scanner.openZenModeSettings()
}
