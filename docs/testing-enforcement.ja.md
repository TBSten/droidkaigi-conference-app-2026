# Enforcement チェッカーのテスト

`:tools:compiler-plugin` のすべての FIR checker ([Enforcement](./enforcement.ja.md) を参照) には、JetBrains 自身のコンパイラテストフレームワーク (`org.jetbrains.kotlin:kotlin-compiler-internal-test-framework` として公開されています) の上に構築された diagnostic テストがあります。テストはプラグインを読み込んだ状態で Kotlin のソースファイルをコンパイルし、diagnostic がマークされたソース範囲にちょうど一致して現れることをアサートします。

```bash
./gradlew :tools:compiler-plugin:test
```

## ディレクトリ構成

| ディレクトリ | 内容 |
| --- | --- |
| `testData/diagnostics/` | ルールごとに 1 つのソースファイル。期待される diagnostic をマーカーとして保持します |
| `test-fixtures/` | 抽象テストランナーと、プラグインを読み込む configurator |
| `test-stubs/` | テストソースが解決対象とする、アプリおよびライブラリの型の代替物 |
| `test-gen/` | `testData/` から生成される JUnit 5 クラス。ビルドによって再生成され、コミットはされません |

## テストデータ

`<!DIAGNOSTIC_NAME!>…<!>` のペアは、diagnostic が現れると期待される正確な範囲をマークします。マークされていないコードは diagnostic を一切生成してはいけないため、各ファイルは却下される形と受け入れられる形の両方を記述します:

```kotlin
class Counter {
    private var mutableCount = 0
    val <!PROPERTY_MUST_USE_PRIVATE_SET!>count<!>: Int get() = mutableCount
}

class CounterWithPrivateSet {
    var count: Int = 0
        private set
}
```

`// FILE: Name.kt` は 1 つのテストデータファイルを複数のコンパイル対象ファイルに分割します。ルールがファイル名を読む場合や、宣言名にマッチするルールが別のルールを誤って発動させないようにする場合に使います。

checker は完全修飾名で型をマッチするため、テストデータは実際のモジュールではなく `test-stubs/` に対して import を解決します — スタブは checker が読む形だけを宣言します。新しい型をキーとするルールには、対応するスタブをそこに追加する必要があります。

Compose コンパイラプラグインの FIR 拡張は enforcement プラグインと並べて登録されるため、テストデータ内の `@Composable` 関数型は、アプリのビルドにおけるものと同じ function-type kind を持ちます。

## ルールのテストデータを追加する

1. `testData/diagnostics/<ruleName>.kt` に、却下される形と受け入れられる形を、マーカーなしで書きます。
2. 期待されるマーカーを埋めます:

   ```bash
   ./gradlew :tools:compiler-plugin:test -Penforcement.updateTestData=true
   ```

   各テストデータファイルは、プラグインが実際に報告した diagnostic で書き換えられます。diff を読んでください: それは現在の挙動の記録であって、意図した挙動の記録ではありません。
3. フラグなしでスイートを実行し、通ることを確認します。

## コンパイラの成果物

テストフレームワークは shade されていない `kotlin-compiler` に対して動作するため、`:tools:compiler-plugin` はその成果物に対してコンパイルします。一方 Kotlin Gradle プラグインは、コンパイラプラグインを `kotlin-compiler-embeddable` へ読み込みます。こちらの IntelliJ のクラスは `org.jetbrains.kotlin.com.intellij` 配下にあります。したがってすべてのモジュールは、`shadowJar` が生成する再配置済みの jar を使い、素の jar を使うことはありません。

## カバレッジ

diagnostic は FIR のソース要素に対して報告され、フレームワークはそれらに `/<file name>` というパスを与えます。`PlatformOnlyNaming` の逆方向 — プラットフォームのプレフィックスには `@PlatformOnly` が必要 — は `/commonMain/` というパスセグメントを条件としているため、このスイートではなくアプリのビルドによってのみカバーされます。

関連: [Enforcement](./enforcement.ja.md) · [テスト概要](./testing.ja.md)
