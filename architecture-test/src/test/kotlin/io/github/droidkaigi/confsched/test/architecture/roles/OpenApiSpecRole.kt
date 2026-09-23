package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.module

fun DeclarationContainerScope.openApiSpec() = "OpenApiSpec" {
    title = "OpenAPI 定義"
    summary = "API クライアントを生成する OpenAPI 定義と生成テンプレート"
    example("openapi.yml", "API 定義")
    layout {
        ":core:data".module {
            "openapi" {
                "openapi.yml".file()
                "templates" / "*.mustache".file()
            }
        }
    }
}
