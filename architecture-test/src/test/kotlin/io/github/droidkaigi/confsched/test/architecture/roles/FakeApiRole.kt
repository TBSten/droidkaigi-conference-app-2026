package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.fakeApi() = "FakeApi" {
    title = "Fake API"
    summary = "サーバーに接続しない環境で使う API の Fake 実装"
    example("FakeTimetableApi", "タイムテーブル API の Fake")
    layout {
        ":core:data".module {
            "commonMain".sourceSet / kotlin / modulePackage / "Fake*Api".ktFile()
        }
    }
}
