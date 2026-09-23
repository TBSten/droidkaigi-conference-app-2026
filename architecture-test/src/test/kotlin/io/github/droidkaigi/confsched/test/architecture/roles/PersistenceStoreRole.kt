package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.invoke
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.persistenceStore() = "PersistenceStore" {
    title = "永続化ストア"
    summary = "DataStore に保存する値のストアと、その DataStore を区別する qualifier"
    example("FavoritesStore", "お気に入りの保存")
    layout {
        ":core:data".module {
            "commonMain".sourceSet / kotlin / modulePackage {
                "*Store".ktFile()
                "*DataStoreQualifier".ktFile()
            }
        }
    }
}
