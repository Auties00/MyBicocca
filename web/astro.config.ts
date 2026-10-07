import { fileURLToPath } from 'node:url';
import { defineConfig, fontProviders } from 'astro/config';

// The app screenshots live at the repository root so the README and the site share a
// single source of truth. Vite only serves files inside the project by default, so the
// folder is explicitly allowed for the dev server.
const projectRoot = fileURLToPath(new URL('.', import.meta.url));
const screenshotsDir = fileURLToPath(new URL('../screenshots', import.meta.url));

export default defineConfig({
  // Set SITE_URL at build time to emit absolute canonical and Open Graph URLs.
  ...(process.env['SITE_URL'] ? { site: process.env['SITE_URL'] } : {}),
  build: {
    inlineStylesheets: 'auto',
  },
  // Self-hosted variable fonts (Latin subset). Archivo carries a width axis, used from
  // condensed timetable numerals to regular text. Astro generates metric-matched
  // fallbacks so swapping the web fonts in doesn't shift the layout.
  fonts: [
    {
      provider: fontProviders.local(),
      name: 'Archivo',
      cssVariable: '--font-archivo',
      fallbacks: ['Arial Narrow', 'sans-serif'],
      options: {
        variants: [
          {
            src: ['@fontsource-variable/archivo/files/archivo-latin-wdth-normal.woff2'],
            weight: '100 900',
            stretch: '62% 125%',
            style: 'normal',
          },
        ],
      },
    },
    {
      provider: fontProviders.local(),
      name: 'Martian Mono',
      cssVariable: '--font-martian',
      fallbacks: ['monospace'],
      options: {
        variants: [
          {
            src: ['@fontsource-variable/martian-mono/files/martian-mono-latin-wght-normal.woff2'],
            weight: '100 800',
            style: 'normal',
          },
        ],
      },
    },
  ],
  vite: {
    server: {
      fs: { allow: [projectRoot, screenshotsDir] },
    },
  },
});
