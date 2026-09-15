# Soil の永続化

`buildPersistedQueryKey` は成功したすべての**サーバレスポンス**を永続化し、次回起動時に復元します。そのため、永続化された画面はオフラインでも即座に描画されます。永続化されるペイロードは意図的に、ドメインモデルではなくサーバレスポンスです。モデル層のリファクタリングがキャッシュを無効化することは決してなく、無効化するのはサーバの契約の変更だけです。

## 仕組み

`buildPersistedQueryKey` は Soil の `buildQueryKey` を 2 つのラムダでラップしたものです — `fetchResponse` がサーバレスポンス（永続化される形式）を生成し、`transformToDomainModel` がそれをモデル（query の `T`）へ整形します。

```kotlin
inline fun <T : Any, @MustBeSerializable reified RESPONSE : Any> buildPersistedQueryKey(
    id: QueryId<T>,
    persistKey: String,
    fileStorage: FileStorage,
    noinline fetchResponse: suspend QueryReceiver.() -> RESPONSE,
    noinline transformToDomainModel: (RESPONSE) -> T,
): QueryKey<T>
```

- fetch に成功すると、レスポンスは kotlinx.serialization JSON でエンコードされ、`persistKey` の下で [`FileStorage`](#なぜ-datastore-ではなく-filestorage-なのか) に書き込まれます。
- 次回起動時には、Soil の `QueryKey.onPreloadData` フックが保存されたレスポンスをデコードして `transformToDomainModel` を再実行し、最初の fetch が完了する前にキャッシュを温めます。
- もはやデコードできないペイロードは**キャッシュミス**として扱われ（単純な再 fetch が 1 回走ります）、未知の JSON キーは無視されます — サーバ側の追加的な変更がキャッシュを無効化することはありません。モデルのリファクタリングはそもそもキャッシュを無効化できません。モデルが永続化形式に触れることはないからです。

## コンパイル時のゲート

`RESPONSE` の serializer は、明示的な `KSerializer` 引数として渡されるのではなく、reified な型パラメータから**内部的に**解決されます。明示的な引数は呼び出し側に `TimetableResponse.serializer()` と書くことを強いますが、IDE はこれを赤く表示します（プラグインが生成する companion のメンバを完全には解決しないためです）。reified な `serializer<RESPONSE>()` の参照は、それ自体では `@Serializable` についてコンパイル時にチェックされないため、ゲートは `MustBeSerializable` FIR checker によって回復されます（[Enforcement](./enforcement.ja.md) を参照）。`RESPONSE` に `@Serializable` が付いていない呼び出しはコンパイルエラーになります。ドメインモデルに `@Serializable` は不要です — 永続化されることがないからです。

## 明示的な永続化の識別子

`persistKey` は**必須であり、ランタイム id へフォールバックするデフォルトはありません**。ランタイム id は自由に変わりえます — 契約の typealias の FQN から導出され（`SoilIds`、[Soil のキー](./soil-keys.ja.md) を参照）、リネームや形の変更がありうるからです — が、永続化キャッシュの識別子はリリースをまたいで安定していなければならないため、明示的に名前を付けます。共有の timetable query はデフォルトで永続化されます。

```kotlin
class DefaultTimetableQueryKey(
    private val api: TimetableApi,
    private val fileStorage: ServerEnvironmentScopedFileStorage,
) : TimetableQueryKey by buildPersistedQueryKey(
    id = SoilIds.timetableQuery,
    persistKey = "timetable", // stable, explicit persisted-cache identity
    fileStorage = fileStorage,
    fetchResponse = { api.getTimetable() }, // RESPONSE = TimetableResponse; persisted as-is
    transformToDomainModel = { response -> Timetable(items = response.toTimetableItems().toPersistentList()) },
)
```

詳細画面は独自の永続化キーを追加しません — `rememberQuery(key, select)` でこのキャッシュから項目を導出します（[Soil のキー](./soil-keys.ja.md) を参照）。

## なぜ DataStore ではなく FileStorage なのか

ペイロードのキャッシュは、DataStore Preferences ではなくプロジェクトの `FileStorage`（キーごとのバイナリ blob: Android/desktop/iOS ではファイル、wasmJs では IndexedDB）に書き込まれます。Preferences は 1 つの map ファイルであり、丸ごと読み込まれて丸ごと書き直されます — 独立した、場合によっては巨大なレスポンスの blob には適さない形です。Web では DataStore のバックエンドは `WebLocalStorage`（ブラウザの localStorage: 同期的、文字列のみ、およそ 5 MB のクォータ）であり、小さな設定には向きますがレスポンスのペイロードには向きません。blob のために用意されたブラウザのストアは IndexedDB です。小さな設定（テーマ）は引き続き DataStore を使い（`DataStore<Preferences>` 上の `ThemeStore` — wasmJs では `WebLocalStorage`）、Soil の subscription を通じてリアクティブに公開されます — [Soil のキー](./soil-keys.ja.md) を参照してください。

関連: [Soil のキー](./soil-keys.ja.md) · [SoilDataBoundary](./soil-data-boundary.ja.md)
