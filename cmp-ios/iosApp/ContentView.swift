import UIKit
import SwiftUI
// ComposeApp is the framework :cmp-ios produces — see cmp-ios/build.gradle.kts, where `baseName`
// sets this name. Xcode finds it via FRAMEWORK_SEARCH_PATHS, which points at the directory the
// "Compile Kotlin Framework" build phase writes into.
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    // MainViewControllerKt is Kotlin's class for the top-level functions in
    // cmp-ios/src/iosMain/.../MainViewController.kt. That file's NAME determines this class name.
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(edges: .all)
    }
}
