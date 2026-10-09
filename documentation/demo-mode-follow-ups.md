# Follow-ups found while building demo mode

Things noticed on `feat/demo-mode` that are not part of that branch. Each is either a bug in the
app that the demo build happened to expose, or a known gap in the demo itself.

## App bugs

### Fixed on `fix/bottom-bar-hide-and-card-tilt`, waiting to be merged

- **Bottom bar strip on sub-pages.** The shell hid the bar by sliding it a fixed 300 px. At 560 dpi
  the bar is about 364 px tall, so a strip stayed visible. It now slides by its measured height.
  (`ui/navigation/MainShell.kt`, `ui/component/bar/MyBicoccaBottomBar.kt`)
- **Profile crash from the tilt sensor.** A rotation sensor reporting NaN (emulators do) reached the
  student card's gradient, and `LinearGradient` throws on NaN. Non-finite readings are now dropped.
  The cause is inferred from the stack trace; the crash stopped once the guard was in.
  (`ui/screen/profile/component/CreditCard.kt`)

### Not fixed

- **In-app theme ignored by most components.** 57 places in 52 UI files call
  `isSystemInDarkTheme()` instead of reading `LocalDarkTheme`. With the app set to Dark and the
  phone in light mode (or the reverse) they pick the wrong colours, for example light service
  tiles on a dark background. Until this is fixed, the capture script switches the system mode
  and leaves the app on "System".
- **Italian text in the English UI.**
  - Sub-page titles "Impostazioni", "Profilo" and "Docente", hard-coded in
    `ui/navigation/route/AppRoute.kt` and already marked `TODO localize`.
  - "Da pagare" / "Pagate" on the Fees sheet.
  - "Tutti", "Preferiti" and "N° anno" on the e-learning filter.
  - "Riprendi" on the course video card.
- **Current time read on every recomposition.** Several composables call
  `LocalDate.now(...)`/`Instant.now(...)` in their body or as a default argument rather than
  remembering it or taking it from the ViewModel (`ActivityRows.kt`, `CourseDetailScreen.kt`,
  `BookingSummaryCard.kt`, `ForecastGraph.kt` among others). This predates demo mode.

## Build and tooling

- **System Gradle 9.4.1 breaks the open-source-licenses task.** It fails with
  `Could not find method addDebugLicense()` whenever the task actually runs. The committed wrapper
  (9.3.1) works. Use `./gradlew`.
- **`android/CLAUDE.md` is in `.git/info/exclude`.** It is local to one machine, so the demo-mode
  rules written there are not shared with other contributors. It also still says AGP 8.13.2; the
  project is on 9.1.1.
- **Eight shared Compose tests fail under the demo variant** (`SheetPagerMotionTest`,
  `ModalSceneStrategyTest`), most likely for lack of the debug-only test manifest.
  `testDemoUnitTest` is filtered to the demo package, so they only run under debug, where they pass.
- **Nothing in GitHub Actions builds the demo variant.** By choice: the check is local,
  `./gradlew :app:demoCheck`.

## Demo build gaps

- **Shares the real app's application id**, because `google-services.json` only knows that one.
  Installing demo on a phone replaces the real app; the capture script refuses physical devices.
- **Calendar keeps its own copy** of the booked exams, the library seat and the office appointment
  instead of reading them from those fakes. Cancelling a booking in Services leaves it on the
  calendar.
- **No lecture video.** Recordings have watch progress but no stream, so the player opens empty.
- **Map is not faked.** It shows public data and needs network access to load rooms.
- **Search history entries all share one timestamp**, because the clock is frozen.
- **The demo DI module repeats** the twelve device-local bindings that are identical to the live
  one.
- **Tapping an added catalogue course does nothing**: enrolling is a no-op and an unknown course id
  shows a spinner.

## Screenshot catalogue gaps

- **Not captured:** Login (demo is always signed in), the QR scanner camera view, global search,
  and the map's building list and building detail.
- **Map buttons are invisible to the capture script.** Nothing on the Map tab is exposed to
  accessibility ("View buildings" included), so the script cannot tap it. Besides blocking those
  two shots, this means screen readers cannot reach the map controls either.
- **README lost its "Inside a building" row**, since there is no building-detail shot to show, and
  the "Building directory" row now shows the map filter sheet.
- **Camera placeholder:** the scanner camera view is not generated, so there is nothing yet to
  drop a placeholder image into. `Screenshot_AttendanceOpt` shows the check-in chooser before it.
- **Language settings shot** shows the captured language selected, not "System", because the
  script pins the language to switch it.
- **A wrong-screen capture is not detected.** The script notices crashes, not a shot that landed
  on the wrong page.
- **Only the default palette and a handful of dark-mode shots have been looked at.**
