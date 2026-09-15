# Soil のキー

Soil は repository クラスを置き換えます。サーバやデータベースの状態はそれぞれ型付きの **key** であり、`SwrClient` がランタイムのキャッシュを担います。宣言は `:core:model` に (`typealias` として)、実装 (`Default*Key`) は `:core:data` にあります。

どの key を使うか:

| Key | 用途 | 例 |
| --- | --- | --- |
| `QueryKey<T>` | **一度きりの読み取り** — 値を一度取得してキャッシュする | API からタイムテーブルを取得する |
| `SubscriptionKey<T>` | **継続的に更新される読み取り** — `Flow` を監視し、emit のたびに再描画する | DataStore に保存されたお気に入り id やテーマ |
| `MutationKey<R, V>` | **書き込み** — ストアを更新したり API を呼び出したりし、成功/失敗を観測する | お気に入りのトグル、プロフィールの更新 |

```kotlin
// :core:model — the contract a feature depends on
typealias TimetableQueryKey = QueryKey<Timetable>
typealias FavoriteTimetableIdsSubscriptionKey = SubscriptionKey<PersistentSet<TimetableItemId>>
typealias FavoriteTimetableItemIdMutationKey = MutationKey<FavoriteToggle, TimetableItemId>
```

```kotlin
// :core:data — implementation, bound into the graph
@ContributesBinding(AppScope::class)
@Inject
class DefaultTimetableQueryKey(
    private val api: TimetableApi,
    private val fileStorage: ServerEnvironmentScopedFileStorage,
) : TimetableQueryKey by buildPersistedQueryKey(
        id = SoilIds.timetableQuery, // generated; no hand-written namespace string
        persistKey = "timetable",    // stable, explicit persisted-cache identity
        fileStorage = fileStorage,
        fetchResponse = { api.getTimetable() },
        transformToDomainModel = { response -> Timetable(items = response.toTimetableItems().toPersistentList()) },
    )
```

- feature が依存するのは `typealias` (model) だけであり、`Default*Key` の実装に依存してはいけません。
- 変数の型や結果の型が Compose UI の型 (`ImageBitmap` など) である key は、`:core:model` と `:core:data` が Compose UI に依存していないため、その `typealias` と実装をそれを使う feature モジュールに宣言します。
- ファイル配置: 両側とも **key ごとに 1 ファイル** です。contract のファイル (typealias にちなんだ名前) は `typealias` と専用の入力/結果 data class を持ち、実装は `Default*Key` ごとに 1 ファイルです。共有のインデックスファイルは、すべての pull request が触れるマージ地点になってしまいます。
- 生の API レスポンスからモデルへの重い整形は、presenter ではなく `fetch` の中 (データ層) で行います。
- **派生した読み取り**は、新しい key を追加せず共有の key を再利用します。詳細画面は `rememberQuery(key, select = { it.items.first { … } })` で共有のタイムテーブルキャッシュから単一のアイテムを選択するので、タイムテーブル全体を再取得することはありません。id ごとの独立した key が正当化されるのは、専用の詳細 API が存在する場合だけです。
- ランタイム引数が本当に必要な key は、アプリ全体のシングルトンにはできません。代わりにその画面の **`@GraphExtension` スコープ**にバインドし、画面のグラフファクトリが id を供給します。[ScreenContext の設計](./screen-context.ja.md) を参照してください。

## 生成される key の id (`SoilIds`)

すべてのランタイム id (`QueryId`/`SubscriptionId`/`MutationId`) は**生成**されるものであり、手書きしてはいけません。KSP プロセッサ (`:tools:ksp-processor` の `SoilIdsGenerator`、`:core:model` と `:core:data` の `kspCommonMainMetadata` 経由で組み込まれています) がコンパイル対象のモジュールを走査し、展開後の型が Soil の key である `typealias` を見つけて、そのモジュールに `SoilIds` オブジェクトを出力します。各 id の名前空間は **contract の typealias の FQN** です。コンパイル時に読み取られるため wasm の `qualifiedName` の制約は当てはまらず、実装のリネームや差し替えをまたいで同一性が保たれます。

```kotlin
// generated into :core:model
public object SoilIds {
  public val timetableQuery: QueryId<Timetable> =
      QueryId("io.github.droidkaigi.confsched.core.model.TimetableQueryKey")
  public val favoriteTimetableIdsSubscription: SubscriptionId<PersistentSet<TimetableItemId>> = …
  // mutations become a factory taking the per-screen MutationTag (see below)
  public fun favoriteTimetableItemIdMutation(extraTag: MutationTag): MutationId<FavoriteToggle, TimetableItemId> =
      MutationId("io.github.droidkaigi.confsched.core.model.FavoriteTimetableItemIdMutationKey", extraTag.value)
}
```

