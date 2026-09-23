package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.screenFile
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.konsist.konsist

fun DeclarationContainerScope.navKey() = "NavKey" {
    title = "NavKey"
    summary = "画面を表す @Serializable な NavKey"
    example("TimetableNavKey", "タイムテーブル画面への遷移先")
    layout {
        ":feature:*".module {
            screenFile("commonMain", "*NavKey")
        }
    }

    "declares a NavKey named after the file".konsist {
        files.must { file ->
            file.objects().any { it.name == file.name && it.parents().any { parent -> parent.name.endsWith("NavKey") } } ||
                file.classes().any { it.name == file.name && it.parents().any { parent -> parent.name.endsWith("NavKey") } }
        }
    }
}
