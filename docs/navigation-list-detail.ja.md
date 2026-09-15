# リスト-詳細シーン（ListDetailSceneStrategy）

EXPANDED のウィンドウ — 幅 840dp 以上 — では、画面を開くリストは**リスト-詳細**としてレンダリングされます。リストは一方のペインとして表示されたまま残り、それが開いた画面がもう一方を占めます。その組み合わせは、Timetable・Favorites・Search とセッション詳細、About と Settings・Sponsors・Contributors・Staff・Licenses、そして Event map と Stamp collecting です。MEDIUM のウィンドウが 1 ペインなのは、Material が推奨する directive がレイアウトを水平方向に分割するのが expanded の幅のブレークポイントからだからです。ストラテジは標準の Material3 adaptive のもの、`org.jetbrains.compose.material3.adaptive:adaptive-navigation3` であり、アプリ独自のペイン分離とドラッグの振る舞いを担わせるために `rememberKaigiListDetailSceneStrategy`（`app-shared/.../KaigiListDetailSceneStrategy.kt`）としてラップされ、`NavDisplay` の `sceneStrategies` に渡されます。

```kotlin
// rootSceneStrategy is FIRST: see "Ordering" below.
sceneStrategies = listOf(
    rootSceneStrategy,
    rememberLoneListPaneSceneStrategy(),
    rememberKaigiListDetailSceneStrategy(),
    SinglePaneSceneStrategy(),
)
```

このライブラリは、アプリが提供するすべてのターゲット、すなわち android、jvm（デスクトップ）、iosArm64、iosSimulatorArm64、wasmJs で解決されます。

## エントリのメタデータによるペインの宣言

このストラテジを使うには、各 `NavEntry` に、それが演じるペインのメタデータを与えます — リストのエントリには `listPane()`、詳細のエントリには `detailPane()` です。ストラテジは、同じシーンキーを共有する隣接したエントリを 1 つの `ListDetailPaneScaffold` にまとめます。

```kotlin
// Timetable (the list). It already carries RootSceneStrategy.root() for the home-root predictive-back
// behavior; listPane() is merged in so the same entry is also the list pane.
entry<TimetableNavKey>(metadata = RootSceneStrategy.root() + listPane()) { ... }

// TimetableItemDetail (the detail), shared by the Timetable, Favorites and Search.
entry<TimetableItemDetailNavKey>(metadata = detailPane()) { ... }
```

`listPane()` と `detailPane()`（`core/common`）はライブラリのペインのメタデータをラップし、`listPane()` は後述の lone-list ストラテジが読む印を追加します。どちらも `Map<String, Any>` を返すため、既存の `RootSceneStrategy.root()` のマップと素の `+` で合成できます。

リストペインが別のリストペインの上に置かれることもあります — Search は Timetable の上に開きます — その場合、scaffold は詳細を最上位のリストと組み合わせ、詳細からの back はそのリストに戻ります。

ウィンドウへの適応はライブラリ自身の関心事です。`rememberListDetailSceneStrategy` は内部でウィンドウサイズを読み取り、compact なウィンドウでは 1 ペインに折りたたみ、その後 `null` を返してエントリを `SinglePaneSceneStrategy` に落とします — このコードベースのどこでもウィンドウの計測は必要ありません。

## ストラテジの順序が重要

Timetable のエントリは `RootSceneStrategy.root()` と `listPane()` の両方を持つため、先に来たストラテジがそれを取ります。`rootSceneStrategy` は `listDetailSceneStrategy` より前に置かれています。Timetable が最上位にあるときは back は依然としてアプリを終了し（[`RootSceneStrategy`](./navigation-predictive-back-tabs.ja.md)）、詳細が最上位にあるときは譲って 2 ペインの scaffold が形成されます。逆にすると、その下にある 2 つのストラテジのいずれかが単独の Timetable エントリを取り、その下のエントリから `previousEntries` を導出してしまい、ホームからの back は終了ではなくスタッシュされたタブを見せることになります。

## 横に何も開かれていないリスト

scaffold はウィンドウが提供するすべてのパーティションを埋め、入れる詳細エントリがなくても詳細ペインを広げるため、バックスタックの最上位にあるリストエントリは、空のプレースホルダーを相手に 1 ペイン分の幅でレンダリングされることになります。`rememberLoneListPaneSceneStrategy`（`core/common`）はその位置にある `listPane()` のエントリを取り、それを 1 ペインとしてレンダリングします。そのために `sceneStrategies` ではリスト-詳細ストラテジより前に置かれ、すでに Timetable を取っている `rootSceneStrategy` より後に置かれています。

