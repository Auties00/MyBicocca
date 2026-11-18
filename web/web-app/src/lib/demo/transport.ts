import { decodeSetCookies } from '@mybicocca/relay/protocol';
import { dev } from '$app/env';
import type { MoodleSignIn } from '#lib/api/auth/moodleLogin.ts';
import { CookieJar } from '#lib/api/cookies.ts';
import { NetworkError } from '#lib/api/errors.ts';
import type { Transport } from '#lib/api/transport.ts';
import {
	DEMO_SET_COOKIE,
	esse3Error,
	json,
	match,
	moodleError,
	type DemoMethod,
	type DemoRequest,
	type DemoRoute
} from './router.ts';
import { readScenarioRules, scenarioFor, serviceOf } from './scenarios.ts';
import { resetDemoState } from './state.ts';
import { DEMO_MOODLE_SESSION, DEMO_PRIVATE_TOKEN, DEMO_WS_TOKEN, esse3Accepts } from './tokens.ts';

export { DEMO_PASSWORD, DEMO_USERNAME } from './tokens.ts';

const modules = import.meta.glob<{ routes?: readonly DemoRoute[] }>(
	['./fixtures/**/*.ts', '!./fixtures/**/*.test.ts'],
	{ eager: true }
);
const routes: readonly DemoRoute[] = Object.values(modules).flatMap(
	(module) => module.routes ?? []
);

/** Enough delay for loading states to show, as on a fast connection. */
const LATENCY_MS = 120;
/** The `slow` scenario's extra delay: long enough to look at skeletons. */
const SLOW_MS = 2_500;

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

/**
 * Answers a demo request from the fixtures, with the checks the real servers make first. The
 * jar's cookies go along in `cookie`, and the cookies a fixture sets come back into it.
 */
async function dispatch(
	jar: CookieJar,
	input: RequestInfo | URL,
	init: RequestInit = {}
): Promise<Response> {
	const url = new URL(input instanceof Request ? input.url : String(input));
	const method = (init.method ?? 'GET').toUpperCase() as DemoMethod;
	const headers = new Headers(init.headers);
	const cookie = [jar.header(url), headers.get('cookie')].filter(Boolean).join('; ');
	if (cookie !== '') headers.set('cookie', cookie);
	const scenario = dev ? scenarioFor(readScenarioRules(), method, url) : undefined;
	await delay(scenario === 'slow' ? LATENCY_MS + SLOW_MS : LATENCY_MS);
	if (scenario === 'offline') {
		// What the live transport throws when fetch fails with the device offline.
		throw new NetworkError('Failed to fetch', {
			cause: new TypeError('Failed to fetch'),
			offline: true
		});
	}

	const body = new Response(init.body ?? null);
	const form = async (): Promise<URLSearchParams> => {
		if (method === 'GET') return url.searchParams;
		if (init.body instanceof URLSearchParams) return init.body;
		if (init.body instanceof FormData) {
			const fields = new URLSearchParams();
			for (const [name, value] of init.body)
				if (typeof value === 'string') fields.append(name, value);
			return fields;
		}
		return new URLSearchParams(await body.clone().text());
	};
	const request = (params: Record<string, string>): DemoRequest => ({
		method,
		url,
		params,
		headers,
		text: () => body.clone().text(),
		json: async <T>() => JSON.parse(await body.clone().text()) as T,
		form,
		formData: () =>
			init.body instanceof FormData ? Promise.resolve(init.body) : body.clone().formData()
	});

	const refused = await gate(url, headers, request({}));
	const response =
		refused ??
		(scenario === 'error'
			? failure(url)
			: await answer(method, url, request, scenario === 'empty'));
	const setCookies = response.headers.get(DEMO_SET_COOKIE);
	if (setCookies !== null) {
		jar.store(decodeSetCookies(setCookies), url);
		response.headers.delete(DEMO_SET_COOKIE);
	}
	if (response.url === '') Object.defineProperty(response, 'url', { value: url.href });
	return response;
}

/** What the real server answers to a request without valid credentials. */
async function gate(
	url: URL,
	headers: Headers,
	request: DemoRequest
): Promise<Response | undefined> {
	if (url.host === 's3w.si.unimib.it' && url.pathname.startsWith('/e3rest/api/')) {
		const minting = url.pathname === '/e3rest/api/login/jwt/new';
		if (!esse3Accepts(headers.get('authorization'), minting)) {
			return esse3Error(401, 'Utente non autenticato');
		}
	}
	if (url.host === 'elearning.unimib.it' && url.pathname === '/webservice/rest/server.php') {
		const form = await request.form();
		const token = form.get('wstoken') ?? url.searchParams.get('wstoken');
		if (token !== DEMO_WS_TOKEN) return moodleError('invalidtoken', 'Token non valido');
	}
	return undefined;
}

async function answer(
	method: DemoMethod,
	url: URL,
	request: (params: Record<string, string>) => DemoRequest,
	empty: boolean
): Promise<Response> {
	for (const candidate of routes) {
		const params = match(candidate, method, url);
		if (params === undefined) continue;
		if (!empty) return candidate.handle(request(params));
		if (candidate.empty !== undefined) return candidate.empty(request(params));
		if (dev) console.warn(`[demo] ${method} ${url.href} has no empty variant; answering as usual`);
		return candidate.handle(request(params));
	}
	if (dev) console.error(`[demo] No fixture answers ${method} ${url.href}`);
	return new Response('No demo fixture', { status: 404 });
}

/** How the service fails, in its own error format, so the SDK raises its own error. */
function failure(url: URL): Response {
	switch (serviceOf(url)) {
		case 'esse3':
			return esse3Error(500, 'Errore interno del server');
		case 'moodle':
			return moodleError('dmlreadexception', 'Errore durante la lettura del database');
		case 'planning':
			return json({ message: 'server_error' }, { status: 500 });
		case 'affluences':
			return json(
				{ error: { code: 'server_error', message: 'Internal server error' } },
				{ status: 500 }
			);
		default:
			return new Response('<h1>500 Internal Server Error</h1>', {
				status: 500,
				headers: { 'content-type': 'text/html' }
			});
	}
}

export const demoTransport: Transport = {
	fetch: (options = {}) => {
		const jar = options.jar;
		// Without a session's jar, each request gets one of its own, as on the live transport.
		return (input, init) => dispatch(jar ?? new CookieJar(), input, init);
	},
	autologin: async () => DEMO_MOODLE_SESSION,
	moodleSignIn: async () => demoMoodleSignIn()
};

/** The demo's Moodle sign-in: the fixtures need no SSO, only the token they check. */
function demoMoodleSignIn(): MoodleSignIn {
	return {
		tokens: { wsToken: DEMO_WS_TOKEN, privateToken: DEMO_PRIVATE_TOKEN },
		cookies: [
			{
				name: 'MoodleSession',
				value: DEMO_MOODLE_SESSION,
				domain: 'elearning.unimib.it',
				hostOnly: true,
				path: '/',
				secure: true
			}
		]
	};
}

/** Forgets what the demo changed, when the demo account signs out. */
export function resetDemo(): void {
	resetDemoState();
}
