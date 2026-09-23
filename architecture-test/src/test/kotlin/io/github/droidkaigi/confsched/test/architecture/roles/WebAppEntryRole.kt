package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.APP_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.webAppEntry() = "WebAppEntry" {
    title = "Web エントリ"
    summary = "Web アプリの main 関数・URL と履歴の同期・ホストページ"
    example("Main", "Web アプリの起動")
    layout {
        ":app-web".module {
            "wasmJsMain".sourceSet {
                kotlin / APP_PACKAGE / "*".ktFile()
                "resources" / "index.html".file()
            }
        }
    }
}
