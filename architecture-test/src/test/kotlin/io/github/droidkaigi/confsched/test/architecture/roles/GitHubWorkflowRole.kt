package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.gitHubWorkflow() = "GitHubWorkflow" {
    title = "GitHub Actions"
    summary = "CI のワークフローと複合アクション"
    example("test.yml", "テストの実行")
    layout {
        ".github" {
            "workflows" / "*.yml".file()
            "actions" / "*" / "action.yml".file()
        }
    }
}
