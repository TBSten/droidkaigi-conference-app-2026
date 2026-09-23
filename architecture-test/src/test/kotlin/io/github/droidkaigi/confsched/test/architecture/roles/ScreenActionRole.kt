package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.konsist.konsist

fun DeclarationContainerScope.screenAction() = "ScreenAction" {
    title = "Action"
    summary = "UI から Presenter へ送る、実処理を伴う入力の sealed interface"
    example("TimetableScreenAction", "ブックマーク・日付選択")
    layout {
        ":feature:*".module {
            screenFile("commonMain", "*ScreenAction")
        }
    }

    "declares a sealed interface named after the file".konsist {
        files.must { file -> file.interfaces().any { it.name == file.name && it.hasSealedModifier } }
    }
}