## 詳細が開いている状態での back

back は詳細だけをポップし、リストがウィンドウ全体を占めるため、`rememberKaigiListDetailSceneStrategy` は `BackNavigationBehavior.PopLatest` を渡します。ライブラリのデフォルトである `PopUntilScaffoldValueChange` は、scaffold のレイアウトを同じままにするエントリをすべて飛ばして戻りますが、詳細を閉じてもそのレイアウトは変わりません — 空いたペインはリストのプレースホルダーの周りで広がったままです — そのためシーンは戻り先のエントリがないと報告し、押下はアプリを離れることになってしまいます。

## 適応する back アイコン

同じ詳細画面が、異なるアフォーダンスを持つ 2 つの状況で現れます。

- **単一ペイン**（compact なウィンドウ、またはナビゲーションで到達した場合） — トップバーには **←** が表示されます。「戻る」です。
- **詳細ペイン**（リストの横） — リストがまだ画面上にあるため **←** は誤って読まれます。自然なアフォーダンスは **✕**、「このペインを閉じる」です。

どちらも同じ動作（詳細の `NavKey` をポップする）を行い、異なるのはアイコンだけです。そして画面 — 純粋な `commonMain` — はウィンドウサイズを読み取ってはいけません。

ライブラリは、その scaffold の内側でレンダリングされるコンテンツに対して、まさに必要なシグナルを公開しています。`CompositionLocal<ListDetailSceneScope?>` である `LocalListDetailSceneScope` です。これが非 null になるのは、エントリがリスト-詳細の scaffold の内側で composition されている間**だけ**です。ストラテジは `paneCount <= 1` のときは常に単一ペインのレンダリングに譲るため、非 null のスコープは「自分はリストの横の詳細ペインである」ことを確実に意味します。詳細画面はそれを直接読み取ります。

```kotlin
IconButton(onClick = onBack) { // the same pop, whichever icon shows
    if (LocalListDetailSceneScope.current != null) {
        Icon(Icons.Filled.Close, contentDescription = "Close")
    } else {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}
```

このように、適応するアイコンは完全にライブラリが提供する local に由来します。

## ペインの分離（LocalPanePartitionSpacerSize）

scaffold 自身の溝は閉じられています。`rememberKaigiListDetailSceneStrategy` は Material の directive を `horizontalPartitionSpacerSize = 0.dp` でコピーするため、2 つのペインの背景は継ぎ目で接し、その間にウィンドウ背景の帯は入りません。分離は代わりにペインの側に属し、`CompositionLocal` として運ばれます。

- **提供側** — `KaigiNavDisplay` が `NavDisplay` 全体を囲んで `LocalPanePartitionSpacerSize`（`core/ui`）を提供します。その値は、ペインが共有される境界に向けて確保しなければならない幅です。
- **消費側** — ペインは、実際に別のペインの横にある間だけこの値を適用します。適応アイコンと同じ `LocalListDetailSceneScope` のゲートを使います。inset は、ペインの端まで伸びる背景の**内側**になければなりません。背景の外側に適用されたパディングは、directive が閉じた帯を再び開いてしまいます。

```kotlin
@Composable
fun paneStartInset(): Dp =
    if (LocalListDetailSceneScope.current != null) LocalPanePartitionSpacerSize.current else 0.dp
```

`paneStartInset()`（`core/ui`）がそのゲートであり、詳細ペインはそれを自身が描画するすべてに適用します。`TimetableItemDetailScreen` はそれを、各セクションが背景とコンテンツの間に適用する `contentInsets` に織り込み、他の詳細ペインはスクロールコンテナのコンテンツパディングの start に織り込みます。`KaigiLargeTopAppBar` はそれを自身の back コントロールとタイトルに適用するため、そのバーを使う画面はコンテンツだけを面倒見ればよくなります。この契約を機械的に強制するものはなく、それを省いたペインはコンテンツを継ぎ目に突き当ててしまいます。

リストペインは inset を取ってはいけません。`LocalListDetailSceneScope` は両方のペインで非 null ですが、継ぎ目に接するのは詳細ペインの start エッジだけです。

## オフセットされたペインでの window inset

`WindowInsets` はウィンドウ全体を記述するため、両方のペインは位置にかかわらず同じエッジの値を読みます。隣のペインによってウィンドウの端から離されているペイン — 詳細ペインの start、リストペインの end — は、それでもそのエッジのシステムバーやディスプレイカットアウトの inset の分だけコンテンツにパディングを入れてしまい、継ぎ目に対して隙間を開けてしまいます。

