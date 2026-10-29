import {
	RelayHeader,
	decodeSetCookies,
	relayPath,
	type AutologinRequest
} from '@mybicocca/relay/protocol';
import { moodleAutologin } from './auth/moodleAutologin.ts';
import { MOODLE_URL, moodleLogin, type MoodleSignIn } from './auth/moodleLogin.ts';
import { CookieJar } from './cookies.ts';
import { NetworkError, RelayError } from './errors.ts';
import { routeOf } from './routes.ts';

export interface TransportOptions {
	/**
	 * The session's cookies, sent through the relay and updated from its answers. Without one, each
	 * request gets a jar of its own, so cookies still travel along its redirects.
	 */
	jar?: CookieJar;
	/**
	 * `follow` (the default) follows redirects like `fetch`; `manual` returns them, `Location`
	 * readable. A request's own `redirect: 'manual'` overrides it.
	 */
	redirect?: 'follow' | 'manual';
	/**
	 * For the whole redirect chain; Esse3 needs 60 s, everything else 30 s. A request with a body
	 * gets more on top, by its size, so an upload on a slow link isn't cut off.
	 */
	timeoutMs?: number;
	/** Aborts every request made with this fetch, such as when a sign-in runs out of time. */
	signal?: AbortSignal;
	/** The browser's `fetch`, replaced in tests. */
	fetch?: typeof fetch;
}

/**
 * How an account's clients reach the services: the live network (through the relay where
 * needed) or, for the demo account, the fixtures that stand in for every server.
 */
export interface Transport {
	fetch(options?: TransportOptions): typeof fetch;
	/** A new `MoodleSession` from the mobile tokens, or undefined when Moodle refuses. */
	autologin(request: AutologinRequest): Promise<string | undefined>;
	/**
	 * Signs into Moodle as its mobile app does, for the tokens and the logged-in pages' cookies.
	 *
	 * @throws LoginError
	 */
	moodleSignIn(username: string, password: string, signal?: AbortSignal): Promise<MoodleSignIn>;
}

export const liveTransport: Transport = {
	fetch: (options) => createFetch(options),
	autologin: (request) => moodleAutologin(request),
	async moodleSignIn(username, password, signal) {
		const jar = new CookieJar();
		const send = createFetch({
			jar,
			timeoutMs: 30_000,
			...(signal === undefined ? {} : { signal })
		});
		const tokens = await moodleLogin(send, username, password);
		return { tokens, cookies: jar.forHost(new URL(MOODLE_URL).hostname) };
	}
};

const MAX_REDIRECTS = 10;

/**
 * Headers browsers refuse to send from scripts; the relay takes them prefixed, direct requests
 * lose them. Firefox would send a scripted User-Agent and list it in the preflight, which
 * Affluences answers with a 403.
 */
const FORBIDDEN = ['cookie', 'origin', 'user-agent', 'referer'] as const;

/** At most this many requests in flight per host, to go easy on the university's servers. */
const HOST_LIMITS: Readonly<Record<string, number>> = {
	's3w.si.unimib.it': 4,
	'gestioneorari.didattica.unimib.it': 2
};

/** Statuses after which a GET is sent once more: the server failed, perhaps only for a moment. */
const RETRIED_STATUSES: ReadonlySet<number> = new Set([500, 502, 503, 504]);
const RETRY_DELAY_MS = 800;
/** The slowest upload worth waiting for, in bytes per millisecond: about 32 kB/s. */
const SLOWEST_UPLOAD = 32;

interface Hop {
	method: string;
	headers: Headers;
	body: BodyInit | null | undefined;
}

/**
 * A `fetch` for the SDK clients with `fetch` semantics on top of the relay: hosts without CORS
 * are reached through it, with the cookie jar, the headers browsers forbid and the redirects
 * handled here; the rest go straight to the host. `response.url` is the URL that answered, which
 * the SDK's scrapers resolve links against. A GET the server fails with a 5xx is sent once more.
 */
export function createFetch(options: TransportOptions = {}): typeof fetch {
	const send = options.fetch ?? ((input, init) => fetch(input, init));
	return async (input, init = {}) => {
		const url = new URL(input instanceof Request ? input.url : String(input));
		const headers = new Headers(init.headers);
		const forbidden = new Map<string, string>();
		for (const name of FORBIDDEN) {
			const value = headers.get(name);
			if (value !== null) forbidden.set(name, value);
			headers.delete(name);
		}
		const jar = options.jar ?? new CookieJar();
		const follow = (init.redirect ?? options.redirect ?? 'follow') !== 'manual';
		const method = (init.method ?? 'GET').toUpperCase();
		const timeoutMs =
			(options.timeoutMs ?? 30_000) + Math.ceil(bodySize(init.body) / SLOWEST_UPLOAD);
		const callerSignals = [options.signal, init.signal].filter(
			(item): item is AbortSignal => item instanceof AbortSignal
		);
		const attempt = () =>
			chain(
				send,
				url,
				{ method, headers: new Headers(headers), body: init.body },
				{
					forbidden,
					jar,
					follow,
					signal: AbortSignal.any([AbortSignal.timeout(timeoutMs), ...callerSignals])
				}
			);

		const response = await attempt();
		if (method !== 'GET' || !RETRIED_STATUSES.has(response.status) || carriesPassword(headers))
			return response;
		await response.body?.cancel();
		await pause(RETRY_DELAY_MS, AbortSignal.any(callerSignals));
		return attempt();
	};
}

