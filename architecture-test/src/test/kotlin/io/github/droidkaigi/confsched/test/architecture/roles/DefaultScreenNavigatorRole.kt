package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.APP_PACKAGE
import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile
import me.tbsten.katachi.konsist.konsist

fun DeclarationContainerScope.defaultScreenNavigator() = "DefaultScreenNavigator" {
    title = "画面 Navigator の実装"
    summary = "feature が宣言する画面 Navigator の実装。feature 間の遷移をここで結ぶ"
    example("DefaultTimetableScreenNavigator", "TimetableScreenNavigator の実装")
    layout {
        ":app-shared".module {
            description = "リリースに含まれる画面の Navigator"
            "commonMain".sourceSet / kotlin / APP_PACKAGE / "Default*ScreenNavigator".ktFile()
        }
        ":feature:debug".module {
            description = "開発専用の画面の Navigator。:feature:debug はリリースに含まれないため自身の中に置く"
            "commonMain".sourceSet / kotlin / modulePackage / "Default*ScreenNavigator".ktFile()
        }
    }
    "declares a class implementing the navigator it is named after".konsist {
        files.must { file ->
            file.classes().any { klass ->
                klass.name == file.name && klass.parents().any { parent -> "Default${parent.name}" == file.name }
            }
        }
    }
}
