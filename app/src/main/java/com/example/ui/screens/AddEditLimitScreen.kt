package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TimeUtils
import com.example.domain.model.AppInfo
import com.example.domain.model.AppLimit
import com.example.ui.components.AppIconView
import com.example.ui.components.InstalledAppPickerSheet
import com.example.ui.theme.ScreenlyBlue
import com.example.ui.theme.ScreenlyDanger
import com.example.ui.theme.ScreenlyWarning
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLimitScreen(
    isEditing: Boolean,
    existingLimit: AppLimit?,
    availableApps: List<AppInfo>,
    existingLimits: List<AppLimit>,
    preselectedAppId: String? = null,
    onSaveNewLimit: (appId: String, hours: Int, minutes: Int, isActive: Boolean, hasValidityPeriod: Boolean, startDate: LocalDate?, endDate: LocalDate?) -> Unit,
    onUpdateLimit: (limitId: String, hours: Int, minutes: Int, isActive: Boolean, hasValidityPeriod: Boolean, startDate: LocalDate?, endDate: LocalDate?) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEditExisting: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialAppId = existingLimit?.appId
        ?: preselectedAppId
        ?: availableApps.firstOrNull()?.id
        ?: ""

    var selectedAppId by remember { mutableStateOf(initialAppId) }
    var hoursText by remember { mutableStateOf((existingLimit?.hours ?: 1).toString()) }
    var minutesText by remember { mutableStateOf((existingLimit?.minutes ?: 0).toString()) }
    var isActive by remember { mutableStateOf(existingLimit?.isActive ?: true) }

    // Validity period state
    var hasValidityPeriod by remember { mutableStateOf(existingLimit?.hasValidityPeriod ?: false) }
    var startDate by remember { mutableStateOf(existingLimit?.startDate ?: LocalDate.now()) }
    var endDate by remember { mutableStateOf(existingLimit?.endDate ?: LocalDate.now().plusDays(30)) }

    // Bottom sheet state for picking real installed apps
    var showAppPickerSheet by remember { mutableStateOf(false) }
    val appPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Date picker dialog states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Error messages per field
    var appError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var validityError by remember { mutableStateOf<String?>(null) }
    var duplicateLimitExistingId by remember { mutableStateOf<String?>(null) }

    val selectedApp = availableApps.firstOrNull { it.id == selectedAppId }

    // Validation runner
    fun validateAndSubmit() {
        appError = null
        timeError = null
        validityError = null
        duplicateLimitExistingId = null

        var hasError = false

        if (selectedAppId.isBlank()) {
            appError = "Es obligatorio seleccionar una aplicación"
            hasError = true
        }

        val parsedHours = hoursText.toIntOrNull()
        val parsedMinutes = minutesText.toIntOrNull()

        if (parsedHours == null || parsedMinutes == null) {
            timeError = "Solo se aceptan valores numéricos enteros"
            hasError = true
        } else if (parsedHours < 0 || parsedHours > 23 || parsedMinutes < 0 || parsedMinutes > 59) {
            timeError = "Las horas (0-23) y minutos (0-59) deben tener rangos válidos"
            hasError = true
        } else {
            val totalMinutes = (parsedHours * 60) + parsedMinutes
            if (totalMinutes <= 0) {
                timeError = "El límite diario debe ser superior a cero"
                hasError = true
            } else if (totalMinutes >= 24 * 60) {
                timeError = "El límite diario no puede superar 24 horas (máx. 23h 59min)"
                hasError = true
            }
        }

        if (hasValidityPeriod) {
            if (endDate.isBefore(startDate)) {
                validityError = "La fecha de finalización debe ser posterior o igual a la de inicio"
                hasError = true
            }
        }

        if (!isEditing) {
            val duplicate = existingLimits.firstOrNull { it.appId == selectedAppId }
            if (duplicate != null) {
                appError = "Esta aplicación ya tiene un límite configurado"
                duplicateLimitExistingId = duplicate.id
                hasError = true
            }
        }

        if (hasError) return

        val finalHours = parsedHours ?: 0
        val finalMinutes = parsedMinutes ?: 0

        if (!isEditing) {
            onSaveNewLimit(selectedAppId, finalHours, finalMinutes, isActive, hasValidityPeriod, if (hasValidityPeriod) startDate else null, if (hasValidityPeriod) endDate else null)
        } else {
            if (existingLimit != null) {
                onUpdateLimit(existingLimit.id, finalHours, finalMinutes, isActive, hasValidityPeriod, if (hasValidityPeriod) startDate else null, if (hasValidityPeriod) endDate else null)
            }
        }
    }

    ScaffoldWrapper(
        title = if (isEditing) "Editar límite" else "Crear límite de tiempo",
        onBack = onNavigateBack
    ) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Section 1: App Selector / Display
                Text(
                    text = "Aplicación objetivo *",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selecciona la aplicación instalada que deseas controlar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isEditing) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("app_selector_readonly"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedApp != null) {
                                AppIconView(app = selectedApp, size = 44.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = selectedApp.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${selectedApp.category} • ${selectedApp.packageName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAppPickerSheet = true }
                            .testTag("app_selector_dropdown_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (appError != null) ScreenlyDanger else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedApp != null) {
                                AppIconView(app = selectedApp, size = 44.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedApp.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${selectedApp.category} • ${selectedApp.packageName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = ScreenlyBlue)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Elegir aplicación instalada...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Button(
                                onClick = { showAppPickerSheet = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ScreenlyBlue)
                            ) {
                                Text("Cambiar", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    if (appError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = appError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = ScreenlyDanger
                        )
                        if (duplicateLimitExistingId != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            FilledTonalButton(
                                onClick = { onNavigateToEditExisting(duplicateLimitExistingId!!) },
                                modifier = Modifier.testTag("edit_duplicate_limit_button")
                            ) {
                                Text("Editar el límite existente")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: Daily limit duration (Hours & Minutes)
                Text(
                    text = "Límite diario de tiempo *",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Cantidad máxima de tiempo de uso permitida por día (entre 1 min y 23h 59min).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { input ->
                            hoursText = input.filter { it.isDigit() }.take(2)
                            timeError = null
                        },
                        label = { Text("Horas") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScreenlyBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_limit_hours")
                    )

                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { input ->
                            minutesText = input.filter { it.isDigit() }.take(2)
                            timeError = null
                        },
                        label = { Text("Minutos") },
                        placeholder = { Text("30") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ScreenlyBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_limit_minutes")
                    )
                }

                if (timeError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timeError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = ScreenlyDanger
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick increment chips (+15m, +30m, +1h, +2h)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Pair("+15m", 15),
                        Pair("+30m", 30),
                        Pair("+1h", 60),
                        Pair("+2h", 120)
                    ).forEach { (label, addMins) ->
                        OutlinedButton(
                            onClick = {
                                val curH = hoursText.toIntOrNull() ?: 0
                                val curM = minutesText.toIntOrNull() ?: 0
                                val total = (curH * 60) + curM + addMins
                                val clamped = total.coerceIn(1, (23 * 60) + 59)
                                hoursText = (clamped / 60).toString()
                                minutesText = (clamped % 60).toString()
                                timeError = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 3: Temporal Validity Period (Vigencia temporal)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Programar período de vigencia",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Establece entre qué fechas se aplicará este límite de uso.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = hasValidityPeriod,
                                onCheckedChange = { hasValidityPeriod = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ScreenlyBlue
                                ),
                                modifier = Modifier.testTag("switch_validity_period")
                            )
                        }

                        if (hasValidityPeriod) {
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Start Date Field
                                OutlinedTextField(
                                    value = TimeUtils.toShortDisplayDate(startDate),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Fecha de inicio") },
                                    trailingIcon = {
                                        IconButton(onClick = { showStartDatePicker = true }) {
                                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Elegir inicio")
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showStartDatePicker = true }
                                )

                                // End Date Field
                                OutlinedTextField(
                                    value = TimeUtils.toShortDisplayDate(endDate),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Fecha de finalización") },
                                    trailingIcon = {
                                        IconButton(onClick = { showEndDatePicker = true }) {
                                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Elegir fin")
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showEndDatePicker = true }
                                )
                            }

                            if (validityError != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = validityError!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ScreenlyDanger
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Switch "Activar límite" (default: true)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Activar límite",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isActive) "El límite se evaluará y enviará alertas al 80% y 100% de uso."
                                else "El límite quedará guardado pero pausado sin enviar alertas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ScreenlyBlue
                            ),
                            modifier = Modifier.testTag("switch_activate_limit_form")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 5: Configuration Summary
                val currentH = hoursText.toIntOrNull() ?: 0
                val currentM = minutesText.toIntOrNull() ?: 0
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Resumen de configuración",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• App: ${selectedApp?.name ?: "Sin seleccionar"}\n" +
                                    "• Tiempo diario permitido: ${TimeUtils.formatMinutesToDuration((currentH * 60) + currentM)}\n" +
                                    "• Estado: ${if (isActive) "Activado" else "Pausado/Inactivo"}\n" +
                                    "• Período: ${if (hasValidityPeriod) "Del ${TimeUtils.toShortDisplayDate(startDate)} al ${TimeUtils.toShortDisplayDate(endDate)}" else "Permanente (sin fecha de caducidad)"}",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Save & Cancel Buttons
                Button(
                    onClick = { validateAndSubmit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_limit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ScreenlyBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isEditing) "Guardar cambios" else "Guardar límite",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("cancel_limit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Bottom Sheet to pick installed apps
    if (showAppPickerSheet) {
        InstalledAppPickerSheet(
            sheetState = appPickerSheetState,
            apps = availableApps,
            selectedAppId = selectedAppId,
            onAppSelected = { app ->
                selectedAppId = app.id
                appError = null
                duplicateLimitExistingId = null
            },
            onDismiss = { showAppPickerSheet = false }
        )
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            startDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showStartDatePicker = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            endDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showEndDatePicker = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScaffoldWrapper(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("limit_form_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        content()
    }
}
