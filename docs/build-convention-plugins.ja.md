# Convention プラグイン

ビルド設定はモジュール間で簡単に重複します — 同じプラグイン、同じターゲット、同じ opt-in です。それをすべての `build.gradle.kts` に書き下すと、設定は**互いに乖離**し、変更のたびに複数箇所の編集が必要になります。**Convention プラグイン**は、その共有の設定を 1 箇所にまとめ、モジュールごとに 1 行で適用します — ここでは **precompiled Kotlin script**（`gradle-conventions` included build 内の `*.gradle.kts`）として実現しています。

## Primitive と convention

プラグインには 2 種類あります。

- **Primitive**（`droidkaigi/primitive/*.gradle.kts`） — それぞれが 1 つの関心事を担います: `kmp`、`kmp.compose`（各モジュールの [Compose Resources](./localization.ja.md) パッケージをそのパスから導出もします）、[`enforcement`](./enforcement.ja.md)、[`buildkonfig`](./build-config-buildkonfig.ja.md)、[`screenshot-test`](./testing-preview-screenshot.ja.md)、…
- **Convention**（`droidkaigi/convention/*.gradle.kts`） — **primitive を合成する**グループごとのレシピです。例えば `kmp-feature` は `kmp`、`kmp.compose`、`screenshot-test`、`spotless` を取り込み、さらにすべての feature が共有する serialization、Metro、KSP、および[プレビューとサンプルアセット](./preview.ja.md)の依存関係を追加します。

許可される依存の方向:

```text
○ module     → convention
○ module     → primitive
○ convention → primitive
○ primitive  → primitive
✗ convention → convention
✗ primitive  → convention
```

禁止されている方向は 2 つだけです — **`primitive → convention`** と **`convention → convention`** です。convention 層より下にあるものは convention を取り込まず、convention 同士が連鎖することもありません。それ以外はすべて下方向に合成されます。

## 実際の姿

プラグイン id は `src/main/kotlin` 配下のファイルのパスであり、`/` とパッケージが接頭辞になります。したがって `droidkaigi/primitive/kmp.compose.gradle.kts` は `droidkaigi.primitive.kmp.compose` になります。`gradle-conventions` ビルドは `kotlin-dsl` を適用しているため、**ファイルを追加するだけで、それが Gradle プラグインへと precompile されます** — 登録は不要です。

```kotlin
// gradle-conventions/src/main/kotlin/droidkaigi/primitive/kmp.compose.gradle.kts
package droidkaigi.primitive

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("droidkaigi.primitive.enforcement")   // primitive → primitive
}
// + the Android preview-renderer classpath and the shared jvmToolchain / optIn block
```

`kotlin-dsl` はそのスクリプトを、スクリプト本体を実行する `Plugin<Project>` のラッパーへコンパイルします — これがそれをプラグインたらしめているものです。

```kotlin
// gradle-conventions/build/…/Kmp_composePlugin.kt (generated) — id: droidkaigi.primitive.kmp.compose
package droidkaigi.primitive

class Kmp_composePlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class.forName("droidkaigi.primitive.Kmp_compose_gradle")   // the compiled script body
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)   // runs it against `target`
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
```

convention は primitive を合成し、そのグループのビルドロジックを追加します。

```kotlin
// gradle-conventions/src/main/kotlin/droidkaigi/convention/kmp-feature.gradle.kts
package droidkaigi.convention

plugins {
    id("droidkaigi.primitive.kmp")
    id("droidkaigi.primitive.kmp.compose")
    id("droidkaigi.primitive.screenshot-test")
    id("droidkaigi.primitive.spotless")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("dev.zacsweers.metro")
    id("com.google.devtools.ksp")
}
// + feature-specific build logic (e.g. run :tools:ksp-processor in kspCommonMainMetadata)
```

関連: [バージョンカタログ](./build-version-catalog.ja.md) · [BuildKonfig（ビルド時の値）](./build-config-buildkonfig.ja.md)
