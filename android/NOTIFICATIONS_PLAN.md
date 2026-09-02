# Notifications Manager — Plan

**Branch:** `notifications` (off `main` at `b1454346`)
**Status:** design agreed, not implemented. Revised after review — §5 and §7
were rewritten; see §12 for what changed and why.

This supersedes the earlier local-only `UPDATE_NOTIFICATIONS_PLAN.md`. That
file predates the nightly-updates PR and describes a silent auto-install flow
(`installSilently`, `InstallResultReceiver`, `nightlyAutoInstall`,
`shouldRunFullyUnattended`) that **no longer exists** — all of it was deleted
before #43 merged. Its durable parts (the root-cause evidence, the Mihon
research, the foreground-service mechanics) are carried over below, rewritten
against the current architecture. Deleting it is a task in §8.

> **Referencing convention:** this document names files and symbols, never line
> numbers. The previous plan's line references were all stale within a week.

---

## 1. Why this exists

Two separate problems, one solution.

**A download does not survive backgrounding.** Confirmed via logcat
2026-08-31: a download started from the UI, then the app backgrounded, dies
seconds later with `SocketException: Software caused connection abort`.
`ApkDownloader.startDownload` runs a plain coroutine on `@ApplicationScope`
with no foreground-service promotion, so the OS freezes and then kills it.
This affects *every* caller — the manual Download tap, restore-to-stable,
`MainShell`'s auto-download effects, and `AppUpdateWorker` alike. There is a
`TODO(update-notifications)` marker on both `ApkDownloader` and
`AppUpdateWorker`; both currently point at the old file and need retargeting
here (§8).

**The app cannot talk to the user when it isn't open.** There is no
*general-purpose* notification infrastructure: no channel registry, no
`POST_NOTIFICATIONS`, no `NotificationCompat` in any feature code. This is why
the background half of the update flow surfaces nothing on its own today and
waits for the next foreground open to raise a snackbar. Every future feature
that wants to reach a student — exam results, deadlines, timetable changes —
is blocked on the same missing layer.

> **One existing exception.** `VideoPlaybackService` is a media3
> `MediaSessionService`: the library creates its own notification channel,
> posts a playback notification, and runs a `mediaPlayback` foreground service.
> So there is already a channel in system settings that this registry will
> neither own nor be able to group, and there is already in-repo precedent for
> an FGS running with notifications denied. §4.2's "refuses to post to an
> unregistered channel" rule governs *our* channels, not every channel the app
> produces.

---

## 2. Scope

### In this milestone

The **spine** (§4), **foreground-service downloads** (§5), **Live Updates**
(§6), and the **update flow rewired onto it** (§7).

The test of whether the spine is right: every notification type in §10 should
become "define a spec and a trigger", with no new infrastructure.

### Deferred — good ideas, explicitly not now

| Feature | Why later |
|---|---|
| **FCM push** | `firebase-messaging` isn't even a dependency yet (we have config/analytics/crashlytics/perf). Unlocks server-driven admin broadcasts and grade pushes without polling. The Remote Config admin popup is its natural precursor. Wants its own design pass — server side, token lifecycle, payload schema. |
| **Wear OS bridging** | `setLocalOnly(false)` for the few things worth a wrist buzz (lecture reminder), `true` for the rest. Cheap to add *later* precisely because the spec type will already carry the flag — just don't wire or test it now. |

Both are listed so the spec design leaves room for them, not so they get built.

### Rejected — do not implement

- **`RemoteInput` direct reply for Moodle forums**
- **Conversation shortcuts / bubbles**
- **Study-room availability notifications** (Affluences)

Recorded here so nobody re-proposes them in a later review.

---

## 3. Platform constraints that shape the design

These are the reason several decisions below look the way they do.

