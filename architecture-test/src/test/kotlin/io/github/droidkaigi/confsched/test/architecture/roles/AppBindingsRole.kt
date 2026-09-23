package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.APP_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.mainSourceSet
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.appBindings() = "AppBindings" {
    title = "アプリのバインディング"
    summary = "アプリのグラフへ寄与するプラットフォームごとの DI バインディング"
    example("AndroidLicensesBindings", "Android のライセンス情報")
    layout {
        ":app-shared".module {
            description = "全プラットフォーム共通のバインディング"
            "commonMain".sourceSet / kotlin / APP_PACKAGE / "*Bindings".ktFile()
        }
        ":app-android".module {
            description = "Android のバインディング。flavor 固有のものは flavor のソースセットに置く"
            mainSourceSet / kotlin / APP_PACKAGE / "*Bindings".ktFile()
            "dev".sourceSet / kotlin / APP_PACKAGE / "*Bindings".ktFile()
        }
        ":app-desktop".module {
            description = "デスクトップのバインディング"
            "jvmMain".sourceSet / kotlin / APP_PACKAGE / "*Bindings".ktFile()
        }
        ":app-web".module {
            description = "Web のバインディング"
            "wasmJsMain".sourceSet / kotlin / APP_PACKAGE / "*Bindings".ktFile()
        }
        ":app-ios-kotlin".module {
            description = "iOS のバインディング"
            "iosMain".sourceSet / kotlin / APP_PACKAGE / "*Bindings".ktFile()
        }
    }
}
