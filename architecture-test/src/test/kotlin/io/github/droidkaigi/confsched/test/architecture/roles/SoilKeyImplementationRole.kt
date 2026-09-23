package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.invoke
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.soilKeyImplementation() = "SoilKeyImplementation" {
    title = "Soil キーの実装"
    summary = ":core:model のキー契約に対する Default 実装。データの整形は fetch の中で行う"
    example("DefaultTimetableQueryKey", "TimetableQueryKey の実装")
    layout {
        ":core:data".module {
            "commonMain".sourceSet / kotlin / modulePackage {
                "Default*QueryKey".ktFile()
                "Default*SubscriptionKey".ktFile()
                "Default*MutationKey".ktFile()
            }
        }
    }
}
