package com.example.model

enum class JunkCategory(val displayName: String, val description: String) {
    CACHE("App Cache Residue", "Internal and external temporary cache files safely clearable"),
    LOGS_AND_TEMP("Temporary Logs & Buffers", "Old log traces and runtime cache buffers"),
    OBSOLETE_APK("Obsolete APK Packages", "Leftover installation packages no longer needed"),
    EMPTY_FOLDERS("Empty Residue Directories", "Ghost directories created by uninstalled software"),
    LARGE_FILES("Large Media & Files (>50MB)", "Files that occupy substantial device memory"),
    OLD_DOWNLOADS("Downloads Folder Residue", "Downloaded files saved over 30 days ago")
}

data class JunkItem(
    val id: String,
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val category: JunkCategory,
    val isChecked: Boolean = true,
    val isSafeToDelete: Boolean = true,
    val detail: String = "",
    val lastModified: Long = System.currentTimeMillis()
)

data class StorageInfo(
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usedPercent: Float = 0f
) {
    val freePercent: Float get() = (1f - usedPercent).coerceIn(0f, 1f)
}

data class StorageCategoryBreakdown(
    val imagesBytes: Long = 0L,
    val videosBytes: Long = 0L,
    val audioBytes: Long = 0L,
    val documentsBytes: Long = 0L,
    val apksBytes: Long = 0L,
    val archivesBytes: Long = 0L,
    val otherBytes: Long = 0L
) {
    val totalCategorizedBytes: Long
        get() = imagesBytes + videosBytes + audioBytes + documentsBytes + apksBytes + archivesBytes + otherBytes
}

data class BatteryStatus(
    val levelPercent: Int = 100,
    val status: String = "Normal",
    val health: String = "Good",
    val temperatureC: Float = 30.0f,
    val voltageMv: Int = 4000,
    val isCharging: Boolean = false,
    val pluggedSource: String = "Unplugged",
    val technology: String = "Li-ion"
) {
    val thermalState: ThermalState
        get() = when {
            temperatureC >= 45.0f -> ThermalState.OVERHEATED
            temperatureC >= 40.0f -> ThermalState.WARM
            temperatureC >= 36.0f -> ThermalState.MODERATE
            else -> ThermalState.NORMAL
        }
}

enum class ThermalState(val label: String, val tip: String) {
    NORMAL("Normal Thermal Level", "Device is running within optimal temperature limits."),
    MODERATE("Optimal", "Temperature is standard for normal device usage."),
    WARM("Slightly Warm", "Avoid running heavy 3D games or direct sunlight exposure."),
    OVERHEATED("High Temperature Alert", "Disconnect charging and allow device to cool down.")
}

data class MemoryStatus(
    val totalBytes: Long = 0L,
    val availBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usedPercent: Float = 0f,
    val isLowMemory: Boolean = false,
    val thresholdBytes: Long = 0L
)

enum class HealthRating(val label: String) {
    OPTIMAL("Optimal"),
    GOOD("Good"),
    FAIR("Fair"),
    CRITICAL("Needs Attention")
}

data class DeviceHealthScore(
    val score: Int = 85,
    val rating: HealthRating = HealthRating.GOOD,
    val statusSummary: String = "Device operating stably",
    val issuesFound: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)

data class AppInfoItem(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val isSystemApp: Boolean,
    val apkSizeBytes: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long
)

enum class ScanState {
    IDLE,
    SCANNING,
    SCANNED,
    CLEANING,
    CLEANED
}
