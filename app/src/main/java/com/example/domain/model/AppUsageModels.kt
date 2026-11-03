package com.example.domain.model

data class DailyAppUsage(
    val app: AppInfo,
    val minutesUsed: Int,
    val launches: Int,
    val limit: AppLimit? = null
) {
    val progressFraction: Float
        get() {
            val limitMins = limit?.dailyLimitMinutes ?: return 0f
            if (limitMins <= 0) return 0f
            return (minutesUsed.toFloat() / limitMins.toFloat()).coerceAtLeast(0f)
        }

    val statusType: LimitStatusType?
        get() = limit?.calculateStatus(minutesUsed)

    val isLimitActive: Boolean
        get() = limit != null && limit.isActive && statusType != LimitStatusType.PROGRAMADO && statusType != LimitStatusType.FINALIZADO

    val isNearLimit: Boolean
        get() = statusType == LimitStatusType.PROXIMO

    val isLimitReached: Boolean
        get() = statusType == LimitStatusType.ALCANZADO

    val limitStatusText: String
        get() = when (statusType) {
            null -> "Sin límite"
            LimitStatusType.INACTIVO -> "Inactivo"
            LimitStatusType.PROGRAMADO -> "Programado"
            LimitStatusType.FINALIZADO -> "Finalizado"
            LimitStatusType.ALCANZADO -> "Alcanzado"
            LimitStatusType.PROXIMO -> "Próximo (80%)"
            LimitStatusType.VIGENTE -> "Vigente"
        }
}

data class DayUsagePoint(
    val dayLabel: String,
    val dateString: String,
    val minutesUsed: Int
)

data class AppDetailStats(
    val app: AppInfo,
    val period: StatsPeriod,
    val totalMinutes: Int,
    val previousPeriodMinutes: Int?,
    val dailyBreakdown: List<DayUsagePoint>,
    val limit: AppLimit?
)

enum class StatsPeriod {
    TODAY,
    LAST_7_DAYS
}

enum class UsageFilterPeriod {
    TODAY,
    YESTERDAY,
    SPECIFIC_DAY,
    MONTH
}

data class PermissionState(
    val hasUsageStatsPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
    val isDemoModeActive: Boolean = true
)

