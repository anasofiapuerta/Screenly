package com.example.core

import android.content.Context
import android.os.Build
import android.util.Base64
import com.example.domain.model.AlertItem
import com.example.domain.model.AlertThreshold
import com.example.domain.model.AppInfo
import com.example.domain.model.AppLimit
import com.example.domain.model.DailyAppUsage
import com.example.domain.model.PermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ScreenlySyncManager {

    private const val PREFS_NAME = "screenly_persistent_store"
    private const val KEY_LIMITS_JSON = "saved_limits_json"
    private const val KEY_ALERTS_JSON = "saved_alerts_json"
    private const val KEY_LAST_SYNC_URL = "last_vercel_sync_url"

    fun saveLimitsToDisk(context: Context, limits: List<AppLimit>) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val arr = JSONArray()
            for (limit in limits) {
                val obj = JSONObject().apply {
                    put("id", limit.id)
                    put("appId", limit.appId)
                    put("dailyLimitMinutes", limit.dailyLimitMinutes)
                    put("isActive", limit.isActive)
                    put("hasValidityPeriod", limit.hasValidityPeriod)
                    put("startDate", limit.startDate?.toString() ?: "")
                    put("endDate", limit.endDate?.toString() ?: "")
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_LIMITS_JSON, arr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun loadLimitsFromDisk(context: Context): List<AppLimit>? {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_LIMITS_JSON, null) ?: return null
            val arr = JSONArray(raw)
            val list = mutableListOf<AppLimit>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val startStr = obj.optString("startDate", "")
                val endStr = obj.optString("endDate", "")
                list.add(
                    AppLimit(
                        id = obj.getString("id"),
                        appId = obj.getString("appId"),
                        dailyLimitMinutes = obj.getInt("dailyLimitMinutes"),
                        isActive = obj.optBoolean("isActive", true),
                        hasValidityPeriod = obj.optBoolean("hasValidityPeriod", false),
                        startDate = if (startStr.isNotBlank()) LocalDate.parse(startStr) else null,
                        endDate = if (endStr.isNotBlank()) LocalDate.parse(endStr) else null
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun saveAlertsToDisk(context: Context, alerts: List<AlertItem>) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val arr = JSONArray()
            for (alert in alerts) {
                val obj = JSONObject().apply {
                    put("id", alert.id)
                    put("appId", alert.appId)
                    put("appName", alert.appName)
                    put("threshold", alert.threshold.name)
                    put("minutesUsed", alert.minutesUsed)
                    put("limitMinutes", alert.limitMinutes)
                    put("timestamp", alert.timestamp)
                    put("isRead", alert.isRead)
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_ALERTS_JSON, arr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun loadAlertsFromDisk(context: Context): List<AlertItem>? {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_ALERTS_JSON, null) ?: return null
            val arr = JSONArray(raw)
            val list = mutableListOf<AlertItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val thresholdName = obj.optString("threshold", AlertThreshold.REACHED_100.name)
                val threshold = try {
                    AlertThreshold.valueOf(thresholdName)
                } catch (_: Exception) {
                    AlertThreshold.REACHED_100
                }
                list.add(
                    AlertItem(
                        id = obj.getString("id"),
                        appId = obj.getString("appId"),
                        appName = obj.getString("appName"),
                        threshold = threshold,
                        minutesUsed = obj.getInt("minutesUsed"),
                        limitMinutes = obj.getInt("limitMinutes"),
                        timestamp = obj.optString("timestamp", "Hoy"),
                        isRead = obj.optBoolean("isRead", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun getSavedSyncUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_SYNC_URL, "") ?: ""
    }

    fun saveSyncUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_SYNC_URL, url.trim()).apply()
    }

    /**
     * Builds a comprehensive JSON telemetry snapshot of the real Android device state
     * so the Vercel Web Dashboard can display exact real apps, usage, limits, and alerts.
     */
    fun buildSyncPayloadJson(
        apps: List<AppInfo>,
        dailyUsage: List<DailyAppUsage>,
        limits: List<AppLimit>,
        alerts: List<AlertItem>,
        permissionState: PermissionState
    ): String {
        val root = JSONObject()
        val nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        root.put("source", "Android Native (${Build.MANUFACTURER} ${Build.MODEL} • Android ${Build.VERSION.RELEASE})")
        root.put("syncedAt", nowStr)
        root.put("date", LocalDate.now().toString())

        val permsObj = JSONObject().apply {
            put("hasUsageStatsPermission", permissionState.hasUsageStatsPermission)
            put("hasNotificationPermission", permissionState.hasNotificationPermission)
            put("hasCompletedOnboarding", permissionState.hasCompletedOnboarding)
        }
        root.put("permissions", permsObj)

        val usageByApp = dailyUsage.associateBy { it.app.id }
        val appsArr = JSONArray()
        for (app in apps) {
            val u = usageByApp[app.id]
            val appObj = JSONObject().apply {
                put("id", app.id)
                put("packageName", app.packageName)
                put("name", app.name)
                put("category", app.category)
                put("primaryColorHex", String.format("#%06X", (0xFFFFFF and app.primaryColorHex.toInt())))
                put("isRealInstalledApp", app.isRealInstalledApp)
                put("minutesUsedToday", u?.minutesUsed ?: 0)
                put("launchesToday", u?.launches ?: 0)
            }
            appsArr.put(appObj)
        }
        root.put("apps", appsArr)

        val limitsArr = JSONArray()
        for (limit in limits) {
            val used = usageByApp[limit.appId]?.minutesUsed ?: 0
            val status = limit.calculateStatus(used)
            val limObj = JSONObject().apply {
                put("id", limit.id)
                put("appId", limit.appId)
                put("dailyLimitMinutes", limit.dailyLimitMinutes)
                put("isActive", limit.isActive)
                put("hasValidityPeriod", limit.hasValidityPeriod)
                put("startDate", limit.startDate?.toString() ?: JSONObject.NULL)
                put("endDate", limit.endDate?.toString() ?: JSONObject.NULL)
                put("status", status.name)
            }
            limitsArr.put(limObj)
        }
        root.put("limits", limitsArr)

        val alertsArr = JSONArray()
        for (alert in alerts) {
            val alObj = JSONObject().apply {
                put("id", alert.id)
                put("appId", alert.appId)
                put("appName", alert.appName)
                put("threshold", alert.threshold.name)
                put("minutesUsed", alert.minutesUsed)
                put("limitMinutes", alert.limitMinutes)
                put("timestamp", alert.timestamp)
                put("isRead", alert.isRead)
            }
            alertsArr.put(alObj)
        }
        root.put("alerts", alertsArr)

        return root.toString()
    }

    fun buildBase64SyncToken(
        apps: List<AppInfo>,
        dailyUsage: List<DailyAppUsage>,
        limits: List<AppLimit>,
        alerts: List<AlertItem>,
        permissionState: PermissionState
    ): String {
        val json = buildSyncPayloadJson(apps, dailyUsage, limits, alerts, permissionState)
        return Base64.encodeToString(json.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE)
    }

    /**
     * Pushes the real Android device telemetry snapshot directly to a Vercel deployment's `/api/sync` endpoint.
     */
    suspend fun pushSyncToVercelEndpoint(
        targetBaseUrl: String,
        payloadJson: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = targetBaseUrl.trim().removeSuffix("/")
            val endpoint = if (cleanBase.endsWith("/api/sync")) cleanBase else "$cleanBase/api/sync"
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(payloadJson)
                writer.flush()
            }

            val code = conn.responseCode
            if (code in 200..299) {
                Result.success("Sincronizado con éxito con Vercel ($code)")
            } else {
                Result.failure(IllegalStateException("Respuesta HTTP $code desde el servidor Vercel"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
