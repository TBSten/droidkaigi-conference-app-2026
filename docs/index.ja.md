# DroidKaigi/conference-app-2026 アーキテクチャドキュメント

DroidKaigi 2026 カンファレンスアプリのアーキテクチャと実装方針を扱うドキュメント群です。

## ドキュメントマップ

### プロジェクト構成

- [モジュール構成](./project-structure.ja.md) … `:core:*` / `:feature:*` / `:app-*` / ツールのモジュールグループと、それぞれに含まれるもの
- [プラットフォームとモジュール](./platforms-and-modules.ja.md) … 4 つのプラットフォームと、各モジュールに属するもの

### アーキテクチャ

- [アーキテクチャ概要](./architecture-overview.ja.md) … プラットフォームのエントリポイントから、描画され操作可能になった画面まで、アプリが端から端までどのように動くか
- [エラーハンドリング](./error-handling.ja.md) … 2 層のエラーモデルと、一度きりのイベント（ナビゲーション、メッセージ）が Soil 由来のエフェクトと ScreenChannel を通って流れる仕組み
- [Presenter のパフォーマンス](./presenter-performance.ja.md) … 重い計算をデータ層へ押し下げることによる責務の分割
- [Enforcement](./enforcement.ja.md) … 型と FIR checker によって不正なコードをコンパイル不能にする
- [命名レビュー](./naming-review.ja.md) … コンパイラでは担保できないところでレビュアーが適用する命名ルール。名前は値が何であるかを述べ、型はそれがどう表現されるかを述べる
- [CompositionLocal レビュー](./compositionlocal-review.ja.md) … 値が `CompositionLocal` に属するかどうかを決める唯一の問いと、答えが no のときに通り抜ける継ぎ目

### 単一画面の構造

- [画面の実装](./building-a-screen.ja.md) … TimetableScreen を例に 1 つの画面を端から端まで実装する（手順とチェックリスト）
- [ScreenContext の設計](./screen-context.ja.md) … 具象クラス + retain、ロールコンテキストの分離（合成、ケイパビリティのゲーティング）
### 依存性注入

- [AppGraph と UiGraph](./di-app-graph.ja.md) … プロセススコープと UI スコープの Metro グラフ
- [画面ごとのグラフ（@GraphExtension）](./di-screen-graph.ja.md) … 画面スコープの拡張グラフとファクトリの結線

### ナビゲーション

- [ナビゲーション概要](./navigation.ja.md) … Navigation3 上のナビゲーションアーキテクチャ
- [Navigator](./navigation-navigator.ja.md) … 画面ごとの Navigator（手書き）と、1 箇所でバックスタックに適用されるコマンド
- [NavEntry の集約（NavEntryProvider）](./navigation-entry-aggregation.ja.md) … 中央を編集することなく行う `@ContributesIntoSet` によるエントリの集約
- [NavKey シリアライザの集約（NavKeySerializersProvider）](./navigation-navkey-serializers.ja.md) … KSP が生成する多相シリアライザの集約
- [エントリの保持（RetainNavEntryDecorator）](./navigation-retain-entry-decorator.ja.md) … ナビゲーションをまたいで画面グラフを保持する
- [ルート NavEntry のエミュレーション（RootSceneStrategy）](./navigation-predictive-back-tabs.ja.md) … ホームルートにおけるバックスタックの終了挙動
- [ルートタブバー（RootTabSceneDecorator）](./navigation-root-tab-bar.ja.md) … ウィンドウサイズをまたいだボトムバーとナビゲーションレール
- [リスト-詳細シーン（ListDetailSceneStrategy）](./navigation-list-detail.ja.md) … expanded ウィンドウにおける 2 ペインのアダプティブレイアウト
- [ディープリンク（DeepLinkEffect）](./navigation-deep-links.ja.md) … 外部リンクとインテントのバッファリングとディスパッチ

### Soil（データ層）

