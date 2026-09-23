package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.repositoryGuide() = "RepositoryGuide" {
    title = "リポジトリの案内"
    summary = "README・コントリビューションガイド・ライセンスと、それらが使う画像"
    example("README.md", "リポジトリの紹介")
    layout {
        "README.md".file()
        "README.ja.md".file()
        "CONTRIBUTING.md".file()
        "CONTRIBUTING.ja.md".file()
        "LICENSE".file()
        "assets" {
            "*.png".file()
            "contributing" / "*.png".file()
        }
    }
}
