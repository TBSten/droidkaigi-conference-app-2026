package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.iconSource() = "IconSource" {
    title = "アイコンの元データ"
    summary = "KaigiIcons の生成元になるベクター画像"
    example("hd_arrow_back.xml", "戻るアイコン")
    layout {
        ":core:designsystem".module {
            "icons" {
                "README.md".file()
                "hd_*.xml".file()
            }
        }
    }
}
