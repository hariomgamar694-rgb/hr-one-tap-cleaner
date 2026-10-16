package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CleanerUiState
import com.example.ui.CleanerViewModel
import com.example.ui.theme.*

@Composable
fun SmartRepairScreen(
    uiState: CleanerUiState,
    viewModel: CleanerViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("smart_repair_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.BuildCircle, contentDescription = "Repair icon", tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "Smart System Repair Guide",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Guided fixes for widespread Android OS glitches",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Because privileged system actions are securely sandboxed in modern Android, HR Cleaner provides official guided workflows and instant deep links to official system repair screens.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Issue 1: Google Play Store Pending Downloads
        item {
            RepairIssueCard(
                title = "Play Store 'Download Pending' / Update Glitch",
                badge = "High Success",
                icon = Icons.Default.Shop,
                accentColor = PrimaryBlue,
                description = "Apps refuse to update or remain stuck on 'Pending' indefinitely.",
                steps = listOf(
                    "Tap below to open Google Play Store Application Info.",
                    "Select 'Storage and Cache'.",
                    "Tap 'Clear Cache' (this will not delete your account or purchased apps).",
                    "Force stop the Play Store and relaunch it."
                ),
                actionLabel = "Open Play Store App Info",
                onAction = { viewModel.openAppDetails("com.android.vending") }
            )
        }

        // Issue 2: App Freezing & Sudden Crash Glitches
        item {
            RepairIssueCard(
                title = "App Crashing & Web Freeze Glitches",
                badge = "Common",
                icon = Icons.Default.BugReport,
                accentColor = CrimsonAlert,
                description = "Apps suddenly close when loading feeds, carts, or login webviews.",
                steps = listOf(
                    "Most in-app rendering bugs originate from Android System WebView.",
                    "Clear the crashing app's specific cache via App Manager.",
                    "Update Android System WebView and Google Chrome via Play Store.",
                    "Restart device to clear stale memory pointers."
                ),
                actionLabel = "Open WebView System Settings",
                onAction = { viewModel.openAppDetails("com.google.android.webview") }
            )
        }

        // Issue 3: Persistent Storage Warning Even After Deleting Files
        item {
            RepairIssueCard(
                title = "'Storage Space Running Out' Won't Disappear",
                badge = "Storage",
                icon = Icons.Default.SdStorage,
                accentColor = AmberWarning,
                description = "System notification persists even after you delete photos or videos.",
                steps = listOf(
                    "Photos and files deleted in modern Android go to a 30-Day Trash Bin.",
                    "Open Google Photos / Gallery and permanently empty the 'Trash' folder.",
                    "Open WhatsApp / Telegram settings and clear cached media.",
                    "Inspect system storage partitions via Android Storage Settings."
                ),
                actionLabel = "Open Android Storage Settings",
                onAction = { viewModel.openStorageSettings() }
            )
        }

        // Issue 4: Abnormal Background Battery Drain
        item {
            RepairIssueCard(
                title = "Sudden Overnight Battery Drain",
                badge = "Battery",
                icon = Icons.Default.BatteryAlert,
                accentColor = EmeraldSafe,
                description = "Phone loses 20%+ battery while idle with screen off.",
                steps = listOf(
                    "Check battery usage summary for high background wake-locks.",
                    "Turn on Adaptive Battery in Android settings.",
                    "Restrict background battery activity on rarely used social apps."
                ),
                actionLabel = "Open Battery Saver Settings",
                onAction = { viewModel.openBatterySettings() }
            )
        }
    }
}

@Composable
private fun RepairIssueCard(
    title: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    description: String,
    steps: List<String>,
    actionLabel: String,
    onAction: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (expanded) 3 else 1
                        )
                    }
                }

                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Text(
                        text = "Step-by-Step Guided Solution:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    steps.forEachIndexed { idx, step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                            Text(
                                text = step,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onAction,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
