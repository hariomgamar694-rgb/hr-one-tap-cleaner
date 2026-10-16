package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryStatus
import com.example.model.MemoryStatus
import com.example.model.StorageInfo
import com.example.ui.CleanerTab
import com.example.ui.theme.*
import com.example.util.FormatUtils

@Composable
fun QuickMetricsRow(
    storageInfo: StorageInfo,
    batteryStatus: BatteryStatus,
    memoryStatus: MemoryStatus,
    onNavigateTab: (CleanerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Storage Card
        MetricItemCard(
            title = "Storage",
            value = "${(storageInfo.usedPercent * 100).toInt()}%",
            subtitle = "${FormatUtils.formatBytes(storageInfo.usedBytes)} used",
            icon = Icons.Default.Storage,
            accentColor = PrimaryBlue,
            progress = storageInfo.usedPercent,
            onClick = { onNavigateTab(CleanerTab.STORAGE) },
            modifier = Modifier.weight(1f).testTag("metric_card_storage")
        )

        // Memory (RAM) Card
        MetricItemCard(
            title = "RAM",
            value = "${(memoryStatus.usedPercent * 100).toInt()}%",
            subtitle = "${FormatUtils.formatBytes(memoryStatus.availBytes)} free",
            icon = Icons.Default.Memory,
            accentColor = if (memoryStatus.isLowMemory) CrimsonAlert else SkyBlueGlow,
            progress = memoryStatus.usedPercent,
            onClick = { onNavigateTab(CleanerTab.LAG_GAMING) },
            modifier = Modifier.weight(1f).testTag("metric_card_ram")
        )

        // Thermal Card
        val tempColor = when {
            batteryStatus.temperatureC >= 45f -> CrimsonAlert
            batteryStatus.temperatureC >= 40f -> AmberWarning
            else -> EmeraldSafe
        }
        MetricItemCard(
            title = "Thermal",
            value = "${batteryStatus.temperatureC.toInt()}°C",
            subtitle = batteryStatus.thermalState.label.split(" ").firstOrNull() ?: "Cool",
            icon = Icons.Default.Thermostat,
            accentColor = tempColor,
            progress = (batteryStatus.temperatureC / 50f).coerceIn(0f, 1f),
            onClick = { onNavigateTab(CleanerTab.THERMAL_BATTERY) },
            modifier = Modifier.weight(1f).testTag("metric_card_thermal")
        )

        // Battery Card
        MetricItemCard(
            title = "Battery",
            value = "${batteryStatus.levelPercent}%",
            subtitle = if (batteryStatus.isCharging) "Charging" else batteryStatus.health,
            icon = Icons.Default.BatteryChargingFull,
            accentColor = if (batteryStatus.isCharging) EmeraldSafe else GoldAccent,
            progress = (batteryStatus.levelPercent / 100f).coerceIn(0f, 1f),
            onClick = { onNavigateTab(CleanerTab.THERMAL_BATTERY) },
            modifier = Modifier.weight(1f).testTag("metric_card_battery")
        )
    }
}

@Composable
private fun MetricItemCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
