package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.iosAppAsset() = "IosAppAsset" {
    title = "iOS のアセットカタログ"
    summary = "アプリアイコンなどのアセットカタログ。構成は Xcode が決める"
    example("AppIcon.appiconset", "アプリアイコン")
    layout {
        "app-ios/Sources/Assets.xcassets" { ignore() }
    }
}
