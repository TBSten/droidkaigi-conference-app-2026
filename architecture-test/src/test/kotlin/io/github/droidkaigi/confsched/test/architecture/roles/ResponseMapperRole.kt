package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.responseMapper() = "ResponseMapper" {
    title = "レスポンスマッパー"
    summary = "API のレスポンスをドメインモデルへ変換する関数"
    example("TimetableResponseMapper", "タイムテーブルのレスポンス変換")
    layout {
        ":core:data".module {
            "commonMain".sourceSet / kotlin / modulePackage / "*ResponseMapper".ktFile()
        }
    }
}
