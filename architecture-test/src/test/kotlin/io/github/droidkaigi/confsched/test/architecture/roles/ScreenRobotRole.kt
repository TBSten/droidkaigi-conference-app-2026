package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.screenRobot() = "ScreenRobot" {
    title = "Robot"
    summary = "画面テストの操作と検証を画面ごとの語彙で包む Robot"
    example("TimetableScreenRobot", "タイムテーブル画面の Robot")
    layout {
        ":feature:*".module {
            screenFile("commonTest", "*ScreenRobot")
        }
    }
}
