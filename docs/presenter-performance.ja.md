# Presenter のパフォーマンス

Presenter は `@Composable` 関数です。その制約のもとで、重いデータ整形がフレームを止めてしまわないように責務が分けられています。

## Composable な presenter で整形するコスト

`@Composable` な Presenter の本体に重い同期計算（`groupBy` / `sortedBy` / `associate` / JOIN / インデックス構築）を書くと、それは **再コンポジションのたびにメインスレッドで実行され**、1 フレームを重くします。これはデータ量が増えるにつれて表面化します。

## どの種類の計算がどこで動くか

| やること | 手段 | 実行されるスレッド / 特性 |
| --- | --- | --- |
| **はじめから構造的な整形をデータレイヤーで行う**（第一選択） | [`QueryKey`](./soil-keys.ja.md)/`SubscriptionKey` の `fetch`/`transform` | **`Default.limitedParallelism(1)`（バックグラウンド）+ キャッシュ + データが変わったときだけ。** すべてのプラットフォームで動作 |
| **軽い結合** | `rememberQuery(k1,k2,transform)` または [`SoilDataBoundary`](./soil-data-boundary.ja.md) の content で結合する | メイン。ただし **ソースの状態が変わったときだけ**（再コンポジションのたびではない）。小さければ無視できる |
| **重い結合（独立した複数のソースを同期する）** | `SubscriptionKey` から `combine(...)` をそのまま返す | **バックグラウンド**（SwrCache のワーカー）+ ソースの変化で再計算 + キャッシュ。**重い結合の主な選択肢** |
| **重い結合（単一のレスポンスからの JOIN）** | 派生した `QueryKey` の `fetch` で JOIN する | バックグラウンド + キャッシュ |
| **UI 状態に依存する重い導出だけが残った場合** | `produceState(keys){ withContext(Dispatchers.Default){…} }` | native では並列 + メモ化、wasm ではメモ化のみ。**フォールバック** |
| **（任意、native の応用）** Presenter 全体をバックグラウンドへ逃がす | バックグラウンドの Molecule で Flow にする | 落とし穴があります。CompositionLocal が継承・伝播されません。デフォルトではありません |
| **やってはいけないこと** | パフォーマンス目的で `rememberQuery(key, select)` / `transform` を使う | **メイン、再コンポジションのたび、メモ化なし** = presenter に直接書くのと同等。パフォーマンスの改善策にはなりません |

> 根拠: `fetch`/`subscribe` はバックグラウンドの SwrCache ワーカーで動作し、`select`/combine はメインスレッドで動作します。

> **原則として、`fetch`/`subscribe` の中に `flowOn`/`withContext(Dispatchers.Default)` を書いてはいけません。** `fetch`/`subscribe` はすでに SwrCache のワーカー（バックグラウンド）で動作しているため、別のコンテキストへ逃がすことは不要であり、意図も不明瞭になります。唯一の例外は、**単一の transform が極端に重く、ほかのクエリを計測できるほど待たせている**場合です。

## 中間モデルの境界（QueryKey はどこまで返してよいか）

- **返してよいもの**: 再利用でき、**UI に依存せず**、すでに整形済み（JOIN / ソート / インデックスが済んでいる）の **ドメインの便宜モデル**（例: `SessionWithSpeakers`）。
- **返してはいけないもの**: 画面のレイアウトや画面の状態（選択中のタブ、編集中、ローディングフラグ）に結び付いた **画面固有の UiState**（例: `SessionScreenUiState`）。
- 判断基準: **複数の画面やテストをまたいで再利用でき、view state を含まない → データレイヤー**。1 つの画面を描画・操作するための形 → Presenter の出力。

## Presenter の責務の境界

**「Presenter はデータを整形しない。UI 状態を整形する。」**