| Constraint | Consequence |
|---|---|
| **minSdk 25** | `NotificationChannel` is API 26+. The registry needs a no-op path on 25, with importance falling back to `NotificationCompat.PRIORITY_*` on the builder. Do not let channel code crash on 25. |
| **targetSdk 36** (Android 16) | Live Updates / promoted ongoing are available (§6). Also means the strictest FGS rules apply to us. |
| **Notification trampolines banned (Android 12+)** | A tap's `PendingIntent` must launch an Activity **directly**. It may not hit a `BroadcastReceiver`/`Service` that then starts one — the system silently drops it. This dictates §4.5 and the install-tap decision in §7. |
| **`POST_NOTIFICATIONS` runtime permission (Android 13+)** | Must be requested. **A denied permission must never block work** — a foreground service still runs with an invisible notification. Downloads degrade to silent, they do not fail. |
| **FGS types enforced (Android 14+)** | Needs `FOREGROUND_SERVICE_DATA_SYNC` alongside the existing `FOREGROUND_SERVICE`, and `ForegroundInfo` must carry the type. We're sideloaded via GitHub Releases, so there is no Play Console FGS justification review to satisfy. |
| **`dataSync` daily budget (Android 15+)** | Roughly 6 hours of `dataSync` FGS per app per day. Fine for APK downloads; worth remembering before anyone reaches for `dataSync` for long-lived sync. |
| **WorkManager's 10-minute cap does not apply to long-running workers** | A worker that calls `setForeground` is exempt. This is the actual reason a 100 MB download inside a worker is legal, and it is the first objection anyone will raise — so it is written down here. |
| **Channel settings are immutable once created** | Importance/sound cannot be changed in code after first creation — the user owns them. Changing one means a **new channel id**. Version the ids from day one (§4.2). |
| **Exact alarms** (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`) | Only relevant once we schedule lecture/deadline reminders (§10). Not needed this milestone; prefer WorkManager's inexact scheduling wherever a few minutes' drift is acceptable. |

---

## 4. Architecture — the spine

Package: `it.attendance100.mybicocca.core.notification` (pure, testable) with
Android-facing pieces under `data/notification`. Final split to be decided when
writing it; the constraint is that **spec construction must be unit-testable
without a device**.

### 4.1 `NotificationSpec` — one value type

A single data class describing *what* to show, separate from *how* to post it.
Every feature builds one of these; nothing calls `NotificationCompat.Builder`
directly.

Roughly:

```
NotificationSpec(
  channel: NotificationChannelId,      // enum, §4.2
  id: NotificationId,                  // typed, §4.3
  title / text / bigText,
  smallIcon: @DrawableRes,             // monochrome, §4.4
  color, colorized,
  alert: Alert,                        // Every | Once | Never  — §4.6
  ongoing, autoCancel, timeoutAfter,
  progress: Progress?,                 // determinate | indeterminate
  chronometer: Chronometer?,           // countUp/countDown + base
  route: NotificationRoute?,           // deep link, §4.5
  actions: List<NotificationAction>,
  group: GroupKey?,
  localOnly: Boolean = true,           // Wear bridging hook, deferred
)
```

Why a value type rather than a builder DSL: it makes the whole surface
comparable in tests, loggable, and lets the poster centrally enforce the
platform rules (trampoline-safe routing, monochrome icon, permission gating)
instead of trusting ~15 call sites to remember them.

### 4.2 Channel registry — an enum

One enum is the single source of truth: id, name/description string res,
importance, group, and defaults. The poster refuses to post to an unregistered
channel (ours — see §1's media3 note).

Channels for this milestone:

| Enum | Importance | Use |
|---|---|---|
| `UPDATE_PROGRESS` | `LOW` (silent) | "Downloading… 42%", ongoing, Live-Update-promoted |
| `UPDATE_ACTIONABLE` | `DEFAULT` | "Update available", "Ready to install" |

Grouped under an **Updates** channel group; later features add *Didattica*,
*Scadenze*, *Sistema* groups (§10). Channel groups exist so system settings
stays legible once there are a dozen channels.

**Registration** happens in `MyBicoccaApplication.onCreate`, **after** the
`:crash` process guard — the crash-host process must not do main-process boot
work. It must be **idempotent**: `onCreate` can re-run per process, and
`UpdateChecker.start()` already needed a `started` flag for exactly this class
of re-entry.

**Id versioning:** since settings are immutable after creation, ids carry a
suffix (`update_progress_v1`). Changing importance later means bumping to `_v2`
and deleting the old channel. Decide the convention now; retrofitting it is
painful.

### 4.3 Notification ids — typed, not magic ints

A sealed type rather than scattered integer constants:

- **Singleton slots** — `UpdateProgress`, `UpdateReady` — one live instance
  each. These are deliberately **two slots, not one**; §7 explains why the
  progress notification cannot simply become the ready notification.
- **Per-entity slots** — derived from a stable hash of (kind, entity key), e.g.
  per-course or per-exam, so a re-post updates the right one.

Prevents two unrelated features colliding on `id = 1` and silently overwriting
each other — a genuinely nasty class of bug to diagnose later.

### 4.4 Small icon

Status-bar icons are **alpha-only**: any colour is flattened to a white
silhouette. A full-colour drawable renders as a featureless white square.

`res/drawable/logo_mono.xml` already exists and is the obvious default —
**verify it is a solid silhouette, not an outline with white fills**, because
an outline will look correct in the app and wrong in the status bar. The spec
carries a per-notification `smallIcon` override. Add a debug-build assertion
(or at minimum a documented checklist item) catching a non-monochrome drawable,
since the failure is silent and reads as a rendering bug rather than an asset
bug.

### 4.5 Deep-link routing

`NotificationRoute` is a sealed type mapping to the existing navigation, not a
raw `Intent`. The poster converts it into a `PendingIntent` that launches
`MyBicoccaActivity` **directly** with an extra the shell reads on start —
required by the trampoline ban (§3).

First routes: *open the update page in `AppInfoSheet`* and *install a
downloaded APK* (§7). Check `MainShell` / the nav graph for an existing
intent-extra pattern before inventing one.

### 4.6 The poster

One entry point, `post(spec)`, on a class that must **not** be named
`NotificationManager` (collides with the platform class — see §11).
Responsible for:

- **Permission gating** — a central `canNotify(channel)` combining
  `POST_NOTIFICATIONS`, `areNotificationsEnabled()`, and the channel's own
  importance not being `NONE`. Features ask this *before* doing expensive work,
  but it never blocks a download.
- **Alert semantics** — `Alert.Once` maps to `setOnlyAlertOnce(true)` so a
  re-post on the same id updates **silently** (no re-buzz). `Alert.Never` adds
  `setSilent(true)`.
- **Progress throttling** — `downloadToFile` emits on every 1% change, so a
  naive implementation issues up to 100 `notify()` calls on one id. The
  platform rate-limits rapid same-id updates (~5/sec) and starts dropping them.
  In practice only a fast connection with a large APK exceeds that, so this is
  hygiene rather than a live bug — but the rule belongs in the poster, not in
  each caller: **coalesce to at most one update per second, or per 5% change,
  whichever is coarser.**
- **Grouping** — auto-attaching group key + generating the summary notification.
- **Coalescing / rate limiting** — beyond progress: don't fire twelve
  notifications when twelve forum posts sync. Roll up, or drop, per channel
  policy.
- **Cancellation** — `cancel(id)` and `cancelAll(channel)`.

### 4.7 media3's playback channel

`VideoPlaybackService` is a media3 `MediaSessionService`, and media3's
`DefaultMediaNotificationProvider` creates its own channel outside this registry
(`default_channel_id`, named "Now playing"). That makes §4.2's claim — nothing
creates a channel outside the enum — false, and leaves an ungrouped, generically
named channel sitting in system settings next to ours.

**Take over the channel, not the notification.** media3 builds that notification
itself, with the transport controls bound to the `MediaSession`. Reimplementing
`MediaNotification.Provider` to route it through our poster would mean owning
play/pause/seek for no user-visible gain. Owning just the channel is a few lines:

```kotlin
DefaultMediaNotificationProvider.Builder(context)
    .setChannelId(NotificationChannelId.MEDIA_PLAYBACK.id)
    .setChannelName(R.string.notification_channel_media_playback_name)
    .build()
```

handed to `setMediaNotificationProvider()` in `VideoPlaybackService.onCreate`,
with a `MEDIA` group and a `media_playback_v1` channel added to the enum.

**Migration wrinkle:** existing installs already have media3's
`default_channel_id`. Taking over means adding that id to `RETIRED_CHANNEL_IDS` --
the one legitimate case for retiring an id this app didn't create, since we are
replacing the channel rather than deleting a library's live one. Anyone who had
customised the old channel loses that customisation; that is the cost.

Independent of the update flow, so it can land whenever. Keeping it out of the
update flow's own test cycle avoids a media regression muddying that read.

---

## 5. Foreground-service downloads

### Research: how Mihon does it

Investigated by shallow-cloning `github.com/mihonapp/mihon` (2026-08-31).

Their manifest has two FGS entries. `dataSync` is **not** a custom service — it
is the merge tag for `androidx.work.impl.foreground.SystemForegroundService`,
which `work-runtime` ships and auto-registers. Any `CoroutineWorker` calling
`setForeground(ForegroundInfo(...))` runs inside that system service and becomes
freeze/kill-exempt for the duration. **No bespoke `Service` class is needed.**
Their `shortService` entry is a real custom service for installing extension
APKs, unrelated to self-update.

**Their own self-updater is not prior art** — it has the same gap we had: a
bare `viewModelScope.launch`, no foreground service, no notification. What's
worth borrowing is only the mechanism they use for their *other* jobs, plus
this defensive helper:

```kotlin
suspend fun CoroutineWorker.setForegroundSafely() {
    try {
        setForeground(getForegroundInfo())
        delay(0.5.seconds) // let Service.startForeground() land before more work runs
    } catch (e: IllegalStateException) {
        // OS can refuse (background-start restrictions) — degrade to a normal worker
    }
}
```

We already depend on `work-runtime-ktx` and `hilt-work` and already have a
`HiltWorker` (`AppUpdateWorker`), so this is a small incremental addition.

### 5.0 Prerequisite refactor — do this first

**This is the step that makes the rest correct, and it is not optional.**

`ApkDownloader.startDownload` launches on `@ApplicationScope`. A worker that
calls it and then collects `downloadState` is **not the parent of the download
job**. If WorkManager stops the worker — constraint loss, cancellation, quota —
the foreground service is torn down while the download keeps running
unprotected. That is precisely the bug in §1, relocated rather than fixed.

Three separate problems share this one root, and must be fixed together or not
at all:

| Problem | Caused by |
|---|---|
| The worker doesn't own the download's lifetime | `scope.launch` on app scope |
| Two channels can't run concurrently without corrupting each other | one global `_downloadState`, `startDownload` no-ops while `Downloading` |
| Restore-to-stable can't be expressed as a worker input | the release must come from the store, but that path never writes to it (§5.1) |

**The refactor:** expose `suspend fun ApkDownloader.download(release): Result`
that runs **in the caller's coroutine** and owns its per-call state. The
existing `startDownload` becomes a thin app-scope wrapper over it, or is
deleted once every call site goes through the worker. With that in place, the
worker is the real parent, per-call state removes the cross-channel collision,
and the release arrives as a parameter.

**Follow-on to decide (§11):** if downloads become per-call, the singleton
`downloadState` is itself the shared-mutable-state shape that produced the #43
reinstall loop. The UI still needs something to observe, so this needs an
explicit answer — most likely one "current download" state plus per-call
results, rather than a global flow that any number of watchers can act on.

### 5.1 Worker input contract

A worker keyed only on `"stable" | "nightly"` that re-reads `UpdateStateStore`
**cannot express restore-to-stable**, which §5.2 lists as a call site to
switch. Two reasons, both verified:

- `AppInfoSheet`'s restore path downloads
  `UpdateCheckResult.UpdateAvailable.release` from
  `UpdateRepositoryImpl.getLatestStableRelease()`, which is a plain GitHub
  fetch that **never writes to `UpdateStateStore`**.
- Restore-to-stable is a *downgrade*, so the stable slot is normally
  `available == false` and `availableRelease()` returns null anyway.

So a store-reading worker would silently do nothing on that path.

**Resolution:** the input carries either a channel *or* an explicit release.
Preferred: persist the requested release under its own key
(`pendingDownloadRelease`) and pass a discriminator, keeping WorkManager `Data`
small; serializing the `AppRelease` into `Data` is acceptable if simpler, in
which case the "not a serialized `AppRelease`" preference stated for the
channel-driven path is explicitly waived here.

### 5.2 The worker

`ApkDownloadWorker`:

- Resolves its release per §5.1.
- Calls `setForegroundSafely()` immediately, `getForegroundInfo()` returning
  the progress notification with `FOREGROUND_SERVICE_TYPE_DATA_SYNC`.
- Awaits `ApkDownloader.download(release)` **in its own scope** (§5.0), so the
  FGS covers exactly the download's lifetime.
- Drives the progress notification from the download's own progress, throttled
  per §4.6.
- On success, posts the terminal notification (§7).

**Single-flight:** use `enqueueUniqueWork` with **one global key**, not a
per-channel key. Per-channel keys would let stable and nightly run
concurrently, and against today's singleton downloader the second would
download nothing while mirroring the first's bytes and terminal state — posting
"ready to install" for the wrong APK. If §5.0's follow-on makes the downloader
genuinely per-request, per-channel keys become viable; until then, one key.
`ExistingWorkPolicy.KEEP`.

**Cancellation:** the progress notification carries a **Cancel** action. This
is not optional polish — an FGS notification is not swipe-dismissable, so
without it the user has no way out. Cancel means `cancelUniqueWork` **plus**
`resetState()`, not just flipping the flow.

### 5.3 Call-site migration

**Every existing caller of `startDownload` must enqueue this worker** — the
repro is a *manually tapped* download that gets backgrounded, so the
interactive paths are the point, not an afterthought. Call sites:
`UpdateModalSheet`'s `onDownload` (via both `MainShell` and `AppInfoSheet`),
the restore-to-stable handler, `MainShell`'s two auto-download effects, and
`AppUpdateWorker`.

**`AppUpdateWorker` has a semantics consequence, not just a style choice.** If
it *enqueues* `ApkDownloadWorker` rather than inlining the foreground
promotion, it stops awaiting the download, and its `Result.success()` no longer
means "downloaded" — only "check ran, download scheduled". Anything reasoning
about that result must be reviewed. Inlining keeps today's meaning. Decide
deliberately (§11).

### 5.4 Manifest changes

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />

<service
    android:name="androidx.work.impl.foreground.SystemForegroundService"
    android:foregroundServiceType="dataSync"
    tools:node="merge" />
```

The manifest today has only `FOREGROUND_SERVICE` and
`FOREGROUND_SERVICE_MEDIA_PLAYBACK`. `POST_NOTIFICATIONS` is also absent and
must be added.

**Verify before relying on it:** `work-runtime-ktx` is pinned at **2.9.1**
against compileSdk 37 / targetSdk 36. This is not an assertion that it's
broken — it is untested here — but `setForeground`'s FGS-type handling across
API 34–36 should be confirmed early, because a version bump would be a
prerequisite rather than a cleanup.

### 5.5 Edge cases

- **`POST_NOTIFICATIONS` denied** → FGS still runs; the notification is merely
  invisible. Never gate the download on it. (media3 already relies on this.)
- **`setForeground` throws `IllegalStateException`** → degrade to a normal
  worker run; log, don't crash.
- **Download completes while the app is dead** → already handled by #43's
  persistence (`UpdateStateStore.downloadedApk` +
  `ApkDownloader.restorePendingDownload`). The notification is the *other* way
  the user learns about it. Note `restorePendingDownload` runs from
  `UpdateChecker.start()` and is async, so a notification tap that goes
  straight to install must not race it (§7).

---

## 6. Live Updates (Android 16) & Samsung Now Bar

Android 16 adds `Notification.ProgressStyle` and
`setRequestPromotedOngoing(true)`, which promote an ongoing notification into
the status-bar chip and a richer lock-screen presentation.

APK download progress is precisely the intended use case, so this rides along
with §5 at near-zero extra cost once `NotificationSpec.progress` exists. Gate it
behind an API-36 check and let it degrade to an ordinary progress notification
below that — which is the majority of installs today.

**Samsung Now Bar** (One UI 7+) surfaces promoted ongoing notifications, so it
should largely come for free. Treat that as *unverified* until checked on real
Samsung hardware — OEM behaviour here varies and is poorly documented. Do not
advertise it as supported on the strength of the standard API alone.

Later Live Update candidates (not now): time-until-next-lecture, attendance
check-in window.

---

## 7. The update flow as first consumer

**This replaces the old plan's "Desired UX" section entirely.** Installs are
always user-confirmed now; there is no silent path to describe. Both channels
behave identically — that symmetry was the point of the #43 rework, and the
notification layer must not reintroduce a split.

### 7.1 What triggers "Update available"

**Not** a second collector on `newUpdateEvents`. That flow is
`Channel<AppRelease>(Channel.BUFFERED).receiveAsFlow()` — **single-consumer**.
A collector added for notifications would *steal* events from `MainShell`'s
collector rather than duplicating them; the two would race per event and the
snackbar would intermittently vanish.

Two acceptable options:

1. Fire the notification at the **discovery site** inside
   `checkForUpdates`, next to `setLastNotifiedVersion` — same place that
   decides an event is worth announcing.
2. Convert `_events`/`_nightlyEvents` to a `SharedFlow` first, then collect
   freely.

(1) is smaller; (2) is better if anything else ever needs to observe
discoveries. Either way, **do not** just add a collector.

### 7.2 Flow

**Auto-download off:**
1. Update found → **"Update available"** (`UPDATE_ACTIONABLE`, id
   `UpdateAvailable`).
2. Tap → opens the in-app update page (§4.5). Existing in-app behaviour must
   not regress: a user already inside the app still gets the snackbar and modal.
3. User taps Download → progress notification (`UPDATE_PROGRESS`, id
   `UpdateProgress`), FGS-backed, with a Cancel action.
4. On success → **cancel the progress notification and post `UpdateReady`
   fresh.** See §7.3.
5. Tap → the system package-installer dialog. See §7.4.

**Auto-download on:** discovery goes straight to step 3. "Update available" is
**not posted at all** — not posted-then-cancelled — since the download starting
on its own is the announcement.

### 7.3 Why progress cannot become "Ready to install"

An earlier draft said the progress notification updates in place to "Ready to
install" on the same id. It can't, for two reasons:

- The progress notification's lifetime belongs to WorkManager: the
  `ForegroundInfo` notification is **cancelled when the worker completes**, so
  there is nothing left to morph.
- §4.3 declares `UpdateProgress` and `UpdateReady` as two separate singleton
  slots, so the same-id claim contradicted this document.

(A third objection — that re-posting an id under a different channel is
unsupported — was **wrong** and is dropped: the platform does move the
notification to the new channel. It is simply a bad idea here.)

Also, the stated goal of "updating without buzzing" was itself questionable:
"your update is ready" is exactly the one buzz worth having. `Alert.Once`
protects **re-posts of the ready notification**, not the progress→ready
transition.

### 7.4 The install tap — pick one, and accept the consequence

A notification tap cannot run code, and the trampoline ban (§3) forbids
routing it through a receiver. So "tap → `installApk(file)`" is not
implementable as written. Two real options:

| Option | Trade-off |
|---|---|
| **A.** `PendingIntent.getActivity` aimed straight at the installer `ACTION_VIEW` intent | Trampoline-safe and simple, but bypasses `installApk`, so `pendingInstall`/`leftForInstaller` are never set and **`InstallDeclined` detection silently stops working**. It cannot work at all with the process dead, since detection rides on `ProcessLifecycleOwner` via `UpdateChecker`. |
| **B.** Route into `MyBicoccaActivity` with an extra; the shell calls `installApk` | Keeps decline detection intact, costs a brief screen flash. Must not race `restorePendingDownload` (§5.5). |

**Recommendation: B.** §7.5 promises decline handling works, and A quietly
breaks it — which is the kind of regression that looks like a device quirk for
a week.

### 7.5 Terminal states

- The install step is **always** the system dialog. `installApk` already
  carries the `NEW_TASK or CLEAR_TASK` flags that stop a tap being swallowed by
  a closing installer task.
- A **declined** install keeps the APK (`DownloadState.InstallDeclined`). The
  `UpdateReady` notification should persist rather than vanish, and must not
  re-alert (`Alert.Once`). This also closes a known gap: the cancelled state
  currently surfaces only inside the modal, so a decline from the snackbar
  gives no feedback at all.
- **A successful install must cancel `UpdateReady`.** Install kills the
  process, so nothing gets to clean up: without this, a stale "Ready to
  install" notification survives, pointing at an APK for the build now running.
  `isRunningBuild()` on next start is the natural place — the same
  reconciliation that already stops the download path re-offering it.
- Nothing re-offers a build the user is already running; the notification path
  must use `isRunningBuild()` too.

### 7.6 Foreground suppression

`MainShell` already raises a snackbar on discovery *and* another on `Success`.
Without a policy, a user with the app open gets snackbar **and** notification
for the same event.

Decide explicitly whether `UPDATE_ACTIONABLE` is suppressed while the process
is foregrounded — `ProcessLifecycleOwner` is already observed by
`UpdateChecker`, so the signal exists. Note the `UPDATE_PROGRESS` FGS
notification **cannot** be suppressed regardless; that one is the price of the
foreground service.

---

## 8. Implementation order

1. **Channels + permission plumbing.** Enum registry (idempotent, post-`:crash`
   guard), `POST_NOTIFICATIONS` request with rationale, `canNotify`.
2. **`NotificationSpec` + poster**, with unit tests over spec construction,
   alert semantics, throttling and permission logic.
3. **Deep-link routing** — one route, proving the trampoline-safe path end to
   end.
4. **§5.0 prerequisite refactor** — `ApkDownloader.download(release)` as a
   suspend function in the caller's scope. **Nothing below is correct without
   this.**
5. **`ApkDownloadWorker`** with `setForeground` + progress + Cancel, wired to
   the *periodic* path first — lowest risk, already backgrounded by definition.
   Includes the manifest changes and the work-runtime version check (§5.4).
6. **Switch the interactive call sites** (§5.3), including restore-to-stable
   via §5.1. This is the step that fixes the original bug.
7. **Terminal notifications** — trigger site (§7.1), `UpdateReady`, install tap
   option B (§7.4), decline handling and post-install cleanup (§7.5).
8. **Live Updates** promotion on the progress notification (API 36 gate).
9. **Foreground suppression policy** (§7.6).
10. **Debug screen** (§9).
11. **media3 channel takeover** (§4.7) — independent of everything above; keep it
    out of the update flow's test cycle.
12. **Housekeeping:** retarget the two `TODO(update-notifications)` markers from
    `/UPDATE_NOTIFICATIONS_PLAN.md` to this file, and delete the old file.

Steps 1–3 are the reusable spine; 4–9 are the first consumer.

---

## 9. Testing

- **Unit** — spec construction, alert semantics, progress throttling, id
  allocation, `canNotify` under each permission/channel combination. All
  device-free.
- **Debug screen firing one of every notification type.** Worth building early:
  the update bugs found during #43 testing each needed a full CI nightly plus an
  install to reproduce. A screen that posts any spec on demand collapses that
  loop to seconds.
- **Manual matrix**: download while backgrounded (the original repro),
  auto-download on and off, restore-to-stable (the §5.1 path), notification
  permission denied, Cancel action, install declined, install succeeded (stale
  `UpdateReady` must not survive), app foregrounded during discovery (§7.6),
  API 25 (no channels), API 36 (Live Update), and a real Samsung for Now Bar.

---

## 10. Future notification types — catalogue, not commitments

Once the spine exists, each of these is a spec plus a trigger. Roughly ordered
by expected student value:

1. **Exam results published** (Esse3) — likely the single highest-value
   notification this app could send.
2. **Exam registration opens / closes tomorrow** — hard deadlines with real
   consequences.
3. **Lecture reminders and timetable changes** — "next lecture in 20 min,
   U6-01" / "cancelled". Needs scheduling (§3, exact alarms) and configurable
   lead time.
4. **Payment deadlines** (pagoPA/taxes).
5. **Moodle activity** — new material, assignment due dates. *(Forum
   direct-reply is rejected, §2.)*
6. **Attendance window open** — pairs with the existing QR check-in.
7. **App updates** — §7, this milestone.
8. **Admin broadcasts** — Remote Config today, FCM later (§2).
9. **Session expiry / re-auth needed** — `AccountEvent.RequireReauth` already
   exists and only raises a snackbar; it deserves a notification when
   backgrounded.

Supporting user controls to design alongside these: per-category in-app
toggles mirroring channels, app-level **quiet hours** (Android's DND is
all-or-nothing per channel), per-course muting, configurable reminder lead
time, and an **in-app notification centre** backed by Room — the tray is
ephemeral and students will swipe away a grade notification.

---

## 11. Open questions

- **What replaces the singleton `downloadState`** once downloads are per-call
  (§5.0). This is the largest one: the current shape is what produced the #43
  reinstall loop.
- Whether `AppUpdateWorker` enqueues `ApkDownloadWorker` or inlines the
  foreground promotion — a **semantics** decision, since enqueuing changes what
  its `Result.success()` means (§5.3).
- §5.1: persist the requested release under its own key, or serialize it into
  WorkManager `Data`.
- §7.1: trigger at the discovery site, or convert the event channels to
  `SharedFlow`.
- §7.6: suppress `UPDATE_ACTIONABLE` while foregrounded, or always post.
- Package split between `core/notification` and `data/notification`.
- Naming for the poster, avoiding the platform `NotificationManager`.
- Whether the in-app notification centre lands with the spine or later — it
  changes whether the poster writes to Room on every post.
- Whether swiping the progress notification cancels the download (default: no;
  note the Cancel action in §5.2 is the supported route).

---

## 12. Revision history

**Rev 3** — during implementation of step 1.

- **§4.7 added.** media3 creating its own channel was recorded in rev 2 as an
  accepted exception; taking the channel over instead is cheap and makes 4.2's
  "nothing creates a channel outside this enum" actually true. The notification
  itself stays media3's.
- `UPDATE_PACKAGES_WITHOUT_USER_ACTION` removed from the manifest: it existed for
  the silent `PackageInstaller` path deleted before #43, and nothing has used it
  since. `REQUEST_INSTALL_PACKAGES` is still required by `installApk`'s
  `ACTION_VIEW`.
- §4.4 resolved: `logo_mono.xml` is **not** usable as a small icon. It is a 108dp
  launcher asset whose mark is knocked out of a filled square, so it would flatten
  to a solid block in the status bar, and its other path is stroke-only. The
  status-bar glyph is `res/drawable/notification.xml`.

**Rev 2** — after review against the codebase. Changes worth knowing if you
read rev 1:

- **§5.0 added.** The original "worker owns lifetime, `ApkDownloader` owns
  mechanics" split was not achievable by delegating to `startDownload`, which
  launches on `@ApplicationScope`. Without the refactor the FGS and the
  download have independent lifetimes — the original bug, relocated.
- **§5.2 single-flight corrected.** Rev 1 said dedupe per channel; against one
  global `_downloadState` that permits two workers where the second reports the
  first's progress and terminal state.
- **§5.1 added.** Rev 1's channel-only worker input could not express
  restore-to-stable, which rev 1 simultaneously listed as a call site to
  migrate.
- **§7.3 added.** Rev 1 had progress morph into "ready to install" on one id;
  impossible (WorkManager cancels the FGS notification on completion) and in
  conflict with §4.3.
- **§7.4 added.** Rev 1's "tap → `installApk(file)`" is not implementable under
  the trampoline ban rev 1 itself documents, and the obvious workaround
  silently disables decline detection.
- **§7.1 added.** Rev 1 never said what triggers the notification; the obvious
  wiring (collect `newUpdateEvents`) steals events from `MainShell`.
- Smaller: media3's pre-existing channel (§1), the 10-minute-cap exemption
  (§3), idempotent registration (§4.2), `logo_mono.xml` (§4.4), progress
  throttling (§4.6), manifest specifics and the work-runtime check (§5.4),
  Cancel action (§5.2), `Result.success()` semantics (§5.3), post-install
  cleanup of `UpdateReady` (§7.5), foreground suppression (§7.6), and TODO
  retargeting (§8).
