# Liquid Glass タブバー

iOS でネイティブな UI はルートのタブバーだけです。システム自身のバーである `UITabBar` で、iOS 26 では Liquid Glass を描画します。その背後にあるすべての画面は Compose Multiplatform が描きます。ナビゲーションのロジック (バックスタック) はどのプラットフォームでも Navigation3 が所有し、iOS はその状態のうちタブに関わる部分をネイティブのバーへミラーします。

バーは再実装ではなくシステムのコンポーネントなので、そのマテリアル、メトリクス、選択アニメーション、アクセシビリティの特性は UIKit のものです。OS のバージョンごとにバーの描き方は異なり — iOS 26 はフローティングのガラスのプラター、それ以前は標準のバー — そのためアプリは可用性の分岐も、独自のフォールバックのマテリアルも持ちません。

## オーバーレイによる埋め込み

バーはクロームにすぎません。`UITabBar` は自身のサイズを決めるため、ビューはバーの領域だけを占め、それ以上は占めません。全画面の `ComposeUIViewController` の上に下揃えで重ねられます。

```swift
ZStack(alignment: .bottom) {
    KaigiAppView(host: host)                            // full-screen ComposeUIViewController
    RootTabBarView(                                     // only the bar's own area
        currentTab: host.currentTab.asAsyncSequence().map { $0?.tab },
        palette: host.tabBarPalette.asAsyncSequence(),
        select: host.selectTab(tab:)
    )
}
.ignoresSafeArea()
```

これが、埋め込みをコンテナの配管から自由に保っているものです。`hitTest` のオーバーライドも、背景のクリアも、バーを囲むビューコントローラもありません。バーの内側のタップは `UITabBar` が受け取り、Compose のレイヤには決して届きません。その外側のすべての点 — プラターの脇の余白と、その下の帯も含めて — は Compose のものです。詳細画面がバーを隠すときには、その領域も Compose に戻ります。

Compose のビューコントローラはバーの親ではなく兄弟なので、下部のインセットを継承しません。ルートの遷移先は自分で余地を確保し、スクロール可能なものは下部のコンテンツパディングに `KaigiNavigationBarDefaults.occupiedHeight` を加えます。これが UIKit がバーに与える高さをカバーします。

スクロールに連動するバーの挙動は利用できません。Compose のスクロールは UIKit からは見えないため、`UITabBarController.tabBarMinimizeBehavior` は観測するものがなく、バーは完全に表示されたままです。ガラス自体は、背後をスクロールする Compose のコンテンツを引き続き屈折させ、色付けします。

`TabView` はこの役割を担えません。SwiftUI の `TabView` は全画面のコンテンツをホストすることを譲りません。Compose のレイヤの上に不透明な背景を塗り、バーの外側のタッチをすべて要求します。そのコンテンツに `allowsHitTesting(false)` を付けても、素通りは復活しません。代わりに `UITabBarController` を重ねることはできますが、それはバーのサブツリーに当たったときはヒットを返し、それ以外では `nil` を返す `hitTest` を持つコンテナビューの内側でのみ可能です。それは `tabBarMinimizeBehavior` と `bottomAccessory` をもたらしますが、どちらもここでは当てはまらず、代償としてそのコンテナとタブごとのプレースホルダのビューコントローラが必要になります。

## 状態のブリッジ

`RootTab` と `RootTabNavigator` (`:app-shared`、UI の型を含みません) が、両側で共有されるモデルを形づくります。

- **Kotlin → Swift**: `RootTabNavigator.currentTab: StateFlow<RootTab?>` がバーの選択状態を駆動します。`null` (タブではないエントリが最前面にある、つまり詳細画面) はバーを隠します。Swift からは `KaigiAppHost.currentTab: Flow<RootTabSelection?>` として到達します。enum がクラスにラップされているのは、[Swift Export が enum を `Flow` に載せて運べない](./ios-interop.ja.md)ためです。
- **Swift → Kotlin**: タブのタップは `select(tab)` を呼び、`IosTabBarSyncEffect` (`KaigiApp` の内側) が各選択を `AppNavigator.selectTab(tab.key)` に変換します — 他のプラットフォームで Compose のバーが発行するのと同じコマンドです。

