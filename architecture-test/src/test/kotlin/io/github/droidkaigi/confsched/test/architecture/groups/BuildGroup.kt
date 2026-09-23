package io.github.droidkaigi.confsched.test.architecture.groups

import io.github.droidkaigi.confsched.test.architecture.roles.aboutLibrariesMetadata
import io.github.droidkaigi.confsched.test.architecture.roles.buildLogicSupport
import io.github.droidkaigi.confsched.test.architecture.roles.conventionPlugin
import io.github.droidkaigi.confsched.test.architecture.roles.gradleModule
import io.github.droidkaigi.confsched.test.architecture.roles.gradleRoot
import io.github.droidkaigi.confsched.test.architecture.roles.jsLockfile
import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.buildGroup() = "build".group {
    title = "ビルド"
    documented = false

    gradleRoot()
    gradleModule()
    conventionPlugin()
    buildLogicSupport()
    jsLockfile()
    aboutLibrariesMetadata()
}
