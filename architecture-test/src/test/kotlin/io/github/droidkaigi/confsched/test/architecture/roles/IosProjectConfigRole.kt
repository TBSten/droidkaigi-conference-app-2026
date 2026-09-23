package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.iosProjectConfig() = "IosProjectConfig" {
    title = "Xcode プロジェクト"
    summary = "XcodeGen の定義と Xcode プロジェクト。xcodeproj の中身は Xcode が管理する"
    example("project.yml", "XcodeGen の定義")
    layout {
        "app-ios" {
            "project.yml".file()
            "KaigiApp-Info.plist".file()
            "KaigiApp.entitlements".file()
            "KaigiApp.xcodeproj" { ignore() }
        }
    }
}
