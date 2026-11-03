package com.example.data.repository

import com.example.domain.model.AlertItem
import com.example.domain.model.AlertThreshold
import com.example.domain.model.AppDetailStats
import com.example.domain.model.AppInfo
import com.example.domain.model.AppLimit
import com.example.domain.model.DailyAppUsage
import com.example.domain.model.DayUsagePoint
import com.example.domain.model.PermissionState
import com.example.domain.model.StatsPeriod
import com.example.domain.repository.ScreenlyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

class ScreenlyDemoRepository : ScreenlyRepository {

    private val initialApps = listOf(
        AppInfo(
            id = "com.instagram.android",
            packageName = "com.instagram.android",
            name = "Instagram",
            category = "Redes sociales",
            primaryColorHex = 0xFFE1306C,
            iconType = "social"
        ),
        AppInfo(
            id = "com.google.android.youtube",
            packageName = "com.google.android.youtube",
            name = "YouTube",
            category = "Entretenimiento",
            primaryColorHex = 0xFFFF0000,
            iconType = "video"
        ),
        AppInfo(
            id = "com.zhiliaoapp.musically",
            packageName = "com.zhiliaoapp.musically",
            name = "TikTok",
            category = "Entretenimiento",
            primaryColorHex = 0xFF00F2FE,
            iconType = "video"
        ),
        AppInfo(
            id = "com.whatsapp",
            packageName = "com.whatsapp",
            name = "WhatsApp",
            category = "Comunicación",
            primaryColorHex = 0xFF25D366,
            iconType = "chat"
        ),
        AppInfo(
            id = "com.android.chrome",
            packageName = "com.android.chrome",
            name = "Google Chrome",
            category = "Navegación",
            primaryColorHex = 0xFF4285F4,
            iconType = "browser"
        ),
        AppInfo(
            id = "com.spotify.music",
            packageName = "com.spotify.music",
            name = "Spotify",
            category = "Música",
            primaryColorHex = 0xFF1DB954,
            iconType = "music"
        ),
        AppInfo(
            id = "com.google.android.gm",
            packageName = "com.google.android.gm",
            name = "Gmail",
            category = "Productividad",
            primaryColorHex = 0xFFEA4335,
            iconType = "work"
        ),
        AppInfo(
            id = "com.Slack",
            packageName = "com.Slack",
            name = "Slack",
            category = "Trabajo",
            primaryColorHex = 0xFF4A154B,
            iconType = "work"
        )
    )

    private val _apps = MutableStateFlow(initialApps)
    override val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val todayDate = LocalDate.now()

    private val initialLimits = listOf(
        AppLimit(
            id = "limit-instagram",
            appId = "com.instagram.android",
            dailyLimitMinutes = 90, // 1h 30m
            isActive = true,
            hasValidityPeriod = true,
            startDate = todayDate.minusDays(5),
            endDate = todayDate.plusDays(10)
        ),
        AppLimit(
            id = "limit-tiktok",
            appId = "com.zhiliaoapp.musically",
            dailyLimitMinutes = 60, // 1h
            isActive = true,
            hasValidityPeriod = false
        ),
        AppLimit(
            id = "limit-youtube",
            appId = "com.google.android.youtube",
            dailyLimitMinutes = 120, // 2h
            isActive = true,
            hasValidityPeriod = true,
            startDate = todayDate.minusDays(2),
            endDate = todayDate.plusDays(5)
        ),
        AppLimit(
            id = "limit-chrome",
            appId = "com.android.chrome",
            dailyLimitMinutes = 45, // 45m
            isActive = false,
            hasValidityPeriod = false
        ),
        AppLimit(
            id = "limit-slack",
            appId = "com.Slack",
            dailyLimitMinutes = 30,
            isActive = true,
            hasValidityPeriod = true,
            startDate = todayDate.plusDays(3), // Programado para el futuro
            endDate = todayDate.plusDays(14)
        )
    )

