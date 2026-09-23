package io.github.droidkaigi.confsched.test.architecture.groups

import io.github.droidkaigi.confsched.test.architecture.roles.agentInstruction
import io.github.droidkaigi.confsched.test.architecture.roles.devScript
import io.github.droidkaigi.confsched.test.architecture.roles.editorConfig
import io.github.droidkaigi.confsched.test.architecture.roles.git
import io.github.droidkaigi.confsched.test.architecture.roles.gitHubTemplate
import io.github.droidkaigi.confsched.test.architecture.roles.gitHubWorkflow
import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.repositoryGroup() = "repository".group {
    title = "リポジトリ"

    gitHubWorkflow()
    gitHubTemplate()
    agentInstruction()
    git()
    editorConfig()
    devScript()
}
