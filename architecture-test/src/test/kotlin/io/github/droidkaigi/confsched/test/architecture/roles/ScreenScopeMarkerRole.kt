package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.screenScopeMarker() = "ScreenScopeMarker" {
    title = "画面スコープマーカー"
    summary = "画面単位の @GraphExtension が使う DI スコープの marker"
    example("TimetableScreenScope", "タイムテーブル画面のスコープ")
    layout {
        ":core:model".module {
            "commonMain".sourceSet / kotlin / modulePackage / "*ScreenScope".ktFile()
        }
    }
}
