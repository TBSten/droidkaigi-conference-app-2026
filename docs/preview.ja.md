# プレビューとサンプルアセット

Compose の `@Preview` にはサンプルデータと画像が必要ですが、それらのアセットは **リリースビルドに含まれてはいけません**。

## モジュール分割（プロダクションからの隔離）

- `:core:preview:api` — 契約とサンプルデータ。型安全な `PreviewImage` enum、`PreviewImageResolver` インタフェース、`LocalPreviewImageResolver`（デフォルトは `null`）、`PreviewScope` マーカー、`NoopPreviewImageResolver`（`@ContributesBinding(PreviewScope)` のデフォルトで、何も解決しません）、`PreviewImageResolverDefaults`（app graph 向けの同等物）、そしてモデルの `fake()` ビルダー。画像バイナリは含みません。
- `:core:preview:impl` — 画像バイナリ（Compose Resources）と `DefaultPreviewImageResolver`。`@ContributesBinding(PreviewScope, replaces = [NoopPreviewImageResolver::class])` で contribute されるため、`:impl` がクラスパスにあるところではどこでも no-op のデフォルトを上書きします。
- `:core:preview:wrapper` — Metro の `PreviewGraph`（`@DependencyGraph(PreviewScope)`）、`KaigiPreviewTheme`、そして feature が自身のプレビューに適用する wrapper。

```text
core/preview/
├─ api/src/commonMain/kotlin/.../preview/
│    PreviewImage.kt            # the type-safe enum (generated)
│    PreviewImageResolver.kt    # contract + LocalPreviewImageResolver
│    PreviewScope.kt            # Metro scope marker
│    NoopPreviewImageResolver.kt # @ContributesBinding(PreviewScope) default; resolves nothing
│    TimetableFake.kt           # Timetable.fake(), TimetableItem.fake()
│    ContributorsFake.kt        # Contributors.fake()
│    SponsorsFake.kt            # Sponsors.fake()
├─ impl/src/commonMain/
│    ├─ kotlin/.../preview/impl/
│    │    DefaultPreviewImageResolver.kt   # @ContributesBinding(PreviewScope, replaces=[Noop]); URL -> resource
│    └─ composeResources/drawable/
│         *.png                            # the image binaries
└─ wrapper/src/commonMain/kotlin/.../preview/wrapper/
     PreviewGraph.kt                  # @DependencyGraph(PreviewScope)
     KaigiPreviewTheme.kt             # KaigiTheme + the resolver, for any preview to call
     KaigiPreviewWrapper.kt           # applies it under one fixed colour scheme
```

`KaigiPreviewTheme` と `PreviewGraph` は `:impl` や `:api` ではなく `:wrapper` にあります。wrapper は、プレビューをレンダリングする 2 つのターゲットである Android と JVM において `:core:preview:impl` を `compileOnly` 依存として取り込みます。これにより Metro は wrapper のコンパイル時に contribute された `DefaultPreviewImageResolver` のバインディングを集約でき、一方で `:impl` はプロダクションのクラスパスから外れたままになります。Kotlin/Native と wasm は partial linkage に頼って dangling な参照を許容し、どちらもプロダクションで実行されることはありません。グラフを `:api` の外に置くことは循環も避けます。`:api -> :impl` は `:impl -> :api` と衝突してしまいます。

プロダクションは（後述の feature convention を通じて）`:core:preview:wrapper` に依存しますが、`:core:preview:impl` には決して依存しないため、画像バイナリはリリースから物理的に除外されます。`:impl` をクラスパスに載せるのはプレビュー / テストビルドだけであり、同じサンプルデータをスクリーンショットテストや fake ビルドと共有します。

## サンプルデータ

サンプル値は、それが構築する型の隣に宣言された、companion object 上の `fake()` 拡張です。モデルの fake は `:core:preview:api` にあります。

```kotlin
fun Timetable.Companion.fake(): Timetable = Timetable(/* sessions across both days, some bookmarked */)
```

画面の `UiState` は feature モジュール内の宣言の隣に自身の `fake()` を持ち、presenter が使うのと同じマッピングを通じてモデルの fake から組み立てられます。そのため、プレビューはプロダクションでも生成しうる状態をレンダリングします。

