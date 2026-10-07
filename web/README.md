# MyBicocca website

The landing page for MyBicocca, built with [Astro](https://astro.build) and TypeScript.

## The idea

The page is the app's calendar day view at the scale of a website. An hour gutter runs
down the left edge, and a red "now" line is fixed across the screen. As you scroll, its
clock runs from 07:00 to midnight. Each feature is a moment in a student's day: the
09:30 lecture, finding room U24-DISCO-C2, booking an appello, a 28 arriving at 18:47, the
project submitted at 23:39. Along the way the page moves from the app's light theme
to its dark one.

The colours, the event blocks and the exam chips are taken from the Android app itself
(`ui/theme/Color.kt`, `calendar/theme/EventPalette.kt`).

## Getting started

```sh
cd web
npm install
npm run dev       # http://localhost:4321
npm run build     # type-checks, then builds to dist/
npm run preview   # serves the production build
```

Set `SITE_URL` (e.g. `SITE_URL=https://example.org npm run build`) to emit canonical and
Open Graph URLs.

## Structure

```
src/
├── components/   # Hero, Chapter (one moment of the day), Moment (app UI fragments), NowLine, ...
├── data/         # Typed content: the day's chapters, privacy points, links
├── lib/clock.ts  # Pure time-of-day helpers shared by the server render and the client
├── scripts/      # <day-clock>: maps scroll position to the time of day
├── layouts/      # Document shell, meta tags and fonts
└── styles/       # Tokens, the `sky` keyframes and the timeline grid
```

To add or change a moment, edit `src/data/day.ts`. Times must increase down the page.

## How the day works

- Every element with `data-time="HH:MM"` is a waypoint. `<day-clock>` interpolates
  between the waypoints around the "now" line to read the time, then writes the day's
  progress to `--day` on the root.
- `--day` scrubs a paused CSS animation (`@keyframes sky`) through registered custom
  properties (`@property --paper`, `--ink`, `--night`…). That keeps all the colour logic
  in CSS, and every browser can interpolate it.
- Event blocks blend the app's light and dark palettes with `color-mix()`, driven by
  `--night`.
- Without JavaScript the page renders as a static, readable morning, with no clock.

## Assets and performance

- Screenshots are imported from the repository's `/screenshots` folder (`@screenshots/*`),
  so the README and the website always match. They're converted at build time to
  responsive AVIF with WebP fallbacks.
- Fonts (Archivo with its width axis, and Martian Mono) are self-hosted Latin subsets,
  preloaded, with metric-matched fallbacks.
- The only client script is the clock: one passive scroll listener, batched per frame.
