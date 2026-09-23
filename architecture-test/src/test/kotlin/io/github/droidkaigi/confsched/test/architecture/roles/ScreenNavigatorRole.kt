package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.konsist.konsist

fun DeclarationContainerScope.screenNavigator() = "ScreenNavigator" {
    title = "画面 Navigator"
    summary = "画面から他画面への遷移を宣言する Navigator インターフェイス。実装は app-shared に置く"
    example("TimetableScreenNavigator", "セッション詳細を開く")
    layout {
        ":feature:*".module {
            screenFile("commonMain", "*ScreenNavigator")
        }
    }

    // `*ScreenNavigator` also matches the Default implementations :feature:debug keeps beside their interfaces.
    "declares a Navigator interface named after the file".konsist {
        files.filterNot { it.name.startsWith("Default") }.must { file ->
            file.interfaces().any { it.name == file.name && it.parents().any { parent -> parent.name == "Navigator" } }
        }
    }
}
