package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.konsist.konsist

fun DeclarationContainerScope.screenActionResult() = "ScreenActionResult" {
    title = "ActionResult"
    summary = "Presenter から Root へ一度だけ届ける結果の sealed interface"
    example("TimetableScreenActionResult", "メッセージ表示")
    layout {
        ":feature:*".module {
            screenFile("commonMain", "*ScreenActionResult")
        }
    }

    "declares a sealed interface named after the file".konsist {
        files.must { file -> file.interfaces().any { it.name == file.name && it.hasSealedModifier } }
    }
}
