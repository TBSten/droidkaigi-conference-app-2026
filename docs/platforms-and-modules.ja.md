# プラットフォームとモジュール

このアプリは 4 つのプラットフォーム — **Android / iOS / Desktop (JVM) / Web (wasmJs)** — を対象とします。このページは、**新しいコードをどこに置くか**を決める取り決めを定義します。モジュールの一覧と依存グラフについては [モジュール構成](./project-structure.ja.md) を参照してください。

## `:core:model` に入るもの

**model = プロジェクト全体で共有される型の置き場所。** 次の 3 つを含みます。

1. **中核となるドメインモデル** — エンティティ / value class / enum。
2. **Soil Key の宣言 (contract)** — `*QueryKey` / `*SubscriptionKey` / `*MutationKey` の typealias ([Soil のキー](./soil-keys.ja.md))。
3. **画面ごとの DI スコープマーカー** (`<Screen>ScreenScope`) — なぜここにあるのかは注意点を参照してください。

**Key の実装 (`Default*Key`) は `:core:data` にあります。** feature が依存するのは Key の「型」だけであり、その実装ではありません。

```text
feature ──► model (types: domain models, *Key)
data    ──► model (implementations: Default*Key implements *Key)
DI (Metro) binds the data implementations to the model types
```

model は data にも feature にも依存してはいけません。最も下流のモジュールです。

**model に入らないもの**

- API レスポンス / DB エンティティ → `:core:data` (model にマッピングされる)
- UiState / Action / ActionResult → `feature`
- 汎用ユーティリティ / Compose の基盤 → `:core:common`

**注意点**

- Key の typealias は `soil.query.*` を参照するため、**`:core:model` は Soil ライブラリへの api 依存を持ちます** (アプリが Soil の上に構築されている以上、承知のうえで受け入れています)。
- **画面ごとの DI スコープマーカー**は `:core:common` ではなく model にあります。`:core:data` が画面ごとの key のバインドのためにそれらを参照する必要があり、data は意図的に `:core:common` (Compose / Navigation3 を api で公開しています) に依存しないためです — データ層は UI から自由なままです。
- **薄い横断的な contract** も model に置けます。model にインターフェースを置き、その実装は data / app 側に置きます。型と薄い contract だけであり、実装の本体は入れません。

## 永続化は `:core:data` に入る

- **key-value の設定**は**すべてのプラットフォームで androidx DataStore Preferences** を使います。プラットフォームごとに異なるのはバッキングだけです。
- **バイナリの blob** は **`FileStorage`** の継ぎ目を通ります (Web のバッキングが本当に非同期であるため `suspend` です)。
- **Soil の query の永続化**は `buildPersistedQueryKey` を通ります。その `@MustBeSerializable` な reified レスポンス型により、シリアライズできないペイロードはコンパイルエラーになります — [Soil の永続化](./soil-persistence.ja.md) を参照してください。

## feature モジュールに入るもの

1 つの画面グループ = 1 つの feature モジュールです。画面の composable、UiState / Action / ActionResult、screen context、そしてナビゲーション一式 (NavKey、entry provider、画面ごとのグラフ、navigator インターフェース) が入ります。登場人物の全体像と命名は [画面の実装](./building-a-screen.ja.md) で定義されています。データ取得は model の Key + Soil を通して行われ、feature が API クライアントと直接やり取りすることはありません。

## app 層に入るもの

`app-shared` は、すべての feature を見る必要があるもの (ナビゲーションの集約、feature をまたぐ navigator の実装) を保持します。プラットフォームごとのエントリモジュールは、プラットフォームごとに異なるものだけを保持します — [`AppGraph`](./di-app-graph.ja.md) の実現と、プラットフォーム SDK に届くバインド (クラッシュレポータ、ライセンスのエクスポート) の提供です。ストレージのバッキングはそこには含まれません。`:core:data` が DataStore と `FileStorage` のために自前の `androidMain` / `iosMain` / `jvmMain` / `wasmJsMain` の actual を持っています。プラットフォーム間で同一のコードは、エントリモジュールではなく `app-shared` に属します。

## オープンソースライセンス

プラットフォームごとに出荷される依存関係のセットが異なるため、ライセンス画面はプラットフォームごとの一覧を読む必要があります。そのため AboutLibraries の Gradle プラグインは**すべてのエントリモジュール**に適用され、各モジュールが自身の解決済み依存グラフから自身の `aboutlibraries.json` をエクスポートします。

