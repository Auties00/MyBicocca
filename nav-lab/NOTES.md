# Lab Android App — Implementation Notes

## Implemented

### `:format` module (pure Kotlin JVM, zero Android imports)
- `ProjectConstants.kt`: Project UUID (`515b8618-bead-46de-ba72-5cea0ec01b22`), Eddystone namespace (`515b86185cea0ec01b22`), building code defaults (`U14` / `14`), canonical ID builders, and project label matcher (`B01`, `B07`, ...).
- `BeaconModels.kt`: `CanonicalBeaconId`, `BeaconFrameType` (`ed_uid`, `ed_tlm`, `ibeacon`, `altbeacon`, `vendor`), and `BeaconFrameParser` supporting raw BLE advertisement byte arrays as well as service/manufacturer payloads.
- `CsvWriter.kt`: `StreamSpec` defining every R0 stream file name and exact CSV header (`acc.csv`, `acc_raw.csv`, `gyro.csv`, `gyro_raw.csv`, `mag.csv`, `mag_raw.csv`, `baro.csv`, `game_rot.csv`, `rot.csv`, `steps.csv`, `sensor_accuracy.csv`, `ble.csv`, `wifi.csv`, `rtt.csv`, `events.csv`, `marks.csv`, `gnss.csv`), `SurveyCsvHeaders`, and RFC 4180 `CsvCodec` (quoting/parsing).
- `TestId.kt`: `ExperimentTemplate` (`E2`, `E3`, `E4`, `E4b`, `E5`, `E6`, `E8`, `E9`, `E10`, `E11`) and `TestIdCodec` (builds and parses `-key=value` parts cleanly with hyphenated values like `hand-up`, `-1`, `int100-pwr0-both`).
- `SessionManifest.kt`: `@Serializable` manifest model matching §2.7 + `FORMAT-QUESTIONS.md` Q2 (`setup.recording_mode`) and Q4 (`sensors[].type`).
- `SessionEvaluator.kt`: `SessionNaming` (`YYYYMMDD-HHMMSS_<phone>_<testid>`) and `SessionEvaluator` quick-look health check (Green/Amber/Red) checking `stopped_cleanly`, required streams, `rx_ns - t_ns` spread/offset, gaps > 2 s, and `wifi_scan_refused`.
- Unit tests in `format/src/test/kotlin/it/unimib/indoornav/lab/format/`: `BeaconParserTest`, `TestIdTest`, `CsvWriterTest`, `SessionManifestTest`.

### `:app` module (`it.unimib.indoornav.lab`)
- **Capture streams (`capture/`)**:
  - `SessionWriter`: dedicated single-threaded buffered writer flushing every 1 s, tracking row counts, first/last `t_ns`, `lastRxNs`, and live rows/s.
  - One capture class per stream: `AccelerometerCapture`, `AccelerometerRawCapture`, `GyroscopeCapture`, `GyroscopeRawCapture`, `MagnetometerCapture`, `MagnetometerRawCapture`, `BarometerCapture`, `GameRotationCapture`, `RotationCapture`, `StepsCapture`, `SensorAccuracyCapture`, `BleCapture`, `WifiCapture`, `WifiRttCapture`, `GnssCapture`, `EventsCapture`, `MarksCapture`.
  - All sensor listeners register on a dedicated `HandlerThread` with explicit periods `max(5_000, max(requestedUs, sensor.minDelay))` and never `SENSOR_DELAY_FASTEST`.
  - BLE normal mode uses hardware `ScanFilter`s (Eddystone `0xFEAA` and Apple iBeacon `0x004C` `02 15`); restarts every 20 minutes with `ble_scan_restart` in `events.csv`; correlates `ed_tlm` to the last `ed_uid` per address. Inventory mode runs unfiltered and logs per-second `ble_other_count` to `events.csv` (with optional warned `storeEverything` mode).
  - Wi-Fi filters to `university_ssids`, logs `la_bit` and `rtt_responder`, logs `wifi_scan` (`scan_id=<n> other=<m>`) and `wifi_scan_refused` in `events.csv`, and triggers `WifiRttCapture` every 0.5 s on up to 10 responders.
