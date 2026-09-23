package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.featureComponent() = "FeatureComponent" {
    title = "フィーチャー内コンポーネント"
    summary = "1 つの feature の画面からだけ使う Compose ビュー。<Name><Kind> の命名で component パッケージに置く"
    example("SessionInfoCard", "セッション詳細のカード")
    layout {
        ":feature:*".module {
            ("commonMain".sourceSet / kotlin / modulePackage) {
                "component" / "*".ktFile()
                "*/component" / "*".ktFile()
            }
        }
    }
}
