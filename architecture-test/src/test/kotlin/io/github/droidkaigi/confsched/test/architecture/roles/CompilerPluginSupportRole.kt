package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.ENFORCEMENT_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.mainSourceSet
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.compilerPluginSupport() = "CompilerPluginSupport" {
    title = "コンパイラプラグインの基盤"
    summary = "プラグインの登録・コマンドライン処理・IR 拡張と、チェッカーが共有する解析補助"
    example("EnforcementCompilerPluginRegistrar", "プラグインの登録")
    layout {
        ":tools:compiler-plugin".module {
            mainSourceSet / kotlin / ENFORCEMENT_PACKAGE {
                "Enforcement*".ktFile()
                "*IrExtension".ktFile()
                "*Utils".ktFile()
                "*Analysis".ktFile()
                "PluginNames".ktFile()
            }
        }
    }
}
