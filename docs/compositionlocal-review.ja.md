# CompositionLocal レビュー

`CompositionLocal` は、**composition 内の位置によって異なる**値を運びます。値を単に渡す場合に対してそれが加えるものは、それがすべてです。任意のサブツリーが自身の値をインストールでき、読み手は最も近いものを受け取ります。どこで読んでも同じ値は、そこから何も得られず、どのコードも必要とせずどのテストも検査しない自由だけを得ます — 1 つの画面の 2 つの部分が、気づかれないまま食い違うおそれがあるのです。

したがって、追加する前に答えるべき問いは「これに手が届くと便利か」ではなく、**「どの 2 つの位置が異なる値を読み、そしてなぜそうあるべきか」**です。

## アプリにあるもの

いずれもその問いに対する基準となります。

| | なぜ位置によって変わるのか |
| --- | --- |
| `LocalSnackbarHostState` | nav entry が自身の host を所有するため、メッセージはそれを上げた画面に表示されます |
| `LocalPreviewImageResolver` | プレビューは画像をローカルの drawable から描画し、リリースビルドはネットワークを参照します |
| `LocalSchemeIsDark` | theme がそれを提供するため、異なるテーマが適用されたサブツリーは異なる値を読み取ります |
| `LocalDeviceTiltSource` | アプリはプラットフォームのセンサーをインストールし、プレビューは水平のデフォルト値を読むため golden が動きません |

## 答えが no の場合

値は依然として使われる場所へ届かなければならず、その値の性質に合った継ぎ目を通ります。

| 値が | 届く経路 |
| --- | --- |
| アプリが所有するサービス（clock、logger、reporter） | DI グラフを通って [role context](./screen-context.ja.md) へ |
| データ、またはそこから導出されるもの | presenter が計算する `UiState` |
| あるコンポーネントが必要とし、その呼び出し元が持っているもの | パラメータ |

パラメータが複数の composable を貫通することになるからという理由で `CompositionLocal` に手を伸ばすのは、値についてではなくコンポーネントツリーについてのシグナルです — [`UiComponentTakesWhatItReads`](./enforcement.ja.md#uicomponenttakeswhatitreads) を参照してください。

## provider の欠落を隠すデフォルト値

デフォルト値は、provider がインストールされていない状態での読み取りが何を意味するかを決めるため、3 つのケースのどれに当てはまるかを表明します。

| デフォルト値 | 表明すること |
| --- | --- |
| `error("…")` | provider が必須であること。provider なしでの読み取りは配線のミスであり、`LocalSnackbarHostState` はそれをインストールする decorator を名指しします |
| `null` | 不在が実際のケースであること — `LocalPreviewImageResolver` を持たない composition は画像をネットワークから読み込みます |
| 使用可能な値 | provider なしでの読み取りが機能し、provider はスコープを狭めるだけであること。`LocalSchemeIsDark` は `KaigiTheme` が別の値を表明するまで light scheme として読み取られます |

正当化が必要なのは 3 つ目です。機能するデフォルト値は、忘れられた provider をクラッシュではなく黙って異なる答えに変えてしまうため、provider のない状態の振る舞いが自ら選ぶであろうものである場合にのみ正しいと言えます。

## レビュー手順

diff で追加された `CompositionLocal` のそれぞれについて:

1. 異なる値を読み取る 2 つの位置を挙げてください。答えが「今はないが、いずれあるかもしれない」であれば、それは該当しません。
2. デフォルト値を読んでください。provider のない読み手が使用可能な値を得るのであれば、そのケースが穴ではなく意図されたものであることを確認してください。
3. 型を読んでください。サービスやデータ型は [答えが no の場合](#答えが-no-の場合) にある継ぎ目のいずれかに属します。

diff 内の**読み取り**のそれぞれについては、読み手が UI であることを確認してください。presenter から到達される `CompositionLocal` は、presenter の結果をどこで composition されたかに依存させてしまい、そのユニットテストではそれを変えられません。

## 静的な enforcement の範囲

値が位置によって変わるかどうかは型ではなく意図についての事実なので、どのチェッカーもそれを判断しません。FIR レイヤーがカバーするのは、`CompositionLocal` に手を伸ばす動機となりがちな形です。すなわち、レンダリングする以上のものを受け取るコンポーネント（[`UiComponentTakesWhatItReads`](./enforcement.ja.md#uicomponenttakeswhatitreads)）と、役割の外側での Soil の読み取り（[`SoilReadConfinement`](./enforcement.ja.md#soilreadconfinement)）です。

関連: [Enforcement](./enforcement.ja.md) · [ScreenContext の設計](./screen-context.ja.md) · [Clock（KaigiClock）](./clock.ja.md)