- プロパティ名 = typealias の単純名から末尾の `Key` を除いた lowerCamel (`TimetableQueryKey` → `timetableQuery`)。
- Query/Subscription の id は `val` ですが、**Mutation** の id は画面ごとの `MutationTag` を受け取り、それを `MutationId` に焼き込む `fun` です。これにより `MutationKeyMustCarryTag` チェッカーを満たします (ファクトリ呼び出しを通じて tag が `id` 引数に流れ込みます)。
- **key をまたぐ副作用は定数を共有します。** id が共有されたシンボルであるため、ある key の `mutate` が名前空間の文字列を重複させることなく別の key のキャッシュを無効化できます。

```kotlin
// after toggling a favorite, invalidate the timetable query so the grid re-reads
queryClient.invalidateQueriesBy(SoilIds.timetableQuery)
```

## データソースの合成

あるデータが複数の形で必要になる場合や、ある取得処理が別の取得結果を必要とする場合:

1. **同じデータの別のビュー** — 共有の key を `rememberQuery(key, select = …)` で再利用し、key を追加しません。
2. **別のリクエストの結果を必要とする取得処理** — 1 つの key の `fetchResponse` の中でリクエストを連鎖させます (リクエストの構築は API 層が所有するので、連鎖は `api.a()` + `api.b(…)` になります)。集約されたレスポンスは 1 つのペイロードとして永続化され、失敗はアトミックです。

query が他の query を読まないのは意図的です。query をまたぐ依存は、あるキャッシュの鮮度ルールを別のキャッシュの中に隠してしまいます。連鎖の中間結果にも独自の画面が必要な場合は、それ専用の key を与えて独立して取得します。リクエストの構築は API 層が共有し続けます。

## `typealias` と Metro のバインド

各 key は `typealias` であり、基底の型に消去されます。新しい型ではありません。したがって 2 つの key が**同じ**基底型に解決される場合、`@ContributesBinding` はその 1 つの型に対して 2 つのバインドを登録し、Metro は**重複バインド**エラーでビルドを失敗させます (`typealias` の*名前*が異なっていても助けにはなりません。比較されるのは基底の型だけです)。

```kotlin
// both erase to MutationKey<Unit, TimetableItemId> → duplicate binding
typealias UpdateFavoriteSessionMutationKey = MutationKey<Unit, TimetableItemId>
typealias MarkSessionSeenMutationKey       = MutationKey<Unit, TimetableItemId>
```

query がこれに当たることはまれです (各 `QueryKey<T>` は固有のペイロードを持ちます)。mutation はより起こりやすく、空のケースが `Unit` に潰れるためです。引数のないアクションはすべて `MutationKey<Unit, Unit>` になります。

2 つの key が本当に同じ型を必要とする場合は、Metro の **qualifier** (`dev.zacsweers.metro` の `@Qualifier` / `@Named`) で区別します。各 `Default*Key` にそれぞれの qualifier を付与すると `@ContributesBinding` が生成されるバインドにそれを伝播させ、2 つは別々のグラフのスロットになります。`typealias` と `… by buildMutationKey(...)` の委譲はそのまま保たれます。(これはランタイムの**キャッシュ**エントリを区別する Soil の `MutationId`/`QueryId` とは別のものであり、qualifier が区別するのは DI のバインドです。)

**グループ内のすべての key が qualifier を持ち、すべての注入箇所も持ちます。** 衝突しているペアの片方だけを qualify してもコンパイルは通り、しかし何も語りません。qualifier なしで要求する箇所は qualifier のない方のバインドに解決されるため、ある画面はパラメータ名とは異なる key を読むことになります。すべてに qualifier が付いていれば、着地してしまう qualifier なしのバインドは存在せず、アノテーションの付け忘れは代わりにバインド不足のエラーになります。

```kotlin
@Qualifier annotation class ClockOverlayEnabled

@Inject
@ClockOverlayEnabled
@ContributesBinding(DebugScreenScope::class)
class DefaultClockOverlayEnabledMutationKey(…) : ClockOverlayEnabledMutationKey by buildMutationKey(…)

class DebugPresenterContext(
    @ClockOverlayEnabled val clockOverlayEnabledMutationKey: ClockOverlayEnabledMutationKey,
)
```

**qualifier は、それが修飾する key と同じファイルに宣言します。**そうすることで、対を探し回るのではなく一緒に読めます。そのファイルがどのモジュールに置かれるかは key に従います。単一の feature に属するものはその feature のモジュールに置き (`ClockOverlayEnabledMutationKey` は `:feature:debug` にあり、dev 専用であるため production ビルドが到達できる場所に置いてはいけません)、それ以外は残りの contract と並んで `:core:model` に置きます。`:core:model` には現在 DI の依存がないため、そこに最初の qualifier を宣言するときに Metro プラグインをビルドスクリプトへ追加します。

