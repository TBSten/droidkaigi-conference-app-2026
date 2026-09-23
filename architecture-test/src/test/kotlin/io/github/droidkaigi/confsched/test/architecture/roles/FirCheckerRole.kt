package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.ENFORCEMENT_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.mainSourceSet
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.firChecker() = "FirChecker" {
    title = "FIR チェッカー"
    summary = "アーキテクチャのルールをコンパイルエラーにする K2 FIR チェッカー。1 ルール 1 ファイル"
    example("NavigatorCheckers", "NavigatorConfinedToNavEntry")
    layout {
        ":tools:compiler-plugin".module {
            mainSourceSet / kotlin / ENFORCEMENT_PACKAGE {
                "*Checkers".ktFile()
                "*Checker".ktFile()
            }
        }
    }
}
