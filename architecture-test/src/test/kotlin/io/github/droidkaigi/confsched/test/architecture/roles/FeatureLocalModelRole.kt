package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.modulePackage
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.div
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.gradle.sourceSet
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.featureLocalModel() = "FeatureLocalModel" {
    title = "画面ローカルのモデル"
    summary = "1 つの画面の中だけで使う enum や変換関数。component にも :core:model にも属さない小さなもの"
    example("TimetableViewMode", "タイムテーブルの表示形式")
    layout {
        ":feature:sessions".module {
            "commonMain".sourceSet / kotlin / modulePackage / "timetable" {
                "TimetableViewMode".ktFile()
                "SessionCalendarEvent".ktFile()
            }
        }
    }
}
