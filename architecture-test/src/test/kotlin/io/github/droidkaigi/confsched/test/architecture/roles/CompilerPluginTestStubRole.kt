package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.compilerPluginTestStub() = "CompilerPluginTestStub" {
    title = "診断テスト用スタブ"
    summary = "診断テストでライブラリの代わりに使う宣言。スタブ対象のパッケージ構造を再現する"
    example("androidx/compose/runtime/Runtime.kt", "Compose Runtime のスタブ")
    // The directories mirror the packages of the stubbed libraries, so their depth varies.
    layout {
        ":tools:compiler-plugin".module {
            "test-stubs" / "**" / "*.kt".file()
        }
    }
}
