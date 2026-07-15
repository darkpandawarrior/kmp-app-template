package com.siddharth.apptemplate.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.siddharth.apptemplate.shared.App

fun main() =
    application {
        Window(onCloseRequest = ::exitApplication, title = "kmp-app-template") {
            App()
        }
    }
