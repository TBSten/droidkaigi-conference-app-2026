# 命名レビュー

名前は、それがラベル付けする宣言そのものよりもはるかに多く読まれ、宣言のうち呼び出し側に届く唯一の部分です。このページでは、名前と型が食い違うのはどのような場合か、名前が何に適用されるのかや複数の同種の値のうちどれを保持するのかを省いているのはどのような場合か、そしてレビュアーがこれらのルールをどう適用するかを述べます。対象は値の宣言 — プロパティ、パラメータ、戻り値であり、イベントコールバックもそこに含まれます。また、composable が、返す値を保持する呼び出しから受け取るプレフィックスについても述べます。Compose view の命名は [画面の実装](./building-a-screen.ja.md#compose-view-の命名規約) で定義されています。

## 名前と型

名前は **値が何であるか** を示し、型は **それがどう表現されるか** を示します。汎用的な型にエンティティを指す名前を付けると、この分担が壊れます。値はそのエンティティではなく、その 1 つの属性だからです。

```kotlin
data class TimetableItem(
    val speaker: String,   // rejected: the value is not a speaker, it is a speaker's name
    …
)
```

`speaker` という名前のプロパティは speaker を約束するので、ある呼び出し側は `item.speaker.name` と書きます。型はテキストを約束するので、別の呼び出し側は `Text(item.speaker)` と書きます。どちらの読みが成り立つのかは、宣言のどこにも書かれていません。名前は、それが運ぶ属性を指さなければなりません:

```kotlin
val speakerName: String
```

チェックは 1 つの問いです: **この宣言の値は `<name>` か?** `speaker: String` に対する答えは no であり、修正は `<entity><Attribute>` の形 — `speakerName`、`roomName`、`sponsorLogoUrl` です。裸のエンティティ名は、型がそのエンティティをモデル化している宣言のものであり、そこでは答えは yes になります: `ContributorsScreenUiState.contributors` は `Contributor` を保持し、サフィックスを取りません。

## 影響

- **呼び出し側はその約束に沿って書かれます。** エンティティとして読める名前は `speaker.name` や `speaker.iconUrl` を誘発します — 型を読んだ時点で書き直さなければならないコードです。
- **誤った操作が正しく見えます。** `sessions.groupBy { it.speaker }` は speaker でグループ化しているように読めますが、実際には表示テキストでグループ化しており、同じ名前を持つ 2 人を統合してしまいます。`groupBy { it.speakerName }` は、その欠陥をそれが存在する行で見えるようにします。
- **エンティティ名は借り物です。** `speaker: String` は、セッションがテキストで識別される 1 人の speaker を持つということも、暗黙のうちに決めてしまいます。アイコンや 2 人目の speaker が加われば、どのみちプロパティはリネームされますが、`speakerName` であれば `speaker` を、それを所有することになる型のために空けておけます。

## 修正方法の選択

ルールを満たす修正は 2 つあり、違いはそのエンティティがドメインにすでに存在するかどうかです。

| 状況 | 修正 |
| --- | --- |
| 値がその 1 つの属性である — アプリはそれを表示し、他には何も読まない | `<entity><Attribute>` にリネームする |
| 呼び出し側がすでに 2 つの属性を同時に必要としている、または同一性を比較している | 型を導入し、裸の名前を維持する |

## Effect の命名

`Effect` サフィックスは、ツリーに対して何をするかによって composable を区分します。処理を実行し — flow の収集、コールバックの登録、外部状態の同期 — ノードを一切 emit しないものはこのサフィックスを取り、ノードを emit するものはこのサフィックスを付けてはいけません。この区分は構造上重要です。`SingleRootEmission` チェッカー ([Enforcement](./enforcement.ja.md#singlerootemission)) はこのサフィックスを「何も emit しない」と読んで呼び出しを除外するため、`…Effect` という名前の emit する composable は、すべての呼び出し側でこのルールから見えなくなります。チェッカーは 1 つ目の方向を担保し — サフィックスのない emit しない composable は、emit の隣で呼び出された箇所でコンパイルに失敗します — 2 つ目はレビューが担保します。こちらはどのチェッカーにも判定できません。

```kotlin
@Composable
fun RemoteImageLoaderEffect() { … }   // registers a factory, emits nothing

@Composable
fun ShimmerEffect(modifier: Modifier) {   // rejected in review: emits a Box, so the name must not say Effect
    Box(modifier.background(shimmerBrush()))
}
```

## 過剰な限定

サフィックスは属性を表すものであり、型を表すものではありません。`title: String` はすでに属性名なのでそのままにします。`titleString` や `titleText` は型が宣言していることを繰り返しているだけです。サフィックスを追加するのは、名前が現時点でエンティティを指している場合だけです。

## 限定の不足

名前は型と一致していても、宣言の対象のどの部分に届くのかを省いているために不適切になることがあります。[名前と型](#名前と型) の問いには yes と答えられます — `seed: Int` の値は seed です — それでも読み手には、引数を渡すために必要な唯一の事実が与えられていません。

```kotlin
@Composable
fun KaigiNavigationBarScope.KaigiNavigationBarItem(
    selected: Boolean,
    seed: Int,   // rejected: the destination draws an icon and an indicator, and this reaches only the indicator
    icon: @Composable () -> Unit,
)
```

修正は要素を名前にすることです: `indicatorSeed`、`dividerSeed`、`outlineSeed`。裸の語は、対象全体がそれを適用する唯一のものである宣言のために取っておきます — `SketchShape.seed` はそれが属する shape に seed を与え、他には何も届く範囲にありません。

この問いは 1 つのシグネチャではなく API 全体に対して投げかけます。`KaigiNavigationBar` は outline を 1 つだけ描くため、その本体の中では `seed` は曖昧さなく読めます。しかし bar とその item は一緒に書かれ、item はすでに `indicatorSeed` を取っています。並べて読むと、bar の裸の `seed` は、呼び出し側が 2 つのどちらを保持しているのかを問いかけてきます。これは `outlineSeed` です。

このルールは seed にとどまらず一般化されます。宣言が、その所有者が描画する複数の部分の中から 1 つを選ぶ場合は常に、その名前がどの部分かを示します。合成物の 1 つの要素に届く色、shape、サイズは、その要素にちなんで命名します。

## カテゴリ名

あるカテゴリの複数のメンバーが届く範囲にある場合、どのメンバーを保持しているかではなくカテゴリを示す名前は、限定が不足しています。

```kotlin
val scope = rememberCoroutineScope()           // rejected
val coroutineScope = rememberCoroutineScope()  // required
```

`scope: CoroutineScope` は [名前と型](#名前と型) の問いを通ります — 値は scope です — それでも読み手はどの scope なのかを判別できません。Compose のコードは scope であふれています: `RowScope`、`BoxScope`、`CoroutineScope`、そしてこのリポジトリが宣言するコンポーネントの scope (`KaigiNavigationBarScope`、`KaigiSingleChoiceSegmentedButtonRowScope`)。裸の語はそれらすべてが属するカテゴリを名前にしているので、名前はメンバーを示します: `coroutineScope`、`rowScope`、`navigationBarScope`。

これは [限定の不足](#限定の不足) を別の軸から見たものです。あちらでは名前が、値が所有者のどの部分に届くのかを省いています。こちらでは、複数の同種の値のうちどれを保持するのかを省いています。あちらと同様、この問いは宣言単体ではなく周囲に対して投げかけます — 同じファイルにカテゴリの 2 つ目のメンバーが現れると、宣言自体が変わらなくても裸の語は曖昧になります。

## イベントコールバック

view に宣言されたコールバックパラメータは、そのハンドラがその後に行う処理ではなく、**それが報告するイベント** を名前にします。パラメータが何を名前にするかは、それを宣言するレイヤーによって変わります。それは [レイヤーとレジスター](#レイヤーとレジスター) が示します。名前をフレーズとして読み、そのフレーズが起きたことを説明しているかを問いかけてください。

### 動詞と目的語

目的語は動詞が作用する対象であり、その動詞が取ることのできるものです。

```kotlin
onDescriptionToggleClick: () -> Unit   // rejected: the description reads the same before and after
```

反転するのはそのセクションが展開されているかどうかなので、名詞は expansion です: `onDescriptionExpansionToggleClick`。コントロールをそれが操作する対象にちなんで命名するのは、実際にそれを操作する場合にのみ成り立ちます — `onBookmarkClick` は通ります。そのコントロールは実際にブックマークを追加・削除するからです。

動詞は widget がすでに使っているものです: 押下には `Click`、ユーザーが編集する値には `Change`、ユーザーが閉じる surface には `Dismiss`。

### 結果と入力

```kotlin
onMemoCommit: (String) -> Unit   // rejected: whether an edit is worth persisting is not the view's call
```

`Commit` は、テキストが確定していて書き込まれることを約束します。フィールドは編集ごとに報告し、presenter はそのうちどれがストレージに届くかを決めます。結果を示す名前は view を 1 つのポリシーに縛り付け、そのポリシーが変わると偽になります — フォーカスを失ったときに書き込むフィールドと、キー入力のたびに書き込むフィールドは、同じイベントを発生させます。

名前は入力を示します: `onMemoChange` であり、それが転送する `onValueChange` と一致します。結果は、それが決定される場所、つまり presenter が処理する action で独自の名前を持ちます。

### イベントとハンドラ

コールバックは 1 つのイベントを報告します。ハンドラがたまたま今は一致している 2 つのコントロールも、やはり 2 つのイベントであり、それぞれが独自のパラメータを取ります。

```kotlin
onArchiveClick: (String) -> Unit   // rejected: the video row and the slides row report through it
```

どちらの行もアドレスを開くため、1 つのパラメータが両方を担っていました — そして screen は、そのどちらが押されたのかを判別できなくなっていました。これらは `onArchiveVideoClick` と `onArchiveSlideClick` です。root が両方を同じラムダにマッピングします。2 つのイベントが 1 つのことを意味すると決めるのは、そこが適切な場所です。

パラメータを共有してよいかどうかは、ハンドラが現在何をしているかではなく、イベントが何であるかによって決まります。一致しなくなったハンドラ — ブラウザではなくプレイヤーで開く動画、カウントされるデッキ — は、共有パラメータが捨てた区別を必要とし、それを復元する作業は、そのイベントが横断するすべてのレイヤーに及びます。

### レイヤーとレジスター

| 宣言される場所 | 名前にするもの | 例 |
| --- | --- | --- |
| view — コンポーネントまたは `<Feature>Screen` | 入力 | `onBackClick`、`onBookmarkClick`、`onFloorClick` |
| `<Feature>ScreenRoot` が転送するラムダ | 意図 | `onNavigateBack`、`onNavigateToStaff` |
| `<Feature>ScreenAction` | presenter に求める変更 | `SelectFloor`、`SaveMemo` |

```kotlin
@Composable
fun AboutScreen(
    onOpenStaff: () -> Unit,   // rejected: opening is what the root does with the press
)
```

このパラメータは、読むことも押すこともできる行に置かれており、`onStaffClick` はそこに何が届いたかを示します。`AboutScreenRoot` が `onStaffClick = onNavigateToStaff` として意味を与えます。意図の名前にすると、2 つのレイヤーが同じことを二度述べることになり、view は自分が持っていない知識を抱えます — pane、ダイアログ、あるいはテストから届いた同じ押下は、開くこと以外のものに結び付きます。

root のラムダが 1 つの action を送る以外に何もしない場合、この 2 つは 1 つのイベントを 2 つの高度で名前にします: **名詞は同じで、動詞はレイヤーに属します**。`onFloorClick` は `SelectFloor` を送り、`onDayClick` は `SelectDay` を送り、`onMemoChange` は `SaveMemo` を送ります — Floor、Day、Memo は横断して受け渡され、`Click` と `Change` は入力を報告し、`Select` と `Save` は変更を要求します。途中で入れ替わる名詞は、やりかけのリネームか、root が暗黙に行っている翻訳です: `onUiTypeChangeClick` は `SwitchToGridView` を送りますが、呼び出し側が読むパラメータは grid について何も語りません。

したがって [結果と入力](#結果と入力) は view で止まり、その下で反転することはありません。action は結果を要求するために存在するので、それが取る動詞は命令形です。[動詞と目的語](#動詞と目的語) はそのまま適用されます: `Bookmark(id)` は、action がトグルする場面で 1 つ追加するように読めます。

## ファクトリのプレフィックス

**戻り値が再コンポジションをまたいで保持する値である** composable は、`remember` がそうであるように、それを保持する呼び出しのプレフィックスを取ります。`retain` 呼び出しであれば `retain<Value>` となります。

```kotlin
@Composable
fun <A, R> screenChannel(): ScreenChannel<A, R> = retain { ScreenChannel() }         // rejected

@Composable
fun <A, R> retainScreenChannel(): ScreenChannel<A, R> = retain { ScreenChannel() }
```

プレフィックスがないと、呼び出し側はその時点で生成された新しい値のように読め、その値が再コンポジションより長く生き残ることも、`retain` が存在する理由である一時的な破棄を生き延びることも、呼び出し箇所では何も示しません。

プレフィックスは本体ではなく、返される値に従います。presenter は自分が切り替える state を retain し、そこから構築した `UiState` を返します。retain された state は呼び出し側が受け取るものではないため、presenter は自分のレイヤーの名前を保ちます。

仕組みを組み込むファクトリは、代わりにその仕組みを名前にし、このルールの外にあります。`retainNavEntryDecorator` は `remember` が保持する decorator を返し、`retain` はその decorator が配下のすべての entry に提供するものを示します — これが `rememberSnackbarNavEntryDecorator` との違いでもあります。

## 関連する不一致

同じ食い違いは、型が名前の約束するほどの構造を持たないところならどこでも現れます。

| 却下される形 | 読まれ方 | 要求される形 |
| --- | --- | --- |
| `val featuredSession: TimetableItemId` | セッション | `featuredSessionId` — 識別子を保持するプロパティはそのことを示します。value class もそれを示しているとしてもです |
| `val favorite: Boolean` | お気に入り | `isFavorite` — 値は主張に対する答えなので、名詞には `is` / `has` / `can` を付けます |
| `val speaker: List<String>` | 1 人の speaker | `speakerNames` — コレクションは複数形にし、要素の属性を名前にします |

形容詞や分詞はすでに主張として読め、それ自体で成立します — `enabled`、`dataCleared`。そこにプレフィックスを付けるのは過剰な限定であり、それが渡される Compose のパラメータ (`Button(enabled = …)`) とも矛盾します。プレフィックスが必要なのは、裸の語がこのドメインにおける物も指す場合です。favorite は `Timetable.bookmarks` の要素なので、`favorite` 単独ではそのうちの 1 つとして読めます。

## レビュー手順

diff の中で型が汎用的な (`String`、`Int`、`Boolean`、またはそれらのコレクション) 宣言それぞれについて:

1. 名前を単体で読み、それが約束する値を述べます。
2. その約束を型と比較します。不一致があれば、[修正方法の選択](#修正方法の選択) から修正を選び、現在の名前の下で最も読みづらい呼び出し箇所とあわせて報告します。
3. 一致していれば、その所有者に値が届き得る部分が複数あるかを問います。複数ある場合は、名前がどれかを示します。[限定の不足](#限定の不足) を参照してください。

型による絞り込みが対象とするのは、この 3 ステップだけです。[イベントコールバック](#イベントコールバック) は diff 内のすべての関数型パラメータに適用されます: 名前をフレーズとして読み、その目的語が動詞の作用対象と合っているか、ハンドラが決める結果を示していないか、1 つのイベントを報告しているか、そしてそれを宣言するレイヤーのレジスターに位置しているかを確認します。[ファクトリのプレフィックス](#ファクトリのプレフィックス) は値を返すすべての composable に適用されます。[カテゴリ名](#カテゴリ名) は型を問わず diff 内のすべての宣言に適用されます: 名前を普通名詞として読み、届く範囲に複数のメンバーを持つカテゴリを名前にしている場合は、名前がそのメンバーを示します。

`:core:model` のドメインモデルと `UiState` のプロパティが最優先です。それらの名前は、それを描画するすべての feature に届きます。

## 静的な強制の範囲

エンティティの語と属性の語を切り分けるにはドメインの語彙が必要です — `title` も `speaker` もどちらも名詞であり、それらを見分けられるのはカンファレンスのドメインだけです。FIR checker はその語彙をハードコードされたリストとして必要とし、そのリストを整備すること自体が、それが置き換えようとしているレビューそのものです。したがってこのルールは [Enforcement](./enforcement.ja.md) 階層のレベル 3 にとどまります。

[カテゴリ名](#カテゴリ名) がレベル 3 にとどまるのには 2 つ目の理由があります: ある時点でカテゴリの 2 つ目のメンバーが届く範囲にあるかどうかは、チェッカーが決定できる性質ではありません。メンバーが 1 つしかスコープにない間は裸の語を許可するルールは、無関係な編集が 2 つ目を持ち込んだ瞬間に正しい宣言を違反へと変えてしまい、その診断はその編集が触れてもいない行に着弾します。

[ファクトリのプレフィックス](#ファクトリのプレフィックス) のルールは、直接的な形であれば判定可能です — 戻り値の式が、[`RememberResultMustBeBound`](./enforcement.ja.md#rememberresultmustbebound) がすでに解決しているプロデューサ呼び出しの 1 つである composable のことです — そしてチェッカーは同じ callable id を読むことになります。決定できないのは一般的なケースです: 自分が構築するオブジェクトを通じて retain された state を公開する関数は、自身の本体が呼び出す呼び出し先の本体を必要としますが、別々にコンパイルされたモジュールはそれを持ちません。代わりに「本体のどこかで retain している」ことを条件に書かれたルールは、すべての presenter を報告してしまいます。presenter は自分のレイヤーが与える名前の下で正しく retain しています。

関連: [Enforcement](./enforcement.ja.md) · [画面の実装](./building-a-screen.ja.md)
