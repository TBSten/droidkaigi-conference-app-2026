package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.featureLocalStore() = "FeatureLocalStore" {
    title = "フィーチャー固有のストア"
    summary = "1 つの画面の中だけで保持する状態のストア"
    example("TimetableDayRequestStore", "タイムテーブルで選択中の日")
    layout {
        ":feature:*".module {
            screenFile("commonMain", "*Store")
        }
    }
}
