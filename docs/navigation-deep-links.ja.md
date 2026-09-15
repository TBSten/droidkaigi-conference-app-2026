# ディープリンク（DeepLinkEffect）

ディープリンクとは、アプリ自身の UI の外側から到着するナビゲーション要求です — 現在はホーム画面のウィジェットが発行する Android の intent であり、wasmJs の URL エントリは同じフローへの将来の 2 つ目の発行元です。設計は 3 つの部分からなります。URI スキーム、バッファリングを行う store、そして 1 つの消費側 effect です。

## URI スキーム

パスは、読み手が辿ってきたであろう surface を名指しするため、合成される履歴は URI だけから決まります。

| URI | デスティネーション |
| --- | --- |
| `droidkaigi2026://session/{id}` | `TimetableItemId` が `{id}` のセッション詳細 |
| `droidkaigi2026://favorites` | お気に入りタブ |
| `droidkaigi2026://favorites/session/{id}` | お気に入り surface を経由して到達するセッション詳細 |
| `droidkaigi2026://about` | About タブ |
| `droidkaigi2026://timetable/day1` \| `.../day2` | その開催日を表示するタイムテーブルタブ |

`DeepLink.parse`（`core:common`）はこの文法の唯一の住処で、プラットフォームのエントリポイントが生の URL 文字列をそこに渡します。`MainActivity` は対応する `VIEW`/`BROWSABLE` の intent-filter を宣言します。これは `singleTask` として動作するため、アプリが生きている間にタップされたリンクは、2 つ目の activity を積み上げるのではなく `onNewIntent` を通じて既存のタスクを前面に出します。

## DeepLinkStore と DeepLinkEffect

プラットフォームのエントリポイントは自身の入力をパースし、プラットフォーム中立な `DeepLink` を `DeepLinkStore`（`core:common`）に発行します。activity 自体はナビゲーションを一切行いません。store はバッファリングするため、最初の composition より前に送信されたリンクも失われません。

```kotlin
// MainActivity — both the cold-start intent and onNewIntent go through here.
private fun submitDeepLink(intent: Intent) {
    intent.toDeepLink()?.let(appGraph.deepLinkStore::submit)
}
```

起動 intent が消費されるのは新規作成時（`savedInstanceState == null`）だけです。再生成 — configuration change やプロセス死 — ではタスク本来の intent が再配信されますが、復元されたバックスタックはすでにそのリンクを反映しています。

## 起動シナリオ

`MainActivity` はアプリで唯一の activity で `singleTask` として動作するため、あらゆる起動経路が 1 つの既存タスクに解決されます。

| シナリオ | 振る舞い | 保証するもの |
| --- | --- | --- |
| アプリがバックグラウンドにある状態でのウィジェットのタップ | 既存のタスクが前面に出て、リンクは push として `onNewIntent` を通じて届きます | `singleTask`。Glance は `PendingIntent` を `FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT` で、activity のフラグなしに構築し、セッション URI が各行の `PendingIntent` を互いに区別された状態に保ちます |
| 別のアプリがフォアグラウンドにある状態で発行されたディープリンク | システムはアプリ自身のタスクへ移り、別アプリのタスクには手を触れません | `singleTask` |
| アプリが分割画面のペアに置かれている状態でのウィジェットのタップ | 同じタスクルーティングが適用され、システムは 2 つ目のインスタンスという選択肢を提示しません | `singleTask`。`documentLaunchMode` やマルチインスタンスの属性は設定されていません |
| ディープリンクされたタスクのプロセス死後の、ランチャーまたは履歴からの再起動 | アプリは通常のフローで起動し、タスク本来の `VIEW` intent は再送信されません | `savedInstanceState` のガード |

`DeepLinkEffect`（app-shared、`KaigiApp` の中で [`NavigatorEffect`](./navigation-navigator.ja.md) と並べて配線されます）が唯一の消費側です。warm なナビゲーションは lambda として navigator を経由し、[Enforcement](./enforcement.ja.md) に従って `Navigator` の型を composable のシグネチャの外に保ちます。

```kotlin
DeepLinkEffect(
    deepLinkStore = uiGraph.deepLinkStore,
    timetableDayRequestStore = uiGraph.timetableDayRequestStore,
    backStack = backStack,
    logger = uiGraph.logger,
    onNavigate = uiGraph.appNavigator::moveToTop,
)
```

