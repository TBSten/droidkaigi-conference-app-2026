# Presenter のユニットテスト（Molecule）

Composable な Presenter は、**画面の状態を返す関数**です。action とデータを受け取り、画面が描画する `UiState` を返します。UI は付いていません。ただし、通常の関数のようには呼び出せません — `remember` と effect の API は、動作中の Compose runtime の内部でしか機能しないからです。幸い、いくつかのツールを組み合わせればその障害は取り除けます。Molecule が Compose runtime を駆動し（UI は不要）、返された `UiState` を `Flow` として公開し、Turbine がその emission をアサートします — action を送り、次の状態を待ち、アサートします。

## ツール

- **Molecule**（`app.cash.molecule:molecule-runtime`）… `@Composable` を駆動し、その戻り値（UiState）を `Flow` として観測します。
- **[テストグラフ（TestingScope）](./testing-graph.ja.md)** … `<Feature>PresenterContext` は手で構築するのではなく DI から解決されるため、そこに新しい依存関係が加わっても、1 つの fake を通じてすべてのテストに行き渡ります。
- **Turbine** … `Flow` の emission をアサートします。
- **kotlinx-coroutines-test**（`runTest` / `TestDispatcher`）… 仮想時間を駆動します。

## 基本のレシピ

Molecule の足場（runtime を駆動する、`SwrClient` を提供する、`PresenterContext` を供給する、Turbine でアサートする）はどの Presenter テストでも同一なので、**一度だけ**書かれています。`:core:testing` の `runPresenterTest`（`core/testing/src/commonMain/…/PresenterTest.kt`）です。

```kotlin
// core:testing — shared scaffolding, written once. The test drives the screen from both ends:
// `send` plays the Root's role (gated by ScreenContext), `uiStates` observes the presenter's
// return value, and `results` observes the ActionResults the presenter emits (captured by an
// ActionResultEffect composed with a test ScreenContext — the Root's role again).
class PresenterTestScope<A, R, S>(
    private val screenContext: ScreenContext,
    private val screenChannel: ScreenChannel<A, R>,
    val uiStates: ReceiveTurbine<S>,
    val results: ReceiveTurbine<R>,
) {
    fun send(action: A) = context(screenContext) { screenChannel.send(action) }
}

fun <C : PresenterContext, A, R, S> runPresenterTest(
    presenterContext: C,
    presenter: @Composable context(C) (ScreenChannel<A, R>) -> S,
    validate: suspend PresenterTestScope<A, R, S>.() -> Unit,
) = runTest {
    val screenContext = object : ScreenContext {
        override val logger: KaigiLogger = presenterContext.logger
    }
    val screenChannel = ScreenChannel<A, R>()
    val results = Channel<R>(Channel.BUFFERED)
    val uiStateFlow = moleculeFlow(RecompositionMode.Immediate) { // drive the Compose runtime
        val client = SwrCachePlus(backgroundScope)          // a real SwrCachePlus works under Molecule (no TestSwrClientPlus needed)
        compositionLocalProviderWithReturnValue(
            LocalSwrClient provides client,
            LocalQueryClient provides client,
            LocalMutationClient provides client,
            LocalSubscriptionClient provides client,
        ) {
            context(screenContext) {                        // play the Root: capture the result side
                ActionResultEffect(screenChannel) { results.send(it) }
            }
            context(presenterContext) {                     // supply the PresenterContext (do not use with=receiver)
                presenter(screenChannel)
            }
        }
    }
    turbineScope {                                          // one flat scope for both turbines
        // Molecule re-emits on every recomposition; equal consecutive states are noise to a test.
        val uiStates = uiStateFlow.distinctUntilChanged().testIn(backgroundScope)
        val resultsTurbine = results.receiveAsFlow().testIn(backgroundScope)
        PresenterTestScope(screenContext, screenChannel, uiStates, resultsTurbine).validate()
        uiStates.cancelAndIgnoreRemainingEvents()
        resultsTurbine.cancelAndIgnoreRemainingEvents()
    }
}

// CompositionLocalProvider's content is @Composable () -> Unit, so the UiState cannot be
// returned through it directly — provide the locals via Composer.startProviders/endProviders
// instead, which lets content return a value.
@OptIn(InternalComposeApi::class)
@Composable
fun <T> compositionLocalProviderWithReturnValue(
    vararg values: ProvidedValue<*>,
    content: @Composable () -> T,
): T {
    currentComposer.startProviders(values)
    val result = content()
    currentComposer.endProviders()
    return result
}
```

