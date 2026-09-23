package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.invoke
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile
import me.tbsten.katachi.konsist.konsist

fun DeclarationContainerScope.soilKeyContract() = "SoilKeyContract" {
    title = "Soil キーの契約"
    summary = "QueryKey / SubscriptionKey / MutationKey の typealias。1 ファイルに 1 キーを置き、実装は :core:data に置く"
    example("TimetableQueryKey", "タイムテーブルの取得")
    example("FavoriteTimetableItemIdMutationKey", "お気に入りの切り替え")
    layout {
        ":core:model".module {
            "commonMain".sourceSet / kotlin / modulePackage {
                "*QueryKey".ktFile()
                "*SubscriptionKey".ktFile()
                "*MutationKey".ktFile()
            }
        }
    }
    "declares a typealias named after the file".konsist {
        files.must { file -> file.typeAliases.any { it.name == file.name } }
    }
}
