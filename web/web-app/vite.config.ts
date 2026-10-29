import { createHash } from 'node:crypto';
import { createReadStream, existsSync, readdirSync, readFileSync } from 'node:fs';
import type { IncomingMessage, ServerResponse } from 'node:http';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { paraglideVitePlugin } from '@inlang/paraglide-js';
import { relayPlugin } from '@mybicocca/relay/vite';
import adapter from '@sveltejs/adapter-static';
import { sveltekit } from '@sveltejs/kit/vite';
import Icons from 'unplugin-icons/vite';
import type { Manifest, Plugin, Rolldown } from 'vite';
import { defineConfig } from 'vitest/config';
import { type BasemapTiles, unpackBasemap } from './scripts/basemapTiles.ts';

/** The Android app's offline basemap: PMTiles archive, style, glyphs and sprite. */
const MAP_ASSETS = fileURLToPath(new URL('../../android/app/src/main/assets/map', import.meta.url));
const MAP_ARCHIVE = 'basemap.pmtiles';

const MAP_TYPES: Record<string, string> = {
	'.json': 'application/json',
	'.pbf': 'application/x-protobuf',
	'.png': 'image/png'
};

function listFiles(root: string, dir = ''): string[] {
	return readdirSync(path.join(root, dir), { withFileTypes: true })
		.flatMap((entry) => {
			const relative = dir ? `${dir}/${entry.name}` : entry.name;
			return entry.isDirectory() ? listFiles(root, relative) : [relative];
		})
		.sort();
}

/**
 * Serves the Android basemap at `/map-assets/<content hash>/` without copying it into the app: the
 * style, glyphs and sprite as they are, and the archive unpacked into `tiles/{z}/{x}/{y}.pbf`,
 * decompressed so hosts compress them like any file (static hosts can't answer the byte ranges an
 * archive is read by). The hashed path lets hosts cache it forever. The app and the service worker
 * read the path, the file list and the tiles' extent from `virtual:map-assets`.
 */
function mapAssetsPlugin(): Plugin {
	const all = existsSync(MAP_ASSETS) ? listFiles(MAP_ASSETS) : [];
	const hash = createHash('sha256');
	for (const file of all) hash.update(file).update(readFileSync(path.join(MAP_ASSETS, file)));
	const base = `/map-assets/${hash.digest('hex').slice(0, 12)}/`;
	const files = all.filter((file) => file !== MAP_ARCHIVE);
	const virtualId = 'virtual:map-assets';

	let unpacked: Promise<BasemapTiles> | undefined;
	const basemap = () => (unpacked ??= unpackBasemap(path.join(MAP_ASSETS, MAP_ARCHIVE)));

	const middleware = (request: IncomingMessage, response: ServerResponse, next: () => void) => {
		const url = request.url?.split('?')[0] ?? '';
		if (!url.startsWith(base)) return next();
		const file = decodeURIComponent(url.slice(base.length));
		const tile = /^tiles\/(\d+\/\d+\/\d+)\.pbf$/.exec(file)?.[1];
		void (tile ? basemap().then(({ tiles }) => tiles.get(tile)) : Promise.resolve(undefined))
			.then((body) => {
				if (!body && !files.includes(file)) {
					response.statusCode = 404;
					return response.end();
				}
				response.setHeader(
					'Content-Type',
					MAP_TYPES[path.extname(file)] ?? 'application/octet-stream'
				);
				response.setHeader('Cache-Control', 'public, max-age=31536000, immutable');
				if (body) return response.end(body);
				createReadStream(path.join(MAP_ASSETS, file)).pipe(response);
			})
			.catch(next);
	};

	return {
		name: 'mybicocca-map-assets',
		resolveId: (id) => (id === virtualId ? `\0${virtualId}` : undefined),
		async load(id) {
			if (id !== `\0${virtualId}`) return;
			const { minzoom, maxzoom, bounds } = await basemap();
			return (
				`export const mapAssetsBase = ${JSON.stringify(base)};\n` +
				`export const mapAssetFiles = ${JSON.stringify(files)};\n` +
				`export const mapTiles = ${JSON.stringify({ minzoom, maxzoom, bounds })};`
			);
		},
		configureServer: (server) => void server.middlewares.use(middleware),
		async generateBundle() {
			if (this.environment.name !== 'client') return;
			for (const file of files) {
				this.emitFile({
					type: 'asset',
					fileName: `${base.slice(1)}${file}`,
					source: readFileSync(path.join(MAP_ASSETS, file))
				});
			}
			for (const [tile, source] of (await basemap()).tiles) {
				this.emitFile({ type: 'asset', fileName: `${base.slice(1)}tiles/${tile}.pbf`, source });
			}
		}
	};
}

/** The client build's Vite manifest, written before the service worker is built. */
const CLIENT_MANIFEST = fileURLToPath(
	new URL('.svelte-kit/output/client/.vite/manifest.json', import.meta.url)
);

/**
 * `virtual:precache`: the files every page starts from (the entries and route nodes, their static
 * imports, styles and assets), for the service worker to keep ahead of use. What pages import on
 * demand (MapLibre, PDF.js, hls.js, the SDK, the demo) is kept the first time it is used.
 */
function precachePlugin(): Plugin {
	const virtualId = 'virtual:precache';
	return {
		name: 'mybicocca-precache',
		resolveId: (id) => (id === virtualId ? `\0${virtualId}` : undefined),
		load(id) {
			if (id !== `\0${virtualId}`) return;
			const files = new Set<string>();
			if (this.environment.config.command === 'build') {
				const manifest = JSON.parse(readFileSync(CLIENT_MANIFEST, 'utf8')) as Manifest;
				const visit = (key: string) => {
					const chunk = manifest[key];
					if (files.has(chunk.file)) return;
					files.add(chunk.file);
					for (const file of [...(chunk.css ?? []), ...(chunk.assets ?? [])]) files.add(file);
					chunk.imports?.forEach(visit);
				};
				for (const [key, chunk] of Object.entries(manifest)) if (chunk.isEntry) visit(key);
			}
			return `export const precache = ${JSON.stringify([...files].sort())};`;
		}
	};
}

