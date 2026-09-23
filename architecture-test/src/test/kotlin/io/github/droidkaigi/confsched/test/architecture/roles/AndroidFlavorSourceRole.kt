package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.APP_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.androidFlavorSource() = "AndroidFlavorSource" {
    title = "Android の flavor 別実装"
    summary = "dev / prod の flavor ごとに差し替える実装"
    example("AndroidCrashReporter", "prod のクラッシュレポート")
    layout {
        ":app-android".module {
            "dev".sourceSet / kotlin / APP_PACKAGE / "*".ktFile()
            "prod".sourceSet / kotlin / APP_PACKAGE / "*".ktFile()
        }
    }
}
