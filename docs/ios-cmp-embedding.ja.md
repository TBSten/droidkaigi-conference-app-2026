# iOS 上の CMP（埋め込み）

`KaigiApp` 全体が `ComposeUIViewController` を介して iOS 上で動作します。すべての画面は `commonMain` なので、iOS 固有の UI はルートタブバーだけです。スタック全体 — Metro、Soil、Navigation3 の両グループ、runtime-retain — は iosArm64 + iosSimulatorArm64 向けにネイティブの klib としてリンクされ、カスタムの `:tools:compiler-plugin` も同じソースに対して実行されます（[Enforcement](./enforcement.ja.md)）。

Xcode は [Swift Export](./ios-interop.ja.md) を通じてそのスタックを取り込みます。Swift Export は framework ではなく Swift のソースと静的ライブラリを生成します。Swift Export は Compose の型を受け付けないため、エクスポートされる面は専用のモジュールに置かれます。

| モジュール | 役割 |
| --- | --- |
| `:app-shared` | Compose UI、`AppGraph` の契約、および他のプラットフォームと共有されるすべて |
| `:app-ios-kotlin` | iOS のエントリモジュール — `AppGraph` の実体化、iOS 専用のバインディング、Swift Package Manager のインポート、およびエクスポートされるモジュールを `AppShared` と名付ける `swiftExport { }` の設定 |

## 埋め込み

`:app-ios-kotlin` は `MainActivity` およびデスクトップの `main` に対応する iOS 側の存在です。グラフを実体化し、それを `KaigiApp` の周りで open し、その結果を `UIViewController` として Swift に引き渡します。そのすべてが `internal` なのは、Swift Export がモジュールのすべての public 宣言をブリッジし、かつグラフが Compose の型に到達するためです。グラフ自体には `internal` に加えて `@HiddenFromObjC` が必要です — 理由は [Swift ↔ Kotlin の相互運用](./ios-interop.ja.md) を参照してください。`KaigiAppHost` が Swift 向け API のエントリポイントであり、それが発行する `RootTabSelection` ラッパーもそこに並びます。

```kotlin
// app-ios-kotlin/src/iosMain/…/KaigiAppHost.kt
class KaigiAppHost(swiftPackageLicensesJson: String) {
    private val graph: IosAppGraph =
        createGraphFactory<IosAppGraph.Factory>().create(swiftPackageLicensesJson)

    val currentTab: Flow<RootTabSelection?> = graph.rootTabNavigator.currentTab.map { tab ->
        tab?.let(::RootTabSelection)
    }

    fun initialize() = graph.appInitializer.initialize()
    fun selectTab(tab: RootTab) = graph.rootTabNavigator.select(tab)
    fun viewController(): UIViewController = ComposeUIViewController {
        context(graph) { KaigiApp() }
    }
}
```

`App` struct が host を所有し、Compose のコントローラを運ぶビューとネイティブのタブバーの両方にそれを渡すため、1 つのグラフがプロセス全体に供されます。その `init` は最初の composition より前にアプリのイニシャライザを実行します（[AppGraph と UiGraph](./di-app-graph.ja.md)）。`UIViewControllerRepresentable` が Kotlin の `UIViewController` を SwiftUI 向けにラップします。

```swift
import AppShared
import SwiftUI

@main
struct KaigiAppApp: App {
    private let host = KaigiAppHost(swiftPackageLicensesJson: swiftPackageLicensesJson())

    init() {
        host.initialize()
    }

    var body: some Scene {
        WindowGroup {
            ZStack(alignment: .bottom) {
                KaigiAppView(host: host)
                RootTabBarView(
                    currentTab: host.currentTab.asAsyncSequence().map { $0?.tab },
                    palette: host.tabBarPalette.asAsyncSequence(),
                    select: host.selectTab(tab:)
                )
            }
            .ignoresSafeArea()
        }
    }
}

private struct KaigiAppView: UIViewControllerRepresentable {
    let host: KaigiAppHost

    func makeUIViewController(context: Context) -> UIViewController { host.viewController() }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

host はこのビューコントローラの上に、バーの分だけのサイズでネイティブの Liquid Glass タブバーを重ねます。オーバーレイの形状とその要件については [Liquid Glass タブバー](./ios-liquid-glass.ja.md) を参照してください。

## Xcode のビルド

pre-build スクリプトが `:app-ios-kotlin:embedSwiftExportForXcode` を実行し、これがエクスポートされるモジュールをコンパイルして、`.swiftmodule` ディレクトリ、ブリッジのモジュールマップ、`libAppShared.a` を `BUILT_PRODUCTS_DIR` に置きます。ターゲットはそれらを `SWIFT_INCLUDE_PATHS`、`LIBRARY_SEARCH_PATHS`、`-lAppShared` を通じて拾い上げます。

このスクリプトは、ネストされた Gradle ビルドのために `SWIFT_INCLUDE_PATHS` を unset します。

```yaml
# app-ios/project.yml
script: cd "$SRCROOT/.." && env -u SWIFT_INCLUDE_PATHS ./gradlew :app-ios-kotlin:embedSwiftExportForXcode
```

Gradle は生成された Swift パッケージのために自前の `xcodebuild` を実行し、そのビルドは継承された値を読み取ります。設定されたままだと、直前のビルドによってすでに `BUILT_PRODUCTS_DIR` にコピーされたモジュールマップが、ネストされたビルドがコンパイル中のものと衝突し、2 回目以降のビルドが `redefinition of module 'KotlinRuntime'` で失敗します。

Compose のリソースは同じタスクからアプリバンドルに届きます。Compose Gradle プラグインが Swift Export のバイナリに対して `syncSwiftExportBinaryComposeResourcesForIos` を登録し、`embedSwiftExportForXcode` をそれに依存させます。このバイナリを宣言するのは `:app-ios-kotlin` なので、そのモジュールが Compose Gradle プラグインを適用します — それがないとバンドルには `compose-resources` ディレクトリが同梱されず、あらゆるリソースの参照が実行時に失敗します。

## リンクされる Swift パッケージ

Swift Package Import（[Swift ↔ Kotlin の相互運用](./ios-interop.ja.md)）は、インポートされたパッケージグラフを、生成された `app-ios/KotlinMultiplatformLinkedPackage` の背後に置きます。その動的プロダクトである `KotlinMultiplatformLinkedPackageDylib` は、エクスポートされた Kotlin コードが実行時に `@rpath` を通じてロードするものなので、Xcode がその動的 framework を埋め込み署名するには、アプリのターゲットが生成されたパッケージをリンクしなければなりません。

```yaml
# app-ios/project.yml
packages:
  KotlinMultiplatformLinkedPackage:
    path: KotlinMultiplatformLinkedPackage
targets:
  KaigiApp:
    dependencies:
      - package: KotlinMultiplatformLinkedPackage
        product: KotlinMultiplatformLinkedPackage
```

それがないとアプリはビルドもリンクも通り、起動時に `Library not loaded: @rpath/KotlinMultiplatformLinkedPackageDylib.framework/…` で失敗します。既存の Xcode プロジェクトにパッケージを配線する Gradle タスク（`integrateLinkagePackage`）はプロダクトをターゲットに追加しますが、Frameworks ビルドフェーズに紐付けるのはターゲットがすでにそれを持っている場合だけです。そのため、`project.yml` で依存関係を宣言することが、生成されるプロジェクトを正しく保つ手立てとなります。

関連: [iOS 概要](./ios.ja.md) · [Liquid Glass タブバー](./ios-liquid-glass.ja.md) · [AppGraph と UiGraph](./di-app-graph.ja.md)
