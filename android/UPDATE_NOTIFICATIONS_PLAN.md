# Update Notifications & Foreground-Service Downloads — Plan

**Status: not implemented. Do not start on this until after the nightlies PR
has merged and settled.** This file is the detailed design so that work can
pick up cold later, without re-deriving the investigation below.

## The problem this solves

Confirmed via logcat on 2026-08-31: a download started from the UI, then the
app backgrounded, dies a few seconds later with `SocketException: Software
caused connection abort`. Root cause: nothing currently keeps the process
alive/unfrozen once the app leaves the foreground. `ApkDownloader.startDownload`
(`app/src/main/java/it/attendance100/mybicocca/data/update/ApkDownloader.kt:54`)
just does `scope.launch { ... }` on `@ApplicationScope` — a plain coroutine,
with no foreground-service promotion. This affects *every* caller of
`startDownload`: the manual "Download" tap in `UpdateModalSheet`, the
"restore to stable" flow in `AppInfoSheet.kt`, `MainShell.kt`'s auto-download
`LaunchedEffect`s, and `AppUpdateWorker.doWork()` itself (the periodic
background check can auto-download per settings, but that download call is
just as unprotected as the interactive one).

Separately (already fixed, see git history for `UpdateChecker.kt`): the
periodic `AppUpdateWorker` was scheduled every 12 hours, not the 30 minutes
the nightly-check TTL slot in `UpdateRepositoryImpl.kt` assumes, and used
`KEEP` instead of `UPDATE` so already-installed builds wouldn't even pick up
a corrected interval. That part is a separate, already-resolved issue — this
doc is only about what happens *after* an update is known and a download
needs to survive backgrounding, plus the notification UX around it.

## Research: how Mihon does it

Investigated by shallow-cloning `github.com/mihonapp/mihon` and grepping
locally (2026-08-31). Two separate things exist in their manifest with
`foregroundServiceType`:

- `dataSync` — this is **not a custom service**. It's the merge tag for
  `androidx.work.impl.foreground.SystemForegroundService`, which
  `androidx.work:work-runtime` ships and auto-registers. Any `CoroutineWorker`
  can call `setForeground(ForegroundInfo(...))` to run inside that system
  service and become freeze/kill-exempt for the duration — no bespoke
  `Service` class needed. Mihon uses this for `LibraryUpdateJob`,
  `BackupCreateJob`, `BackupRestoreJob`, `MetadataUpdateJob`, `DownloadJob` —
  i.e. their various background sync/download jobs, all `CoroutineWorker`s.
- `shortService` — a real custom service, `ExtensionInstallService`. This is
  for installing/uninstalling *extensions* (their manga-source plugin APKs),
  a short (~seconds) operation. Not related to downloading their own app
  update.

**Their actual core-app self-updater does neither.** `AppUpdateChecker` runs
once per `MainActivity` launch (`LaunchedEffect(Unit)`, foreground-only, no
periodic worker at all). On finding an update it pushes a Compose screen,
`NewUpdateScreen`/`NewUpdateScreenModel`
(`app/src/main/java/eu/kanade/tachiyomi/ui/more/NewUpdateScreenModel.kt`),
whose `startDownload()` is a bare `viewModelScope.launch { ... }` — no
foreground service, no notification, no silent install ever (`installUpdate()`
always fires `ACTION_VIEW`, even for their nightly/"preview" build — verified
no branching on `isNightlyBuildType` in that path). If the user backgrounds
mid-download there, it very likely breaks the same way ours did; they just
never built anything to prevent it.

**Conclusion:** Mihon is not prior art for "survive backgrounding" — their own
updater has the same gap. What *is* worth borrowing is the mechanism they use
for their other background jobs: `CoroutineWorker.setForeground()`, not a
hand-rolled `Service`. This project already depends on
`androidx.work:work-runtime-ktx:2.9.1` and `androidx.hilt:hilt-work:1.2.0`,
and already has one `HiltWorker` (`AppUpdateWorker`), so this is a small
incremental addition, not a new subsystem from scratch. Mihon's helper
(for reference, not copied verbatim):

```kotlin
// eu.kanade.tachiyomi.util.system.WorkManagerExtensions.kt
suspend fun CoroutineWorker.setForegroundSafely() {
    try {
        setForeground(getForegroundInfo())
        delay(0.5.seconds) // let Service.startForeground() land before more work runs
    } catch (e: IllegalStateException) {
        // OS can refuse (background-start restrictions) — degrade to a normal background worker
    }
}
```

