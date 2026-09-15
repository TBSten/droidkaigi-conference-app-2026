# AppGraph と UiGraph

依存はライフタイムの異なる 2 つの入れ子になったグラフに存在します。

- **`AppGraph`**（`AppScope`）はプロセスの間ずっと生存します — データレイヤ、Soil クライアント、ロギング。
- **`UiGraph`**（`UiScope`）は 1 つの UI インスタンスの間だけ生存します — `AppNavigator` や集約された `AppEntryProvider` といったナビゲーション状態です。Android では各 `Activity` が自身の `UiGraph` を持つため、同時に起動された複数の Activity がバックスタックの navigator を共有することはありません。

## AppGraph

`AppGraph` は **素のインタフェース**（契約）であり、プラットフォームごとに Metro の `@DependencyGraph` として実体化されます。`KaigiApp` はインタフェースだけに依存し、それを `context(AppGraph)` パラメータ経由で受け取るため、共有 UI はどのプラットフォームがそれを実体化したかを知りません。

```kotlin
interface AppGraph {
    val uiGraph: UiGraph
    val appInitializer: AppInitializer // run by the platform entry point during startup
    val rootTabNavigator: RootTabNavigator // bridge to shells outside the Compose tree
}

@DependencyGraph(scope = AppScope::class)
interface AndroidAppGraph : AppGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): AndroidAppGraph
    }
}
```

- グラフが終端のアプリモジュールでプラットフォームごとに実体化される理由は 2 つあります。
  - **すべての contribution を見渡せなければならないためです。** グラフは自身のコンパイルクラスパス上にある `@Contributes*` の binding だけを集約するため、すべての feature モジュールと core モジュールが見える位置になければなりません。`app-shared` がそれらを `api` 依存として持ち、すべての終端モジュールがそれに依存するため、各グラフは全体を見渡せます。
  - **各プラットフォームが独自の依存を渡せるためです。** グラフのファクトリによって、プラットフォームのホストは生成時にプラットフォーム固有の値を注入できます — Android では `Context`（`create(context)`）、iOS では実装が Swift にある Kotlin インタフェースになる可能性があります。追加で何も必要としないプラットフォームは引数なしの `createGraph()` で構築します。
- それ以外はすべて `@Contributes*` でモジュールをまたいで contribute されるため、グラフには手作業の結線がほとんどありません。

## 生成される API プロバイダ

`:core:data` にある Ktorfit の API インタフェースに `@ProvidedApi` を付けると、KSP が API ごとのプロバイダの三点セットを生成します。`<Api>Provider` インタフェース、本番の `Ktorfit` 上で API を構築し `@ContributesBinding(AppScope)` で bind される `Default<Api>Provider`、そして API 自体をグラフに公開する `provide<Api>` ブリッジです。

```kotlin
// generated for @ProvidedApi TimetableApi
@Inject @SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultTimetableApiProvider(ktorfit: Ktorfit) : TimetableApiProvider {
    override val api: TimetableApi = ktorfit.createTimetableApi()
}
```

`:feature:debug` は dev ビルドでデフォルトを差し替えます。`EnvironmentAwareTimetableApiProvider` が `@ContributesBinding(AppScope::class, replaces = [DefaultTimetableApiProvider::class])` で contribute され、呼び出しを fake または debug 画面が選択した環境へルーティングします。置き換えが同じ binding に乗るため、`:feature:debug` がクラスパスにある場所ではどこでもグラフが自動的にそれを解決します。

## 横断的な binding

一部の binding は生成されるのではなく、グラフに直接宣言されます。`CommonAppBindings`（`app-shared`）は Soil の `SwrClientPlus` と、それが利用する `ErrorRelay` を提供します。この relay は常に bind され、最新のレコードだけを保持するため、エラーの表面化にプラットフォームごとの結線は不要です。[エラーハンドリング](./error-handling.ja.md) を参照してください。

## UiGraph

`UiGraph` はアプリグラフの `@GraphExtension(UiScope::class)` であり、`app-shared` に宣言されています。`AppGraph` はそれを素のアクセサとして宣言し — `appGraph.uiGraph` を読むたびに新しいグラフが構築されます — `KaigiApp` は `retain` で 1 つを保持するため、設定変更をまたいで生き延びつつ、UI インスタンスとともに破棄されます。

```kotlin
@GraphExtension(UiScope::class)
interface UiGraph {
    val appNavigator: AppNavigator
    val appEntryProvider: AppEntryProvider
    val swrClient: SwrClientPlus
    val themeColorSchemeSubscriptionKey: ThemeColorSchemeSubscriptionKey
    // …
}

// KaigiApp
val uiGraph = retain { appGraph.uiGraph }
```

`@GraphExtension.Factory` が必要になるのは、生成そのものを別のクラスに *注入* しなければならない場合か、グラフが引数を取る場合だけです。`AppGraph` は `KaigiApp` に直接渡されるため、アクセサで十分です。

ライフタイムが 1 つの UI インスタンスであるものはすべて `UiScope` に bind されます。`AppNavigator` は `@SingleIn(UiScope::class)` であり、feature は `@ContributesIntoSet(UiScope::class)` で `NavEntryProvider` を contribute し、画面ごとのグラフファクトリは `@ContributesTo(UiScope::class)` で contribute されます — したがって画面グラフは `UiGraph` の拡張であり、UI スコープの binding を注入できます。プロセスライフタイムの binding が UI スコープのものに依存してはいけません。Metro は逆向きのエッジをコンパイル時に拒否します。

アクセサは binding のスコープではなく利用者に従います。UI シェルが読むものはすべて — `SwrClientPlus` やロガーのようなアプリスコープの binding も含めて — `UiGraph` に公開され、一方 `AppGraph` は UI インスタンスより前あるいは外から到達可能でなければならないものだけを保持します（`UiGraph` のアクセサ、エントリポイントが起動時に実行する `AppInitializer`、そして Swift に面する `RootTabNavigator`）。

関連: [ScreenContext の設計](./screen-context.ja.md) · [画面ごとのグラフ（@GraphExtension）](./di-screen-graph.ja.md)
