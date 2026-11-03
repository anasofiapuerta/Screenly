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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TimeUtils
import com.example.domain.model.AppInfo
import com.example.domain.model.AppLimit
import com.example.domain.model.DailyAppUsage
import com.example.domain.model.LimitStatusType
import com.example.ui.components.AppIconView
import com.example.ui.components.EmptyStateView
import com.example.ui.components.UsageProgressBar
import com.example.ui.theme.ScreenlyBlue
import com.example.ui.theme.ScreenlyDanger
import com.example.ui.theme.ScreenlyFinished
import com.example.ui.theme.ScreenlyProgrammed
import com.example.ui.theme.ScreenlySuccess
import com.example.ui.theme.ScreenlyWarning

@Composable
fun LimitsScreen(
    limits: List<AppLimit>,
    apps: List<AppInfo>,
    dailyUsage: List<DailyAppUsage>,
    onToggleActive: (String, Boolean) -> Unit,
    onEditLimit: (String) -> Unit,
    onDeleteLimit: (AppLimit) -> Unit,
    onCreateLimitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appMap = apps.associateBy { it.id }
    val usageMap = dailyUsage.associateBy { it.app.id }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Límites diarios",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${limits.count { it.isActive }} activos de ${limits.size} configurados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FloatingActionButton(
                    onClick = onCreateLimitClick,
                    containerColor = ScreenlyBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("fab_create_limit")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Crear nuevo límite")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (limits.isEmpty()) {
                EmptyStateView(
                    title = "Aún no has configurado límites de tiempo",
                    message = "Crea un límite de tiempo para tus aplicaciones favoritas y Screenly te avisará al 80% y 100% de uso.",
                    icon = Icons.Default.Timer,
                    actionButtonText = "Crear primer límite",
                    onActionClick = onCreateLimitClick,
                    modifier = Modifier.padding(top = 48.dp),
                    testTag = "empty_limits_screen"
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("limits_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(limits, key = { it.id }) { limit ->
                        val app = appMap[limit.appId]
                        val usage = usageMap[limit.appId]
                        LimitCardItem(
                            limit = limit,
                            app = app,
                            usage = usage,
                            onToggleActive = { active -> onToggleActive(limit.id, active) },
                            onEdit = { onEditLimit(limit.id) },
                            onDelete = { onDeleteLimit(limit) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun LimitCardItem(
    limit: AppLimit,
    app: AppInfo?,
    usage: DailyAppUsage?,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val appName = app?.name ?: limit.appId
    val minutesUsed = usage?.minutesUsed ?: 0
    val limitMins = limit.dailyLimitMinutes
    val ratio = if (limitMins > 0) minutesUsed.toFloat() / limitMins.toFloat() else 0f

    val statusType = limit.calculateStatus(minutesUsed)
    val statusText = when (statusType) {
        LimitStatusType.INACTIVO -> "Inactivo"
        LimitStatusType.PROGRAMADO -> "Programado"
        LimitStatusType.FINALIZADO -> "Finalizado"
        LimitStatusType.ALCANZADO -> "Límite alcanzado"
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
            .testTag("limit_card_${limit.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (limit.isActive) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (limit.isActive) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: App icon, name, status badge, active switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (app != null) {
                    AppIconView(app = app, size = 44.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBadgeColor
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = statusTextColor,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Switch(
                    checked = limit.isActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ScreenlyBlue
                    ),
                    modifier = Modifier.testTag("switch_limit_${limit.id}")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Limit metrics & registered usage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Límite diario",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = TimeUtils.formatMinutesToDuration(limitMins),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Tiempo registrado hoy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = TimeUtils.formatMinutesToDuration(minutesUsed),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (statusType == LimitStatusType.ALCANZADO) ScreenlyDanger else ScreenlyBlue
                        )
                    )
                }
            }

            // Validity period notice if configured
            if (limit.hasValidityPeriod && limit.startDate != null && limit.endDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Vigencia: del ${TimeUtils.toShortDisplayDate(limit.startDate)} al ${TimeUtils.toShortDisplayDate(limit.endDate)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (limit.isActive && statusType != LimitStatusType.PROGRAMADO && statusType != LimitStatusType.FINALIZADO) {
                Spacer(modifier = Modifier.height(10.dp))
                UsageProgressBar(
                    progress = ratio,
                    isNearLimit = statusType == LimitStatusType.PROXIMO,
                    isLimitReached = statusType == LimitStatusType.ALCANZADO,
                    height = 8.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Edit and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_limit_button_${limit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar límite",
                        tint = ScreenlyBlue
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_limit_button_${limit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar límite",
                        tint = ScreenlyDanger
                    )
                }
            }
        }
    }
}
