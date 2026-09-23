package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.testFake() = "TestFake" {
    title = "テスト用 Fake"
    summary = "テストで差し込む Soil キーやロガーなどの Fake"
    example("FakeTimetableQueryKey", "TimetableQueryKey の Fake")
    layout {
        ":core:testing".module {
            "commonMain".sourceSet / kotlin / modulePackage / "Fake*".ktFile()
        }
    }
}
