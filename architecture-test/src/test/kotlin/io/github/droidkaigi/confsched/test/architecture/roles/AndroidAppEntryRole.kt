package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.APP_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.mainSourceSet
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.androidAppEntry() = "AndroidAppEntry" {
    title = "Android エントリ"
    summary = "Android アプリの Application と Activity、およびそれらが使う依存"
    example("MainActivity", "起動時の Activity")
    layout {
        ":app-android".module {
            mainSourceSet / kotlin / APP_PACKAGE / "*".ktFile()
        }
    }
}
