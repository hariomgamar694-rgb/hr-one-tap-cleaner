package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.provider.Settings
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class DeviceScanner(private val context: Context) {

    fun getStorageInfo(): StorageInfo {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
            val usedPercent = if (totalBytes > 0) usedBytes.toFloat() / totalBytes.toFloat() else 0f

            StorageInfo(
                totalBytes = totalBytes,
                freeBytes = freeBytes,
                usedBytes = usedBytes,
                usedPercent = usedPercent
            )
        } catch (e: Exception) {
            StorageInfo(totalBytes = 64_000_000_000L, freeBytes = 16_000_000_000L, usedBytes = 48_000_000_000L, usedPercent = 0.75f)
        }
    }

    fun getMemoryStatus(): MemoryStatus {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager?.getMemoryInfo(memInfo)

            val total = memInfo.totalMem
            val avail = memInfo.availMem
            val used = (total - avail).coerceAtLeast(0L)
            val usedPercent = if (total > 0) used.toFloat() / total.toFloat() else 0f

            MemoryStatus(
                totalBytes = total,
                availBytes = avail,
                usedBytes = used,
                usedPercent = usedPercent,
                isLowMemory = memInfo.lowMemory,
                thresholdBytes = memInfo.threshold
            )
        } catch (e: Exception) {
            MemoryStatus()
        }
    }

    fun getBatteryStatus(): BatteryStatus {
        return try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, ifilter)

            if (batteryStatus != null) {
                val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val levelPercent = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 100

                val rawTemp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                val tempC = if (rawTemp > 0) rawTemp / 10.0f else 31.5f

                val voltageMv = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

                val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val statusStr = when (status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                    BatteryManager.BATTERY_STATUS_FULL -> "Full"
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                    else -> "Normal"
                }

                val health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                val healthStr = when (health) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                    BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                    else -> "Standard"
                }

                val chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val pluggedSource = when (chargePlug) {
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Base"
                    else -> "Battery Power"
                }

                val tech = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

                BatteryStatus(
                    levelPercent = levelPercent,
                    status = statusStr,
                    health = healthStr,
                    temperatureC = tempC,
                    voltageMv = voltageMv,
                    isCharging = isCharging,
                    pluggedSource = pluggedSource,
                    technology = tech
                )
            } else {
                BatteryStatus()
            }
        } catch (e: Exception) {
            BatteryStatus()
        }
    }

    suspend fun scanJunkItems(onProgress: (Int, String) -> Unit): List<JunkItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<JunkItem>()

        onProgress(10, "Scanning application internal cache...")
        try {
            val internalCache = context.cacheDir
            scanDirectory(internalCache, JunkCategory.CACHE, isSafe = true, results)
        } catch (_: Exception) {}

        onProgress(25, "Scanning application code cache and temporary buffers...")
        try {
            val codeCache = context.codeCacheDir
            scanDirectory(codeCache, JunkCategory.CACHE, isSafe = true, results)
        } catch (_: Exception) {}

        onProgress(40, "Scanning external app cache directories...")
        try {
            val externalCaches = context.externalCacheDirs
            externalCaches?.filterNotNull()?.forEach { cacheDir ->
                scanDirectory(cacheDir, JunkCategory.CACHE, isSafe = true, results)
            }
        } catch (_: Exception) {}

        onProgress(55, "Scanning accessible downloads & temporary files...")
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir != null && downloadsDir.exists() && downloadsDir.canRead()) {
                scanDownloadFolder(downloadsDir, results)
            }
        } catch (_: Exception) {}

        onProgress(70, "Scanning accessible empty directories...")
        try {
            val externalRoot = Environment.getExternalStorageDirectory()
            if (externalRoot != null && externalRoot.exists() && externalRoot.canRead()) {
                scanEmptyDirectories(externalRoot, results, maxDepth = 2)
            }
        } catch (_: Exception) {}

        onProgress(85, "Analyzing large files across MediaStore...")
        try {
            scanLargeMediaFiles(results)
        } catch (_: Exception) {}

        onProgress(100, "Scan completed")
        results
    }

    private fun scanDirectory(dir: File, category: JunkCategory, isSafe: Boolean, results: MutableList<JunkItem>) {
        if (!dir.exists() || !dir.canRead()) return
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isFile && file.length() > 0) {
                results.add(
                    JunkItem(
                        id = UUID.randomUUID().toString(),
                        name = file.name,
                        path = file.absolutePath,
                        sizeBytes = file.length(),
                        category = category,
                        isChecked = isSafe,
                        isSafeToDelete = isSafe,
                        detail = "Located in ${file.parentFile?.name ?: "app cache"}",
                        lastModified = file.lastModified()
                    )
                )
            } else if (file.isDirectory) {
                // shallow recursion for sub-caches
                file.listFiles()?.take(50)?.forEach { subFile ->
                    if (subFile.isFile && subFile.length() > 0) {
                        results.add(
                            JunkItem(
                                id = UUID.randomUUID().toString(),
                                name = subFile.name,
                                path = subFile.absolutePath,
                                sizeBytes = subFile.length(),
                                category = category,
                                isChecked = isSafe,
                                isSafeToDelete = isSafe,
                                detail = "Sub-cache in ${file.name}",
                                lastModified = subFile.lastModified()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun scanDownloadFolder(dir: File, results: MutableList<JunkItem>) {
        val files = dir.listFiles() ?: return
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)

        for (file in files) {
            if (file.isFile) {
                val nameLower = file.name.lowercase()
                val isApk = nameLower.endsWith(".apk")
                val isTemp = nameLower.endsWith(".tmp") || nameLower.endsWith(".log") || nameLower.endsWith(".bak")
                val isOld = file.lastModified() < thirtyDaysAgo

                if (isApk) {
                    results.add(
                        JunkItem(
                            id = UUID.randomUUID().toString(),
                            name = file.name,
                            path = file.absolutePath,
                            sizeBytes = file.length(),
                            category = JunkCategory.OBSOLETE_APK,
                            isChecked = true,
                            isSafeToDelete = true,
                            detail = "Leftover installation package in Downloads",
                            lastModified = file.lastModified()
                        )
                    )
                } else if (isTemp) {
                    results.add(
                        JunkItem(
                            id = UUID.randomUUID().toString(),
                            name = file.name,
                            path = file.absolutePath,
                            sizeBytes = file.length(),
                            category = JunkCategory.LOGS_AND_TEMP,
                            isChecked = true,
                            isSafeToDelete = true,
                            detail = "Temporary log or buffer file",
                            lastModified = file.lastModified()
                        )
                    )
                } else if (isOld && file.length() > 5 * 1024 * 1024L) {
                    results.add(
                        JunkItem(
                            id = UUID.randomUUID().toString(),
                            name = file.name,
                            path = file.absolutePath,
                            sizeBytes = file.length(),
                            category = JunkCategory.OLD_DOWNLOADS,
                            isChecked = false, // Not checked by default to protect user files
                            isSafeToDelete = false,
                            detail = "Downloaded over 30 days ago",
                            lastModified = file.lastModified()
                        )
                    )
                }
            }
        }
    }

    private fun scanEmptyDirectories(dir: File, results: MutableList<JunkItem>, maxDepth: Int) {
        if (maxDepth <= 0 || !dir.exists() || !dir.canRead()) return
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                val children = file.listFiles()
                if (children != null && children.isEmpty() && !file.name.startsWith(".")) {
                    results.add(
                        JunkItem(
                            id = UUID.randomUUID().toString(),
                            name = "${file.name}/",
                            path = file.absolutePath,
                            sizeBytes = 4096L, // Directory inode size representation
                            category = JunkCategory.EMPTY_FOLDERS,
                            isChecked = true,
                            isSafeToDelete = true,
                            detail = "Ghost empty directory",
                            lastModified = file.lastModified()
                        )
                    )
                } else if (file.name in listOf("Android", "Download", "DCIM", "Pictures", "Documents")) {
                    // Do not descend deeply into protected folders
                    continue
                } else {
                    scanEmptyDirectories(file, results, maxDepth - 1)
                }
            }
        }
    }

    private fun scanLargeMediaFiles(results: MutableList<JunkItem>) {
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )
        // Files larger than 50MB
        val minSizeBytes = 50L * 1024 * 1024
        val selection = "${MediaStore.Files.FileColumns.SIZE} >= ?"
        val selectionArgs = arrayOf(minSizeBytes.toString())
        val sortOrder = "${MediaStore.Files.FileColumns.SIZE} DESC LIMIT 20"

        try {
            val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
            cursor?.use {
                val idCol = it.getColumnIndex(MediaStore.Files.FileColumns._ID)
                val nameCol = it.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeCol = it.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val dateCol = it.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)

                while (it.moveToNext()) {
                    val id = if (idCol >= 0) it.getLong(idCol) else 0L
                    val name = if (nameCol >= 0) it.getString(nameCol) ?: "Large File" else "Large File"
                    val size = if (sizeCol >= 0) it.getLong(sizeCol) else 0L
                    val dateModified = if (dateCol >= 0) it.getLong(dateCol) * 1000L else System.currentTimeMillis()

                    results.add(
                        JunkItem(
                            id = "media_$id",
                            name = name,
                            path = "content://media/external/files/$id",
                            sizeBytes = size,
                            category = JunkCategory.LARGE_FILES,
                            isChecked = false, // NEVER select large files by default
                            isSafeToDelete = false,
                            detail = "Large file (${FormatUtils.formatBytes(size)}) - Review required",
                            lastModified = dateModified
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // MediaStore query gracefully bypassed if permission pending
        }
    }

    suspend fun getStorageCategoryBreakdown(): StorageCategoryBreakdown = withContext(Dispatchers.IO) {
        var imagesSize = 0L
        var videosSize = 0L
        var audioSize = 0L
        var documentsSize = 0L
        var apksSize = 0L
        var archivesSize = 0L

        fun querySize(uri: Uri, selection: String? = null, args: Array<String>? = null): Long {
            var sum = 0L
            try {
                val projection = arrayOf(MediaStore.MediaColumns.SIZE)
                val cursor = context.contentResolver.query(uri, projection, selection, args, null)
                cursor?.use {
                    val sizeCol = it.getColumnIndex(MediaStore.MediaColumns.SIZE)
                    if (sizeCol >= 0) {
                        while (it.moveToNext()) {
                            sum += it.getLong(sizeCol)
                        }
                    }
                }
            } catch (_: Exception) {}
            return sum
        }

        imagesSize = querySize(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        videosSize = querySize(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        audioSize = querySize(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)

        // Query documents and downloads
        try {
            val filesUri = MediaStore.Files.getContentUri("external")
            val projection = arrayOf(MediaStore.Files.FileColumns.SIZE, MediaStore.Files.FileColumns.MIME_TYPE, MediaStore.Files.FileColumns.DISPLAY_NAME)
            val cursor = context.contentResolver.query(filesUri, projection, null, null, null)
            cursor?.use {
                val sizeCol = it.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val mimeCol = it.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                val nameCol = it.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)

                while (it.moveToNext()) {
                    val size = if (sizeCol >= 0) it.getLong(sizeCol) else 0L
                    val mime = if (mimeCol >= 0) it.getString(mimeCol)?.lowercase() ?: "" else ""
                    val name = if (nameCol >= 0) it.getString(nameCol)?.lowercase() ?: "" else ""

                    when {
                        mime.contains("pdf") || mime.contains("document") || mime.contains("msword") ||
                                name.endsWith(".pdf") || name.endsWith(".docx") || name.endsWith(".xlsx") || name.endsWith(".txt") -> {
                            documentsSize += size
                        }
                        name.endsWith(".apk") || mime.contains("vnd.android.package-archive") -> {
                            apksSize += size
                        }
                        name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".tar") || name.endsWith(".gz") || name.endsWith(".7z") -> {
                            archivesSize += size
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        val other = 100_000_000L // Baseline estimate for miscellaneous files

        StorageCategoryBreakdown(
            imagesBytes = imagesSize,
            videosBytes = videosSize,
            audioBytes = audioSize,
            documentsBytes = documentsSize,
            apksBytes = apksSize,
            archivesBytes = archivesSize,
            otherBytes = other
        )
    }

    suspend fun getInstalledAppsList(): List<AppInfoItem> = withContext(Dispatchers.IO) {
        val appList = mutableListOf<AppInfoItem>()
        val pm = context.packageManager

        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolvedApps = pm.queryIntentActivities(mainIntent, 0)
            val seenPackages = mutableSetOf<String>()

            for (resolveInfo in resolvedApps) {
                val pkgName = resolveInfo.activityInfo.packageName
                if (seenPackages.contains(pkgName)) continue
                seenPackages.add(pkgName)

                try {
                    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pm.getPackageInfo(pkgName, PackageManager.PackageInfoFlags.of(0))
                    } else {
                        @Suppress("DEPRECATION")
                        pm.getPackageInfo(pkgName, 0)
                    }

                    val appName = resolveInfo.loadLabel(pm).toString()
                    val isSystem = (packageInfo.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0
                    val sourceDir = packageInfo.applicationInfo?.sourceDir
                    val sizeBytes = if (sourceDir != null) File(sourceDir).length() else 0L

                    appList.add(
                        AppInfoItem(
                            packageName = pkgName,
                            appName = appName,
                            versionName = packageInfo.versionName ?: "1.0",
                            isSystemApp = isSystem,
                            apkSizeBytes = sizeBytes,
                            firstInstallTime = packageInfo.firstInstallTime,
                            lastUpdateTime = packageInfo.lastUpdateTime
                        )
                    )
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        appList.sortedByDescending { it.apkSizeBytes }
    }

    suspend fun cleanItems(itemsToDelete: List<JunkItem>): Pair<Long, Int> = withContext(Dispatchers.IO) {
        var freedBytes = 0L
        var cleanedCount = 0

        for (item in itemsToDelete) {
            try {
                if (item.path.startsWith("content://")) {
                    // For MediaStore files, attempt contentResolver delete if allowed
                    val uri = Uri.parse(item.path)
                    val deleted = context.contentResolver.delete(uri, null, null)
                    if (deleted > 0) {
                        freedBytes += item.sizeBytes
                        cleanedCount++
                    }
                } else {
                    val file = File(item.path)
                    if (file.exists()) {
                        if (file.isDirectory) {
                            val deleted = file.delete()
                            if (deleted) {
                                freedBytes += item.sizeBytes
                                cleanedCount++
                            }
                        } else {
                            val length = file.length()
                            if (file.delete()) {
                                freedBytes += length
                                cleanedCount++
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // Also clean context internal cache files to maximize recovery
        try {
            context.cacheDir.listFiles()?.forEach { f ->
                val size = f.length()
                if (f.delete()) {
                    freedBytes += size
                    cleanedCount++
                }
            }
        } catch (_: Exception) {}

        Pair(freedBytes, cleanedCount)
    }

    fun calculateHealthScore(
        storage: StorageInfo,
        memory: MemoryStatus,
        battery: BatteryStatus,
        junkTotalBytes: Long
    ): DeviceHealthScore {
        var score = 100
        val issues = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        // 1. Storage impact
        if (storage.usedPercent >= 0.90f) {
            score -= 30
            issues.add("Critical Storage: Over 90% space occupied")
            recommendations.add("Free up space to avoid severe system slowdowns and app update failures")
        } else if (storage.usedPercent >= 0.80f) {
            score -= 15
            issues.add("Storage High: More than 80% space occupied")
            recommendations.add("Remove old downloads or offload rarely used applications")
        }

        // 2. Memory impact
        if (memory.isLowMemory) {
            score -= 25
            issues.add("Low RAM Alert: Android system reports active memory strain")
            recommendations.add("Close unused background multitasking applications via Android Recent Apps")
        } else if (memory.usedPercent >= 0.85f) {
            score -= 10
            issues.add("Elevated RAM Consumption: Over 85% system memory utilized")
            recommendations.add("Restart device occasionally to reset memory buffers")
        }

        // 3. Thermal impact
        if (battery.temperatureC >= 45.0f) {
            score -= 25
            issues.add("Thermal Alert: Battery temperature is ${battery.temperatureC}°C (Hot)")
            recommendations.add("Stop intensive 3D gaming or video recording and disconnect fast charger")
        } else if (battery.temperatureC >= 40.0f) {
            score -= 10
            issues.add("Device Warm: Battery temperature is ${battery.temperatureC}°C")
            recommendations.add("Avoid direct sun exposure and remove thick phone cases while heavy usage")
        }

        // 4. Junk accumulation
        if (junkTotalBytes > 1024L * 1024 * 1024) { // > 1GB
            score -= 12
            issues.add("Over 1 GB of temporary residue and cache files detected")
            recommendations.add("Perform One Tap Cleanup to reclaim storage")
        } else if (junkTotalBytes > 250L * 1024 * 1024) { // > 250MB
            score -= 5
            issues.add("Residual cache files ready for safe cleanup")
            recommendations.add("Run safe junk cleanup")
        }

        score = score.coerceIn(15, 100)

        val rating = when {
            score >= 88 -> HealthRating.OPTIMAL
            score >= 70 -> HealthRating.GOOD
            score >= 50 -> HealthRating.FAIR
            else -> HealthRating.CRITICAL
        }

        val summary = when (rating) {
            HealthRating.OPTIMAL -> "Your device hardware and storage are in peak operating condition."
            HealthRating.GOOD -> "Device is running smoothly with minor optimization opportunities."
            HealthRating.FAIR -> "Noticeable resource pressure detected. Clean junk and monitor temperature."
            HealthRating.CRITICAL -> "Heavy resource strain detected. Immediate cleanup and cooling recommended."
        }

        if (recommendations.isEmpty()) {
            recommendations.add("System parameters are within optimal manufacturer limits.")
            recommendations.add("Keep Android system and apps updated via Google Play Store.")
        }

        return DeviceHealthScore(
            score = score,
            rating = rating,
            statusSummary = summary,
            issuesFound = issues,
            recommendations = recommendations
        )
    }

    fun openAppDetailsSettings(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun openStorageSettings() {
        try {
            val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun openBatterySettings() {
        try {
            val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun openDisplaySettings() {
        try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun openZenModeSettings() {
        try {
            val intent = Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }
}
