# iOS のお気に入りウィジェット

ホーム画面のウィジェットは `FavoritesWidget` という WidgetKit の extension で、エクスポートされた Kotlin を一切リンクしません。アプリは、ウィジェットが描画するすべての内容のスナップショットを共有の App Group コンテナに書き込み、extension はそのファイルだけを元に描画します。Android のウィジェットがデザインの基準です。どちらも同じルールから同じ状態を計算します。

## スナップショットの契約

アプリは App Group コンテナに `favorites-widget-snapshot.json` を書き込みます。writer は `FavoritesWidgetSnapshot.kt` と `FavoritesWidgetSnapshotStore.kt` (`:app-ios-kotlin`) が持ち、reader は `FavoritesWidgetSnapshot.swift` が持ちます。書き込みは一旦別の場所に用意されてからアトミックに所定の位置へ移動されます。ウィジェットのリフレッシュが書き込みの途中に発生し得るためです。

| フィールド | 内容 |
| --- | --- |
| `schemaVersion` | 形式のバージョン。これ以外の値を持つファイルは破棄されます |
| `clockOffsetSeconds` | デバッグ用の clock オフセット。ウィジェットがアプリと同じ時点を描画するためのものです |
| `conference` | カンファレンスの期間 (epoch 秒)、その UTC オフセット、2 日分の日付、および各日の最後のセッションの終了時刻 |
| `colors` | 選択されたカラースキームから解決された `surface`、`onSurface`、`onSurfaceVariant`、`primary`、`onPrimary` を `#AARRGGBB` として、加えて `isDark` |
| `favorites` | 両日のお気に入り登録されたすべてのセッション: id、カンファレンス日、両言語のタイトル、壁時計時刻と epoch による開始・終了、そして room の chip ラベルと色 |

extension が描画するすべてのものは、このファイルを通じて extension に届かなければなりません。extension は、色も room のテーマもロケール依存のタイトルも共有コードからは解決しません — 自身の string catalog にある文言だけを保持します。

スナップショットはそれ単体で描画可能な状態を保たなければなりません: extension が必要とするフィールドは writer と reader に同時に追加し、古い形式に対して書かれた reader がファイルを誤読することになる場合は常に `schemaVersion` を上げます。

## App Group とスキーマバージョン

`app-ios/project.yml` が App Group の識別子とスキーマバージョンの唯一の情報源です。このやり取りに参加するすべてのバンドル — アプリ、extension、テストバンドル — に両方を刻み込み、同じノードを両ターゲットの生成された entitlements にエイリアスします。`FavoritesWidgetContract` が実行中のバンドルからそれらを読み戻し、アプリは `KaigiAppHost` を通じて Kotlin に渡します。どちらの値も、他のどこにもリテラルとして書いてはいけません。

したがって、スキーマバージョンを上げる作業は 1 箇所の編集で済みます。アプリと extension は一緒に出荷されるため、不一致は以前のインストールが残したファイルを意味するほかなく、それはまさに reader が拒否するものです。

## タイムラインと境界

`FavoritesWidgetState.swift` は、`core/model/.../FavoritesWidgetState.kt` から `computeFavoritesWidgetState`、`nextFavoritesWidgetBoundary`、`toFavoritesWidgetRows` を移植したものです。この 2 つは歩調を合わせ続けなければなりません。`FavoritesWidgetStateTests` は fixture のスナップショット上で Kotlin のテストケースを写し取っています。

状態は、カウントダウン、イベント当日、続いてカンファレンス日ごとの状態 — Day 1 のプログラムが終わっていれば day wrap-up、そうでなければ schedule、today done、または empty — そしてカンファレンス終了後です。ある日のプログラムは、その日の最後のセッションが終了した時点で終わりであり、それは conference ブロックの日ごとのセッション終了時刻が示します。タイムテーブルの項目がない日は、その終了時刻が不明なままで、決して終わりません。現在の日について状態が必要とするすべて — どのお気に入りがその日に該当するか、もう一方の日にいくつ該当するか — は、各お気に入り自身の日から得られます。extension はタイムテーブルを一度も見ないからです。

境界とは、新しい入力なしに状態が変化する、現在より後で最も早い時点です: イベント当日より前であれば次のカンファレンスの深夜 0 時、イベント当日であれば Day 1 の深夜 0 時、そして当日の間は、その日のお気に入りの開始時刻と終了時刻、その日の最後のセッションの終了時刻、その日自身の深夜 0 時、およびカンファレンスの終了時刻のうち最も早いものです。カンファレンス後に境界はありません。

`FavoritesWidgetProvider` はそれらの境界をたどり、各境界で 1 つの entry を出力します。WidgetKit に無限のタイムラインが渡されないよう上限が設けられています。上限に達したときは reload ポリシーが最後の entry からスケジュールを再開し、そうでなければ `never` です。entry の日付はシステム時刻である一方、状態はスナップショット自身の clock で計算され、この 2 つをデバッグオフセットが分けています。

## リフレッシュ

アプリは `KaigiAppHost.favoritesWidgetSnapshots` を収集します。これはお気に入り、カラースキーム、clock オフセットにまたがる flow です。各 emission は完了した書き込みの後に続くため、それが引き起こす reload は常に、extension がすでに読めるファイルを見つけます:

```swift
for try await _ in host.favoritesWidgetSnapshots.asAsyncSequence() {
    WidgetCenter.shared.reloadAllTimelines()
}
```

emission は完了した書き込みを表すため、App Group コンテナが受け付けられない書き込みは何も emit せず、writer はコンテナが存在しないことを `KaigiLogger` を通じて 1 回だけ報告します。これ以外のものがタイムラインを reload してはいけません: 書き込みに続かない reload は、同じ内容を再び表示するだけです。

## タップ

各状態は `widgetURL` を設定し、進行中のセッションの行は自身の `Link` を設定します。URL は共有のディープリンクスキームであり、`onOpenURL` がそれを `KaigiAppHost.submitDeepLink(url:)` に渡します。状態と URL の対応表については、[ディープリンク（DeepLinkEffect）](./navigation-deep-links.ja.md) を参照してください。

## フレームとアートワーク

フレームは `SketchFrame` で、`app-android/.../widget/SketchBorderBitmap.kt` が固定している seed、基準サイズ、wobble パラメータを引き継いだ `SketchRoundRectShape` の移植です。そのノイズは、もう一方の実装がそれを正確に再現できるように仕様化されており、両プラットフォームが同じアウトラインを描きます。どちらか一方への変更は、もう一方にも反映しなければなりません。

シンボルマークとマスコットは `WidgetArtworks` で、`app-android/src/main/res/drawable` 配下の Android の vector drawable のアウトラインを、各呼び出し箇所で着色したものです — ほとんどのキャラクターはストロークの線画、マスコット F は塗りつぶしのアウトラインです。これらはそれらのファイルから書き起こされたものであり、drawable が変更されたら書き起こし直さなければなりません。ウィジェットは描画のたびに、描画時点を seed として 6 体のマスコットから 1 体を疑似ランダムに選びます。

関連: [iOS 概要](./ios.ja.md) · [ディープリンク（DeepLinkEffect）](./navigation-deep-links.ja.md) · [Swift ↔ Kotlin の相互運用](./ios-interop.ja.md)
