package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.APP_PACKAGE
import io.github.droidkaigi.confsched.test.architecture.platformSourceSets
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.appSharedPlatformSource() = "AppSharedPlatformSource" {
    title = "アプリシェルのプラットフォーム実装"
    summary = "アプリシェルの expect に対する各プラットフォームの actual と、プラットフォーム専用の実装"
    example("DefaultNotificationPermissionMutationKey", "通知権限の要求")
    layout {
        ":app-shared".module {
            platformSourceSets.forEach { sourceSet ->
                sourceSet.sourceSet / kotlin / APP_PACKAGE / "*".ktFile()
            }
        }
    }
}
