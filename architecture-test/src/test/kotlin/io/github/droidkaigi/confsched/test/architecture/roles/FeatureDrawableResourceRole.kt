package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet

fun DeclarationContainerScope.featureDrawableResource() = "FeatureDrawableResource" {
    title = "フィーチャーの画像リソース"
    summary = "feature の Compose Resources の drawable。snake_case のベクター画像と PNG"
    example("stamp_seal.xml", "スタンプ台紙のシール")
    layout {
        ":feature:*".module {
            "commonMain".sourceSet / "composeResources/drawable" {
                "*.xml".file()
                "*.png".file()
            }
        }
    }
}
