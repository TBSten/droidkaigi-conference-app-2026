package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.APP_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.desktopAppEntry() = "DesktopAppEntry" {
    title = "デスクトップエントリ"
    summary = "デスクトップアプリの main 関数"
    example("Main", "デスクトップアプリの起動")
    layout {
        ":app-desktop".module {
            "jvmMain".sourceSet / kotlin / APP_PACKAGE / "*".ktFile()
        }
    }
}
