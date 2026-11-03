package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ScreenlyWarning
import com.example.ui.theme.ScreenlyWarningContainer

@Composable
fun PendingPermissionBanner(
    hasUsageStatsPermission: Boolean,
    hasNotificationPermission: Boolean,
    onResolveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPending = !hasUsageStatsPermission || !hasNotificationPermission

    AnimatedVisibility(
        visible = isPending,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val message = when {
            !hasUsageStatsPermission && !hasNotificationPermission ->
                "Permisos pendientes de acceso a datos de uso y notificaciones."
            !hasUsageStatsPermission ->
                "Debes conceder el permiso de acceso a datos de uso para ver esta información."
            else ->
                "Permiso de notificaciones pendiente para recibir alertas de tus límites."
        }

        BoxBanner(
            message = message,
            buttonText = "Configurar permisos",
            onClick = onResolveClick,
            modifier = modifier
        )
    }
}

@Composable
private fun BoxBanner(
    message: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ScreenlyWarningContainer.copy(alpha = 0.25f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = "Aviso de permisos",
                tint = ScreenlyWarning,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Permiso requerido",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ScreenlyWarning
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.testTag("resolve_permissions_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ScreenlyWarning),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = buttonText, style = MaterialTheme.typography.labelSmall)
        }
    }
}
