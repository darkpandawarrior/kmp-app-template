package com.siddharth.apptemplate.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** The top-level destinations of the root navigation state machine. */
sealed interface RootDestination {
    data object Splash : RootDestination

    data object Login : RootDestination

    data object Home : RootDestination
}

/**
 * The root navigation state machine (Splash → Login/Home), the clean-room seed of the template's
 * RootNav. A real app swaps this remembered holder for a retained ViewModel + a persisted auth check
 * (and can drop in navigation-compose if it wants a back stack) — the transitions stay the same.
 */
class RootNavState {
    var current by mutableStateOf<RootDestination>(RootDestination.Splash)
        private set

    /** Splash finished: route to Home if already authenticated, else to Login. */
    fun onSplashComplete(isLoggedIn: Boolean = false) {
        current = if (isLoggedIn) RootDestination.Home else RootDestination.Login
    }

    fun onLoggedIn() {
        current = RootDestination.Home
    }

    fun onLoggedOut() {
        current = RootDestination.Login
    }
}
