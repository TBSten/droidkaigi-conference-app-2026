package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.compilerPluginDiagnosticTestData() = "CompilerPluginDiagnosticTestData" {
    title = "診断テストデータ"
    summary = "FIR チェッカーの診断テストの入力。ファイル名は診断名"
    example("composableNestingDepth.kt", "ComposableNestingDepth の入力")
    layout {
        ":tools:compiler-plugin".module {
            "testData/diagnostics" / "*.kt".file()
        }
    }
}
