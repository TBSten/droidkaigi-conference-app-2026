package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.iosSwiftApp() = "IosSwiftApp" {
    title = "iOS アプリ"
    summary = "SwiftUI のエントリポイントとネイティブのルートタブバー"
    example("KaigiAppApp.swift", "SwiftUI のエントリポイント")
    layout {
        "app-ios" {
            "Sources" / "*.swift".file()
            "Shared" / "*.swift".file()
        }
    }
}
