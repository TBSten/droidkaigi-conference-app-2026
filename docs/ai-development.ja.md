# AI 支援開発

このプロジェクトは **AI がコードの主要な書き手であること**を前提としています ([Enforcement](./enforcement.ja.md) を参照)。アーキテクチャは、レビューに頼るのではなく、誤ったコードがコンパイルに失敗するように設計されています。そうしたガードレールの上に、このリポジトリは AI エージェント (と人間) が直接使うツールを同梱しています。

## 新しい画面のスキャフォールド

`scripts/new-screen.sh` は、画面に必要なすべてのファイルをモジュールをまたいで一度に生成します。

```sh
scripts/new-screen.sh --feature sponsors --screen Sponsors
```

| 場所 | 生成されるもの |
| --- | --- |
| `:core:model` | `<Screen>ScreenScope` (スコープマーカー) |
| `feature/<f>` | [`ScreenContext`](./screen-context.ja.md)/`PresenterContext`、`@GraphExtension` のグラフ、`Action` / `ActionResult` / `UiState` (それぞれ 1 ファイル)、Presenter、Screen、ScreenRoot、`NavKey`、`NavEntryProvider`、`ScreenNavigator` |
| `app-shared` | `Default<Screen>ScreenNavigator` |

feature モジュールが存在しない場合は、`--create-module` を渡すとモジュール自体 (ビルドファイル、`settings.gradle.kts` の include、`app-shared` の依存) をスキャフォールドします。このフラグがなければモジュールがないことはエラーになるので、`--feature` のタイプミスが黙ってモジュールを作ってしまうことはありません。既存のファイルが上書きされることは決してなく、スクリプトは作成したものの要約を出力します。生成されたコードは 4 つのターゲットすべてでコンパイルでき、すべての FIR チェッカーを通過します。

Claude Code 向けには、**`new-screen` skill** (`.claude/skills/new-screen`) がこのスクリプトをラップしています。Claude に「sponsors 画面を追加して」と頼めば、それを実行し、ビルドを検証し、要約を報告します。

## AI が頼るガードレール

- **FIR チェッカー** (`:tools:compiler-plugin`) はアーキテクチャのルールをコンパイルエラーに変えます — ロールコンテキストのゲーティング、直接の `mutate` の禁止、Navigator の封じ込め、Soil の読み取りの封じ込め、mutation tag の分離、プレビューのラッパー。全体像は [Enforcement](./enforcement.ja.md) にあります。プラグイン自体の開発と保守については、[kotlin-compiler-plugin-skills](https://github.com/kitakkun/kotlin-compiler-plugin-skills) のエージェント skill が、出典付きのリファレンスとともに FIR/IR の拡張作業を扱います。
- **コード生成は、AI が微妙に間違えがちなボイラープレートを取り除きます**: [NavKey シリアライザの集約（NavKeySerializersProvider）](./navigation-navkey-serializers.ja.md)、Soil の key の id (`SoilIds`)、JVM 以外向けのプレビューレジストリ — いずれも KSP 生成 (`:tools:ksp-processor`) です。
- **AI レビュア向けのレビュールール**は、同じアーキテクチャ上の決定を検出可能なレビュー基準として符号化します — [命名レビュー](./naming-review.ja.md) もその 1 つで、これはドメインの語彙に照らして判断されるためチェッカーには決められません。

## 並行する working tree

エージェントはそれぞれ自分の `git worktree` で作業するため、複数のチェックアウトが並存します。そうでなければ各チェックアウトが Swift Package Manager のインポート — おおよそ 3 GB と、同期のほとんど — を繰り返すことになります。その結果が Gradle のビルドキャッシュにも次の working tree にも届かないからです。`scripts/link-swiftpm-cache.sh --install-hook` を一度実行しておけば、そのクローンから追加されるすべての working tree が単一のストアを共有します。詳細は [worktree 間での SwiftPM インポートキャッシュ](./build-worktree-swiftpm-cache.ja.md) を参照してください。

## 変更の検証

- Presenter のロジック: `runPresenterTest` ハーネス (`:core:testing`) — [Presenter のユニットテスト（Molecule）](./testing-presenter.ja.md) を参照してください。
- 画面の振る舞い: Robot の BDD レイヤ — [Robot パターンテスト](./testing-robot.ja.md) を参照してください。
- 描画: Roborazzi のプレビュースクリーンショット — [プレビュースクリーンショットテスト](./testing-preview-screenshot.ja.md) を参照してください。
- マルチターゲットの全体チェック: `./gradlew :app-desktop:compileKotlinJvm :app-web:compileKotlinWasmJs :app-android:compileDevDebugKotlin :app-ios-kotlin:compileKotlinIosSimulatorArm64 :feature:sessions:jvmTest`。このうち iOS 側は Kotlin をコンパイルするだけです。Swift Export は Xcode から実行され、それは iOS のビルドワークフローが実施します。

関連: [Enforcement](./enforcement.ja.md) · [画面の実装](./building-a-screen.ja.md)
