package io.github.droidkaigi.confsched.test.architecture.groups

import io.github.droidkaigi.confsched.test.architecture.roles.docsSite
import io.github.droidkaigi.confsched.test.architecture.roles.documentation
import io.github.droidkaigi.confsched.test.architecture.roles.repositoryGuide
import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.docsGroup() = "docs".group {
    title = "ドキュメント"

    documentation()
    docsSite()
    repositoryGuide()
}
