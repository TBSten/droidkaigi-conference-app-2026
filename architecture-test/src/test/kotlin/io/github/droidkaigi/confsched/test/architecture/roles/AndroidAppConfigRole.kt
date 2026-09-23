package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.mainSourceSet
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.androidAppConfig() = "AndroidAppConfig" {
    title = "Android アプリの設定"
    summary = "マニフェスト・R8 のルール・リリース用スクリプト"
    example("AndroidManifest.xml", "アプリのマニフェスト")
    layout {
        ":app-android".module {
            mainSourceSet / "AndroidManifest.xml".file()
            "proguard-rules.pro".file()
            "release.sh".file()
        }
    }
}
