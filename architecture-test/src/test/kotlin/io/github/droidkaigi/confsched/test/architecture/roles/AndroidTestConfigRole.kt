package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet

fun DeclarationContainerScope.androidTestConfig() = "AndroidTestConfig" {
    title = "Android テスト設定"
    summary = "Robolectric で Robot テストを動かすための Android の設定"
    example("robolectric.properties", "Robolectric の設定")
    layout {
        ":core:testing".module {
            "androidMain".sourceSet {
                "AndroidManifest.xml".file()
                "resources" / "robolectric.properties".file()
            }
        }
    }
}
