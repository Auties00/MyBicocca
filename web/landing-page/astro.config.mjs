import sitemap from '@astrojs/sitemap';
import { defineConfig } from 'astro/config';

export default defineConfig({
	site: 'https://mybicocca.it',
	build: {
		inlineStylesheets: 'auto'
	},
	integrations: [sitemap()]
});
