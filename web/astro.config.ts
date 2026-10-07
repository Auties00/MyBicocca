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
  // Self-hosted variable fonts (Latin subset only). Astro generates metric-matched
  // fallbacks so swapping in the web font doesn't shift the layout.
  fonts: [
    {
      provider: fontProviders.local(),
      name: 'Inter',
      cssVariable: '--font-sans',
      fallbacks: ['system-ui', 'sans-serif'],
      options: {
        variants: [
          {
            src: ['@fontsource-variable/inter/files/inter-latin-wght-normal.woff2'],
            weight: '100 900',
            style: 'normal',
          },
        ],
      },
    },
    {
      provider: fontProviders.local(),
      name: 'Bricolage Grotesque',
      cssVariable: '--font-display',
      fallbacks: ['system-ui', 'sans-serif'],
      options: {
        variants: [
          {
            src: ['@fontsource-variable/bricolage-grotesque/files/bricolage-grotesque-latin-wght-normal.woff2'],
            weight: '200 800',
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
