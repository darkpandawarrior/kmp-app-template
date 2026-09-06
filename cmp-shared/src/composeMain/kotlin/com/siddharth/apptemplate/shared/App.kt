package com.siddharth.apptemplate.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.siddharth.apptemplate.shared.ai.AiPanel
import com.siddharth.apptemplate.shared.ai.AiPanelState
import com.siddharth.apptemplate.shared.ai.HomeAiBackend
import com.siddharth.apptemplate.shared.di.aiModule
import kotlinx.coroutines.delay
import org.koin.core.Koin
import org.koin.core.context.startKoin

/**
 * The single Compose entry point every platform renders. Android's MainActivity, the desktop `main`,
 * an iOS UIViewController, and a wasm `main` all just call [App] — keep platform code to the shell.
 * Starting Koin here too (guarded, since every platform calls [App] exactly once per process) keeps
 * DI bootstrap out of five separate per-platform entry points for one small [aiModule].
 */
@Composable
fun App() {
    remember { AppKoin.ensureStarted() }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
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
    LaunchedEffect(Unit) {
        delay(800)
        onReady()
    }
    Centered { Text("kmp-app-template", style = MaterialTheme.typography.headlineMedium) }
}

@Composable
private fun LoginScreen(onLoggedIn: () -> Unit) {
    Centered {
        Text("Sign in", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onLoggedIn, modifier = Modifier.padding(top = 16.dp)) { Text("Continue") }
    }
}

@Composable
private fun HomeScreen(onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    val panelState = remember { AiPanelState(AppKoin.ensureStarted().get<HomeAiBackend>(), scope) }
    Centered {
        Text("Home", style = MaterialTheme.typography.headlineSmall)
        Text("You're signed in.", modifier = Modifier.padding(top = 8.dp))
        AiPanel(panelState, modifier = Modifier.padding(top = 16.dp))
        Button(onClick = onLogout, modifier = Modifier.padding(top = 16.dp)) { Text("Sign out") }
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
