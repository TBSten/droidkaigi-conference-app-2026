package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import io.github.droidkaigi.confsched.test.architecture.platformSourceSets
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.corePlatformSource() = "CorePlatformSource" {
    title = "core のプラットフォーム実装"
    summary = "commonMain の expect に対する各プラットフォームの actual と、プラットフォーム専用の補助実装"
    example("HttpClientEngineFactory.android.kt", "Android の HTTP エンジン")
    layout {
        ":core:*".module {
            platformSourceSets.forEach { sourceSet ->
                sourceSet.sourceSet / kotlin / modulePackage / "*".ktFile()
            }
        }
    }
}
