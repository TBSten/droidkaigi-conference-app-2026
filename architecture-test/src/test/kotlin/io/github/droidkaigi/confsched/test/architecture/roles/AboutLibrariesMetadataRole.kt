package io.github.droidkaigi.confsched.test.architecture.roles

import me.tbsten.katachi.dsl.DeclarationContainerScope

fun DeclarationContainerScope.aboutLibrariesMetadata() = "AboutLibrariesMetadata" {
    title = "ライセンスの補足情報"
    summary = "AboutLibraries が自動収集できないライブラリとライセンスの定義"
    documented = false
    example("libraries/lib_noto_sans.json", "Noto Sans の定義")
    layout {
        "config/aboutlibraries" {
            "libraries" / "lib_*.json".file()
            "licenses" / "lic_*.json".file()
        }
    }
}
