# iOS のトップバー

iOS のトップバーは、ネイティブの `UINavigationBar` ではなく Compose Multiplatform — `:core:ui` の `KaigiTopAppBar` と `KaigiLargeTopAppBar` — によって描画されます。ルートタブバーは iOS で唯一のネイティブなサーフェスです。[Liquid Glass タブバー](./ios-liquid-glass.ja.md) を参照してください。このページでは、トップバーが Compose のままである理由と、この問いを再び開くことになる唯一の条件を述べます。

## タブバーの手法が通用しない理由

タブバーがネイティブなのは、その状態が小さく共有されているからです。`RootTab?` という 1 つの enum があり、すべての画面で同一で、`RootTabNavigator` を通して一度だけブリッジされます — [状態のブリッジ](./ios-liquid-glass.ja.md#状態のブリッジ) を参照してください。テーマが依然として決定する部分は、`RootTabBarAppearance` が publish する別個の `RootTabBarPalette`（アクセントカラーと、スキームがダークかどうか）としてタブバーに届き — [テーマのブリッジ](./ios-liquid-glass.ja.md#テーマのブリッジ) を参照してください — これもアプリ全体で 1 つの値です。トップバーの状態は画面ごとであり、小さくも一様でもありません。

## 画面ごとのトップバーの状態

本番の画面は現在これを描画しています。開発専用の `DebugScreen` と `SoilErrorsScreen` は Material3 の `TopAppBar` を直接使用しています。

| 画面 | バー | タイトル | 先頭のコントロール | アクション | 帯を共有するクローム |
| --- | --- | --- | --- | --- | --- |
| Timetable | `KaigiTopAppBar` | 静的 | なし | 検索（ナビゲーションする）、グリッド切り替え（presenter へ） | 日付ピッカー（`DayTabRow`） |
| Favorites、Event map | `KaigiTopAppBar` | 静的 | なし | なし | なし |
| About | `KaigiTopAppBar` | `UiState` から | なし | なし | なし |
| Contributors、Sponsors、Staff、Licenses | `KaigiLargeTopAppBar` | 静的 | 戻る | なし | なし。バーは `scrollBehavior` を受け取れますが、渡している画面はありません |
| Session detail | `KaigiTopAppBar` | 空 | 戻る、または閉じる | なし | 見出しがバーのサーフェスを引き継ぎます |

## ネイティブのバーが受け取らなければならないもの

ネイティブのトップバーは、タブバーのように 1 つの小さなモデル — タイトル、戻るフラグ、アクションのリスト — として表現することはできません。

- **アクションはフラグではなくサブモデルです。** 各アクションは、Material の `ImageVector` が Swift Export を越えられないアイコンを持ち — タブバーはすでに、Swift 側で各遷移先を SF Symbol の名前にマッピングすることでこれに対処しています — さらに content description、有効状態、画面ごとの処理を行うコールバックを持ちます。各アクションは逆方向の呼び出しチャネルを備えた独自のブリッジされた値であり、その集合は 1 つのコンポーネントを封じ込めるのではなく画面グラフに追従します。
- **先頭のコントロールは真偽値ではありません。** なし、戻る、閉じるのいずれかであり、実行時の状態によって変わります。セッション詳細は、リスト-詳細のペインでは閉じるコントロールを、それ以外では戻る矢印を表示します。
- **タイトルは動的になり得ます。** About は自身の `UiState` からバーのタイトルを読み取ります。
- **帯は手描きのクロームと共有されています。** Timetable の日付ピッカーはバーのサーフェス上に置かれ、リストがスクロールするとバーの背後へ折り畳まれ、セッション詳細の見出しはそれを引き継ぎます。ネイティブのバーは自身のマテリアルを描画するため、この継ぎ目はサーフェスの途中で分断されてしまいます。

## スクロール駆動の挙動

ネイティブのバーを正当化するであろう挙動 — ラージタイトルの折り畳みと scroll edge effect — は、システムが所有する `UIScrollView` を読み取ります。ここでのコンテンツは Compose であり、UIKit からは見えないため、ネイティブのバーは静的なままになります。これは、タブバーにおいて `UITabBarController.tabBarMinimizeBehavior` を機能しないままにしているのと同じ制約であり、[タブごとの埋め込み](./ios-liquid-glass.ja.md#代替案-タブごとに-1-つの-compose-インスタンス) についても同様に当てはまります。

## 決定

トップバーは Compose のままとします。その状態は画面ごとであり画面グラフとともに増えていくため、これをブリッジすると相互運用のサーフェスは 1 つのコンポーネントを囲む継ぎ目から画面の鏡へと変わってしまいます。また、ネイティブのバーがもたらす挙動は Compose のコンテンツの上では機能しません。さらに、トップバーをシステムに委ねると、手描きの `SketchShape` の言語が、タブバーによってすでに分断されている以上にさらに分断されます。

## 再検討すべきタイミング

これは、iOS がタブごとのスタックへ移行した場合に変わります。その経路は [タブごとに 1 つの Compose インスタンス](./ios-liquid-glass.ja.md#代替案-タブごとに-1-つの-compose-インスタンス) でコストが見積もられ、[ルートタブバー（RootTabSceneDecorator）](./navigation-root-tab-bar.ja.md) のタブ切り替えのセマンティクスと合わせて結論づけられています。各タブのスタックを所有する `UINavigationController` は副次的な効果としてトップバーを描画するため、問いは、そのコントローラーにどのバーのコンテンツを渡すかになり、ブリッジをゼロから構築するかどうかではなくなります。この決定はそこに属し、それより前ではありません。

関連: [iOS 概要](./ios.ja.md) · [Liquid Glass タブバー](./ios-liquid-glass.ja.md) · [ルートタブバー（RootTabSceneDecorator）](./navigation-root-tab-bar.ja.md) · [Swift ↔ Kotlin の相互運用](./ios-interop.ja.md)
