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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * The single Compose entry point every platform renders. Android's MainActivity, the desktop `main`,
 * an iOS UIViewController, and a wasm `main` all just call [App] — keep platform code to the shell.
 */
@Composable
fun App() {
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
    Centered {
        Text("Home", style = MaterialTheme.typography.headlineSmall)
        Text("You're signed in.", modifier = Modifier.padding(top = 8.dp))
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
