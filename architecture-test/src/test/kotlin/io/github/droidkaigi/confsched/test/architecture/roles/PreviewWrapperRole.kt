package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.previewWrapper() = "PreviewWrapper" {
    title = "プレビューラッパー"
    summary = "@Preview を包むラッパーと、その DI グラフ"
    example("KaigiPreviewWrapper", "プレビューのラッパー")
    layout {
        ":core:preview:wrapper".module {
            "commonMain".sourceSet / kotlin / modulePackage / "*".ktFile()
        }
    }
}
