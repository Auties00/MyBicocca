# Modal navigation

How MyBicocca shows modal sheets and the pages inside them. Everything here lives in
`app/src/main/java/it/attendance100/mybicocca/ui/navigation` and `ui/component/modal`.

## The model

The signed-in shell has **one Navigation 3 back stack**. It holds full-screen pages
(`AppRoute`), sheets (`SheetRoute`) and the pages inside sheets (`SheetRoute.InSheetPage`), and it is
changed only through `AppNavigator` (`LocalAppNavigator` in composition).

```
[TabRoot] [CourseDetail(42)] [AssignmentDetail] [FileOpenChooser]
 page       page               sheet root ─┐      in-sheet page ─┘   → one sheet, two pages
```

- A regular `SheetRoute` opens a **new sheet**, stacked above whatever is showing. That can be a page
  or another sheet, e.g. Appelli opened from a calendar event sheet.
- An `InSheetPage` route continues the sheet beneath it. Pushed while a sheet is open, it becomes the
  next page inside that sheet, which morphs instead of opening a window on top. Pushed from a
  full-screen page, it opens as its own sheet.
- An `AppRoute` pushed from a sheet (e.g. a PDF from Appuntamenti) covers it. The sheet slides away,
  and slides back with its state when the page is closed.

Every push is wrapped in a `Destination(route, id)` with a unique id. That id is:
- the entry's contentKey, under which its saveable state and ViewModel store are filed;
- the identity of the sheet a root page opens;
- the handle pops are addressed by.

Because the back stack is saved with `rememberNavBackStack`, open sheets and the page they show are
restored after process death.

## Rendering

`ModalSceneStrategy` turns the top run of sheet entries into one Navigation 3 `OverlayScene` per
sheet:

- **Scene identity is the sheet** (its root page's id), not the pages it shows. Navigation 3 keeps a
  scene composed for as long as an equal scene is calculated. The sheet therefore stays the same
  window while its pages change, and is never composed twice.
- The sheet reads its pages from the live stack (`LocalModalEntries`). Navigation 3 keeps the
  first scene object of a sheet, so the pages can't be read from that object.
- `onRemove()` animates the sheet out when navigation closes it (a "Fatto" button, the tab bar, a
  deep link). If the same sheet returns while it is hiding, it slides back in.

The container is `PredictiveModalBottomSheet`, a Material 3 `ModalBottomSheet` that provides:
- the scrim and drag handle, accessibility actions, IME handling and edge-to-edge insets;
- **native predictive back** on the sheet's first page (the sheet scales with the finger, then
  dismisses);
- a sheet-local snackbar host;
- `SheetDismissControl` (`LocalSheetDismissControl`), which lets the page on top lock gestures
  or veto a dismissal (e.g. "uscire senza inviare?").

Per-sheet looks (corners, handle, insets) come from `sheetStyle(SheetContainerStyle(...))` metadata on
the sheet's first page.

## Pages inside a sheet: `SheetPager`

Every multi-page sheet uses `SheetPager`, whether its pages are back-stack entries or come from its
own state machine (wizards, confirm and result pages). One `SeekableTransitionState` drives:
- the **pinned header**: back arrow, inset, title and subtitle (from `SheetHeaderSpec`, via
  `sheetHeader<Route> { … }` metadata for back-stack pages);
- the **body**: a horizontal push;
- the **sheet height**: a `SizeTransform` that re-measures the sheet every frame.

The system back gesture seeks that one transition toward the previous page, commits on release and
rewinds on cancel. Header, body and height therefore move in lockstep.

## Rules of thumb

- **Open a sheet or page:** `navigator.navigate(route)`. Pushes are single-top, so a double tap is
  harmless.
- **Close the current sheet from inside it:** `LocalSheetDismissControl.current?.dismiss()`.
- **Close just this page:** `navigator.pop(LocalDestination.current!!.id)`.
- **Close the sheet a page belongs to:** `navigator.dismissSheetOf(id)`.
- **Never pop by count, and never pop "whatever is on top" from a callback.** Pops are addressed by id,
  are idempotent, and can never remove the root.
- **Don't navigate from an effect because data is missing:** after process death the page is back
  before its data. Use `PopWhenMissing(loaded, missing)`.
- **Resetting a flow when its sheet closes:** use `DisposableEffectOnPop { … }`, not `DisposableEffect`.
  A sheet hidden under a full-screen page is disposed but still on the stack.
- **Body gestures are native:** content scrolls, and at the top a downward drag or fling moves and
  dismisses the sheet. Lock it with `SheetDismissControl.gesturesEnabled` where a dismissal would
  lose work.
- **Small pickers tied to a screen's transient state** may stay local, but must still use
  `PredictiveModalBottomSheet` (and `SheetPager` for sub-pages). Examples: the course page's folder and
  link sheets, the video quality picker, and the map's building panel that follows the map selection.
  Alert dialogs stay local.

## Issues this addresses

| Issue | Cause | Fix |
|---|---|---|
| #44 crash "Key Isee was used multiple times" after resume | Every shell recomposition rebuilt `listOf(…, SinglePaneSceneStrategy())`, recalculating scenes. On Navigation 3 1.1.0 overlay scenes were tracked by `equals`, and the sheet scene had none, so NavDisplay composed a second sheet next to the first. When its window attached first, the same entry was provided twice. Reproduced in a Robolectric test on the old code: the sheet page was freshly composed 6 times in 5 recompositions. | Upgraded to Navigation 3 1.2.0, which tracks overlays by `Scene.key` (and fixes key reuse under rapid pops). Strategies are remembered. Sheet scenes are keyed and equal per sheet and read their pages live, so the one scene NavDisplay keeps never goes stale. Every push has a unique id, so equal routes never share state. Covered by `ModalSceneStrategyTest` (single composition, restore). |
| #22 empty-back-stack crash | Pops by count (`pop(run.size)`) captured in stale lambdas, `removeLastOrNull()` from several callbacks, and detail pages popping "the top" when their data was missing. | `AppNavigator`: id-addressed, idempotent pops; the root is unremovable; `PopWhenMissing` only acts once data loaded. |
| #9 sub-page back not synced with the header | The header animated on its own clocks from the settled page; only the body followed the gesture. | `SheetPager`: one seekable transition for header, body and height. |
| #14 height not animating between page and sub-page | Sheets with back-stack sub-pages were re-created on each push, and other sheets each wired their own AnimatedContent. | Persistent sheet per root and one `SheetPager` for all sheets. |
| #10 swipe-to-dismiss from the body glitches | The sheet window was rebuilt mid-gesture. Duplicate dismiss callbacks over-popped the stack. A height-animating close transition ran during the gesture. Some sheets swallowed body drags, others didn't. | All of those are removed: native Material predictive back and nested-scroll dismissal everywhere, with idempotent dismissal. |
