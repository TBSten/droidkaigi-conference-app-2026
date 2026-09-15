> [!NOTE]
> 日本語話者向けに docs/ ディレクトリなどドキュメントが日本語訳されています。

![DroidKaigi 2026](assets/readme_header.png)

# DroidKaigi 2026 公式アプリ

[DroidKaigi](https://2026.droidkaigi.jp) は Android 開発者のためのカンファレンスで、今年で 12 回目を
迎えます。2026 年 9 月 1 日から 3 日までの 3 日間、東京のベルサール渋谷ガーデンで開催されます。

公式アプリは、参加するコミュニティ自身の手によってオープンに開発されています。**Android / iOS /
Desktop (JVM) / Web (wasmJs)** 向けの Compose Multiplatform アプリです。どなたでも開発に参加できます。
[コントリビューション](#コントリビューション) をご覧ください。

## 機能

DroidKaigi 2026 公式アプリは、カンファレンス体験をより良くするさまざまな機能を提供します。

- **タイムテーブル**: スケジュールを閲覧し、見たいセッションをブックマークできます。
- **イベントマップ**: 会場内での道順がわかります。
- **コントリビューター**: アプリを支えるコントリビューターを知ることができます。

...ほかにもあります！

## 試してみる！

### Web

**[droidkaigi.github.io/conference-app-2026](https://droidkaigi.github.io/conference-app-2026/)**

### Android

<a href="https://play.google.com/store/apps/details?id=io.github.droidkaigi.confsched2026"><img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="70" alt="Get it on Google Play"></a>

### iOS

<a href="https://apps.apple.com/app/id6801159161"><img src="https://toolbox.marketingtools.apple.com/api/v2/badges/download-on-the-app-store/black/en-us" height="48" alt="Download on the App Store"></a>

## コントリビューション

コントリビューションを歓迎します。

ステップバイステップのガイドは [CONTRIBUTING.md](CONTRIBUTING.md) をご覧ください。環境のセットアップから
プルリクエストの提出まで、ひととおり案内しています。

コントリビューションの詳細な手順については [CONTRIBUTING.ja.md](CONTRIBUTING.ja.md) をご覧ください。
初めての方でもわかりやすいステップバイステップのガイドを用意しています。

> [!NOTE]
> **今年から Issue のアサイン運用が変わりました。** できるだけ多くの方に参加の機会を持っていただくため、
> コントリビューター 1 人が同時に持てるオープンな Issue は **1 件** です。次の Issue に取りかかる前に、
> いま持っているものを完了させてください。動きのないアサイン済みの Issue にはリマインドが入り、その後も
> 静かなままであれば自動的にアサインが解除されます。Issue へのコメント、または Issue に紐づいた
> オープンなプルリクエスト（ドラフトも含みます）があればアサインは維持され、解除されたあとも
> いつでも再度お引き受けいただけます。
>
> **今年からIssueのアサイン運用が変わりました。**
> より多くの方に参加していただけるよう、一人が同時に持てるIssueは**1件まで**としています。
> 次のIssueに取りかかる前に、いま持っているものを完了させてください。
> アサインされたIssueに動きがない場合はリマインドのコメントが入り、その後も動きがなければ自動的にアサインが解除されます。
> Issueへのコメント、または紐づいたオープンなPull Request（ドラフトでも構いません）があれば、アサインは維持されます。
> 解除されたあとも、いつでも再度お引き受けいただけます。

## 必要なもの

- **Android Studio**: [このページ](https://developer.android.com/studio) から入手できる最新の安定版。
- **JDK 21** 以上。
- **Xcode**。iOS アプリのビルドと実行に必要です。

Gradle は wrapper から取得されるため、ほかにインストールするものはありません。

## デザイン

画面は Figma で設計されています。
**[DroidKaigi 2026 App UI](https://www.figma.com/design/tpllAs1pnsj03a9rimcFnp/DroidKaigi-2026-App-UI)** をご覧ください。

**デザイナー**: [@kitakkun](https://github.com/kitakkun), [@chihokotaro](https://x.com/chihokotaro)

## アーキテクチャ

今年はアーキテクチャと設計を詳細に文書化し、その全体を
**[droidkaigi.github.io/conference-app-2026/docs](https://droidkaigi.github.io/conference-app-2026/docs/)** で公開しています。

全体の構造は昨年から大きくは変わっていません。Compose Multiplatform がプラットフォーム間で UI を共有し、
[Metro](https://github.com/ZacSweers/metro) がコンパイル時に依存関係を解決し、
[Soil](https://github.com/soil-kt/soil) がデータレイヤーを支え、Navigation3 がバックスタックを所有します。
2025 年のアプリにコントリビュートした方には、見慣れた形でしょう。

モジュールがどう分かれているかは [モジュール構成](https://droidkaigi.github.io/conference-app-2026/docs/project-structure)
から、画面が端から端までどう動くかは
[アーキテクチャ概要](https://droidkaigi.github.io/conference-app-2026/docs/architecture-overview)
から読み始めてください。

各ページは [`docs/`](./docs/index.ja.md) にあります。`docs-site/` には、それらをローカルでプレビューする
ための VitePress の設定が入っています。

## 今年の挑戦

以下は 1 つの前提から始まります。**AI がこのコードの主要な書き手である** という前提です。そのうえで、
今年アプリが得たものへと話を進めます。

### コンパイラが形を保つアーキテクチャ

レビューはすべてのバグや、少しずつずれていく規約を捕まえきれません。そのためアーキテクチャは、それらを
より早い段階で弾くように作られています。**コンパイラがルールを判定できる場所では、ルールを破ると
ビルドが失敗します。**

Enforcement は、決まった優先順位で適用されます。

1. **型**。表現不可能にできるものに使います。context parameter とレシーバーは、宣言を呼び出せる
   スコープを狭めるため、誤った場所からの呼び出しは解決されません。この層はツールを必要とせず、
   コンパイラのアップグレードにも手を加えずに耐えます。
2. **Kotlin コンパイラフロントエンド (FIR) のチェッカー**。型システムでは表現できないものの、
   是か非かで答えが決まるルールに使います。
3. **レビューとテスト**。静的に判定できないものに使います。

`:tools:compiler-plugin` は、その 2 番目の層に 20 を超える FIR チェッカーを実装しています。コードを
読みやすく保つ規約、役割をそれぞれの層に閉じ込める境界、そして本来なら実行時にしか現れない API の誤用を
対象としており、そのいずれかを破るとコンパイルエラーになります。すべてのチェッカーは Kotlin コンパイラの
テストフレームワークでカバーされているため、ルール自体もほかのコードと同じようにテストされています。
各ルールと、それが弾くコードおよびその理由は
[Enforcement](./docs/enforcement.ja.md) にあります。

### 変更どうしを引き離しておく構造

AI の書き手はコードを素早く、しかもしばしば複数の箇所を同時に編集します。2 つの対策が、それらの編集の
衝突を防ぎ、それぞれをレビューできる大きさに保ちます。

**feature どうしは共有ファイルではなくインターフェースを介して出会います。** feature は単一メソッドの
インターフェースである `NavEntryProvider` を通じてナビゲーションエントリを提供し、その実装に
`@ContributesIntoSet` を付けます。Metro はコンパイル時にそれらの実装を 1 つのセットに集め、
`AppEntryProvider` はどの feature の名前も書かずにそのセットを走査します。各 feature の `NavKey`
シリアライザの登録は Kotlin Symbol Processing (KSP) が生成するため、こちらも中央での編集を必要としません。
画面の追加は既存ファイルの変更ではなくファイルの追加になり、feature どうしは互いに到達できません。
開発専用のツールを除けば Gradle のモジュールグラフに feature 間のエッジはないため、この分離は規約ではなく
ビルドによって強制されます。
[NavEntry の集約（NavEntryProvider）](./docs/navigation-entry-aggregation.ja.md) を参照してください。

**1 つの大きなファイルへと育ちようがない UI。** 2 つのチェッカーが feature の UI を複数のファイルに
分割された状態に保ちます。`ComposableNestingDepth` は content ラムダを 4 段までに制限するため、5 段目は
それ自体が 1 つの composable にならざるを得ません。さらに画面のファイルでは
`ScreenIsSoleComponentInFile` が、その composable を独立したファイルに置くことを要求します。2 つの
コンポーネントへの編集は 2 つのファイルに分かれるため、レビュアーは 1 つの大きな差分ではなく 2 つの
小さな差分を読むことになります。

`scripts/new-screen.sh` は新しい画面が必要とするファイル一式を書き出し、その出力は 4 つのターゲット
すべてでコンパイルが通り、チェッカーも満たします。
[AI 支援開発](./docs/ai-development.ja.md) では、AI の書き手が頼るツールを扱っています。

### 4 つのターゲット、1 つの共有アプリ

今年は Web (wasmJs) が 4 つ目のターゲットです。各プラットフォームが持つのは小さな terminal モジュール
だけです。Metro の依存グラフを構築し、共有の `KaigiApp` を起動するほかには何も持ちません。
何がどこに属するかは [プラットフォームとモジュール](./docs/platforms-and-modules.ja.md) が定めています。

Navigation3 は 4 つのターゲットすべてに行き渡りました。昨年、iOS は `navigation-compose` に
フォールバックしていました。[ナビゲーション概要](./docs/navigation.ja.md) を参照してください。

### context parameter が担う役割

各画面は Root・Presenter・Screen という 3 つの部分から構成され、双方向の `ScreenChannel` が `Action` を
Root から Presenter へ、`ActionResult` を逆向きに運びます。チャネルの各端には、対応する context parameter
がスコープにある場合にのみ到達できるため、誤った層から誤った端に到達しようとするとコンパイルエラーに
なります。[アーキテクチャ概要](./docs/architecture-overview.ja.md) を参照してください。

### デフォルトでオフラインファースト

`buildPersistedQueryKey` で構築されたクエリは、サーバーの生のレスポンスを保存し、起動時に復元します。
そのため画面はネットワークの応答を待たずにキャッシュから描画されます。コンパイラは `@Serializable` でない
永続化対象の型を弾くため、失敗は永続化が初めて実行されるときではなくビルド時に現れます。
[Soil の永続化](./docs/soil-persistence.ja.md) を参照してください。

### iOS: Liquid Glass タブバーを備えた Compose Multiplatform

iOS のすべての画面は Compose Multiplatform が描画します。唯一のネイティブな部分がルートタブバーです。
Compose のビューの上に重ねられた SwiftUI のビューで、その表面は iOS 26 ではシステムの Liquid Glass
マテリアルです。

2 つの実験的な仕組みが、実装を Kotlin 側にとどめています。**Swift Export** は、Swift から呼び出される
Kotlin に対して、Objective-C のヘッダーではなく Swift らしい Swift を生成します。`Flow` は
`AsyncSequence` として、Kotlin の enum は Swift の enum として届きます。**Swift Package Import** は
逆向きで、Kotlin が Swift を 1 行も書かずに Apple のフレームワークに到達できるようにします。Swift に
残っているのはタブバーとエントリポイントです。[iOS 概要](./docs/ios.ja.md) を参照してください。

## 商標

Google Play および Google Play ロゴは Google LLC の商標です。Apple および Apple ロゴは、米国およびその他の国で登録された Apple Inc. の商標です。
