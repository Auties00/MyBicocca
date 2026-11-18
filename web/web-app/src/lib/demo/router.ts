/**
 * The demo's stand-in servers. Every request of the demo account reaches `dispatch` instead of the
 * network, and the SDK decodes the answers exactly as it decodes live ones, so every page runs the
 * same code in the demo as in production.
 *
 * ## Adding fixtures
 *
 * Fixtures live in `src/lib/demo/fixtures/<service>/<sdk-module>.ts`, one file per SDK API module:
 * `esse3/careers.ts` answers `esse3.careers.*`, `moodle/course.ts` answers `moodle.course.*`,
 * `easystaff/schedule.ts`, `planning/reservations.ts`, `affluences/sites.ts`, `kaltura/playback.ts`.
 * When the module's file already exists and belongs to another feature, add
 * `<sdk-module>.<feature>.ts` beside it instead of editing it. Each file exports its routes:
 *
 * ```ts
 * import { DemoCareer, DemoStudent } from '../../persona.ts';
 * import { json, route } from '../../router.ts';
 * import { demoState } from '../../state.ts';
 *
 * const bookings = () => demoState('esse3.bookings', () => [...initialBookings]);
 *
 * export const routes = [
 *   route('GET https://s3w.si.unimib.it/e3rest/api/libretto-service-v2/libretti/:matId/prenotazioni',
 *     () => json(bookings())),
 *   route('POST https://s3w.si.unimib.it/e3rest/api/calesa-service-v1/appelli/:cdsId/:adId/:appId/iscritti',
 *     async (request) => { bookings().push(…); return json({ … }); })
 * ];
 * ```
 *
 * Files are found by `import.meta.glob`: no index to edit, and a helper module beside them that
 * exports no `routes` is skipped. The rules:
 * - Answer in the service's real wire format: JSON with the wire field names the SDK's decoders read
 *   (`stuId`, not `studentId`), HTML where the SDK scrapes a page, bytes for files.
 * - Date everything from today with `dates.ts` (no fake clock): `days(8, 9, 30)`, and Android demo
 *   dates with `fromAndroid(2025, 1, 22)`, which keeps their distance from Android's frozen day.
 * - Use the persona (`persona.ts`) so the same student, careers and courses appear everywhere.
 * - Writes change state from `demoState`, which lives until the demo signs out, so booking an exam
 *   makes it appear among the booked ones. What must outlive a reload goes in `sessionStorage`
 *   under `DEMO_SESSION_PREFIX` (`state.ts`), which the sign-out clears too.
 * - Cookies work as on the servers: a route sets them with `withCookies(response, 'name=value;
 *   Path=/')`, the client's jar keeps them, and later requests carry them in `cookie`. A sign-in
 *   chain that ends with a session cookie runs once, not before every call.
 * - Give list routes an empty variant, the answer when there is nothing to show (Esse3 answers an
 *   empty list with a 404): `route(pattern, handler, { empty: () => esse3Error(404, 'Nessun dato') })`.
 * - Esse3 calls need the demo JWT and Moodle calls the demo token: `transport.ts` checks both, as the
 *   servers do, before a route runs. A request no route answers fails with a 404, logged in dev.
 *
 * Patterns: `:name` matches one path segment (in `request.params`), a trailing `*` the rest of the
 * path; a query in the pattern must be present with that value, so Moodle's web services route by
 * function: `POST https://elearning.unimib.it/webservice/rest/server.php?wsfunction=core_webservice_get_site_info`.
 *
 * ## Scenarios
 *
 * To see a page loading, empty, failing or offline, set scenarios from DevTools; they apply to the
 * next request, with no reload, on the dev server only:
 *
 * ```js
 * localStorage.setItem('mybicocca:demo-scenarios', JSON.stringify({
 *   'GET https://s3w.si.unimib.it/e3rest/api/libretto-service-v2/libretti/:matId/prenotazioni': 'empty',
 *   'GET *libretti*righe*': 'error',
 *   esse3: 'slow',
 *   '*': 'offline'
 * }));
 * localStorage.removeItem('mybicocca:demo-scenarios'); // back to normal
 * ```
 *
 * Keys are a service (`esse3`, `moodle`, `moodleWeb`, `kaltura`, `easystaff`, `planning`,
 * `affluences`), a route pattern as `route()` takes it, where `*` matches any text and the method
 * is optional, or `*` for every request. The most specific match wins: a pattern with a method,
 * then one without, then a service, then `*`; between patterns, the longer literal text. Scenarios:
 * - `slow`: the usual answer, 2.5 s late, so skeletons show;
 * - `error`: the service fails in its own format (Esse3 500 with `retErrMsg`, a Moodle exception),
 *   so the SDK raises its own error;
 * - `offline`: the request fails as with no network, which `toDataError` maps to `offline`;
 * - `empty`: the route's empty variant; a route without one answers as usual, with a dev warning.
 */