| モジュール | エクスポートのタスク | アプリがどう読むか |
| --- | --- | --- |
| `app-android` | `exportLibraryDefinitions<Variant>` | 生成された Android リソース、`R.raw.aboutlibraries` |
| `app-desktop` | `exportLibraryDefinitionsJvm` | Compose リソース、`files/aboutlibraries.json` |
| `app-web` | `exportLibraryDefinitionsWasmJs` | Compose リソース、`files/aboutlibraries.json` |
| `app-ios-kotlin` | `exportLibraryDefinitionsIosArm64` | Compose リソース、`files/aboutlibraries.json` |
| `app-ios` | `scripts/generate-swift-package-licenses.py` | バンドルリソース、`KaigiAppHost` に渡される |

各エントリモジュールは自身のエクスポートを `LicensesJsonProvider` を通じて提供します。`:core:data` はそれぞれを AboutLibraries の `Libs` にパースし、`LicensesQueryKey` の背後でマージします。どのプラットフォームで動いているのかを知ることはありません。画面はその `Libs` を AboutLibraries 自身の `LibrariesContainer` で描画します。`LibrariesContainer` は行、詳細の展開、ライセンスダイアログを所有し、提示するリンクを `LocalUriHandler` 経由で開きます。

provider がリストを返すのは、1 つのプラットフォームがソースを 2 つ持つためです。このセクションの残りはその話です。

### iOS

`app-ios` は自身の Gradle 依存グラフを持たない Xcode のシェルなので、Kotlin の依存関係は iOS の成果物のビルド元となる Gradle モジュール `app-ios-kotlin` からエクスポートされます。このプラグインは `*CompileClasspath` / `*RuntimeClasspath` という名前の configuration からしか収集せず、Kotlin/Native はそれらを生成しないため、`droidkaigi.primitive.licenses-export` プラグインが `iosArm64CompileKlibraries` を認識される名前でミラーします。

1 つの iOS ターゲットが両方を代表します。Compose リソースのディレクトリはソースセットごとに登録され、2 つのターゲットは `iosMain` を共有するため、両者の間でエクスポートが変わることはありません。その 1 つのソースセットをコンパイルするので同じライブラリに解決され、出荷されるのはデバイス向けターゲットです。シミュレータ向けのビルドも同じエクスポートをパッケージし同じ一覧を表示します。異なるのは、座標の一方が持つ成果物のサフィックスだけです。

そのエクスポートは Swift Export のビルドフェーズを通じて、Compose リソースとしてアプリのバンドルに届きます。Compose の Gradle プラグインが `embedSwiftExportForXcode` をリソースの同期に依存させ、`:app-ios-kotlin` が宣言する Swift Export のバイナリに対して登録します — [iOS 上の CMP（埋め込み）](./ios-cmp-embedding.ja.md) を参照してください。

Swift package は Xcode によって解決され、どの Gradle configuration にも現れないため、iOS のビルドが自分でそれらを記述します。`app-ios/scripts/generate-swift-package-licenses.py` がビルドフェーズとして実行され、2 つ目のエクスポートをバンドルに書き出します。package のセットとバージョンは `Package.resolved` から、ライセンス本文はチェックアウトされたソースから取ります。SPDX の識別子はライセンスファイルから導出できないため、スクリプトは package ごとに 1 つをマッピングし、知らない package があればビルドを失敗させます。

アプリはそのエクスポートを読んで `KaigiAppHost` に渡し、`KaigiAppHost` はそれをグラフのファクトリに渡します。これが、iOS のグラフだけが他のプラットフォームにはないパラメータを取る理由です。

```kotlin
@DependencyGraph(scope = AppScope::class)
internal interface IosAppGraph : AppGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides @SwiftPackageLicenses swiftPackageLicensesJson: String): IosAppGraph
    }
}
```

## 生成 vs. 手書き

`:tools:ksp-processor` は、書き漏らしによって腐りやすいもの — [NavKey シリアライザの集約（NavKeySerializersProvider）](./navigation-navkey-serializers.ja.md)、Soil の key の id、JVM 以外向けのプレビューレジストリ — を生成します。`NavEntryProvider` / `Navigator` は意図的に手書きです。プレビュー画像の enum は KSP ではなく Gradle のタスクで生成されます — [プレビュー画像 enum の生成](./preview-image-enum.ja.md) を参照してください。

関連: [モジュール構成](./project-structure.ja.md) · [画面の実装](./building-a-screen.ja.md)
