package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.ENFORCEMENT_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.mainSourceSet
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.firDiagnosticErrors() = "FirDiagnosticErrors" {
    title = "FIR 診断定義"
    summary = "FIR チェッカーが報告する診断とそのメッセージ"
    example("NavigatorErrors", "Navigator のルールの診断")
    layout {
        ":tools:compiler-plugin".module {
            mainSourceSet / kotlin / ENFORCEMENT_PACKAGE / "*Errors".ktFile()
        }
    }
}
