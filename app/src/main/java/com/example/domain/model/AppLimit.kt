package com.example.domain.model

import java.time.LocalDate

enum class LimitStatusType {
    PROGRAMADO,  // Hoy < startDate
    VIGENTE,     // En vigor y uso normal
    PROXIMO,     // En vigor y uso >= 80%
    ALCANZADO,   // En vigor y uso >= 100%
    INACTIVO,    // Desactivado manualmente
    FINALIZADO   // Hoy > endDate
}

data class AppLimit(
    val id: String,
    val appId: String,
    val dailyLimitMinutes: Int,
    val isActive: Boolean = true,
    val hasValidityPeriod: Boolean = false,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
) {
    val hours: Int get() = dailyLimitMinutes / 60
    val minutes: Int get() = dailyLimitMinutes % 60

    fun calculateStatus(minutesUsedToday: Int = 0, today: LocalDate = LocalDate.now()): LimitStatusType {
        if (!isActive) return LimitStatusType.INACTIVO

        if (hasValidityPeriod) {
            if (startDate != null && today.isBefore(startDate)) {
                return LimitStatusType.PROGRAMADO
            }
            if (endDate != null && today.isAfter(endDate)) {
                return LimitStatusType.FINALIZADO
            }
        }

        val ratio = if (dailyLimitMinutes > 0) minutesUsedToday.toFloat() / dailyLimitMinutes.toFloat() else 0f
        return when {
            ratio >= 1.0f -> LimitStatusType.ALCANZADO
            ratio >= 0.8f -> LimitStatusType.PROXIMO
            else -> LimitStatusType.VIGENTE
        }
    }
}
