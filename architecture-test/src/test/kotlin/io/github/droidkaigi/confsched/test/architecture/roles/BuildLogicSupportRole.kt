package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.kotlin.ktFile
import me.tbsten.katachi.dsl.kotlin.ktsFile

fun DeclarationContainerScope.buildLogicSupport() = "BuildLogicSupport" {
    title = "ビルドロジックの補助"
    summary = "コンベンションプラグインが使う Gradle タスクと DSL の補助、およびインクルードビルド自身のビルドファイル"
    documented = false
    example("util/VersionCatalogDsl.kt", "バージョンカタログの参照")
    // gradle-conventions holds its own settings file, so it is an included build rather than a module.
    layout {
        "gradle-conventions" {
            "settings.gradle".ktsFile()
            "build.gradle".ktsFile()
            "src/main/kotlin" {
                "droidkaigi" / "*".ktFile()
                "droidkaigi" / "*" / "*".ktFile()
                "util" / "*".ktFile()
            }
        }
    }
}
