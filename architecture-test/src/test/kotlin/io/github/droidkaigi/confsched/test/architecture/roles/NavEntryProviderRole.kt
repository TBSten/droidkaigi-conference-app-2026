package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.navEntryProvider() = "NavEntryProvider" {
    title = "NavEntryProvider"
    summary = "NavKey を画面ルートへ結び付け、UiScope に集約されるエントリ"
    example("TimetableNavEntryProvider", "TimetableNavKey のエントリ")
    layout {
        ":feature:*".module {
            screenFile("commonMain", "*NavEntryProvider")
        }
    }
}
