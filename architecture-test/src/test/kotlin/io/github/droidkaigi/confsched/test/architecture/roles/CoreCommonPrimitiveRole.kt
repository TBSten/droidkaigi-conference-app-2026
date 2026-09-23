package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.coreCommonPrimitive() = "CoreCommonPrimitive" {
    title = "画面の基盤プリミティブ"
    summary = "すべての feature が使う、ロールコンテキスト・ScreenChannel・ナビゲーション基盤などのプリミティブ"
    example("ScreenChannel", "Action と ActionResult の受け渡し")
    example("Navigator", "画面 Navigator の基底インターフェイス")
    layout {
        ":core:common".module {
            "commonMain".sourceSet / kotlin / modulePackage / "*".ktFile()
        }
    }
}
