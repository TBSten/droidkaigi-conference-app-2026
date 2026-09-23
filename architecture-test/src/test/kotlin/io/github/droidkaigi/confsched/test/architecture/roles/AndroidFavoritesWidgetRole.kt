package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.APP_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.mainSourceSet
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.androidFavoritesWidget() = "AndroidFavoritesWidget" {
    title = "Android のお気に入りウィジェット"
    summary = "ホーム画面に置くお気に入りセッションのウィジェット"
    example("FavoritesWidget", "ウィジェット本体")
    layout {
        ":app-android".module {
            mainSourceSet / kotlin / APP_PACKAGE / "widget" / "*".ktFile()
        }
    }
}
