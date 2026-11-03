package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.components.UsageProgressBar
import com.example.ui.theme.ScreenlyBlue
import com.example.ui.theme.ScreenlyDanger
import com.example.ui.theme.ScreenlyFinished
import com.example.ui.theme.ScreenlyProgrammed
import com.example.ui.theme.ScreenlySuccess
import com.example.ui.theme.ScreenlyWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    app: AppInfo,
    usage: DailyAppUsage?,
    limit: AppLimit?,
    onNavigateBack: () -> Unit,
    onCreateOrEditLimit: (String) -> Unit,
    onViewStatsForApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val minutesUsed = usage?.minutesUsed ?: 0
    val limitMins = limit?.dailyLimitMinutes
    val ratio = if (limitMins != null && limitMins > 0) minutesUsed.toFloat() / limitMins.toFloat() else 0f

    val statusType = limit?.calculateStatus(minutesUsed)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Detalle de aplicación",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("app_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Header
                AppIconView(app = app, size = 68.dp)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = app.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "${app.category} • ${app.packageName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Usage Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Tiempo registrado hoy",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = TimeUtils.formatMinutesToDuration(minutesUsed),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (statusType == LimitStatusType.ALCANZADO) ScreenlyDanger else ScreenlyBlue
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Aperturas registradas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${usage?.launches ?: 0} veces",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Estado de límite",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = usage?.limitStatusText ?: "Sin límite",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when (statusType) {
                                            LimitStatusType.ALCANZADO -> ScreenlyDanger
                                            LimitStatusType.PROXIMO -> ScreenlyWarning
                                            LimitStatusType.PROGRAMADO -> ScreenlyProgrammed
                                            LimitStatusType.FINALIZADO -> ScreenlyFinished
                                            else -> ScreenlySuccess
                                        }
                                    )
                                )
                            }
                        }

                        if (limit != null && limit.isActive && statusType != LimitStatusType.PROGRAMADO && statusType != LimitStatusType.FINALIZADO) {
                            Spacer(modifier = Modifier.height(14.dp))
                            UsageProgressBar(
                                progress = ratio,
                                isNearLimit = statusType == LimitStatusType.PROXIMO,
                                isLimitReached = statusType == LimitStatusType.ALCANZADO,
                                height = 8.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Configured limit details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = ScreenlyBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Límite de tiempo",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            if (limit != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (limit.isActive) ScreenlySuccess.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (limit.isActive) "Activo" else "Inactivo",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (limit.isActive) ScreenlySuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (limit != null) {
                            Text(
                                text = "Límite diario: ${TimeUtils.formatMinutesToDuration(limit.dailyLimitMinutes)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            if (limit.hasValidityPeriod && limit.startDate != null && limit.endDate != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Vigencia: del ${TimeUtils.toShortDisplayDate(limit.startDate)} al ${TimeUtils.toShortDisplayDate(limit.endDate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ScreenlyBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Alertas preventivas al 80% y de límite al 100%.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FilledTonalButton(
                                onClick = { onCreateOrEditLimit(app.id) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_limit_from_detail_button")
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Modificar límite")
                            }
                        } else {
                            Text(
                                text = "Aún no has configurado un límite para esta aplicación.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onCreateOrEditLimit(app.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = ScreenlyBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_limit_from_detail_button")
                            ) {
                                Text("Establecer límite de tiempo")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action to view detailed stats
                OutlinedButton(
                    onClick = { onViewStatsForApp(app.id) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("view_stats_from_detail_button")
                ) {
                    Icon(imageVector = Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver historial y estadísticas detalladas")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
