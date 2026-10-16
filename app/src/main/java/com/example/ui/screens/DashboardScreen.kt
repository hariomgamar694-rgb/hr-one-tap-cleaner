package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.model.ScanState
import com.example.ui.CleanerTab
import com.example.ui.CleanerUiState
import com.example.ui.CleanerViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    uiState: CleanerUiState,
    viewModel: CleanerViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. App Header with HR brand & health pill
        item {
            HeaderBar(
                healthScore = uiState.healthScore,
                onRefresh = { viewModel.refreshHardwareStats() }
            )
        }

        // 2. Quick Hardware status cards (Storage, RAM, Heat, Battery)
        item {
            QuickMetricsRow(
                storageInfo = uiState.storageInfo,
                batteryStatus = uiState.batteryStatus,
                memoryStatus = uiState.memoryStatus,
                onNavigateTab = { viewModel.setTab(it) }
            )
        }

        // 3. Central One Tap Scan / Clean Button
        item {
            OneTapButton(
                scanState = uiState.scanState,
                scanProgress = uiState.scanProgress,
                scanStepText = uiState.scanStepText,
                selectedJunkBytes = uiState.selectedJunkBytes,
                lastCleanedBytes = uiState.lastCleanedBytes,
                onButtonClick = {
                    when (uiState.scanState) {
                        ScanState.IDLE -> viewModel.startScan()
                        ScanState.SCANNING -> { /* in progress */ }
                        ScanState.SCANNED -> viewModel.requestCleanConfirmation()
                        ScanState.CLEANING -> { /* in progress */ }
                        ScanState.CLEANED -> viewModel.resetToIdle()
                    }
                }
            )
        }

        // 4. Scanned Findings section (Visible when Scanned or Cleaned)
        if (uiState.scanState == ScanState.SCANNED) {
            item {
                JunkFindingsSection(
                    junkItems = uiState.junkItems,
                    selectedJunkBytes = uiState.selectedJunkBytes,
                    totalJunkBytes = uiState.totalDetectedJunkBytes,
                    onToggleItem = { viewModel.toggleJunkItem(it) },
                    onToggleCategory = { cat, checked -> viewModel.toggleCategory(cat, checked) },
                    onSelectAllSafe = { viewModel.selectAllSafeItems() },
                    onCleanClicked = { viewModel.requestCleanConfirmation() }
                )
            }
        }

        // 5. Real Device Health Score and observations
        item {
            HealthScoreCard(healthScore = uiState.healthScore)
        }

        // 6. Quick Feature Grid
        item {
            FeaturesGridSection(
                onNavigate = { viewModel.setTab(it) }
            )
        }
    }
}

@Composable
private fun FeaturesGridSection(
    onNavigate: (CleanerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Optimization & Diagnostic Tools",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val features = listOf(
            FeatureItem(
                title = "Storage Analyzer",
                desc = "Visual storage category breakdown",
                icon = Icons.Default.PieChart,
                tab = CleanerTab.STORAGE,
                color = PrimaryBlue
            ),
            FeatureItem(
                title = "Heat & Battery",
                desc = "Real thermal monitor & safe cooling tips",
                icon = Icons.Default.Thermostat,
                tab = CleanerTab.THERMAL_BATTERY,
                color = AmberWarning
            ),
            FeatureItem(
                title = "Lag Troubleshooter",
                desc = "Diagnose low memory & bottlenecks",
                icon = Icons.Default.Speed,
                tab = CleanerTab.LAG_GAMING,
                color = SkyBlueGlow
            ),
            FeatureItem(
                title = "Gaming Assistant",
                desc = "Safe pre-gaming checklist & DND mode",
                icon = Icons.Default.SportsEsports,
                tab = CleanerTab.LAG_GAMING,
                color = GoldAccent
            ),
            FeatureItem(
                title = "Smart Repair",
                desc = "Fix Play Store, crashes & settings",
                icon = Icons.Default.BuildCircle,
                tab = CleanerTab.SMART_REPAIR,
                color = EmeraldSafe
            ),
            FeatureItem(
                title = "App Manager",
                desc = "Installed apps & direct settings access",
                icon = Icons.Default.Apps,
                tab = CleanerTab.APP_MANAGER,
                color = DeepSapphire
            )
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (i in features.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val first = features[i]
                    FeatureShortcutCard(
                        item = first,
                        onClick = { onNavigate(first.tab) },
                        modifier = Modifier.weight(1f)
                    )

                    if (i + 1 < features.size) {
                        val second = features[i + 1]
                        FeatureShortcutCard(
                            item = second,
                            onClick = { onNavigate(second.tab) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private data class FeatureItem(
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val tab: CleanerTab,
    val color: Color
)

@Composable
private fun FeatureShortcutCard(
    item: FeatureItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("feature_shortcut_${item.title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = item.desc,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}
