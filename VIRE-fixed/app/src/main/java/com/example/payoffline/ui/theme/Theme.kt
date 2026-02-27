package com.example.payoffline.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── VIRE Premium Light Scheme ─────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary              = Violet600,
    onPrimary            = Color.White,
    primaryContainer     = Violet100,
    onPrimaryContainer   = Violet900,
    secondary            = Cyan600,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary             = Teal500,
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFFCCFBF1),
    onTertiaryContainer  = Color(0xFF134E4A),
    error                = Rose600,
    onError              = Color.White,
    errorContainer       = Color(0xFFFFE4E6),
    onErrorContainer     = Color(0xFF9B1C1C),
    background           = Color(0xFFF5F3FF),   // very soft violet-white
    onBackground         = Slate900,
    surface              = Color.White,
    onSurface            = Slate900,
    surfaceVariant       = Violet50,
    onSurfaceVariant     = Slate600,
    outline              = Violet200,
    outlineVariant       = Slate200,
)

// ── VIRE Premium Dark Scheme ──────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = Violet400,
    onPrimary            = Violet900,
    primaryContainer     = Violet800,
    onPrimaryContainer   = Violet100,
    secondary            = Cyan400,
    onSecondary          = Color(0xFF164E63),
    secondaryContainer   = Color(0xFF164E63),
    onSecondaryContainer = Cyan400,
    tertiary             = Teal400,
    onTertiary           = Color(0xFF134E4A),
    error                = Rose500,
    onError              = Color.White,
    background           = DarkBg,
    onBackground         = Slate100,
    surface              = DarkSurface,
    onSurface            = Slate100,
    surfaceVariant       = DarkSurface2,
    onSurfaceVariant     = Slate400,
    outline              = Slate600,
    outlineVariant       = DarkCard,
)

@Composable
fun PayOfflineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
