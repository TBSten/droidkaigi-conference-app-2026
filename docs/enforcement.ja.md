# Enforcement

**AI がコードの主要な書き手である**ことを前提に、このプロジェクトの規約は — 正しさのルールもハウススタイルも同様に — コンパイラが担えるところではコンパイラが担います。すなわち、型によって表現不可能にするか、FIR checker によって拒否します。そのどちらでも判定できないものは、レビューとテストに委ねられます。

## 原則（強制メカニズムの優先順位）

1. **API/型によって不正な状態を表現不可能にする** — 最も強力で、**Kotlin のバージョンに依存せず**、IDE に理解されます。妥当なコードしか書けなくなります。
2. **K2 FIR Checker** — 型では表現できない**二値のルール**をコンパイルエラーに変えます。拡張 API は不安定で、Kotlin のバージョンごとにメンテナンスが必要です。
3. **レビュー / テスト** — データ量や意味的な依存関係など、静的に判定できないもの。

「レベル 1 で排除できるものは排除する（プラグインを書かない）」「型では表現できない二値のものにだけレベル 2 を使う」「曖昧なものは 3 へ」。

## Enforcement マップ

以下のいずれかのルールに違反すると、コンパイルが失敗します。型/境界によるルールにはプラグインは不要です。FIR checker はそれぞれ以下にサブセクション（拒否される例と理由）があります。

| ルール | メカニズム |
| --- | --- |
| Action は Presenter でのみ消費し、結果は Root でのみ消費する | 型 — `context(_: …Context)` |
| 結果は effect の内側でのみ emit する | 型 — `emit` が `suspend` |
| feature は ScreenChannel の受信側に触れられない | 可視性 — `internal` + モジュール境界 |
| feature 間の分離（他の feature の `NavKey` を import しない） | モジュール境界 — feature 間に Gradle のエッジがない（開発専用ツールである `:feature:debug` は対象外） |
| `NavKey` が `@Serializable` である | KSP 生成のシリアライザ登録 — 漏れはコンパイルエラー |
| プレビュー用アセットが本番に入らない | モジュール境界 — release では `:core:preview:impl` を除外 |
| `@MustBeSerializable` の型引数がシリアライズ可能である | FIR `MustBeSerializable` |
| `rememberSerializable` の型引数がシリアライズ可能である | FIR `RememberSerializable` |
| `mutate` を直接呼び出さない | FIR `NoDirectMutate` |
| Presenter は [`ScreenContext`](./screen-context.ja.md) を宣言してはいけない | FIR `PresenterMustNotDeclareScreenContext` |
| ScreenContext は PresenterContext のサブタイプではない | FIR `ScreenContextMustNotBePresenterContext` |
| screen root で presenter 用の effect を使わない | FIR `NoPresenterEffectInScreenRoot` |
| `Navigator` は NavEntry に限定する | FIR `NavigatorConfinedToNavEntry` |
| すべての [`MutationKey`](./soil-mutation.ja.md) が `MutationTag` を持つ | FIR `MutationKeyMustCarryTag` |
| Screen は Soil を直接読まない（ロールでゲートされる） | FIR `SoilReadConfinement` |
| `@Preview` には認められたラッパーが必要 | FIR `PreviewRequiresWrapper` |
| ナビゲーションのみのクリックを presenter 経由にしない | FIR `NoForwardOnlyAction` |
| テーマに依存するプレビューは `@PreviewParameter` を使う | FIR による読み取り + IR の `@ThemeSensitive` メタデータ |
| ロケールに依存するプレビューは `@LocalePreviews` / `@LocaleScreenPreviews` を使う | FIR による読み取り + IR の `@LocaleSensitive` メタデータ |
| 引数を転送するだけのラムダは callable reference を使う | FIR `LambdaCanBeCallableReference` |
| 素通しのラムダは関数値そのものを渡す | FIR `LambdaCanBePassedDirectly` |
| 最後のパラメータにある `@Composable` ラムダリテラルは trailing にする | FIR `ComposableLambdaMustBeTrailing` |
| mutation の effect ハンドラは `reset()` を呼ぶ | FIR `MutationEffectMustReset` |
| プラットフォームに限定された common の宣言はプラットフォームの接頭辞を持つ | FIR `PlatformOnlyNaming` |
| screen レベルの composable はそのファイル内で唯一のコンポーネントである | FIR `ScreenIsSoleComponentInFile` |
| content ラムダのネストは最大 4 レベルまで | FIR `ComposableNestingDepth` |
| レイアウトスコープを持たない composable はルートで最大 1 ノードしか emit しない | FIR `SingleRootEmission` |
| より広い可視性のプロパティから公開される private プロパティは明示的なバッキングフィールドを使う | FIR `ExplicitBackingFieldRequired` |
| 読み取り専用で公開される private な `var` は `private set` を使う | FIR `PrivateSetRequired` |
| feature の UI composable は同じファイル内にプレビューを持つ | FIR `UiComponentRequiresPreview` |
| feature の UI composable は受け取った state のすべてのプロパティを読む | FIR `UiComponentTakesWhatItReads` |
| コールバックは、その composable 自身が渡された値を呼び出し元に報告し返さない | FIR `NoCallerSuppliedCallbackArgument` |
| remember された値は使用前にローカル変数に束縛する | FIR `RememberResultMustBeBound` |
| `.value` 経由でのみ読まれる state は `by` で宣言する | FIR `StateMustBeDelegated` |

