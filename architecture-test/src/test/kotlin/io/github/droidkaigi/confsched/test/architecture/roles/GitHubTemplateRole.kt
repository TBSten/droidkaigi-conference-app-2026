package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.gitHubTemplate() = "GitHubTemplate" {
    title = "GitHub のテンプレート"
    summary = "Issue と Pull Request のテンプレート"
    example("PULL_REQUEST_TEMPLATE.md", "Pull Request のテンプレート")
    layout {
        ".github" {
            "ISSUE_TEMPLATE" / "*.md".file()
            "PULL_REQUEST_TEMPLATE.md".file()
        }
    }
}
