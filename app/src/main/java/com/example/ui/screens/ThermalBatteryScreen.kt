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
import com.example.model.BatteryStatus
import com.example.model.ThermalState
import com.example.ui.CleanerUiState
import com.example.ui.CleanerViewModel
import com.example.ui.theme.*

@Composable
fun ThermalBatteryScreen(
    uiState: CleanerUiState,
    viewModel: CleanerViewModel,
    modifier: Modifier = Modifier
) {
    val battery = uiState.batteryStatus

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("thermal_battery_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Thermal Monitor Card
        item {
            val thermalColor = when (battery.thermalState) {
                ThermalState.NORMAL -> EmeraldSafe
                ThermalState.MODERATE -> SkyBlueGlow
                ThermalState.WARM -> AmberWarning
                ThermalState.OVERHEATED -> CrimsonAlert
            }

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
                            text = "Thermal Monitor",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = thermalColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = battery.thermalState.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = thermalColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(thermalColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = "Thermal icon",
                                tint = thermalColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${battery.temperatureC.toInt()}°C",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "/ ${(battery.temperatureC * 9 / 5 + 32).toInt()}°F",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Text(
                                text = battery.thermalState.tip,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Safe thresholds legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ThermalRangePill(label = "Optimal", range = "<36°C", color = EmeraldSafe)
                        ThermalRangePill(label = "Standard", range = "36-39°C", color = SkyBlueGlow)
                        ThermalRangePill(label = "Warm", range = "40-44°C", color = AmberWarning)
                        ThermalRangePill(label = "Hot", range = "≥45°C", color = CrimsonAlert)
                    }
                }
            }
        }

        // 2. Battery Telemetry Card
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
                            text = "Battery Telemetry",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (battery.isCharging) "Charging (${battery.pluggedSource})" else "On Battery",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (battery.isCharging) EmeraldSafe else PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${battery.levelPercent}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryBlue
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Health: ${battery.health}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Voltage: ${battery.voltageMv} mV • Tech: ${battery.technology}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Status: ${battery.status}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 3. Genuine Thermal Cooling Tips
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Authentic Hardware Cooling Advice",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Android hardware dissipates heat through its metal/glass body. No mobile application can bypass physical thermodynamics, but these proven practices actively reduce thermal stress:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    CoolingTipRow(
                        title = "Pause Fast Charging During Intense Gaming",
                        desc = "Simultaneous high-wattage input and GPU processing generate compound thermal spikes."
                    )
                    CoolingTipRow(
                        title = "Remove Thick Protective Cases",
                        desc = "Heavy cases act as thermal insulators, trapping heat inside your device's motherboard."
                    )
                    CoolingTipRow(
                        title = "Lower Display Brightness & GPS Tracking",
                        desc = "The OLED screen panel and GPS modems are top battery heat generators in direct sunlight."
                    )
                }
            }
        }

        // 4. Quick Android Battery Settings Link
        item {
            Button(
                onClick = { viewModel.openBatterySettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("open_system_battery_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepSapphire,
                    contentColor = PureWhite
                )
            ) {
                Icon(Icons.Default.BatterySaver, contentDescription = "Battery Settings", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Official Android Battery Settings", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ThermalRangePill(label: String, range: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = range, fontSize = 9.sp, color = color)
        }
    }
}

@Composable
private fun CoolingTipRow(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircleOutline,
            contentDescription = "Tip",
            tint = EmeraldSafe,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
        }
    }
}
