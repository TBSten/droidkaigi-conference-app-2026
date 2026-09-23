package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.invoke
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.domainModel() = "DomainModel" {
    title = "ドメインモデル"
    summary = ":core:model のドメインモデルとデータ層の契約。どのモジュールにも依存しない"
    example("TimetableItem", "セッション")
    layout {
        ":core:model".module {
            "commonMain".sourceSet / kotlin / modulePackage {
                "*".ktFile()
                "buildconfig" / "*".ktFile()
            }
        }
    }
}
