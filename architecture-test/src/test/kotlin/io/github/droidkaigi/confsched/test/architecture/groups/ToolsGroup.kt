package io.github.droidkaigi.confsched.test.architecture.groups

import io.github.droidkaigi.confsched.test.architecture.roles.architectureDefinition
import io.github.droidkaigi.confsched.test.architecture.roles.compilerPluginDiagnosticTestData
import io.github.droidkaigi.confsched.test.architecture.roles.compilerPluginSupport
import io.github.droidkaigi.confsched.test.architecture.roles.compilerPluginTestFixture
import io.github.droidkaigi.confsched.test.architecture.roles.compilerPluginTestStub
import io.github.droidkaigi.confsched.test.architecture.roles.firChecker
import io.github.droidkaigi.confsched.test.architecture.roles.firDiagnosticErrors
import io.github.droidkaigi.confsched.test.architecture.roles.jetWhalePluginHost
import io.github.droidkaigi.confsched.test.architecture.roles.jetWhalePluginProtocol
import io.github.droidkaigi.confsched.test.architecture.roles.kspProcessor
import io.github.droidkaigi.confsched.test.architecture.roles.serviceRegistration
import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.toolsGroup() = "tools".group {
    title = "ツール"

    firChecker()
    firDiagnosticErrors()
    compilerPluginSupport()
    compilerPluginDiagnosticTestData()
    compilerPluginTestStub()
    compilerPluginTestFixture()
    serviceRegistration()
    kspProcessor()
    jetWhalePluginProtocol()
    jetWhalePluginHost()
    architectureDefinition()
}
