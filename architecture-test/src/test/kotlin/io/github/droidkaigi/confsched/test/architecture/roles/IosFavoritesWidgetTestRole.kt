package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.iosFavoritesWidgetTest() = "IosFavoritesWidgetTest" {
    title = "iOS ウィジェットのテスト"
    summary = "お気に入りウィジェットの XCTest と、そのフィクスチャ"
    example("FavoritesWidgetStateTests.swift", "ウィジェットの状態のテスト")
    layout {
        "app-ios/FavoritesWidget/Tests" {
            "*Tests.swift".file()
            "*-Info.plist".file()
            "*.json".file()
        }
    }
}
