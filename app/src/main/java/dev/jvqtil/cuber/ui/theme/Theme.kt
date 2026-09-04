package dev.jvqtil.cuber.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val FallbackDarkColors = darkColorScheme(
    background = CuberBackground,
    onBackground = Color(0xFFE6E1E9),
    surface = CuberSurface,
    onSurface = Color(0xFFE6E1E9)
)

private val FallbackLightColors = lightColorScheme(
    background = CuberLightBackground,
    onBackground = Color(0xFF1A1A1A),
    surface = CuberLightSurface,
    onSurface = Color(0xFF1A1A1A)
)

@Composable
fun CuberTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val baseColors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isDark ->
            dynamicDarkColorScheme(context)

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            dynamicLightColorScheme(context)

        isDark ->
            FallbackDarkColors

        else ->
            FallbackLightColors
    }

    val backgroundTarget =
        if (isDark) CuberBackground else CuberLightBackground

    val surfaceTarget =
        if (isDark) CuberSurface else CuberLightSurface

    val background by animateColorAsState(
        targetValue = backgroundTarget,
        animationSpec = tween(650, easing = LinearEasing),
        label = "background"
    )

    val surface by animateColorAsState(
        targetValue = surfaceTarget,
        animationSpec = tween(650, easing = LinearEasing),
        label = "surface"
    )

    MaterialTheme(
        colorScheme = baseColors.copy(
            background = background,
            surface = surface
        ),
        content = content
    )
}