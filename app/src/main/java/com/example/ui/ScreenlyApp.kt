package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.ScreenlyPreferences
import com.example.presentation.ScreenlyViewModel
import com.example.ui.components.AlertsBottomSheet
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.LimitReachedDialog
import com.example.ui.components.NavDestination
import com.example.ui.components.ScreenlyBottomBar
import com.example.ui.components.ScreenlyTopBar
import com.example.ui.components.SimulatorBottomSheet
import com.example.ui.screens.AddEditLimitScreen
import com.example.ui.screens.AppDetailScreen
import com.example.ui.screens.DashboardHomeScreen
import com.example.ui.screens.LimitsScreen
import com.example.ui.screens.OnboardingPermissionsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.UsageScreen
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenlyApp(
    viewModel: ScreenlyViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize preferences and permissions check on first render
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    // Re-check permissions when returning from system settings (Lifecycle ON_RESUME)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.syncSystemPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavDestination.Dashboard.route

    val snackbarHostState = remember { SnackbarHostState() }

    // State collections from ViewModel
    val permissionState by viewModel.permissionState.collectAsStateWithLifecycle()
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val limits by viewModel.limits.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val dailyUsage by viewModel.dailyUsage.collectAsStateWithLifecycle()
    val topApps by viewModel.topApps.collectAsStateWithLifecycle()
    val totalMinutesToday by viewModel.totalUsageMinutesToday.collectAsStateWithLifecycle()
    val selectedCalendarDate by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val usageFilterPeriod by viewModel.usageFilterPeriod.collectAsStateWithLifecycle()
    val isCalendarExpanded by viewModel.isCalendarExpanded.collectAsStateWithLifecycle()
    val datesWithData by viewModel.datesWithData.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentDeviceTime.collectAsStateWithLifecycle()
    val currentDate by viewModel.currentDeviceDate.collectAsStateWithLifecycle()


    val selectedStatsAppId by viewModel.selectedStatsAppId.collectAsStateWithLifecycle()
    val selectedStatsPeriod by viewModel.selectedStatsPeriod.collectAsStateWithLifecycle()
    val currentAppStats by viewModel.currentAppStats.collectAsStateWithLifecycle()

    val limitReachedDialogApp by viewModel.limitReachedDialogApp.collectAsStateWithLifecycle()
    val deleteLimitConfirmTarget by viewModel.deleteLimitConfirmTarget.collectAsStateWithLifecycle()
    val showAlertsSheet by viewModel.showAlertsSheet.collectAsStateWithLifecycle()
    val showSimulatorSheet by viewModel.showSimulatorSheet.collectAsStateWithLifecycle()

    val alertsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val simulatorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Listen for UI snackbar messages
    LaunchedEffect(Unit) {
        viewModel.uiMessages.collectLatest { msg ->
            snackbarHostState.showSnackbar(message = msg.message)
        }
    }

    val isTopLevelDestination = currentRoute in listOf(
        NavDestination.Dashboard.route,
        NavDestination.Usage.route,
        NavDestination.Limits.route,
        NavDestination.Stats.route
    )

    val currentTitle = when (currentRoute) {
        NavDestination.Dashboard.route -> "Screenly"
        NavDestination.Usage.route -> "Uso de aplicaciones"
        NavDestination.Limits.route -> "Límites diarios"
        NavDestination.Stats.route -> "Estadísticas"
        "onboarding" -> "Bienvenida y autorizaciones"
        else -> "Screenly"
    }

    val unreadAlertsCount = alerts.count { !it.isRead }
    val prefs = remember { ScreenlyPreferences(context) }
    val startDestination = if (prefs.hasCompletedOnboarding) NavDestination.Dashboard.route else "onboarding"

    Scaffold(
        containerColor = Color.White,
        topBar = {

            if (isTopLevelDestination) {
                ScreenlyTopBar(
                    title = currentTitle,
                    subtitle = if (currentRoute == NavDestination.Dashboard.route) "Bienestar digital" else null,
                    unreadAlertsCount = unreadAlertsCount,
                    onAlertsClick = { viewModel.openAlertsSheet() },
                    onSimulatorClick = { viewModel.openSimulatorSheet() }
                )
            }
        },
        bottomBar = {
            if (isTopLevelDestination) {
                ScreenlyBottomBar(
                    currentRoute = currentRoute,
                    onNavigateToDestination = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(NavDestination.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                // Onboarding & Permissions
                composable("onboarding") {
                    OnboardingPermissionsScreen(
                        permissionState = permissionState,
                        onToggleUsagePermission = { granted -> viewModel.setUsageStatsPermission(granted) },
                        onToggleNotificationPermission = { granted -> viewModel.setNotificationPermission(granted) },
                        onContinue = {
                            viewModel.completeOnboarding(context)
                            navController.navigate(NavDestination.Dashboard.route) {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }

                // Main Dashboard / Home
                composable(NavDestination.Dashboard.route) {
                    DashboardHomeScreen(
                        permissionState = permissionState,
                        totalMinutesToday = totalMinutesToday,
                        topApps = topApps,
                        limits = limits,
                        currentTime = currentTime,
                        currentDate = currentDate,
                        onAppClick = { appId ->
                            navController.navigate("app_detail/$appId")
                        },
                        onCreateLimitClick = {
                            // Enforce rule: if onboarding is not completed, route to onboarding
                            if (!prefs.hasCompletedOnboarding) {
                                navController.navigate("onboarding")
                            } else {
                                navController.navigate("create_limit")
                            }
                        },
                        onViewAllUsageClick = {
                            navController.navigate(NavDestination.Usage.route)
                        },
                        onViewAllLimitsClick = {
                            navController.navigate(NavDestination.Limits.route)
                        },
                        onViewStatsClick = {
                            navController.navigate(NavDestination.Stats.route)
                        },
                        onResolvePermissionsClick = {
                            navController.navigate("onboarding")
                        }
                    )
                }

                // Usage Screen with Collapsible Calendar & Period Filter
                composable(NavDestination.Usage.route) {
                    UsageScreen(
                        permissionState = permissionState,
                        dailyUsage = dailyUsage,
                        selectedDate = selectedCalendarDate,
                        selectedYearMonth = selectedYearMonth,
                        filterPeriod = usageFilterPeriod,
                        isCalendarExpanded = isCalendarExpanded,
                        datesWithData = datesWithData,
                        onDateSelected = { date -> viewModel.selectCalendarDate(date) },
                        onToggleCalendarExpanded = { viewModel.toggleCalendarExpanded() },
                        onSelectToday = { viewModel.selectToday() },
                        onSelectYesterday = { viewModel.selectYesterday() },
                        onSelectMonth = { ym -> viewModel.selectMonth(ym) },
                        onAppClick = { appId ->
                            navController.navigate("app_detail/$appId")
                        },
                        onResolvePermissionsClick = {
                            navController.navigate("onboarding")
                        }
                    )
                }


                // Limits Screen
                composable(NavDestination.Limits.route) {
                    LimitsScreen(
                        limits = limits,
                        apps = apps,
                        dailyUsage = dailyUsage,
                        onToggleActive = { id, active -> viewModel.toggleLimitActive(id, active) },
                        onEditLimit = { limitId -> navController.navigate("edit_limit/$limitId") },
                        onDeleteLimit = { limit -> viewModel.requestDeleteLimit(limit) },
                        onCreateLimitClick = {
                            if (!prefs.hasCompletedOnboarding) {
                                navController.navigate("onboarding")
                            } else {
                                navController.navigate("create_limit")
                            }
                        }
                    )
                }

                // Stats Screen
                composable(NavDestination.Stats.route) {
                    StatsScreen(
                        apps = apps,
                        dailyUsage = dailyUsage,
                        limits = limits,
                        selectedAppId = selectedStatsAppId,
                        selectedPeriod = selectedStatsPeriod,
                        stats = currentAppStats,
                        selectedDate = selectedCalendarDate,
                        selectedYearMonth = selectedYearMonth,
                        filterPeriod = usageFilterPeriod,
                        isCalendarExpanded = isCalendarExpanded,
                        datesWithData = datesWithData,
                        onSelectApp = { appId -> viewModel.selectStatsApp(appId) },
                        onSelectPeriod = { period -> viewModel.selectStatsPeriod(period) },
                        onDateSelected = { date -> viewModel.selectCalendarDate(date) },
                        onToggleCalendarExpanded = { viewModel.toggleCalendarExpanded() },
                        onSelectToday = { viewModel.selectToday() },
                        onSelectYesterday = { viewModel.selectYesterday() },
                        onCreateLimitForApp = { appId ->
                            navController.navigate("create_limit_for/$appId")
                        },
                        onAppClick = { appId ->
                            navController.navigate("app_detail/$appId")
                        }
                    )
                }

                // Create Limit
                composable("create_limit") {
                    AddEditLimitScreen(
                        isEditing = false,
                        existingLimit = null,
                        availableApps = apps,
                        existingLimits = limits,
                        onSaveNewLimit = { appId, hours, minutes, isActive, hasValidity, startD, endD ->
                            viewModel.createLimit(appId, hours, minutes, isActive, hasValidity, startD, endD) {
                                navController.popBackStack()
                            }
                        },
                        onUpdateLimit = { _, _, _, _, _, _, _ -> },
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEditExisting = { existingId ->
                            navController.navigate("edit_limit/$existingId") {
                                popUpTo("create_limit") { inclusive = true }
                            }
                        }
                    )
                }

                // Create Limit with preselected app
                composable(
                    route = "create_limit_for/{appId}",
                    arguments = listOf(navArgument("appId") { type = NavType.StringType })
                ) { entry ->
                    val preselectedId = entry.arguments?.getString("appId")
                    AddEditLimitScreen(
                        isEditing = false,
                        existingLimit = null,
                        availableApps = apps,
                        existingLimits = limits,
                        preselectedAppId = preselectedId,
                        onSaveNewLimit = { appId, hours, minutes, isActive, hasValidity, startD, endD ->
                            viewModel.createLimit(appId, hours, minutes, isActive, hasValidity, startD, endD) {
                                navController.popBackStack()
                            }
                        },
                        onUpdateLimit = { _, _, _, _, _, _, _ -> },
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEditExisting = { existingId ->
                            navController.navigate("edit_limit/$existingId") {
                                popUpTo("create_limit_for/{appId}") { inclusive = true }
                            }
                        }
                    )
                }

                // Edit Limit
                composable(
                    route = "edit_limit/{limitId}",
                    arguments = listOf(navArgument("limitId") { type = NavType.StringType })
                ) { entry ->
                    val limitId = entry.arguments?.getString("limitId") ?: ""
                    val limit = viewModel.getLimitById(limitId)
                    AddEditLimitScreen(
                        isEditing = true,
                        existingLimit = limit,
                        availableApps = apps,
                        existingLimits = limits,
                        onSaveNewLimit = { _, _, _, _, _, _, _ -> },
                        onUpdateLimit = { id, hours, minutes, isActive, hasValidity, startD, endD ->
                            viewModel.updateLimit(id, hours, minutes, isActive, hasValidity, startD, endD) {
                                navController.popBackStack()
                            }
                        },
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEditExisting = {}
                    )
                }

                // App Detail
                composable(
                    route = "app_detail/{appId}",
                    arguments = listOf(navArgument("appId") { type = NavType.StringType })
                ) { entry ->
                    val appId = entry.arguments?.getString("appId") ?: ""
                    val app = viewModel.getAppById(appId)
                    val usage = dailyUsage.firstOrNull { it.app.id == appId }
                    val limit = viewModel.getLimitForApp(appId)

                    if (app != null) {
                        AppDetailScreen(
                            app = app,
                            usage = usage,
                            limit = limit,
                            onNavigateBack = { navController.popBackStack() },
                            onCreateOrEditLimit = { id ->
                                if (limit != null) {
                                    navController.navigate("edit_limit/${limit.id}")
                                } else {
                                    navController.navigate("create_limit_for/$id")
                                }
                            },
                            onViewStatsForApp = { id ->
                                viewModel.selectStatsApp(id)
                                navController.navigate(NavDestination.Stats.route)
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog: Limit reached
    limitReachedDialogApp?.let { usage ->
        LimitReachedDialog(
            usage = usage,
            onDismiss = { viewModel.dismissLimitReachedDialog() }
        )
    }

    // Dialog: Confirm delete limit
    deleteLimitConfirmTarget?.let { limit ->
        val app = viewModel.getAppById(limit.appId)
        ConfirmDeleteDialog(
            limit = limit,
            appInfo = app,
            onConfirm = { viewModel.confirmDeleteLimit() },
            onDismiss = { viewModel.dismissDeleteLimitDialog() }
        )
    }

    // BottomSheet: Alerts Center
    if (showAlertsSheet) {
        AlertsBottomSheet(
            sheetState = alertsSheetState,
            alerts = alerts,
            onDismiss = { viewModel.closeAlertsSheet() },
            onAlertClick = { alert ->
                viewModel.markAlertAsRead(alert.id)
                viewModel.closeAlertsSheet()
                navController.navigate("app_detail/${alert.appId}")
            },
            onClearAll = { viewModel.clearAllAlerts() }
        )
    }

    // BottomSheet: Permission & Scenario Simulator
    if (showSimulatorSheet) {
        SimulatorBottomSheet(
            sheetState = simulatorSheetState,
            permissionState = permissionState,
            onDismiss = { viewModel.closeSimulatorSheet() },
            onToggleUsagePermission = { granted -> viewModel.setUsageStatsPermission(granted) },
            onToggleNotificationPermission = { granted -> viewModel.setNotificationPermission(granted) },
            onTriggerLimit100 = { appId -> viewModel.triggerTestLimitReached(appId) },
            onResetOnboarding = {
                viewModel.resetOnboarding(context)
                navController.navigate("onboarding") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}
