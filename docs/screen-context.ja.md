# ScreenContext の設計

画面の依存関係は、画面ごとの Metro グラフの上に構築された、context parameters が運ぶ 2 つの **role context** を通じて注入されます。このページでは、その構造と、2 つの役割を分離し続けるためのルールを定義します。

## 概要

- **2 つの role context** は、どちらも具象の `@Inject` クラスです。`ScreenContext`（Root の依存関係 — [QueryKey / SubscriptionKey](./soil-keys.ja.md) など）と `PresenterContext`（Presenter の依存関係 — [MutationKey](./soil-mutation.ja.md) など）です。どちらも `val logger: KaigiLogger` を宣言しているため、すべての画面が診断のための 1 つの接点を持ち、channel の effect は失敗したハンドラを報告できます。
- **すべての画面が同じ 3 つの部品を使います**。scope marker、画面ごとの `@GraphExtension`、そして `@SingleIn` な ScreenContext クラスです。
- **ScreenContext は PresenterContext をプロパティとして保持し**、決してそれを実装してはいけません — 役割は別々の型のままです。
- **入出力は context の型によってゲートされます**。action は `PresenterContext` の下でのみ、result は `ScreenContext` の下でのみ消費できます。

## 画面ごとの構造

画面ごとに 3 つの宣言があります（「その画面は NavKey 引数を取るか?」という分岐はありません — key を取る画面も同じ形を使います）。

1. `:core:model` にある **scope marker**（`sealed interface TimetableScreenScope`） — 最下層であり、`:core:data` の binding からも feature の extension からも同じように参照されます。
2. feature にある**画面ごとの `@GraphExtension`**。`val screenContext` を公開します（feature が Navigator のファサードを持つ場合は `val screenNavigator` も）。
3. 具象の **`@Inject @SingleIn(<ScreenScope>::class)` な ScreenContext クラス**。

```kotlin
// :core:model — the scope marker
sealed interface TimetableScreenScope

// feature — the per-screen graph extension
@GraphExtension(TimetableScreenScope::class)
interface TimetableScreenGraph {
    val screenContext: TimetableScreenContext
    val screenNavigator: TimetableScreenNavigator

    @GraphExtension.Factory
    @ContributesTo(UiScope::class)
    fun interface Factory {
        fun createTimetableScreenGraph(): TimetableScreenGraph
    }
}

// feature — both role contexts are concrete @Inject classes
@Inject
class TimetablePresenterContext(
    val favoriteTimetableItemIdMutationKey: FavoriteTimetableItemIdMutationKey,
    override val logger: KaigiLogger,
) : PresenterContext

@Inject
@SingleIn(TimetableScreenScope::class)
class TimetableScreenContext(
    val timetableQueryKey: TimetableQueryKey,
    val favoriteTimetableIdsSubscriptionKey: FavoriteTimetableIdsSubscriptionKey,
    override val logger: KaigiLogger,
    val presenterContext: TimetablePresenterContext, // the instance, held as a property
) : ScreenContext
```

## 役割を分離し続ける

- **ScreenContext は `PresenterContext` のインスタンスをプロパティとして保持します。同時に `PresenterContext` を実装してはいけません**（継承ではなくコンポジション）。実装してしまうと、Presenter 専用の effect が Root 全体でコンパイル可能になります — `ScreenContextMustNotBePresenterContext` チェッカーがそれを禁止します。
- 保持される値は **`Provider` ではなくインスタンス**です。`PresenterContext` はステートレスな依存関係の入れ物であり（Soil の mutation の状態は id をキーとして `SwrClient` に存在します）、エントリごとに作り直しても何もリセットされません。
- **context の型はスコープ付きのヘルパーも解放します**。ヘルパー関数は `context(_: ScreenContext)` / `context(_: PresenterContext)` パラメータを宣言するため、その context がスコープ内にある場所でしかコンパイルされません — `ActionResultEffect` は `ScreenContext` を必要とし、`ActionEffect` は `PresenterContext` を必要とします。
- **Root は presenter context を Presenter の呼び出しにだけ絞って供給します**。`val uiState = context(screenContext.presenterContext) { xxxScreenPresenter(...) }` とし、Screen の呼び出しはブロックの外に置きます。`NoPresenterEffectInScreenRoot` チェッカーが、そのブロックを他のあらゆる Presenter 役割の呼び出しから封じます。

## ライフタイムと閉じ込め