```kotlin
internal fun TimetableListSectionUiState.Companion.fake(): TimetableListSectionUiState {
    val timetable = Timetable.fake()
    return TimetableListSectionUiState(
        timeSlots = timetable.itemsOn(DroidKaigi2026Day.Day1).toTimeSlots(),
        bookmarks = timetable.bookmarks,
    )
}
```

プレビューは `uiState = TimetableScreenUiState.fake()` を渡すだけで、自前のフィクスチャを宣言しません。テストも同じ呼び出しを通じて同じ値に到達します。

## プレビューの wrapper

`KaigiPreviewTheme` は、すべてのプレビューがその内側でレンダリングされる包みです。指定されたカラースキームのもとで `KaigiTheme` を適用し、`PreviewImageResolver` を — `createGraph<PreviewGraph>().previewImageResolver` によって遅延生成し — `LocalPreviewImageResolver` を通じて提供します。これにより `RemoteImage` は `preview://` の URL をローカルの drawable に解決します。

プレビューがそこへ到達する方法は 2 つあります。ほとんどのプレビューは固定されたスキームを 1 つ取るため、`KaigiPreviewWrapper` がそれを供給し、プレビュー側はアノテーション（`@PreviewWrapper`、これも `androidx.compose.ui.tooling.preview` 由来）だけを持ちます。

```kotlin
@PreviewWrapper(KaigiPreviewWrapper::class)
@Preview
@Composable
private fun AboutScreenPreview() {
    AboutScreen(/* sample */)
}
```

