package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.screenPresenter() = "ScreenPresenter" {
    title = "Presenter"
    summary = "PresenterContext だけを使い、Action を処理して UiState を計算する @Composable 関数"
    example("TimetableScreenPresenter", "タイムテーブル画面の Presenter")
    layout {
        ":feature:*".module {
            screenFile("commonMain", "*ScreenPresenter")
        }
    }
}
