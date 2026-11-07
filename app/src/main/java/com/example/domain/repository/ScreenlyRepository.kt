package com.example.domain.repository

import com.example.domain.model.AlertItem
import com.example.domain.model.AppDetailStats
import com.example.domain.model.AppInfo
import com.example.domain.model.AppLimit
import com.example.domain.model.DailyAppUsage
import com.example.domain.model.PermissionState
import com.example.domain.model.StatsPeriod
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.YearMonth

interface ScreenlyRepository {
    val permissionState: StateFlow<PermissionState>
    val apps: StateFlow<List<AppInfo>>
    val limits: StateFlow<List<AppLimit>>
    val alerts: StateFlow<List<AlertItem>>

    fun getDailyUsage(dateOffsetDays: Int = 0): StateFlow<List<DailyAppUsage>>

    fun getDailyUsageForDate(date: LocalDate): StateFlow<List<DailyAppUsage>>

    fun getMonthlyUsage(yearMonth: YearMonth): StateFlow<List<DailyAppUsage>>

    fun getTopApps(dateOffsetDays: Int = 0, limitCount: Int = 5): List<DailyAppUsage>


    fun getTotalUsageMinutes(dateOffsetDays: Int = 0): Int

    fun getAppStats(appId: String, period: StatsPeriod): AppDetailStats

    fun saveLimit(
        appId: String,
        totalMinutes: Int,
        isActive: Boolean,
        hasValidityPeriod: Boolean = false,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): Result<AppLimit>

    fun updateLimit(
        limitId: String,
        totalMinutes: Int,
        isActive: Boolean,
        hasValidityPeriod: Boolean = false,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): Result<AppLimit>

    fun toggleLimitActive(limitId: String, isActive: Boolean)

    fun deleteLimit(limitId: String)

    fun getLimitForApp(appId: String): AppLimit?

    fun getLimitById(limitId: String): AppLimit?

    fun markAlertAsRead(alertId: String)

    fun clearAllAlerts()

    fun setUsageStatsPermission(granted: Boolean)

    fun setNotificationPermission(granted: Boolean)

    fun setOnboardingCompleted(completed: Boolean)

    fun syncSystemPermissions(hasUsageStats: Boolean, hasNotifications: Boolean)

    fun setInstalledApps(installedApps: List<AppInfo>)

    fun updateRealUsageStats(date: LocalDate, usageMap: Map<String, com.example.core.RealAppUsageRecord>)

    fun restorePersistedData(savedLimits: List<AppLimit>?, savedAlerts: List<AlertItem>?)

    fun toggleDemoMode(enabled: Boolean)

    fun triggerSimulatedLimitReached(appId: String)
}
