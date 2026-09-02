# Notifications Manager — Plan

**Branch:** `notifications` (off `main` at `b1454346`)
**Status:** design agreed, not implemented.

This supersedes the earlier local-only `UPDATE_NOTIFICATIONS_PLAN.md`. That
file predates the nightly-updates PR and describes a silent auto-install flow
(`installSilently`, `InstallResultReceiver`, `nightlyAutoInstall`,
`shouldRunFullyUnattended`) that **no longer exists** — all of it was deleted
before #43 merged. Its durable parts (the root-cause evidence, the Mihon
research, the foreground-service mechanics) are carried over below, rewritten
against the current architecture. The old file can be deleted.

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
`AppUpdateWorker` pointing here.

**The app cannot talk to the user when it isn't open.** There is currently
*zero* notification infrastructure: no channel, no `POST_NOTIFICATIONS`, no
`NotificationCompat` anywhere in `app/src/main`. This is why the background
half of the update flow surfaces nothing on its own today and simply waits for
the next foreground open to raise a snackbar. Every future feature that wants
to reach a student — exam results, deadlines, timetable changes — is blocked
on the same missing layer.

The manager is therefore built as **general infrastructure**, with the update
flow as its first consumer, not as an update-specific helper.

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
| **Notification trampolines banned (Android 12+)** | A tap's `PendingIntent` must launch an Activity **directly**. It may not hit a `BroadcastReceiver`/`Service` that then starts one — the system silently drops it. This dictates the routing design in §4.5. |
| **`POST_NOTIFICATIONS` runtime permission (Android 13+)** | Must be requested. **A denied permission must never block work** — a foreground service still runs with an invisible notification. Downloads degrade to silent, they do not fail. |
| **FGS types enforced (Android 14+)** | Needs `FOREGROUND_SERVICE_DATA_SYNC` alongside the existing `FOREGROUND_SERVICE`. We're sideloaded via GitHub Releases, so there is no Play Console FGS justification review to satisfy. |
| **`dataSync` daily budget (Android 15+)** | Roughly 6 hours of `dataSync` FGS per app per day. Fine for APK downloads; worth remembering before anyone reaches for `dataSync` for long-lived sync. |
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
importance, group, and defaults. Registered once at app start; the poster
refuses to post to an unregistered channel.

Channels for this milestone:

| Enum | Importance | Use |
|---|---|---|
| `UPDATE_PROGRESS` | `LOW` (silent) | "Downloading… 42%", ongoing, Live-Update-promoted |
| `UPDATE_ACTIONABLE` | `DEFAULT` | "Update available", "Ready to install" |

Grouped under an **Updates** channel group; later features add *Didattica*,
*Scadenze*, *Sistema* groups (§10). Channel groups exist so system settings
stays legible once there are a dozen channels.

**Id versioning:** since settings are immutable after creation, ids carry a
suffix (`update_progress_v1`). Changing importance later means bumping to `_v2`
and deleting the old channel. Decide the convention now; retrofitting it is
painful.

### 4.3 Notification ids — typed, not magic ints

A sealed type rather than scattered integer constants:

- **Singleton slots** — `UpdateProgress`, `UpdateReady` — one live instance each.
- **Per-entity slots** — derived from a stable hash of (kind, entity key), e.g.
  per-course or per-exam, so a re-post updates the right one.

Prevents two unrelated features colliding on `id = 1` and silently overwriting
each other — a genuinely nasty class of bug to diagnose later.

### 4.4 Small icon

Status-bar icons are **alpha-only**: any colour is flattened to a white
silhouette. A full-colour drawable renders as a featureless white square.

The spec carries a per-notification `smallIcon`, defaulting to a monochrome app
glyph. Add a debug-build assertion (or at minimum a documented checklist item)
that catches a non-monochrome drawable, because the failure is silent and looks
like a rendering bug rather than an asset bug.

### 4.5 Deep-link routing

`NotificationRoute` is a sealed type mapping to the existing navigation, not a
raw `Intent`. The poster converts it into a `PendingIntent` that launches
`MyBicoccaActivity` **directly** with an extra the shell reads on start —
required by the trampoline ban (§3).

First routes: *open the update page in `AppInfoSheet`* (same destination the
in-app update tile opens today) and *open What's New*.

Check `MainShell` / the nav graph for an existing intent-extra pattern before
inventing one.

### 4.6 The poster

One entry point, `NotificationManager.post(spec)` (name TBD — must not collide
with the platform class), responsible for:

- **Permission gating** — a central `canNotify(channel)` combining
  `POST_NOTIFICATIONS`, `areNotificationsEnabled()`, and the channel's own
  importance not being `NONE`. Features ask this *before* doing expensive work,
  but it never blocks a download.
- **Alert semantics** — `Alert.Once` maps to `setOnlyAlertOnce(true)` so a
  re-post on the same id updates **silently** (no re-buzz). This is exactly what
  progress ticks and the "downloading → downloaded" transition need.
  `Alert.Never` adds `setSilent(true)`.
- **Grouping** — auto-attaching group key + generating the summary notification.
- **Coalescing / rate limiting** — do not fire twelve notifications when twelve
  forum posts sync. Roll up, or drop, per channel policy.
- **Cancellation** — `cancel(id)` and `cancelAll(channel)`.

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
`HiltWorker` (`AppUpdateWorker`), so this is a small incremental addition rather
than a new subsystem.

### Decision