> 実装済みの FIR checker はすべて `:tools:compiler-plugin` にあります。`droidkaigi.primitive.enforcement` [convention プラグイン](./build-convention-plugins.ja.md)がそれらを各コンパイルの compiler-plugin クラスパスに載せ、`droidkaigi.primitive.kmp` / `kmp.compose` がそれを適用するため、アプリコードを持つすべてのモジュールがカバーされます。その対象外となるコンパイルが 2 つあります。どちらの primitive も適用しないビルド時の `:tools:*` モジュールと、ソースをこのプロジェクトではなくジェネレータが書く Swift Export のブリッジコンパイルです。**ロールはアノテーションではなく、context パラメータの型と `*Presenter`/`*ScreenRoot` という命名の組み合わせによって識別されます。**

以下の各 checker は診断テストでカバーされています。その実行方法と拡張方法については [Enforcement チェッカーのテスト](./testing-enforcement.ja.md) を参照してください。

## FIR checker（拒否される例と理由）

### `NoDirectMutate`

```kotlin
// in a presenter
LaunchedEffect(Unit) {
    bookmarkMutation.mutate(itemId)     // ERROR: NoDirectMutate
    val m = bookmarkMutation.mutate     // ERROR: aliasing is rejected too
}
```

理由: `mutate` は `MutationState` の遷移をバイパスするため、`MutatedEffect` / `MutationErrorEffect` が発火しません — `mutateAsync(...)` を使ってください。Soil の `mutate` は `val: suspend (S) -> T` のプロパティであり、checker はそのプロパティへの**あらゆるアクセス**を禁止するため、直接の呼び出しも脱糖された別名も、どちらもコンパイルエラーになります。

### `PresenterMustNotDeclareScreenContext`

```kotlin
context(_: SearchScreenContext, _: SearchPresenterContext) // ERROR on the ScreenContext param
@Composable
fun searchPresenter(): SearchUiState { … }
```

理由: presenter が取るのは `PresenterContext` だけです。`ScreenContext` 由来の context パラメータを宣言すると、Root ロールの依存を消費できてしまいます。

### `ScreenContextMustNotBePresenterContext`

```kotlin
// ERROR: implements BOTH
class SearchScreenContext : ScreenContext, PresenterContext
```

理由: is-a の関係は Root と presenter のロールを 1 つの型に漏らします。コンポジションを使い、`PresenterContext` をプロパティとして保持してください: `class SearchScreenContext(val presenterContext: SearchPresenterContext) : ScreenContext`。

### `NoPresenterEffectInScreenRoot`

```kotlin
context(screenContext: SearchScreenContext)
@Composable
fun SearchScreenRoot(...) {
    context(screenContext.presenterContext) {
        searchPresenter()           // OK: presenter launch is the sole exception
        ActionEffect(channel) { … } // ERROR: presenter-only effect in the Root
    }
}
```

理由: Root が `PresenterContext` のスコープを狭く開くのは `*Presenter` 関数を呼び出すためだけです。`PresenterContext` の context パラメータを要求する他のあらゆる呼び出し（`ActionEffect` / `ScreenChannel.emit` など）は presenter 専用であり、そのブロックに残された穴を塞ぎます。

### `NavigatorConfinedToNavEntry`

```kotlin
class SearchScreenContext(val navigator: SearchNavigator) : ScreenContext // ERROR
```

理由: ナビゲーションはラムダとして Root に届きます。`Navigator` 型が現れてよいのは NavEntryProvider の配線（および core のナビゲーション基盤）だけであり、`ScreenContext`/`PresenterContext`、presenter/`@Composable` のシグネチャ、`UiState`/`Action`/`ActionResult` には決して現れてはいけません。受け取れない `Navigator` は誤用できないため、シグネチャレベルのチェックで十分です。

### `MutationKeyMustCarryTag`

```kotlin
class BookmarkMutationKey(
    private val itemId: TimetableItemId, // ERROR: no MutationTag parameter
) : MutationKey<Unit, TimetableItemId> by buildMutationKey(
    id = MutationId("bookmark/$itemId"), // ERROR: tag not passed into the id
    …
)
```

理由: `MutationTag` を `MutationId` に畳み込まないと、画面ごとの mutation キャッシュが衝突します（Query/Subscription のキーは意図的に共有されますが、mutation のキーはそうではありません）。コンストラクタパラメータと、`build*MutationKey` への委譲の `id` 引数内でのその参照の両方が必要です。

