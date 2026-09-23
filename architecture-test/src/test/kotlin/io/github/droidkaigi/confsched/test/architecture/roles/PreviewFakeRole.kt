package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.previewApiPackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.previewFake() = "PreviewFake" {
    title = "プレビュー用 Fake"
    summary = "プレビューとテストで使うドメインモデルの fake() ビルダー"
    example("TimetableFake", "Timetable.fake()")
    layout {
        ":core:preview:api".module {
            "commonMain".sourceSet / kotlin / previewApiPackage / "*Fake".ktFile()
        }
    }
}
