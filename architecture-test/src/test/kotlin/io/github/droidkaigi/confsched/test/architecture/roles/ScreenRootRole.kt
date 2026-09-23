package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.screenRoot() = "ScreenRoot" {
    title = "画面ルート"
    summary = "画面の入口の @Composable。ScreenContext を受け取り、Presenter・データ境界・ナビゲーションを結線する"
    example("TimetableScreenRoot", "タイムテーブル画面のルート")
    layout {
        ":feature:*".module {
            screenFile("commonMain", "*ScreenRoot")
        }
    }
}