Add a `CoroutineWorker` (`ApkDownloadWorker`) that:

- Takes a **channel string** (`"stable" | "nightly"`) as input, not a
  serialized `AppRelease` — it re-reads the persisted release from
  `UpdateStateStore`, exactly as `AppUpdateWorker` already does.
- Calls the `setForegroundSafely` equivalent immediately, with
  `getForegroundInfo()` returning the progress notification (`dataSync`).
- **Delegates to `ApkDownloader`'s existing logic** — reuse `downloadToFile`,
  the integrity check, and the `DownloadState` flow. Do not duplicate download
  code; the worker owns *lifetime*, `ApkDownloader` owns *mechanics*.
- Collects `downloadState` to keep the notification's progress in sync, and
  posts the terminal notification on `Success`.

**Every existing caller of `startDownload` must enqueue this worker instead** —
that is what actually fixes the reported bug, since the repro is a *manually
tapped* download that gets backgrounded. Call sites: `UpdateModalSheet`'s
`onDownload` (via both `MainShell` and `AppInfoSheet`), the restore-to-stable
handler, `MainShell`'s two auto-download effects, and `AppUpdateWorker` itself.

For `AppUpdateWorker`, it is an open call whether to enqueue the new worker or
inline the same foreground promotion — it is already a worker that has just
decided a download should happen, so double-hopping may be gratuitous. Decide
when writing it.

### Edge cases

- **`POST_NOTIFICATIONS` denied** → FGS still runs; the notification is merely
  invisible. Never gate the download on it.
- **`setForeground` throws `IllegalStateException`** (background-start
  restrictions) → degrade to a normal worker run; log, don't crash.
- **Concurrent triggers** — `ApkDownloader.startDownload` already no-ops while
  `Downloading`. Preserve that by deduping with `enqueueUniqueWork` keyed by
  channel, `ExistingWorkPolicy.KEEP`.
- **User swipes the progress notification** — Android normally prevents
  swipe-dismiss on an active FGS notification. Decide explicitly whether
  dismissal should cancel the download (default: no).
- **Download completes while the app is dead** — already handled by the
  persistence added in #43 (`UpdateStateStore.downloadedApk` +
  `ApkDownloader.restorePendingDownload`); the notification is now the *other*
  way the user learns about it.

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
always user-confirmed now; there is no silent path to describe.

Both channels behave identically — that symmetry was the whole point of the
#43 rework, and the notification layer must not reintroduce a split.

**Auto-download off:**
1. Update found → **"Update available"** notification (`UPDATE_ACTIONABLE`).
2. Tap → opens the in-app update page (§4.5). Existing in-app behaviour must
   not regress: a user already inside the app still gets the snackbar and the
   modal.
3. User taps Download → progress notification, foreground-service backed.
4. On success → notification becomes **"Ready to install"** (same id,
   `Alert.Once`, so it updates without buzzing).
5. Tap → `ApkDownloader.installApk(file)` → the OS package installer dialog.

**Auto-download on:** identical, minus steps 2–3 — discovery goes straight to
the progress notification.

Notes:
- The install step is **always** the system dialog. `installApk` already
  handles the `NEW_TASK or CLEAR_TASK` flags that stop a tap being swallowed by
  a closing installer task.
- A **declined** install keeps the APK (`DownloadState.InstallDeclined`). The
  "Ready to install" notification should persist rather than vanish, and must
  not re-alert. This also closes a known gap: the cancelled state currently only
  shows inside the modal, so a decline from the snackbar gives no feedback.
- Nothing re-offers a build the user is already running — `isRunningBuild()`
  is the shared check, and the notification path must use it too.

---

## 8. Implementation order

1. **Channels + permission plumbing.** Enum registry, `POST_NOTIFICATIONS`
   request with rationale, `canNotify`. Nothing depends on downloads yet.
2. **`NotificationSpec` + poster**, with unit tests over spec construction and
   alert/permission logic.
3. **Deep-link routing** — one route (the update page), proving the
   trampoline-safe path end to end.
4. **`ApkDownloadWorker`** with `setForeground` + progress, wired to the
   *periodic* path first — lowest risk, already backgrounded by definition.
5. **Switch the interactive call sites.** This is the step that fixes the
   original bug.
6. **Terminal notifications** — "Ready to install", decline handling.
7. **Live Updates** promotion on the progress notification (API 36 gate).
8. **Debug screen** (§9).

Steps 1–3 are the reusable spine; 4–7 are the first consumer.

---

## 9. Testing

- **Unit** — spec construction, alert semantics, id allocation, `canNotify`
  under each permission/channel combination. All device-free.
- **Debug screen firing one of every notification type.** Worth building early:
  the update bugs found during #43 testing each needed a full CI nightly plus an
  install to reproduce. A screen that posts any spec on demand collapses that
  loop to seconds.
- **Manual matrix**: download while backgrounded (the original repro),
  auto-download on and off, notification permission denied, progress
  notification swiped, install declined, API 25 (no channels), API 36 (Live
  Update), and a real Samsung device for Now Bar.

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

- Package split between `core/notification` and `data/notification`.
- Whether `AppUpdateWorker` enqueues `ApkDownloadWorker` or inlines the
  foreground promotion (§5).
- Whether swiping the progress notification cancels the download (default: no).
- Naming, to avoid colliding with the platform `NotificationManager`.
- Whether the in-app notification centre lands with the spine or later — it
  changes whether the poster writes to Room on every post.