自身でスキームを選ぶプレビューは wrapper を通じてそれを受け取れないため、自分で `KaigiPreviewTheme` を開きます（[マルチテーマプレビュー](#マルチテーマプレビュー) を参照）。`PreviewRequiresWrapperChecker` FIR checker はどちらの形も受け入れ、それ以外をすべて拒否します。素の `KaigiTheme` を開く本体も含まれます。それは resolver なしでレンダリングされてしまいます。

## マルチテーマプレビュー

コンテンツが `@ThemeSensitive` であるプレビューは、`@PreviewParameter(KaigiSchemeProvider::class)` を通じてカラースキームを受け取り、`KaigiPreviewTheme` に渡します。

```kotlin
@Preview
@Composable
private fun TimetableScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        TimetableScreen(uiState = /* sample */, onBookmarkClick = {}, /* … */)
    }
}
```

`PreviewParameterProvider` は値ごとに 1 つのレンダリングを生成するため、ツールは `KaigiColorScheme` ごとにフルサイズのタイルを 1 つ生成し、タイルの配置自体も行います。プレビューのサイズはどこにも宣言しません。レンダリング面はコンテンツに合わせて縮みますが、レンダリング先のデバイスを超えて拡大することはありません。そのため、すべてのテーマを横並びに保持する単一のレンダリングは固定幅を指定しなければならなくなりますが、分かれたタイルはそれぞれデバイスサイズのままです。`KaigiSchemeProvider` は `:core:preview:api` にあります。パラメータの型は inline value class ではなく enum でなければなりません。

`ThemeSensitivePreviewChecker` FIR checker は、そのようなパラメータを取らないテーマ依存のプレビューを拒否します — [Enforcement](./enforcement.ja.md) を参照してください。

## マルチロケールプレビュー

コンテンツが Compose Resources の文字列を解決するプレビュー（[ローカライズ](./localization.ja.md) を参照）は、`@Preview` の代わりに `@LocalePreviews` を持ちます。

```kotlin
@LocalePreviews
@Composable
private fun EventMapScreenPreview(
    @PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    KaigiPreviewTheme(colorScheme) {
        EventMapScreen(uiState = /* sample */, onFloorClick = {})
    }
}
```

`@LocalePreviews` は `@Preview(locale = "en")` と `@Preview(locale = "ja")` を持つマルチプレビューのアノテーションなので、ツールはロケールごとに 1 つのタイルを、そしてプレビューが `@PreviewParameter` も取る場合はその値ごとにレンダリングします。これは `KaigiSchemeProvider` の隣、`:core:preview:api` にあります。

`LocaleSensitivePreviewChecker` FIR checker は、それを持たないロケール依存のプレビューを拒否します — [Enforcement](./enforcement.ja.md) を参照してください。

型安全なプレビュー画像 enum は別途生成されます — [プレビュー画像 enum の生成](./preview-image-enum.ja.md) を参照してください。

## 配線（プロダクションはアセットを持たないまま）

`droidkaigi.convention.kmp-feature` プラグインは、すべての feature の `commonMain` に `implementation(project(":core:preview:wrapper"))` を与えるため、各 `@Preview` の隣で `KaigiPreviewWrapper` を参照できます。wrapper は `:core:preview:impl` を `compileOnly` としてのみ持つため、画像バイナリと `DefaultPreviewImageResolver` がプロダクションの実行時クラスパスに届くことはありません。`:impl` が存在しない場所では Metro のグラフは `NoopPreviewImageResolver` にフォールバックし、プレビューは空白でレンダリングされます（プレビューはプレビュー / スクリーンショットのクラスパスでのみレンダリングされるため、これがプロダクションで起きることはありません）。

`KaigiApp` は常に app graph から `LocalPreviewImageResolver` を提供します。異なるのはその背後にある resolver です。リリースビルドは何も解決しない `PreviewImageResolverDefaults` をバインドするため、すべての画像 URL はネットワーク経路を通ります。無条件に提供することでコンポジションは一様に保たれます。リリースビルドが差し控えるのは provider ではなくバインディングです。

`:core:preview:impl` はプレビューを *レンダリングする* クラスパスには載っている必要がありますが、`releaseRuntimeClasspath` には含まれていません。それを取り込む非プロダクションの経路は次のとおりです。

- **Android Studio の `@Preview` レンダリング** — `kmp-feature` convention が `"androidRuntimeClasspath"(project(":core:preview:impl"))`（および `androidMain` での `compileOnly(project(":core:preview:impl"))`）を追加し、drawable リソースが IDE のプレビューレンダラから見えるようにします。`kmp.compose` primitive は `ComposeViewAdapter` のために `"androidRuntimeClasspath"(libs.composeUiTooling)` を追加します。どちらの configuration もリリースの実行時クラスパスには供給されません。
- **テスト / CI** — テストのソースセット（たとえば `jvmTest`）から `:core:preview:impl` に依存します。`:core:preview:wrapper` の `PreviewWiringTest` は、`PreviewGraph` がそこから contribute された `DefaultPreviewImageResolver` を解決し、`preview://` の URL を `DrawableResource` にマップすることを示します。`Robot` は自身がコンポーズする画面にその resolver を提供するため、robot のキャプチャはプレビューと同じ URL を解決します。
- **Android の dev フレーバー** — `devImplementation(project(":core:preview:impl"))` に加えて `PreviewImageResolverDefaults` を置き換えるバインディングにより、fake サーバ環境の `preview://` の URL が drawable としてレンダリングされます。prod フレーバーは何も解決しないデフォルトのままです。

## Android Studio でのプレビューレンダリング

Android Studio はこれらの `commonMain` のプレビューを Android ターゲットを通じてレンダリングします。`kmp.compose` primitive が `ui-tooling`（`ComposeViewAdapter`）を `androidRuntimeClasspath` に載せ、`kmp-feature` convention がそこに `:core:preview:impl` を追加することで、レンダリング時に drawable リソースが解決されます（[配線](#配線（プロダクションはアセットを持たないまま）) を参照）。インタラクティブなペインがプレビューをレンダリングできない場合は、アプリをビルドして実行すれば（`./gradlew :app-android:assembleDevDebug`）実際の画面を確認でき、また Roborazzi + ComposablePreviewScanner のパイプラインがプレビューをヘッドレスにレンダリングします — [テスト概要](./testing.ja.md) を参照してください。

## スクリーンショットテスト

プレビューは `private` です。ツールとスクリーンショットのスキャン以外がプレビューを呼ぶことはありません。Roborazzi + ComposablePreviewScanner は `@PreviewWrapper` / `@PreviewParameter` を尊重するため、同じプレビューが Android 上でスクリーンショットテストを駆動します — [プレビュースクリーンショットテスト](./testing-preview-screenshot.ja.md) を参照してください。

関連: [テスト概要](./testing.ja.md) · [Convention プラグイン](./build-convention-plugins.ja.md)
