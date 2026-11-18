/**
 * The relay forwards a request for `https://{host}{path}` received at `/relay/{host}{path}`, for
 * the hosts that send no CORS headers. It is stateless and never follows redirects: the client
 * keeps the cookie jars and follows redirects itself, through the relay, so every hop is checked
 * against {@link ALLOWLIST}. What browsers hide from scripts travels in `x-relay-*` headers.
 */
export const RELAY_PREFIX = '/relay/';

/** Moodle's autologin, which must ask for the key and spend it from one IP within a minute. */
export const AUTOLOGIN_PATH = `${RELAY_PREFIX}x/moodle-autologin`;

export const RelayHeader = {
	/** Prefixes a request header browsers refuse to send, such as `cookie`; the relay sends it unprefixed. */
	tunneled: 'x-relay-h-',
	/** The upstream status, on every relayed response; a redirect reaches the browser as a 200. */
	status: 'x-relay-status',
	/** The absolute `Location` of a redirect, left as is for other schemes (`moodlemobile:`). */
	location: 'x-relay-location',
	/** The upstream `Set-Cookie` headers, as a JSON array of strings. */
	setCookie: 'x-relay-set-cookie',
	/** Marks a response the relay produced itself, with a {@link RelayErrorBody}. */
	error: 'x-relay-error'
} as const;

export type RelayErrorCode =
	| 'host_not_allowed'
	| 'path_not_allowed'
	| 'method_not_allowed'
	| 'header_not_allowed'
	| 'cross_site'
	| 'bad_request'
	| 'body_too_large'
	| 'upstream_timeout'
	| 'upstream_unreachable'
	| 'autologin_refused'
	| 'autologin_too_soon';

export interface RelayErrorBody {
	error: RelayErrorCode;
	message: string;
	/** The Moodle error code behind an `autologin_refused`. */
	moodleCode?: string;
}

export type RelayMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

interface Rule {
	methods: readonly RelayMethod[];
	path: RegExp;
}

/**
 * The only requests the relay forwards, by host. Moodle's web services, files and Kaltura answer
 * browsers directly and are absent on purpose: attendance marking must leave from the student's
 * own IP.
 */