### `SoilReadConfinement`

```kotlin
@Composable
fun SearchResultList(...) {              // no ScreenContext/PresenterContext param
    val items = rememberQuery(key)       // ERROR: Root-role read outside the root
}
```

理由: 読み取りはロールでゲートされます。`rememberQuery`/`rememberSubscription` は囲む `ScreenContext` の context パラメータ（Root ロール）を必要とし、`rememberMutation` は `PresenterContext` のものを必要とします。Soil は feature UI の深いところではなく、screen root で読んでください。（アプリシェルと `:core` の基盤はスコープ外です。）

### `PreviewRequiresWrapper`

```kotlin
@Preview
@Composable
private fun SearchScreenPreview() {
    SearchScreen(uiState = fakeState) // ERROR: not wrapped
}
```

理由: すべての `@Preview`（JetBrains でも AndroidX でも）は、ラッパーが提供するプレビュー画像リゾルバとともに `KaigiTheme` の中でレンダリングされなければなりません。関数に `@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)` を付けるか、プレビューが独自のカラースキームを選ぶ場合は `KaigiPreviewTheme(colorScheme) { … }` を本体のトップレベルの文にしてください。どちらの checker もメタアノテーション 1 段階を通してアノテーションを読むため、それらを持つマルチプレビューアノテーションも有効です。[プレビューとサンプルアセット](./preview.ja.md) を参照してください。

### `NoForwardOnlyAction`

```kotlin
ActionEffect(channel) { action ->
    when (action) {
        is Search.ItemClicked -> screenChannel.emit(NavigateToDetail(action.id)) // ERROR
    }
}
```

理由: 唯一の副作用を伴う文が `ScreenChannel.emit(...)` であるハンドラは、action を結果としてそのまま外へ転送しているだけであり、無意味な間接参照です。UI のコールバックは Screen から Root のナビゲーションラムダへ直接配線してください（[エラーハンドリング](./error-handling.ja.md) を参照）。その単一の `emit` に還元される分岐/本体のみが検出されます。

### `MustBeSerializable`

```kotlin
// declaration side: the requirement is declared on the type parameter
inline fun <T : Any, @MustBeSerializable reified RESPONSE : Any> buildPersistedQueryKey(…)

// call site
data class SearchResponse(…)                                   // no @Serializable
buildPersistedQueryKey(id, persistKey = "…", byteStore = …,
    fetchResponse = { searchResponse },                        // ERROR: RESPONSE not @Serializable
    transformToDomainModel = { … })
```

理由: `@Serializable` の欠落は、永続化がシリアライズする実行時にしか失敗しません。この checker は、reified なシリアライザ探索が取り除いたコンパイル時のゲートを復元します。このチェックは、ハードコードされた callable と引数のインデックスではなく、型パラメータに付いた `@MustBeSerializable` アノテーション（`:core:common`）によって駆動されるため、シグネチャの変更によって黙って外れることがなく、どんな関数でもオプトインできます。解決できない classifier（型パラメータ、ローカル/匿名の型）は、黙って許可されるのではなく拒否されます。

型引数がシリアライズ可能とみなされるのは、`@Serializable` を持つ場合、enum class である場合、または `kotlinx.serialization.builtins` がカバーする型のいずれかである場合です。すなわち、プリミティブとその符号なし版、`String`、`Unit`、`Nothing`、`List`/`Set`/`Map` とそれらの mutable 版、`Map.Entry`、`Pair`/`Triple`、`Array` とプリミティブ配列型、`kotlin.time.Duration`、`kotlin.uuid.Uuid` です。ジェネリック型はさらに、そのすべての型引数が条件を満たす必要があるため、`List<Session>` は `Session` が条件を満たす場合にのみ受け入れられます。そしてその場合、診断はコンテナではなく `Session` を名指しします。

### `RememberSerializable`

```kotlin
class SearchFilters(val query: String)   // no @Serializable

@Composable
fun SearchScreenRoot() {
    val filters = rememberSerializable { mutableStateOf(SearchFilters("")) } // ERROR
}
```

