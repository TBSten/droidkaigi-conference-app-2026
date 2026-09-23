package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.APP_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.iosKotlinEntry() = "IosKotlinEntry" {
    title = "iOS 向け Kotlin エントリ"
    summary = "Xcode がリンクする Swift Export モジュール。Compose を含まない公開面の裏で UI をホストする"
    example("KaigiAppHost", "Compose UI をホストするビューコントローラ")
    layout {
        ":app-ios-kotlin".module {
            "iosMain".sourceSet / kotlin / APP_PACKAGE {
                "*".ktFile()
                "*" / "*".ktFile()
            }
        }
    }
}
