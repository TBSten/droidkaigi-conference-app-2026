# デバッグ

デバッグ専用のツールは専用の **`:feature:debug`** モジュールに置かれており、開発ビルドにのみ組み込まれ、本番に出荷されることは決してありません。このモジュールは 2 つのものを担います: アプリ内のデバッグ画面と、実行中のアプリをデスクトップのデバッガに接続する JetWhale agent です。独自の feature モジュールに保つことで、アプリの他の部分はデバッグコードへの依存を持たず、このツールは通常の feature と同じ DI / ナビゲーションの継ぎ目を利用できます。このモジュールは無条件に依存されるわけではありません: すべてのプラットフォームがデフォルトではこれを除外し、開発ビルドと認識したビルド — Android の dev product flavor、`:app-desktop:run`、`:app-web:wasmJsBrowserDevelopmentRun`、そして iOS アプリの Xcode Debug ビルド — に対してのみ追加し直します。画面も agent も Metro の集約だけを通じてグラフに到達するため、依存を外せば他のコード変更なしにそれらも外れます。プラットフォームごとの配線と除外の検証方法については、[開発専用コードをリリースから除外する](./build-dev-only-exclusion.ja.md) を参照してください。

`DebugScreen` は他の画面と同じように配線されています (`DebugNavKey` + `DebugScreenContext` + `DebugScreenGraph` + `DebugNavEntryProvider`)。`BuildConfigProvider` を通じて実際のアプリバージョンを表示し、**Clear persisted data** アクションを提供し、Soil のエラーオーバーレイを制御し (トグルと、`SoilErrorsScreen` を開くライブのエラー件数)、アプリの時刻をずらす **Clock** セクション — [Clock（KaigiClock）](./clock.ja.md) を参照 — と、ライブの pitch と roll を表示する **Device tilt** セクションを備えます。本番のロギングは [Kermit ベース](./logging.ja.md) です。HTTP トラフィックはアプリ内のログではなく JetWhale を通じて検査します。

## 永続化データのクリア

メニューの **Clear persisted data** ボタンは、アプリの永続化された (およびメモリ上のセッションの) 状態を 1 回の呼び出しで消去し、テスターがアプリをまっさらな状態に戻せるようにします。これは `PersistedDataResetter` (`:core:data`、`AppScope`) によって集約されています:

- **Preferences DataStore** (テーマ) — `ThemeStore.clear()` (`dataStore.edit { it.clear() }` により、preferences ファイル全体をクリアします)。
- **Favorites** — `FavoritesStore.clear()` (favorites の preferences ファイルをクリアします)。
- **Blobs** (プロフィール画像など) — `FileStorage.clear()`。プラットフォームごとに実装されています: JVM/Android/iOS では blob ディレクトリを削除し、Web (wasmJs) の IndexedDB actual では `IDBObjectStore.clear()` を呼びます。

`DebugPresenterContext` が `PersistedDataResetter` を保持します。ボタンは `ClearData` アクションを送り、presenter は `ActionEffect` の中で `clearAll()` を実行して、"cleared ✓" の確認を表示する状態を切り替えます。

## JetWhale agent

