import { relayError } from './errors.ts';
import {
	MOODLE_MOBILE_USER_AGENT,
	type AutologinRequest,
	type AutologinResponse
} from './protocol.ts';

const MOODLE = 'https://elearning.unimib.it';
const KEY_URL = `${MOODLE}/webservice/rest/server.php?wsfunction=tool_mobile_get_autologin_key&moodlewsrestformat=json`;
const LANDING_PATH = '/user/profile.php';
/** Moodle hands out one key per user every six minutes; asking sooner only burns the window. */
const MIN_INTERVAL_MS = 6 * 60_000;
const MAX_HOPS = 10;
const TIMEOUT_MS = 30_000;

/**
 * Renews a `MoodleSession` from the mobile tokens in one invocation: Moodle only accepts the key
 * from the IP that asked for it, within a minute, and only hands it to its mobile app's User-Agent,
 * none of which a browser can satisfy on its own.
 */
export function createAutologin(
	send: typeof fetch,
	now: () => number
): (request: Request) => Promise<Response> {
	const lastCall = new Map<number, number>();

	return async (request) => {
		if (request.method !== 'POST') return relayError(405, 'method_not_allowed', 'POST only');
		const body = parse(await request.text());
		if (body === undefined)
			return relayError(400, 'bad_request', 'Expected wstoken, privatetoken, userid');

		const previous = lastCall.get(body.userid);
		if (previous !== undefined && now() - previous < MIN_INTERVAL_MS) {
			return relayError(429, 'autologin_too_soon', 'One autologin per user every six minutes');
		}
		lastCall.set(body.userid, now());

		const signal = AbortSignal.timeout(TIMEOUT_MS);
		try {
			const key = await requestKey(send, body, signal);
			if ('refused' in key) {
				const tooSoon = key.refused === 'autologinkeygenerationlockout';
				return tooSoon
					? relayError(429, 'autologin_too_soon', 'Moodle refused a key this soon')
					: relayError(409, 'autologin_refused', 'Moodle refused the key', {
							moodleCode: key.refused
						});
			}
			const session = await spendKey(send, key.url, body.userid, key.key, signal);
			if (session === undefined) {
				return relayError(409, 'autologin_refused', 'The autologin did not log in', {
					moodleCode: 'landed_on_login_page'
				});
			}
			const answer: AutologinResponse = { moodleSession: session };
			return Response.json(answer, { headers: { 'cache-control': 'no-store' } });
		} catch (error) {
			if (error instanceof DOMException && error.name === 'TimeoutError') {
				return relayError(504, 'upstream_timeout', 'Moodle did not answer in time');
			}
			return relayError(502, 'upstream_unreachable', 'Moodle could not be reached');
		}
	};
}

function parse(text: string): AutologinRequest | undefined {
	try {
		const value: unknown = JSON.parse(text);
		if (typeof value !== 'object' || value === null) return undefined;
		const { wstoken, privatetoken, userid } = value as Record<string, unknown>;
		if (typeof wstoken !== 'string' || !/^[0-9a-f]{32}$/i.test(wstoken)) return undefined;
		if (typeof privatetoken !== 'string' || !/^[0-9A-Za-z]{1,128}$/.test(privatetoken)) {
			return undefined;
		}
		if (typeof userid !== 'number' || !Number.isInteger(userid) || userid <= 0) return undefined;
		return { wstoken, privatetoken, userid };
	} catch {
		return undefined;
	}
}

async function requestKey(
	send: typeof fetch,
	body: AutologinRequest,
	signal: AbortSignal
): Promise<{ key: string; url: URL } | { refused: string }> {
	const response = await send(KEY_URL, {
		method: 'POST',
		headers: { 'user-agent': MOODLE_MOBILE_USER_AGENT, accept: 'application/json' },
		body: new URLSearchParams({ privatetoken: body.privatetoken, wstoken: body.wstoken }),
		signal
	});
	const json = (await response.json().catch(() => undefined)) as
		Record<string, unknown> | undefined;
	const code = json?.errorcode;
	if (typeof code === 'string') return { refused: code };
	const key = json?.key;
	const autologinUrl = json?.autologinurl;
	if (typeof key !== 'string' || typeof autologinUrl !== 'string') return { refused: 'no_key' };
	const url = new URL(autologinUrl);
	if (url.origin !== MOODLE) return { refused: 'foreign_autologin_url' };
	return { key, url };
}

/** Follows the autologin to its landing page, keeping Moodle's cookies, and returns the session. */
async function spendKey(
	send: typeof fetch,
	autologinUrl: URL,
	userId: number,
	key: string,
	signal: AbortSignal
): Promise<string | undefined> {
	const cookies = new Map<string, string>();
	let url = new URL(autologinUrl);
	url.searchParams.set('userid', String(userId));
	url.searchParams.set('key', key);
	url.searchParams.set('urltogo', `${MOODLE}${LANDING_PATH}`);
	for (let hop = 0; hop <= MAX_HOPS; hop++) {
		const headers = new Headers();
		if (cookies.size > 0) {
			headers.set('cookie', [...cookies].map(([name, value]) => `${name}=${value}`).join('; '));
		}
		const response = await send(url, { headers, redirect: 'manual', signal });
		for (const cookie of response.headers.getSetCookie()) remember(cookies, cookie);
		const location = response.headers.get('location');
		if (response.status >= 300 && response.status < 400 && location !== null) {
			url = new URL(location, url);
			if (url.origin !== MOODLE) return undefined;
			continue;
		}
		await response.body?.cancel();
		return response.status === 200 && url.pathname === LANDING_PATH
			? cookies.get('MoodleSession')
			: undefined;
	}
	return undefined;
}

function remember(cookies: Map<string, string>, setCookie: string): void {
	const pair = setCookie.split(';', 1)[0];
	const equals = pair.indexOf('=');
	if (equals <= 0) return;
	const name = pair.slice(0, equals).trim();
	const value = pair.slice(equals + 1).trim();
	const expired = /;\s*max-age=(0|-\d+)/i.test(setCookie) || value === 'deleted';
	if (expired) cookies.delete(name);
	else cookies.set(name, value);
}
