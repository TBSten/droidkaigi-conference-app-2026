# Clock（KaigiClock）

アプリは現在時刻を、注入された 1 つの継ぎ目である `KaigiClock`（`:core:common`）を通じて読み取り、`Clock.System` を直接使うことはありません。プロダクションはこれをシステムクロックにバインドし、dev ビルドはデバッグツールが動かせるクロックにバインドし、テストは自分で制御するクロックに差し替えます。すべての読み取りが同じインタフェースを通るため、時刻を動かすとアプリ全体が一度に動きます。

`Instant` は Kotlin 2.3 時点で stdlib の型（`kotlin.time`）なので、`KaigiClock` の背後にライブラリは必要ありません。kotlinx-datetime はカレンダー側（タイムゾーン、`LocalDateTime`、フォーマット）を提供し、それ自身の `Instant` / `Clock` は stdlib のものへの非推奨のエイリアスです。

```kotlin
interface KaigiClock {
    fun now(): Instant
}

@Inject
@ContributesBinding(AppScope::class)
class SystemKaigiClock : KaigiClock {
    override fun now(): Instant = Clock.System.now()
}
```

`KaigiClock` は `kotlin.time.Clock` を継承するのではなく、自身で `now()` を宣言します。アプリがそれに求めるのは時刻を読むことだけであり、唯一のメソッドを明示するインタフェースは、読み手を親型へ送るのではなく宣言そのものでそれを示します。

## ルール

