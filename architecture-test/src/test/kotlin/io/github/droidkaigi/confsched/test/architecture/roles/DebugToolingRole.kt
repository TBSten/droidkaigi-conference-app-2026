package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.debugTooling() = "DebugTooling" {
    title = "デバッグツール"
    summary = ":feature:debug の開発専用ツール。リリースビルドには含まれず、パッケージを分けずに平置きする"
    example("DebugKaigiClock", "時刻を差し替える開発用の時計")
    example("EnvironmentAwareTimetableApiProvider", "接続先サーバーを切り替える API")
    layout {
        ":feature:debug".module {
            "commonMain".sourceSet / kotlin / modulePackage / "*".ktFile()
        }
    }
}