## Desired UX (as specified by the user)

### Stable releases

1. An update is found (periodic worker or foreground on-open check).
2. Post a notification: **"Update available"**.
3. Tapping it starts the download (foreground-service-backed), which shows a
   **silent** (no sound/vibration, low-importance channel), ongoing progress
   notification — "Downloading update… 42%".
4. On success, that notification is replaced by **"Update ready to install"**.
5. Tapping it launches the normal OS package-installer confirmation dialog
   (`ACTION_VIEW`, not silent — matches existing stable behavior, stable
   never silent-installs today either).
6. If the user is already inside the app when the update is found/available,
   they should land on the actual in-app download page (the existing
   `AppInfoSheet` update tile / `UpdateModalSheet`) rather than only a
   notification — that part is already implemented today and shouldn't
   regress.

### Nightly (beta) releases

Same shape as stable **except**: if the user has *both* "auto-download beta
updates" and "auto-install beta updates" enabled
(`nightlyAutoDownload && nightlyAutoInstall` — the existing gate already used
in `MainShell.kt:602`), skip the "tap to start"/"tap to install" steps
entirely:

1. Update found → straight to the silent progress notification (no "tap to
   download" step — it starts on its own).
2. On download success, silently install via the existing
   `ApkDownloader.installSilently` / `PackageInstaller` session path
   (`ApkDownloader.kt:213-258`, already implemented, `USER_ACTION_NOT_REQUIRED`
   on API 31+). Show the progress notification as "Installing…" for the
   installer's duration (a few seconds).
3. On install success (`InstallResultReceiver` → `ApkDownloader.onInstallResult`,
   `ApkDownloader.kt:260-267`, already implemented but currently only resets
   `_downloadState` — needs to also trigger the notification), post a final,
   dismissible **"Nightly updated to vX"** notification.

If only one of the two nightly settings is on (or neither), nightly follows
the same tap-driven flow as stable, just routed to the silent installer
instead of the confirmation dialog when the user does tap install (this part
already matches existing behavior — `release.isPreRelease` already picks
`silent = true` wherever install is invoked today).

## Technical plan

### 1. Permissions & manifest

- `POST_NOTIFICATIONS` — request at runtime (Android 13+). This app has
  **zero** existing notification infrastructure (no channel, no permission
  request anywhere) — this is genuinely new plumbing, not a tweak. Foreground
  service still functions if this is denied; the notification is just
  invisible, so this should degrade gracefully, not block the download.
- `FOREGROUND_SERVICE_DATA_SYNC` — required (Android 14+ enforces per-type
  foreground-service permissions). Since this app is sideloaded via GitHub
  Releases (not Play-distributed), there's no Play Console foreground-service
  justification review to worry about.
- No new `<service>` entry needed beyond what `work-runtime` auto-merges
  (`SystemForegroundService`, `dataSync`) — confirmed this is how Mihon's
  manifest gets that entry too, not a hand-written one.

### 2. Notification channels (new, e.g. `UpdateNotifications.kt`)

Two channels:
- `updates_progress` — `IMPORTANCE_LOW`, silent, ongoing/non-dismissible
  while active. Used for "Downloading… X%" and "Installing…".
- `updates_actionable` — `IMPORTANCE_DEFAULT`. Used for "Update available",
  "Ready to install", "Nightly updated to vX".

### 3. Download mechanism — route everything through WorkManager

**Key decision:** rather than a bespoke `Service`, add a small
`CoroutineWorker` (e.g. `ApkDownloadWorker`) that:
- Takes a single string input, `channel: "stable" | "nightly"` (not the
  `AppRelease` itself — avoids serializing it through WorkManager `Data`;
  instead re-reads the currently-persisted release for that channel from
  `UpdateStateStore`, exactly like `AppUpdateWorker.doWork()` already does
  today for `stableState.release` / `nightlyState.release`).
- Calls `setForegroundSafely()`-equivalent immediately, with
  `getForegroundInfo()` returning the "Downloading…" notification
  (`dataSync` type).
- Delegates to `ApkDownloader`'s existing download logic (`downloadToFile`
  etc. — reuse as-is, don't duplicate) and collects `downloadState` to keep
  the foreground notification's progress in sync.
- On success: for nightly with both settings on, calls
  `installApk(file, silent = true)` and updates the notification through
  "Installing…" → "Nightly updated to vX" (via the `InstallResultReceiver`
  callback). Otherwise posts "Ready to install" and stops the foreground
  state, leaving a plain (non-progress) notification behind for the user to
  tap later.

**All existing call sites of `ApkDownloader.startDownload` should enqueue this
worker instead of calling it directly** — that's what actually fixes the
user's repro (a manually-tapped download that gets backgrounded), not just
the already-automatic periodic-worker path. Concretely:
- `AppInfoSheet.kt` — `UpdateModalSheet`'s `onDownload` callback, and the
  "restore to stable" confirm handler.
- `MainShell.kt:549-596` — the `events`/`nightlyEvents` `LaunchedEffect`s and
  the modal's `onDownload`.
- `AppUpdateWorker.doWork()` — instead of calling `apkDownloader.startDownload`
  directly (`AppUpdateWorker.kt:34`, `:51`), enqueue the same one-time
  `ApkDownloadWorker` (or inline the same foreground-promotion logic directly
  into `AppUpdateWorker` itself, since it's already a periodic worker and
  could just do both jobs — simpler than always double-hopping through a
  second worker when it's the one that already decided a download should
  happen. Pick whichever reads cleaner once actually writing it; documented
  here as an open call, not a hard requirement).

The two existing `// TODO: Implement System Notification Manager to notify
user of ready update` markers in `AppUpdateWorker.kt` (lines ~39 and ~62) are
exactly the two spots this plan fills in.

### 4. Notification tap targets (`PendingIntent`s)

- "Update available" → opens `MainActivity` and routes into
  `AppInfoSheet`'s update page (same destination the existing in-app tile
  already opens) — reuse whatever deep-link/intent-extra mechanism is
  idiomatic in this codebase's navigation (check `MainShell.kt`/nav graph for
  an existing pattern before inventing one).
