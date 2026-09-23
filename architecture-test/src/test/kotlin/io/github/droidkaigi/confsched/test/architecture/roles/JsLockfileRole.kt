package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.jsLockfile() = "JsLockfile" {
    title = "Kotlin/Wasm のロックファイル"
    summary = "Kotlin Gradle プラグインが管理する wasm ターゲットの yarn.lock"
    documented = false
    example("yarn.lock", "npm 依存の解決結果")
    layout {
        "kotlin-js-store/wasm" / "yarn.lock".file()
    }
}
