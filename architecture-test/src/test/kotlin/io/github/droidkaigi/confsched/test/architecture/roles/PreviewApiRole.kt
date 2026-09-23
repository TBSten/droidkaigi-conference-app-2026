package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.previewApiPackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.previewApi() = "PreviewApi" {
    title = "プレビュー API"
    summary = "本番コードから参照できるプレビューの契約。画像ハンドルとリゾルバのインターフェイス"
    example("PreviewImageResolver", "プレビュー画像の解決")
    layout {
        ":core:preview:api".module {
            "commonMain".sourceSet / kotlin / previewApiPackage / "*".ktFile()
        }
    }
}
