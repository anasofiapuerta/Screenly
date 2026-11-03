package com.example.core

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.example.domain.model.AppInfo
import java.util.concurrent.ConcurrentHashMap

object InstalledAppsLoader {

    val iconCache = ConcurrentHashMap<String, Drawable>()

    fun loadInstalledApps(context: Context): List<AppInfo> {
        return try {
            val pm: PackageManager = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            val myPackage = context.packageName

            val apps = resolveInfos.mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                if (pkgName == myPackage) return@mapNotNull null // Do not allow limiting Screenly itself

                val name = resolveInfo.loadLabel(pm).toString().trim()
                if (name.isBlank()) return@mapNotNull null

                try {
                    val iconDrawable = resolveInfo.loadIcon(pm)
                    if (iconDrawable != null) {
                        iconCache[pkgName] = iconDrawable
                    }
                } catch (_: Exception) {}

                val appCategory = try {
                    val appInfo = pm.getApplicationInfo(pkgName, 0)
                    categorizeApp(appInfo, name, pkgName)
                } catch (_: Exception) {
                    "Aplicaciones"
                }

                AppInfo(
                    id = pkgName,
                    packageName = pkgName,
                    name = name,
                    category = appCategory,
                    primaryColorHex = generateAppColor(name),
                    iconType = "app",
                    isRealInstalledApp = true
                )
            }.distinctBy { it.packageName }
                .sortedBy { it.name.lowercase() }

            apps
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun categorizeApp(appInfo: ApplicationInfo, name: String, pkg: String): String {
        val lowerPkg = pkg.lowercase()
        val lowerName = name.lowercase()
        return when {
            lowerPkg.contains("chrome") || lowerPkg.contains("browser") || lowerPkg.contains("firefox") -> "Navegación"
            lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") || lowerPkg.contains("messages") || lowerPkg.contains("chat") -> "Comunicación"
            lowerPkg.contains("instagram") || lowerPkg.contains("facebook") || lowerPkg.contains("tiktok") || lowerPkg.contains("twitter") || lowerPkg.contains("social") -> "Redes sociales"
            lowerPkg.contains("youtube") || lowerPkg.contains("netflix") || lowerPkg.contains("twitch") || lowerPkg.contains("media") -> "Entretenimiento"
            lowerPkg.contains("spotify") || lowerPkg.contains("music") || lowerPkg.contains("audio") -> "Música"
            lowerPkg.contains("gmail") || lowerPkg.contains("mail") || lowerPkg.contains("calendar") || lowerPkg.contains("slack") || lowerPkg.contains("docs") -> "Productividad"
            lowerPkg.contains("game") || lowerPkg.contains("play") -> "Juegos"
            lowerPkg.contains("camera") || lowerPkg.contains("gallery") || lowerPkg.contains("photos") -> "Fotografía"
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 -> "Sistema"
            else -> "Herramientas"
        }
    }

    private fun generateAppColor(name: String): Long {
        val colors = listOf(
            0xFF2563EB, 0xFF0284C7, 0xFF0D9488, 0xFF7C3AED,
            0xFFEA580C, 0xFFDC2626, 0xFF16A34A, 0xFF4F46E5
        )
        val hash = kotlin.math.abs(name.hashCode())
        return colors[hash % colors.size]
    }
}