`RootTab.label` が両側で各遷移先に名前を与えます。Compose のバーはそれをアイコンのコンテンツ説明として与え、ネイティブのバーはアイコンの下にタブのタイトルとして表示します。アイコンには共有の形がありません — Compose は遷移先を Material の `ImageVector` で、UIKit は SF Symbol で名指しします — そのためシンボル名は Swift 側のバーにあります。

## テーマのブリッジ

バーは自分のマテリアルを描くので、テーマはバーがなお決める僅かなものとして届きます。`RootTabBarAppearance` は `RootTabBarPalette` を公開します — テーマのアクセントを sRGB ARGB で、そして有効なスキームがダークかどうかを — バーはそれを `tintColor` と `overrideUserInterfaceStyle` として受け取ります。このスタイルの上書きこそが、バーをデバイスではなくアプリに合わせ続けるものです。アプリは自分でスキームを選び、5 つのうち 2 つはダークです。

パレットがそれ以上を運ばないのは、それ以上が効果を持たないからです。iOS 26 のプラターでは、`unselectedItemTintColor` と `UITabBarAppearance.selectionIndicatorTintColor` はマテリアルに上書きされます。

`RootTabSceneDecorator` (Compose のボトムバー) は iOS では適用されません。ネイティブのバーがそれを置き換えます。`rememberRootTabSceneDecorator` は `currentPlatform` が `TargetPlatform.Ios` のとき `null` を返します。タブ切り替えのセマンティクスについては [ルートタブバー（RootTabSceneDecorator）](./navigation-root-tab-bar.ja.md) を参照してください。

## レンダラをまたぐコンポジット

Liquid Glass のバーは、背後にある CMP (Skia/Metal) のバックドロップを屈折させ色付けします。ガラスは生きている Compose の Metal レイヤをサンプリングするので、コンテンツがスクロールするとガラスは下にある CMP の色を追跡します。このコンポジットには iOS 26 が必要です。

![iOS 26 の Liquid Glass が、トップバーと下部のタブカプセルの背後にある CMP (Skia/Metal) のコンテンツを屈折させ色付けしている様子](./images/ios-liquid-glass-cmp-backdrop.png)

トップバーは背後の赤いカードを拾って赤く色付けされ、下部に浮かぶタブカプセルは背後のテキストをガラス越しに屈折させます。iOS 26.2 のシミュレータ、ライトモードで撮影したものです。

CMP は `Info.plist` に `CADisableMinimumFrameDurationOnPhone=true` を必要とし、なければ起動時に `PlistSanityCheck` で異常終了します。

## 代替案: タブごとに 1 つの Compose インスタンス

別の埋め込み方として、オーバーレイの代わりに `UITabBarController` の各タブへ本物のコンテンツとして専用の `ComposeUIViewController` を与える方法があります。これはネイティブのタブ切り替えトランジションとセーフエリアの自動伝播をもたらしますが、次を必要とします。

- **タブごとのバックスタック。** 単一の `NavBackStack` がタブごとのスタックに分かれます。プラットフォーム共通のタブ切り替えモデルである `AppNavigator.moveToTop` の並べ替えは iOS では当てはまらなくなり、navigator のコマンドは選択中のタブのスタックへルーティングされなければなりません。
- **タブごとの状態の配管。** バックスタックの永続化、`RetainNavEntryDecorator` のスコープ、スナックバーとオーバーレイのホストがスタックの数だけ増え、戻るのセマンティクスは他のプラットフォームと乖離します。戻るは退避されたタブへ落ちていかなくなるため、[`RootSceneStrategy`](./navigation-predictive-back-tabs.ja.md) のモデルは引き継がれません。
- **ディープリンクのルーティング。** ディープリンクはまずタブに解決され、それからそのタブのスタックに push されます。

この埋め込みでもスクロールに連動するバーの挙動は利用できないままです — 各タブの中身は依然としてネイティブの `UIScrollView` ではなく Compose だからです。オーバーレイによる埋め込みが既定であるのは、ナビゲーションのモデルをプラットフォーム間で同一に保ち、共有のナビゲーションコードに変更を必要としないからです。

関連: [iOS 概要](./ios.ja.md) · [iOS のトップバー](./ios-top-bar.ja.md) · [ルートタブバー（RootTabSceneDecorator）](./navigation-root-tab-bar.ja.md) · [ナビゲーション概要](./navigation.ja.md)
