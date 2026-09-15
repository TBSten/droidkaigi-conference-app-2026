# Soil のミューテーション

**mutation** は Soil の書き込み経路です。お気に入りのトグル、プロフィールの更新 — 読み取りではなくデータを変更するものすべてです。presenter は Soil の mutation を通じて書き込みを発火し、その結果を状態として観測します。発火して放置することはなく、命令的にナビゲートすることもありません。ローディング・成功・失敗はすべて mutation の状態とその一度きりのエフェクトを通じて戻ってきます。

## 使い方

1. **`ActionEffect` のハンドラから `mutateAsync` で発火します。**
2. 画面が必要とする場所で `MutationState` を UiState にマッピングして、**ローディングを反映します**。
3. **成功は `MutationSuccessEffect` で処理します** — 後続の処理 (ナビゲーション、メッセージ) を `ActionResult` として emit します。
4. **失敗は `MutationErrorEffect` (`:core:common`) で処理します** — 同じように `ShowMessage` の result を emit します。

```kotlin
val mutation = rememberMutation(presenterContext.profileMutationKey)

ActionEffect(screenChannel) { action ->
    when (action) {
        is CreateProfile -> mutation.mutateAsync(action.profile) // mutateAsync, not mutate
    }
}
// success/failure are emitted as a result via screenChannel.emit → Root's ActionResultEffect handles them
MutationSuccessEffect(mutation) {
    screenChannel.emit(NavigateToCard)
    mutation.reset() // required — see "Resetting the mutation state"
}
MutationErrorEffect(mutation) {
    screenChannel.emit(ShowMessage(it.toUserMessage()))
    mutation.reset()
}
```

## なぜ `mutate` ではなく `mutateAsync` なのか

`mutate` は結果を待ち、それを — **スローされた例外も含めて** — 呼び出し元にそのまま返します。これには 2 つの帰結があります。

- **捕捉されないエラーがクラッシュを招きます。** 失敗した fetcher は `mutate` の呼び出し箇所で再スローするため、すべての呼び出し箇所が自前の `try`/`catch` を必要とします。1 つ忘れればアプリはクラッシュします。この使い方では、Soil はただの suspend 関数の呼び出しに成り下がります。
- **フィードバックが黙って失われることがあります。** 結果は、それを待っているコルーチンにしか届きません。そのコルーチンが途中でキャンセルされると (画面がコンポジションから外れると)、`mutate` の後に書かれた後続処理は破棄されます — ユーザは成功もエラーも目にしません。

`mutateAsync` は代わりに結果を `SwrClient` が所有する mutation の状態に記録し、エフェクトがそこから消費します。エラーは状態として表面化し (再スローされません)、`MutationSuccessEffect` / `MutationErrorEffect` はどの成功/エラーをすでに処理したかを追跡するので、フィードバックはリコンポジションと呼び出し元のキャンセルを生き延びます。要するに、`mutateAsync` こそが Soil の mutation キャッシュを輪の中に留めるものです。

これは単なる慣習ではありません。**`NoDirectMutate` FIR チェッカー** (`:tools:compiler-plugin`、すべてのモジュールに適用) が直接の `mutate` 呼び出しを**コンパイルエラー**にするため、この誤用は出荷され得ません — [Enforcement](./enforcement.ja.md) を参照してください。

## MutationSuccessEffect / MutationErrorEffect

どちらの一度きりのエフェクトもこのプロジェクト自身のもの (`:core:common`) で、Soil の `MutatedEffect` を置き換えます。この対を自前で所有することで、キーの規則を 1 つに保てます — 成功側は `replyUpdatedAt`、エラー側は `errorUpdatedAt` (タイムスタンプ) で消費します — さらに、リコンポジションの最適化によってゼロにされてしまう `MutatedEffect` の `mutatedCount` のデフォルトを回避できます。エラー側は次のとおりです。

```kotlin
// Collects the state via snapshotFlow and consumes each error exactly once, keyed by
// errorUpdatedAt. MutationSuccessEffect is its mirror image (replyUpdatedAt-keyed).
@Composable
fun MutationErrorEffect(
    mutation: MutationObject<*, *>,
    onError: suspend (Throwable) -> Unit,
) {
    val mutationState by rememberUpdatedState(mutation)
    var lastConsumedKey by rememberSaveable { mutableStateOf<Long?>(null) }
    LaunchedEffect(Unit) {
        snapshotFlow { mutationState as? MutationErrorObject }
            .filterNotNull()
            .collect {
                if (lastConsumedKey != it.errorUpdatedAt) {
                    lastConsumedKey = it.errorUpdatedAt
                    onError(it.error)
                }
            }
    }
}
```

## mutation の状態のリセット

消費された Success/Error は、リセットされるか GC されるまで `SwrClient` のキャッシュに残ります。2 つのルールが、この残留物の誤発火を防ぎます。

- **画面をまたぐ残留は構造上起こり得ません**: すべての `MutationKey` 実装は画面ごとの `MutationTag` を `MutationId` に持つため、画面が mutation のキャッシュエントリを共有することはありません (`MutationKeyMustCarryTag` チェッカーによって強制されます — [Soil のキー](./soil-keys.ja.md) を参照してください)。
- **`reset()` は常に明示的であり、エフェクトの中に隠されることはありません。** ハンドラ自身が `mutation.reset()` を呼びます。エフェクトが代わりにリセットしないのは意図的です。ハンドラは suspend ブロックであり、隠されたリセットが走る前に中断点 (`showSnackbar`、`delay`) でキャンセルされれば、状態は黙って生き残ってしまいます。いつ破壊的に消費するかは画面ごとの判断であり、呼び出し箇所から見えるべきです。とはいえ、ハンドラが*どこかで*リセットすること自体は任意ではありません。それがなければ、消費された状態は画面のインスタンスより長く生き残り、次のインスタンスで再発火します — `MutationEffectMustReset` チェッカー ([Enforcement](./enforcement.ja.md)) は、`reset()` を一度も呼ばない `MutationSuccessEffect` / `MutationErrorEffect` のハンドラを拒否します。

関連: [Soil のキー](./soil-keys.ja.md) · [エラーハンドリング](./error-handling.ja.md) (`ScreenChannel` による result の配送)
