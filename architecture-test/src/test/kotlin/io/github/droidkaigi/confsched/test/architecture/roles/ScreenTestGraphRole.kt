package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.screenTestGraph() = "ScreenTestGraph" {
    title = "テスト用画面グラフ"
    summary = "Robot テストで Fake のキーを差し込む、画面単位のテスト用 DI グラフ"
    example("TimetableScreenTestGraph", "タイムテーブル画面のテスト用グラフ")
    layout {
        ":feature:*".module {
            screenFile("commonTest", "*ScreenTestGraph")
        }
    }
}
