package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.ScreenlyPreferences
import com.example.core.TimeUtils
import com.example.data.repository.ScreenlyDemoRepository
import com.example.domain.model.AppLimit
import com.example.domain.model.LimitStatusType
import com.example.domain.model.StatsPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Screenly", appName)
  }

  @Test
  fun `repository limits validation rules and validity periods`() {
    val repo = ScreenlyDemoRepository()

    // Rule 1: Limit must be greater than zero
    val zeroResult = repo.saveLimit("com.spotify.music", 0, true)
    assertTrue(zeroResult.isFailure)
    assertEquals("El límite debe ser mayor que cero", zeroResult.exceptionOrNull()?.message)

    // Rule 2: Limit cannot exceed 24 hours (1440 mins)
    val maxResult = repo.saveLimit("com.spotify.music", 24 * 60, true)
    assertTrue(maxResult.isFailure)
    assertEquals("El límite no puede superar 24 horas", maxResult.exceptionOrNull()?.message)

    // Rule 3: Validity period end date cannot be before start date
    val invalidValidity = repo.saveLimit(
        appId = "com.spotify.music",
        totalMinutes = 60,
        isActive = true,
        hasValidityPeriod = true,
        startDate = LocalDate.now().plusDays(5),
        endDate = LocalDate.now().plusDays(2)
    )
    assertTrue(invalidValidity.isFailure)
    assertEquals("La fecha de finalización debe ser posterior o igual a la de inicio", invalidValidity.exceptionOrNull()?.message)

    // Rule 4: Valid creation with validity period
    val validResult = repo.saveLimit(
        appId = "com.spotify.music",
        totalMinutes = 45,
        isActive = true,
        hasValidityPeriod = true,
        startDate = LocalDate.now(),
        endDate = LocalDate.now().plusDays(30)
    )
    assertTrue(validResult.isSuccess)
    val created = validResult.getOrNull()
    assertNotNull(created)
    assertEquals(45, created?.dailyLimitMinutes)
    assertTrue(created?.hasValidityPeriod == true)

    // Rule 5: Duplicate app limit restriction
    val duplicateResult = repo.saveLimit("com.spotify.music", 60, true)
    assertTrue(duplicateResult.isFailure)
    assertEquals("Esta aplicación ya tiene un límite configurado", duplicateResult.exceptionOrNull()?.message)
  }

  @Test
  fun `limit status calculation based on dates and usage`() {
    val today = LocalDate.now()

    // Programado (future)
    val scheduled = AppLimit(
        id = "1",
        appId = "app1",
        dailyLimitMinutes = 60,
        isActive = true,
        hasValidityPeriod = true,
        startDate = today.plusDays(3),
        endDate = today.plusDays(10)
    )
    assertEquals(LimitStatusType.PROGRAMADO, scheduled.calculateStatus(minutesUsedToday = 30, today = today))

    // Finalizado (expired)
    val expired = AppLimit(
        id = "2",
        appId = "app2",
        dailyLimitMinutes = 60,
        isActive = true,
        hasValidityPeriod = true,
        startDate = today.minusDays(10),
        endDate = today.minusDays(1)
    )
    assertEquals(LimitStatusType.FINALIZADO, expired.calculateStatus(minutesUsedToday = 30, today = today))

    // Inactivo (switch disabled)
    val inactive = AppLimit(
        id = "3",
        appId = "app3",
        dailyLimitMinutes = 60,
        isActive = false
    )
    assertEquals(LimitStatusType.INACTIVO, inactive.calculateStatus(minutesUsedToday = 70, today = today))

    // Alcanzado (100%+)
    val reached = AppLimit(
        id = "4",
        appId = "app4",
        dailyLimitMinutes = 60,
        isActive = true
    )
    assertEquals(LimitStatusType.ALCANZADO, reached.calculateStatus(minutesUsedToday = 65, today = today))

    // Proximo (80%+)
    val near = AppLimit(
        id = "5",
        appId = "app5",
        dailyLimitMinutes = 60,
        isActive = true
    )
    assertEquals(LimitStatusType.PROXIMO, near.calculateStatus(minutesUsedToday = 50, today = today))

    // Vigente (healthy)
    val valid = AppLimit(
        id = "6",
        appId = "app6",
        dailyLimitMinutes = 60,
        isActive = true
    )
    assertEquals(LimitStatusType.VIGENTE, valid.calculateStatus(minutesUsedToday = 20, today = today))
  }

  @Test
  fun `screenly preferences onboarding persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = ScreenlyPreferences(context)
    prefs.resetPreferences()

    assertFalse(prefs.hasCompletedOnboarding)
    prefs.hasCompletedOnboarding = true
    assertTrue(prefs.hasCompletedOnboarding)
  }

  @Test
  fun `time formatting helpers`() {
    assertEquals("1h 30min", TimeUtils.formatMinutesToDuration(90))
    assertEquals("45min", TimeUtils.formatMinutesToDuration(45))
    assertEquals("2h", TimeUtils.formatMinutesToDuration(120))
    assertEquals("0 min", TimeUtils.formatMinutesToDuration(0))
  }

  @Test
  fun `app stats calculation for today and 7 days`() {
    val repo = ScreenlyDemoRepository()
    val statsToday = repo.getAppStats("com.instagram.android", StatsPeriod.TODAY)
    val statsWeek = repo.getAppStats("com.instagram.android", StatsPeriod.LAST_7_DAYS)

    assertEquals("Instagram", statsToday.app.name)
    assertTrue(statsToday.totalMinutes > 0)
    assertTrue(statsWeek.totalMinutes >= statsToday.totalMinutes)
    assertEquals(7, statsWeek.dailyBreakdown.size)
  }

  @Test
  fun `query usage for today, yesterday, and any selected date`() {
    val repo = ScreenlyDemoRepository()
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val pastDate = today.minusDays(14)

    val todayUsage = repo.getDailyUsageForDate(today).value
    val yesterdayUsage = repo.getDailyUsageForDate(yesterday).value
    val pastUsage = repo.getDailyUsageForDate(pastDate).value

    assertTrue("Today usage should have apps", todayUsage.isNotEmpty())
    assertTrue("Yesterday usage should have apps", yesterdayUsage.isNotEmpty())
    assertTrue("Past date usage should have apps", pastUsage.isNotEmpty())

    val todayTotal = todayUsage.sumOf { it.minutesUsed }
    val yesterdayTotal = yesterdayUsage.sumOf { it.minutesUsed }
    assertTrue(todayTotal > 0)
    assertTrue(yesterdayTotal > 0)
  }

  @Test
  fun `query monthly usage aggregates full month`() {
    val repo = ScreenlyDemoRepository()
    val currentMonth = java.time.YearMonth.now()
    val monthlyUsage = repo.getMonthlyUsage(currentMonth).value

    assertTrue("Monthly usage should contain apps", monthlyUsage.isNotEmpty())
    val totalMonthlyMinutes = monthlyUsage.sumOf { it.minutesUsed }
    assertTrue("Monthly usage should aggregate more than a single day", totalMonthlyMinutes > 100)
  }
}

