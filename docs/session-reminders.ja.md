# セッションリマインダー

お気に入りに登録されたセッションは、開始の 15 分前にローカル通知を出します。Android と iOS がそれをスケジュールします。desktop と web はスケジュールせず、それらのグラフはこれに関するものを何も持ちません。

## 何を、いつ通知するか

`:core:model` がそれに純粋関数で答えます。お気に入りウィジェット自身の計算の隣に置かれています。

```kotlin
data class SessionReminder(
    val itemId: TimetableItemId,
    val title: MultiLangText,
    val room: Room,
    val startsAt: Instant,
    val startsAtText: String,
    val notifyAt: Instant,
)

fun computeSessionReminders(
    now: Instant,
    timetable: Timetable,
    favoriteIds: Set<TimetableItemId>,
): List<SessionReminder>
```

結果は、セッションがまだ始まっていないお気に入りを、早い順に保持します。リマインダーは `notifyAt` を過ぎても結果に残り続けるため、再スケジュールがまだ期限内のものを取りこぼすことはなく、リードタイム内にお気に入り登録されたセッションは即座に通知されます。これはプラットフォームが解釈しなければならないスケジュールではなく時刻のリストであるため、プラットフォームレイヤは変換して引き渡すだけです。

## プラットフォームとの同期を保つ

`SessionReminderSync`（`:app-shared`、`AppScope`）は、お気に入りが変わったとき、`KaigiClock` のオフセットがずれたとき、サーバー環境が変わったとき、あるいはタイムテーブルの取得が永続化されたペイロードを置き換えたとき（`PersistedTimetableReader.updates`）に再スケジュールします。各ラウンドは永続化されたタイムテーブルを読み、リマインダーを計算して `SessionReminderScheduler` の binding に渡します。最初の emission でも再スケジュールするため、アプリの起動時には前のプロセスが残したものが何であれ調停されます。読み取り可能なタイムテーブルがないラウンドは既存のスケジュールをそのまま残し、失敗したラウンドはログに記録され、collector を壊すことはありません。

```kotlin
interface SessionReminderScheduler {
    /** Replaces every reminder scheduled before with this set. */
    suspend fun reschedule(reminders: List<SessionReminder>)
}
```

`:app-shared` はデフォルトを bind しません。リマインダーを必要とする各プラットフォームが自身の実装を contribute し、自身のグラフに `sessionReminderSync` を公開します。`AndroidAppGraph` と `IosAppGraph` はそうしており、desktop と web のグラフはこの sync にまったく到達しません。Android は `KaigiApplication.onCreate` から、iOS は `KaigiAppHost.initialize()` から、いずれもプロセスライフタイムのスコープで開始します。

## Android

`AndroidSessionReminderScheduler` は、リマインダーごとに `setAndAllowWhileIdle(RTC_WAKEUP, …)` で `AlarmManager` のアラームを 1 つスケジュールし、スケジュール済みの id を自身の `DataStore<Preferences>` に保存します。1 ラウンドではまず保存済みの id と新しい id の和集合を永続化し、外れた id のアラームと投稿済みの通知をキャンセルし、残りを設定し、それから新しい id を永続化します。したがってラウンドの途中でクラッシュしても、次のラウンドが到達できないアラームが残ることはありません。トリガー時刻はデバイスの時計に `KaigiClock.now()` から `notifyAt` までの距離を足したものであるため、デバッグツールのずらした時計もアラームに反映されます。前のラウンドですでに設定されたリマインダーは、その `notifyAt` を過ぎたらそのままにされるため、再スケジュールがユーザーの見た通知を再投稿することはありません。

これらのアラームは意図的に不正確です。正確なアラームには `SCHEDULE_EXACT_ALARM` が必要ですが、これは Android 14 以降ではユーザーがシステム設定で付与しなければならず、いつでも取り消せます。あるいは `USE_EXACT_ALARM` が必要ですが、Google Play はこれを中核機能が計時であるアプリからしか受け付けません。数分ずれて届くリマインダーもリマインダーであることに変わりはないため、アプリはこの権限には一切関わりません。

`SessionReminderReceiver` が通知を投稿します。セッションタイトル、その下に "Starts at HH:mm · room (floor)"、そしてコンテンツインテントとしてセッションのディープリンクです。`POST_NOTIFICATIONS` が付与されていない場合は投稿をスキップします。`BootCompletedReceiver` は再起動後に再スケジュールします。再起動はアプリが設定したすべてのアラームを消去するためです。

## iOS

`IosSessionReminderScheduler` は、リマインダーごとにセッション id で識別される `UNTimeIntervalNotificationTrigger` のリクエストを 1 つ保持します。1 ラウンドでは、もはや不要になった id の保留中のリクエストと配信済みの通知を削除し、まだ先にある各リマインダーのリクエストを追加します。`notifyAt` を過ぎたリマインダーは 1 秒のインターバルで追加されます。ただし前のラウンドですでに設定されている場合を除きます — その比較のために、両プラットフォームとも最後のラウンドの id を共有の `DataStore`（`ScheduledSessionReminderIds`）に保持します。iOS はアプリごとに保留中のリクエストを最大 64 件しか保持せず、残りは黙って破棄するため、スケジューラは最も早い 64 件を保持します。

認可（`alert`、`sound`、`badge`）も、スケジュールするものがあるときには必ず要求されます。OS はプロンプトを一度だけ表示します。`KaigiAppHost.initialize()` からインストールされる `SessionReminderNotificationDelegate` は、タップを `DeepLinkStore` を通じてセッションのお気に入りディープリンクに変え、アプリがフォアグラウンドにある間はリマインダーをバナーとして表示させます。

## 権限の要求

権限は起動時ではなく、最初のお気に入り登録時のガイダンスから要求されます。リマインドするものが存在するまでは伝えるべきことがなく、システムのプロンプトが現れる前にダイアログが通知が何のためのものかを説明するためです。

お気に入りを追加できる画面は、その追加をセッションのルームを伴う `ActionResult` として報告し、その navigator が `FirstFavoriteNotificationNavKey`（`:app-shared`）を push します。これは 2 ステップのダイアログの最初のもので — 通知のステップ、次にホーム画面ウィジェットのステップで、後者は `AppNavigator.replaceTop` で到達します。ルームはダイアログが描くマスコットを決めます。ガイダンスをまだ提示すべきかどうかは 1 つの subscribe された値 `FirstFavoriteGuidanceOfferableSubscriptionKey` であり、Android と iOS でのみ、かつガイダンスに回答されるまでの間だけ true です。どちらであれ明示的な回答は `FirstFavoriteGuidanceStore` に記録され、一方で戻る操作で閉じられたダイアログはこのフラグをそのままにするため、後のお気に入り登録でもう一度提示されます。

実際に要求するのは `rememberNotificationPermissionRequester`（`:core:ui`）で、読み手が回答したら戻ります。Android 13 以降では `POST_NOTIFICATIONS` のランタイム権限で、システムのプロンプトをもはや表示できない場合はアプリの通知設定にフォールバックし、iOS では `UNUserNotificationCenter` の認可です。

関連: [Clock（KaigiClock）](./clock.ja.md) · [AppGraph と UiGraph](./di-app-graph.ja.md) · [ディープリンク（DeepLinkEffect）](./navigation-deep-links.ja.md)
