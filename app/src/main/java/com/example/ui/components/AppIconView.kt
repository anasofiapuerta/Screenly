package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.InstalledAppsLoader
import com.example.domain.model.AppInfo

@Composable
fun AppIconView(
    app: AppInfo,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val realDrawable = InstalledAppsLoader.iconCache[app.packageName]
    val density = LocalDensity.current
    val pxSize = with(density) { size.roundToPx() }

    val realBitmap = remember(app.packageName, pxSize) {
        realDrawable?.let { drawableToBitmap(it, pxSize, pxSize) }
    }

    if (realBitmap != null) {
        Image(
            bitmap = realBitmap.asImageBitmap(),
            contentDescription = "Icono de ${app.name}",
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.26f))
        )
    } else {
        val baseColor = Color(app.primaryColorHex)
        val gradient = Brush.linearGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.9f),
                baseColor.copy(alpha = 1.0f)
            )
        )

        val iconVector = when (app.iconType) {
            "social" -> Icons.Default.CameraAlt
            "video" -> Icons.Default.PlayArrow
            "chat" -> Icons.Default.Chat
            "browser" -> Icons.Default.Language
            "music" -> Icons.Default.MusicNote
            "work" -> Icons.Default.Work
            else -> Icons.Default.Widgets
        }

        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.28f))
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = "Icono de ${app.name}",
                tint = Color.White,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

private fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap? {
    return try {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val w = width.coerceAtLeast(1)
        val h = height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    } catch (_: Exception) {
        null
    }
}
