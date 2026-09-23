package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.screenRobotTest() = "ScreenRobotTest" {
    title = "Robot テスト"
    summary = "本物の ScreenRoot を組み立て、Robot を通して画面を検証するテスト"
    example("TimetableScreenRobotTest", "タイムテーブル画面の Robot テスト")
    layout {
        ":feature:*".module {
            screenFile("commonTest", "*ScreenRobotTest")
        }
    }
}
