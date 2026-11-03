package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ScreenlyDanger
import com.example.ui.theme.ScreenlyTeal
import com.example.ui.theme.ScreenlyWarning

@Composable
fun UsageProgressBar(
    progress: Float, // 0.0f to 1.0f or more
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    isNearLimit: Boolean = false,
    isLimitReached: Boolean = false
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = clampedProgress, label = "progressAnim")

    val targetColor = when {
        isLimitReached -> ScreenlyDanger
        isNearLimit -> ScreenlyWarning
        else -> ScreenlyTeal
    }

    val animatedColor by animateColorAsState(targetValue = targetColor, label = "colorAnim")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(height / 2))
                .background(animatedColor)
        )
    }
}
