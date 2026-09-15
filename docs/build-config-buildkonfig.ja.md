# BuildKonfig（ビルド時の値）

`droidkaigi.primitive.buildkonfig` [convention プラグイン](./build-convention-plugins.ja.md)は `gradle-conventions` にあり、`:app-shared` がこれを適用し、About 画面とデバッグ画面が `BuildConfigProvider` を通して実際のバージョンを表示します。

## common コードにおけるビルド時の値

一部の値はビルド（Gradle）に由来します — アプリのバージョン、ビルドフラグ、環境設定 — が、すべてのプラットフォームで common な Kotlin から読める必要があります。Android の `BuildConfig` は KMP の `commonMain` では利用できません。

`com.codingfeline.buildkonfig` は `commonMain` に `BuildKonfig` オブジェクトを生成し、ビルド時の状態がアプリコードへ出ていくための、適切に型付けされた唯一の出口を与えます。バージョンのフィールドは[バージョンカタログ](./build-version-catalog.ja.md)から供給され、ビルドとアプリを単一のソースに保ちます。

```kotlin
// gradle-conventions/src/main/kotlin/droidkaigi/primitive/buildkonfig.gradle.kts
buildkonfig {
    packageName = "io.github.droidkaigi.confsched"
    defaultConfigs {
        buildConfigField(STRING, "versionName", libs.versions.droidkaigiApp.get())
    }
}
```

```toml
# gradle/libs.versions.toml — the version BuildKonfig publishes to common code
droidkaigiApp = "0.1.0"
```

## アプリ側: インタフェース経由で読む

生成された `BuildKonfig` を直接使うのではなく、`BuildConfigProvider` インタフェース（`:core:model` にあります）を通して読み、feature がインタフェースだけに依存するようにします。`:app-shared` がプラグインを適用して実装を contribute します。About 画面とデバッグ画面は、この方法で実際のバージョンを表示します。

```kotlin
@ContributesBinding(AppScope::class)
@Inject
class DefaultBuildConfigProvider : BuildConfigProvider {
    override val versionName: String get() = BuildKonfig.versionName
}
```

## 信頼できる唯一の情報源

- アプリが表示するバージョンは `libs.versions.toml` の 1 箇所（`droidkaigiApp`）にあり、すべてのプラットフォームが同じ文字列を表示します。
- Android 自身の `versionName` — OS が報告するもの — は `app-android/build.gradle.kts` で設定される、別の値です。
- その他の値（API のベース URL、…）も同じ方法で `commonMain` に流し込めます。

関連: [Convention プラグイン](./build-convention-plugins.ja.md) · [バージョンカタログ](./build-version-catalog.ja.md)
