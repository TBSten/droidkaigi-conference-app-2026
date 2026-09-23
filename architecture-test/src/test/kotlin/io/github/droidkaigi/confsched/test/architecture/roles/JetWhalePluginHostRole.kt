package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.BASE_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.mainSourceSet
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.jetWhalePluginHost() = "JetWhalePluginHost" {
    title = "JetWhale プラグインのホスト"
    summary = "デスクトップのデバッガが読み込むプラグインのビューとマニフェスト"
    example("KaigiClockPluginView", "時計プラグインのビュー")
    layout {
        ":tools:jetwhale-plugin:host".module {
            mainSourceSet {
                kotlin / "$BASE_PACKAGE/jetwhale/host" {
                    "Kaigi*PluginView".ktFile()
                    "KaigiHostPluginFactory".ktFile()
                }
                "resources/META-INF/jetwhale" / "plugin-manifest.json".file()
            }
        }
    }
}