    private val _limits = MutableStateFlow(initialLimits)
    override val limits: StateFlow<List<AppLimit>> = _limits.asStateFlow()

    // 7-day usage database: appId -> list of minutes for (today, yesterday, -2d, ... -6d)
    private val appHistoricalMinutes = mapOf(
        "com.instagram.android" to listOf(105, 95, 110, 80, 120, 90, 85), // today: 1h 45m (limit 1h30m -> Reached)
        "com.google.android.youtube" to listOf(70, 85, 90, 60, 110, 100, 75), // today: 1h 10m (limit 2h -> 58%)
        "com.zhiliaoapp.musically" to listOf(52, 45, 60, 50, 55, 65, 40), // today: 52m (limit 1h -> 86% Proximity)
        "com.whatsapp" to listOf(48, 55, 40, 60, 45, 50, 35),
        "com.android.chrome" to listOf(32, 25, 40, 30, 35, 20, 28),
        "com.spotify.music" to listOf(25, 40, 30, 20, 35, 30, 15),
        "com.google.android.gm" to listOf(18, 22, 15, 20, 25, 10, 15),
        "com.Slack" to listOf(15, 30, 25, 35, 40, 20, 10)
    )

    private val initialAlerts = listOf(
        AlertItem(
            id = "alert-1",
            appId = "com.instagram.android",
            appName = "Instagram",
            threshold = AlertThreshold.REACHED_100,
            minutesUsed = 105,
            limitMinutes = 90,
            timestamp = "17:42",
            isRead = false
        ),
        AlertItem(
            id = "alert-2",
            appId = "com.zhiliaoapp.musically",
            appName = "TikTok",
            threshold = AlertThreshold.PROXIMITY_80,
            minutesUsed = 52,
            limitMinutes = 60,
            timestamp = "18:15",
            isRead = false
        )
    )

    private val _alerts = MutableStateFlow(initialAlerts)
    override val alerts: StateFlow<List<AlertItem>> = _alerts.asStateFlow()

    private val _permissionState = MutableStateFlow(
        PermissionState(
            hasUsageStatsPermission = true,
            hasNotificationPermission = true,
            hasCompletedOnboarding = true,
            isDemoModeActive = true
        )
    )
    override val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    override fun setInstalledApps(installedApps: List<AppInfo>) {
        if (installedApps.isEmpty()) return
        val current = _apps.value
        val installedPackageNames = installedApps.map { it.packageName }.toSet()
        // Merge real installed apps with any missing demo apps
        val merged = installedApps + current.filter { it.packageName !in installedPackageNames }
        _apps.value = merged.sortedBy { it.name.lowercase() }
    }

    override fun getDailyUsage(dateOffsetDays: Int): StateFlow<List<DailyAppUsage>> {
        val safeOffset = dateOffsetDays.coerceIn(0, 6)
        val currentLimits = _limits.value.associateBy { it.appId }

        val usageList = _apps.value.map { app ->
            val history = appHistoricalMinutes[app.id] ?: listOf(8, 12, 10, 15, 7, 10, 5)
            val minutes = history.getOrElse(safeOffset) { 0 }
            val launches = (minutes / 5).coerceAtLeast(1)
            DailyAppUsage(
                app = app,
                minutesUsed = minutes,
                launches = launches,
                limit = currentLimits[app.id]
            )
        }.sortedByDescending { it.minutesUsed }

        return MutableStateFlow(usageList).asStateFlow()
    }

