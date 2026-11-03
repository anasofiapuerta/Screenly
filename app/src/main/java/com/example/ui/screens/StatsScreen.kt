package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TimeUtils
import com.example.domain.model.AppDetailStats
import com.example.domain.model.AppInfo
import com.example.domain.model.AppLimit
import com.example.domain.model.DailyAppUsage
import com.example.domain.model.DayUsagePoint
import com.example.domain.model.StatsPeriod
import com.example.domain.model.UsageFilterPeriod
import com.example.ui.components.AppIconView
import com.example.ui.components.EmptyStateView
import com.example.ui.components.InteractiveCalendarView
import com.example.ui.components.UsageProgressBar
import com.example.ui.theme.ScreenlyBlue
import com.example.ui.theme.ScreenlyBorderLight
import com.example.ui.theme.ScreenlyCyan
import com.example.ui.theme.ScreenlyDanger
import com.example.ui.theme.ScreenlySuccess
import com.example.ui.theme.ScreenlyWarning
import java.time.LocalDate
import java.time.YearMonth

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun StatsScreen(
    apps: List<AppInfo>,
    dailyUsage: List<DailyAppUsage>,
    limits: List<AppLimit>,
    selectedAppId: String,
    selectedPeriod: StatsPeriod,
    stats: AppDetailStats?,
    selectedDate: LocalDate,
    selectedYearMonth: YearMonth,
    filterPeriod: UsageFilterPeriod,
    isCalendarExpanded: Boolean,
    datesWithData: Set<LocalDate>,
    onSelectApp: (String) -> Unit,
    onSelectPeriod: (StatsPeriod) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onToggleCalendarExpanded: () -> Unit,
    onSelectToday: () -> Unit,
    onSelectYesterday: () -> Unit,
    onCreateLimitForApp: (String) -> Unit,
    onAppClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val totalMinutes = dailyUsage.sumOf { it.minutesUsed }
    val topApps = dailyUsage.sortedByDescending { it.minutesUsed }
    val monitoredAppsCount = dailyUsage.size
    val activeLimitsCount = limits.count { it.isActive }
    val reachedLimitsCount = dailyUsage.count { it.isLimitReached }
    val mostUsedApp = topApps.firstOrNull()

    val dateHeaderLabel = when (filterPeriod) {
        UsageFilterPeriod.TODAY -> "Hoy • ${TimeUtils.getFormattedDate(selectedDate)}"
        UsageFilterPeriod.YESTERDAY -> "Ayer • ${TimeUtils.getFormattedDate(selectedDate)}"
        UsageFilterPeriod.SPECIFIC_DAY -> TimeUtils.getFormattedDate(selectedDate)
        UsageFilterPeriod.MONTH -> "Mes de ${TimeUtils.getMonthYearFormatted(selectedYearMonth)}"
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Estadísticas de bienestar",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Monitoreo integral del tiempo en pantalla, cumplimiento de límites y tendencias.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Dropdown Calendar Header Card with filter chips
            StatsCalendarHeaderCard(
                dateHeaderLabel = dateHeaderLabel,
                isCalendarExpanded = isCalendarExpanded,
                filterPeriod = filterPeriod,
                selectedDate = selectedDate,
                selectedYearMonth = selectedYearMonth,
                datesWithData = datesWithData,
                onToggleCalendarExpanded = onToggleCalendarExpanded,
                onSelectToday = onSelectToday,
                onSelectYesterday = onSelectYesterday,
                onDateSelected = onDateSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Metric Card: Total Usage Time
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_total_usage_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ScreenlyBorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                    .background(ScreenlyBlue.copy(alpha = 0.12f)),
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
                                text = "Tiempo total de uso",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0F172A)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ScreenlyBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = when (filterPeriod) {
                                    UsageFilterPeriod.TODAY -> "Hoy"
                                    UsageFilterPeriod.YESTERDAY -> "Ayer"
                                    UsageFilterPeriod.SPECIFIC_DAY -> "Fecha elegida"
                                    UsageFilterPeriod.MONTH -> "Mes completo"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ScreenlyBlue,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = TimeUtils.formatMinutesToDuration(totalMinutes),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ScreenlyBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val targetDailyMins = 240
                    val fraction = (totalMinutes.toFloat() / targetDailyMins.toFloat()).coerceIn(0f, 1f)
                    UsageProgressBar(
                        progress = fraction,
                        isNearLimit = fraction >= 0.8f && fraction < 1.0f,
                        isLimitReached = fraction >= 1.0f,
                        height = 8.dp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Meta saludable diaria: 4 horas máximas recomendadas.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4-item Metrics Grid (Monitored apps, Active limits, Reached limits, Top app)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatsMetricCard(
                    title = "Monitoreadas",
                    value = "$monitoredAppsCount apps",
                    icon = Icons.Default.Apps,
                    accentColor = ScreenlyBlue,
                    modifier = Modifier.weight(1f)
                )
                StatsMetricCard(
                    title = "Límites activos",
                    value = "$activeLimitsCount activos",
                    icon = Icons.Default.Timer,
                    accentColor = ScreenlyCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatsMetricCard(
                    title = "Límites alcanzados",
                    value = "$reachedLimitsCount en el período",
                    icon = Icons.Default.HourglassBottom,
                    accentColor = if (reachedLimitsCount > 0) ScreenlyDanger else ScreenlySuccess,
                    modifier = Modifier.weight(1f)
                )
                StatsMetricCard(
                    title = "App más utilizada",
                    value = mostUsedApp?.app?.name ?: "Ninguna",
                    subtitle = if (mostUsedApp != null) TimeUtils.formatMinutesToDuration(mostUsedApp.minutesUsed) else null,
                    icon = Icons.Default.TrendingUp,
                    accentColor = ScreenlyBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Top Used Apps Ranking Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_top_ranking_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ScreenlyBorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aplicaciones más utilizadas",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${topApps.take(5).size} principales",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (topApps.isEmpty()) {
                        Text(
                            text = "No hay registros de uso para esta fecha.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    } else {
                        topApps.take(5).forEachIndexed { index, item ->
                            val itemFraction = if (totalMinutes > 0) item.minutesUsed.toFloat() / totalMinutes.toFloat() else 0f
                            val percentText = "${(itemFraction * 100).toInt()}%"

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAppClick(item.app.id) }
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ScreenlyBlue.copy(alpha = 0.1f),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = ScreenlyBlue
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    AppIconView(app = item.app, size = 32.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.app.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "${item.app.category} • ${item.launches} aperturas",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = TimeUtils.formatMinutesToDuration(item.minutesUsed),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isLimitReached) ScreenlyDanger else ScreenlyBlue
                                            )
                                        )
                                        Text(
                                            text = percentText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                UsageProgressBar(
                                    progress = itemFraction,
                                    isNearLimit = item.isNearLimit,
                                    isLimitReached = item.isLimitReached,
                                    height = 4.dp
                                )
                            }
                            if (index < topApps.take(5).size - 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Detailed per-app section
            Text(
                text = "Análisis individual por aplicación",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Selecciona una aplicación para ver su tendencia semanal y control de límites.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal app chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stats_app_selector_row")
            ) {
                items(apps, key = { it.id }) { app ->
                    val isSelected = app.id == selectedAppId
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) ScreenlyBlue.copy(alpha = 0.12f) else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) ScreenlyBlue else ScreenlyBorderLight
                        ),
                        modifier = Modifier
                            .clickable { onSelectApp(app.id) }
                            .testTag("stats_app_chip_${app.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIconView(app = app, size = 26.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ScreenlyBlue else Color(0xFF0F172A)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Period Selector Tab: Hoy vs Últimos 7 días
            PrimaryTabRow(
                selectedTabIndex = if (selectedPeriod == StatsPeriod.TODAY) 0 else 1,
                containerColor = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("stats_period_tab_row")
            ) {
                Tab(
                    selected = selectedPeriod == StatsPeriod.TODAY,
                    onClick = { onSelectPeriod(StatsPeriod.TODAY) },
                    text = { Text("Hoy", style = MaterialTheme.typography.titleSmall) },
                    modifier = Modifier.testTag("tab_period_today")
                )
                Tab(
                    selected = selectedPeriod == StatsPeriod.LAST_7_DAYS,
                    onClick = { onSelectPeriod(StatsPeriod.LAST_7_DAYS) },
                    text = { Text("Últimos 7 días", style = MaterialTheme.typography.titleSmall) },
                    modifier = Modifier.testTag("tab_period_7_days")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (stats == null) {
                EmptyStateView(
                    title = "No hay datos disponibles",
                    message = "Selecciona una aplicación para ver sus estadísticas de uso.",
                    icon = Icons.Default.BarChart
                )
            } else {
                // App Detail Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stats_main_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, ScreenlyBorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIconView(app = stats.app, size = 44.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stats.app.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Categoría: ${stats.app.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Tiempo total registrado (${if (stats.period == StatsPeriod.TODAY) "Hoy" else "Últimos 7 días"})",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = TimeUtils.formatMinutesToDuration(stats.totalMinutes),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ScreenlyBlue
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Limit comparison
                        if (stats.limit != null) {
                            val limitMins = if (stats.period == StatsPeriod.TODAY)
                                stats.limit.dailyLimitMinutes
                            else
                                stats.limit.dailyLimitMinutes * 7

                            val ratio = if (limitMins > 0) stats.totalMinutes.toFloat() / limitMins.toFloat() else 0f
                            val isOver = ratio >= 1.0f

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, ScreenlyBorderLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (stats.period == StatsPeriod.TODAY) "Límite diario configurado" else "Límite equivalente semanal",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = TimeUtils.formatMinutesToDuration(limitMins),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    UsageProgressBar(
                                        progress = ratio,
                                        isNearLimit = ratio >= 0.8f && ratio < 1.0f,
                                        isLimitReached = isOver,
                                        height = 8.dp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isOver) "Superaste el límite configurado por ${TimeUtils.formatMinutesToDuration(stats.totalMinutes - limitMins)}"
                                        else "Consumiste el ${(ratio * 100).toInt()}% de tu límite establecido.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = if (isOver) ScreenlyDanger else ScreenlySuccess
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, ScreenlyBorderLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = ScreenlyBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Esta aplicación no tiene un límite configurado.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Text(
                                        text = "Crear límite",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ScreenlyBlue,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                            .clickable { onCreateLimitForApp(stats.app.id) }
                                            .padding(4.dp)
                                            .testTag("stats_create_limit_shortcut")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Comparison against previous period
                        val prev = stats.previousPeriodMinutes
                        if (prev != null) {
                            val diff = stats.totalMinutes - prev
                            val diffPercent = if (prev > 0) (kotlin.math.abs(diff).toFloat() / prev.toFloat() * 100).toInt() else 0

                            val isIncrease = diff > 0
                            val comparisonIcon = if (isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                            val comparisonColor = if (isIncrease) ScreenlyDanger else ScreenlySuccess

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = comparisonColor.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(comparisonColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = comparisonIcon,
                                            contentDescription = null,
                                            tint = comparisonColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (stats.period == StatsPeriod.TODAY)
                                                "${if (isIncrease) "+" else "-"}${TimeUtils.formatMinutesToDuration(kotlin.math.abs(diff))} ($diffPercent%) comparado con ayer"
                                            else
                                                "${if (isIncrease) "+" else "-"}${TimeUtils.formatMinutesToDuration(kotlin.math.abs(diff))} ($diffPercent%) comparado con los 7 días anteriores",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = comparisonColor
                                        )
                                        Text(
                                            text = "Período anterior: ${TimeUtils.formatMinutesToDuration(prev)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No hay datos del período anterior para comparar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Daily breakdown chart for last 7 days
                if (stats.period == StatsPeriod.LAST_7_DAYS && stats.dailyBreakdown.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, ScreenlyBorderLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Historial diario (últimos 7 días)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            UsageBarChart(
                                breakdown = stats.dailyBreakdown,
                                dailyLimitMinutes = stats.limit?.dailyLimitMinutes,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun StatsMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ScreenlyBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0F172A)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                    color = accentColor
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun StatsCalendarHeaderCard(
    dateHeaderLabel: String,
    isCalendarExpanded: Boolean,
    filterPeriod: UsageFilterPeriod,
    selectedDate: LocalDate,
    selectedYearMonth: YearMonth,
    datesWithData: Set<LocalDate>,
    onToggleCalendarExpanded: () -> Unit,
    onSelectToday: () -> Unit,
    onSelectYesterday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stats_calendar_header_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ScreenlyBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header row with toggle dropdown button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleCalendarExpanded)
                    .testTag("stats_toggle_calendar_button"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ScreenlyBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = ScreenlyBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Fecha de consulta",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = dateHeaderLabel,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isCalendarExpanded) "Ocultar" else "Ver calendario",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ScreenlyBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isCalendarExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = ScreenlyBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Filter Chips: Hoy, Ayer, Calendario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterPeriod == UsageFilterPeriod.TODAY,
                    onClick = onSelectToday,
                    label = { Text("Hoy", style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ScreenlyBlue,
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFF8FAFC),
                        labelColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("stats_filter_chip_today")
                )

                FilterChip(
                    selected = filterPeriod == UsageFilterPeriod.YESTERDAY,
                    onClick = onSelectYesterday,
                    label = { Text("Ayer", style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ScreenlyBlue,
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFF8FAFC),
                        labelColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("stats_filter_chip_yesterday")
                )

                FilterChip(
                    selected = isCalendarExpanded,
                    onClick = onToggleCalendarExpanded,
                    label = { Text(if (isCalendarExpanded) "Cerrar calendario" else "Elegir fecha...", style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ScreenlyBlue.copy(alpha = 0.15f),
                        selectedLabelColor = ScreenlyBlue,
                        containerColor = Color(0xFFF8FAFC),
                        labelColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("stats_filter_chip_calendar")
                )
            }

            // Collapsible interactive calendar
            AnimatedVisibility(
                visible = isCalendarExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    InteractiveCalendarView(
                        selectedDate = selectedDate,
                        selectedYearMonth = selectedYearMonth,
                        filterPeriod = filterPeriod,
                        datesWithData = datesWithData,
                        onDateSelected = { date ->
                            onDateSelected(date)
                        },
                        onSelectToday = onSelectToday,
                        onSelectYesterday = onSelectYesterday,
                        onSelectMonth = {},
                        onCloseCalendar = onToggleCalendarExpanded
                    )
                }
            }
        }
    }
}

@Composable
fun UsageBarChart(
    breakdown: List<DayUsagePoint>,
    dailyLimitMinutes: Int?,
    modifier: Modifier = Modifier
) {
    val maxMins = (breakdown.maxOfOrNull { it.minutesUsed } ?: 60).coerceAtLeast(dailyLimitMinutes ?: 60).coerceAtLeast(30)
    val barColor = ScreenlyBlue
    val warningColor = ScreenlyDanger
    val trackBgColor = Color(0xFFF1F5F9)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val totalWidth = size.width
            val totalHeight = size.height
            val barCount = breakdown.size
            if (barCount == 0) return@Canvas

            val slotWidth = totalWidth / barCount
            val barWidth = (slotWidth * 0.55f).coerceAtMost(36.dp.toPx())

            breakdown.forEachIndexed { index, point ->
                val centerX = (index * slotWidth) + (slotWidth / 2f)
                val left = centerX - (barWidth / 2f)
                val fraction = (point.minutesUsed.toFloat() / maxMins.toFloat()).coerceIn(0f, 1f)
                val barHeight = (totalHeight * fraction).coerceAtLeast(4.dp.toPx())
                val top = totalHeight - barHeight

                drawRoundRect(
                    color = trackBgColor,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, totalHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                val isExceeded = dailyLimitMinutes != null && point.minutesUsed >= dailyLimitMinutes
                val color = if (isExceeded) warningColor else barColor

                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            breakdown.forEach { point ->
                Text(
                    text = point.dayLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
