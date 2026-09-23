package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.konsist.konsist

fun DeclarationContainerScope.screenUiState() = "ScreenUiState" {
    title = "UiState"
    summary = "Screen の描画入力となる不変の状態"
    example("TimetableScreenUiState", "タイムテーブル画面の状態")
    layout {
        ":feature:*".module {
            screenFile("commonMain", "*ScreenUiState")
        }
    }

    "declares a class or an interface named after the file".konsist {
        files.must { file ->
            file.classes().any { it.name == file.name } || file.interfaces().any { it.name == file.name }
        }
    }
}
