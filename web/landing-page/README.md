# MyBicocca landing page

The Astro static landing page for [mybicocca.it](https://mybicocca.it), presenting MyBicocca
across Web ([web.mybicocca.it](https://web.mybicocca.it)), Android (GitHub Releases APK), and
iOS & iPadOS (coming soon), using the first supported browser language (Italian or English) and falling back to English.

```sh
npm install
npm run screens         # import curated dark screenshots from web & android screenshot folders
npm run dev             # http://localhost:4321
npm run check           # astro check & TypeScript verification
npm run build           # build static site into dist/
npm run preview         # preview dist/ locally
npm run preview:worker  # preview with Cloudflare Worker static asset serving & redirects
npm run deploy          # build and deploy to Cloudflare Workers
```

## Adaptive multi-platform UX

- **Desktop visitors** see **Open the Web App** (`web.mybicocca.it`) as the primary CTA alongside a
  dual-device hero (desktop web app + native Android phone).
- **Android visitors** see **Download for Android** (latest GitHub Releases APK) as the primary CTA
  with a native Android hero preview.
- **iOS / iPadOS visitors** see **Open the Web App** (`web.mybicocca.it`) as the primary CTA with a
  mobile web preview while the native iOS app is in development.
- Pass `?device=desktop`, `?device=android`, or `?device=ios` in the URL to preview any device
  perspective from a desktop browser.