## バックスタックの合成

`DeepLinkEffect` は、スタックに `StartupNavKey` が残らなくなるまで各リンクを保持し、それから着地させます。`buildSyntheticBackStack(link)` は純粋でユニットテストされており、素のセッションリンクには `[TimetableNavKey, TimetableItemDetailNavKey(id)]`、お気に入りタブには `[TimetableNavKey, FavoritesNavKey]`、About タブには `[TimetableNavKey, AboutNavKey]`、お気に入りのセッションリンクには `[TimetableNavKey, FavoritesNavKey, TimetableItemDetailNavKey(id)]`、タイムテーブルの日付リンクには `[TimetableNavKey]` を返します。

- **コールドスタート** — スタックはまだ 1 つのエントリしか保持していません（起動はまだナビゲートしていません）。スタックは合成されたスタックで**置き換えられる**ため、back は名指しされた surface を辿ってタイムテーブルまで戻ります。2 ペインのシーンでは、お気に入りのリストが詳細の横に画面上へ残ります。
- **warm** — それより深いスタックはその履歴を保ちます。合成されたルートはすでにあらゆるスタックの下にあり、残りのエントリは **move-to-top** によって着地するため、同じ順序が上に形成されます — お気に入りのセッションリンクはお気に入りタブを浮上させ（またはプッシュし）、その上に詳細を置きます。ルートだけを保持する合成スタックはルートそのものを名指しし、move-to-top はそれをタブバーと同じやり方で浮上させます。

自身のデスティネーションではなく、デスティネーションの状態を名指しするリンクは、その状態をスタックと並べて運びます。`DeepLink.Timetable` はその URI が名指しする day セグメントを保持し、`DeepLinkEffect` はナビゲートする前にその日付を `TimetableDayRequestStore`（`:feature:sessions` の `AppScope` シングルトン）に書き込みます。store は 1 つの要求をバッファし、それを一度だけ引き渡すため、タイムテーブルの presenter は画面が composition されるたびにそれを `selectedDay` に適用し、その後に読み手が選んだ日付はそのまま残ります。

エントリが 1 つだけのスタックが intent のフラグではなくコールドスタートのシグナルとなるため、このルールはプラットフォーム中立なままです。`StartupNavKey` は、起動フローをホストし、終わるとスタックから去るデスティネーションを示します — dev サーバーピッカーがこれを実装しており、永続化されたサーバー環境を復元し（設定がそう示していれば自動的にスキップします）、自身をタイムテーブルで置き換えます。ディープリンクが解決されるのはその後だけです。

## ウィジェットのトリガー

お気に入りウィジェットは、その状態によってタップを振り分けます。

| ウィジェットの状態 | タップの対象 | URI |
| --- | --- | --- |
| スケジュール / ライブ — セッションの行、またはちょうど 1 つのセッションを保持する small ウィジェットのライブ帯 | お気に入り surface を経由したそのセッション | `droidkaigi2026://favorites/session/{id}` |
| スケジュール — ウィジェットの背景 | お気に入りタブ | `droidkaigi2026://favorites` |
| カンファレンス終了後 — 任意のタップ | About タブ | `droidkaigi2026://about` |
| 空、本日終了 — 任意のタップ | ウィジェットの日付のタイムテーブルタブ | `droidkaigi2026://timetable/day1` \| `.../day2` |
| 一日のまとめ — 任意のタップ | Day 2 のタイムテーブルタブ | `droidkaigi2026://timetable/day2` |
| カウントダウン、イベント当日 — 任意のタップ | 開始デスティネーションでの素の起動 | — |

共有スロットはセッションの選択を開いたままにするため、そのライブ帯は背景と同じように起動します。

iOS では同じ表が、各状態が設定する `widgetURL` として表現され、ライブのセッション行は自身の `Link` を持ちます。`onOpenURL` がその URL を `KaigiAppHost.submitDeepLink(url:)` に渡し、同じ `DeepLinkStore` に到達します。ウィジェット自体については [iOS のお気に入りウィジェット](./ios-favorites-widget.ja.md) を参照してください。

関連: [ナビゲーション概要](./navigation.ja.md) · [Navigator](./navigation-navigator.ja.md)