/** Sends `first` and, unless told not to, the requests its redirects lead to. */
async function chain(
	send: typeof fetch,
	start: URL,
	first: Hop,
	context: {
		forbidden: ReadonlyMap<string, string>;
		jar: CookieJar;
		follow: boolean;
		signal: AbortSignal;
	}
): Promise<Response> {
	let url = start;
	let { method, body } = first;
	const { headers } = first;
	for (let hop = 0; ; hop++) {
		const hopInit: RequestInit = {
			method,
			headers,
			body,
			signal: context.signal,
			cache: 'no-store'
		};
		// Cookies the SDK set itself (Planning's filters) belong to the host it addressed.
		const tunneled = new Map(context.forbidden);
		if (url.origin !== start.origin) tunneled.delete('cookie');
		const response =
			routeOf(url) === 'direct'
				? await direct(send, url, hopInit)
				: await relayed(send, url, hopInit, tunneled, context.jar);
		const location = response.headers.get('location');
		if (!context.follow || location === null || !isRedirect(response.status))
			return withUrl(response, url);
		const next = new URL(location, url);
		if (next.protocol !== 'https:') return withUrl(response, url);
		if (hop === MAX_REDIRECTS) throw new NetworkError(`Too many redirects from ${start.origin}`);
		await response.body?.cancel();
		if (
			response.status === 303 ||
			((response.status === 301 || response.status === 302) && method === 'POST')
		) {
			method = 'GET';
			body = undefined;
			headers.delete('content-type');
		}
		url = next;
	}
}

/** A sign-in's own request: sending it twice would count as two failed attempts at the IdP. */
function carriesPassword(headers: Headers): boolean {
	return headers.get('authorization')?.startsWith('Basic ') === true;
}

/** The bytes `body` sends, as far as they are known before sending it. */
function bodySize(body: BodyInit | null | undefined): number {
	if (body === null || body === undefined) return 0;
	if (typeof body === 'string') return body.length;
	if (body instanceof Blob) return body.size;
	if (body instanceof ArrayBuffer || ArrayBuffer.isView(body)) return body.byteLength;
	if (body instanceof URLSearchParams) return body.toString().length;
	if (body instanceof FormData) {
		let size = 0;
		for (const [, value] of body) size += typeof value === 'string' ? value.length : value.size;
		return size;
	}
	return 0;
}

function pause(ms: number, signal: AbortSignal): Promise<void> {
	return new Promise((resolve, reject) => {
		if (signal.aborted) {
			reject(signal.reason);
			return;
		}
		const timer = setTimeout(resolve, ms);
		signal.addEventListener(
			'abort',
			() => {
				clearTimeout(timer);
				reject(signal.reason);
			},
			{ once: true }
		);
	});
}

function direct(send: typeof fetch, url: URL, init: RequestInit): Promise<Response> {
	return network(() =>
		send(url, { ...init, credentials: 'omit', mode: 'cors', redirect: 'follow' })
	);
}

async function relayed(
	send: typeof fetch,
	url: URL,
	init: RequestInit,
	tunneled: ReadonlyMap<string, string>,
	jar: CookieJar
): Promise<Response> {
	const headers = new Headers(init.headers);
	const cookie = [jar.header(url), tunneled.get('cookie')].filter(Boolean).join('; ');
	if (cookie !== '') headers.set(`${RelayHeader.tunneled}cookie`, cookie);
	for (const name of ['origin', 'user-agent', 'referer']) {
		const value = tunneled.get(name);
		if (value !== undefined) headers.set(`${RelayHeader.tunneled}${name}`, value);
	}
	const response = await limited(url.hostname, () =>
		network(() => send(relayPath(url), { ...init, headers, credentials: 'omit' }))
	);
	if (response.headers.has(RelayHeader.error)) throw await RelayError.from(response);
	jar.store(decodeSetCookies(response.headers.get(RelayHeader.setCookie)), url);

	const status = Number(response.headers.get(RelayHeader.status) ?? response.status);
	const out = new Headers();
	for (const [name, value] of response.headers) {
		if (!name.startsWith('x-relay-')) out.set(name, value);
	}
	const location = response.headers.get(RelayHeader.location);
	if (location !== null) out.set('location', location);
	const empty = location !== null || status === 204 || status === 205 || status === 304;
	return new Response(empty ? null : response.body, { status, headers: out });
}

function isRedirect(status: number): boolean {
	return status === 301 || status === 302 || status === 303 || status === 307 || status === 308;
}

function withUrl(response: Response, url: URL): Response {
	if (response.url === '') Object.defineProperty(response, 'url', { value: url.href });
	return response;
}

/** Tells a failed connection from a programming error: both reach callers of `fetch` as TypeError. */
async function network(run: () => Promise<Response>): Promise<Response> {
	try {
		return await run();
	} catch (error) {
		if (error instanceof TypeError) {
			throw new NetworkError(error.message, { cause: error, offline: navigator.onLine === false });
		}
		throw error;
	}
}

const inFlight = new Map<string, number>();
const waiting = new Map<string, Array<() => void>>();

async function limited<T>(host: string, run: () => Promise<T>): Promise<T> {
	const limit = HOST_LIMITS[host];
	if (limit === undefined) return run();
	while ((inFlight.get(host) ?? 0) >= limit) {
		await new Promise<void>((resolve) =>
			waiting.set(host, [...(waiting.get(host) ?? []), resolve])
		);
	}
	inFlight.set(host, (inFlight.get(host) ?? 0) + 1);
	try {
		return await run();
	} finally {
		inFlight.set(host, (inFlight.get(host) ?? 1) - 1);
		const [next, ...rest] = waiting.get(host) ?? [];
		waiting.set(host, rest);
		next?.();
	}
}
