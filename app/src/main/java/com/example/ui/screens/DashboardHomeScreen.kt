package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TimeUtils
import com.example.domain.model.AppLimit
import com.example.domain.model.DailyAppUsage
import com.example.domain.model.LimitStatusType
import com.example.domain.model.PermissionState
import com.example.ui.components.AppIconView
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PendingPermissionBanner
import com.example.ui.components.UsageProgressBar
import com.example.ui.theme.ScreenlyBlue
import com.example.ui.theme.ScreenlyBlueContainer
import com.example.ui.theme.ScreenlyCyan
import com.example.ui.theme.ScreenlyDanger
import com.example.ui.theme.ScreenlyFinished
import com.example.ui.theme.ScreenlyProgrammed
import com.example.ui.theme.ScreenlySuccess
import com.example.ui.theme.ScreenlyWarning

@Composable
fun DashboardHomeScreen(
    permissionState: PermissionState,
    totalMinutesToday: Int,
    topApps: List<DailyAppUsage>,
    limits: List<AppLimit>,
    currentTime: String,
    currentDate: String,
    onAppClick: (String) -> Unit,
    onCreateLimitClick: () -> Unit,
    onViewAllUsageClick: () -> Unit,
    onViewAllLimitsClick: () -> Unit,
    onViewStatsClick: () -> Unit,
    onResolvePermissionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeLimits = limits.filter { it.isActive }
    val reachedLimitsCount = topApps.count { it.isLimitReached }
    val monitoredAppsCount = topApps.size

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Header with live device clock & date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bienestar digital",
                            style = MaterialTheme.typography.titleSmall,
                            color = ScreenlyBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Resumen de hoy",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Live device clock container
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("device_live_clock")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = ScreenlyBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentTime,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = currentDate,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Persistent Permission Banner if needed
            item {
                PendingPermissionBanner(
                    hasUsageStatsPermission = permissionState.hasUsageStatsPermission,
                    hasNotificationPermission = permissionState.hasNotificationPermission,
                    onResolveClick = onResolvePermissionsClick
                )
            }

            // Main Metric Card: Total Screen Time Today
            item {
                TotalUsageCard(
                    totalMinutes = totalMinutesToday,
                    hasPermission = permissionState.hasUsageStatsPermission,
                    onStatsClick = onViewStatsClick
                )
            }

            // Summary Metrics Grid (Monitored Apps, Active Limits, Reached Limits)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMetricChip(
                        title = "Monitoreadas",
                        value = "$monitoredAppsCount apps",
                        icon = Icons.Default.Apps,
                        accentColor = ScreenlyBlue,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricChip(
                        title = "Límites activos",
                        value = "${activeLimits.size} activos",
                        icon = Icons.Default.Timer,
                        accentColor = ScreenlyCyan,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricChip(
                        title = "Alcanzados",
                        value = "$reachedLimitsCount hoy",
                        icon = Icons.Default.HourglassBottom,
                        accentColor = if (reachedLimitsCount > 0) ScreenlyDanger else ScreenlySuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section: Most Used Apps
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aplicaciones más utilizadas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = onViewAllUsageClick,
                        modifier = Modifier.testTag("view_all_usage_button")
                    ) {
                        Text("Ver todas", style = MaterialTheme.typography.labelMedium, color = ScreenlyBlue)
                    }
                }
            }

            if (!permissionState.hasUsageStatsPermission) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    ) {
                        EmptyStateView(
                            title = "Acceso restringido",
                            message = "Debes conceder el permiso de acceso a datos de uso para ver esta información.",
                            icon = Icons.Default.WarningAmber,
                            actionButtonText = "Conceder permiso",
                            onActionClick = onResolvePermissionsClick,
                            testTag = "perm_restricted_top_apps"
                        )
                    }
                }
            } else if (topApps.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    ) {
                        EmptyStateView(
                            title = "No hay datos de uso",
                            message = "No hay información suficiente para mostrar el resumen.",
                            testTag = "empty_top_apps"
                        )
                    }
                }
            } else {
                items(topApps.take(5), key = { it.app.id }) { item ->
                    TopAppCardItem(
                        item = item,
                        totalScreenMinutes = totalMinutesToday,
                        onClick = { onAppClick(item.app.id) }
                    )
                }
            }

            // Section: Configured Limits Summary
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Límites de tiempo",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${activeLimits.size} activos de ${limits.size} configurados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = onViewAllLimitsClick,
                            modifier = Modifier.testTag("view_all_limits_button")
                        ) {
                            Text("Gestionar", style = MaterialTheme.typography.labelMedium, color = ScreenlyBlue)
                        }
                        Button(
                            onClick = onCreateLimitClick,
                            modifier = Modifier.testTag("create_limit_dashboard_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ScreenlyBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nuevo", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // When no limits are configured: clean functional empty state
            if (limits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    ) {
                        EmptyStateView(
                            title = "Aún no has configurado límites de tiempo",
                            message = "Define un límite diario para tus aplicaciones más usadas y recibe alertas al 80% y 100%.",
                            icon = Icons.Default.Timer,
                            actionButtonText = "Crear primer límite",
                            onActionClick = onCreateLimitClick,
                            testTag = "empty_limits_state"
                        )
                    }
                }
            } else {
                items(limits.take(3), key = { it.id }) { limit ->
                    val matchingUsage = topApps.firstOrNull { it.app.id == limit.appId }
                    DashboardLimitCardItem(
                        limit = limit,
                        matchingUsage = matchingUsage,
                        onClick = onViewAllLimitsClick
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SummaryMetricChip(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TotalUsageCard(
    totalMinutes: Int,
    hasPermission: Boolean,
    onStatsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("total_usage_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            ScreenlyBlue.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ScreenlyBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = ScreenlyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tiempo total de uso hoy",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    FilledTonalButton(
                        onClick = onStatsClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ScreenlyBlueContainer.copy(alpha = 0.6f),
                            contentColor = ScreenlyBlue
                        ),
                        modifier = Modifier.testTag("quick_stats_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Estadísticas", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (hasPermission) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = TimeUtils.formatMinutesToDuration(totalMinutes),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp
                            ),
                            color = ScreenlyBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "en pantalla activa",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val targetDailyMins = 240
                    val fraction = (totalMinutes.toFloat() / targetDailyMins.toFloat()).coerceIn(0f, 1f)
                    UsageProgressBar(
                        progress = fraction,
                        isNearLimit = fraction >= 0.8f && fraction < 1.0f,
                        isLimitReached = fraction >= 1.0f,
                        height = 10.dp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Objetivo recomendado: menos de 4h de pantalla diaria",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "-- h -- min",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Requiere permiso de acceso a datos de uso.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ScreenlyWarning
                    )
                }
            }
        }
    }
}

@Composable
private fun TopAppCardItem(
    item: DailyAppUsage,
    totalScreenMinutes: Int,
    onClick: () -> Unit
) {
    val fraction = if (totalScreenMinutes > 0) item.minutesUsed.toFloat() / totalScreenMinutes.toFloat() else 0f
    val percentText = "${(fraction * 100).toInt()}% del uso total"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("top_app_item_${item.app.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconView(app = item.app, size = 42.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.app.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${item.app.category} • ${item.launches} aperturas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = TimeUtils.formatMinutesToDuration(item.minutesUsed),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.isLimitReached) ScreenlyDanger else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = percentText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            UsageProgressBar(
                progress = fraction,
                isNearLimit = item.isNearLimit,
                isLimitReached = item.isLimitReached,
                height = 6.dp
            )
        }
    }
}

