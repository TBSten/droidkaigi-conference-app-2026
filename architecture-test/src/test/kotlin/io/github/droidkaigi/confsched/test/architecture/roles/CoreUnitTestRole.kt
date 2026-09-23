package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.coreTestSourceSets
import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.coreUnitTest() = "CoreUnitTest" {
    title = "core の単体テスト"
    summary = "core モジュールの単体テスト。永続化のテストは jvmTest に置く"
    example("TimetableResponseMapperTest", "レスポンス変換のテスト")
    layout {
        ":core:*".module {
            description = "core モジュールの単体テスト"
            coreTestSourceSets.forEach { sourceSet ->
                sourceSet.sourceSet / kotlin / modulePackage / "*Test".ktFile()
            }
        }
        ":core:preview:wrapper".module {
            description = "プレビューの DI 結線のテスト"
            "androidHostTest".sourceSet / kotlin / modulePackage / "*Test".ktFile()
        }
    }
}
