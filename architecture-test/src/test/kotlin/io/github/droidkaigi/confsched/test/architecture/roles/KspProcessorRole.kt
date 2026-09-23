package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.KSP_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.mainSourceSet
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.kspProcessor() = "KspProcessor" {
    title = "KSP プロセッサ"
    summary = "NavKey のシリアライザ登録・API プロバイダ・Soil の ID を生成する KSP プロセッサ"
    example("NavKeySerializersProviderGenerator", "NavKey のシリアライザ登録の生成")
    layout {
        ":tools:ksp-processor".module {
            mainSourceSet / kotlin / KSP_PACKAGE {
                "*Generator".ktFile()
                "*SymbolProcessorProvider".ktFile()
            }
        }
    }
}
