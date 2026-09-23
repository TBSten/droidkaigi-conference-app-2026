package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.APP_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.testSourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.androidAppTest() = "AndroidAppTest" {
    title = "Android アプリのテスト"
    summary = "Android アプリモジュールの単体テスト"
    example("FavoritesWidgetRenderTest", "ウィジェットの描画")
    layout {
        ":app-android".module {
            testSourceSet / kotlin / APP_PACKAGE {
                "*Test".ktFile()
                "*" / "*Test".ktFile()
            }
        }
    }
}
