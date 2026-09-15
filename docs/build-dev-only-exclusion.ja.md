# 開発専用コードをリリースから除外する

開発専用のアセットと画面は、実行時の `if` ではなく**依存グラフの形**によって本番から除外されます。同じ原則の 2 つの適用例です。

## プレビューアセット — モジュール境界

`:core:preview:impl`（画像バイナリとリゾルバ）は**テスト / プレビューのソースセットからのみ**依存され、加えて Android Studio がプレビューを描画できるように `compileOnly`（および `androidRuntimeClasspath`）のエントリがあります。どちらもリリースの実行時クラスパスには到達しないため、アセットが出荷されることはありません。本番コードが見てよいのは `:core:preview:api`（型安全な契約。バイナリなし）と `:core:preview:wrapper`（`:impl` を `compileOnly` として持つ）です。詳細: [プレビューとサンプルアセット](./preview.ja.md)。

## デバッグ画面 — プラットフォームごとのゲート

`:feature:debug` は Metro の `@ContributesIntoSet` だけで自身の画面を提供するため、コンパイルクラスパスからモジュールを取り除けば画面もきれいに取り除かれます。すべてのプラットフォームは**デフォルトでこれを除外し**、開発ビルドであると自らを示すビルドに対してのみ追加します。

| プラットフォーム | 配線 | 含まれる条件 |
| --- | --- | --- |
| Android | `app-android` の `"devImplementation"(project(":feature:debug"))`（dev / prod のプロダクトフレーバー。dev は `.dev` という id サフィックスにより prod と並べてインストールされます） | dev フレーバーがビルドされたとき |
| Desktop | `app-desktop` の `jvmMain` への条件付き依存 | `run`、または Compose Hot Reload の run タスクのいずれかが、要求された Gradle タスクに含まれるとき |
| Web | `app-web` の `wasmJsMain` への条件付き依存 | `wasmJsBrowserDevelopmentRun` が要求された Gradle タスクに含まれるとき |
| iOS | `app-ios-kotlin` の `iosMain` への条件付き依存 | Xcode が `embedSwiftExportForXcode` に `CONFIGURATION=Debug` をエクスポートするとき |

`-PincludeDebugFeature=true|false` はプラットフォームのデフォルトをどちらの方向にも上書きします。デスクトップの配布物、Web のバンドル、Gradle 駆動の iOS コンパイルにデバッグ画面を入れる唯一の方法です — 例えば `./gradlew :app-ios-kotlin:compileKotlinIosSimulatorArm64 -PincludeDebugFeature=true` は、デバッグ機能を含んだ状態で iOS のグラフをコンパイルします。

Android はこのゲートをプロダクトフレーバーとして表現しており、迂回することはできません。他の 3 つのプラットフォームには、依存をぶら下げるためのそうしたバリアントがありません。webpack のモード、iOS の debug と release のビルド、Compose Desktop のリリース配布物は、それぞれ**1 つのコンパイル**を再利用するため、ゲートは代わりに要求されたタスクや Xcode のビルド構成を読まなければなりません。どちらのシグナルも網羅的ではないため、フォールバックは除外です — 認識されないビルドはデバッグ画面を出荷するのではなく失い、それは開発中にすぐ表面化します。`gradle-conventions` の `droidkaigi.includeDebugFeature` が、3 つすべてについてこのルールを保持しています。

除外されていることは、クラスパスを調べて検証します。例: `./gradlew :app-desktop:dependencies --configuration jvmRuntimeClasspath | grep debug`（何も出力されないことを期待します）。

関連: [モジュール構成](./project-structure.ja.md) · [デバッグ](./debugging.ja.md)
