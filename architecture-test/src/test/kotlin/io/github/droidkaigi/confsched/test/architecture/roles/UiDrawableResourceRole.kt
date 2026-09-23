package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet

fun DeclarationContainerScope.uiDrawableResource() = "UiDrawableResource" {
    title = "共有 UI の画像リソース"
    summary = "共有コンポーネントが使うベクター画像"
    example("card_mascot_a.xml", "カードのマスコット")
    layout {
        ":core:ui".module {
            "commonMain".sourceSet / "composeResources/drawable" / "*.xml".file()
        }
    }
}
