package io.github.droidkaigi.confsched.test.architecture.groups

import io.github.droidkaigi.confsched.test.architecture.roles.androidTestConfig
import io.github.droidkaigi.confsched.test.architecture.roles.coreCommonPrimitive
import io.github.droidkaigi.confsched.test.architecture.roles.corePlatformSource
import io.github.droidkaigi.confsched.test.architecture.roles.coreUnitTest
import io.github.droidkaigi.confsched.test.architecture.roles.dataInfrastructure
import io.github.droidkaigi.confsched.test.architecture.roles.designSystemFont
import io.github.droidkaigi.confsched.test.architecture.roles.designSystemTheme
import io.github.droidkaigi.confsched.test.architecture.roles.domainModel
import io.github.droidkaigi.confsched.test.architecture.roles.fakeApi
import io.github.droidkaigi.confsched.test.architecture.roles.iconSource
import io.github.droidkaigi.confsched.test.architecture.roles.openApiSpec
import io.github.droidkaigi.confsched.test.architecture.roles.persistenceStore
import io.github.droidkaigi.confsched.test.architecture.roles.previewApi
import io.github.droidkaigi.confsched.test.architecture.roles.previewFake
import io.github.droidkaigi.confsched.test.architecture.roles.previewImage
import io.github.droidkaigi.confsched.test.architecture.roles.previewWrapper
import io.github.droidkaigi.confsched.test.architecture.roles.responseMapper
import io.github.droidkaigi.confsched.test.architecture.roles.screenScopeMarker
import io.github.droidkaigi.confsched.test.architecture.roles.sharedUiComponent
import io.github.droidkaigi.confsched.test.architecture.roles.soilKeyContract
import io.github.droidkaigi.confsched.test.architecture.roles.soilKeyImplementation
import io.github.droidkaigi.confsched.test.architecture.roles.testFake
import io.github.droidkaigi.confsched.test.architecture.roles.testFixture
import io.github.droidkaigi.confsched.test.architecture.roles.testHarness
import io.github.droidkaigi.confsched.test.architecture.roles.uiDrawableResource
import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.coreGroup() = "core".group {
    title = "コア"

    domainModel()
    soilKeyContract()
    screenScopeMarker()
    soilKeyImplementation()
    persistenceStore()
    responseMapper()
    fakeApi()
    dataInfrastructure()
    openApiSpec()
    coreCommonPrimitive()
    designSystemTheme()
    designSystemFont()
    iconSource()
    sharedUiComponent()
    uiDrawableResource()
    previewFake()
    previewApi()
    previewImage()
    previewWrapper()
    testFake()
    testFixture()
    testHarness()
    androidTestConfig()
    corePlatformSource()
    coreUnitTest()
}
