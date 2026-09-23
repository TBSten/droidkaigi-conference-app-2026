package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.ENFORCEMENT_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.compilerPluginTestFixture() = "CompilerPluginTestFixture" {
    title = "診断テストの基盤"
    summary = "診断テストの生成・実行・環境設定"
    example("GenerateTests", "テストクラスの生成")
    layout {
        ":tools:compiler-plugin".module {
            "test-fixtures" / ENFORCEMENT_PACKAGE {
                "GenerateTests".ktFile()
                "runners" / "Abstract*Test".ktFile()
                "services" / "*".ktFile()
            }
        }
    }
}
