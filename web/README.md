# MyBicocca website

The landing page for MyBicocca, built with [Astro](https://astro.build) and TypeScript.
It's a fully static site, and the only client-side script is a tiny custom element for the tour tabs.

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
├── components/
│   ├── sections/   # One component per page section (Hero, Tour, FAQ, ...)
│   └── ui/         # Reusable primitives (Button, PhoneFrame, Wordmark, ...)
├── data/           # Typed page content: features, tour chapters, FAQ, links
├── layouts/        # Document shell, meta tags and fonts
├── pages/          # Routes
├── scripts/        # Client-side TypeScript (the <feature-tabs> element)
└── styles/         # Design tokens and global styles
```

Content lives in `src/data/*.ts`, so most copy changes don't touch markup.

## Assets

App screenshots are imported straight from the repository's `/screenshots` folder through
the `@screenshots/*` alias, so the README and the website always show the same images.
Astro converts them at build time into responsive AVIF images with WebP fallbacks.

## Performance notes

- Fonts (Inter, Bricolage Grotesque) are self-hosted Latin-only variable subsets,
  preloaded, with metric-matched fallbacks so they don't shift the layout.
- Scroll reveals and the header backdrop use CSS scroll-driven animations, so they need
  no JavaScript. Browsers without support just show the content.
- The mobile menu uses the Popover API and the FAQ uses exclusive `<details>`. Both work
  without JavaScript.
- Animations respect `prefers-reduced-motion`.
