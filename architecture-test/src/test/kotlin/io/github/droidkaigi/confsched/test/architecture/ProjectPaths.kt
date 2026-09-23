package io.github.droidkaigi.confsched.test.architecture

import me.tbsten.katachi.dsl.LayoutDirectoryScope
import me.tbsten.katachi.dsl.gradle.ModulePackage
import me.tbsten.katachi.dsl.gradle.capitalizedModuleNamePackage
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.invoke
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.sourceSet

const val BASE_PACKAGE = "io/github/droidkaigi/confsched"

// The :app-* modules share one package instead of one derived from their module paths.
const val APP_PACKAGE = "$BASE_PACKAGE/app"

const val ENFORCEMENT_PACKAGE = "$BASE_PACKAGE/enforcement"

const val KSP_PACKAGE = "$BASE_PACKAGE/ksp"

val modulePackage: ModulePackage = capitalizedModuleNamePackage("io.github.droidkaigi.confsched")

// :core:preview:api owns the parent package that :impl and :wrapper extend.
val previewApiPackage: ModulePackage = ModulePackage { "$BASE_PACKAGE/core/preview" }

val platformSourceSets: List<String> = listOf("androidMain", "iosMain", "jvmMain", "wasmJsMain")

val coreTestSourceSets: List<String> = listOf("commonTest", "jvmTest", "iosTest")

/**
 * Declares [fileName] in the module package of a feature, or in one screen subpackage below it
 * (`feature/sessions/timetable`).
 */
fun LayoutDirectoryScope.screenFile(sourceSet: String, fileName: String) {
    (sourceSet.sourceSet / kotlin / modulePackage) {
        "$fileName.kt".file()
        "*" / "$fileName.kt".file()
    }
}

fun LayoutDirectoryScope.stringResources(required: Boolean = true) {
    "commonMain".sourceSet / "composeResources" {
        listOf("values", "values-ja").forEach { directory ->
            val strings = directory / "strings.xml".file()
            if (!required) strings.optional()
        }
    }
}