- [Soil のキー](./soil-keys.ja.md) … query、subscription、mutation のキーの契約と実装
- [SoilDataBoundary](./soil-data-boundary.ja.md) … 画面ルートにおけるローディングとエラーのバウンダリ
- [Soil のミューテーション](./soil-mutation.ja.md) … `mutateAsync` + `MutationSuccessEffect` と失敗時の処理
- [Soil の永続化](./soil-persistence.ja.md) … `buildPersistedQueryKey` によるオフラインファーストのキャッシュ永続化

### 通知とリマインダー

- [セッションリマインダー](./session-reminders.ja.md) … 共有の「まもなく開始」の計算と、それを実行する Android / iOS のスケジューラ

### ビルド

- [バージョンカタログ](./build-version-catalog.ja.md) … 依存とバージョンの一元管理
- [Convention プラグイン](./build-convention-plugins.ja.md) … `gradle-conventions` 内の共有ビルドロジック
- [BuildKonfig（ビルド時の値）](./build-config-buildkonfig.ja.md) … ビルド時の値（バージョンやその他のビルド状態）を単一のソースから common コードへ公開する
- [開発専用コードをリリースから除外する](./build-dev-only-exclusion.ja.md) … 開発ツールのコンパイル時除外
- [worktree 間での SwiftPM インポートキャッシュ](./build-worktree-swiftpm-cache.ja.md) … 並行する worktree 間で SwiftPM のキャッシュを共有する

### iOS 統合

- [iOS 概要](./ios.ja.md) … タブバーだけが Liquid Glass で、ほぼ全面が CMP
- [Liquid Glass タブバー](./ios-liquid-glass.ja.md) … iOS 26 における Liquid Glass マテリアルのネイティブ `UITabBar`
- [iOS のトップバー](./ios-top-bar.ja.md) … タブバーがネイティブである一方で、トップバーが Compose のままである理由
- [Swift ↔ Kotlin の相互運用](./ios-interop.ja.md) … Swift Export と Swift Package Import による相互運用
- [iOS 上の CMP（埋め込み）](./ios-cmp-embedding.ja.md) … iOS シェルの内側への Compose Multiplatform の埋め込み

### テスト

- [テスト概要](./testing.ja.md) … 3 層のテスト戦略
- [テストグラフ（TestingScope）](./testing-graph.ja.md) … テスト用の DI グラフと共有のテストスキャフォールディング
- [Presenter のユニットテスト（Molecule）](./testing-presenter.ja.md) … Molecule と Turbine による純粋ロジックの presenter テスト
- [プレビュースクリーンショットテスト](./testing-preview-screenshot.ja.md) … Roborazzi によるビジュアルリグレッションテスト
- [Robot パターンテスト](./testing-robot.ja.md) … Compose UI 上の BDD シナリオテスト
- [Enforcement チェッカーのテスト](./testing-enforcement.ja.md) … コンパイラプラグインの診断テスト

### プレビュー

- [プレビューとサンプルアセット](./preview.ja.md) … IDE プレビューとマルチテーマ / マルチロケール対応
- [プレビュー画像 enum の生成](./preview-image-enum.ja.md) … アセットから生成される型安全なプレビュー画像ハンドル
- [ローカライズ](./localization.ja.md) … Compose Resources の文字列と多言語テキストの解決

### ロギングとデバッグ

- [ロギング（Kermit）](./logging.ja.md) … プラットフォームごとに KMP ネイティブな writer を持つ、単一の AppScope Kermit `Logger`
- [Clock（KaigiClock）](./clock.ja.md) … 注入される唯一の時刻の継ぎ目。本番、開発時のオフセット、テストにおける fake clock
- [デバッグ](./debugging.ja.md) … 開発専用のデバッグ画面と JetWhale 連携

### AI 支援開発

- [AI 支援開発](./ai-development.ja.md) … コンパイラのガードレール、スキャフォールディングスクリプト、AI ワークフロー
