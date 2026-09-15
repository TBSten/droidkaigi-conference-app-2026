# NavKey シリアライザの集約（NavKeySerializersProvider）

各 feature は自身の `NavKey` のシリアライザを登録し、`MergedNavKeySerializersProvider` がそれらをバックスタックが必要とする単一の `SerializersModule` にマージします — feature ごとの登録は KSP が生成します。

## 集約する理由

Navigation3 のバックスタックは `NavKey` のリストであり、設定変更やプロセス死をまたいで **保存・復元するためにシリアライズされなければなりません**。`rememberNavBackStack` は、`SerializersModule` が **アプリ内のすべての `NavKey`** をカバーする `SavedStateConfiguration` を要求します。しかし `NavKey` は別々の feature モジュールに存在し、sealed な階層はモジュールをまたげません — そのため各キーは open な多態として登録する必要があり、モジュールから漏れたキーは、そのキーがバックスタックに載った状態で保存された **瞬間に実行時クラッシュします**。

この登録が誰かが中央のファイルを編集するのを覚えていることに依存しないよう、各 feature が **自身の `NavKey` の多態シリアライザを contribute** し、`MergedNavKeySerializersProvider`（`:core:common`、`UiGraph` を通じて公開）がそれらを、`KaigiApp` がバックスタックを構築するときに渡す単一の `SerializersModule` にマージします。

## 方法: contribute とマージ

```kotlin
interface NavKeySerializersProvider {
    val serializersModule: SerializersModule
}

@Inject
@SingleIn(AppScope::class)
class MergedNavKeySerializersProvider(providers: Set<NavKeySerializersProvider>) : NavKeySerializersProvider {
    override val serializersModule: SerializersModule = SerializersModule {
        providers.forEach { provider ->
            include(provider.serializersModule)
        }
    }
}
```

各プロバイダは `polymorphic(NavKey::class) { subclass(TimetableNavKey::class, TimetableNavKey.serializer()) }` によって自身のキーを登録し、`@ContributesIntoSet(AppScope::class)` によってその set に入ります。

## KSP がプロバイダを生成する理由

feature ごとのプロバイダは純粋なボイラープレートであり — モジュールの `NavKey` サブクラスを列挙してそれぞれを登録するだけです — 手で書くとこの集約が塞ごうとしているまさにその穴、つまり新しいキーの追加を忘れることを再び持ち込みます。そこで `:tools:ksp-processor` が各 feature モジュールを走査して `NavKey` のサブクラスを探し、その `…NavKeySerializersProvider` を出力します。これは `droidkaigi.convention.kmp-feature` [Convention プラグイン](./build-convention-plugins.ja.md) に組み込まれており（`add("kspCommonMainMetadata", project(":tools:ksp-processor"))` で、生成されたソースは `commonMain` に追加されます）、したがって **`NavKey` を追加すれば自動的にそのシリアライザが登録されます**。

有用な副作用として、生成されたコードは各キーのコンパニオンの `.serializer()` を参照するため、`@Serializable` が付いていない `NavKey` はバックスタックの保存時にクラッシュするのではなく **コンパイルに失敗します** — カスタムチェッカーなしでの enforcement です。

コード生成はここまでです。各 feature の `NavEntryProvider` と `Default…Navigator` は意図的に手書きです（それぞれ数行です）。

関連: [NavEntry の集約（NavEntryProvider）](./navigation-entry-aggregation.ja.md) · [Enforcement](./enforcement.ja.md)
