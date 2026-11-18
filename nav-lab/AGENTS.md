# Lab: brief for coding agents

Lab is an Android recorder app for indoor-positioning field tests in building U14 (University of Milano-Bicocca). It records phone sensors, Bluetooth beacons and Wi-Fi to local files that a separate Python package, `labtools`, analyses on a laptop. There is no backend, no account, no network use, no map.

## Read first

- `docs/LAB-APP-SPEC.md`: the specification. Sections 1–6 are in scope (release **R0**). Sections 7–8 (R1, R2, iOS) are **out of scope**, except where "Day-1 tools" below borrows from them.
- `docs/FORMAT-QUESTIONS.md`: written by the `labtools` author. Where the spec was ambiguous, it records what `labtools` expects (event `kind`/`detail` strings, `setup.recording_mode`, test-ID syntax, and so on). **Follow every "labtools expects" and "Proposal" item there**: the files Lab writes must be read by `labtools` without errors.
- `docs/TOOLS-SPEC-7.2-7.3.md`: folder layout and test-ID syntax.

## Scope

1. **R0, complete** (spec §2–§6): Setup screen with experiment templates, readiness panel and capability list; guided recording with voice prompts (Android `TextToSpeech` + `ToneGenerator`) and the exact protocol in §2.1; free mode; follower mode; marker-only mode; live screen with per-stream counters, CHECKPOINT button, undo, event buttons, volume-down marks, touch-lock overlay, keep-screen-on and the "allow screen off" switch; foreground service that owns all capture; every stream in §2.3–§2.6 with the exact file names and CSV columns; `t_ns`/`rx_ns` as in §2.2; manifest (§2.7); Sessions list with status light, share as zip, delete; the reliability items in §3; marks in §4; privacy in §5.
2. **Day-1 tools**, a separate "Survey" tab, so the team needs no third-party scanner apps on the first site visit:
   - **Beacon inventory**: the "inventory mode" of §2.4. Live list of every beacon frame heard (Eddystone-UID, Eddystone-TLM, iBeacon, AltBeacon), never other Bluetooth devices. Per beacon: frame type, canonical ID (format rules in §2.4), the project label when the ID matches the project scheme (below), current and median RSSI, packets/s, hardware address, advertised tx power, TLM battery mV and temperature. Minimum-RSSI slider (default −70 dBm), sort by RSSI, tap for details. "Save snapshot" writes the list to a CSV under the app's sessions folder (`survey/` subfolder) with a user-entered location note.
   - **Wi-Fi survey**: live list of access points from scans (SSID, BSSID, RSSI, frequency, channel width, 802.11mc/RTT responder flag, locally-administered-address flag per §2.5), the connected AP highlighted, the count of APs whose SSID is in a user-editable "university SSIDs" list (default `eduroam`) above −75 dBm. "Save snapshot" with a checkpoint label (e.g. `CP3`) appends rows to a survey CSV. Show whether the phone supports Wi-Fi RTT.
   - **RTT ranging**: if the phone supports RTT and responders are present, range to them once per second and show distance ± std; log to CSV.
   - **Connected-AP log**: a toggle that logs the connected BSSID and RSSI every 5 s with a timestamp, to a CSV.

## Project constants

- Project UUID `515b8618-bead-46de-ba72-5cea0ec01b22`; Eddystone namespace `515b86185cea0ec01b22`.
- iBeacon major = building code (U14 = 14), minor = beacon number; Eddystone instance = `0000` + building code (4 hex) + beacon number (4 hex). Label `B07` = beacon 7. Building code is a setting, default 14.

## Code layout

- `:format` (pure Kotlin JVM module, already configured): CSV writers, the manifest model (kotlinx.serialization), the test-ID builder, canonical beacon-ID parsing and formatting, beacon-frame parsing from raw advertisement bytes. **No Android imports.** Unit tests in `format/src/test` for the test-ID builder, canonical IDs, frame parsing (use real byte arrays for Eddystone-UID, Eddystone-TLM and iBeacon), and CSV headers matching the spec exactly.
- `:app` (package `it.unimib.indoornav.lab`): Compose Material 3 UI, the foreground service, one capture class per stream in a `capture` package, permissions, the guided protocol, voice prompts, the Survey tab.
- Keep dependencies to what `app/build.gradle.kts` already declares. No dependency injection framework. Navigation Compose is available.

## Rules

- minSdk 29, targetSdk 36. Request runtime permissions properly (Bluetooth scan/connect, fine location, nearby Wi-Fi devices on 33+, notifications on 33+, activity recognition if used). Foreground service with the right `foregroundServiceType` and manifest permissions.
- Never use `SENSOR_DELAY_FASTEST`; register with explicit periods as §2.3 says.
- No network calls, no analytics, no crash reporters. Never write personal data other than the operator names the user types.
- Kotlin code style: official. No numbered-step comments (`// 1. do X`). Comment only non-obvious facts, briefly.
- When the spec and FORMAT-QUESTIONS conflict, FORMAT-QUESTIONS wins (it describes what the analysis tools read).
- **It must build:** `./gradlew :format:test :app:assembleDebug` must pass. Run it yourself and fix every error before you finish. Never run two Gradle builds at once.
- Do not run `git commit`.
- Finish by writing `NOTES.md`: what is implemented, what is not, every deviation from the spec and why, and anything you could not verify without a real phone.
