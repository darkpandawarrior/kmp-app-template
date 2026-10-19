package com.siddharth.apptemplate.shared.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.siddharth.kmp.designsystem.ProvideReducedMotion
import com.siddharth.kmp.designsystem.systemReducedMotion

/**
 * The one theme every screen and preview goes through.
 *
 * - Dark follows the system by default ([isSystemInDarkTheme]).
 * - [dynamicColorScheme], when it returns non-null, wins over the static schemes. Compose
 *   Multiplatform has no common dynamic-colour API, so the Android shell passes
 *   `dynamicLightColorScheme/dynamicDarkColorScheme` on Android 12+ (see MainActivity); every other
 *   target leaves it null and gets the static scheme. Replace [LightColors]/[DarkColors] with the
 *   brand palette (and fill DESIGN.md's colour table from them).
 * - [reducedMotion] provides the toolkit's `LocalReducedMotion`. The platform mapping is
 *   best-effort: Android reads `Settings.Global.ANIMATOR_DURATION_SCALE == 0` ("Remove animations"),
 *   iOS `UIAccessibility.isReduceMotionEnabled`, web `prefers-reduced-motion`, desktop is always
 *   false. Pass your own value to drive it from an in-app setting.
 */
@Composable
fun AppTheme(
    dark: Boolean = isSystemInDarkTheme(),
    reducedMotion: Boolean = systemReducedMotion(),
    dynamicColorScheme: @Composable (dark: Boolean) -> ColorScheme? = { null },
    content: @Composable () -> Unit,
) {
    val scheme = dynamicColorScheme(dark) ?: if (dark) DarkColors else LightColors
    ProvideReducedMotion(reduced = reducedMotion) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

// Stock Material 3 baseline schemes: a deliberate placeholder, not a brand.
private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()