/** The generated SDK's sources. */
const SDK = /[\\/]data-api[\\/]src[\\/]/;

/** A file's module id as the bundler writes it: absolute, with forward slashes. */
const moduleId = (file: string) =>
	fileURLToPath(new URL(file, import.meta.url)).replaceAll('\\', '/');

/** Kit's start file: it sets the page's payload, then loads the client. */
const KIT_START = moduleId('node_modules/@sveltejs/kit/src/runtime/client/entry.js');

/** What every page runs once the payload is set: Kit's client, and the app's routes and hooks. */
const BOOT = [
	moduleId('node_modules/@sveltejs/kit/src/runtime/client/client-entry.js'),
	moduleId('.svelte-kit/generated/build/client-optimized/app.js')
];

/** The modules `roots` import statically, short of the SDK. */
function staticImports(context: Rolldown.ChunkingContext, roots: string[]): Set<string> {
	const modules = new Set<string>();
	const pending = [...roots];
	for (let id = pending.pop(); id !== undefined; id = pending.pop()) {
		// The SDK's index modules re-export every client: a group takes only the SDK modules its
		// other modules use, as their dependencies.
		if (modules.has(id) || SDK.test(id)) continue;
		const info = context.getModuleInfo(id);
		if (!info) continue;
		modules.add(id);
		pending.push(...info.importedIds);
	}
	return modules;
}

/**
 * Fewer files for a first visit. Left to itself the bundler gives each set of modules shared by a
 * different set of routes its own file, which comes to about 250 requests on a signed-in route.
 * Grouped, a route makes a few dozen:
 * - start: what Kit's start file imports, in a file of its own. Kit's client reads the payload as
 *   its modules load, so none of them may share a file with what loads before the payload is set;
 * - boot: what the client and the app's entry import (Svelte, Kit, the data layer, i18n);
 * - ui and shell: the kit and the shell, each with its styles and the icons and messages only it uses;
 * - icons: those of eight routes or more; the others stay with the routes that use them.
 * A group also takes the static imports of what it captures, so what only a dynamic import reaches
 * (the SDK clients, MapLibre, PDF.js, hls.js, the demo) still loads when it is first used.
 */
function chunkGroups(): Rolldown.CodeSplittingOptions {
	const graphs = new WeakMap<Rolldown.ChunkingContext, { start: Set<string>; boot: Set<string> }>();
	const graph = (context: Rolldown.ChunkingContext) => {
		let found = graphs.get(context);
		if (!found) {
			found = { start: staticImports(context, [KIT_START]), boot: staticImports(context, BOOT) };
			graphs.set(context, found);
		}
		return found;
	};
	return {
		groups: [
			{
				name: (id, context) => (id !== KIT_START && graph(context).start.has(id) ? 'start' : null),
				priority: 5
			},
			{
				name: (id, context) =>
					graph(context).boot.has(id) && !context.getModuleInfo(id)?.isEntry ? 'boot' : null,
				priority: 4
			},
			{ name: 'ui', test: /[\\/]src[\\/]lib[\\/]ui[\\/]/, tags: ['$initial'], priority: 3 },
			{ name: 'shell', test: /[\\/]src[\\/]lib[\\/]shell[\\/]/, tags: ['$initial'], priority: 2 },
			{ name: 'icons', test: /^~icons[\\/]/, tags: ['$initial'], minShareCount: 8, priority: 1 }
		]
	};
}

export default defineConfig(({ mode }) => ({
	define: { __DEMO__: JSON.stringify(mode === 'demo') },
	plugins: [
		sveltekit({
			compilerOptions: {
				runes: ({ filename }) =>
					filename.split(/[/\\]/).includes('node_modules') ? undefined : true
			},
			// The demo build goes apart from the one hosts serve.
			adapter: adapter({
				pages: mode === 'demo' ? 'build-demo' : 'build',
				assets: mode === 'demo' ? 'build-demo' : 'build',
				fallback: 'index.html'
			}),
			// Registered by the root layout once the first page has settled (shell/serviceWorker.ts).
			serviceWorker: { register: false }
		}),
		paraglideVitePlugin({
			project: './project.inlang',
			outdir: './src/lib/paraglide',
			emitTsDeclarations: true,
			strategy: ['custom-language', 'preferredLanguage', 'baseLocale'],
			// One module per locale in dev, as `npm run i18n` writes; one per message in builds.
			outputStructure: process.env.NODE_ENV === 'production' ? 'message-modules' : 'locale-modules'
		}),
		Icons({ compiler: 'svelte' }),
		relayPlugin(),
		mapAssetsPlugin(),
		precachePlugin()
	],
	environments: {
		client: {
			build: {
				rolldownOptions: {
					/*
					 * The generated SDK runs nothing as it loads, which its package.json doesn't declare.
					 * Said here, a chunk loads only the SDK modules it uses: otherwise the error classes
					 * the boot code imports through the SDK's index modules bring every client and
					 * decoder (over half a megabyte) into the first download.
					 */
					treeshake: {
						moduleSideEffects: (id) => (SDK.test(id) ? false : undefined)
					},
					output: { codeSplitting: chunkGroups() }
				}
			}
		}
	},
	test: {
		expect: { requireAssertions: true },
		include: ['src/**/*.test.ts', 'relay/**/*.test.ts'],
		setupFiles: ['src/lib/testing/dom.ts']
	}
}));
