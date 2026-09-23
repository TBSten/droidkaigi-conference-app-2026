package io.github.droidkaigi.confsched.test.architecture

import me.tbsten.katachi.ExperimentalKatachiApi
import me.tbsten.katachi.check.KonsistCheck
import me.tbsten.katachi.check.assert
import org.junit.jupiter.api.Test

class ProjectArchitectureTest {
    @OptIn(ExperimentalKatachiApi::class)
    @Test
    fun `プロジェクトの構成が定義どおりになっている`() {
        projectArchitecture.assert(KonsistCheck())
    }
}