`:core:common` はすでに Metro を持ち、すべての feature から見えるとしても、置き場所としては誤りです。qualifier は 1 つの key を指すものであり、このモジュールはすべての画面が組み立てられる土台を保持しています。また、それは qualifier を key から 1 モジュール離れた場所に置くことになり、それこそが両者を一緒に宣言することで避けたいことです。

qualifier は値についての事実ではなく、グラフについての事実であり続けます。`MutationKey<Unit, Boolean>` はどちらの key も正しく記述しており、両者は宣言では名前によって、注入箇所ではパラメータ名によって、キャッシュでは `MutationId` によってすでに区別されています。違いが見えないのはバインドの解決だけであり、そこが qualifier の対象とする層です。

## Mutation の入力型と結果型

`MutationKey<Result, Variable>` は 2 つの型引数を取ります。順序に注意してください。**`Result` が先** (`mutate` が返すもの)、**`Variable` が後** (入力) です。それぞれに何が入るか:

| 値の数 | 入力 (`Variable`) | 結果 (`Result`) |
| --- | --- | --- |
| 0 | `Unit` | `Unit` |
| 1 | ドメイン型をそのまま | ドメイン型をそのまま |
| 2 以上 | 専用の `…Input` data class | 専用の `…Result` data class |

ラップするのは、値が本当に 2 つ以上ある場合**だけ**であり、位置に依存する `Pair` / `Triple` 引数を避けるためです。key・その入力クラス・その結果クラスは**同じ動詞始まりの語幹**を共有し、いずれも `typealias` の隣の `:core:model` に置かれるので、1 つのセットとして読めます。

```kotlin
// :core:model — a mutation that needs several inputs and returns several values
typealias SubmitFeedbackMutationKey =
    MutationKey<SubmitFeedbackResult, SubmitFeedbackInput>

data class SubmitFeedbackInput(val sessionId: TimetableItemId, val comment: String, val rating: Int)
data class SubmitFeedbackResult(val id: FeedbackId, val createdAt: Instant)
```

値が 1 つであればその側にラッパーは不要です。`FavoriteTimetableItemIdMutationKey = MutationKey<FavoriteToggle, TimetableItemId>` は id を直接受け取り、結果だけをラップします。その結果は id と、トグルがお気に入りを追加したのか削除したのかの両方を運びます。

## 画面ごとの mutation tag の分離

Soil は mutation の状態を `MutationId(namespace, *tags)` をキーとして `SwrClient` に保持します。2 つの画面が**同じ** `MutationKey` のバインドに解決されると、1 つのキャッシュスロットを共有することになり、一方の画面が残した `Success`/`Error` の残留状態によって、もう一方の画面の成功/失敗のエフェクトが入場時に誤発火します。Query と Subscription のキャッシュは設計上共有されます (読み取りは読み取りです) が、mutation の状態はインタラクションごとのものであり、**画面ごと**でなければなりません。

この分離は慣習ではなく機械的です。すべての `MutationKey` 実装は (`:core:model` の `value class` である) `MutationTag` を受け取って `MutationId` に焼き込み、画面ごとの `@GraphExtension` がそれぞれの画面型の tag を `@Provides` します。mutation key のバインドは `AppScope` から画面ごとのスコープへ移ります。

```kotlin
// :core:data — one impl, bound into each screen scope that toggles favorites
@Inject
@ContributesBinding(TimetableScreenScope::class)
@ContributesBinding(TimetableItemDetailScreenScope::class)
@ContributesBinding(FavoritesScreenScope::class)
@ContributesBinding(SearchScreenScope::class)
class DefaultFavoriteTimetableItemIdMutationKey(
    extraTag: MutationTag,
    private val store: FavoritesStore,
) : FavoriteTimetableItemIdMutationKey by buildMutationKey(
    id = SoilIds.favoriteTimetableItemIdMutation(extraTag), // generated factory bakes the tag in
    mutate = { id -> FavoriteToggle(id = id, added = store.toggle(id)) },
)
```

どちらか一方を忘れるとコンパイルエラーになります。`MutationKeyMustCarryTag` FIR チェッカーは、`MutationTag` のコンストラクタパラメータを持たない、あるいはそれを `MutationId` に渡していない `MutationKey` 実装を拒否します。[Enforcement](./enforcement.ja.md) を参照してください。

関連: [SoilDataBoundary](./soil-data-boundary.ja.md) (画面ルートでの key の消費) · [Soil のミューテーション](./soil-mutation.ja.md) · [Soil の永続化](./soil-persistence.ja.md)
