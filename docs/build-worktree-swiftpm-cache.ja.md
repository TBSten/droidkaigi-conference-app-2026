# worktree 間での SwiftPM インポートキャッシュ

`:app-ios-kotlin` は `swiftPMDependencies` を通じて Swift Package Manager の依存関係を宣言しているため、Gradle sync のたびに Firebase iOS SDK のパッケージグラフが解決され、`xcodebuild` でビルドされます。Kotlin Gradle plugin は、その結果を working tree 内の 3 つのディレクトリに書き込みます。

| ディレクトリ | 内容 | 共有 |
| --- | --- | --- |
| `.swiftpm-locks/default/swiftPMCheckout` | umbrella パッケージの SwiftPM checkout | はい |
| `app-ios-kotlin/build/kotlin/swiftPMCheckout` | `-clonedSourcePackagesDirPath` として `xcodebuild` に渡される SwiftPM checkout | はい |
| `app-ios-kotlin/build/kotlin/swiftImportDd` | `-derivedDataPath` として渡される `xcodebuild` の derived data | いいえ |

合わせておよそ 3 GB を占め、そのうち 2 つの checkout が 2.4 GB です。いずれも宣言された Gradle の出力ではなく、SwiftPM の import タスクはすべて `@DisableCachingByDefault` を持つため、Gradle のビルドキャッシュが結果をある working tree から別の working tree へ運ぶことはできません。そうでなければ、`git worktree` を追加するたびに解決とビルドのサイクル全体が繰り返されます。

derived data は working tree ごとのままです。`xcodebuild` は 1 つの derived data ディレクトリの同時使用を拒否し（Kotlin Gradle plugin が既に SDK ごとに 1 つ保持しているのはそのためです）、共有しても計測できるほどの時間短縮にはなりません。プラグインはいずれにせよ、実行のたびに synthetic なパッケージの build ディレクトリを削除するからです。

## セットアップ

clone ごとに一度、hook をインストールしてください。これが推奨されるセットアップです。リンクされる前に sync してしまった working tree は、フルのコストを一度支払ったうえで自分自身のコピーを抱えることになるからです。

```sh
scripts/link-swiftpm-cache.sh --install-hook
```

これは clone の共有 hook ディレクトリに `post-checkout` hook を書き込むため、その clone からの `git worktree add` はすべて自分自身をリンクします — コーディングエージェントが作る working tree も含みます。Git がすべてゼロの ref を直前の HEAD として渡すのは `git worktree add` と `git clone` のときだけなので、通常のブランチやファイルの checkout では hook はスキップされます。また hook は常に成功して終了するため、失敗が checkout を失敗させることはありません。既存の `post-checkout` hook はそのまま残され、代わりにその旨が報告されます。

hook が及ぶのは、それ以降に作られた working tree だけです。既に存在するものは 1 つずつリンクしてください。

```sh
scripts/link-swiftpm-cache.sh
```

どちらの経路でも、2 つの checkout は clone の git ディレクトリ内の `swiftpm-import` へのシンボリックリンクに置き換えられます — `git rev-parse --git-common-dir` が報告するディレクトリで、その clone のすべての working tree が共有します。そこに置くことで、ストアは clone に紐づきます。clone を削除すればストアも一緒に消え、どの working tree もそれを所有しないため、1 つを削除しても残りは無傷です。Git はそのディレクトリ内の何も追跡しないので、`.gitignore` のエントリなしでストアは `git status` に現れません。

実体のディレクトリを既に保持している working tree では、それらがストアへ移動されます。あとから作られたものは、直接ストアへリンクします。スクリプトの再実行は何もせず、既存の出力を破棄する代わりに上書きを拒否します。

ストアを別の場所に置くには `--store <dir>` を渡します。hook は引数を取らないため、hook にもそれを尊重させたい場合は代わりに `SWIFTPM_IMPORT_CACHE` を export してください。移動する理由になるのは clone のバックアップです。バケットは数ギガバイトあり、git ディレクトリは通常バックアップから除外されないからです。

## 依存関係のセットごとに 1 つのバケット

ストアは、SwiftPM のマニフェスト — `.swiftpm-locks` 配下のすべての `Package.swift` — のダイジェストにちなんで名付けられたバケットに分割されます。同じ依存関係を宣言する working tree はバケットを共有し、互いの作業を再利用します。依存関係を変更するブランチは専用のバケットを得るため、その解決が他のブランチの指す checkout を書き換えることはありません。

ダイジェストが対象とするのはマニフェストだけです。`Package.resolved` は解決によって書き換えられるため、それをキーにすると、バージョンが動くたびに working tree が別のバケットへ移ってしまいます。

Gradle はマニフェストを `app-ios-kotlin/build.gradle.kts` から再生成するため、`swiftPMDependencies` が変わると working tree はバケットを変えます。編集後にスクリプトを再実行すれば、リンクが移し替えられます。

マニフェストが再生成されるのは sync によってであり、つまりその sync が走るまでは以前の依存関係のセットを記述したままです。`swiftPMDependencies` を編集したブランチは、他のブランチが使うバケットの中で一度だけ解決を行い、他のブランチの `Package.resolved` は次の sync でそれに追随します。そのようなブランチでは、その最初の sync を共有ストアの外に置くために `--store <dir>` を渡してください。

## バケットの回収

Git には `git worktree remove` 用の hook がないため、バケットはそれを作った working tree よりも長く残ります。使われていないものを回収してください。

```sh
scripts/link-swiftpm-cache.sh --gc
```

これは、working tree がまだリンクしているバケットをすべて残します。ダイジェストを再計算するのではなくリンク自体を読むため、再実行されないまま依存関係が変わった working tree も、実際に使っているバケットを保持できます。それ以外はすべて削除されます。バケットは数ギガバイトあります。

バケットを削除すると、それを指していた working tree にはリンク切れが残ります。そこでスクリプトを再実行してください。ディレクトリが復元され、そのバケットに付随していたパスを指す `.def` と `.ld` のファイルが破棄されます。それらは次の sync で再生成されます。

これらのファイルを取り除くことが重要なのは、それらを生成するタスクが入力としてマニフェストしか宣言していないからです。それらが指すパスが消えても Gradle はファイルを保持し続け、その結果ビルドは、もう存在しないファイルについてリンカで失敗します — 原因から遠く離れた場所で。バケットや derived data ディレクトリを、あとでスクリプトを再実行することなく手で削除してはいけません。

`clang` と `xcodebuild` はシンボリックリンクを実体のパスへ解決するため、生成される `.def` と `.ld` のファイルはストアの場所を記録し、どの working tree でも有効なままです。リンクされた working tree が抱えるビルド出力は 3 GB ではなく約 700 MB になり、IDE の sync が実行するタスクである `prepareKotlinIdeaImport` は、およそ 4 分から 1 分半程度へ短縮されます。

## 制約

- `./gradlew clean` はリンクを削除しますが、ストアは削除しません。あとでスクリプトを再実行してください。
- checkout は共有されたままなので、2 つの working tree が同時に解決を行うと、1 つのディレクトリに対して解決することになります。

## 残る sync のコスト

残った時間のおよそ半分は `fetchUmbrellaPackageIdentifierForDefault` と `fetchSyntheticImportProjectPackages` に費やされます。これらは checkout が完全であっても、グラフ内の各パッケージの現在のリビジョンを GitHub に問い合わせます。その部分はネットワークの遅延に依存し、共有ストアの影響を受けません。
