# プレビュー画像 enum の生成

画像を置くと、ビルドタスクがそれを型安全な enum のエントリに変え、UI は CompositionLocal を通じて URL から解決します。3 ステップです。

## 1. 画像を配置する

`:core:preview:impl` の Compose Resources 配下にファイルを追加します。

```text
core/preview/impl/src/commonMain/composeResources/drawable/
    avatar_sample.png
    session_cover.png
```

## 2. Gradle タスクが enum を生成する（コンパイルにフックされる）

Gradle タスクがそれらのリソース名を読み取り、`compileKotlin` の前に実行されるよう配線されて（その出力ディレクトリは `:core:preview:api` の `commonMain` ソースセットに追加されます）、画像ごとに 1 つのエントリを持つ `PreviewImage.kt` を出力します — KSP ではなく素のビルドタスクです。

各エントリは、専用の `preview://` スキームの下に安定した `imageUrl`（リソース名をキーとします）も持つため、実在のネットワーク URL と衝突することなくその代わりを務められます — プロダクションの UI は画像を URL で読み込むので、プレビューもそうしなければなりません。

```kotlin
// build/generated/.../preview/PreviewImage.kt   (lands in core:preview:api)
enum class PreviewImage(val imageUrl: String) {
    AvatarSample("preview://avatar_sample"),
    SessionCover("preview://session_cover"),
}
```

したがって drawable の追加や削除は次のビルドで enum を変え、削除された画像への参照はコンパイルが通らなくなります。

## 3. UI で URL から解決する（CompositionLocal）

プロダクションでは共通の `RemoteImage(imageUrl: String)` composable でネットワーク画像を読み込みます。サンプルデータは実際の URL の代わりにプレビュー URL を渡します。

```kotlin
Speaker(avatarUrl = PreviewImage.AvatarSample.imageUrl)   // "preview://avatar_sample"
```

`RemoteImage` はまず `LocalPreviewImageResolver` を参照します。プレビュー / テストビルドでは既知のプレビュー URL をローカルリソースへ解決し、それ以外（プロダクション）ではネットワークから読み込みます。

```kotlin
@Composable
fun RemoteImage(imageUrl: String, contentDescription: String?) {
    val resource = LocalPreviewImageResolver.current?.resolve(imageUrl)
    if (resource != null) Image(painter = painterResource(resource), contentDescription = contentDescription)
    else AsyncImage(model = imageUrl, contentDescription = contentDescription) // network in production
}
```

resolver はプレビュー URL を enum へ、そしてその Compose Resource へと突き合わせます。それ以外のものには null を返すため、リリースビルドはネットワークへ抜けていきます。そもそもどの composition が resolver を得るのかは [プレビューとサンプルアセット](./preview.ja.md) です。

```kotlin
// core:preview:api — contract + injection point (null in production)
fun interface PreviewImageResolver {
    fun resolve(imageUrl: String): DrawableResource?
}
val LocalPreviewImageResolver = staticCompositionLocalOf<PreviewImageResolver?> { null }
```

```kotlin
// core:preview:impl — where the binaries live
class DefaultPreviewImageResolver : PreviewImageResolver {
    override fun resolve(imageUrl: String): DrawableResource? {
        val image = PreviewImage.entries.firstOrNull { it.imageUrl == imageUrl } ?: return null
        return when (image) {
            PreviewImage.AvatarSample -> Res.drawable.avatar_sample
            PreviewImage.SessionCover -> Res.drawable.session_cover
        }
    }
}
```

関連: [プレビューとサンプルアセット](./preview.ja.md) · [プレビュースクリーンショットテスト](./testing-preview-screenshot.ja.md)
