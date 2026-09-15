# ローカライズ

アプリは 2 つのロケールを提供します。英語がベースのリソースセットで、日本語がその翻訳です。UI が描画するすべての文字列は Compose Resources の文字列であり、composable 内の文字列リテラルは欠陥です。

## 文字列の置き場所

各モジュールは、自身のコードが描画する文字列を自身のソースセット配下で所有します。

```text
feature/eventmap/src/commonMain/composeResources/
├─ values/strings.xml        # English, the base
└─ values-ja/strings.xml     # Japanese
```

`droidkaigi.primitive.kmp.compose` [Convention プラグイン](./build-convention-plugins.ja.md) は、生成される `Res` クラスのパッケージをモジュールパスから導出します。そのため `:feature:eventmap` は自身の文字列を `io.github.droidkaigi.confsched.feature.eventmap.generated.resources.Res` を通じて読み取り、どのモジュールもそのパッケージを自分で宣言しません。Compose Resources は `composeResources` ディレクトリが存在する場所にのみクラスを生成します。

`values-ja` のエントリがない文字列は英語のベースにフォールバックするため、翻訳の欠落はビルドを失敗させるのではなく表面化します。両方のロケールで値が同じ文字列は `values/` にのみ宣言します。それを繰り返す `values-ja` のエントリは冗長です。

## 文字列の読み取り

```kotlin
KaigiTopAppBar(title = stringResource(Res.string.event_map_title))
```

数量は複数形リソースを使い、そのカテゴリは言語ごとに異なります。英語は `one` と `other` を宣言し、日本語は `other` のみです。

```kotlin
Text(pluralStringResource(Res.plurals.contributors_count, count, count))
```

解決は表示する箇所で行います。presenter は `@Composable` であり、文字列が自身の構築する状態の一部である場合にはリソースを読み取ってもかまいませんが、画面が無条件に描画する文字列は UI の装飾であり、画面側で読み取ります。

## サーバが提供するテキスト

カンファレンス API はセッションタイトルを両方の言語で返します。この組は `MultiLangText` として UI まで運ばれ、描画される箇所で解決されます。

```kotlin
TimetableItemCard(title = item.title.current(), /* … */)
```

データ層で解決すると表示上の判断が [Soil](./soil-keys.ja.md) のキャッシュに焼き込まれてしまい、ロケールを変更してもキャッシュが無効化されるまで以前の言語が表示され続けます。`MultiLangText.current()` は `androidx.compose.ui.text.intl.Locale` を読み取ります。これは Compose Resources が自身の文字列を解決するのと同じロケールなので、2 種類のローカライズされたテキストは常に一致します。

## プレビュー

文字列リソースや `MultiLangText` を読み取る composable はロケール依存であり、コンパイラはそれをすべての呼び出し元に伝播します。そのようなプレビューは `@LocalePreviews` を付与し、ツールが両方のロケールでレンダリングするようにします。[マルチロケールプレビュー](./preview.ja.md#マルチロケールプレビュー) と [Enforcement](./enforcement.ja.md) を参照してください。

## ローカライズしないテキスト

- `:feature:debug` — 開発者向けツールであり、デバッグビルドにのみ存在します。
- プレビューとサンプルの値。これらはプロダクトの文言ではなくプレースホルダです。
- 語を含まない値。部屋名、フロアラベル、時間帯など。

関連: [プレビューとサンプルアセット](./preview.ja.md) · [Enforcement](./enforcement.ja.md)
