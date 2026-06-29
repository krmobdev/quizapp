package com.rustam.quizapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.rustam.quizapp.data.ThemeMode

// iOS-style color schemes using Apple Human Interface Guidelines colors.
private val DarkColorScheme = darkColorScheme(
    primary               = IosBlue80,
    onPrimary             = OnIosBlue80,
    primaryContainer      = IosBlueContainerDark,
    onPrimaryContainer    = OnIosBlueContainerDark,
    secondary             = IosIndigo80,
    onSecondary           = OnIosSurfaceDark,
    secondaryContainer    = IosSurfaceVariantDark,
    onSecondaryContainer  = OnIosSurfaceDark,
    tertiary              = Color(0xFF5E5CE6),   // iOS Indigo Dark
    background            = IosSurfaceDark,
    onBackground          = OnIosSurfaceDark,
    surface               = IosSurfaceDark,
    onSurface             = OnIosSurfaceDark,
    surfaceVariant        = IosSurfaceVariantDark,
    onSurfaceVariant      = OnIosSurfaceVariantDark,
    outline               = IosOutlineDark,
    outlineVariant        = IosOutlineVariantDark,
    error                 = WrongRedDark,
    errorContainer        = Color(0xFF5C1010)
)

private val LightColorScheme = lightColorScheme(
    primary               = IosBlue40,
    onPrimary             = Color.White,
    primaryContainer      = IosBlueContainer,
    onPrimaryContainer    = OnIosBlueContainer,
    secondary             = IosIndigo40,
    onSecondary           = Color.White,
    secondaryContainer    = IosSurfaceVariant,
    onSecondaryContainer  = OnIosSurface,
    tertiary              = Color(0xFF5856D6),   // iOS Indigo
    background            = IosSurface,
    onBackground          = OnIosSurface,
    surface               = IosSurface,
    onSurface             = OnIosSurface,
    surfaceVariant        = IosSurfaceVariant,
    onSurfaceVariant      = OnIosSurfaceVariant,
    outline               = IosOutline,
    outlineVariant        = IosOutlineVariant,
    error                 = WrongRed,
    errorContainer        = Color(0xFFFFDDD8)
)

/** Overrides the primary/tertiary/container roles of a base scheme with a shop accent palette. */
private fun ColorScheme.withAccent(accent: AccentTheme, dark: Boolean): ColorScheme =
    if (dark) {
        copy(
            primary          = accent.primaryDark,
            onPrimary        = accent.onPrimaryDark,
            primaryContainer = accent.containerDark,
            tertiary         = accent.tertiaryDark
        )
    } else {
        copy(
            primary          = accent.primaryLight,
            onPrimary        = accent.onPrimaryLight,
            primaryContainer = accent.containerLight,
            tertiary         = accent.tertiaryLight
        )
    }

@Composable
fun shouldUseDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.DARK   -> true
    ThemeMode.LIGHT  -> false
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
}

@Composable
fun QuizappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    accent: AccentTheme = AccentTheme.DEFAULT,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme.withAccent(accent, dark = true)
        else      -> LightColorScheme.withAccent(accent, dark = false)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars    = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
