package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.docsSite() = "DocsSite" {
    title = "ドキュメントサイト"
    summary = "docs/ を公開する VitePress サイトの設定"
    example(".vitepress/config.mts", "サイトの設定")
    layout {
        "docs-site" {
            "package.json".file()
            "package-lock.json".file()
            ".vitepress" {
                "config.mts".file()
                "theme" {
                    "index.ts".file()
                    "custom.css".file()
                }
            }
            "scripts" / "*.mjs".file()
        }
    }
}
