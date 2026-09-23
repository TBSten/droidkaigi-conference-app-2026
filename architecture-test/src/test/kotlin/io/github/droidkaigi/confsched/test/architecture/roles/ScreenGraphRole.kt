package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.screenGraph() = "ScreenGraph" {
    title = "画面グラフ"
    summary = "画面単位の @GraphExtension。ScreenContext・Navigator・MutationTag を提供する"
    example("TimetableScreenGraph", "タイムテーブル画面の DI グラフ")
    layout {
        ":feature:*".module {
            screenFile("commonMain", "*ScreenGraph")
        }
    }
}
