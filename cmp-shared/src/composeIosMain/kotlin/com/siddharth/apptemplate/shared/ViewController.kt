package com.siddharth.apptemplate.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** The iOS entry point — iosApp/ContentView.swift calls `ViewControllerKt.viewController()`. */
fun viewController(): UIViewController = ComposeUIViewController { App() }