    override fun getDailyUsageForDate(date: LocalDate): StateFlow<List<DailyAppUsage>> {
        val today = LocalDate.now()
        val daysDiff = ChronoUnit.DAYS.between(date, today).toInt()
        val currentLimits = _limits.value.associateBy { it.appId }

        val usageList = when {
            daysDiff < 0 -> {
                // Fecha futura: 0 minutos
                _apps.value.map { app ->
                    DailyAppUsage(app = app, minutesUsed = 0, launches = 0, limit = currentLimits[app.id])
                }
            }
            daysDiff in 0..6 -> {
                // Últimos 7 días (0 = hoy, 1 = ayer, ...)
                _apps.value.map { app ->
                    val history = appHistoricalMinutes[app.id] ?: listOf(8, 12, 10, 15, 7, 10, 5)
                    val minutes = history.getOrElse(daysDiff) { 0 }
                    val launches = (minutes / 5).coerceAtLeast(if (minutes > 0) 1 else 0)
                    DailyAppUsage(app = app, minutesUsed = minutes, launches = launches, limit = currentLimits[app.id])
                }.sortedByDescending { it.minutesUsed }
            }
            else -> {
                // Cualquier día seleccionado del pasado (> 6 días)
                _apps.value.map { app ->
                    val history = appHistoricalMinutes[app.id] ?: listOf(8, 12, 10, 15, 7, 10, 5)
                    val base = history[Math.floorMod(date.dayOfMonth, history.size)]
                    val variance = Math.floorMod(date.dayOfYear * 7 + app.id.hashCode(), 15) - 7
                    val minutes = (base + variance).coerceAtLeast(4)
                    val launches = (minutes / 5).coerceAtLeast(1)
                    DailyAppUsage(app = app, minutesUsed = minutes, launches = launches, limit = currentLimits[app.id])
                }.sortedByDescending { it.minutesUsed }
            }
        }

        return MutableStateFlow(usageList).asStateFlow()
    }

    override fun getMonthlyUsage(yearMonth: YearMonth): StateFlow<List<DailyAppUsage>> {
        val today = LocalDate.now()
        val currentLimits = _limits.value.associateBy { it.appId }
        val daysInMonth = yearMonth.lengthOfMonth()
        val isCurrentMonth = (yearMonth.year == today.year && yearMonth.month == today.month)
        val daysCount = if (isCurrentMonth) today.dayOfMonth.coerceAtLeast(1) else daysInMonth

        val usageList = _apps.value.map { app ->
            val history = appHistoricalMinutes[app.id] ?: listOf(8, 12, 10, 15, 7, 10, 5)
            val dailyAvg = history.average().toInt().coerceAtLeast(5)
            val monthVariation = Math.floorMod(yearMonth.monthValue * 3 + app.id.hashCode(), 10) - 5
            val effectiveDailyAvg = (dailyAvg + monthVariation).coerceAtLeast(3)
            val totalMinutes = effectiveDailyAvg * daysCount
            val totalLaunches = (totalMinutes / 6).coerceAtLeast(daysCount)

            DailyAppUsage(
                app = app,
                minutesUsed = totalMinutes,
                launches = totalLaunches,
                limit = currentLimits[app.id]
            )
        }.sortedByDescending { it.minutesUsed }

        return MutableStateFlow(usageList).asStateFlow()
    }

    override fun getTopApps(dateOffsetDays: Int, limitCount: Int): List<DailyAppUsage> {

        return getDailyUsage(dateOffsetDays).value.take(limitCount)
    }

    override fun getTotalUsageMinutes(dateOffsetDays: Int): Int {
        return getDailyUsage(dateOffsetDays).value.sumOf { it.minutesUsed }
    }

