package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.mainSourceSet
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.serviceRegistration() = "ServiceRegistration" {
    title = "サービス登録"
    summary = "コンパイラプラグインと KSP プロセッサを ServiceLoader に登録するファイル"
    example("org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar", "コンパイラプラグインの登録")
    layout {
        ":tools:compiler-plugin".module {
            description = "コンパイラプラグインの登録"
            mainSourceSet / "resources/META-INF/services" / "*".file()
        }
        ":tools:ksp-processor".module {
            description = "KSP プロセッサの登録"
            mainSourceSet / "resources/META-INF/services" / "*".file()
        }
    }
}