- **Foreground service & protocols (`service/`)**:
  - `RecordingService` (`foregroundServiceType="location"`) with partial `WakeLock`, persistent notification, crash-safe `manifest.json` written at start and stop, and 20-minute warning.
  - `GuidedProtocolController` + `VoicePrompter` (`TextToSpeech` + `ToneGenerator`): start shake (or 10 s), 5 s place countdown, 10 s still, double-beep + "Go", finish stand-still 10 s, take-out shake (or 15 s), plus Free stand-mode 10 s `away` countdown (Q19). Logs every `phase` row to `events.csv` per Q3.
  - Follower mode (all streams, no prompts, touch lock on from start) and Marker-only mode (`acc`, `marks`, `events` only, guided prompts on).
  - Post-walk prompt dialog for `afterWalk` template fields (e.g. E6 `steps`), updating `manifest.json`.
- **UI (`ui/` & `MainActivity.kt`)**:
  - Setup screen with operator/phone/building/floor/building-code fields, mode/carry/template selectors, readiness panel (BT, Location, 2-scan Wi-Fi throttling test, battery exemption, battery >= 30%, storage >= 1 GB, runtime permissions), and expandable device capability list.
  - Live recording screen with elapsed timer, 20-min warning, per-stream counters turning red when silent > 3 s, large `CHECKPOINT` button + `Undo last mark`, event buttons (`stopped`, `resumed`, `stairs_start`, `stairs_end`, `lift_start`, `lift_end`, `dropped_phone` note, custom `note`), `Allow screen off` switch, `TouchLockOverlay` (two-finger long-press unlock), keep-screen-on + brightness dimming, and Volume-Down mark / Volume-Up 2 s finish hardware keys.
  - Survey tab (Day-1 tools): Beacon Inventory (live filtered list, RSSI slider default -70 dBm, median RSSI, pps, TLM battery/temp, details dialog, snapshot CSV to `survey/`), Wi-Fi Survey (live APs, connected AP highlight, university SSIDs > -75 dBm count, editable SSIDs, RTT support indicator, checkpoint CSV append), RTT Ranging (1 Hz ranging + CSV log), and Connected-AP Log (5 s CSV log).
  - Sessions tab: session list with Green/Amber/Red status light, findings, Share as ZIP (`FileProvider`), Delete, and Survey CSV file share/delete.

## Not Implemented (Out of Scope for R0)
- R1 features: floor-plan image/transform view, on-phone beacon registry/provisioning/placement, checkpoint/route CSV editor, ARCore stream (`arcore.csv`), Bluetooth marker-phone remote link (`source = remote`), on-phone plots/session review.
- R2 features: server sync, radio maps, live positioning engine view, iOS recorder.

## Deviations / Clarifications vs. Spec
- Followed `FORMAT-QUESTIONS.md` wherever the spec was silent or ambiguous:
  - Added `setup.recording_mode` (`guided`, `follower`, `marker_only`, `free`) to `manifest.json` (Q2).
  - Used `events.csv` `kind`/`detail` strings from Q3 and Q7 (`phase` values, `wifi_scan` with `scan_id=<n> other=<m>`, `ble_other_count` once per second).
  - Used lowercase `TYPE_*` names in `sensors[].type` and `sensor_accuracy.csv` (Q4), and `detector`/`counter` in `steps.csv` (Q7c).
  - Used `t_ns = rx_ns` for marks (Q5) and RFC 4180 CSV quoting (Q7b).
  - Added `E11` template reusing E5 fields (Q29) and Free stand-mode 10 s `away`/`back` phase events (Q19).
- AltBeacon canonical ID formatted as `ab:<40-hex-lowercase>` (spec §2.4 specifies `ed:` and `ib:` formats explicitly and lists `altbeacon` as a frame type without a canonical prefix).
- `dropped phone` button writes `kind=note`, `label=dropped_phone` in `marks.csv` to keep `marks.csv` `kind` strictly within the §2.6 vocabulary.
- Checkpoints on the Live screen auto-increment (`CP1`, `CP2`, ...) because `routes.csv` import is an R1 feature.
- `local.properties` had unescaped single backslashes in `sdk.dir` on Windows; updated to escaped `\\` so Gradle could resolve the Android SDK path.

## Not Verified Without a Real Phone
- Live hardware sensor rates, accelerometer shake detection thresholds, `TextToSpeech`/`ToneGenerator` audibility, BLE hardware `ScanFilter` rejection of nearby iPhones/AirPods, 20-minute BLE scan restart behavior, Android Wi-Fi scan throttling toggle detection, 802.11mc Wi-Fi RTT ranging results, GNSS indoors/outdoors transition, `ACTION_SCREEN_OFF` background capture behavior (E4b), and pocket touch-lock ergonomics on physical test devices.

