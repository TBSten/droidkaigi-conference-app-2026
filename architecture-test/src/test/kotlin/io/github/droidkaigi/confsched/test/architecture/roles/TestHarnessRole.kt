package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.testHarness() = "TestHarness" {
    title = "テスト基盤"
    summary = "Presenter テストと Robot テストの実行基盤"
    example("PresenterTest", "runPresenterTest")
    example("RobotTest", "Robot テストの実行")
    layout {
        ":core:testing".module {
            "commonMain".sourceSet / kotlin / modulePackage / "*".ktFile()
        }
    }
}
