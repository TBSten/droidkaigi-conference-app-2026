package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.dataInfrastructure() = "DataInfrastructure" {
    title = "データ層の基盤"
    summary = "HTTP クライアント・DataStore・ファイル保存・接続先環境など、:core:data の実装が共有する基盤"
    example("HttpClientBindings", "HTTP クライアントの DI バインディング")
    layout {
        ":core:data".module {
            "commonMain".sourceSet / kotlin / modulePackage / "*".ktFile()
        }
    }
}
