# バージョンカタログ

依存とプラグインのすべてのバージョンは、信頼できる唯一の情報源である `gradle/libs.versions.toml` にあります。エイリアスは**フラットな camelCase**（`libs.` の下のアクセサは 1 セグメント）なので、`libs.` と入力して名前をあいまい一致させるだけで、あらゆる依存に到達できます — 覚えるべきネストしたプレフィックスはありません。

```toml
[versions]
kotlin = "2.4.0"
soil = "1.0.0-alpha15"
androidxDatastore = "1.3.0-alpha09"

[libraries]
soilQueryCore = { module = "com.soil-kt.soil:query-core", version.ref = "soil" }
soilReacty   = { module = "com.soil-kt.soil:reacty",     version.ref = "soil" }

[plugins]
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
metro               = { id = "dev.zacsweers.metro",               version.ref = "metro" }
```

モジュールは `alias(libs.plugins.*)` でプラグインを適用し、`libs.*` で依存を宣言します。プラグインのバージョンはカタログで一度だけ宣言されるため、`settings.gradle.kts` が `pluginManagement` でそれらを固定することはもうありません。

関連: [Convention プラグイン](./build-convention-plugins.ja.md) · [BuildKonfig（ビルド時の値）](./build-config-buildkonfig.ja.md)
