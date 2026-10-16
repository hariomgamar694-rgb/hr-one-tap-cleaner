package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CleanerUiState
import com.example.ui.CleanerViewModel
import com.example.ui.theme.*
import com.example.util.FormatUtils

@Composable
fun LagGamingScreen(
    uiState: CleanerUiState,
    viewModel: CleanerViewModel,
    modifier: Modifier = Modifier
) {
    val memory = uiState.memoryStatus
    val storage = uiState.storageInfo
    val battery = uiState.batteryStatus

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("lag_gaming_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Lag Diagnostics Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lag & Performance Diagnostics",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (memory.isLowMemory || storage.usedPercent > 0.88f) CrimsonAlert.copy(alpha = 0.15f) else EmeraldSafe.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (memory.isLowMemory) "Memory Pressure" else if (storage.usedPercent > 0.88f) "Storage Strain" else "Stable",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (memory.isLowMemory || storage.usedPercent > 0.88f) CrimsonAlert else EmeraldSafe,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // RAM meter
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("System RAM Utilization", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${(memory.usedPercent * 100).toInt()}% (${FormatUtils.formatBytes(memory.usedBytes)} / ${FormatUtils.formatBytes(memory.totalBytes)})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { memory.usedPercent },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = if (memory.isLowMemory) CrimsonAlert else PrimaryBlue,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Diagnostic factors
                    DiagnosticItem(
                        label = "Storage Headroom",
                        value = "${FormatUtils.formatBytes(storage.freeBytes)} Available (${(storage.freePercent * 100).toInt()}%)",
                        isOk = storage.usedPercent < 0.85f,
                        explanation = if (storage.usedPercent >= 0.85f) "Low storage causes write thrashing and frame stutter" else "Sufficient storage headroom for temporary Android swap"
                    )

                    DiagnosticItem(
                        label = "Thermal Throttling Risk",
                        value = "${battery.temperatureC.toInt()}°C",
                        isOk = battery.temperatureC < 42f,
                        explanation = if (battery.temperatureC >= 42f) "CPU/GPU clocks are likely throttling to prevent overheating" else "Processor running at normal operating frequency"
                    )

                    DiagnosticItem(
                        label = "Low Memory System Flag",
                        value = if (memory.isLowMemory) "ACTIVE ALERT" else "NORMAL",
                        isOk = !memory.isLowMemory,
                        explanation = if (memory.isLowMemory) "Android kernel actively killing background processes" else "Ample memory available for smooth multitasking"
                    )
                }
            }
        }

        // 2. Gaming Assistant
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GoldAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SportsEsports, contentDescription = "Gaming icon", tint = GoldDark, modifier = Modifier.size(22.dp))
                            }
                            Text(
                                text = "Safe Gaming Assistant",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = "Prepare your device for optimal, stutter-free gameplay using authentic Android system configurations:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    GamingCheckItem(
                        title = "1. Temperature Pre-Check",
                        status = if (battery.temperatureC < 38f) "Ready (${battery.temperatureC.toInt()}°C)" else "Wait to Cool (${battery.temperatureC.toInt()}°C)",
                        isReady = battery.temperatureC < 38f
                    )

                    GamingCheckItem(
                        title = "2. Storage Buffer for Shaders",
                        status = if (storage.freeBytes > 3_000_000_000L) "Ready (${FormatUtils.formatBytes(storage.freeBytes)})" else "Critically Low",
                        isReady = storage.freeBytes > 3_000_000_000L
                    )

                    GamingCheckItem(
                        title = "3. Memory Buffer for Textures",
                        status = if (memory.availBytes > 1_000_000_000L) "Ready (${FormatUtils.formatBytes(memory.availBytes)})" else "Low RAM",
                        isReady = memory.availBytes > 1_000_000_000L
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.openZenModeSettings() },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.DoNotDisturb, contentDescription = "DND", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DND Gaming Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.openDisplaySettings() },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepSapphire)
                        ) {
                            Icon(Icons.Default.Tv, contentDescription = "Refresh Rate", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Display Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Ethical Gaming Disclosure
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = "Ethics", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Text(
                        text = "HR Transparent Promise: Android OS enforces hardware protections that prevent third-party apps from overclocking chipsets or bypassing game FPS locks without root. We optimize gaming safely by helping you manage real thermal and storage overhead.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItem(label: String, value: String, isOk: Boolean, explanation: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOk) EmeraldSafe else AmberWarning
            )
        }
        Text(text = explanation, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GamingCheckItem(title: String, status: String, isReady: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isReady) EmeraldSafe else AmberWarning,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = status,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isReady) EmeraldSafe else AmberWarning
            )
        }
    }
}
