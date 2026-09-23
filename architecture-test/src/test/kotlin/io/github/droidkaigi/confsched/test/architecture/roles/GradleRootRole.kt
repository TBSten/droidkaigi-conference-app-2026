package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.kotlin.ktsFile

fun DeclarationContainerScope.gradleRoot() = "GradleRoot" {
    title = "ルートのビルドファイル"
    summary = "settings.gradle.kts・ルートの build.gradle.kts・バージョンカタログ・Gradle wrapper"
    documented = false
    example("gradle/libs.versions.toml", "バージョンカタログ")
    layout {
        "settings.gradle".ktsFile()
        "build.gradle".ktsFile()
        "gradle.properties".file()
        "gradlew".file()
        "gradlew.bat".file()
        "gradle" {
            "libs.versions.toml".file()
            "gradle-daemon-jvm.properties".file()
            "wrapper" {
                "gradle-wrapper.jar".file()
                "gradle-wrapper.properties".file()
            }
        }
    }
}