各 feature のテストは、解決された context、Presenter の呼び出し、そして action とアサーションだけになります。

```kotlin
private val graph = createGraph<TimetableScreenTestGraph>()

@Test
fun bookmark_event_marks_session() = runPresenterTest(
    presenterContext = graph.presenterContext,
    presenter = { channel -> timetableScreenPresenter(channel, fakeTimetable) },
) {
    assertEquals(expectedInitial, uiStates.awaitItem())
    send(TimetableScreenAction.Bookmark(id))
    assertEquals(expectedBookmarked, uiStates.awaitItem())
    assertEquals(TimetableItemId("d1a"), graph.favoriteMutationKey.invocations.receive())
}
```

`ScreenChannel` の両端はゲートされています。`send` は [`ScreenContext`](./screen-context.ja.md) を必要とし、result は `ActionResultEffect` を通してしか読めません（これも `ScreenContext` でゲートされます。channel 自体は `:core:common` に対して `internal` です）。そのため足場はテスト用の `ScreenContext` で Root の役割を演じます — `send` は `screenChannel.send` の周りでそれを開き、Presenter の隣に composed された `ActionResultEffect` が、emit された result を `results` turbine に捕捉します。

Presenter には loading からコンテンツへの遷移はありません。Root の [`SoilDataBoundary`](./soil-data-boundary.ja.md) と `rememberQuery` が loading を扱い、Presenter は既にロード済みの `Timetable` を受け取ります。Presenter のテストは状態遷移だけをアサートします。

## 作り込みのポイント

1. **`RecompositionMode.Immediate`**: frame clock を待たずに recomposition を即座に実行します（テストの要です）。
2. **依存関係の供給**: Presenter は `rememberMutation` / `SwrClientProvider` と `PresenterContext`（`context(presenterContext){}` で供給されます）を必要とします。テストの composition の内部では、**本物の `SwrCachePlus(backgroundScope)`（`TestSwrClientPlus` は不要）と `runTest` の仮想時間、そしてテストグラフが解決する `PresenterContext`** を供給します。
3. **成功/失敗の検証**: `MutationSuccessEffect` / `MutationErrorEffect` を発火させるには、**fake のキーを `failWith(…)` で仕込むか、成功するままにしておき**、**`results` turbine 上の emission をアサートします** → 一度きりの配線でさえ検証できます。
4. **入出力**: スコープの `send(action)` で action を流し込み、UiState の遷移を駆動します。`uiStates` 以外に 2 つのアサーション面があります — action がデータ層で到達した先は fake の `invocations` に、Presenter が返した emit は `results` turbine に現れます。状態を変えるだけの action は、そのどちらにも到達しません。
5. **Multiplatform**: Presenter は commonMain にある純粋なロジックなので、**JVM で実行すれば十分です**（描画を伴わないロジックの検証に UI ターゲットは必要ありません）。

## テストピラミッドにおける位置

- **Presenter テスト（Molecule）**: UI を描画せずに状態とロジックを高速に検証します。
- **Screen テスト（[Robot](./testing-robot.ja.md) + Roborazzi）**: 描画 + スクリーンショット + 振る舞い。

Presenter の層は Robot/Roborazzi の下にあります。実行コストが安いため、状態遷移はここでカバーし、画面の層には描画の関心事を残します。

関連: [テスト概要](./testing.ja.md) · [テストグラフ（TestingScope）](./testing-graph.ja.md) · [Robot パターンテスト](./testing-robot.ja.md)
