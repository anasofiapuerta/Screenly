package com.example.core

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.max

data class RealAppUsageRecord(
    val packageName: String,
    val minutesUsed: Int,
    val launches: Int
)

object RealUsageStatsManagerHelper {

    /**
     * Queries Android's UsageStatsManager for the exact start and end timestamps of the given [date].
     * Extracts both total foreground time (converted to minutes) and activity launch counts.
     */
    fun queryDailyUsageForDate(context: Context, date: LocalDate): Map<String, RealAppUsageRecord> {
        if (!PermissionHelper.hasUsageStatsPermission(context)) {
            return emptyMap()
        }

        return try {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                    ?: return emptyMap()

            val zoneId = ZoneId.systemDefault()
            val startOfDayMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endOfDayMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                .coerceAtMost(System.currentTimeMillis())

            if (endOfDayMillis <= startOfDayMillis) {
                return emptyMap()
            }

            queryUsageRange(usageStatsManager, startOfDayMillis, endOfDayMillis)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    /**
     * Queries Android's UsageStatsManager for an entire [yearMonth].
     */
    fun queryMonthlyUsage(context: Context, yearMonth: YearMonth): Map<String, RealAppUsageRecord> {
        if (!PermissionHelper.hasUsageStatsPermission(context)) {
            return emptyMap()
        }

        return try {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                    ?: return emptyMap()

            val zoneId = ZoneId.systemDefault()
            val startOfMonthMillis = yearMonth.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endOfMonthMillis = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                .coerceAtMost(System.currentTimeMillis())

            if (endOfMonthMillis <= startOfMonthMillis) {
                return emptyMap()
            }

            queryUsageRange(usageStatsManager, startOfMonthMillis, endOfMonthMillis)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun queryUsageRange(
        usageStatsManager: UsageStatsManager,
        startMillis: Long,
        endMillis: Long
    ): Map<String, RealAppUsageRecord> {
        val result = mutableMapOf<String, RealAppUsageRecord>()

        // 1. Aggregate foreground time from queryAndAggregateUsageStats
        val aggregatedStats = usageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis)
        val foregroundMillisMap = mutableMapOf<String, Long>()

        for ((pkg, stats) in aggregatedStats) {
            val totalTimeMs = stats.totalTimeInForeground
            if (totalTimeMs > 0L) {
                foregroundMillisMap[pkg] = totalTimeMs
            }
        }

        // 2. Count app launches and fine-grained foreground transitions via UsageEvents
        val launchCounts = mutableMapOf<String, Int>()
        val events = usageStatsManager.queryEvents(startMillis, endMillis)
        val event = UsageEvents.Event()

        while (events != null && events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                launchCounts[pkg] = (launchCounts[pkg] ?: 0) + 1
            }
        }

        val allPackages = foregroundMillisMap.keys + launchCounts.keys
        for (pkg in allPackages) {
            val fgMillis = foregroundMillisMap[pkg] ?: 0L
            // Convert ms to minutes; if app was used for at least 15 seconds, count at least 1 minute
            val minutes = when {
                fgMillis >= 60_000L -> (fgMillis / 60_000L).toInt()
                fgMillis >= 15_000L -> 1
                else -> 0
            }
            val rawLaunches = launchCounts[pkg] ?: 0
            val launches = if (minutes > 0) max(1, rawLaunches / 2) else rawLaunches

            if (minutes > 0 || launches > 0) {
                result[pkg] = RealAppUsageRecord(
                    packageName = pkg,
                    minutesUsed = minutes,
                    launches = launches
                )
            }
        }

        return result
    }
}
