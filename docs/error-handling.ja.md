# エラーハンドリング

エラーと 1 回限りのイベント（完了したナビゲーション、一時的なメッセージ、成功通知）は、`UiState` 上のフラグではなく、Soil の状態から導出された Compose の effect を通じて配信されます。このページはそのルールを定めます。これらの要素がリクエストフローのどこに位置するかは [アーキテクチャ概要](./architecture-overview.ja.md) を参照してください。

## 2 層のエラーモデル

エラーは深刻度で分かれ、各層は 1 つの sink を持ちます。

- **致命的 / 読み込み失敗 — 全画面。** コンテンツを生成できないクエリやサブスクリプションは、[`SoilDataBoundary`](./soil-data-boundary.ja.md) の `ErrorBoundary` を通じて表面化し、画面全体をフォールバックに置き換えます。フォールバックは `SoilFallback`（`SoilFallbackDefaults.default()` または `custom()`）を通じて差し替え可能です。
- **一時的 / 部分的、および成功通知 — snackbar。** 失敗した書き込み、あるいはコンテンツを画面に残したままの部分的な失敗、そして成功通知は `SnackbarHostState` へ送られます。ホストは画面ごとです。`rememberSnackbarNavEntryDecorator`（`:core:common`）はナビゲーションエントリごとに 1 つの `SnackbarHostState` を作成し、エントリを `Scaffold` で包み、`LocalSnackbarHostState` を通じて公開します。Root は自身の `Scaffold` を入れ子にするのではなく、その local を読み取ります。アプリ全体のホストは存在しません。

この両層とは別に、`SoilErrorMonitor`（`:core:common`）が Soil の報告するエラーを一覧するアプリ全体のオーバーレイをレンダリングします。プロダクションのバインディングは no-op で、`:feature:debug` がそれを置き換えるため、このオーバーレイはユーザ向けの sink ではなく開発時の補助です。[デバッグ](./debugging.ja.md) を参照してください。

`Throwable.toUserMessage()`（`:core:common`）は例外を `AppError` に分類し、`UserMessage` でラップします。`SnackbarHostState.showSnackbar(UserMessage)`（`:core:ui`）はそのカテゴリに対応するローカライズされたテキストを解決します。例外メッセージがユーザに届くことはありません。

## 1 回限りのイベント

1 回限りのイベントは、Soil のミューテーション状態を観測し、遷移ごとにちょうど 1 回だけ発火する Compose の effect です。`UiState` 上の boolean から 1 回限りのイベントを導出することは禁止されています。

- `MutationSuccessEffect(mutation) { … }` は成功時に発火し、reply のタイムスタンプをキーとするため、完了したミューテーションごとに 1 回実行されます。
- `MutationErrorEffect(mutation) { … }` は同じワンショットのキーイングのもとで失敗時に発火します。

どちらも `:core:common` にあります。

## ScreenChannel

画面のアクション（入力）と結果（出力）は、1 つの `ScreenChannel<Action, ActionResult>` に集約されます。これは `Channel.BUFFERED` 容量の 2 つの kotlinx `Channel` に支えられており、すべてのイベントがリプレイなしでちょうど 1 回配信されます。これは 1 回限りのイベントが必要とする形です。

Root は `retainScreenChannel()` でチャネルを作成します。remember ではなく retain されるのは、バッファリングされたまだ消費されていないイベントが、エントリの一時的な破棄を生き延びるようにするためです。

`ActionEffect` と `ActionResultEffect` はどちらも次の 3 つのルールを守るため、画面がそれらを再度述べる必要はありません。