@Composable
private fun DashboardLimitCardItem(
    limit: AppLimit,
    matchingUsage: DailyAppUsage?,
    onClick: () -> Unit
) {
    val app = matchingUsage?.app
    val usedMins = matchingUsage?.minutesUsed ?: 0
    val limitMins = limit.dailyLimitMinutes
    val ratio = if (limitMins > 0) usedMins.toFloat() / limitMins.toFloat() else 0f

    val statusType = limit.calculateStatus(usedMins)
    val statusText = when (statusType) {
        LimitStatusType.INACTIVO -> "Inactivo"
        LimitStatusType.PROGRAMADO -> "Programado"
        LimitStatusType.FINALIZADO -> "Finalizado"
        LimitStatusType.ALCANZADO -> "Alcanzado"
        LimitStatusType.PROXIMO -> "Próximo (80%)"
        LimitStatusType.VIGENTE -> "Vigente"
    }

    val statusBadgeColor = when (statusType) {
        LimitStatusType.INACTIVO -> MaterialTheme.colorScheme.surfaceVariant
        LimitStatusType.PROGRAMADO -> ScreenlyProgrammed.copy(alpha = 0.15f)
        LimitStatusType.FINALIZADO -> ScreenlyFinished.copy(alpha = 0.15f)
        LimitStatusType.ALCANZADO -> ScreenlyDanger.copy(alpha = 0.15f)
        LimitStatusType.PROXIMO -> ScreenlyWarning.copy(alpha = 0.15f)
        LimitStatusType.VIGENTE -> ScreenlySuccess.copy(alpha = 0.15f)
    }

    val statusTextColor = when (statusType) {
        LimitStatusType.INACTIVO -> MaterialTheme.colorScheme.onSurfaceVariant
        LimitStatusType.PROGRAMADO -> ScreenlyProgrammed
        LimitStatusType.FINALIZADO -> ScreenlyFinished
        LimitStatusType.ALCANZADO -> ScreenlyDanger
        LimitStatusType.PROXIMO -> ScreenlyWarning
        LimitStatusType.VIGENTE -> ScreenlySuccess
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("dashboard_limit_card_${limit.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (app != null) {
                    AppIconView(app = app, size = 38.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app?.name ?: limit.appId,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Límite: ${TimeUtils.formatMinutesToDuration(limitMins)} • Usado: ${TimeUtils.formatMinutesToDuration(usedMins)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBadgeColor
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusTextColor,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (limit.isActive && statusType != LimitStatusType.PROGRAMADO && statusType != LimitStatusType.FINALIZADO) {
                Spacer(modifier = Modifier.height(10.dp))
                UsageProgressBar(
                    progress = ratio,
                    isNearLimit = statusType == LimitStatusType.PROXIMO,
                    isLimitReached = statusType == LimitStatusType.ALCANZADO,
                    height = 6.dp
                )
            }
        }
    }
}
