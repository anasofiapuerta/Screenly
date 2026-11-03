package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.domain.model.AppInfo
import com.example.domain.model.AppLimit
import com.example.ui.theme.ScreenlyDanger

@Composable
fun ConfirmDeleteDialog(
    limit: AppLimit,
    appInfo: AppInfo?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val appName = appInfo?.name ?: "esta aplicación"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = ScreenlyDanger
            )
        },
        title = {
            Text(
                text = "¿Eliminar este límite?",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = "Se quitará el límite diario de uso configurado para $appName. Tu historial de tiempo de uso no se borrará.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = ScreenlyDanger),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}
