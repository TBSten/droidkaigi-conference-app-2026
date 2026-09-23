package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.iosKmpLinkedPackage() = "IosKmpLinkedPackage" {
    title = "KMP のリンク用パッケージ"
    summary = "Kotlin の swiftPMDependencies が生成する、Xcode から Kotlin のフレームワークをリンクする Swift パッケージ"
    example("Package.swift", "リンク用パッケージの定義")
    layout {
        "app-ios/KotlinMultiplatformLinkedPackage" { ignore() }
    }
}