[JetWhale](https://kitakkun.github.io/JetWhale/) は、実行中のアプリが WebSocket 経由で接続するデスクトップのデバッガです。dev ビルドはそのプラグインのうち 3 つを接続します: **Nav3 Navigator** (ライブの Navigation 3 バックスタック。host から push / pop / 並べ替えを操作できます)、**Network Inspector** (HTTP トランザクションとレスポンスのモック)、そして **Compose Semantics Inspector** (Compose のノードツリー。各ノード自身の semantics アクションを呼び出せます)。それらと並んで、アプリ自身のプラグインである **DroidKaigi 2026** が動作し、host から到達できるデバッグ用のコントロールを備えます: clock ([Clock（KaigiClock）](./clock.ja.md) を参照) と、tilt 駆動のエフェクトを指定した角度に固定するデバイスの tilt オーバーライドです。host はこれら 4 つすべてを MCP サーバー経由でも公開するため、AI エージェントが同じ操作でアプリを動かせます。

### 実行方法

host は [JetWhale のリリースページ](https://github.com/kitakkun/JetWhale/releases) からインストールして起動します。host はポート **5080** でデバッギーを待ち受けます。dev ビルドを実行すると、起動中、最初のコンポジションより前に、アプリが host 上のセッションとして現れます。Android の実機とエミュレータは `adb reverse tcp:5080 tcp:5080` を通じて host に到達します。これは、設定で ADB の自動ポートマッピングを無効にしていない限り、host が自動的に設定します。

| ターゲット | host への到達方法 | Compose Semantics Inspector |
| --- | --- | --- |
| Android (dev flavor) | `adb reverse` 経由、自動 | 利用可能 |
| Desktop | `localhost` 経由 | 利用可能 |
| Web | `localhost` 経由 | 利用不可 |
| iOS Simulator | `localhost` 経由 | 利用不可 |
| iOS 実機 | コンパイル時に埋め込まれたビルドマシンのアドレスへ `wss` 経由 | 利用不可 |

Nav3 Navigator と Network Inspector はすべてのターゲットで動作します。Compose Semantics Inspector にはプラットフォームの Compose ルートを見つけるプローブが必要で、JetWhale は Android とデスクトップ向けにのみそれを提供しています。それ以外では空のツリーを報告します。

`JetWhaleDebugger` は `ws("localhost", 5080)` を最初に、`buildMachineWss(5443)` をその後に並べます。ループバックは host マシンのネットワーク名前空間を共有するすべてのターゲットをカバーします。物理的な iPhone はそうではないため、2 つ目の候補にフォールスルーします。この候補は `com.kitakkun.jetwhale.agent` Gradle プラグインがコンパイル時にこのマシン自身のアドレスへ書き換えます — [ビルドマシンのアドレスを埋め込む](https://kitakkun.github.io/JetWhale/guide/getting-started#baking-in-the-build-machine-s-address-no-browse) を参照してください。

このプラグインは `:feature:debug` だけに適用されます。埋め込まれるアドレスはコンパイルタスクの入力なので、開発者がネットワークを移動するたびにこのモジュールは再コンパイルされ、それらのコンパイルは共有ビルドキャッシュから得られることがありません。プロジェクト全体に適用すると、そのコストがすべてのモジュールに広がってしまいます。

物理的な iPhone ではさらに、iOS がローカルネットワークへのアクセスを許可するように、iOS アプリの `Info.plist` に `NSLocalNetworkUsageDescription` が必要です。証明書まわりについては、上流の [セキュアな接続 (wss)](https://kitakkun.github.io/JetWhale/guide/getting-started#secure-connections-wss) のガイドが扱っています。

### 配線のしくみ

本番コードから見えるのは `:core:common` にある 3 つの継ぎ目です。`AppInitializer` はプロセス全体の起動フックです。他の 2 つは `@Composable operator fun invoke` を持つ `fun interface` なので、注入されたインスタンスは、その隣にある素の composable のエフェクトとまったく同じように呼び出されます:

```kotlin
fun interface AppInitializer {
    fun initialize()
}

fun interface BackStackDebuggingEffect {
    @Composable
    operator fun invoke(backStack: NavBackStack<NavKey>)
}

fun interface SemanticsDebuggingEffect {
    @Composable
    operator fun invoke()
}
```

各プラットフォームのエントリポイントは、UI がまだ存在しないうちに initializer を 1 回実行します — Android では `Application.onCreate`、デスクトップと Web では `main()`、iOS では `App` struct の `init` です:

```kotlin
appGraph.appInitializer.initialize()
```

`KaigiApp` は 2 つのエフェクトを `NavDisplay` の隣でコンポーズします:

```kotlin
uiGraph.backStackDebuggingEffect(backStack)
uiGraph.semanticsDebuggingEffect()
```

`:feature:debug` がクラスパスにない限り、バインディングは `NoopAppInitializer`、`NoopBackStackDebuggingEffect`、`NoopSemanticsDebuggingEffect` です。クラスパスにある場合は、1 つの `JetWhaleDebugger` が Metro を通じて 3 つすべてを置き換えます — デバッグ画面が使うのと同じ集約です。

`JetWhaleDebugger` は `AppScope` のシングルトンで、その `initialize()` が接続を開いてプラグインを接続します。各プラグインは、すでに存在する継ぎ目を通じてアプリに到達します:

- **Nav3** — `Nav3KeyCodec.openPolymorphic` は、バックスタックがすでにそこから構築されているマージ済みの `SerializersModule` を受け取るため、host はアプリ内のすべての `NavKey` をデコードし構築できます。[NavKey シリアライザの集約（NavKeySerializersProvider）](./navigation-navkey-serializers.ja.md) を参照してください。
- **Network** — インターセプタは Ktor の `HttpSend` を通じて注入済みの `HttpClient` に接続し、`:core:data` のプロバイダには手を触れません。`HttpSend` はインターセプタを解除できず、重複も拒否しないため、シングルトンのスコープと単一のエントリポイント呼び出しが、各トランザクションを 1 回だけ記録することを保証します。
- **Compose semantics** — `SemanticsProbe()` は `expect` 関数です。Android とデスクトップの actual は JetWhale のプローブをインストールし、iOS と Web の actual は何もしません。

エントリポイントが最初のコンポジションより前に initializer を実行するため、起動中に発行されたリクエストも捕捉されます。

関連: [開発専用コードをリリースから除外する](./build-dev-only-exclusion.ja.md) · [ロギング（Kermit）](./logging.ja.md)
