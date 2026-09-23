package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.screenContext() = "ScreenContext" {
    title = "ScreenContext"
    summary = "画面の ScreenContext と、それが保持する PresenterContext"
    example("TimetableScreenContext", "TimetableScreenContext と TimetablePresenterContext")
    layout {
        ":feature:*".module {
            screenFile("commonMain", "*ScreenContext")
        }
    }
}
