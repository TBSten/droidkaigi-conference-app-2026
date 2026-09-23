package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.devScript() = "DevScript" {
    title = "開発用スクリプト"
    summary = "フォントの生成・モジュールグラフの生成・画面の雛形作成など、開発者が手元で実行するスクリプト"
    example("new-screen.sh", "画面の雛形を作る")
    layout {
        "scripts" {
            "*.sh".file()
            "*.mjs".file()
        }
    }
}
