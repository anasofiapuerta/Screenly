package com.example.domain.model

enum class AlertThreshold {
    PROXIMITY_80, // 80% del límite diario alcanzado
    REACHED_100   // 100% del límite diario alcanzado
}

data class AlertItem(
    val id: String,
    val appId: String,
    val appName: String,
    val threshold: AlertThreshold,
    val minutesUsed: Int,
    val limitMinutes: Int,
    val timestamp: String,
    val isRead: Boolean = false
) {
    val title: String
        get() = when (threshold) {
            AlertThreshold.PROXIMITY_80 -> "Límite próximo a alcanzarse"
            AlertThreshold.REACHED_100 -> "Límite diario alcanzado"
        }

    val description: String
        get() = when (threshold) {
            AlertThreshold.PROXIMITY_80 -> "Has utilizado el 80% del tiempo permitido en $appName."
            AlertThreshold.REACHED_100 -> "Has alcanzado tu límite de uso para $appName."
        }
}
