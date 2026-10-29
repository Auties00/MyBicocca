import { createAutologin } from './autologin.ts';
import { relayError } from './errors.ts';
import {
	AUTOLOGIN_PATH,
	FORWARDED_REQUEST_HEADERS,
	FORWARDED_RESPONSE_HEADERS,
	RelayHeader,
	TUNNELED_HEADERS,
	acceptsTunneled,
	encodeSetCookies,
	refusal,
	relayTarget,
	timeoutFor
} from './protocol.ts';

export { RELAY_PREFIX } from './protocol.ts';

const MAX_BODY_BYTES = 25 * 1024 * 1024;

export interface RelayOptions {
	/** The `fetch` that reaches upstream; the global one by default. */
	fetch?: typeof fetch;
	now?: () => number;
}

/** Creates the relay's request handler. */
export function createRelay(options: RelayOptions = {}): (request: Request) => Promise<Response> {
	const send = options.fetch ?? ((input, init) => fetch(input, init));
	const autologin = createAutologin(send, options.now ?? Date.now);

	return async (request) => {
		const url = new URL(request.url);
		const site = request.headers.get('sec-fetch-site');
		if (site !== null && site !== 'same-origin') {
			return relayError(403, 'cross_site', 'Only the app may use the relay');
		}
		if (url.pathname === AUTOLOGIN_PATH) return autologin(request);

		const target = relayTarget(url.pathname, url.search);
		if (target === undefined) return relayError(403, 'host_not_allowed', 'Not a relayed host');
		const refused = refusal(request.method, target);
		if (refused !== undefined) {
			return relayError(403, refused, `${request.method} ${target.pathname} is not relayed`);
		}

		const headers = upstreamHeaders(request.headers, target);
		if (typeof headers === 'string') {
			return relayError(400, 'header_not_allowed', `The tunneled ${headers} is refused`);
		}
		const length = Number(request.headers.get('content-length') ?? 0);
		if (length > MAX_BODY_BYTES) return relayError(413, 'body_too_large', 'Body too large');
		const hasBody = request.method !== 'GET' && request.method !== 'HEAD';
		const body = hasBody ? await request.arrayBuffer() : undefined;
		if (body !== undefined && body.byteLength > MAX_BODY_BYTES) {
			return relayError(413, 'body_too_large', 'Body too large');
		}

		// The timeout bounds the wait for the upstream's answer, not the transfer of its body.
		const answer = new AbortController();
		const timer = setTimeout(
			() => answer.abort(new DOMException('No answer in time', 'TimeoutError')),
			timeoutFor(target)
		);
		let upstream: Response;
		try {
			upstream = await send(target, {
				method: request.method,
				headers,
				body,
				redirect: 'manual',
				signal: answer.signal
			});
		} catch (error) {
			return upstreamFailure(error);
		} finally {
			clearTimeout(timer);
		}
		return envelope(upstream, target);
	};
}

/** Forwards a request received at `/relay/{host}{path}`, statelessly. */
export const relay = createRelay();

/**
 * The headers to send upstream: the forwarded ones and the accepted tunneled ones, or the name of
 * a tunneled header the target may not receive. The browser's own User-Agent goes along unless a
 * tunneled one replaces it.
 */
function upstreamHeaders(incoming: Headers, target: URL): Headers | string {
	const headers = new Headers();
	const userAgent = incoming.get('user-agent');
	if (userAgent !== null) headers.set('user-agent', userAgent);
	for (const [name, value] of incoming) {
		if (name.startsWith(RelayHeader.tunneled)) {
			const tunneled = name.slice(RelayHeader.tunneled.length);
			if (!TUNNELED_HEADERS.has(tunneled) || !acceptsTunneled(tunneled, value, target)) {
				return tunneled;
			}
			headers.set(tunneled, value);
		} else if (FORWARDED_REQUEST_HEADERS.has(name)) {
			headers.set(name, value);
		}
	}
	return headers;
}

/** The upstream response as the browser may read it: redirects and cookies enveloped. */
function envelope(upstream: Response, target: URL): Response {
	const headers = new Headers({ 'cache-control': 'no-store' });
	for (const [name, value] of upstream.headers) {
		if (FORWARDED_RESPONSE_HEADERS.has(name)) headers.set(name, value);
	}
	headers.set(RelayHeader.status, String(upstream.status));
	const cookies = upstream.headers.getSetCookie();
	if (cookies.length > 0) headers.set(RelayHeader.setCookie, encodeSetCookies(cookies));

	const location = upstream.headers.get('location');
	if (upstream.status >= 300 && upstream.status < 400 && location !== null) {
		headers.set(RelayHeader.location, new URL(location, target).href);
		return new Response(null, { status: 200, headers });
	}
	const empty = upstream.status === 204 || upstream.status === 205 || upstream.status === 304;
	return new Response(empty ? null : upstream.body, { status: upstream.status, headers });
}

function upstreamFailure(error: unknown): Response {
	if (error instanceof DOMException && error.name === 'TimeoutError') {
		return relayError(504, 'upstream_timeout', 'The upstream server did not answer in time');
	}
	return relayError(502, 'upstream_unreachable', 'The upstream server could not be reached');
}
