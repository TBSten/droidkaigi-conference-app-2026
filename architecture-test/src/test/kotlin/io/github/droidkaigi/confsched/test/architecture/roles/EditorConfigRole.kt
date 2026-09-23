package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.editorConfig() = "EditorConfig" {
    title = "エディタの設定"
    summary = "インデントなどのエディタ設定。ktlint もこのファイルを読む"
    example(".editorconfig", "ktlint も読むエディタ設定")
    layout {
        ".editorconfig".file()
    }
}
