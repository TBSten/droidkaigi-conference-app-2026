package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.stringResources
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.stringResource() = "StringResource" {
    title = "文字列リソース"
    summary = "Compose Resources の strings.xml。英語の values と日本語の values-ja を対で置く"
    example("values-ja/strings.xml", "日本語の文字列")
    layout {
        ":feature:*".module {
            description = "その feature の画面が表示する文字列。開発専用の :feature:debug は持たない"
            stringResources(required = false)
        }
        ":core:ui".module {
            description = "複数の feature から使う共有コンポーネントの文字列"
            stringResources()
        }
        ":app-shared".module {
            description = "アプリシェルの文字列"
            stringResources()
        }
    }
}
