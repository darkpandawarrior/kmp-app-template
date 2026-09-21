package com.siddharth.apptemplate.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.siddharth.apptemplate.shared.App
import platform.UIKit.UIViewController

/**
 * The iOS entry point, and the only Kotlin symbol the Swift side touches.
 *
 * Kotlin puts top-level functions on a class named after the file, so Swift sees this as
 * `MainViewControllerKt.MainViewController()`. cmp-ios/iosApp/ContentView.swift calls exactly that.
 *
 * ```swift
 * import ComposeApp
 *
 * struct ComposeView: UIViewControllerRepresentable {
 *     func makeUIViewController(context: Context) -> UIViewController {
 *         MainViewControllerKt.MainViewController()
 *     }
 *     func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
 * }
 * ```
 *
 * Like every other platform shell in this template ([App] is called identically from
 * MainActivity, the desktop `main()` and the wasm `main()`), this file holds the entry point and
 * nothing else — all UI lives in :cmp-shared.
 */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
