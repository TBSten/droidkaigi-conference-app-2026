# ナビゲーション概要

ナビゲーションは Navigation3 の上に構築されており、**デスティネーションの追加が中心部の編集を伴わないこと、フィーチャー同士が依存しないこと、画面の `NavEntry` と `NavKey` シリアライザが未登録のままにならないこと**を目指しています。

設計は関心事ごとに分割されています。

- [Navigator](./navigation-navigator.ja.md) — 各フィーチャーの外向きのナビゲーションを型安全なメソッドとして表現し、コマンドは 1 箇所でバックスタックに適用されます
- [NavEntry の集約（NavEntryProvider）](./navigation-entry-aggregation.ja.md) — 各フィーチャーが自身の `NavEntryProvider` を提供し、`NavDisplay` はマージされた結果を読み取ります
- [NavKey シリアライザの集約（NavKeySerializersProvider）](./navigation-navkey-serializers.ja.md) — バックスタックの永続化。フィーチャーごとの登録は KSP が生成します
- [エントリの保持（RetainNavEntryDecorator）](./navigation-retain-entry-decorator.ja.md) — 画面のグラフを、その `NavEntry` が生存している間だけ正確に保ちます
- [ルート NavEntry のエミュレーション（RootSceneStrategy）](./navigation-predictive-back-tabs.ja.md) — 他のタブが下にスタッシュされていても、ホームルートからは back でアプリを終了します
- [ルートタブバー（RootTabSceneDecorator）](./navigation-root-tab-bar.ja.md) — `RootTabSceneDecorator` によるボトムバーとタブ切り替え
- [リスト-詳細シーン（ListDetailSceneStrategy）](./navigation-list-detail.ja.md) — 大きなウィンドウでの `ListDetailSceneStrategy`。詳細ペインの back アイコンは `CompositionLocal` を通じて適応します
- [ディープリンク（DeepLinkEffect）](./navigation-deep-links.ja.md) — 外部からのナビゲーション要求（ウィジェットのタップ、URL）を `DeepLinkStore` にバッファリングし、コールドスタート時には合成したバックスタックとともに適用します

関連: [画面の実装](./building-a-screen.ja.md) · [ScreenContext の設計](./screen-context.ja.md) · [Enforcement](./enforcement.ja.md)
