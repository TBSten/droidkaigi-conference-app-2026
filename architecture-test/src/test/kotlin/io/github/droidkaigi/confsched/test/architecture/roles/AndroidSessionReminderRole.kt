package io.github.droidkaigi.confsched.test.architecture.roles

import io.github.droidkaigi.confsched.test.architecture.APP_PACKAGE
import me.tbsten.katachi.dsl.DeclarationContainerScope
import me.tbsten.katachi.dsl.gradle.kotlin
import me.tbsten.katachi.dsl.gradle.mainSourceSet
import me.tbsten.katachi.dsl.gradle.module
import me.tbsten.katachi.dsl.kotlin.ktFile

fun DeclarationContainerScope.androidSessionReminder() = "AndroidSessionReminder" {
    title = "Android のセッションリマインダー"
    summary = "セッション開始前の通知をスケジュールし、受け取るレシーバー"
    example("SessionReminderReceiver", "リマインダーの受信")
    layout {
        ":app-android".module {
            mainSourceSet / kotlin / APP_PACKAGE / "notification" / "*".ktFile()
        }
    }
}
