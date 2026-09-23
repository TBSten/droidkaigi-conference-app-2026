package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.invoke
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.featureLocalSoilKey() = "FeatureLocalSoilKey" {
    title = "フィーチャー固有の Soil キー"
    summary = "1 つの feature の中だけで使う MutationKey の契約と実装。共有するキーは :core:model / :core:data に置く"
    example("ShareProfileCardMutationKey", "プロフィールカードの共有")
    layout {
        ":feature:profilecard".module {
            description = "プロフィールカード画像の共有"
            "commonMain".sourceSet / kotlin / modulePackage {
                "ShareProfileCardMutationKey".ktFile()
                "DefaultShareProfileCardMutationKey".ktFile()
            }
        }
        ":feature:debug".module {
            description = "開発用の設定値"
            "commonMain".sourceSet / kotlin / modulePackage {
                "*MutationKey".ktFile()
            }
        }
    }
}
