package io.github.droidkaigi.confsched.test.architecture.groups

import io.github.droidkaigi.confsched.test.architecture.roles.debugTooling
import io.github.droidkaigi.confsched.test.architecture.roles.featureComponent
import io.github.droidkaigi.confsched.test.architecture.roles.featureDrawableResource
import io.github.droidkaigi.confsched.test.architecture.roles.featureLocalModel
import io.github.droidkaigi.confsched.test.architecture.roles.featureLocalSoilKey
import io.github.droidkaigi.confsched.test.architecture.roles.featureLocalStore
import io.github.droidkaigi.confsched.test.architecture.roles.featurePlatformSource
import io.github.droidkaigi.confsched.test.architecture.roles.featureUnitTest
import io.github.droidkaigi.confsched.test.architecture.roles.navEntryProvider
import io.github.droidkaigi.confsched.test.architecture.roles.navKey
import io.github.droidkaigi.confsched.test.architecture.roles.screen
import io.github.droidkaigi.confsched.test.architecture.roles.screenAction
import io.github.droidkaigi.confsched.test.architecture.roles.screenActionResult
import io.github.droidkaigi.confsched.test.architecture.roles.screenContext
import io.github.droidkaigi.confsched.test.architecture.roles.screenGraph
import io.github.droidkaigi.confsched.test.architecture.roles.screenNavigator
import io.github.droidkaigi.confsched.test.architecture.roles.screenPresenter
import io.github.droidkaigi.confsched.test.architecture.roles.screenPresenterTest
import io.github.droidkaigi.confsched.test.architecture.roles.screenRobot
import io.github.droidkaigi.confsched.test.architecture.roles.screenRobotTest
import io.github.droidkaigi.confsched.test.architecture.roles.screenRoot
import io.github.droidkaigi.confsched.test.architecture.roles.screenTestGraph
import io.github.droidkaigi.confsched.test.architecture.roles.screenUiState
import io.github.droidkaigi.confsched.test.architecture.roles.stringResource
import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.featureGroup() = "feature".group {
    title = "フィーチャー"

    screenRoot()
    screen()
    screenPresenter()
    screenContext()
    screenGraph()
    screenAction()
    screenActionResult()
    screenUiState()
    navKey()
    navEntryProvider()
    screenNavigator()
    featureComponent()
    featureLocalSoilKey()
    featureLocalStore()
    featureLocalModel()
    debugTooling()
    featurePlatformSource()
    featureDrawableResource()
    stringResource()
    screenPresenterTest()
    screenRobot()
    screenRobotTest()
    screenTestGraph()
    featureUnitTest()
}
