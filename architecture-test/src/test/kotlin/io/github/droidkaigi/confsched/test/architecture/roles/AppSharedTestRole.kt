package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.APP_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.appSharedTest() = "AppSharedTest" {
    title = "アプリシェルのテスト"
    summary = "ディープリンクやバックスタックなど、アプリシェルの単体テスト"
    example("DeepLinkParseTest", "ディープリンクの解析")
    layout {
        ":app-shared".module {
            "commonTest".sourceSet / kotlin / APP_PACKAGE / "*Test".ktFile()
        }
    }
}
