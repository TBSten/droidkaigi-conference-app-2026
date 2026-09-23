package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.iosBuildScript() = "IosBuildScript" {
    title = "iOS のビルドスクリプト"
    summary = "Xcode のビルドから呼ぶ補助スクリプト"
    example("generate-swift-package-licenses.py", "Swift パッケージのライセンス一覧の生成")
    layout {
        "app-ios/scripts" / "*.py".file()
    }
}
