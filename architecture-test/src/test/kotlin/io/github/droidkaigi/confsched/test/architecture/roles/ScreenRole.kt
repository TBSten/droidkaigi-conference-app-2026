package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.screen() = "Screen" {
    title = "画面"
    summary = "描画のみを担う画面の @Composable と、そのプレビュー"
    example("TimetableScreen", "タイムテーブル画面")
    layout {
        ":feature:*".module {
            screenFile("commonMain", "*Screen")
        }
    }
}
