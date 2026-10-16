package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScanState
import com.example.ui.theme.*
import com.example.util.FormatUtils

@Composable
fun OneTapButton(
    scanState: ScanState,
    scanProgress: Float,
    scanStepText: String,
    selectedJunkBytes: Long,
    lastCleanedBytes: Long,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for idle and scanned states
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing energy ring
        if (scanState == ScanState.IDLE || scanState == ScanState.SCANNED) {
            Box(
                modifier = Modifier
                    .size(208.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                (if (scanState == ScanState.SCANNED) GoldAccent else SkyBlueGlow).copy(alpha = auraAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Secondary subtle gold border ring
        Box(
            modifier = Modifier
                .size(186.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            GoldAccent,
                            PrimaryBlue,
                            SkyBlueGlow,
                            GoldAccent
                        )
                    )
                )
                .padding(3.dp) // Border thickness
        ) {
            // Main Button Circle
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .shadow(elevation = 12.dp, shape = CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = when (scanState) {
                                ScanState.CLEANED -> listOf(Color(0xFF065F46), Color(0xFF047857))
                                ScanState.SCANNED -> listOf(DeepSapphire, Color(0xFF1E3A8A))
                                else -> listOf(DeepSapphire, NavyDark)
                            }
                        )
                    )
                    .clickable(onClick = onButtonClick)
                    .testTag("one_tap_main_button"),
                contentAlignment = Alignment.Center
            ) {
                // Interactive content by state
                when (scanState) {
                    ScanState.IDLE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "One tap scan icon",
                                tint = GoldAccent,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ONE TAP",
                                color = PureWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "SCAN",
                                color = SkyBlueGlow,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Detect safe junk",
                                color = PureWhite.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    ScanState.SCANNING -> {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { scanProgress },
                                modifier = Modifier.size(150.dp),
                                color = GoldAccent,
                                strokeWidth = 5.dp,
                                trackColor = PureWhite.copy(alpha = 0.15f)
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "${(scanProgress * 100).toInt()}%",
                                    color = PureWhite,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = "SCANNING",
                                    color = SkyBlueGlow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = scanStepText,
                                    color = PureWhite.copy(alpha = 0.75f),
                                    fontSize = 9.sp,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.widthIn(max = 120.dp)
                                )
                            }
                        }
                    }

                    ScanState.SCANNED -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = "Clean junk icon",
                                tint = GoldAccent,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "CLEAN NOW",
                                color = PureWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = FormatUtils.formatBytes(selectedJunkBytes),
                                color = GoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Selected Safe Junk",
                                color = PureWhite.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    ScanState.CLEANING -> {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(140.dp),
                                color = SkyBlueGlow,
                                strokeWidth = 5.dp
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "CLEANING",
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Freeing storage...",
                                    color = PureWhite.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    ScanState.CLEANED -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Cleaned checkmark",
                                tint = PureWhite,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "CLEANED!",
                                color = PureWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "+${FormatUtils.formatBytes(lastCleanedBytes)} Freed",
                                color = GoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Tap to scan again",
                                color = PureWhite.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
