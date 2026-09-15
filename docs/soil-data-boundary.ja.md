# SoilDataBoundary

`SoilDataBoundary` はデータ取得を UI から切り離します。その content ラムダの内側ではすべての reply が利用可能であることが保証されるため、UI がローディングやエラーの状態を直接扱うことは決してありません。

```kotlin
SoilDataBoundary(
    state1 = rememberQuery(screenContext.timetableQueryKey),
    state2 = rememberSubscription(screenContext.favoriteTimetableIdsSubscriptionKey),
) { timetable, favoriteIds ->
    // both values are non-null here
}
```

## 合成

これは soil-reacty の 3 つのプリミティブの薄い合成です — `ErrorBoundary`（致命的なエラー / ロード失敗 → 全画面のフォールバック）が `Suspense`（ローディングのフォールバック）を包み、それが `Await`（すべての reply が揃った時点で content を描画する）を包みます。

```kotlin
context(_: SoilDataContext)
@Composable
fun <T> SoilDataBoundary(
    state: DataModel<T>,
    modifier: Modifier = Modifier,
    fallback: SoilFallback = SoilFallbackDefaults.default(),
    content: @Composable (T) -> Unit,
) {
    ErrorBoundary(
        fallback = fallback.errorFallback,
        onReset = rememberQueriesErrorReset(), // retry: resume every failed query (soil-query-compose util)
        modifier = modifier,
    ) {
        Suspense(fallback = fallback.suspenseFallback) {
            Await(state = state, content = content)
        }
    }
}
```

- 1 ソースと 2 ソースのオーバーロードがあります。より多くの引数を取るバリアントは、画面が必要とするのに応じて追加してください。
- これは **`SoilDataContext`（context parameter）にゲートされている**ため、データの読み取りが認められた場所にしか現れません — presenter やただの composable に紛れ込んだデータバウンダリは、コンパイラが拒否します。[`ScreenContext`](./screen-context.ja.md) は `SoilDataContext` を継承しているので、すべての画面ルートが条件を満たします。アプリシェルの `AppGraph` もこれを実装しており、テーマの subscription が同じバウンダリを通して描画されます。

## フォールバック画面のカスタマイズ

ローディングとエラーの UI は `fallback: SoilFallback` パラメータを通じて差し込みます。デフォルト（`SoilFallbackDefaults.default()`）は、ローディング中は中央寄せの `CircularProgressIndicator` を表示し、エラー時にはローカライズされた `AppError` のカテゴリを添えた "Failed to load" というメッセージと再試行ボタンを表示します。独自の見た目にしたい画面は `SoilFallbackDefaults.custom(...)` を渡します。

```kotlin
SoilDataBoundary(
    state1 = rememberQuery(screenContext.timetableQueryKey),
    fallback = SoilFallbackDefaults.custom(
        suspenseFallback = { TimetableSuspenseFallback() },
        errorFallback = { TimetableErrorFallback() },
    ),
) { timetable -> … }

// The error view declares the context parameter and reads the cause from it.
context(errorContext: SoilErrorContext)
@Composable
fun TimetableErrorFallback(modifier: Modifier = Modifier) {
    Text("Failed to load: ${errorContext.errorBoundaryContext.err.message}", modifier = modifier)
}
```

各フォールバックは `Box` の内側でコンポーズされ（ラムダは `BoxScope` の拡張です）、context parameter とともに実行されます — ローディングには `SoilSuspenseContext`、エラーには `SoilErrorContext` で、後者は soil-reacty の `ErrorBoundaryContext`（スローされたエラー）を公開します。上記のように自前のフォールバック composable にその context を宣言することが、エラーの詳細に到達する方法です。

関連: [Soil のキー](./soil-keys.ja.md) · [エラーハンドリング](./error-handling.ja.md)
