# エントリの保持（RetainNavEntryDecorator）

画面のグラフのような値 — `retain { screenGraphFactory.createTimetableScreenGraph() }` — を、**画面の `NavEntry` がバックスタックに残っている間ちょうどそのあいだだけ** 生存させたいと考えています。Navigation3 は Compose の `retain {}` API との統合を提供していないため、カスタムの `NavEntryDecorator` で自前で配線します。

## 方法: decorator によるエントリごとの RetainScope

公式の [Navigation 3 retain レシピ](https://developer.android.com/guide/navigation/navigation-3/recipes/retain) に示された統合方法に従い、`RetainNavEntryDecorator` は **`NavDisplay` 全体で 1 つの `RetainedValuesStoreRegistry`** を保持し、各 `NavEntry` に **キーごとのストア** を供給します。保持された値はエントリがバックスタックにあるあいだ生存し、エントリが pop されると破棄されます（リークなし）。つまり **`NavEntry` のライフサイクルと同期** します。

```kotlin
@Composable
fun <T : Any> retainNavEntryDecorator(): NavEntryDecorator<T> {
    val registry = retainRetainedValuesStoreRegistry()              // one per NavDisplay (retained)
    return remember(registry) {
        NavEntryDecorator(
            onPop = { contentKey -> registry.clearChild(contentKey) }, // discard on pop
            decorate = { entry ->
                registry.LocalRetainedValuesStoreProvider(entry.contentKey) { entry.Content() }
            },
        )
    }
}
```

registry は（エントリごとに実行される）`decorate` ラムダの **外側** で作成されるため、すべてのエントリが 1 つの registry を共有しつつ、それぞれがキーごとに異なるストアを得ます。

## エントリごとに 1 つの呼び出し箇所

`RetainNavEntryDecorator` とエントリのコンテンツのあいだにあるすべての decorator は、エントリの生存期間全体を通じて単一の呼び出し箇所から `entry.Content()` を呼び出さなければなりません。2 つの呼び出し箇所を選択する decorator — `entry.Content()` を囲む `if` の条件が、`LocalListDetailSceneScope` のように、エントリが現在存在する scene に依存するもの — は、detail ペインがリストの横に開いた瞬間にコンテンツを新しい位置へ移動させます。Compose は古い位置を破棄し、その下にあるすべての `remember` と `retain` が失われます。

保持はこの移動をカバーしません。ストアが退出する値を保持するのは、自身の provider がコンポジションから離脱しているあいだだけですが、ここでは provider はコンポジションに残り続けるため、値は保持されるのではなく破棄されます。エントリに対して固定であるエントリのメタデータで分岐し、その単一の分岐がコンテンツに渡すものを変えてください。

エントリが生き延びる移動であっても、一部の状態は意図的にリセットされます。ペインのエントリがリスト-詳細の境界をまたぐときに再構築しなければならない lazy list / grid の状態については、[ペイン内の Lazy コンテナ](./navigation-list-detail.ja.md#ペイン内の-lazy-コンテナ) を参照してください。

関連: [ルート NavEntry のエミュレーション（RootSceneStrategy）](./navigation-predictive-back-tabs.ja.md) · [ScreenContext の設計](./screen-context.ja.md)
