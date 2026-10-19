package com.siddharth.apptemplate.android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext
import com.siddharth.apptemplate.shared.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Before super/setContent: transparent system bars; App() applies safeDrawingPadding().
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            // Dynamic colour (wallpaper-derived) on Android 12+; older devices get the static scheme.
            // Delete this lambda to ship a fixed brand palette.
            val context = LocalContext.current
            App(
                dynamicColorScheme = { dark ->
                    when {
                        Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> null
                        dark -> dynamicDarkColorScheme(context)
                        else -> dynamicLightColorScheme(context)
                    }
                },
            )
        }
    }
}
