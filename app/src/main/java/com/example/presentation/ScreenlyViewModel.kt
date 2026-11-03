package com.example.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.InstalledAppsLoader
import com.example.core.PermissionHelper
import com.example.core.ScreenlyPreferences
import com.example.core.TimeUtils
import com.example.data.repository.ScreenlyDemoRepository
import com.example.domain.model.AlertItem
import com.example.domain.model.AppDetailStats
import com.example.domain.model.AppInfo
import com.example.domain.model.AppLimit
import com.example.domain.model.DailyAppUsage
import com.example.domain.model.PermissionState
import com.example.domain.model.StatsPeriod
import com.example.domain.model.UsageFilterPeriod
import com.example.domain.repository.ScreenlyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class UiNotificationMessage(

    val message: String,
    val isError: Boolean = false,
    val actionLabel: String? = null,
    val actionPayload: String? = null
)

class ScreenlyViewModel(
    private val repository: ScreenlyRepository = ScreenlyDemoRepository()
) : ViewModel() {

    val permissionState: StateFlow<PermissionState> = repository.permissionState
    val apps: StateFlow<List<AppInfo>> = repository.apps
    val limits: StateFlow<List<AppLimit>> = repository.limits
    val alerts: StateFlow<List<AlertItem>> = repository.alerts

    // Filter mode: Hoy, Ayer, Día específico, Mes determinado
    private val _usageFilterPeriod = MutableStateFlow(UsageFilterPeriod.TODAY)
    val usageFilterPeriod: StateFlow<UsageFilterPeriod> = _usageFilterPeriod.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow(LocalDate.now())
    val selectedCalendarDate: StateFlow<LocalDate> = _selectedCalendarDate.asStateFlow()

    private val _selectedYearMonth = MutableStateFlow(YearMonth.now())
    val selectedYearMonth: StateFlow<YearMonth> = _selectedYearMonth.asStateFlow()

    // Interactive calendar starts hidden initially as required
    private val _isCalendarExpanded = MutableStateFlow(false)
    val isCalendarExpanded: StateFlow<Boolean> = _isCalendarExpanded.asStateFlow()

    // Real-time device clock and date
    private val _currentDeviceTime = MutableStateFlow(TimeUtils.getDeviceTimeFormatted())
    val currentDeviceTime: StateFlow<String> = _currentDeviceTime.asStateFlow()

    private val _currentDeviceDate = MutableStateFlow(TimeUtils.getDeviceDateFormatted())
    val currentDeviceDate: StateFlow<String> = _currentDeviceDate.asStateFlow()

    // Usage list computed based on current filter period (Hoy, Ayer, Día seleccionado, Mes determinado)
    val dailyUsage: StateFlow<List<DailyAppUsage>> = combine(
        _usageFilterPeriod,
        _selectedCalendarDate,
        _selectedYearMonth,
        limits,
        apps
    ) { filter, date, ym, _, _ ->
        when (filter) {
            UsageFilterPeriod.TODAY -> repository.getDailyUsageForDate(LocalDate.now()).value
            UsageFilterPeriod.YESTERDAY -> repository.getDailyUsageForDate(LocalDate.now().minusDays(1)).value
            UsageFilterPeriod.SPECIFIC_DAY -> repository.getDailyUsageForDate(date).value
            UsageFilterPeriod.MONTH -> repository.getMonthlyUsage(ym).value
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    // Top 5 apps for the selected date
    val topApps: StateFlow<List<DailyAppUsage>> = dailyUsage.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    // Total usage minutes today / selected date
    val totalUsageMinutesToday: StateFlow<Int> = dailyUsage.combine(permissionState) { list, perm ->
        if (perm.hasUsageStatsPermission) list.sumOf { it.minutesUsed } else 0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Dates with recorded usage data (today and past 6 days)
    val datesWithData: StateFlow<Set<LocalDate>> = MutableStateFlow(
        (0..6).map { LocalDate.now().minusDays(it.toLong()) }.toSet()
    ).asStateFlow()

    // Stats Screen selections
    private val _selectedStatsAppId = MutableStateFlow("com.instagram.android")
    val selectedStatsAppId: StateFlow<String> = _selectedStatsAppId.asStateFlow()

    private val _selectedStatsPeriod = MutableStateFlow(StatsPeriod.TODAY)
    val selectedStatsPeriod: StateFlow<StatsPeriod> = _selectedStatsPeriod.asStateFlow()

    val currentAppStats: StateFlow<AppDetailStats?> = combine(
        _selectedStatsAppId,
        _selectedStatsPeriod,
        limits
    ) { appId, period, _ ->
        repository.getAppStats(appId, period)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Active limit reached dialog
    private val _limitReachedDialogApp = MutableStateFlow<DailyAppUsage?>(null)
    val limitReachedDialogApp: StateFlow<DailyAppUsage?> = _limitReachedDialogApp.asStateFlow()

    // Delete limit confirmation dialog
    private val _deleteLimitConfirmTarget = MutableStateFlow<AppLimit?>(null)
    val deleteLimitConfirmTarget: StateFlow<AppLimit?> = _deleteLimitConfirmTarget.asStateFlow()

    // Alerts bottom sheet visibility
    private val _showAlertsSheet = MutableStateFlow(false)
    val showAlertsSheet: StateFlow<Boolean> = _showAlertsSheet.asStateFlow()

    // Installed app picker bottom sheet visibility
    private val _showInstalledAppPickerSheet = MutableStateFlow(false)
    val showInstalledAppPickerSheet: StateFlow<Boolean> = _showInstalledAppPickerSheet.asStateFlow()

    // Simulator bottom sheet visibility
    private val _showSimulatorSheet = MutableStateFlow(false)
    val showSimulatorSheet: StateFlow<Boolean> = _showSimulatorSheet.asStateFlow()

    // User feedback / snackbar messages
    private val _uiMessages = MutableSharedFlow<UiNotificationMessage>()
    val uiMessages: SharedFlow<UiNotificationMessage> = _uiMessages.asSharedFlow()

    init {
        // Ticking clock coroutine for live time and date updates
        viewModelScope.launch {
            while (true) {
                _currentDeviceTime.value = TimeUtils.getDeviceTimeFormatted(LocalTime.now())
                _currentDeviceDate.value = TimeUtils.getDeviceDateFormatted(LocalDate.now())
                delay(1000)
            }
        }
    }

    fun initialize(context: Context) {
        val prefs = ScreenlyPreferences(context)
        repository.setOnboardingCompleted(prefs.hasCompletedOnboarding)

        // Sync real system permissions
        syncSystemPermissions(context)

        // Load real installed apps
        viewModelScope.launch {
            val installed = InstalledAppsLoader.loadInstalledApps(context)
            if (installed.isNotEmpty()) {
                repository.setInstalledApps(installed)
                if (selectedStatsAppId.value.isBlank() || apps.value.none { it.id == selectedStatsAppId.value }) {
                    _selectedStatsAppId.value = installed.first().id
                }
            }
        }
    }

    fun syncSystemPermissions(context: Context) {
        val hasUsage = PermissionHelper.hasUsageStatsPermission(context)
        val hasNotif = PermissionHelper.hasNotificationPermission(context)
        // If in demo mode and permissions were enabled in demo, preserve unless real is checked
        if (!permissionState.value.isDemoModeActive || hasUsage || hasNotif) {
            repository.syncSystemPermissions(hasUsage, hasNotif)
        }
    }

    fun completeOnboarding(context: Context) {
        val prefs = ScreenlyPreferences(context)
        prefs.hasCompletedOnboarding = true
        repository.setOnboardingCompleted(true)
    }

    fun resetOnboarding(context: Context) {
        val prefs = ScreenlyPreferences(context)
        prefs.resetPreferences()
        repository.setOnboardingCompleted(false)
    }

    fun toggleCalendarExpanded() {
        _isCalendarExpanded.value = !_isCalendarExpanded.value
    }

    fun setCalendarExpanded(expanded: Boolean) {
        _isCalendarExpanded.value = expanded
    }

    fun selectToday() {
        _usageFilterPeriod.value = UsageFilterPeriod.TODAY
        _selectedCalendarDate.value = LocalDate.now()
        _selectedYearMonth.value = YearMonth.now()
    }

    fun selectYesterday() {
        _usageFilterPeriod.value = UsageFilterPeriod.YESTERDAY
        val yesterday = LocalDate.now().minusDays(1)
        _selectedCalendarDate.value = yesterday
        _selectedYearMonth.value = YearMonth.from(yesterday)
    }

    fun selectCalendarDate(date: LocalDate) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        _selectedCalendarDate.value = date
        _selectedYearMonth.value = YearMonth.from(date)
        _usageFilterPeriod.value = when (date) {
            today -> UsageFilterPeriod.TODAY
            yesterday -> UsageFilterPeriod.YESTERDAY
            else -> UsageFilterPeriod.SPECIFIC_DAY
        }
    }

    fun selectMonth(yearMonth: YearMonth) {
        _selectedYearMonth.value = yearMonth
        _usageFilterPeriod.value = UsageFilterPeriod.MONTH
    }

    fun setFilterPeriod(period: UsageFilterPeriod) {
        _usageFilterPeriod.value = period
        when (period) {
            UsageFilterPeriod.TODAY -> {
                _selectedCalendarDate.value = LocalDate.now()
                _selectedYearMonth.value = YearMonth.now()
            }
            UsageFilterPeriod.YESTERDAY -> {
                val yesterday = LocalDate.now().minusDays(1)
                _selectedCalendarDate.value = yesterday
                _selectedYearMonth.value = YearMonth.from(yesterday)
            }
            UsageFilterPeriod.SPECIFIC_DAY -> {
                // Preserves selectedCalendarDate
            }
            UsageFilterPeriod.MONTH -> {
                // Preserves selectedYearMonth
            }
        }
    }

    fun selectDateOffset(offset: Int) {
        selectCalendarDate(LocalDate.now().minusDays(offset.toLong()))
    }


    fun selectStatsApp(appId: String) {
        _selectedStatsAppId.value = appId
    }

    fun selectStatsPeriod(period: StatsPeriod) {
        _selectedStatsPeriod.value = period
    }

    fun createLimit(
        appId: String,
        hours: Int,
        minutes: Int,
        isActive: Boolean,
        hasValidityPeriod: Boolean = false,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        onSuccess: () -> Unit
    ) {
        val totalMinutes = (hours * 60) + minutes
        val result = repository.saveLimit(
            appId = appId,
            totalMinutes = totalMinutes,
            isActive = isActive,
            hasValidityPeriod = hasValidityPeriod,
            startDate = startDate,
            endDate = endDate
        )
        result.fold(
            onSuccess = {
                viewModelScope.launch {
                    _uiMessages.emit(UiNotificationMessage(message = "Límite guardado correctamente"))
                }
                onSuccess()
            },
            onFailure = { error ->
                viewModelScope.launch {
                    _uiMessages.emit(
                        UiNotificationMessage(
                            message = error.message ?: "Error al guardar el límite",
                            isError = true
                        )
                    )
                }
            }
        )
    }

    fun updateLimit(
        limitId: String,
        hours: Int,
        minutes: Int,
        isActive: Boolean,
        hasValidityPeriod: Boolean = false,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        onSuccess: () -> Unit
    ) {
        val totalMinutes = (hours * 60) + minutes
        val result = repository.updateLimit(
            limitId = limitId,
            totalMinutes = totalMinutes,
            isActive = isActive,
            hasValidityPeriod = hasValidityPeriod,
            startDate = startDate,
            endDate = endDate
        )
        result.fold(
            onSuccess = {
                viewModelScope.launch {
                    _uiMessages.emit(UiNotificationMessage(message = "Límite actualizado correctamente"))
                }
                onSuccess()
            },
            onFailure = { error ->
                viewModelScope.launch {
                    _uiMessages.emit(
                        UiNotificationMessage(
                            message = error.message ?: "Error al actualizar el límite",
                            isError = true
                        )
                    )
                }
            }
        )
    }

    fun toggleLimitActive(limitId: String, isActive: Boolean) {
        repository.toggleLimitActive(limitId, isActive)
    }

    fun requestDeleteLimit(limit: AppLimit) {
        _deleteLimitConfirmTarget.value = limit
    }

    fun dismissDeleteLimitDialog() {
        _deleteLimitConfirmTarget.value = null
    }

    fun confirmDeleteLimit() {
        val target = _deleteLimitConfirmTarget.value ?: return
        repository.deleteLimit(target.id)
        _deleteLimitConfirmTarget.value = null
        viewModelScope.launch {
            _uiMessages.emit(UiNotificationMessage(message = "Límite eliminado"))
        }
    }

    fun getLimitById(limitId: String): AppLimit? {
        return repository.getLimitById(limitId)
    }

    fun getLimitForApp(appId: String): AppLimit? {
        return repository.getLimitForApp(appId)
    }

    fun getAppById(appId: String): AppInfo? {
        return apps.value.firstOrNull { it.id == appId }
    }

    fun showLimitReachedDialog(appUsage: DailyAppUsage) {
        _limitReachedDialogApp.value = appUsage
    }

    fun dismissLimitReachedDialog() {
        _limitReachedDialogApp.value = null
    }

    fun openAlertsSheet() {
        _showAlertsSheet.value = true
    }

    fun closeAlertsSheet() {
        _showAlertsSheet.value = false
    }

    fun openInstalledAppPicker() {
        _showInstalledAppPickerSheet.value = true
    }

    fun closeInstalledAppPicker() {
        _showInstalledAppPickerSheet.value = false
    }

    fun openSimulatorSheet() {
        _showSimulatorSheet.value = true
    }

    fun closeSimulatorSheet() {
        _showSimulatorSheet.value = false
    }

    fun markAlertAsRead(alertId: String) {
        repository.markAlertAsRead(alertId)
    }

    fun clearAllAlerts() {
        repository.clearAllAlerts()
    }

    fun setUsageStatsPermission(granted: Boolean) {
        repository.setUsageStatsPermission(granted)
    }

    fun setNotificationPermission(granted: Boolean) {
        repository.setNotificationPermission(granted)
    }

    fun toggleDemoMode(enabled: Boolean) {
        repository.toggleDemoMode(enabled)
    }

    fun triggerTestLimitReached(appId: String) {
        repository.triggerSimulatedLimitReached(appId)
        val usage = repository.getDailyUsage(0).value.firstOrNull { it.app.id == appId }
        if (usage != null) {
            _limitReachedDialogApp.value = usage
        }
    }
}
