import type { Cookie } from '../cookies.ts';
import { LoginError } from '../errors.ts';
import { postForm, readPage } from './forms.ts';
import { idpLogin } from './idp.ts';

export const MOODLE_URL = 'https://elearning.unimib.it/';
const AJAX_URL = 'https://elearning.unimib.it/lib/ajax/service.php';
const PUBLIC_CONFIG_REQUEST =
	'[{"index":0,"methodname":"tool_mobile_get_public_config","args":{}}]';

/** The tokens a Moodle login yields; the `MoodleSession` cookie stays in the login's jar. */
export interface MoodleTokens {
	/** Web service token carried by every Moodle web service call. */
	wsToken: string;
	/** Lets the autologin mint a new browser session; Moodle issues it only on a fresh login. */
	privateToken: string | null;
}

/** A Moodle sign-in: the tokens, and the session cookies Moodle's pages take. */
export interface MoodleSignIn {
	tokens: MoodleTokens;
	/** The logged-in `MoodleSession` and its companions. */
	cookies: Cookie[];
}

/**
 * Logs into Moodle the way the official mobile app does: the mobile launch page hands off to the
 * UniMiB IdP, the SAML response goes back to Moodle, and Moodle finally redirects to
 * `moodlemobile://token=…` carrying the web service token. `send` must follow redirects by default
 * and carry a fresh cookie jar, which ends up holding the logged-in `MoodleSession`.
 *
 * @throws LoginError
 */
export async function moodleLogin(
	send: typeof fetch,
	username: string,
	password: string
): Promise<MoodleTokens> {
	const launch = await send(await launchUrl(send));
	const token = launch.headers.get('location');
	// Moodle still has a logged-in session in this jar and issues the token straight away.
	if (isRedirect(launch.status) && token !== null) return parseToken(token);
	if (launch.status !== 200) {
		throw new LoginError('moodle', `The launch page answered ${launch.status}`);
	}

	const ssoUrl = (await readPage(launch)).querySelector<HTMLAnchorElement>('a[title=UniMiB]')?.href;
	if (ssoUrl === undefined) throw new LoginError('moodle', 'Missing login button');
	const sso = await send(ssoUrl);
	await sso.body?.cancel();
	if (sso.status !== 200)
		throw new LoginError('moodle', `The SAML endpoint answered ${sso.status}`);

	const assertion = await idpLogin(send, 'moodle', username, password);
	const acs = await postForm(send, assertion.action, assertion.fields);
	const redirectUrl = acs.headers.get('location');
	if (redirectUrl === null) {
		throw new LoginError('moodle', `The assertion was answered with ${acs.status}`);
	}
	const final = await send(new URL(redirectUrl, assertion.action));
	await final.body?.cancel();
	const tokenUrl = final.headers.get('location');
	if (tokenUrl === null) throw new LoginError('moodle', `The launch ended with ${final.status}`);
	return parseToken(tokenUrl);
}

/**
 * Moodle's mobile launch URL, from the public site config, pointed at the official app service.
 * The call also sets the guest `MoodleSession` the login page's sesskey is bound to.
 */
async function launchUrl(send: typeof fetch): Promise<string> {
	const response = await send(AJAX_URL, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: PUBLIC_CONFIG_REQUEST,
		redirect: 'manual'
	});
	const result = ((await response.json().catch(() => undefined)) as unknown[] | undefined)?.[0] as
		{ error?: unknown; data?: { launchurl?: unknown } } | undefined;
	if (result === undefined) throw new LoginError('moodle', 'Empty public config');
	if (result.error !== false) throw new LoginError('moodle', 'The public config failed');
	const launch = result.data?.launchurl;
	if (typeof launch !== 'string') throw new LoginError('moodle', 'No launch URL');
	const url = new URL(launch);
	url.searchParams.append('service', 'moodle_mobile_app');
	url.searchParams.append('passport', String(Math.random() * 1000));
	return url.href;
}

/**
 * The tokens in `moodlemobile://token=BASE64`, whose payload is `SIGNATURE:::TOKEN[:::PRIVATE]`.
 * The signature (md5 of the site URL and passport) isn't checked.
 */
export function parseToken(url: string): MoodleTokens {
	const parts = payload(url);
	const wsToken = parts[1];
	if (wsToken === undefined || wsToken === '') {
		throw new LoginError('moodle', 'Malformed mobile launch payload');
	}
	const privateToken = parts[2]?.trim();
	return {
		wsToken,
		privateToken: privateToken === undefined || privateToken === '' ? null : privateToken
	};
}

function payload(url: string): string[] {
	const start = url.indexOf('token=');
	if (start === -1) return [];
	try {
		return atob(url.slice(start + 'token='.length)).split(':::');
	} catch {
		return [];
	}
}

function isRedirect(status: number): boolean {
	return status >= 300 && status < 400;
}