## Review 1 fixes
1. **[blocker] Missing `ACCESS_NETWORK_STATE` / manifest permissions audit (`AndroidManifest.xml`)** — **Fixed.** Added `android.permission.ACCESS_NETWORK_STATE` (for `ConnectivityManager.getActiveNetwork`/`getNetworkCapabilities`) and audited all Survey/capture system APIs (`ACCESS_WIFI_STATE`, `CHANGE_WIFI_STATE`, `NEARBY_WIFI_DEVICES`, `BLUETOOTH_SCAN`/`CONNECT`, `ACCESS_FINE_LOCATION`, `ACTIVITY_RECOGNITION`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE_LOCATION`, `WAKE_LOCK`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`).
2. **[minor] Free mode with stand templates showing `CHECKPOINT` button (`LiveRecordingScreen.kt`, `RecordingCoordinator.kt`, `RecordingService.kt`, `TestId.kt`)** — **Fixed.** Added `templateCode` to `RecordingUiState` and `ExperimentTemplate.STAND_TEMPLATE_CODES` (`E2`, `E3`, `E4`, `E4b`, `E10`); hid the `CHECKPOINT` button (and `Vol- = Checkpoint` hint, plus ignored `Vol-` checkpoint key presses in `MainActivity`) when `recordingMode == "free"` with a stand template, while keeping event buttons and undo.
3. **[blocker] `SessionWriter.close()` dropping queued rows (`SessionWriter.kt`)** — **Fixed.** Removed the inner `closed` abort inside `ioExecutor.execute` for `openStream`/`writeRow` and introduced `handlesClosed` flipped inside the final `close()` executor task so every task queued before `close()` executes before handles flush and close.
4. **[blocker] Volume keys active when touch-locked or in follower mode (`MainActivity.kt`)** — **Fixed.** Ignored `KEYCODE_VOLUME_DOWN` and `KEYCODE_VOLUME_UP` in `onKeyDown`/`onKeyUp` whenever `recState.touchLocked` is true or `recState.recordingMode == "follower"`.
5. **[blocker] Unsynchronized `carryMode`/`templateFields["mode"]` and `operator`/`templateFields["walker"]` (`SetupScreen.kt`, `TestId.kt`)** — **Fixed.** Synchronized `carryMode` ↔ `templateFields["mode"]` (for `E2`, `E4b`, `E10`) and `operator` ↔ `templateFields["walker"]` (for `E5`, `E6`, `E11`) across all text-field and chip updates.
6. **[blocker] `END_SHAKE` premature arming, truncation after 3rd spike, and cross-thread race (`GuidedProtocolController.kt`, `VoicePrompter.kt`)** — **Fixed.** Synchronized shake state with `shakeLock` + `@Volatile`, waited for TTS utterance completion (`UtteranceProgressListener`) plus 2 s pocket-removal grace before arming `END_SHAKE`, and added a 1 s settling tail delay after the 3rd spike before returning.
7. **[blocker] `readConnectedWifiFull()` returning `null` on Android 12+ (`SurveyManager.kt`)** — **Fixed.** Fell back to `wifiManager?.connectionInfo` when `ConnectivityManager` `WifiInfo.bssid` is null/blank or `"02:00:00:00:00:00"`.
8. **[major] Recording start not stopping active Survey scans (`MainActivity.kt`, `SurveyManager.kt`)** — **Fixed.** Called `app.surveyManager.release()` in `onStartRecording` and when navigating away from the `"survey"` route.
9. **[major] Asynchronous Wi-Fi scan refusal (`EXTRA_RESULTS_UPDATED == false`) not logged (`WifiCapture.kt`)** — **Fixed.** Logged `wifi_scan_refused` to `events.csv` and used the 2,000 ms backoff when `!updated`.
10. **[major] `checkWifiThrottling` testing only 2 scans at 600 ms without awaiting results (`SurveyManager.kt`)** — **Fixed.** Rewrote `checkWifiThrottling` to register a `BroadcastReceiver` for `SCAN_RESULTS_AVAILABLE_ACTION`, await each completion, and run 5 consecutive scans to trigger and detect the 4-scans-per-2-minutes throttle.
11. **[major] `activeAdvertisementLength` / `parseRawAdvertisement` stopping at first `0x00` in 62-byte `ScanRecord` (`BeaconModels.kt`, `BeaconParserTest.kt`)** — **Fixed.** Advanced past `0x00` padding bytes across the buffer, computed `activeAdvertisementLength` as the end of the last valid AD structure, and added unit tests for 62-byte primary + scan-response buffers.
12. **[major] Missing `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` permission (`AndroidManifest.xml`)** — **Fixed.** Declared `<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />`.
13. **[major] BLE permission check omitting `ACCESS_FINE_LOCATION` on API 31+ (`BleCapture.kt`, `SurveyManager.kt`)** — **Fixed.** Required both `BLUETOOTH_SCAN` and `ACCESS_FINE_LOCATION` on API 31+ in `hasScanPermission()` and `hasBlePermission()`.
14. **[major] Silent-stream alert missing initial silence, inactive streams added to `activeStreamKeys`, and `wifi` excluded (`SessionWriter.kt`, `StreamCapture.kt`, `RecordingService.kt`, `LiveRecordingScreen.kt`, capture classes)** — **Fixed.** Initialized `StreamHandle.lastRxNs` when opening streams, changed `StreamCapture.start()` to return `Boolean` so only started streams enter `activeStreamKeys`, and removed `"wifi"` from `isEventLike`.
15. **[major] `keepScreenOn` ignoring `inventoryMode` when `allowScreenOff` is enabled (`MainActivity.kt`)** — **Fixed.** Forced `keepScreenOn = true` whenever `recordingState.isRecording && recordingState.inventoryMode`.
16. **[major] `step_detector`/`step_counter` in `manifest.sensors` and `steps.csv` opened unconditionally (`DeviceCapabilities.kt`, `StepsCapture.kt`, `RecordingService.kt`)** — **Fixed.** Excluded `requestedPeriodUs <= 0` sensors from `toManifestSensors()` (keeping Q4's 9 periodic sensors) and checked `ACTIVITY_RECOGNITION` + sensor presence/registration before opening `steps.csv`.
17. **[major] `SurveyManager` BLE tracker thread safety and duplicate `ED_TLM` entries (`SurveyManager.kt`)** — **Fixed.** Dispatched `processBleResult` onto `mainHandler` and keyed `ED_TLM` trackers by `"ed_tlm:$addr"` (updating `canonicalId` in place on `ED_UID` arrival).
18. **[major] `VoicePrompter` lazy initialization delaying `"Shake now."` (`LabApp.kt`, `VoicePrompter.kt`, `RecordingService.kt`, `GuidedProtocolController.kt`)** — **Fixed.** Pre-initialized `VoicePrompter` in `LabApp.onCreate()`, reused it across sessions via `stopSpeaking()`, and awaited `prompter.awaitReady()` at `startGuided()`.
19. **[major] Non-volatile `StreamHandle` counters, missing crash flush, and Main-thread disk I/O (`SessionWriter.kt`, `LabApp.kt`, `RecordingService.kt`, `RecordingCoordinator.kt`, `SurveyManager.kt`)** — **Fixed.** Marked cross-thread `StreamHandle` fields `@Volatile`, added `emergencyFlush()` wired to a chained `UncaughtExceptionHandler` in `LabApp`, and moved manifest writes, `writer.close()`, `applyAfterWalkFields()`, and Survey CSV writers onto `Dispatchers.IO` / `surveyIoExecutor`.
20. **[minor] `Floor` and `Building numeric code` fields rejecting backspace/`"-"` (`SetupScreen.kt`)** — **Fixed.** Kept local `String` state (`floorText`, `buildingCodeText`) synchronized via `LaunchedEffect` and updated `SetupConfig` when `toIntOrNull()` succeeds (with `KeyboardType.Text` for `Floor` so `"-"` is always typable).
21. **[minor] Notification `ACTION_STOP` skipping guided end protocol while recording `stopped_cleanly = true` (`RecordingService.kt`)** — **Fixed.** Routed `ACTION_STOP` to `requestFinish()`, and set `stoppedCleanly = false` in `requestImmediateStop()` for `guided`/`marker_only` sessions.
22. **[minor] `SessionEvaluator` omitting `GYRO`/`BARO` gaps and Wi-Fi scan rate (`SessionEvaluator.kt`, `SessionEvaluatorTest.kt`)** — **Fixed.** Tracked `GYRO` and `BARO` max gaps (>2 s), flagged non-`marker_only` sessions ≥10 s with `<10` `wifi_scan` events/min as `AMBER`, and added unit tests in `SessionEvaluatorTest`.

## Review 2 fixes
1. **[blocker] First checkpoint label (`TestId.kt`, `TestIdTest.kt`, `RecordingCoordinator.kt`, `RecordingService.kt`)** — **Fixed.** Added `RouteCheckpoints` in `:format` to derive the checkpoint sequence from the template `route` field (`R1` = `CP0`…`CP8`, `R1r` = `CP8`…`CP0`, `R1u` = `CP0`…`CP3 T1 CP3`…`CP0`, `R1L`/`R1Lr` = `CP0L CP4L CP8L`/reverse, `R1R`/`R1Rr` = `CP0R CP4R CP8R`/reverse, `S1`/`S1r` = `ST0 ST1 ST2`/reverse, `L1`/`L1r` = `LF0 LF1 LF2`/reverse), falling back to 0-based `CP0`, `CP1`, … for unknown routes or indices past the known sequence. `RecordingService` and `RecordingUiState` now start at index `0` so the `CHECKPOINT` button shows the route's first checkpoint (e.g. `CP0`) before any mark, advances on mark, and steps back on undo.
2. **[blocker] Experiment templates matching `FIELD-TEST-GUIDE.md` and `labtools` (`TestId.kt`, `TestIdTest.kt`, `SetupScreen.kt`)** — **Fixed.** Audited and updated all 10 templates (`E2`, `E3`, `E4`, `E4b`, `E5`, `E6`, `E8`, `E9`, `E10`, `E11`):
   - `E2`: `d` (`0.5, 1, 2, 3, 5, 8, 12, 16, 20, 30`), `mode` (`stand, hand-up, pocket, body, crowd`), `beacon` (`B01`…).
   - `E3`: `orient` (`0, 90`) only.
   - `E4`: `d` (`5, 12`), `setting` (free-form, e.g. `int100-pwr0-both`, `int300-pwr0-both`, `int1000-pwr0-both`, `int300-pwr0-ibeacon`).
   - `E4b`: no template keys (`E4b`).
   - `E5`: `route`, `dir`, `pace`, `mode`, `layout`.
   - `E6`: `route`, `dir`, `pace`, `mode`, `walker`, `steps` (`afterWalk = true`, numeric input in `AfterWalkFieldsDialog`).
   - `E8`: `route` (`still, S1, S1r, L1, L1r`), `mode` (`stairs, lift, still`).
   - `E9`: `route` (`R1, R1r, R1L, R1R`), `dir`, `offset` (`-1, 0, 1`).
   - `E10`: `cp` (`CP0`…`CP8` or `rtt`) with conditional variant fields — `face` (`pos, neg`) for checkpoint fingerprints and `d` (`2, 5, 10, 15`) when `cp=rtt`.
   - `E11`: `route`, `dir`, `pace`, `mode`, `layout` (matching E5).
   - Added unit tests in `TestIdTest` asserting the exact test-ID strings for all 10 templates and `RouteCheckpoints` sequences.
3. **[major] Carry mode and template `mode` field single source of truth (`TestId.kt`, `SetupScreen.kt`, `LabPreferences.kt`, `RecordingService.kt`)** — **Fixed.** Replaced `CARRY_MODE_FIELD_TEMPLATES` with `ExperimentTemplate.hasModeField()` (`E2`, `E5`, `E6`, `E8`, `E11`). Hid the separate Carry mode chip row for those templates and bound `carryMode` (`manifest.setup.carry_mode`, touch-lock default including `body`, and protocol stand handling for `STAND_TEMPLATE_CODES`) directly to `templateFields["mode"]` across `LabPreferences`, `SetupScreen`, and `RecordingService`.
4. **[minor] Recording modes offered and defaulted per template (`TestId.kt`, `SetupScreen.kt`, `LabPreferences.kt`, `RecordingService.kt`)** — **Fixed.** Added `allowedRecordingModes` per template: `E2`, `E3`, `E4`, `E4b`, `E10` offer and default to `free`; `E5`, `E6`, `E8`, `E9`, `E11` offer `guided`, `follower`, `marker_only` and default to `guided`. Normalized persisted preferences and service start against `allowedRecordingModes`.
