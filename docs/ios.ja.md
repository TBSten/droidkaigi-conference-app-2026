# iOS 概要

iOS はすべての画面で共有の Compose Multiplatform UI を実行しますが、1 つだけネイティブの例外があります。ルートタブバー、すなわち Liquid Glass デザインを描画するシステムの `UITabBar` です。トップバーは Compose のままで、その理由は [iOS のトップバー](./ios-top-bar.ja.md) に記録されています。

- Swift の実装は最小限で、アプリは Compose Multiplatform を基盤に動作します。`KaigiApp` は `ComposeUIViewController` 上で動き、すべての画面が共有の CMP UI を使います。KMP の Presenter 連携を伴う画面ごとの SwiftUI は継続しません。
- 唯一のネイティブの例外はルートタブバーで、そのビューコントローラの上に重ねられます。すべての画面 — 画面遷移のクロームも含めて — は CMP が描画します。Navigation3 が全プラットフォームでバックスタックを所有し、iOS はその状態のうちタブに関する部分をネイティブのバーへミラーします。

## ナビゲーションとの関係

Navigation3 が 4 つのプラットフォームすべてでバックスタックを所有します。iOS はその状態を `RootTabNavigator` を通じてネイティブのタブバーに反映します。これは `:app-shared` にある UI の型を含まないモデルです。Kotlin が現在のタブを publish し（`null` はバーを隠します）、ネイティブのタブタップは選択として戻ってきて、他のプラットフォームで Compose のバーが発行するのと同じバックスタックのコマンドになります。

## Swift ↔ Kotlin の相互運用

Swift ↔ Kotlin の境界はタブバーの周辺で小さく保たれています。Swift は Swift Export された `AppShared` モジュールを通じて Kotlin を呼び出し（タブの選択、バーが自身を描画するために使うテーマ、ビューコントローラのファクトリ）、Kotlin は Swift Package Import を通じて Apple のフレームワークに到達します。どちらも 2026 年時点で実験的です。

詳細は [Swift ↔ Kotlin の相互運用](./ios-interop.ja.md) を参照してください。

## ターゲット

iOS は iosArm64 + iosSimulatorArm64 をターゲットとします。エクスポートされる `AppShared` モジュールは Metro、Soil、Navigation3 の両グループ、そして runtime-retain をネイティブの klib としてリンクし、画面は完全に `commonMain` に留まります。iosX64（Intel シミュレータ）は対象外です。CMP の `compose.ui`、`runtime-retain`、Navigation3 のグループが、非推奨となったこのターゲット向けに公開されていないためです — これは `xcodebuild` が `generic/platform=iOS Simulator` という destination ではなく具体的な arm64 シミュレータを指定しなければならない理由でもあります。

ネイティブの Liquid Glass タブバーは、iOS 26 では CMP のバックドロップの上に合成されます。埋め込みの形については [iOS 上の CMP（埋め込み）](./ios-cmp-embedding.ja.md) を、タブバーについては [Liquid Glass タブバー](./ios-liquid-glass.ja.md) を参照してください。

関連: [iOS 上の CMP（埋め込み）](./ios-cmp-embedding.ja.md) · [Liquid Glass タブバー](./ios-liquid-glass.ja.md) · [iOS のトップバー](./ios-top-bar.ja.md) · [Swift ↔ Kotlin の相互運用](./ios-interop.ja.md) · [iOS のお気に入りウィジェット](./ios-favorites-widget.ja.md)
