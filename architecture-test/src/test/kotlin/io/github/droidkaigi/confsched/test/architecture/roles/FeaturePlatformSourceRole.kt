package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import io.github.droidkaigi.confsched.test.architecture.platformSourceSets
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.featurePlatformSource() = "FeaturePlatformSource" {
    title = "フィーチャーのプラットフォーム実装"
    summary = "feature の commonMain の expect に対する各プラットフォームの actual"
    example("ClipEntryFactory", "クリップボードへの書き込み")
    layout {
        ":feature:*".module {
            platformSourceSets.forEach { sourceSet ->
                sourceSet.sourceSet / kotlin / modulePackage / "*".ktFile()
            }
        }
    }
}