- **持つもの**: 一時的な UI 状態（選択中のタブ、編集中、展開中）、イベントの消費（`ActionEffect(screenChannel)`）、ミューテーションの起動（`mutateAsync`）、一度きりの出力（`screenChannel.emit`）、そして **整形済みのデータ + UI 状態から UiState を安価に組み立てること**。
- **持たないもの**: 重いデータ整形 / JOIN / ソート / インデックス → **データレイヤー（QueryKey）**。ローディングとエラーの境界、Coroutine と Dispatcher の管理 → **`SoilDataBoundary`/Soil**。描画、ナビゲーションの方針、テーマ → **UI/Root**。

### 整形をデータレイヤーへ押し出すと「非同期にすることの欠点」が消える

Presenter 側で `produceState` を使ってバックグラウンドへ逃がすと、(1) 値が `T?` になり `if (x == null) Loading()` が増え、(2) Coroutine・Dispatcher・ローディングの管理で Presenter が肥大化します。
**整形を `fetch`/`transform`（データレイヤー）へ押し出せば、`SoilDataBoundary` が境界でローディングとエラーを扱い、非 null の準備済みデータを content ラムダへ渡す**ため、(1) も (2) も起こりません。つまりデータレイヤーのアプローチは、「バックグラウンドへ逃がす」と「Presenter を薄く保つ」を同時に満たします。

## ルールの強制（Presenter を薄く保つこと）

- **強いゲート（`groupBy` などをコンパイルエラーにするコンパイラプラグイン）は設けません。** 「重さ」はデータ量に依存し、静的には判断できないため、静的なルールは多くの偽陽性（安価な使用箇所）と見逃しを生みます。これは `mutate` を禁止する場合（二値であり、常に誤り）とは性質が異なります。
- **強制はせず、代わりに正しい道筋（派生した QueryKey / Subscription）+ レビュー + このガイドラインによって誘導します。** `@Composable` な presenter の中の `groupBy`/`sortedBy`/`associate`/`distinctBy` に対しては、せいぜいレビューでの一押し（「データレイヤーを検討してください」）にとどめます。

## 最小のコード例

> 読みやすさのために簡略化しています。`buildPersistedQueryKey` に独立した `transform` パラメータはありません。整形は `fetch` の末尾、同じバックグラウンドワーカー上で行います。Root は `ScreenContext` のもとで `rememberQuery` を通じて読み取ります（[Enforcement](./enforcement.ja.md) の `SoilReadConfinement` を参照）。

```kotlin
// Data layer: structural shaping in transform (background, cached, only when data changes)
val sessionsByDayKey = buildPersistedQueryKey<Map<Day, List<Session>>, SessionsResponse>(
    id = QueryId("sessionsByDay"),
    fetch = { httpClient.get(".../sessions").body() },
    transform = { res -> res.toSessions().groupBy { it.day } },  // heavy shaping goes here
)

// Heavy combining: just return combine from a SubscriptionKey (background, recomputed on source change, cached)
//    flowOn is unnecessary — subscribe is already collected on the SwrCache worker (background)
class SessionWithSpeakersKey : SubscriptionKey<List<SessionWithSpeakers>> by buildSubscriptionKey(
    id = SubscriptionId("sessionWithSpeakers"),
    subscribe = {
        combine(sessionsFlow, speakersFlow) { sessions, speakers ->
            sessions.map { it.withSpeakers(speakers) }  // heavy JOIN
        }
    },
)

// Presenter: just reads already-shaped data. Only cheap UI selection
@Composable
fun timetableScreenPresenter(/* context: TimetablePresenterContext */): TimetableUiState {
    val byDay = soil.query(sessionsByDayKey)        // already shaped
    var selectedDay by remember { mutableStateOf(Day.Day1) }
    return TimetableUiState(
        day = selectedDay,
        sessions = byDay[selectedDay].orEmpty(),     // just looks up a bucket (cheap)
    )
}
```

関連: [アーキテクチャ概要](./architecture-overview.ja.md) · [Soil のキー](./soil-keys.ja.md) · [SoilDataBoundary](./soil-data-boundary.ja.md) · [画面の実装](./building-a-screen.ja.md)
