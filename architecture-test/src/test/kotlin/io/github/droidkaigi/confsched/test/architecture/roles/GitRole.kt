package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.git() = "Git" {
    title = "Git の設定"
    summary = "Git の管理から外すファイルの指定"
    example(".gitignore", "生成物を Git の管理から外す")
    layout {
        "**/.gitignore".file()
    }
}
