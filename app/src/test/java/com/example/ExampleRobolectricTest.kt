package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.*
import com.example.util.DeviceScanner
import com.example.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HR One Tap Cleaner", appName)
    }

    @Test
    fun `formatBytes formats correctly`() {
        assertEquals("0 B", FormatUtils.formatBytes(0L))
        assertEquals("1.0 KB", FormatUtils.formatBytes(1024L))
        assertEquals("1.0 MB", FormatUtils.formatBytes(1024L * 1024L))
        assertEquals("2.5 GB", FormatUtils.formatBytes((2.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun `health score calculation evaluates severe storage strain`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scanner = DeviceScanner(context)

        val fullStorage = StorageInfo(
            totalBytes = 100_000_000_000L,
            freeBytes = 5_000_000_000L,
            usedBytes = 95_000_000_000L,
            usedPercent = 0.95f
        )
        val normalMemory = MemoryStatus(
            totalBytes = 8_000_000_000L,
            availBytes = 4_000_000_000L,
            usedBytes = 4_000_000_000L,
            usedPercent = 0.5f,
            isLowMemory = false
        )
        val normalBattery = BatteryStatus(
            levelPercent = 80,
            temperatureC = 32.0f
        )

        val report = scanner.calculateHealthScore(fullStorage, normalMemory, normalBattery, 0L)
        assertTrue(report.score < 80)
        assertTrue(report.issuesFound.any { it.contains("Critical Storage") })
    }
}
