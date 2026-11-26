/**
 * Smoke-tests a deployed web-app origin (a Cloudflare Version URL, PR preview, or production).
 * Verifies the HTML shell, an immutable JS bundle, the service worker, SPA fallback routing, and
 * the Worker CORS relay before or after promoting traffic.
 */

const rawBaseUrl = process.argv[2];
if (!rawBaseUrl) {
	console.error('Usage: node scripts/smoke.ts <base-url>');
	process.exit(1);
}

const baseUrl = rawBaseUrl.replace(/\/+$/, '');
const MAX_ATTEMPTS = 8;
const RETRY_DELAY_MS = 3_000;
const REQUEST_TIMEOUT_MS = 15_000;

async function sleep(ms: number): Promise<void> {
	await new Promise((resolve) => setTimeout(resolve, ms));
}

async function fetchWithTimeout(url: string, init?: RequestInit): Promise<Response> {
	const controller = new AbortController();
	const timer = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
	try {
		return await fetch(url, { redirect: 'follow', ...init, signal: controller.signal });
	} finally {
		clearTimeout(timer);
	}
}

async function retry<T>(label: string, fn: () => Promise<T>): Promise<T> {
	let lastError: unknown;
	for (let attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
		try {
			return await fn();
		} catch (error) {
			lastError = error;
			if (attempt < MAX_ATTEMPTS) {
				const message = error instanceof Error ? error.message : String(error);
				console.log(`Retry ${attempt}/${MAX_ATTEMPTS} (${label}): ${message}`);
				await sleep(RETRY_DELAY_MS);
			}
		}
	}
	throw lastError;
}

function assert(condition: unknown, message: string): asserts condition {
	if (!condition) throw new Error(message);
}

async function main(): Promise<void> {
	console.log(`Smoke-testing ${baseUrl}`);

	// 1. Root HTML shell (retried while freshly uploaded version / DNS propagates across edge PoPs).
	const { html } = await retry('GET /', async () => {
		const res = await fetchWithTimeout(`${baseUrl}/`);
		const body = await res.text();
		assert(res.status === 200, `GET / expected 200, got ${res.status}`);
		const contentType = res.headers.get('content-type') ?? '';
		assert(contentType.includes('text/html'), `GET / expected text/html, got "${contentType}"`);
		const cacheControl = res.headers.get('cache-control') ?? '';
		assert(
			cacheControl.includes('no-cache'),
			`GET / expected Cache-Control to include no-cache, got "${cacheControl}"`
		);
		assert(
			body.includes('_app/immutable/'),
			'GET / HTML does not reference any _app/immutable/ bundle'
		);
		return { html: body };
	});
	console.log('PASS GET / (200 text/html, no-cache)');

	// 2. First referenced immutable JS bundle.
	const match = /(?:\.\/|\/)(_app\/immutable\/[^\s"'<>]+\.js)/.exec(html);
	assert(match?.[1], 'Could not extract an immutable JS asset path from index.html');
	const bundlePath = `/${match[1]}`;
	const bundleRes = await fetchWithTimeout(`${baseUrl}${bundlePath}`);
	assert(bundleRes.status === 200, `GET ${bundlePath} expected 200, got ${bundleRes.status}`);
	const bundleType = bundleRes.headers.get('content-type') ?? '';
	assert(
		bundleType.includes('javascript'),
		`GET ${bundlePath} expected javascript, got "${bundleType}"`
	);
	const bundleCache = bundleRes.headers.get('cache-control') ?? '';
	assert(
		bundleCache.includes('immutable'),
		`GET ${bundlePath} expected immutable Cache-Control, got "${bundleCache}"`
	);
	console.log(`PASS GET ${bundlePath} (200 javascript, immutable)`);

	// 3. Service worker entrypoint.
	const swRes = await fetchWithTimeout(`${baseUrl}/service-worker.js`);
	assert(swRes.status === 200, `GET /service-worker.js expected 200, got ${swRes.status}`);
	const swType = swRes.headers.get('content-type') ?? '';
	assert(
		swType.includes('javascript'),
		`GET /service-worker.js expected javascript, got "${swType}"`
	);
	const swCache = swRes.headers.get('cache-control') ?? '';
	assert(
		swCache.includes('no-cache'),
		`GET /service-worker.js expected Cache-Control to include no-cache, got "${swCache}"`
	);
	console.log('PASS GET /service-worker.js (200 javascript, no-cache)');

	// 4. SPA deep-link fallback.
	const spaRes = await fetchWithTimeout(`${baseUrl}/login`);
	assert(spaRes.status === 200, `GET /login expected 200, got ${spaRes.status}`);
	const spaType = spaRes.headers.get('content-type') ?? '';
	assert(spaType.includes('text/html'), `GET /login expected text/html, got "${spaType}"`);
	console.log('PASS GET /login (200 text/html SPA fallback)');

	// 5. Worker CORS relay route (must run Worker code, never fall back to SPA HTML).
	const relayRes = await fetchWithTimeout(`${baseUrl}/relay/example.com/`);
	const relayBody = await relayRes.text();
	assert(relayRes.status === 403, `GET /relay/example.com/ expected 403, got ${relayRes.status}`);
	const relayType = relayRes.headers.get('content-type') ?? '';
	assert(
		relayType.includes('application/json'),
		`GET /relay/example.com/ expected application/json, got "${relayType}"`
	);
	assert(
		relayBody.includes('host_not_allowed'),
		`GET /relay/example.com/ expected host_not_allowed error, got "${relayBody}"`
	);
	console.log('PASS GET /relay/example.com/ (403 JSON host_not_allowed)');

	console.log('All smoke checks passed.');
}

main().catch((error: unknown) => {
	console.error(`Smoke check failed: ${error instanceof Error ? error.message : String(error)}`);
	process.exit(1);
});
