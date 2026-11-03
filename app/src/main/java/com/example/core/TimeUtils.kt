package com.example.core

import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeUtils {

    private val spanishLocale = Locale("es", "ES")

    fun formatMinutesToDuration(totalMinutes: Int): String {
        if (totalMinutes <= 0) return "0 min"
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
            hours > 0 -> "${hours}h"
            else -> "${minutes}min"
        }
    }

    fun formatMinutesToHourMinParts(totalMinutes: Int): Pair<Int, Int> {
        val safe = totalMinutes.coerceAtLeast(0)
        return Pair(safe / 60, safe % 60)
    }

    fun totalMinutesFromHoursAndMinutes(hours: Int, minutes: Int): Int {
        return (hours * 60) + minutes
    }

    fun getTodayDateFormatted(): String {
        return try {
            val now = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", spanishLocale)
            val formatted = now.format(formatter)
            formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(spanishLocale) else it.toString() }
        } catch (_: Exception) {
            "Hoy"
        }
    }

    fun getDeviceDateFormatted(date: LocalDate = LocalDate.now()): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", spanishLocale)
            date.format(formatter)
        } catch (_: Exception) {
            date.toString()
        }
    }

    fun getDeviceTimeFormatted(time: LocalTime = LocalTime.now()): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("h:mm a", spanishLocale)
            val formatted = time.format(formatter)
            formatted.lowercase(spanishLocale)
                .replace("am", "a. m.")
                .replace("pm", "p. m.")
        } catch (_: Exception) {
            ""
        }
    }

    fun getFormattedDate(date: LocalDate): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM", spanishLocale)
            date.format(formatter)
        } catch (_: Exception) {
            date.toString()
        }
    }

    fun getMonthYearFormatted(yearMonth: YearMonth): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", spanishLocale)
            val formatted = yearMonth.format(formatter)
            formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(spanishLocale) else it.toString() }
        } catch (_: Exception) {
            yearMonth.toString()
        }
    }

    fun toShortDisplayDate(date: LocalDate): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", spanishLocale)
            date.format(formatter)
        } catch (_: Exception) {
            date.toString()
        }
    }
}
