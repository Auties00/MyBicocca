/** A cookie as a browser keeps it (RFC 6265 §5.3), minus what the app never needs (SameSite). */
export interface Cookie {
	name: string;
	value: string;
	/** Lower-case host, without a leading dot. */
	domain: string;
	/** Whether only `domain` itself receives it, as when `Set-Cookie` names no Domain. */
	hostOnly: boolean;
	path: string;
	/** Expiry in epoch milliseconds; a session cookie has none. */
	expires?: number;
	secure: boolean;
}

/**
 * The cookie `header` sets when it answers a request for `url`, or undefined when a browser would
 * ignore it (no name, or a Domain the host doesn't belong to).
 */
export function parseSetCookie(header: string, url: URL, now: number): Cookie | undefined {
	const [pair, ...attributes] = header.split(';');
	const equals = pair.indexOf('=');
	if (equals <= 0) return undefined;
	const host = url.hostname.toLowerCase();
	const cookie: Cookie = {
		name: pair.slice(0, equals).trim(),
		value: pair.slice(equals + 1).trim(),
		domain: host,
		hostOnly: true,
		path: defaultPath(url.pathname),
		secure: false
	};
	let maxAge: number | undefined;
	let expires: number | undefined;
	for (const attribute of attributes) {
		const separator = attribute.indexOf('=');
		const name = (separator === -1 ? attribute : attribute.slice(0, separator))
			.trim()
			.toLowerCase();
		const value = separator === -1 ? '' : attribute.slice(separator + 1).trim();
		if (name === 'max-age' && /^-?\d+$/.test(value)) maxAge = Number(value);
		else if (name === 'expires' && !Number.isNaN(Date.parse(value))) expires = Date.parse(value);
		else if (name === 'domain' && value !== '') {
			const domain = value.replace(/^\./, '').toLowerCase();
			if (!domainMatches(host, domain)) return undefined;
			cookie.domain = domain;
			cookie.hostOnly = false;
		} else if (name === 'path' && value.startsWith('/')) cookie.path = value;
		else if (name === 'secure') cookie.secure = true;
	}
	// Max-Age wins over Expires (RFC 6265 §5.3 step 3).
	if (maxAge !== undefined) cookie.expires = now + maxAge * 1000;
	else if (expires !== undefined) cookie.expires = expires;
	return cookie;
}

function defaultPath(pathname: string): string {
	const slash = pathname.lastIndexOf('/');
	return slash <= 0 ? '/' : pathname.slice(0, slash);
}

function domainMatches(host: string, domain: string): boolean {
	return host === domain || host.endsWith(`.${domain}`);
}

function pathMatches(requestPath: string, cookiePath: string): boolean {
	if (requestPath === cookiePath) return true;
	if (!requestPath.startsWith(cookiePath)) return false;
	return cookiePath.endsWith('/') || requestPath[cookiePath.length] === '/';
}

/**
 * The cookies of one session, kept by the app because browsers keep relayed cookies from scripts:
 * the relay hands them over as `Set-Cookie` strings and takes them back as a `Cookie` header.
 */
export class CookieJar {
	#cookies: Cookie[];
	readonly #onChange: (() => void) | undefined;

	constructor(cookies: readonly Cookie[] = [], onChange?: () => void) {
		this.#cookies = [...cookies];
		this.#onChange = onChange;
	}

	/** Keeps the cookies `setCookies` set in answer to a request for `url`. */
	store(setCookies: readonly string[], url: URL, now = Date.now()): void {
		let changed = false;
		for (const header of setCookies) {
			const cookie = parseSetCookie(header, url, now);
			if (cookie !== undefined) changed = this.#put(cookie, now) || changed;
		}
		if (changed) this.#onChange?.();
	}

	/** Adds or replaces `cookie`, as a `Set-Cookie` would. */
	set(cookie: Cookie, now = Date.now()): void {
		if (this.#put(cookie, now)) this.#onChange?.();
	}

	/** The `Cookie` header a browser would send to `url`: longer paths first. */
	header(url: URL, now = Date.now()): string {
		return this.matching(url, now)
			.map((cookie) => `${cookie.name}=${cookie.value}`)
			.join('; ');
	}

	/** The value of the cookie `name` a request for `url` would carry. */
	get(name: string, url: URL, now = Date.now()): string | undefined {
		return this.matching(url, now).find((cookie) => cookie.name === name)?.value;
	}

	/** The cookies a request for `url` would carry. */
	matching(url: URL, now = Date.now()): Cookie[] {
		const host = url.hostname.toLowerCase();
		return this.#cookies
			.filter(
				(cookie) =>
					(cookie.expires === undefined || cookie.expires > now) &&
					(cookie.hostOnly ? host === cookie.domain : domainMatches(host, cookie.domain)) &&
					pathMatches(url.pathname, cookie.path) &&
					(!cookie.secure || url.protocol === 'https:')
			)
			.sort((a, b) => b.path.length - a.path.length);
	}

	/** The cookies `hostname` receives on any of its paths. */
	forHost(hostname: string, now = Date.now()): Cookie[] {
		const host = hostname.toLowerCase();
		return this.cookies(now).filter((cookie) =>
			cookie.hostOnly ? host === cookie.domain : domainMatches(host, cookie.domain)
		);
	}

	/** Every cookie still alive, for persisting the jar. */
	cookies(now = Date.now()): Cookie[] {
		return this.#cookies.filter((cookie) => cookie.expires === undefined || cookie.expires > now);
	}

	clear(): void {
		if (this.#cookies.length === 0) return;
		this.#cookies = [];
		this.#onChange?.();
	}

	#put(cookie: Cookie, now: number): boolean {
		const index = this.#cookies.findIndex(
			(kept) =>
				kept.name === cookie.name && kept.domain === cookie.domain && kept.path === cookie.path
		);
		const expired = cookie.expires !== undefined && cookie.expires <= now;
		if (index === -1) {
			if (expired) return false;
			this.#cookies.push(cookie);
			return true;
		}
		const kept = this.#cookies[index];
		if (expired) this.#cookies.splice(index, 1);
		else if (kept.value === cookie.value && kept.expires === cookie.expires) return false;
		else this.#cookies[index] = cookie;
		return true;
	}
}
