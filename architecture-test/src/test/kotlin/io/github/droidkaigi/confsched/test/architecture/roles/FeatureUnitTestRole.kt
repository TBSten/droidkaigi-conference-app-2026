package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.featureUnitTest() = "FeatureUnitTest" {
    title = "フィーチャーの単体テスト"
    summary = "画面以外の feature 内ロジック (コンポーネントの計算や補助関数) の単体テスト"
    example("TimetableGridTimeTest", "タイムテーブルの時刻計算")
    layout {
        ":feature:*".module {
            screenFile("commonTest", "*Test")
            screenFile("jvmTest", "*Test")
            ("commonTest".sourceSet / kotlin / modulePackage) {
                "component" / "*Test".ktFile()
                "*/component" / "*Test".ktFile()
            }
        }
    }
}
