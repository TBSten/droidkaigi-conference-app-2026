package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.gradleModule() = "GradleModule" {
    title = "モジュールのビルドスクリプト"
    summary = "settings.gradle.kts が include する各モジュールの build.gradle.kts"
    documented = false
    example("feature/sessions/build.gradle.kts", ":feature:sessions のビルドスクリプト")
    // One line per include in settings.gradle.kts; only :feature:* is expected to grow.
    layout {
        ":tools:compiler-plugin".module { }
        ":tools:ksp-processor".module { }
        ":tools:jetwhale-plugin:protocol".module { }
        ":tools:jetwhale-plugin:host".module { }
        ":core:model".module { }
        ":core:common".module { }
        ":core:data".module { }
        ":core:designsystem".module { }
        ":core:testing".module { }
        ":core:ui".module { }
        ":core:preview:api".module { }
        ":core:preview:impl".module { }
        ":core:preview:wrapper".module { }
        ":feature:*".module { }
        ":app-shared".module { }
        ":app-ios-kotlin".module { }
        ":app-desktop".module { }
        ":app-web".module { }
        ":app-android".module { }
        ":architecture-test".module { }
    }
}
