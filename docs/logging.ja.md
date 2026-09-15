# ロギング（Kermit）

本番のロギングには、KMP ネイティブのロギングライブラリである [Kermit](https://github.com/touchlab/Kermit) を使い、common コードで 1 度だけバインドして、app graph を通じて提供される単一の `Logger` としています。

## Kermit を選ぶ理由

[Kermit](https://github.com/touchlab/Kermit) (Touchlab) は KMP ネイティブのロギングライブラリです。**プラットフォームネイティブの writer を組み込みで** 同梱しており、プラットフォームごとに適切な出力先を自動的に選びます: Android では Logcat、iOS では `os_log`、Desktop (JVM) では SLF4J、Web (wasmJs) では `console` です。

この組み込みのプラットフォームごとの挙動こそが、Kermit を選んだ理由です。common コードにある単一の `co.touchlab.kermit.Logger` のバインディングが、**`expect`/`actual` もプラットフォームごとのプロバイダもなしに** すべてのターゲットをカバーします。これを [`FileStorage` / DataStore の継ぎ目](./platforms-and-modules.ja.md) と対比してください。こちらはプラットフォームごとのプロバイダを *実際に* 必要とします — Android の裏側は `Context` を必要とするため、`:core:data` はプラットフォームのソースセットごとに `actual` と contribute されたバインディングを持ちます。Logger にはそのようなプラットフォームとの結合はありません。

## KaigiLogger ファサード

アプリケーションコードは、Kermit の `Logger` を直接使うのではなく、プロジェクトが所有する `KaigiLogger` インターフェース (`:core:common`) を通じてログを出力します。`KermitKaigiLogger` が唯一の `@SingleIn(AppScope::class)` 実装で、`@ContributesBinding` で contribute され、[`AppGraph`](./di-app-graph.ja.md) に公開されています:

```kotlin
interface KaigiLogger {
    fun debug(message: () -> String)
    fun info(message: () -> String)
    fun warn(message: () -> String)
    fun error(throwable: Throwable?, message: () -> String)
}
```

このファサードは呼び出し箇所を Kermit の API から遠ざけ、静的な抜け道 (`co.touchlab.kermit.Logger.w { … }` は注入を完全に迂回します) を塞ぎます。`AppNavigator` (`:core:common`) が最初の実際の呼び出し箇所で、ナビゲーションのコマンドごとにログを出力します:

```kotlin
class AppNavigator(private val logger: KaigiLogger) : Navigator {
    fun goTo(key: NavKey) {
        logger.debug { "goTo $key" }
        // …
    }
}
```

## クラッシュレポート

`KaigiLogger.error` はさらに `CrashReporter` (`:core:common`) にも転送します。デフォルトのバインディング (`CrashReporterDefaults`) はどこにも報告しません。Android と iOS はそれを、完全に Kotlin で書かれた Firebase Crashlytics の実装で置き換えます。それぞれ、そのプラットフォームのエントリモジュール (`app-android`、`app-ios-kotlin`) にあります。Android では SDK は素の Gradle 依存です。iOS では `swiftPMDependencies` による Swift Package Manager のインポートを通じて宣言され、生成された cinterop が `FIRApp`/`FIRCrashlytics` を `iosMain` に公開します。どちらの reporter も、Firebase プロジェクトの設定が同梱されるまでは no-op のままです。デバイスの外に出るのは error レベルのログだけで、debug/info/warn は注入された `MinLogSeverity` によって本番ではミュートされます (デバッグビルドではこれを Verbose に置き換えます)。

関連: [デバッグ](./debugging.ja.md) · [AppGraph と UiGraph](./di-app-graph.ja.md)
