package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CleanerTab
import com.example.ui.CleanerViewModel
import com.example.ui.components.CleanConfirmationDialog
import com.example.ui.screens.*
import com.example.ui.theme.DeepSapphire
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: CleanerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Runtime Permission Request for Scoped Storage / Media
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        viewModel.updatePermissionStatus(granted)
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val hasAny = permissionsToRequest.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        viewModel.updatePermissionStatus(hasAny)

        if (!hasAny) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    // Predictive back / BackHandler: if not on Home, back key navigates back to Home
    if (uiState.currentTab != CleanerTab.DASHBOARD) {
        BackHandler {
            viewModel.setTab(CleanerTab.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            if (uiState.currentTab != CleanerTab.DASHBOARD) {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.currentTab.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.setTab(CleanerTab.DASHBOARD) },
                            modifier = Modifier.testTag("top_bar_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back to dashboard",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.setTab(CleanerTab.HISTORY_ABOUT) },
                            modifier = Modifier.testTag("top_bar_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "About HR Cleaner",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            AppBottomNavigation(
                currentTab = uiState.currentTab,
                onSelectTab = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = uiState.currentTab,
                label = "screen_crossfade"
            ) { tab ->
                when (tab) {
                    CleanerTab.DASHBOARD -> DashboardScreen(uiState = uiState, viewModel = viewModel)
                    CleanerTab.STORAGE -> StorageAnalyzerScreen(uiState = uiState, viewModel = viewModel)
                    CleanerTab.THERMAL_BATTERY -> ThermalBatteryScreen(uiState = uiState, viewModel = viewModel)
                    CleanerTab.LAG_GAMING -> LagGamingScreen(uiState = uiState, viewModel = viewModel)
                    CleanerTab.SMART_REPAIR -> SmartRepairScreen(uiState = uiState, viewModel = viewModel)
                    CleanerTab.APP_MANAGER -> AppManagerScreen(uiState = uiState, viewModel = viewModel)
                    CleanerTab.HISTORY_ABOUT -> HistoryAboutScreen(uiState = uiState, viewModel = viewModel)
                }
            }

            // Cleanup confirmation dialog
            if (uiState.showCleanConfirmationDialog) {
                CleanConfirmationDialog(
                    selectedBytes = uiState.selectedJunkBytes,
                    onConfirm = { viewModel.confirmAndClean() },
                    onDismiss = { viewModel.dismissCleanConfirmation() }
                )
            }
        }
    }
}

@Composable
private fun AppBottomNavigation(
    currentTab: CleanerTab,
    onSelectTab: (CleanerTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        val navItems = listOf(
            NavItem(CleanerTab.DASHBOARD, "Home", Icons.Default.Home),
            NavItem(CleanerTab.STORAGE, "Storage", Icons.Default.PieChart),
            NavItem(CleanerTab.THERMAL_BATTERY, "Battery", Icons.Default.Thermostat),
            NavItem(CleanerTab.LAG_GAMING, "Gaming", Icons.Default.SportsEsports),
            NavItem(CleanerTab.SMART_REPAIR, "Repair", Icons.Default.Build),
            NavItem(CleanerTab.APP_MANAGER, "Apps", Icons.Default.Apps),
            NavItem(CleanerTab.HISTORY_ABOUT, "Info", Icons.Default.Info)
        )

        navItems.forEach { item ->
            val isSelected = currentTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectTab(item.tab) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    indicatorColor = PrimaryBlue.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_${item.label.lowercase()}")
            )
        }
    }
}

private data class NavItem(
    val tab: CleanerTab,
    val label: String,
    val icon: ImageVector
)
