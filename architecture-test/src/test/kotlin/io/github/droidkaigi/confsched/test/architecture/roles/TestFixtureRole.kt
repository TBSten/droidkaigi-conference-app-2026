package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.testFixture() = "TestFixture" {
    title = "テストフィクスチャ"
    summary = "テストで使うモデルの固定データ"
    example("TimetableItemFixture", "セッションの固定データ")
    layout {
        ":core:testing".module {
            "commonMain".sourceSet / kotlin / modulePackage / "*Fixture".ktFile()
        }
    }
}
