# 画面ごとのグラフ（@GraphExtension）

[AppGraph と UiGraph](./di-app-graph.ja.md) と並んで、**各画面はそれ自身の DI グラフ**を持ちます。これは Metro の `@GraphExtension` であり、`:core:model` にある画面ごとのマーカー (`TimetableScreenScope`、`AboutScreenScope`、…) にスコープされます。

このグラフは `@ContributesTo(UiScope)` なファクトリを通じて [UiGraph](./di-app-graph.ja.md) に contribute され、画面の `ScreenContext` を accessor として公開します。

```kotlin
// just a marker — a sealed interface has no constructor and nothing can implement it
sealed interface TimetableScreenScope

@GraphExtension(TimetableScreenScope::class)
interface TimetableScreenGraph {
    val screenContext: TimetableScreenContext
    val screenNavigator: TimetableScreenNavigator

    @GraphExtension.Factory
    @ContributesTo(UiScope::class)
    fun interface Factory {
        fun createTimetableScreenGraph(): TimetableScreenGraph
    }
}
```

- **なぜ画面ごとにグラフを持つのか。** 画面スコープのバインド — `ScreenContext`、キー付きの query key、画面の Navigator — は `UiScope` より狭いスコープに属するべきであり、そうすることで `@SingleIn(<ScreenScope>)` にでき、アプリのグラフや UI のグラフからは到達できないままにできます。
- **どのように構築されるか。** グラフは UI のグラフから解決されるのではなく、その `Factory` から作成され、`@SingleIn(<ScreenScope>)` な accessor はグラフが保持されている間ずっと安定した `ScreenContext` を返します。

そこで公開される `ScreenContext` と、より細かいスコープのルールについては [ScreenContext の設計](./screen-context.ja.md) で扱います。

関連: [AppGraph と UiGraph](./di-app-graph.ja.md) · [ScreenContext の設計](./screen-context.ja.md)
