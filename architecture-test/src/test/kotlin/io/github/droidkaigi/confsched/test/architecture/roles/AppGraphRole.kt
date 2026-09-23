package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.APP_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.mainSourceSet
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.appGraph() = "AppGraph" {
    title = "アプリグラフ"
    summary = "アプリ全体の DI グラフの契約と、各プラットフォームでの実現"
    example("AppGraph", "共通のグラフの契約")
    example("AndroidAppGraph", "Android でのグラフ")
    layout {
        ":app-shared".module {
            description = "全プラットフォーム共通のグラフの契約"
            "commonMain".sourceSet / kotlin / APP_PACKAGE {
                "AppGraph".ktFile()
                "UiGraph".ktFile()
            }
        }
        ":app-android".module {
            description = "Android のグラフ"
            mainSourceSet / kotlin / APP_PACKAGE / "AndroidAppGraph".ktFile()
        }
        ":app-desktop".module {
            description = "デスクトップのグラフ"
            "jvmMain".sourceSet / kotlin / APP_PACKAGE / "DesktopAppGraph".ktFile()
        }
        ":app-web".module {
            description = "Web のグラフ"
            "wasmJsMain".sourceSet / kotlin / APP_PACKAGE / "WebAppGraph".ktFile()
        }
        ":app-ios-kotlin".module {
            description = "iOS のグラフ"
            "iosMain".sourceSet / kotlin / APP_PACKAGE / "IosAppGraph".ktFile()
        }
    }
}