- **イベントごとに 1 つのハンドラ。** 各イベントは effect の子コルーチンで処理されるため、ハンドラがまだ実行中のイベント — 実行中のミューテーション、まだ画面に出ている snackbar — が後続のイベントを止めることはありません。したがってハンドラは重なります。ある書き込みが次の前に完了することを要求する画面は、自分でそれらを順序付けます。
- **ハンドラは最新のコンポジションに対して実行される。** effect はチャネルのみをキーとするため、それを起動したコンポジションより長く生存します。ハンドラは `rememberUpdatedState` を通じて読み取られます。それがなければ、ハンドラは起動時のコンポジションの値をクローズオーバーし、画面がすでに先へ進んだ状態を書き戻してしまいます。
- **throw したハンドラは報告されるが致命的ではない。** 失敗は `KaigiLogger.error` へ送られ、そこから `CrashReporter` に非致命的なレポートとして転送され、画面は消費を続けます。ここでの失敗はユーザが対処できる状況ではなく画面の欠陥です。ユーザに見せるべきものは上記の snackbar の sink を通ります。

それぞれの端は context パラメータでゲートされているため、誤った層から誤った端を使うことは通常のコンパイルエラーになります。

| 操作 | 方向 | Context | 形態 |
| --- | --- | --- | --- |
| `send(action)` | Root が生成 | `ScreenContext` | non-suspend |
| `ActionEffect(channel) { … }` | Presenter が消費 | `PresenterContext` | effect |
| `emit(result)` | Presenter が生成 | `PresenterContext` | suspend |
| `ActionResultEffect(channel) { … }` | Root が消費 | `ScreenContext` | effect |

`emit` は `suspend` であり `PresenterContext` でゲートされているため、effect の内部でのみ実行でき、コンポジション本体からは実行できません。これは presenter が `PresenterContext` のみを宣言し `ScreenContext` を決して宣言しない（[ScreenContext の設計](./screen-context.ja.md) におけるロールとコンテキストの分離。`PresenterMustNotDeclareScreenContextChecker` FIR checker によって強制されます — [enforcement](./enforcement.ja.md) を参照）ために成り立ちます。

```kotlin
// Presenter (context: PresenterContext)
ActionEffect(screenChannel) { action -> if (action is Save) mutation.mutateAsync(action.value) }
MutationSuccessEffect(mutation) { screenChannel.emit(NavigateToCard) }
MutationErrorEffect(mutation) { screenChannel.emit(ShowMessage(it.toUserMessage())) }

// Root (context: ScreenContext)
ActionResultEffect(screenChannel) { result ->
    when (result) {
        NavigateToCard -> onSaved()
        is ShowMessage -> snackbarHostState.showSnackbar(result.message)
    }
}
// UI input: onSaveClick = { screenChannel.send(Save(value)) }
```

アクションも 1 回限りのイベントも持たない読み取り専用の画面には `ScreenChannel` は不要です。

## ナビゲーションのみのクリック

ナビゲーションだけを行うクリック（タップ → 画面を開く、それ以外は何もしない）は presenter を経由しません。Root は自身の `on*` ナビゲーションラムダをそのまま Screen の対応するパラメータへ渡し、Screen はユーザがタップするコントロールからそれを呼び出します。そのようなクリックをチャネルに通して、そのまま変えずに送り返すだけの実装は `NoForwardOnlyActionChecker` FIR checker によって拒否されます。

presenter が起点となるナビゲーションは結果として流れ戻ります。ミューテーションが成功 → `emit(NavigateToCard)` → `ActionResultEffect` → Root のナビゲーションラムダ。

## ナビゲーション重複に対する冪等性

成功したナビゲーションは、3 つの層で重複発火から保護されます。

1. チャネルは各結果をちょうど 1 回だけ消費します。
2. `MutationSuccessEffect` は成功したミューテーションごとに 1 回発火するため、1 つのナビゲーションは 1 つの結果を emit します。
3. `NavigatorEffect` は、すでにバックスタックの最上位にあるキーの `Push` をスキップします — [バックスタックのガード](./navigation-navigator.ja.md#バックスタックのガード) を参照してください。

関連: [アーキテクチャ概要](./architecture-overview.ja.md) · [Soil のミューテーション](./soil-mutation.ja.md) · [SoilDataBoundary](./soil-data-boundary.ja.md) · [画面の実装](./building-a-screen.ja.md)
