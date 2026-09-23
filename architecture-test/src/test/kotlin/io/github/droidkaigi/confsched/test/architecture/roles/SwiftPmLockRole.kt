package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.swiftPmLock() = "SwiftPmLock" {
    title = "SwiftPM のロック"
    summary = "Kotlin の swiftPMDependencies が生成・更新する SwiftPM の解決結果"
    example("Package.resolved", "依存の解決結果")
    layout {
        ".swiftpm-locks" { ignore() }
    }
}
