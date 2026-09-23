package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.invoke
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.designSystemTheme() = "DesignSystemTheme" {
    title = "デザインシステム"
    summary = "テーマ・タイポグラフィ・アイコンの定義"
    example("KaigiTheme", "アプリのテーマ")
    layout {
        ":core:designsystem".module {
            "commonMain".sourceSet / kotlin / modulePackage {
                "*".ktFile()
                "icon" / "*".ktFile()
            }
        }
    }
}
