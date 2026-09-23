package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.conventionPlugin() = "ConventionPlugin" {
    title = "コンベンションプラグイン"
    summary = "各モジュールのビルドスクリプトが適用する precompiled script plugin。primitive は単一の関心、convention は primitive の組み合わせ"
    documented = false
    example("primitive/kmp.gradle.kts", "KMP のターゲット設定")
    layout {
        "gradle-conventions/src/main/kotlin/droidkaigi" {
            "primitive" / "*.gradle.kts".file()
            "convention" / "*.gradle.kts".file()
        }
    }
}