import { encodeSetCookies } from '@mybicocca/relay/protocol';

export type DemoMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface DemoRequest {
	method: DemoMethod;
	url: URL;
	/** The pattern's `:name` segments, decoded. */
	params: Readonly<Record<string, string>>;
	headers: Headers;
	text(): Promise<string>;
	/** The urlencoded or multipart form's text fields, or the query string of a GET. */
	form(): Promise<URLSearchParams>;
	/** The multipart form with its files, as Moodle's uploads send it. */
	formData(): Promise<FormData>;
	json<T>(): Promise<T>;
}

export type DemoHandler = (request: DemoRequest) => Response | Promise<Response>;

export interface DemoRoute {
	readonly method: DemoMethod;
	readonly host: string;
	readonly segments: readonly string[];
	readonly query: ReadonlyArray<readonly [string, string]>;
	readonly handle: DemoHandler;
	/** The answer when there is nothing to show, for the `empty` scenario. */
	readonly empty?: DemoHandler;
}

/** A route for `spec`, written `METHOD https://host/path[?name=value]`. */
export function route(
	spec: string,
	handle: DemoHandler,
	variants: { empty?: DemoHandler } = {}
): DemoRoute {
	const [method, address] = spec.trim().split(/\s+/, 2) as [DemoMethod, string];
	const url = new URL(address);
	return {
		method,
		host: url.host,
		segments: url.pathname.split('/').slice(1),
		query: [...url.searchParams],
		handle,
		...variants
	};
}

/** The params of `url` if `candidate` answers it. */
export function match(
	candidate: DemoRoute,
	method: string,
	url: URL
): Record<string, string> | undefined {
	if (candidate.method !== method || candidate.host !== url.host) return undefined;
	if (candidate.query.some(([name, value]) => url.searchParams.get(name) !== value))
		return undefined;
	const segments = url.pathname.split('/').slice(1);
	const params: Record<string, string> = {};
	for (let index = 0; index < candidate.segments.length; index++) {
		const expected = candidate.segments[index];
		if (expected === '*' && index === candidate.segments.length - 1) return params;
		const actual = segments[index];
		if (actual === undefined) return undefined;
		if (expected.startsWith(':')) params[expected.slice(1)] = decodeURIComponent(actual);
		else if (expected !== actual) return undefined;
	}
	return segments.length === candidate.segments.length ? params : undefined;
}

export function json(body: unknown, init: ResponseInit = {}): Response {
	return Response.json(body, init);
}

/** A page for the SDK's scrapers. */
export function html(body: string, init: ResponseInit = {}): Response {
	return new Response(body, {
		...init,
		headers: { 'content-type': 'text/html; charset=utf-8', ...init.headers }
	});
}

export function text(body: string, type = 'text/plain'): Response {
	return new Response(body, { headers: { 'content-type': type } });
}

export function bytes(data: Uint8Array<ArrayBuffer> | Blob, type: string): Response {
	return new Response(data, { headers: { 'content-type': type } });
}

export function empty(status = 204): Response {
	return new Response(null, { status });
}

/** Carries a fixture's `Set-Cookie`s: browsers drop `Set-Cookie` from a constructed Response. */
export const DEMO_SET_COOKIE = 'x-demo-set-cookie';

/** `response`, setting `cookies` (`Set-Cookie` values) in the client's jar as a server would. */
export function withCookies(response: Response, ...cookies: string[]): Response {
	response.headers.set(DEMO_SET_COOKIE, encodeSetCookies(cookies));
	return response;
}

/** An Esse3 failure as e3rest sends it, `retErrMsg` in Italian. */
export function esse3Error(status: number, message: string, code = -1): Response {
	return json({ retCode: code, retErrMsg: message }, { status });
}

/** A Moodle web service failure: Moodle answers 200 with the exception. */
export function moodleError(errorcode: string, message = errorcode): Response {
	return json({ exception: 'moodle_exception', errorcode, message });
}
