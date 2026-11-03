package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TimeUtils
import com.example.domain.model.UsageFilterPeriod
import com.example.ui.theme.ScreenlyBlue
import com.example.ui.theme.ScreenlyBlueContainer
import com.example.ui.theme.ScreenlyBlueDark
import com.example.ui.theme.ScreenlyBorderLight
import com.example.ui.theme.ScreenlyCyan
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun InteractiveCalendarView(
    selectedDate: LocalDate,
    selectedYearMonth: YearMonth,
    filterPeriod: UsageFilterPeriod,
    datesWithData: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    onSelectToday: () -> Unit,
    onSelectYesterday: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onCloseCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember(selectedYearMonth, selectedDate) {
        mutableStateOf(if (filterPeriod == UsageFilterPeriod.MONTH) selectedYearMonth else YearMonth.from(selectedDate))
    }

    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val isMonthModeActive = filterPeriod == UsageFilterPeriod.MONTH && currentMonth == selectedYearMonth

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_calendar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, ScreenlyBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Month Navigation + Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { currentMonth = currentMonth.minusMonths(1) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .testTag("calendar_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Mes anterior",
                            tint = ScreenlyBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = TimeUtils.getMonthYearFormatted(currentMonth),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { currentMonth = currentMonth.plusMonths(1) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .testTag("calendar_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Mes siguiente",
                            tint = ScreenlyBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Month selection button: "Ver todo el mes"
                Button(
                    onClick = { onSelectMonth(currentMonth) },
                    shape = RoundedCornerShape(10.dp),
                    colors = if (isMonthModeActive) {
                        ButtonDefaults.buttonColors(containerColor = ScreenlyBlue, contentColor = Color.White)
                    } else {
                        ButtonDefaults.buttonColors(containerColor = ScreenlyBlueContainer, contentColor = ScreenlyBlueDark)
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("select_full_month_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isMonthModeActive) "Mes activo" else "Ver todo el mes",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Shortcut Row: Hoy, Ayer, Ocultar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filterPeriod == UsageFilterPeriod.TODAY,
                        onClick = {
                            onSelectToday()
                            currentMonth = YearMonth.from(today)
                        },
                        label = { Text("Hoy", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)) },
                        leadingIcon = if (filterPeriod == UsageFilterPeriod.TODAY) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ScreenlyBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF8FAFC),
                            labelColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.testTag("calendar_shortcut_today")
                    )

                    FilterChip(
                        selected = filterPeriod == UsageFilterPeriod.YESTERDAY,
                        onClick = {
                            onSelectYesterday()
                            currentMonth = YearMonth.from(yesterday)
                        },
                        label = { Text("Ayer", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)) },
                        leadingIcon = if (filterPeriod == UsageFilterPeriod.YESTERDAY) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ScreenlyBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF8FAFC),
                            labelColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.testTag("calendar_shortcut_yesterday")
                    )
                }

                TextButton(
                    onClick = onCloseCalendar,
                    modifier = Modifier.testTag("calendar_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = ScreenlyBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Ocultar",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ScreenlyBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day of week labels: L M X J V S D
            val dayHeaders = listOf("L", "M", "X", "J", "V", "S", "D")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dayHeaders.forEach { header ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = header,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Month Grid
            val firstDayOfMonth = currentMonth.atDay(1)
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Monday) to 7 (Sunday)
            val daysInMonth = currentMonth.lengthOfMonth()

            val totalCells = ((firstDayOfWeek - 1) + daysInMonth)
            val totalRows = (totalCells + 6) / 7

            for (row in 0 until totalRows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (col in 0..6) {
                        val dayIndex = (row * 7) + col - (firstDayOfWeek - 1) + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayIndex in 1..daysInMonth) {
                                val cellDate = currentMonth.atDay(dayIndex)
                                val isSelected = filterPeriod != UsageFilterPeriod.MONTH && cellDate == selectedDate
                                val isCellToday = cellDate == today
                                val hasData = datesWithData.contains(cellDate)

                                CalendarDayCell(
                                    date = cellDate,
                                    isSelected = isSelected,
                                    isToday = isCellToday,
                                    hasData = hasData,
                                    isMonthMode = isMonthModeActive,
                                    onClick = { onDateSelected(cellDate) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Information bar describing active selection
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, ScreenlyBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusLabel = when (filterPeriod) {
                        UsageFilterPeriod.TODAY -> "Consultando hoy: ${TimeUtils.getFormattedDate(today)}"
                        UsageFilterPeriod.YESTERDAY -> "Consultando ayer: ${TimeUtils.getFormattedDate(yesterday)}"
                        UsageFilterPeriod.SPECIFIC_DAY -> "Día seleccionado: ${TimeUtils.getFormattedDate(selectedDate)}"
                        UsageFilterPeriod.MONTH -> "Mes seleccionado: ${TimeUtils.getMonthYearFormatted(selectedYearMonth)}"
                    }

                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ScreenlyBlue
                    ) {
                        Text(
                            text = if (filterPeriod == UsageFilterPeriod.MONTH) "Mes completo" else "Día",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasData: Boolean,
    isMonthMode: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> ScreenlyBlue
        isMonthMode -> ScreenlyBlueContainer.copy(alpha = 0.5f)
        isToday -> ScreenlyBlueContainer
        else -> Color.Transparent
    }

    // Always deep black for regular days, white for selected, deep blue for today
    val textColor = when {
        isSelected -> Color.White
        isToday -> ScreenlyBlueDark
        else -> Color(0xFF0F172A)
    }

    val cellModifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .then(
            if (isToday && !isSelected) {
                Modifier.border(1.5.dp, ScreenlyBlue, CircleShape)
            } else {
                Modifier
            }
        )
        .background(bgColor)
        .clickable(onClick = onClick)
        .testTag("calendar_day_${date.dayOfMonth}")

    Column(
        modifier = cellModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = textColor
        )

        if (hasData) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else ScreenlyCyan)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