- プロダクションと feature のコードは `KaigiClock` を注入します。`Clock.System.now()` が現れるのはクロック自体を組み立てる箇所だけです。すなわち `KaigiClock` の実装と、ずらされていないシステムクロックに対するシフトを測定するオフセットストアです。
- presenter は他の依存と同じように、自身の `PresenterContext` からクロックを取得します。
- UI の composable はクロックを読み取ってはいけません。presenter がそこから計算したものをレンダリングします — [UI への到達](#ui-への到達) を参照してください。
- 時刻に依存するテストは、実際のクロックではなく `FakeClock`（`:core:testing`）を使います。

## UI への到達

クロックは `CompositionLocal` ではありません。`CompositionLocal` はコンポジション内の位置によって異なる値を運ぶものであり（[CompositionLocal レビュー](./compositionlocal-review.ja.md)）、アプリが所有するものはすべてその理由で存在します。snackbar のホストは nav entry に属し、プレビュー画像の resolver はプレビューには存在しプロダクションには存在せず、適用されるスキームはあるサブツリー内ではダークで別のサブツリーではそうではありません。時刻はどこで尋ねても同じであり、デバッグのために適用されるシフトはアプリ全体に対する 1 つのシフトです。サブツリーが異なる答えを返せる継ぎ目は、何も必要とせず何も検査しない自由を提供することになります。

これが成り立つのは、どのコンポーネントも時刻から何かを決定しないからです。コンポーネントが欲しいのは答え — セッションが進行中かどうか、次のセッションが始まるまでどれだけあるか、now-line がどこに位置するか — であり、答えは presenter が計算し `UiState` が運ぶものです。[`UiComponentTakesWhatItReads`](./enforcement.ja.md#uicomponenttakeswhatitreads) が、コンポーネントが書かれていく中でこれを真に保ちます。またこれは、その判断を、スクリーンショットや Robot のシナリオの背後ではなく、[`FakeClock` が高速なユニットテストで固定する場所](./testing-presenter.ja.md) に置きます。

コストは、刻む値 1 つにつき `UiState` のフィールドが 1 つ増えることです。画面全体を毎秒再コンポーズするのが粗すぎる場合、クロックがさらに下まで届くのではなく、刻む部分が自分自身の UiState 型を持ちます — [Presenter のパフォーマンス](./presenter-performance.ja.md) を参照してください。

アニメーションの計時は別の問題です。経過時間は単調な情報源（`TimeSource.Monotonic`、`withFrameNanos`）から得られ、シフトされたり再同期されたりする壁時計がそれを乱すことはできません。

## dev ビルドで時刻をずらす

`:feature:debug` はプロダクションのバインディングを `DebugKaigiClock` で置き換えます。これは `KaigiClockOffsetStore`（`AppScope` のシングルトン）が保持するオフセットを加算します。

```kotlin
@ContributesBinding(AppScope::class, replaces = [SystemKaigiClock::class])
class DebugKaigiClock(private val offsetStore: KaigiClockOffsetStore) : KaigiClock {
    override fun now(): Instant = Clock.System.now() + offsetStore.offset.value
}
```

時刻を設定すると、その時刻とシステムクロックの差が保存されるため、選んだ瞬間から **アプリは実時間の速度で刻み続けます**。時刻に依存する振る舞い（セッションの開始、カウントダウン）は引き続き進行します。`reset()` はオフセットをゼロに戻します。

オフセットはメモリ上にのみ存在します。セッションのあいだ持続し、再起動するとアプリはシステムクロックに戻ります。ストア全体が `:feature:debug` にあるため、リリースビルドには適用すべきオフセットが存在しません — [開発専用コードをリリースから除外する](./build-dev-only-exclusion.ja.md) を参照してください。

2 つのフロントエンドが同じストアに書き込みます。

| フロントエンド | 場所 | 提供するもの |
| --- | --- | --- |
| デバッグ画面の "Clock" セクション | アプリ内 | 現在時刻、カンファレンス当日のプリセット、ISO-8601 のフィールド、リセット |
| アプリの JetWhale プラグイン | デスクトップホスト上、および MCP サーバ経由 | 同じ一式に加えて、AI エージェントが呼び出せるツールコール |

どちらも `KaigiClockOffsetStore` を読み書きするため、どちらか一方で行ったシフトはもう一方にも現れます。

プリセットは `DroidKaigi2026Day`（`:core:model`）からその instant を構成します。これは各カンファレンス当日の日付を保持し、そこから `9/2` 形式のラベルを自身で導出します。`at(hour, minute)` は壁時計の時刻を `ConferenceTimeZone` に対して解決します。日本はサマータイムを採用していないため、`ConferenceTimeZone` は固定オフセットとしてその隣に宣言されています。カンファレンスの日付を他の場所に書いたことが、かつてプリセットがタイムテーブルからずれる原因になりました。

## JetWhale プラグイン

プラグインはこのリポジトリ内で、JetWhale の [プラグインガイド](https://kitakkun.github.io/JetWhale/guide/developing-plugins) が説明するレイアウトで構築されています。アプリ内のエージェント側、デスクトップのデバッガがロードするホスト側、そして両者が共有するプロトコルモジュールです。

| モジュール / クラス | 側 | 責務 |
| --- | --- | --- |
| `:tools:jetwhale-plugin:protocol` | 共有 | `@Serializable` なメッセージと `KAIGI_PLUGIN_ID` |
| `KaigiAgentPlugin`（`:feature:debug`） | アプリ | 状態リクエストへの応答、シフトの適用、デバッグ画面でのシフトのイベントとしての報告 |
| `:tools:jetwhale-plugin:host` | デスクトップ | プラグイン UI と `io.github.droidkaigi.confsched2026.clock.*` MCP ツール |

アプリが contribute するプラグインは **1 つ**、`io.github.droidkaigi.confsched2026` であり、クロックはその主題ではなく最初のコントロールです。両側を対にするのも、ドロワーに一覧されるのも `pluginId` なので、デバッグコントロールごとに 1 つの id を割り当てると、同じツールに属するつまみのために、対応付け、`onPrepare` のやり取り、有効化スイッチ、ドロワーのアイコンが増えていきます。2 つ目のコントロールは、プロトコルモジュールのもう 1 つのメッセージ型であり、ホスト UI のもう 1 つのセクションです。id が年を含むのは application id と同じ理由です。ホストのインストールは id ごとに 1 つの jar を保持します。

ホスト側は、対象とする JetWhale リリースの Compose とホスト SDK のバージョンに対して `compileOnly` としてコンパイルされます。ホストが実行時にそれらを供給するため、バンドルしてはいけません。バージョンカタログはこれらのピンをアプリ自身の Compose バージョンとは分けて管理します。

プリセットのリストはホスト側で再度述べるのではなくワイヤ越しに送られるため、どの instant を提供するかを決めるのはアプリ側だけです。

プラグインをロードした状態でホストを実行します。

```shell
./gradlew :tools:jetwhale-plugin:host:runJetWhaleHot   # host + hot reload on source changes
./gradlew :tools:jetwhale-plugin:host:installPlugin    # install into ~/.jetwhale/plugins/
```

## テスト

`FakeClock`（`:core:testing`）は `TestingScope` で `KaigiClock` をバインドするため、クロックを取得する presenter は画面の [テストグラフ](./testing-graph.ja.md) からそれを解決し、テストを編集する必要はありません。同じアクセサでテストがそれを操作します。

```kotlin
graph.clock.instant = Instant.parse("2026-09-02T10:00:00+09:00")
// … exercise the presenter …
graph.clock.advanceBy(40.minutes)
```

デフォルトの instant は Day 1 のセッション時間帯なので、時刻を設定しないテストでもイベント中の instant を読み取ります。

関連: [デバッグ](./debugging.ja.md) · [テストグラフ（TestingScope）](./testing-graph.ja.md) · [開発専用コードをリリースから除外する](./build-dev-only-exclusion.ja.md)
