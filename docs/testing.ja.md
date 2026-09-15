# テスト概要

3 つのレイヤーがあり、それぞれ異なる範囲をカバーします。

- [Presenter のユニットテスト（Molecule）](./testing-presenter.ja.md) — presenter はイベントとデータを `UiState` に変換する `@Composable` なので、Molecule によって純粋なロジックとしてテストします（UI なし、高速）。
- [プレビュースクリーンショットテスト](./testing-preview-screenshot.ja.md) — すべての `@Preview`、および Robot のシナリオが到達するすべての状態をレンダリングし、Roborazzi でゴールデン画像と比較します。
- [Robot パターンテスト](./testing-robot.ja.md) — Compose UI test 上の BDD（behavior-driven development、振る舞い駆動開発）スタイルの DSL による、画面のエンドツーエンドの振る舞い。

Presenter テストと Robot テストは [テストグラフ（TestingScope）](./testing-graph.ja.md) を通じて配線を共有します。画面ごとに 1 つの `TestingScope` グラフが、`:core:testing` で contribute された fake からコンテキストを解決します。

コンパイル時のルールは別途テストします。[Enforcement チェッカーのテスト](./testing-enforcement.ja.md) は、プラグインをロードした状態で Kotlin ソースをコンパイルし、各 FIR checker が報告する diagnostics をアサートします。