    override fun getAppStats(appId: String, period: StatsPeriod): AppDetailStats {
        val app = _apps.value.firstOrNull { it.id == appId }
            ?: _apps.value.first()
        val limit = _limits.value.firstOrNull { it.appId == app.id }
        val history = appHistoricalMinutes[app.id] ?: listOf(20, 20, 20, 20, 20, 20, 20)

        val dayNames = listOf("Hoy", "Ayer", "Mié", "Mar", "Lun", "Dom", "Sáb")
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))

        val breakdown = history.mapIndexed { index, mins ->
            val date = today.minusDays(index.toLong())
            val label = if (index < dayNames.size) dayNames[index] else date.format(formatter)
            DayUsagePoint(
                dayLabel = label,
                dateString = date.format(formatter),
                minutesUsed = mins
            )
        }.reversed()

        return when (period) {
            StatsPeriod.TODAY -> {
                val todayMins = history[0]
                val yesterdayMins = history.getOrNull(1)
                AppDetailStats(
                    app = app,
                    period = period,
                    totalMinutes = todayMins,
                    previousPeriodMinutes = yesterdayMins,
                    dailyBreakdown = listOf(breakdown.last()),
                    limit = limit
                )
            }
            StatsPeriod.LAST_7_DAYS -> {
                val sum7Days = history.take(7).sum()
                val prev7Days = (sum7Days * 0.92f).toInt()
                AppDetailStats(
                    app = app,
                    period = period,
                    totalMinutes = sum7Days,
                    previousPeriodMinutes = prev7Days,
                    dailyBreakdown = breakdown,
                    limit = limit
                )
            }
        }
    }

    override fun saveLimit(
        appId: String,
        totalMinutes: Int,
        isActive: Boolean,
        hasValidityPeriod: Boolean,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Result<AppLimit> {
        if (appId.isBlank()) {
            return Result.failure(IllegalArgumentException("La aplicación es obligatoria"))
        }
        if (totalMinutes <= 0) {
            return Result.failure(IllegalArgumentException("El límite debe ser mayor que cero"))
        }
        if (totalMinutes >= 24 * 60) {
            return Result.failure(IllegalArgumentException("El límite no puede superar 24 horas"))
        }
        if (hasValidityPeriod) {
            if (startDate == null || endDate == null) {
                return Result.failure(IllegalArgumentException("Debes definir las fechas de inicio y finalización"))
            }
            if (endDate.isBefore(startDate)) {
                return Result.failure(IllegalArgumentException("La fecha de finalización debe ser posterior o igual a la de inicio"))
            }
        }

        val existing = _limits.value.firstOrNull { it.appId == appId }
        if (existing != null) {
            return Result.failure(IllegalStateException("Esta aplicación ya tiene un límite configurado"))
        }

        val newLimit = AppLimit(
            id = "limit-${UUID.randomUUID()}",
            appId = appId,
            dailyLimitMinutes = totalMinutes,
            isActive = isActive,
            hasValidityPeriod = hasValidityPeriod,
            startDate = startDate,
            endDate = endDate
        )
        _limits.update { it + newLimit }
        checkAndTriggerAlertsForLimit(newLimit)
        return Result.success(newLimit)
    }

    override fun updateLimit(
        limitId: String,
        totalMinutes: Int,
        isActive: Boolean,
        hasValidityPeriod: Boolean,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Result<AppLimit> {
        if (totalMinutes <= 0) {
            return Result.failure(IllegalArgumentException("El límite debe ser mayor que cero"))
        }
        if (totalMinutes >= 24 * 60) {
            return Result.failure(IllegalArgumentException("El límite no puede superar 24 horas"))
        }
        if (hasValidityPeriod) {
            if (startDate == null || endDate == null) {
                return Result.failure(IllegalArgumentException("Debes definir las fechas de inicio y finalización"))
            }
            if (endDate.isBefore(startDate)) {
                return Result.failure(IllegalArgumentException("La fecha de finalización debe ser posterior o igual a la de inicio"))
            }
        }

        var updatedLimit: AppLimit? = null
        _limits.update { list ->
            list.map { item ->
                if (item.id == limitId) {
                    val updated = item.copy(
                        dailyLimitMinutes = totalMinutes,
                        isActive = isActive,
                        hasValidityPeriod = hasValidityPeriod,
                        startDate = startDate,
                        endDate = endDate
                    )
                    updatedLimit = updated
                    updated
                } else item
            }
        }

        return if (updatedLimit != null) {
            checkAndTriggerAlertsForLimit(updatedLimit!!)
            Result.success(updatedLimit!!)
        } else {
            Result.failure(NoSuchElementException("Límite no encontrado"))
        }
    }

    override fun toggleLimitActive(limitId: String, isActive: Boolean) {
        _limits.update { list ->
            list.map { if (it.id == limitId) it.copy(isActive = isActive) else it }
        }
    }

    override fun deleteLimit(limitId: String) {
        _limits.update { list -> list.filterNot { it.id == limitId } }
    }

    override fun getLimitForApp(appId: String): AppLimit? {
        return _limits.value.firstOrNull { it.appId == appId }
    }

    override fun getLimitById(limitId: String): AppLimit? {
        return _limits.value.firstOrNull { it.id == limitId }
    }

    override fun markAlertAsRead(alertId: String) {
        _alerts.update { list ->
            list.map { if (it.id == alertId) it.copy(isRead = true) else it }
        }
    }

    override fun clearAllAlerts() {
        _alerts.update { emptyList() }
    }

    override fun setUsageStatsPermission(granted: Boolean) {
        _permissionState.update { it.copy(hasUsageStatsPermission = granted) }
    }

    override fun setNotificationPermission(granted: Boolean) {
        _permissionState.update { it.copy(hasNotificationPermission = granted) }
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        _permissionState.update { it.copy(hasCompletedOnboarding = completed) }
    }

    override fun syncSystemPermissions(hasUsageStats: Boolean, hasNotifications: Boolean) {
        _permissionState.update {
            it.copy(
                hasUsageStatsPermission = hasUsageStats,
                hasNotificationPermission = hasNotifications
            )
        }
    }

    override fun toggleDemoMode(enabled: Boolean) {
        _permissionState.update { it.copy(isDemoModeActive = enabled) }
    }

    override fun triggerSimulatedLimitReached(appId: String) {
        val app = _apps.value.firstOrNull { it.id == appId } ?: return
        val limit = _limits.value.firstOrNull { it.appId == appId }
        val limitMins = limit?.dailyLimitMinutes ?: 60

        val newAlert = AlertItem(
            id = "alert-${UUID.randomUUID()}",
            appId = appId,
            appName = app.name,
            threshold = AlertThreshold.REACHED_100,
            minutesUsed = limitMins + 5,
            limitMinutes = limitMins,
            timestamp = "Ahora",
            isRead = false
        )
        _alerts.update { listOf(newAlert) + it.filterNot { a -> a.appId == appId && a.threshold == AlertThreshold.REACHED_100 } }
    }

    private fun checkAndTriggerAlertsForLimit(limit: AppLimit) {
        if (!limit.isActive) return
        val app = _apps.value.firstOrNull { it.id == limit.appId } ?: return
        val currentUsage = (appHistoricalMinutes[limit.appId] ?: listOf(0))[0]
        val ratio = currentUsage.toFloat() / limit.dailyLimitMinutes.toFloat()

        if (ratio >= 1.0f) {
            val alert = AlertItem(
                id = "alert-${limit.appId}-100",
                appId = limit.appId,
                appName = app.name,
                threshold = AlertThreshold.REACHED_100,
                minutesUsed = currentUsage,
                limitMinutes = limit.dailyLimitMinutes,
                timestamp = "Hoy",
                isRead = false
            )
            _alerts.update { list ->
                listOf(alert) + list.filterNot { it.appId == limit.appId && it.threshold == AlertThreshold.REACHED_100 }
            }
        } else if (ratio >= 0.8f) {
            val alert = AlertItem(
                id = "alert-${limit.appId}-80",
                appId = limit.appId,
                appName = app.name,
                threshold = AlertThreshold.PROXIMITY_80,
                minutesUsed = currentUsage,
                limitMinutes = limit.dailyLimitMinutes,
                timestamp = "Hoy",
                isRead = false
            )
            _alerts.update { list ->
                listOf(alert) + list.filterNot { it.appId == limit.appId && it.threshold == AlertThreshold.PROXIMITY_80 }
            }
        }
    }
}