export const ALLOWLIST: Readonly<Record<string, readonly Rule[]>> = {
	's3w.si.unimib.it': [
		{ methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'], path: /^\/e3rest\/api\// },
		{ methods: ['GET'], path: /^\/auth\/studente\// },
		{ methods: ['GET', 'POST'], path: /^\/Shibboleth\.sso\// }
	],
	'idp-idm.unimib.it': [{ methods: ['GET', 'POST'], path: /^\/idp\/profile\/SAML2\// }],
	// Moodle's SAML service provider (SimpleSAMLphp): the IdP posts its assertion here, and the
	// provider sends the browser back to Moodle's login page with the session it set.
	'sp.elearning.unimib.it': [{ methods: ['GET', 'POST'], path: /^\/module\.php\/saml\/sp\// }],
	'elearning.unimib.it': [
		{ methods: ['POST'], path: /^\/lib\/ajax\/service\.php$/ },
		{ methods: ['GET'], path: /^\/admin\/tool\/mobile\/launch\.php$/ },
		{ methods: ['GET'], path: /^\/login\/index\.php$/ },
		{ methods: ['GET', 'POST'], path: /^\/auth\/unimibsaml\// },
		{ methods: ['GET'], path: /^\/mod\/kalvidres\/view\.php$/ },
		{ methods: ['GET'], path: /^\/mod\/attendance\/view\.php$/ },
		{ methods: ['GET'], path: /^\/course\/info\.php$/ }
	],
	'gestioneorari.didattica.unimib.it': [
		{ methods: ['GET'], path: /^\/PortaleStudentiUnimib\/(combo|index)\.php$/ },
		{
			methods: ['POST'],
			path: /^\/PortaleStudentiUnimib\/(grid_call|test_call|bookings_call|rooms_call)\.php$/
		},
		{ methods: ['POST'], path: /^\/easybadge\/api\/(corso_iscritti|TimbratureApi)\.php$/ },
		{ methods: ['GET', 'POST'], path: /^\/portaleplanningAPI\/api\// }
	],
	'api.affluences.com': [{ methods: ['GET', 'POST'], path: /^\/app\/v[34]\// }],
	'reservation.affluences.com': [
		{ methods: ['POST'], path: /^\/api\/reserve\/[^/]+$/ },
		// The rest of the reservation API answers browsers directly; this is the fallback if it stops.
		{ methods: ['GET', 'POST', 'DELETE'], path: /^\/api\// }
	]
};

/** Request headers forwarded as they are. */
export const FORWARDED_REQUEST_HEADERS: ReadonlySet<string> = new Set([
	'accept',
	'accept-language',
	'authorization',
	'content-type',
	'range',
	'if-none-match',
	'if-modified-since',
	'user-identifier',
	'x-cliente',
	'x-app-locale',
	'x-app-version',
	'x-device-type',
	'x-service-name'
]);

/** Response headers handed back as they are; `Set-Cookie` and `Location` travel enveloped. */
export const FORWARDED_RESPONSE_HEADERS: ReadonlySet<string> = new Set([
	'accept-ranges',
	'cache-control',
	'content-disposition',
	'content-range',
	'content-type',
	'etag',
	'expires',
	'last-modified',
	'retry-after',
	'warning',
	'www-authenticate'
]);

/** Headers a page can't set, which reach the relay prefixed with {@link RelayHeader.tunneled}. */
export const TUNNELED_HEADERS: ReadonlySet<string> = new Set([
	'cookie',
	'origin',
	'referer',
	'user-agent'
]);

/** The only `Origin` each host may receive: Planning refuses bookings from any other. */
const FIXED_ORIGINS: Readonly<Record<string, string>> = {
	'gestioneorari.didattica.unimib.it': 'https://gestioneorari.didattica.unimib.it'
};

/** The desktop browser Affluences' app API expects; it answers "Invalid device" to unknown clients. */
const AFFLUENCES_USER_AGENT =
	'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36';

const USER_AGENTS: Readonly<Record<string, readonly string[]>> = {
	'api.affluences.com': [AFFLUENCES_USER_AGENT],
	'reservation.affluences.com': [AFFLUENCES_USER_AGENT]
};

/** The User-Agent Moodle hands autologin keys to; only the composite endpoint sends it. */
export const MOODLE_MOBILE_USER_AGENT = 'MoodleMobile 4.5.0';

/** The upstream URL `pathname` and `search` of a relay request stand for, if its host is relayed. */
export function relayTarget(pathname: string, search: string): URL | undefined {
	if (!pathname.startsWith(RELAY_PREFIX)) return undefined;
	const rest = pathname.slice(RELAY_PREFIX.length);
	const slash = rest.indexOf('/');
	const host = slash === -1 ? rest : rest.slice(0, slash);
	if (!Object.hasOwn(ALLOWLIST, host)) return undefined;
	return new URL(`https://${host}${slash === -1 ? '/' : rest.slice(slash)}${search}`);
}

/** The relay path standing for `url`. */
export function relayPath(url: URL): string {
	return `${RELAY_PREFIX}${url.host}${url.pathname}${url.search}`;
}

/** Why the allowlist refuses `method` on `target`, or undefined when it allows it. */
export function refusal(
	method: string,
	target: URL
): 'host_not_allowed' | 'path_not_allowed' | 'method_not_allowed' | undefined {
	if (target.protocol !== 'https:' || target.port !== '') return 'host_not_allowed';
	const rules = Object.hasOwn(ALLOWLIST, target.hostname) ? ALLOWLIST[target.hostname] : undefined;
	if (rules === undefined) return 'host_not_allowed';
	const matching = rules.filter((rule) => rule.path.test(target.pathname));
	if (matching.length === 0) return 'path_not_allowed';
	return matching.some((rule) => (rule.methods as readonly string[]).includes(method))
		? undefined
		: 'method_not_allowed';
}

/** Whether the relay may send `value` as the tunneled header `name` to `target`. */
export function acceptsTunneled(name: string, value: string, target: URL): boolean {
	switch (name) {
		case 'cookie':
			return true;
		case 'origin':
			return FIXED_ORIGINS[target.hostname] === value;
		case 'user-agent':
			return USER_AGENTS[target.hostname]?.includes(value) ?? false;
		case 'referer':
			return isAllowlistedUrl(value);
		default:
			return false;
	}
}

function isAllowlistedUrl(value: string): boolean {
	try {
		const url = new URL(value);
		return url.protocol === 'https:' && Object.hasOwn(ALLOWLIST, url.hostname);
	} catch {
		return false;
	}
}

/** How long the relay waits for `target`: Esse3's record-book calls can take longer than 30 s. */
export function timeoutFor(target: URL): number {
	return target.hostname === 's3w.si.unimib.it' ? 60_000 : 30_000;
}

/** The {@link RelayHeader.setCookie} value for `cookies`, escaped to stay a valid header value. */
export function encodeSetCookies(cookies: readonly string[]): string {
	return JSON.stringify(cookies).replace(
		/[\u007f-￿]/g,
		(char) => `\\u${char.charCodeAt(0).toString(16).padStart(4, '0')}`
	);
}

/** The `Set-Cookie` headers carried by a {@link RelayHeader.setCookie} value. */
export function decodeSetCookies(header: string | null): string[] {
	if (header === null) return [];
	try {
		const value: unknown = JSON.parse(header);
		return Array.isArray(value)
			? value.filter((item): item is string => typeof item === 'string')
			: [];
	} catch {
		return [];
	}
}

/** The body of a request to {@link AUTOLOGIN_PATH}. */
export interface AutologinRequest {
	wstoken: string;
	privatetoken: string;
	/** The Moodle user id the tokens belong to. */
	userid: number;
}

/** The answer to a successful {@link AutologinRequest}. */
export interface AutologinResponse {
	/** The new `MoodleSession` cookie value. */
	moodleSession: string;
}
