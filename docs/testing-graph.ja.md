# テストグラフ（TestingScope）

`TestingScope` はテストの DI スコープです。fake は `:core:testing` からそこへ contribute され、各画面は `commonTest` ソースセットに `<Screen>ScreenTestGraph` を宣言して、テストが必要とするコンテキストを返します。[Presenter のユニットテスト（Molecule）](./testing-presenter.ja.md) はそこから `PresenterContext` を取得し、[Robot パターンテスト](./testing-robot.ja.md) は `ScreenContext` を取得します。

Metro がそれらのコンテキストを構築するため、`PresenterContext` や `ScreenContext` に新しい依存が加わっても、fake を 1 つ追加すれば満たされます — テストを編集する必要はありません。

## Fake

fake は本番グラフが bind するのと同じインタフェースを bind するため、`TestingScope` に contribute されなければなりません。

```kotlin
// :core:testing — one fake per role, shared by every feature
@Inject @SingleIn(TestingScope::class)
@ContributesBinding(TestingScope::class)
class FakeKaigiLogger : KaigiLogger { … }
```

Soil のキーの fake は `buildQueryKey` / `buildSubscriptionKey` / `buildMutationKey` に委譲します。それらの式は構築中のクラスを参照できないため、可変な部分はコンストラクタパラメータとして渡されます — クエリやサブスクリプションには `FakeFixture`、ミューテーションの記録された呼び出しと仕込まれた失敗には `FakeMutationState` です。クエリの fake は `FakeKeyControl` を継承し、これがそのフィクスチャをテストが操作する面として公開します。

```kotlin
@SingleIn(TestingScope::class)
@ContributesBinding(TestingScope::class, binding = binding<SponsorsQueryKey>())
class FakeSponsorsQueryKey private constructor(
    fixture: FakeFixture<Sponsors>,
) : FakeKeyControl<Sponsors>(fixture),
    SponsorsQueryKey by buildQueryKey(
        id = QueryId("fake-sponsors"),
        fetch = { fixture.await() },
    ) {
    @Inject constructor() : this(FakeFixture(Sponsors(groups = persistentListOf())))
}
```

`FakeKeyControl` は 2 つ目のスーパータイプを加えるため、`@ContributesBinding` は bind される型を明示的に指定しなければなりません。

## ローディング状態とエラー状態

fetch は `FakeFixture.await()` を通じて実行されます。ここが、[`SoilDataBoundary`](./soil-data-boundary.ja.md) がコンテンツの周囲に描画する 2 つの状態にテストが到達する地点です。

| 呼び出し | boundary への影響 |
| --- | --- |
| `hold()` | fetch が中断するため、`Suspense` のローディングフォールバックが画面に残ります |
| `release()` | fetch が現在の値で完了し、コンテンツがフォールバックを置き換えます |
| `failWith(…)` | fetch が例外を投げるため、`ErrorBoundary` のエラーフォールバックが描画されます |

Robot はそれらをシナリオのステップとして公開します。

```kotlin
fun setupPendingSponsors() {
    graph.sponsorsQueryKey.hold()
}

fun releaseSponsors(sponsors: Sponsors) {
    graph.sponsorsQueryKey.set(sponsors)
    graph.sponsorsQueryKey.release()
    composeUiTest.waitForIdle()
}
```

画面の表示はそれ自体で 1 つのステップのまま保たれるため、シナリオは `setupPendingSponsors()` のあとに `setupContent()` と読めます。[Robot パターンテスト](./testing-robot.ja.md) を参照してください。

すべての `MutationKey` は `MutationTag` を持たなければなりません（[Soil のミューテーション](./soil-mutation.ja.md)） — チェッカーはテストソースに対しても実行されるため、fake も含みます。`:core:testing` はすべてのテストグラフに 1 つのタグを提供します。各テストは自身の `SwrClient` を所有するため、このタグが存在する目的である画面ごとのキャッシュ分離はすでに満たされています。

## グラフ

グラフはアクセサだけです。コンテキストに加えて、テストが設定または検査する各 fake が並びます。`ScreenContext` は `@SingleIn(<Screen>ScreenScope::class)` であるため、`additionalScopes` が画面スコープを受け入れます。

```kotlin
@DependencyGraph(scope = TestingScope::class, additionalScopes = [SponsorsScreenScope::class])
interface SponsorsScreenTestGraph {
    val screenContext: SponsorsScreenContext
    val presenterContext: SponsorsPresenterContext
    val sponsorsQueryKey: FakeSponsorsQueryKey
}
```

本番の `Default<…>Key` の binding は `:core:data` にあり、これは feature のテストクラスパスには載らないため、画面スコープを受け入れても競合する binding は引き込まれません。

## ライフタイム

グラフは 1 つの結線を固定するため、変化するものはすべて fake 内部の可変状態になります — クエリの `set(…)` / `hold()` / `release()`、ミューテーションに例外を投げさせる `failWith(…)`、何が到達したかを読む `invocations` です。

グラフの保持者はそれぞれ自分のものを生成し、グラフが共有されることはありません。

- Presenter のテストクラスは 1 つを保持します。各テストメソッドは新しいインスタンス上で実行され、したがって新しいグラフになります。
- Robot は 1 つを保持します。`runRobotTest` は `itShould` ごとに新しい Robot を構築し、したがって新しいグラフになります。また `setupContent` は新しい `SwrClient` を通じてフィクスチャを読み直すため、シナリオはそれを複数回呼び出すことができます。

関連: [テスト概要](./testing.ja.md) · [Presenter のユニットテスト（Molecule）](./testing-presenter.ja.md) · [Robot パターンテスト](./testing-robot.ja.md)
