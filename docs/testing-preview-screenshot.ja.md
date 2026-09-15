# プレビュースクリーンショットテスト

Compose の `@Preview` はスクリーンショットテストも兼ねます。**ComposablePreviewScanner** がすべての `@Preview` を検出し、**Roborazzi** がそれぞれを **Compose Desktop（JVM）ターゲット**上で描画して、記録済みの golden 画像と比較します。このパイプラインは素の JVM テストとして実行されます — デバイスもエミュレータも Robolectric のサンドボックスも不要で、他のターゲットも使いません。

[Robot パターンテスト](./testing-robot.ja.md)のシナリオも同じタスクで撮影され、`itShould` ごとに 1 枚の画像が得られます。そのため、プレビューでは到達できない状態 — loading、error、そしてタップがたどり着く先のすべて — もカバーされます。

## 配線の仕組み

すべての feature モジュールが対象です。`droidkaigi.convention.kmp-feature` convention が `droidkaigi.primitive.screenshot-test` primitive を適用するため（[Convention プラグイン](./build-convention-plugins.ja.md)）、モジュール側は 1 行も書く必要がありません。

```kotlin
// droidkaigi.convention.kmp-feature (excerpt)
plugins {
    id("droidkaigi.primitive.kmp")
    id("droidkaigi.primitive.kmp.compose")
    id("droidkaigi.primitive.screenshot-test")
    …
}
```

primitive は、そうでなければモジュールがコピーすることになるものをすべて引き受けます。

- Roborazzi の Gradle プラグインを適用します（record / verify / compare のタスク）、
- `jvmTest` の依存関係を追加します（`:core:testing`、`:core:preview:impl`、および Roborazzi プラグインがモジュール自体に対して検証する成果物）、
- Roborazzi の **`generateComposePreviewDesktopTests`** を、モジュールのパッケージ（プロジェクトのパスから導出され、`android { namespace }` の慣習に一致します）とともに有効にします。

```kotlin
// droidkaigi.primitive.screenshot-test (excerpt)
generateComposePreviewDesktopTests {
    enable.set(true)
    packages.set(listOf(screenshotPackage))
    includePrivatePreviews.set(true)
}
```

パラメータ化されたテストクラスは Roborazzi プラグイン自身が生成します（`build/generated/roborazzi/preview-screenshot/` 配下）— プロジェクトはテストクラスのテンプレートを保守しません。生成されたテストは、共有の robot テストや presenter テストの隣で、モジュールの `jvmTest` ソースセットに加わります。

## 検出

プレビューの検出と撮影は Roborazzi のデフォルトの desktop tester が行い、プロジェクトは独自の tester を保守しません。これは `androidx.compose.ui.tooling.preview.Preview` をスキャンします。これは Compose Multiplatform のプレビューが付けているアノテーションです（[プレビューとサンプルアセット](./preview.ja.md) を参照）。

`ComposablePreviewScanner` は ClassGraph ベースなので、JVM のクラスパス上の**コンパイル済みクラス**をスキャンします。`jvmTest` のクラスパスには JVM ターゲットのコンパイル出力が含まれ、そこには `commonMain` が含まれるため、ソースセットの可視性に関する回避策なしで `commonMain` のプレビューが見えます。

すべてのプレビューは `private` であり、スキャナは指示がない限り private なメソッドをスキップします。そのため上記の `includePrivatePreviews.set(true)` は欠かせません。これがないとモジュールのプレビューは 1 つも検出されず、生成されたテストクラスは `No tests found` で実行に失敗します。

## `@PreviewParameter` の展開

`TimetableScreenPreview` は `@PreviewParameter(KaigiSchemeProvider::class)` のカラースキームを受け取ります（[プレビューとサンプルアセット](./preview.ja.md) を参照）。スキャナは `@PreviewParameter` を尊重し、その単一の `@Preview` を **`KaigiColorScheme` ごとに 1 つの `ComposablePreview`** へ展開します — 5 つのパラメータ化されたケースとなり、5 枚の golden（`…TimetableScreenPreview_0.png` … `_4.png`）を生成します。

## タスク

各タスクはすべての feature モジュールにまたがって実行されます。1 つに絞るには、プロジェクトのパス（`:feature:sessions:…`）を接頭辞として付けてください。

| タスク | 目的 |
| --- | --- |
| `recordRoborazziJvm` | プレビューを描画し、golden を（再）書き込みます。 |
| `verifyRoborazziJvm` | 描画し、記録済みの golden との間にピクセル差分があれば失敗します。 |
| `compareRoborazziJvm` | 描画・比較し、差分画像を出力します（ビルドは失敗しません）。 |

golden は `<module>/build/outputs/roborazzi/` に書き出され、コミットされません。そのため `verify` は比較対象として `record` の実行を必要とします。CI の golden ストアは未決の事項です。プレビューは既にサンプルデータと `PreviewImageResolver` を注入しているため、スクリーンショットは決定的でネットワークを必要としません — [プレビュー画像 enum の生成](./preview-image-enum.ja.md) を参照してください。

## ターゲットは 1 つだけ

スクリーンショットは JVM だけで撮影されます。Android、desktop、iOS は同じ Compose のコードを同じ Skia バックエンドを通じて描画するため、1 つのプレビューの 2 枚目・3 枚目の画像は、JVM のものが既に示していたことを繰り返すだけでした。Android の環境を通すのではなくホストの Skia で描画することは、実行をモジュールごとの Robolectric サンドボックスのコストからも解放します。

`commonTest` にある共有の robot / presenter テストは、引き続き iOS（`iosSimulatorArm64Test`）でも実行されます — そこで行わなくなったのは撮影です。JVM では、生成されたプレビューテストと同じ `jvmTest` タスクの中で実行されます。素の `jvmTest` の実行（Roborazzi のタスクなし）では、撮影は不活性です。

## 範囲 / 制限

- Web（wasmJs）は対象外です。Roborazzi には wasm の成果物がありません。
- 画像は Android のデバイス構成ではなくホストの Skia から得られます。SDK レベルやデバイス修飾子は存在せず、プレビューのサイズはその `@Preview` のオプション（`widthDp` / `heightDp`）または内容から決まります。

関連: [プレビューとサンプルアセット](./preview.ja.md) · [テスト概要](./testing.ja.md) · [Convention プラグイン](./build-convention-plugins.ja.md)
