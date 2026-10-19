package com.siddharth.apptemplate.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.siddharth.apptemplate.shared.ai.AiPanel
import com.siddharth.apptemplate.shared.ai.AiPanelState
import com.siddharth.apptemplate.shared.ai.HomeAiBackend
import com.siddharth.apptemplate.shared.di.aiModule
import com.siddharth.apptemplate.shared.theme.AppTheme
import org.koin.core.Koin
import org.koin.core.context.startKoin

/**
 * The single Compose entry point every platform renders. Android's MainActivity, the desktop `main`,
 * an iOS UIViewController, and a wasm `main` all just call [App] — keep platform code to the shell.
 * [dynamicColorScheme] lets a shell supply a platform colour scheme (Android 12+ dynamic colour).
 * Starting Koin here too (guarded, since every platform calls [App] exactly once per process) keeps
 * DI bootstrap out of five separate per-platform entry points for one small [aiModule].
 */
@Composable
fun App(dynamicColorScheme: @Composable (dark: Boolean) -> ColorScheme? = { null }) {
    remember { AppKoin.ensureStarted() }
    AppTheme(dynamicColorScheme = dynamicColorScheme) {
        // The Surface paints edge to edge (behind the system bars); safeDrawingPadding() on the
        // content keeps text and controls out from under the status/navigation bars, the notch,
        // the iOS home indicator and the IME. Android's MainActivity calls enableEdgeToEdge().
        Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            val nav = remember { RootNavState() }
            when (nav.current) {
                RootDestination.Splash -> SplashScreen(onReady = { nav.onSplashComplete() })
                RootDestination.Login -> LoginScreen(onLoggedIn = { nav.onLoggedIn() })
                RootDestination.Home -> HomeScreen(onLogout = { nav.onLoggedOut() })
            }
        }
    }
}

/**
 * Holds the one [Koin] instance every platform's [App] call shares — no `GlobalContext` lookup,
 * since that's a JVM-only convenience, not part of koin-core's commonMain API surface (this file
 * is compiled once, shared by all five [App]-calling targets).
 */
private object AppKoin {
    private var koin: Koin? = null

    fun ensureStarted(): Koin = koin ?: startKoin { modules(aiModule()) }.koin.also { koin = it }
}

@Composable
private fun SplashScreen(onReady: () -> Unit) {
    // No fixed delay: route as soon as the (here, instant) auth check is done. A real app waits on
    // its session restore here, and on Android drives the system splash via installSplashScreen().
    LaunchedEffect(Unit) { onReady() }
    Centered { Text(AppName, style = MaterialTheme.typography.headlineMedium) }
}

@Composable
private fun LoginScreen(onLoggedIn: () -> Unit) {
    Centered {
        Text("Sign in", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        Button(onClick = onLoggedIn, modifier = Modifier.padding(top = 16.dp)) { Text("Continue") }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun HomeScreen(onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    val panelState = remember { AiPanelState(AppKoin.ensureStarted().get<HomeAiBackend>(), scope) }
    var confirmSignOut by remember { mutableStateOf(false) }
    // The sample dismissable state: Back on Home asks before signing out instead of leaving the app.
    // While the dialog is open Back dismisses the dialog itself, so this handler is disabled then.
    // Compose's BackHandler registers with the platform dispatcher, so Android 13+ predictive back
    // (enableOnBackInvokedCallback in the manifest) animates it.
    BackHandler(enabled = !confirmSignOut) { confirmSignOut = true }
    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Sign out?") },
            confirmButton = { TextButton(onClick = onLogout) { Text("Sign out") } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Stay") } },
        )
    }
    // Scrolls so a long streamed answer or the keyboard never pushes "Sign out" out of reach; the
    // IME inset is already consumed by safeDrawingPadding() at the root.
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Home", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        Text("You're signed in.", modifier = Modifier.padding(top = 8.dp))
        AiPanel(panelState, modifier = Modifier.padding(top = 16.dp))
        Button(onClick = { confirmSignOut = true }, modifier = Modifier.padding(top = 16.dp)) { Text("Sign out") }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) { content() }
    }
}

// ---- Previews -------------------------------------------------------------------------------
//
// `androidx.compose.ui.tooling.preview.Preview`, imported above, comes from
// org.jetbrains.compose.ui:ui-tooling-preview — the MULTIPLATFORM annotation since Compose
// Multiplatform 1.10, which is why a file in composeMain can use the androidx FQN. The
// pre-1.10 `org.jetbrains.compose.ui.tooling.preview.Preview` is deprecated.
//
// Previews live in the same file as the composable they render because these screens are
// private. That is the intended shape: a preview is a second call site, not a public API, so it
// belongs next to what it calls rather than in a previews/ package that drifts.
//
// FORK NOTE — what is deliberately NOT here:
//   - HomeScreen has no preview. It resolves HomeAiBackend out of Koin, so previewing it would
//     mean starting DI inside the IDE renderer. Preview the leaf composables a screen is built
//     from (AiPanel.kt does) and leave the DI-wired screen to the running app.
//   - Previews go through AppTheme, which follows the system, so @PreviewLightDark renders both.
//   - No @PreviewScreenSizes / @PreviewFontScale. Six and seven renders respectively; reach for
//     them when an adaptive-layout bug actually bites, not as a default.

@PreviewLightDark
@Composable
private fun SplashScreenPreview() {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) { SplashScreen(onReady = {}) }
    }
}

@PreviewLightDark
@Composable
private fun LoginScreenPreview() {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) { LoginScreen(onLoggedIn = {}) }
    }
}
