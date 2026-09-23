package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.mainSourceSet
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.androidResource() = "AndroidResource" {
    title = "Android リソース"
    summary = "Android アプリのランチャーアイコン・テーマ・文字列・ウィジェット定義"
    example("res/values/themes.xml", "アプリのテーマ")
    // The qualifier directories under res (values-ja, mipmap-hdpi, ...) are named by Android.
    layout {
        ":app-android".module {
            mainSourceSet / "res" / "*" {
                "*.xml".file()
                "*.png".file()
            }
        }
    }
}
