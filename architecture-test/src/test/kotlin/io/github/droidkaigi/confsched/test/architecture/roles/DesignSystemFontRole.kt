package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet

fun DeclarationContainerScope.designSystemFont() = "DesignSystemFont" {
    title = "フォント"
    summary = "デザインシステムが使うフォントファイル"
    example("kaigi_sans_regular.ttf", "本文のフォント")
    layout {
        ":core:designsystem".module {
            "commonMain".sourceSet / "composeResources/font" / "*.ttf".file()
        }
    }
}