ペインのメタデータは、そのエントリが接しないエッジを名指しし、1 つの decorator がそれを consume します。

```kotlin
entry<TimetableNavKey>(metadata = RootSceneStrategy.root() + listPane()) { ... }

entry<TimetableItemDetailNavKey>(metadata = detailPane()) { ... }
```

各ペインが接しないエッジはその役割から決まるため — リストペインの継ぎ目は end エッジ、詳細ペインのそれは start エッジです — `listPane()` と `detailPane()` が consume のメタデータを自ら持ちます。

`KaigiNavDisplay` の `entryDecorators` にある `rememberListDetailPaneInsetsNavEntryDecorator` は、このメタデータを持つすべてのエントリを `Modifier.consumeWindowInsets` で包み、名指しされたエッジを consume するのは `LocalListDetailSceneScope` が非 null の間だけなので、単一ペインの画面はウィンドウの inset をそのまま保ちます。`Scaffold` と `KaigiTopAppBar` は consume された inset を差し引くため、画面はパディングを変わらず読み取れます。

このラップが無条件なのは、包むかどうかの判断がエントリの生存期間を通じて変わってはならないからです。[single call site ルール](./navigation-retain-entry-decorator.ja.md#エントリごとに-1-つの呼び出し箇所) を参照してください。

## ペイン内の lazy コンテナ

ペインのエントリのコンテンツは `movableContentOf` の内側にあるため、詳細を開閉すると、エントリはその場で recomposition されるのではなく composable 呼び出し階層上の新しい位置に移動します（[single call site ルール](./navigation-retain-entry-decorator.ja.md#エントリごとに-1-つの呼び出し箇所) を参照）。したがって、ペインのエントリが描画するすべての lazy コンテナは、その状態を `core/ui` から取らなければなりません。

```kotlin
LazyColumn(state = rememberListDetailSceneAwareLazyListState()) { ... }
LazyVerticalGrid(state = rememberListDetailSceneAwareLazyGridState(), columns = ...) { ... }
```

どちらのヘルパーも状態を `LocalListDetailSceneScope` でキーイングするため、移動によって新しい状態が構築され、スクロール位置が引き継がれます。移動をまたいで保持された状態は、2 つの形で壊れます。

- scaffold はペインを `LookaheadScope` の内側でレイアウトし、lazy の状態は最初に見た lookahead パスに固定されます。いったん詳細が閉じると lookahead パスは二度と実行されず、状態はそこで最後に見た値のまま凍結するため、リストはそのオフセットより先へスクロールしなくなります。この固定にはリセットがありません: https://issuetracker.google.com/issues/552354343
- composition の途中で一時停止したプリフェッチは、後のフレームで再開して適用され、記録された操作を、その間に移動によって detach され再度 attach されたノードに対して再生するため、`onReuse is only expected on attached node` という事前条件に失敗します。ヘルパーは状態が置き換えられる際に、未処理のまま残っているプリフェッチのハンドルをすべてキャンセルします。これはエントリを移動させるフレームがまだ走っている間、つまりプリフェッチャがメインスレッドで次の順番を得る前に行われます。

## 分割のリサイズ

scaffold は継ぎ目の上にドラッグハンドル（`paneExpansionDragHandle`）を表示します。ドラッグを離すと、分割は 3 つのアンカー — リストが `PaneMinWidth`、50:50、詳細が `PaneMinWidth` — のうち最も近いものに落ち着くため、端のアンカーはペインが静止するときの最小幅になります。ポインタが押されている間は、`PaneExpansionDragBounds` が状態の `consumeDragDelta` フックを通じて、継ぎ目を端のアンカーの先までラバーバンドさせます。抵抗は距離とともに増し、`PaneMaxOvershoot` で動きは完全に止まります。一方、境界に向かって戻る方向のデルタは手を加えられずに通過するため、離したときのアニメーションは影響を受けません。

ハンドルのスロットは、継ぎ目のリスト側に細いグラデーションの帯も描画します。ペインは自身の境界にクリップされるため、詳細ペインは継ぎ目をまたいで実際の影を落とせません。両方のペインの上に置かれたハンドルのスロットから描かれるこの帯が、その高度の手がかりの代わりを務めます。

これらはすべて `app-shared/.../KaigiListDetailSceneStrategy.kt` にあります。ジオメトリは左から右へのレイアウトを前提としており、これはアプリが提供するすべてのロケールで成り立ちます。

関連: [ルートタブバー（RootTabSceneDecorator）](./navigation-root-tab-bar.ja.md) · [ルート NavEntry のエミュレーション（RootSceneStrategy）](./navigation-predictive-back-tabs.ja.md)
