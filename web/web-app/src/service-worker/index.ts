import { dev, version } from '$app/env';
import { assets, immutable } from '$app/manifest';
import { self } from '$app/service-worker';
import { mapAssetFiles, mapAssetsBase } from 'virtual:map-assets';
import { precache } from 'virtual:precache';

/*
 * Offline start and instant loads. Pages come from the network when it answers, so the next load
 * after a deploy runs it, and from the kept shell when it doesn't; the code and files they load
 * come from the caches below. Personal data never passes through here: the data layer keeps it in
 * IndexedDB, and API calls (the relay, the services) go straight to the network.
 */

/** The shell page and the static files (fonts, icons), per deploy: their names carry no hash. */
const SHELL = `shell-${version}`;
/** Build files by content-hashed name: one kept for a deploy serves every later deploy with it. */
const CODE = 'code';
/** The basemap: style, glyphs and sprite ahead of use; each tile once the map first draws it. */
const MAP = 'map';

const scope = new URL(self.registration.scope);
const at = (path: string) => new URL(path, scope).pathname;

const page = at('');
const statics = new Set(
	assets
		.map(({ path }) => path)
		.filter((path) => !path.endsWith('.txt') && !path.startsWith('_'))
		.map(at)
);
const codeRoot = at('_app/immutable/');
const mapFiles = mapAssetFiles.map((file) => at(mapAssetsBase + file));
const tileRoot = at(`${mapAssetsBase}tiles/`);
const isMap = (path: string) => path.startsWith(tileRoot) || mapFiles.includes(path);

if (!dev) {
	self.addEventListener('install', (event) => {
		event.waitUntil(
			Promise.all([
				caches.open(SHELL).then((cache) => cache.addAll([page, ...statics])),
				keep(CODE, precache.map(at)),
				keep(MAP, mapFiles)
			])
		);
	});

	self.addEventListener('activate', (event) => {
		const current = new Set(immutable.map(({ path }) => at(path)));
		event.waitUntil(
			(async () => {
				for (const name of await caches.keys()) {
					if (![SHELL, CODE, MAP].includes(name)) await caches.delete(name);
				}
				await prune(CODE, (path) => current.has(path));
				await prune(MAP, isMap);
				await self.registration.navigationPreload?.enable();
				await self.clients.claim();
			})()
		);
	});

	self.addEventListener('fetch', (event) => {
		const { request } = event;
		const url = new URL(request.url);
		if (request.method !== 'GET' || url.origin !== scope.origin) return;
		if (request.mode === 'navigate') event.respondWith(navigate(event));
		else if (url.pathname.startsWith(codeRoot)) event.respondWith(fromCode(event, url.pathname));
		else if (isMap(url.pathname)) event.respondWith(fromMap(event, url.pathname));
		else if (statics.has(url.pathname)) event.respondWith(fromShell(request, url.pathname));
	});
}

/** Adds the files `name` lacks: content-hashed files already kept are never downloaded again. */
async function keep(name: string, paths: string[]): Promise<void> {
	const cache = await caches.open(name);
	const kept = new Set((await cache.keys()).map((request) => new URL(request.url).pathname));
	await cache.addAll(paths.filter((path) => !kept.has(path)));
}

async function prune(name: string, wanted: (path: string) => boolean): Promise<void> {
	const cache = await caches.open(name);
	for (const request of await cache.keys()) {
		if (!wanted(new URL(request.url).pathname)) await cache.delete(request);
	}
}

async function navigate(event: FetchEvent): Promise<Response> {
	try {
		const preloaded = (await event.preloadResponse) as Response | undefined;
		return preloaded ?? (await fetch(event.request));
	} catch {
		return (await (await caches.open(SHELL)).match(page)) ?? Response.error();
	}
}

async function fromShell(request: Request, path: string): Promise<Response> {
	return (await (await caches.open(SHELL)).match(path)) ?? fetch(request);
}

async function fromCode(event: FetchEvent, path: string): Promise<Response> {
	const cache = await caches.open(CODE);
	const kept = await cache.match(path);
	if (kept) return kept;
	const response = await fetch(event.request);
	// A host answering a missing file with the app's page must not have it kept as code.
	if (response.ok && !response.headers.get('Content-Type')?.includes('text/html')) {
		event.waitUntil(cache.put(path, response.clone()));
	}
	return response;
}

async function fromMap(event: FetchEvent, path: string): Promise<Response> {
	const cache = await caches.open(MAP);
	const kept = await cache.match(path);
	if (kept) return kept;
	const response = await fetch(event.request);
	if (response.status === 200 && !response.headers.get('Content-Type')?.includes('text/html')) {
		event.waitUntil(cache.put(path, response.clone()));
	}
	return response;
}
