package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.iosFavoritesWidget() = "IosFavoritesWidget" {
    title = "iOS のお気に入りウィジェット"
    summary = "WidgetKit で作るお気に入りセッションのウィジェット"
    example("FavoritesWidgetView.swift", "ウィジェットのビュー")
    layout {
        "app-ios/FavoritesWidget" {
            "FavoritesWidget-Info.plist".file()
            "FavoritesWidget.entitlements".file()
            "Sources" {
                "*.swift".file()
                "Localizable.xcstrings".file()
            }
        }
    }
}
