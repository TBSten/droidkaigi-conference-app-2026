package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.BASE_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.jetWhalePluginProtocol() = "JetWhalePluginProtocol" {
    title = "JetWhale プラグインのプロトコル"
    summary = "アプリとデスクトップのデバッガの間で送るメッセージ"
    example("KaigiClockMessages", "時計プラグインのメッセージ")
    layout {
        ":tools:jetwhale-plugin:protocol".module {
            "commonMain".sourceSet / kotlin / "$BASE_PACKAGE/jetwhale/protocol" {
                "Kaigi*Messages".ktFile()
                "KaigiPluginId".ktFile()
            }
        }
    }
}
