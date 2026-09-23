package io.github.droidkaigi.confsched.test.architecture.groups

import io.github.droidkaigi.confsched.test.architecture.roles.androidAppConfig
import io.github.droidkaigi.confsched.test.architecture.roles.androidAppEntry
import io.github.droidkaigi.confsched.test.architecture.roles.androidAppTest
import io.github.droidkaigi.confsched.test.architecture.roles.androidFavoritesWidget
import io.github.droidkaigi.confsched.test.architecture.roles.androidFlavorSource
import io.github.droidkaigi.confsched.test.architecture.roles.androidResource
import io.github.droidkaigi.confsched.test.architecture.roles.androidSessionReminder
import io.github.droidkaigi.confsched.test.architecture.roles.appBindings
import io.github.droidkaigi.confsched.test.architecture.roles.appGraph
import io.github.droidkaigi.confsched.test.architecture.roles.appSharedPlatformSource
import io.github.droidkaigi.confsched.test.architecture.roles.appSharedTest
import io.github.droidkaigi.confsched.test.architecture.roles.appShell
import io.github.droidkaigi.confsched.test.architecture.roles.defaultScreenNavigator
import io.github.droidkaigi.confsched.test.architecture.roles.desktopAppEntry
import io.github.droidkaigi.confsched.test.architecture.roles.iosKotlinEntry
import io.github.droidkaigi.confsched.test.architecture.roles.webAppEntry
import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.appGroup() = "app".group {
    title = "アプリ"

    appShell()
    appGraph()
    appBindings()
    defaultScreenNavigator()
    appSharedPlatformSource()
    appSharedTest()
    androidAppEntry()
    androidSessionReminder()
    androidFavoritesWidget()
    androidFlavorSource()
    androidAppTest()
    androidResource()
    androidAppConfig()
    desktopAppEntry()
    webAppEntry()
    iosKotlinEntry()
}
