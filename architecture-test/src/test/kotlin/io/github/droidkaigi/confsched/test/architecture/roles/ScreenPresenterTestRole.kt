package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.screenPresenterTest() = "ScreenPresenterTest" {
    title = "Presenter テスト"
    summary = "runPresenterTest で UiState の遷移と ActionResult を検証する Presenter の単体テスト"
    example("TimetableScreenPresenterTest", "タイムテーブル画面の Presenter テスト")
    layout {
        ":feature:*".module {
            screenFile("commonTest", "*ScreenPresenterTest")
        }
    }
}
