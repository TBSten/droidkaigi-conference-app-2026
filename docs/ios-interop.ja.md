# Swift ↔ Kotlin の相互運用

iOS はネイティブのタブバーだけを持つほぼ全面的な CMP であるため、Swift ↔ Kotlin の境界は小さくなっています。Swift Export / Swift Package Import は 2026 年時点で experimental であり、これもその境界を小さく保つもう 1 つの理由です。その境界の中では:

- **Kotlin → Swift（Swift が Kotlin を呼ぶ）: Swift Export。** ネイティブのタブバーは、export された `AppShared` モジュールを通じて Kotlin の API（タブの選択、view controller の factory）を呼び出します。このモジュールは、間に Obj-C ヘッダを挟むことなく、Kotlin が idiomatic な Swift として生成します。`:app-ios-kotlin` はモジュールに名前を付け、自身のパッケージをフラット化するため、その宣言は直接 import できます。`:app-shared` において到達するものはすべて、生成された `ExportedKotlinPackages` 名前空間の下に Kotlin のパッケージを保ったままであり、Swift 側は `typealias` でそれを短縮します。
- **Swift → Kotlin（Apple のフレームワークを使う）: Swift Package Import。** iOS 固有の Apple フレームワーク / SPM が必要な場合は、Swift Package Import 経由で Kotlin からそれらを呼び出し、実装を Kotlin 側に保ちます。sync のたびに、import されたパッケージグラフが解決されビルドされます。その作業を `git worktree` の checkout 間で共有する方法については、[worktree 間での SwiftPM インポートキャッシュ](./build-worktree-swiftpm-cache.ja.md) を参照してください。

## export される API 表面に含めてよいもの

Swift Export が API の形を決めるため、それが向けられる Kotlin はそのために書かれなければなりません。`:app-ios-kotlin` はまさにその層を収めるために存在し、何が境界を越えてよいかは 3 つのルールが定めます。

- **Compose の型は不可。** Swift Export はブリッジする関数型から `@Composable` を落とすため、それを持つ宣言は、Swift が正しく呼び出せない API を export することになります。Composable は `KaigiAppHost` の背後に留まり、`KaigiAppHost` はそれらを private なメンバとして保持します。
- **`Flow` の中の enum は不可。** 生成される flow のイテレータはすべての要素をそのクラスブリッジを通してキャストしますが、Kotlin の enum は Swift の enum という値型としてブリッジされるため、それに実行時に失敗します。enum をクラス（`RootTabSelection`）でラップすれば、そのまま境界を越えられます。
- **Metro のグラフには `@HiddenFromObjC` が必要。** `internal` は通常の宣言を export される API 表面から除外しますが、`@DependencyGraph` はそうではありません。それは関係なくソース上の名前で export され、supertype として持つすべての `@ContributesTo` インターフェースを引き連れ、それらとともに Ktor、Soil、DataStore、Compose の名前空間まで持ち込みます。`IosAppGraph` がそのアノテーションを付けているのはそのためであり、モジュール内でそれを必要とする唯一の宣言です。

## 注意点（experimental であることのリスク）

- Swift Export と Swift Package Import はどちらも experimental です（2026 年）。このアプリにはフォールバックできる Obj-C ヘッダの経路がないため、上流の破壊的変更は、迂回されるのではなくここで吸収されます。
- coroutines/flow の Swift interop はまだ安定化の途上にあるため、状態を受け渡す境界は小さく保ってください。

関連: [iOS 概要](./ios.ja.md) · [iOS 上の CMP（埋め込み）](./ios-cmp-embedding.ja.md) · [Swift export](https://kotlinlang.org/docs/native-swift-export.html)
