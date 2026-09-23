package io.github.droidkaigi.confsched.test.architecture.groups

import io.github.droidkaigi.confsched.test.architecture.roles.iosAppAsset
import io.github.droidkaigi.confsched.test.architecture.roles.iosBuildScript
import io.github.droidkaigi.confsched.test.architecture.roles.iosFavoritesWidget
import io.github.droidkaigi.confsched.test.architecture.roles.iosFavoritesWidgetTest
import io.github.droidkaigi.confsched.test.architecture.roles.iosKmpLinkedPackage
import io.github.droidkaigi.confsched.test.architecture.roles.iosProjectConfig
import io.github.droidkaigi.confsched.test.architecture.roles.iosSwiftApp
import io.github.droidkaigi.confsched.test.architecture.roles.swiftPmLock
import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.iosGroup() = "ios".group {
    title = "iOS"

    iosSwiftApp()
    iosAppAsset()
    iosFavoritesWidget()
    iosFavoritesWidgetTest()
    iosProjectConfig()
    iosKmpLinkedPackage()
    swiftPmLock()
    iosBuildScript()
}
