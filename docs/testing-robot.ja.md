# Robot パターンテスト

画面のエンドツーエンドの振る舞いは、BDD（behavior-driven development、振る舞い駆動開発）スタイルの **Robot パターン** でテストし、シナリオが振る舞いとして読めるようにします。足場は `:core:testing` にあり、最初の実際の画面テストは `:feature:sessions` の `TimetableScreenRobotTest` です。

## 何を

- 画面ごとの **`Robot`** が、その画面の操作（`setupContent`、タップ）とアサーション（`check…`）を `ComposeUiTest` 上でカプセル化します。シナリオはそれらを組み合わせます。
- 小さな **BDD DSL** — `describe` / `doIt` / `itShould` — がシナリオツリーを構築し、`itShould` ごとに 1 つの実行可能なブロックへ平坦化します。各ブロックはスコープ内の `doIt` ステップを再生し、その後に新しいコンポジションに対してアサーションを実行するため、アサーションは互いに独立を保ちます。

## どうやって

画面レベルの Compose テストは Compose Multiplatform の `runComposeUiTest`（`org.jetbrains.compose.ui:ui-test` 由来。desktop の actual と、`compose.desktop.currentOs` 経由で Skiko のネイティブランタイムが併せて提供されます）を通じて実行されます。BDD DSL 自体は `commonMain` にある純粋な Kotlin なので、シナリオはすべてのターゲットで実行されます。`iosSimulatorArm64Test` ではアサーションが、`jvmTest` ではそれに加えて各 `itShould` のスクリーンショットが実行されます。

テストクラスは `RobotTest` を継承します。これは、actual が必要とするターゲットごとのテストの足場を持つ共通の基底クラスで、JVM では素の状態です。

```kotlin
class TimetableScreenRobotTest : RobotTest() {

    @Test
    fun timetable_screen_behaviour() = runRobotTest(…)
}
```

各 `itShould` はそのアサーションの後にキャプチャされるため、画像はその文が説明する状態を示します。ファイルは Robot の画面名とシナリオにちなんで命名され — `LicensesScreen.when_the_libraries_fail_to_load___show_the_error_fallback.png` — プレビューのゴールデンの隣に配置されます。ゴールデンがどのように記録・比較されるかは [プレビュースクリーンショットテスト](./testing-preview-screenshot.ja.md) を参照してください。

Robot は画面の [`ScreenContext`](./screen-context.ja.md) を [テストグラフ](./testing-graph.ja.md) から取得します。データを用意することと画面を表示することは別のステップなので、シナリオは「データ層がある状態にあり、その上で画面が開く」という形で読めます。

```kotlin
private val graph = createGraph<SponsorsScreenTestGraph>()

fun setupSponsors(sponsors: Sponsors) {
    graph.sponsorsQueryKey.set(sponsors)
}

fun setupContent() {
    setScreenContent {
        context(graph.screenContext) { SponsorsScreenRoot(…) }
    }
}
```

`setScreenContent` は `Robot` 上のハーネスです。プロダクションで nav entry が供給するもの — 新しい `SwrClient` と `snackbarNavEntryDecorator` が提供する `LocalSnackbarHostState` — の代わりを務め、その後 idle になるまで待機します。

Root が [`SoilDataBoundary`](./soil-data-boundary.ja.md) を所有しているため、ローディングとエラーのフォールバックは presenter ではなく Robot の関心事です。`hold()` はクエリを suspend させたままにしてローディングのフォールバックをアサートできるようにし、`failWith(…)` は boundary をエラーのフォールバックへ送ります。[テストグラフ](./testing-graph.ja.md) を参照してください。

## 実際のシナリオ

```kotlin
runRobotTest(robotFactory = { TimetableScreenRobot(this) }) {
    describe("when the timetable has loaded") {
        doIt {
            setupTimetable(sampleTimetable)
            setupContent()
        }
        itShould("show Day1 sessions") {
            checkSessionDisplayed("Day1 A")
            checkSessionDoesNotExist("Day2 A")
        }
        describe("and the Day2 tab is tapped") {
            doIt { clickDayTab(DroidKaigi2026Day.Day2) }
            itShould("swap the list to Day2 sessions") {
                checkSessionDisplayed("Day2 A")
                checkSessionDoesNotExist("Day1 A")
            }
        }
    }
}
```

画面ごとの [プレビュースクリーンショットテスト](./testing-preview-screenshot.ja.md)（静的なレンダリングをカバーする）を、操作と状態を実行することで補完します。

関連: [テスト概要](./testing.ja.md) · [テストグラフ（TestingScope）](./testing-graph.ja.md) · [Presenter のユニットテスト（Molecule）](./testing-presenter.ja.md)