- "Ready to install" → for stable, `ACTION_VIEW` on the APK
  (`installWithSystemUi`, already implemented,
  `ApkDownloader.kt:199-211`); for nightly-manual, the same but silent
  (`installSilently`).
- "Nightly updated to vX" → informational only; tap opens the app (e.g. to
  the What's New page) — no action needed beyond that.

### 5. Edge cases to handle when implementing

- `POST_NOTIFICATIONS` denied → foreground service must still work (Android
  doesn't require the notification to be *visible*, just posted); don't gate
  the download on permission grant.
- `setForeground()` throwing `IllegalStateException` (background-start
  restrictions) → degrade to a normal (non-foreground) worker run rather than
  crashing; log and continue, per Mihon's `setForegroundSafely` pattern.
- User dismisses/swipes the progress notification mid-download — decide
  whether that should cancel the download (probably not, since it's
  low-importance/ongoing and Android normally prevents swipe-dismiss on an
  active foreground-service notification anyway).
- Overlapping triggers: manual tap while a periodic-worker download is
  already in flight — `ApkDownloader.startDownload` already no-ops if
  `_downloadState.value is DownloadState.Downloading` (`ApkDownloader.kt:55`);
  make sure the new worker-enqueue path preserves that (e.g. dedupe via
  `WorkManager.enqueueUniqueWork` keyed by channel, `ExistingWorkPolicy.KEEP`).

## Explicitly out of scope for now

- No change to *when* checks happen (the 30-minute periodic interval fix is
  separate and already done).
- No change to stable's install flow (already correctly non-silent).
- No retry/resume-on-failure improvements to the download itself — only the
  survive-backgrounding + notification layer.

## Suggested implementation order

1. Notification channels + `POST_NOTIFICATIONS` request plumbing (foundation,
   nothing depends on downloads yet).
2. `ApkDownloadWorker` (or the inline-into-`AppUpdateWorker` variant) with
   `setForeground` + progress notification, wired to the periodic path first
   (lowest-risk, already backgrounded by definition).
3. Switch the interactive call sites (`AppInfoSheet.kt`, `MainShell.kt`) to
   enqueue the same worker instead of calling `ApkDownloader.startDownload`
   directly — this is what actually fixes the reported bug.
4. "Ready to install" / "Nightly updated" terminal notifications + tap
   `PendingIntent`s.
5. Manual on-device test matrix: download while backgrounded (the original
   repro), both nightly settings on (full silent flow), only auto-download on
   (manual install tap from notification), notification permission denied.
