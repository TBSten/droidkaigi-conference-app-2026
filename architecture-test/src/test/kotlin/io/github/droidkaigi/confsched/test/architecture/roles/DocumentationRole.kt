package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.documentation() = "Documentation" {
    title = "ドキュメント"
    summary = "アーキテクチャと開発手順のドキュメント。英語の <topic>.md と日本語の <topic>.ja.md を対で置く"
    example("building-a-screen.md", "画面の作り方")
    layout {
        "docs" {
            "*.md".file()
            "images" / "*.png".file()
        }
    }
}
