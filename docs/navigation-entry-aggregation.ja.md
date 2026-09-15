# NavEntry の集約（NavEntryProvider）

新しい遷移先ごとに中央の `NavDisplay` を編集するとマージコンフリクトを招くため、**各 feature が自身のエントリを提供**し、`NavDisplay` は集約された provider を読みます。

## feature 側: エントリをセットに提供する

```kotlin
interface NavEntryProvider {
    fun EntryProviderScope<NavKey>.register()
}

@ContributesIntoSet(UiScope::class)
@Inject
class TimetableNavEntryProvider(
    private val screenGraphFactory: TimetableScreenGraph.Factory, // the per-screen graph factory (contributed to UiScope)
) : NavEntryProvider {
    override fun EntryProviderScope<NavKey>.register() {
        entry<TimetableNavKey> {
            val graph = retain { screenGraphFactory.createTimetableScreenGraph() } // retain scope supplied by RetainNavEntryDecorator
            context(graph.screenContext) {
                TimetableScreenRoot(onNavigateToDetail = { graph.screenNavigator.openSessionDetail(it) })
            }
        }
    }
}
```

## app-shared 側: 一度だけ集約する

```kotlin
@Inject
@SingleIn(UiScope::class)
class AppEntryProvider(providers: Set<NavEntryProvider>) {
    val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
        providers.forEach { provider ->
            with(provider) {
                register()
            }
        }
    }
}
```

- Metro の `@ContributesIntoSet` は**追加設定ゼロ**でモジュール境界を越えて集約します（feature のエントリが app-shared の `UiGraph` に届きます）。
- 引数を取る画面（詳細）は `retain(key) { screenGraphFactory.create(key.id) }` で **id ごとのグラフ**を構築します。`@SingleIn` な ScreenContext（および、存在する場合は画面の Navigator）は、その retain されたグラフから読み取られます（[ScreenContext の設計](./screen-context.ja.md)）。

## KaigiApp 側: NavDisplay が集約された provider を読む

```kotlin
NavDisplay(
    backStack = backStack,
    // …
    entryProvider = uiGraph.appEntryProvider.entryProvider,
)
```

画面の追加が `KaigiApp` に触れることは決してありません。新しい feature の `NavEntryProvider` がセットに加わり、`NavDisplay` は同じ 1 行を通してそれを拾い上げます。

関連: [NavKey シリアライザの集約（NavKeySerializersProvider）](./navigation-navkey-serializers.ja.md) · [Navigator](./navigation-navigator.ja.md)
