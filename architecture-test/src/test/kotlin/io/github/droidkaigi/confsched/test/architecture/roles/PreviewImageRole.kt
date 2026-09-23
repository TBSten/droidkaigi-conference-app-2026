package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.previewImage() = "PreviewImage" {
    title = "プレビュー画像"
    summary = "プレビュー画像のバイナリとリゾルバ実装。テストと IDE プレビューのクラスパスにだけ入る"
    example("DefaultPreviewImageResolver", "プレビュー画像の解決の実装")
    layout {
        ":core:preview:impl".module {
            "commonMain".sourceSet {
                kotlin / modulePackage / "*".ktFile()
                "composeResources/drawable" / "*.png".file()
            }
        }
    }
}
