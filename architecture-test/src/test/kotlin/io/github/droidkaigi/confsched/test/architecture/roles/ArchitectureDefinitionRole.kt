package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.BASE_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.testSourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.architectureDefinition() = "ArchitectureDefinition" {
    title = "アーキテクチャ定義"
    summary = "katachi で書いたこのリポジトリの構成の定義と、それを検証するテスト"
    example("roles/ScreenRootRole.kt", "ScreenRoot の役割の宣言")
    layout {
        ":architecture-test".module {
            testSourceSet / kotlin / "$BASE_PACKAGE/test/architecture" {
                "*".ktFile()
                "groups" / "*Group".ktFile()
                "roles" / "*Role".ktFile()
            }
        }
    }
}
