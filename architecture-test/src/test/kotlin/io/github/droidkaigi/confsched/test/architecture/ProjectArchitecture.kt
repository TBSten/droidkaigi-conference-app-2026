package io.github.droidkaigi.confsched.test.architecture

import io.github.droidkaigi.confsched.test.architecture.groups.appGroup
import io.github.droidkaigi.confsched.test.architecture.groups.buildGroup
import io.github.droidkaigi.confsched.test.architecture.groups.coreGroup
import io.github.droidkaigi.confsched.test.architecture.groups.docsGroup
import io.github.droidkaigi.confsched.test.architecture.groups.featureGroup
import io.github.droidkaigi.confsched.test.architecture.groups.iosGroup
import io.github.droidkaigi.confsched.test.architecture.groups.repositoryGroup
import io.github.droidkaigi.confsched.test.architecture.groups.toolsGroup
import me.tbsten.katachi.dsl.Architecture
import me.tbsten.katachi.dsl.architecture

val projectArchitecture: Architecture = architecture {
    featureGroup()
    coreGroup()
    appGroup()
    iosGroup()
    toolsGroup()
    buildGroup()
    docsGroup()
    repositoryGroup()
}