理由: `rememberSerializable`（`androidx.compose.runtime.saveable`）は、同じ reified な `serializer<T>()` 探索を通じて state のシリアライザを解決するため、シリアライザを持たない型もコンパイルは通り、プロセス death で state が保存されるときに例外を投げます。シリアライズ可能性の判定は [`MustBeSerializable`](#mustbeserializable) と同じです。`serializer = …` / `stateSerializer = …` を取るオーバーロードは探索を呼び出し元に委ねるため対象外です。シリアライザが `SavedStateConfiguration` の serializers module を通じて呼び出しに届く型には、`@Suppress("REMEMBER_SERIALIZABLE_TYPE_NOT_SERIALIZABLE")` を付けます。

### `MutationEffectMustReset`

```kotlin
MutationErrorEffect(favoriteMutation) { error ->   // ERROR: no reset() in the handler
    screenChannel.emit(ShowMessage(error.toUserMessage()))
}
```

理由: 消費された Success/Error は画面インスタンスを超えて Soil のキャッシュに残るため、`mutation.reset()` を呼ばないハンドラは、次の画面インスタンスで古い結果を再発火させます。チェックされるのはハンドラ内に `reset()` の呼び出しが存在することだけで、ハンドラのどこで実行するかはユースケース次第です。

### `PlatformOnlyNaming`

```kotlin
// commonMain
@PlatformOnly(TargetPlatform.Ios)
fun HapticsSyncEffect(...) { … }   // ERROR: name must start with "Ios"

fun IosHapticsSyncEffect(...) { … } // ERROR: "Ios" prefix without @PlatformOnly
```

理由: common のソースセットにある宣言が 1 つのプラットフォームでしか効果を持たないなら、その旨を名前で示さなければなりません。またプラットフォームの接頭辞を持つ名前は `@PlatformOnly`（`:core:common`）に裏付けられていなければならず、そうすることで接頭辞が嘘をついたり古くなったりしなくなります。逆向きのルールは `commonMain` 配下のトップレベル宣言にのみ適用されます。プラットフォームのソースセットではプラットフォーム接頭辞付きの名前を自由に使えます。

### `ScreenIsSoleComponentInFile`

```kotlin
// TimetableScreen.kt
@Composable
fun TimetableScreen(...) { … }

@Composable
private fun TimetableCard(...) { … }   // ERROR: move it to TimetableCard.kt
```

理由: ファイルパスがコンポーネントのアイデンティティであり、エージェントはそれがたまたま置かれている画面を読まずにコンポーネントを見つけて編集できます。トップレベルの `Unit` を返す `*Screen`/`*ScreenRoot` という名前の `@Composable` を宣言するファイルは、他の UI コンポーネントを宣言してはいけません。`@Preview` 関数と値を返す composable（presenter）は対象外です。抽出したコンポーネントは `internal` になります。ここではファイルプライベートな可視性は本質的ではありません。モジュール境界がすでにそれをその feature に閉じ込めているからです。

### `LambdaCanBeCallableReference`

```kotlin
TimetableScreenRoot(
    onNavigateToDetail = { id -> navigator.openSessionDetail(id) }, // ERROR
    // OK: onNavigateToDetail = navigator::openSessionDetail
)
```

理由: 本体全体が、ラムダのパラメータをそのまま転送する 1 つの呼び出しであるラムダはノイズです — callable reference を書いてください。checker は、reference で置き換えられない形をすべてスキップします。`suspend` またはレシーバ型の関数型、可変長引数、infix/operator 呼び出し、明示的な型引数、そして単純な `this`/オブジェクト/`val` の連鎖ではないレシーバです（reference はレシーバを一度だけキャプチャするため、可変なレシーバでは意味が変わってしまいます）。

`@Composable` ラムダは、必要性からではなく選択として除外されています。composable の reference はコンパイルできますが、content スロットでの `::Title` は、Compose のコードで composable であることを示す呼び出し構文を隠してしまいます。composable の値の転送は、代わりに `LambdaCanBePassedDirectly` がカバーします。

### `LambdaCanBePassedDirectly`

```kotlin
Wrapper(content = { content() })                     // ERROR: content = content
Modifier.clickable { onOpenSoilErrors() }            // ERROR: clickable(onClick = onOpenSoilErrors)
flow.collect { block(it) }                           // ERROR: collect(block)

RowScopeConsumer(content = { content() })            // OK: adapts () -> Unit to RowScope.() -> Unit
```

理由: すでにスコープにある関数値を呼び出すだけのラムダは、意味のない 1 段の間接参照です — その値を渡してください。これは callable reference ではないため、`LambdaCanBeCallableReference` が適用されない場所でも利用でき、`@Composable` や `suspend` の関数型も含まれます。

ラムダの型と値の型は等しくなければならず、これにより適応（adaptation）が除外されます。`@Composable () -> Unit` は、それ自体では `@Composable RowScope.() -> Unit` のパラメータに届きません。また値はパラメータか `val` でなければなりません。`var` は、ラムダが生成される時点と実行される時点の間で変わりうるからです。

### `ComposableLambdaMustBeTrailing`

```kotlin
CompositionLocalProvider(LocalContentColor provides contentColor, content = { Label() })  // ERROR

CompositionLocalProvider(LocalContentColor provides contentColor) {                       // OK
    Label()
}

Wrapper(label = "label", content = content)          // OK: a value, not a literal
LeadingSlot(icon = { Icon() }, label = "label")      // OK: `icon` is not the last parameter
PlainSlot(content = { })                             // OK: not a @Composable function type
```

理由: 名前付き引数として書かれた content スロットは、UI を生み出すブロックを引数リストの中に埋もれさせ、それが本来である入れ子の content ではなく設定のように読めてしまいます。trailing 構文は、ファイル内の他のすべての content ブロックと同じ場所にそれを置きます。

スコープに入るのは、呼び出し先の最後の値パラメータに束縛されたラムダリテラルだけです。名前付き引数は任意の順序で書けるため、最後のパラメータにあるリテラルは、ソース上でその後に別の名前付き引数が続いていても、常に括弧の外に出せます。このルールは trailing 構文で表現できないものには手を出しません。最後ではないパラメータ、`vararg` の最後のパラメータ、匿名 `fun` 式、そして infix または operator の呼び出しです。

### `ComposableNestingDepth`

```kotlin
@Composable
fun TimetableScreen(uiState: TimetableScreenUiState) {
    Scaffold { padding ->                 // 1
        Column(Modifier.padding(padding)) {   // 2
            LazyColumn {                  // 3
                items(uiState.sessions) { item ->  // 4
                    Card {                // ERROR: 5
                        Text(item.title)
                    }
                }
            }
        }
    }
}
```

理由: 深くネストしたツリーは画面の構造を隠します。`@Composable` 関数は content ラムダを最大**4**レベルまでネストできます。5 レベル目は独自の `@Composable` 関数（上の `TimetableCard`）に移さなければなりません。screen のファイルでは、続いて [`ScreenIsSoleComponentInFile`](#screenissolecomponentinfile) がそのコンポーネントに独自のファイルを与えます。

ラムダが深さにカウントされるのは、その本体が UI を emit するときだけです。したがって `onClick`、`remember`、コルーチンの本体は自由です。content をラップするビルダーラムダ — `forEach`、`LazyListScope` — はカウントされます。読み手が追わなければならない波括弧を 1 レベル増やすからです。エラーは、問題のラムダを保持する呼び出しに報告されます。

### `SingleRootEmission`

```kotlin
@Composable
fun ItemIcon(selected: Boolean, icon: @Composable () -> Unit) {  // ERROR
    if (selected) {
        Box(Modifier.background(indicatorColor))
    }
    icon()
}

@Composable
fun BoxScope.ItemIcon(selected: Boolean, icon: @Composable () -> Unit) {  // OK
    if (selected) {
        Box(Modifier.background(indicatorColor))
    }
    icon()
}
```

理由: ルートのコンテナを持たない composable は、配置を呼び出す側に委ねてしまいます。中央寄せの `Box` は 2 つのノードを emit 順に重ね、`Row` や `Column` は横または縦に並べるため、そのコンポーネントは書かれた対象の呼び出し箇所では意図どおりにレンダリングされ、次の呼び出し箇所では違って見えます。レイアウトスコープは配置の所有者が呼び出し側であることを名指しし、複数の emit を持つコンポーネントを書くための認められた方法です。レシーバとして宣言されていても context パラメータとして宣言されていてもカウントされ、そのサブタイプもカウントされます — `KaigiNavigationBarScope` は `RowScope` を継承します。対象のスコープは、子を配置するレイアウトのものです: `BoxScope`、`RowScope`、`ColumnScope`、`FlowRowScope`、`FlowColumnScope`、`LazyItemScope`、`LazyGridItemScope`、`LazyStaggeredGridItemScope`、`PagerScope`。

emit のカウントは単一の制御フローパスに従います。`if` と `when` の分岐は各パスのうち最大のものを寄与し、return する分岐はそれ以降の文を自身のパスから外し、`for` や `while` の本体の中の emitter は 2 としてカウントされます — このルールは、あるパスが 1 つより多くを運ぶかどうかだけを問うからです。`@Composable` の呼び出しは、その結果型が `Unit` のときに emit するとみなされ、これにより呼び出し箇所で `Unit` に置き換えられたジェネリックな composable（`key` など）も含まれます。結果が値に束縛される呼び出しは、`remember` を含めてノードを生成しません。また `run` のような inline かつ非 composable なラムダ内の emit は、それを保持する呼び出しの箇所でカウントされます。このルールが及ぶのは名前付き関数です。`@Composable` のラムダリテラルは、それが渡されるレイアウトに委ねられ、`forEach` や `repeat` によって繰り返される emitter は 1 回としてカウントされます。繰り返しは呼び出し先にあるからです。

effect は `…Effect` という名前であり、その名前を持つ composable は、emit するのではなく処理を実行するものとして読まれます。名前がすべての判定基準であるため、emit する composable はそのような名前を付けてはいけません。名前はそれを所有する宣言から取られるため、`fun interface` の effect はその `invoke` の呼び出し箇所でインターフェース名によって認識されます。

```kotlin
fun interface HistorySyncEffect {
    @Composable operator fun invoke(backStack: NavBackStack<NavKey>)
}

uiGraph.historySyncEffect(backStack)  // reads as HistorySyncEffect, so it emits nothing
```

### `ExplicitBackingFieldRequired`

```kotlin
class ServerEnvironmentStore {
    private val mutableEnvironment = MutableStateFlow(ServerEnvironment.Staging)
    val environment: StateFlow<ServerEnvironment> = mutableEnvironment.asStateFlow() // ERROR

    // OK:
    val environment: StateFlow<ServerEnvironment>
        field = MutableStateFlow(ServerEnvironment.Staging)
}
```

理由: private なプロパティと、それを再公開するだけのより広いプロパティの組は、1 つの状態を 2 つの名前で重複させ、クラスが育つにつれてその組の同期を保つものが何もありません。Kotlin の明示的なバッキングフィールドは同じ意図を 1 つの宣言で表します — クラスの内側ではフィールドの型、外側ではプロパティの型です。この checker が発火するのは書き換えが機械的な場合だけです。すなわち、両方のプロパティが読み取り専用であり、private なプロパティの型がすでに公開される型のサブタイプであり、その値がコンストラクタパラメータやゲッターではなく初期化子から来ている場合です。両者の間では、単純な読み取りか、同一インスタンスの読み取り専用ビュー（`asStateFlow` / `asSharedFlow`）が条件を満たします。サブタイプを超えて広げる変換（`Channel.receiveAsFlow()`）や、派生した値（`Flow.map`）は対象外です。

### `PrivateSetRequired`

```kotlin
class Counter {
    private var mutableCount = 0
    val count: Int get() = mutableCount // ERROR

    // OK:
    var count: Int = 0
        private set
}
```

理由: `ExplicitBackingFieldRequired` と同じ状態の重複という問題を、フィールドの型では表現できないケース — クラス内で再代入され外部から読まれる private な `var` — について扱います。`private set` はセッターだけを狭めるため、その組は 1 つの宣言に畳まれます。この 2 つのルールは型によって分担されます。型が同一なら `private set` の仕事、private 側の型が厳密に狭いなら明示的なバッキングフィールドの仕事です。

### `UiComponentRequiresPreview`

```kotlin
// TimetableCard.kt
@Composable
internal fun TimetableCard(title: String, onClick: () -> Unit) { … } // ERROR: no preview here

// OK: a preview for it in the same file
@PreviewWrapper(KaigiPreviewWrapper::class)
@Preview
@Composable
fun TimetableCardPreview() { TimetableCard(title = /* sample */, onClick = {}) }
```

理由: プレビューを持たないコンポーネントは、アプリを実行しなければ確認できず、読み手はそれがどう見えるかを知る手段がありません。feature パッケージ配下のトップレベルの `Unit` を返す `@Composable` はすべて、同じファイル内に**それをレンダリングする** `@Preview` を必要とします。ファイル内の別のところにあるプレビューは数に入らないため、複数のコンポーネントを持つファイルにはそれぞれに届くプレビューが必要です。このチェックはプレビューの本体を読み、`KaigiPreviewTheme(colorScheme) { … }` のようなラッパーのラムダの中に降り、同じファイル内で宣言されたヘルパーを辿るため、プレビューは間接的にコンポーネントへ届いても構いません。[`ScreenIsSoleComponentInFile`](#screenissolecomponentinfile) はプレビューを対象外とするため、プレビューはそれがレンダリングするコンポーネントの隣に置かれ、続いて [`PreviewRequiresWrapper`](#previewrequireswrapper) がそれを認められたラッパー経由に強制します。

このルールは `:feature:debug` を含むすべての feature モジュールに適用されます。モジュール単位の例外は編集している場所からは見えないため、「このコンポーネントにはプレビューは不要」と読まれ、そこに次に書かれるコンポーネントがその欠落を引き継いでしまいます。残された例外は、すべて宣言そのものから見て取れます。

| 対象外 | 理由 |
| --- | --- |
| context パラメータを宣言する composable | すべての `*ScreenRoot`。その `ScreenContext` は画面の Metro グラフから来るもので、プレビューでは構築できません |
| `expect` / `actual` の宣言 | `expect` には本体がなく、ツールは common と Android のビューのみをレンダリングします |
| `*Effect` という名前の composable | 副作用を実行し何も emit しないため、そのプレビューは空になります |
| メンバの composable | このルールはトップレベルの宣言を読みます。クラス上の composable はその所有者を通じて到達されます |
| `Unit` 以外を返す composable | すべての `*Presenter` は UI を emit せず UiState を返します |

単独では本当にレンダリングできないコンポーネントには、理由を添えて `@Suppress("UI_COMPONENT_WITHOUT_PREVIEW")` を付けます。コードベースで該当するのは `SoilErrorBottomSheet` の 1 件です。`ModalBottomSheet` はポップアップウィンドウにレンダリングし、プレビューはそれを空のツリーとして取り込むため、その content は `SoilErrorSheetContent` に分割され、そちらでプレビューされています。

### `UiComponentTakesWhatItReads`

```kotlin
@Composable
internal fun SessionHeaderView(item: TimetableItem) { // ERROR: day, startsAt, endsAt, asset unread
    Text(item.room.name)
    Text(item.title.current())
    Text(item.speakers.joinToString { it.name })
}

// OK: the properties it reads
@Composable
internal fun SessionHeaderView(room: SessionRoom, title: String, speakers: List<TimetableSpeaker>) { … }
```

理由: いくつかのプロパティのために集約型を受け取るコンポーネントは、その型を自身の読み取りが正当化する範囲を超えて広げます。すべての呼び出し元がコンポーネントをレンダリングするために state 全体を保持しなければならず、プレビューはそれを構築しなければならず、Compose は読んでいないプロパティが変わったときにもそのコンポーネントを再コンポーズします。feature の UI `@Composable` は、選択の対象としたパラメータの**すべての**プロパティを読まなければなりません。そうでなければ、読むものだけを保持するコンポーネント用の UiState 型を宣言するか（`FavoritesListSectionUiState` が真似るべき形です）、それらのプロパティを個別のパラメータとして受け取ってください。

対象となるパラメータの型は、プライマリコンストラクタを持つプロジェクト所有のクラスであり、カウントされるプロパティはそのコンストラクタが宣言するものです。選択の対象ではなく値として使われるパラメータ — 別の composable に渡される、比較される、メンバ呼び出しのレシーバになる — は対象外です。その場合はそれ自体の形が本質的だからです。

リストのアイテムがこのルールの下で軽量なままでいられるのは、それがレンダリングしない状態は別の場所に属するからです。選択状態（`isFavorite`）はモデルのフィールドではなく独自のパラメータとして届き、親が適用するレイアウト状態（`SponsorPlan`）はそもそもアイテムに入りません。アイテムがコールバックに渡すだけの識別子も同様に入りません — [`NoCallerSuppliedCallbackArgument`](#nocallersuppliedcallbackargument) を参照してください。

### `NoCallerSuppliedCallbackArgument`

```kotlin
@Composable
internal fun TimetableCard(
    id: TimetableItemId,
    title: String,
    onClick: (TimetableItemId) -> Unit,   // ERROR: reports back `id`
) {
    Card(modifier = Modifier.clickable { onClick(id) }) { Text(title) }
}

// OK: the caller holds the identifier, so the callback carries nothing
@Composable
internal fun TimetableCard(title: String, onClick: () -> Unit) {
    Card(modifier = Modifier.clickable(onClick = onClick)) { Text(title) }
}

// at the call site
items(slot.items) { item ->
    TimetableCard(title = item.title, onClick = { onItemClick(item.id) })
}
```

理由: コールバックのパラメータは、呼び出し元が持っていない情報を運ぶために存在します。呼び出し元が少し前に渡した値はそのような情報ではありません — 呼び出し元は呼び出し箇所でそれをクロージャに取り込めますし、往復はコンポーネントのシグネチャを広げ、識別子をレンダリングのために存在する state 型に押し込むだけです。したがって `@Composable` は、`Unit` を返す関数型のパラメータを、自身の値パラメータの 1 つ、またはそこから選択されたプロパティで呼び出してはいけません。

拒否されるのは、引数が単純な読み取りである場合だけです。`forEach`/`items` のラムダで束縛された要素、`remember` された値、そして計算された式（`count + 1`）は、いずれもコンポーネント自身が所有する値です。`@Composable` と `suspend` の関数型は独自の関数型の種別を持つため、content スロットと suspend するハンドラは対象外です。

### `RememberResultMustBeBound`

```kotlin
remember { SnackbarHostState() }.showSnackbar(message)   // ERROR
retain { mutableStateOf(DroidKaigi2026Day.Day1) }.value  // ERROR

val snackbarHostState = remember { SnackbarHostState() } // OK
snackbarHostState.showSnackbar(message)

var selectedDay by retain { mutableStateOf(DroidKaigi2026Day.Day1) } // OK: a delegate
SnackbarHost(retain { SnackbarHostState() })                         // OK: argument position
rememberCoroutineScope().launch { … }                                // OK: a handle, not a value
```

理由: レシーバとして直接書かれた呼び出しは、その地点で行われる計算のように読めますが、それが生み出すのは再コンポジションをまたいで保持される値です。ローカル変数が与える名前は、それが生成される地点でそのことを示し、それ以降のすべての使用は保持された値の使用として読めるようになります。連鎖した形はまた、その呼び出しに与えられたキー — 値がいつ再計算されるかを決めるもの — を隠します。

対象は**値**を remember するファミリーです: `remember`、`rememberSaveable`、`rememberSerializable`、`rememberUpdatedState`、`retain`。解決された callable id でマッチするため、それぞれの名前のすべてのオーバーロードが該当し、別パッケージの同名関数は該当しません。即座に使うのが慣用的な形である**ハンドル**を返す呼び出しはこのファミリーに含まれず、それが `rememberCoroutineScope().launch { … }` がコンパイルできる理由です。Soil の読み取り（`rememberQuery`、`rememberMutation`、`rememberSubscription`）と、ナビゲーションやスクロールの基盤を生み出す `remember*`/`retain*` のファクトリも、同じ理由で対象外です。

対象になるのはレシーバ位置だけです。引数位置は慣用的であり対象外で、これにより `with(remember { … }) { … }` や、値を引数として渡す他のあらゆる言い換えも対象外になります。それらを塞ぐにはスコープ関数の一覧が必要になり、[上記の原則](#原則（強制メカニズムの優先順位）) はそれをレビューに委ねています。レシーバ位置そのものは解決された呼び出しから読まれるため、`?.` を通じたレシーバ、インデックスアクセスを通じたレシーバ、`for` ループの対象としてのレシーバもカウントされ、レシーバとして書かれたスコープ関数の呼び出し（`remember { … }.let { … }`）もカウントされます。

### `StateMustBeDelegated`

```kotlin
val selectedDay = remember { mutableStateOf(DroidKaigi2026Day.Day1) } // ERROR
selectedDay.value = day
Text(selectedDay.value.name)

// OK: the delegate reads and writes the same state
var selectedDay by remember { mutableStateOf(DroidKaigi2026Day.Day1) }
selectedDay = day
Text(selectedDay.name)

// OK: the object leaves the scope, so `by` is unavailable
val selectedDay = remember { mutableStateOf(DroidKaigi2026Day.Day1) }
return selectedDay
```

理由: 使用のたびに書く `.value` は、言語がすでに取り除いてくれるノイズです。`androidx.compose.runtime.getValue` と `setValue` により `by` は同じ state を読み書きするため、委譲した形は宣言とそれ以外の場所での素の名前だけになり、書き換えが増やすのは import のペアだけです。

このルールは使用箇所ではなく**宣言**について述べられています。delegate は値を手渡し、オブジェクトを取り上げるからです。宣言が拒否されるのは、それが delegate を持たない `val` であり、型が `androidx.compose.runtime.State` かそのサブタイプであり、それへの**すべての**参照がその `value` の読み取りまたは書き込みである場合です。オブジェクトそのものの使用が 1 つでもあれば — 引数として渡される、返される、分解される、あるいは他の何かのレシーバになる — 対象外になります。そこで `by` を要求することは、存在しない書き換えを要求することになるからです。参照がまったくない宣言も同じ理由で対象外です。短くするものが何もありません。

この除外は `by` が表現できないことを記録したものであり、オブジェクトに手を伸ばす理由にはなりません。screen はイミュータブルな `UiState` をレンダリングし、操作はコールバックで報告するため、state 型は composable のパラメータリストに属しません — [画面の実装](./building-a-screen.ja.md#action-actionresult-uistate) を参照してください。

可視性はこのルールをコンパイラが見える範囲に限定します。対象となるのはローカル変数か `private` なプロパティだけです。それらは、そのすべての参照がコンパイル対象のファイル内にある宣言だからです。より広い可視性のプロパティは別のモジュールからオブジェクトとして読まれる可能性があり、checker は部分的な証拠で判断することになってしまいます。

さらに 3 つの除外は、可視性からではなく書き換えから導かれます。

- `var` は決して報告されません。再代入可能であり、`by` の下では代入は変数の再束縛ではなく state の値の書き込みになります。
- カスタムゲッターを持つプロパティと、宣言で初期化されないプロパティは決して報告されません。`by` は宣言の時点で delegate 式を必要とし、読み取りごとに再計算はしません。
- `kotlinx.coroutines.flow.StateFlow` と `MutableStateFlow` は決して報告されません。それらの `value` は無関係な型の別のメンバであり、`getValue` operator は適用されません。checker はメンバ名ではなく Compose の `State` のクラス id でマッチするため、たまたまプロパティ名が `value` である value class も同じ理由で対象外です。

## レビュー + テスト（曖昧なもの）

データ量や意味に依存するルールは静的な強制の外に置かれます。データ層に属する重い整形、emit される結果の妥当性、mutation 結果の扱いの正しさ — これらは AI のレビュールール + Presenter/Screen のテストによって担保されます。命名のルールもそこに加わります。名前は型に対してではなくドメインの語彙に対して判断されるからです — [命名レビュー](./naming-review.ja.md) を参照してください。ある値が `CompositionLocal` に属するかどうかも同様で、これは型ではなく意図によって決まります — [CompositionLocal レビュー](./compositionlocal-review.ja.md) を参照してください。

**型で排除できるものは型で排除し**（必須のシリアライザ、context パラメータ、`suspend`、`internal`）、**型では表現できない二値のものには FIR checker を使い**、**曖昧なケースはレビューに委ねます**。AI がコードを書く場合でも、型と FIR のレイヤーは有効です。**コンパイルが失敗する**からです。

関連: [アーキテクチャ概要](./architecture-overview.ja.md) · [画面の実装](./building-a-screen.ja.md) · [ScreenContext の設計](./screen-context.ja.md) · [エラーハンドリング](./error-handling.ja.md) · [命名レビュー](./naming-review.ja.md)