- NavEntryProvider は**グラフ**を一度だけ `retain` します。ScreenContext は `@SingleIn(scope)` であるため、グラフが retain されている間、アクセサを読むたびに同じインスタンスが返されます — ナビゲーションの往復（Timetable → Detail → 戻る）をまたいでも同様です。
- `@SingleIn` は**閉じ込め**も行います。ScreenContext は app グラフと UI グラフからは解決できず、グラフの factory だけが到達手段です。画面スコープの Navigator の binding についても同じことが成り立ちます（[Navigator](./navigation-navigator.ja.md)）。

## Factory の命名

contribute された `@GraphExtension.Factory` はすべて `UiGraph` にマージされ、そこでは戻り値の型だけが異なる引数なしの `create()` オーバーロードが衝突します（Kotlin のエラー）。そのため factory のメソッドは画面の名前を持ちます — 例外のない一律のルールです: `createTimetableScreenGraph()`、`createTimetableItemDetailScreenGraph(id)`、… 素の `create(id)` についてシグネチャの一意性に頼ると、id を取る 2 つ目の画面が現れた時点で破綻します。

## スコープ: アクセサ vs. コンストラクタ

- **グラフのアクセサとして公開される** binding（`screenContext`、`screenNavigator`）は `@SingleIn(scope)` です — アクセサはグラフが retain されている間、安定していなければなりません。
- **コンストラクタで消費される** binding（例: `:core:data` で `@ContributesBinding(scope)` により bind され、画面ごとの `MutationTag` を取る favorite の mutation key）は **unscoped のまま**です — 消費側の `val` がそれらを固定します。

key を取る画面では、NavKey の id が factory の `@Provides` パラメータを通じてスコープに入ります。それは `@SingleIn` な ScreenContext に保持され、ScreenContext はそれを使って共有の timetable query から単一の項目を `select` します（id から導出される別のキーはありません）。画面ごとの `MutationTag` も同じ方法で提供されます。

```kotlin
@GraphExtension(TimetableItemDetailScreenScope::class)
interface TimetableItemDetailScreenGraph {
    val screenContext: TimetableItemDetailScreenContext

    @Provides
    private fun provideMutationTag(): MutationTag = MutationTag("TimetableItemDetailScreen")

    @GraphExtension.Factory
    @ContributesTo(UiScope::class)
    fun interface Factory {
        fun createTimetableItemDetailScreenGraph(
            @Provides timetableItemId: TimetableItemId,
        ): TimetableItemDetailScreenGraph
    }
}
```

画面のグラフに閉じ込めるべき依存関係はすべて同じ方法で注入されます — factory（またはグラフ）の `@Provides`、あるいは画面スコープへの `@ContributesBinding` です。現在のコードベースには、これら以外の例はありません。

## NavEntryProvider（両方の形）

NavEntryProvider はグラフの `Factory` だけを注入し（`back()` が必要な場合は `AppNavigator` も）、グラフを一度 retain して、アクセサを読みます。

```kotlin
// (1) No NavKey argument: retain the graph once
entry<TimetableNavKey> {
    val graph = retain { screenGraphFactory.createTimetableScreenGraph() }
    context(graph.screenContext) {
        TimetableScreenRoot(onNavigateToDetail = { graph.screenNavigator.openSessionDetail(it) })
    }
}

// (2) With a NavKey argument: retain the graph per id
entry<TimetableItemDetailNavKey> { key ->
    val graph = retain(key) { screenGraphFactory.createTimetableItemDetailScreenGraph(key.id) }
    context(graph.screenContext) {
        TimetableItemDetailScreenRoot(onNavigateBack = { appNavigator.back(origin = key) })
    }
}
```

## 却下した代替案

- **Graph-as-ScreenContext**（グラフの extension 自体が ScreenContext を実装する）: recomposition ごとのキーの入れ替わりを再び持ち込み、さらに navigator のアクセサを ScreenContext に載せてしまいます — それは Root へ漏れ出すことになり、`NavigatorConfinedToNavEntry` チェッカーがそれを禁止します。
- **ScreenContext が PresenterContext を実装する**: 型レベルの ScreenChannel のゲートを命名規約へ格下げします — 「役割を分離し続ける」を参照してください。

関連: [画面ごとのグラフ（@GraphExtension）](./di-screen-graph.ja.md) · [画面の実装](./building-a-screen.ja.md) · [Enforcement](./enforcement.ja.md) · [アーキテクチャ概要](./architecture-overview.ja.md)
