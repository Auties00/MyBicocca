import { describe, expect, it, vi } from 'vitest';
import { CookieJar } from './cookies.ts';
import { NetworkError, RelayError } from './errors.ts';
import { createFetch } from './transport.ts';

interface Sent {
	url: string;
	method: string;
	headers: Headers;
	body: unknown;
}

/** A fake relay: answers by upstream URL, enveloping redirects and cookies as the real one does. */
function relay(
	answer: (
		url: URL,
		sent: Sent
	) => { status?: number; location?: string; cookies?: string[]; body?: string }
) {
	const sent: Sent[] = [];
	const fetch: typeof globalThis.fetch = async (input, init = {}) => {
		const path = String(input);
		const request: Sent = {
			url: path,
			method: init.method ?? 'GET',
			headers: new Headers(init.headers),
			body: init.body
		};
		sent.push(request);
		if (!path.startsWith('/relay/')) return new Response('direct');
		const upstream = new URL(`https://${path.slice('/relay/'.length)}`);
		const reply = answer(upstream, request);
		const headers = new Headers({ 'x-relay-status': String(reply.status ?? 200) });
		if (reply.location !== undefined) headers.set('x-relay-location', reply.location);
		if (reply.cookies !== undefined)
			headers.set('x-relay-set-cookie', JSON.stringify(reply.cookies));
		const redirect = reply.location !== undefined;
		return new Response(redirect ? null : (reply.body ?? ''), {
			status: redirect ? 200 : (reply.status ?? 200),
			headers
		});
	};
	return { fetch, sent };
}

describe('createFetch', () => {
	it('follows redirects through the relay, carrying the cookies they set', async () => {
		const { fetch, sent } = relay((url) => {
			if (url.pathname === '/auth/studente/HomePageStudente.do') {
				return {
					status: 302,
					location: 'https://idp-idm.unimib.it/idp/a',
					cookies: ['s3w=1; Path=/']
				};
			}
			if (url.pathname === '/idp/a') {
				return {
					status: 302,
					location: 'https://idp-idm.unimib.it/idp/b',
					cookies: ['idp=2; Path=/']
				};
			}
			return { body: 'login page' };
		});
		const jar = new CookieJar();
		const response = await createFetch({ fetch, jar })(
			'https://s3w.si.unimib.it/auth/studente/HomePageStudente.do'
		);
		expect(await response.text()).toBe('login page');
		expect(response.url).toBe('https://idp-idm.unimib.it/idp/b');
		expect(sent.map((request) => request.url)).toEqual([
			'/relay/s3w.si.unimib.it/auth/studente/HomePageStudente.do',
			'/relay/idp-idm.unimib.it/idp/a',
			'/relay/idp-idm.unimib.it/idp/b'
		]);
		expect(sent[2].headers.get('x-relay-h-cookie')).toBe('idp=2');
		expect(jar.get('s3w', new URL('https://s3w.si.unimib.it/'))).toBe('1');
	});

	it('turns a POST into a GET on 302 and 303, and keeps it on 307', async () => {
		const { fetch, sent } = relay((url) => {
			if (url.pathname === '/a') return { status: 302, location: '/b' };
			if (url.pathname === '/c') return { status: 307, location: '/d' };
			return { body: 'ok' };
		});
		const send = createFetch({ fetch });
		const form = {
			method: 'POST',
			body: 'x=1',
			headers: { 'content-type': 'application/x-www-form-urlencoded' }
		};
		await send('https://idp-idm.unimib.it/a', form);
		await send('https://idp-idm.unimib.it/c', form);
		expect(sent.map((request) => [request.method, request.body ?? null])).toEqual([
			['POST', 'x=1'],
			['GET', null],
			['POST', 'x=1'],
			['POST', 'x=1']
		]);
		expect(sent[1].headers.has('content-type')).toBe(false);
	});

	it('returns a redirect to another scheme instead of following it', async () => {
		const { fetch } = relay(() => ({ status: 303, location: 'moodlemobile://token=YQ==' }));
		const response = await createFetch({ fetch })(
			'https://elearning.unimib.it/admin/tool/mobile/launch.php'
		);
		expect(response.status).toBe(303);
		expect(response.headers.get('location')).toBe('moodlemobile://token=YQ==');
	});

	it('returns redirects in manual mode', async () => {
		const { fetch, sent } = relay(() => ({
			status: 302,
			location: 'https://idp-idm.unimib.it/next'
		}));
		const response = await createFetch({ fetch })('https://idp-idm.unimib.it/idp/x', {
			method: 'POST',
			redirect: 'manual'
		});
		expect(response.status).toBe(302);
		expect(response.headers.get('location')).toBe('https://idp-idm.unimib.it/next');
		expect(sent).toHaveLength(1);
	});

	it('moves the headers browsers forbid into tunneled ones', async () => {
		const { fetch, sent } = relay(() => ({ body: '{}' }));
		await createFetch({ fetch })(
			'https://gestioneorari.didattica.unimib.it/portaleplanningAPI/api/aree/2',
			{
				headers: {
					Cookie: 'entry-filters={}',
					Origin: 'https://gestioneorari.didattica.unimib.it',
					'X-CLIENTE': '2'
				}
			}
		);
		const headers = sent[0].headers;
		expect(headers.get('x-relay-h-cookie')).toBe('entry-filters={}');
		expect(headers.get('x-relay-h-origin')).toBe('https://gestioneorari.didattica.unimib.it');
		expect(headers.has('cookie')).toBe(false);
		expect(headers.get('x-cliente')).toBe('2');
	});

	it('sends direct hosts straight, without the forbidden headers', async () => {
		const { fetch, sent } = relay(() => ({}));
		const response = await createFetch({ fetch })(
			'https://reservation.affluences.com/api/myreservations',
			{
				headers: { 'User-Agent': 'Mozilla/5.0', Accept: 'application/json' }
			}
		);
		expect(await response.text()).toBe('direct');
		expect(sent[0].url).toBe('https://reservation.affluences.com/api/myreservations');
		expect(sent[0].headers.has('user-agent')).toBe(false);
		expect(sent[0].headers.get('accept')).toBe('application/json');
	});

	it('throws relay errors and connection failures as their own errors', async () => {
		const refused: typeof fetch = async () =>
			new Response('{"error":"path_not_allowed","message":"no"}', {
				status: 403,
				headers: { 'x-relay-error': 'path_not_allowed' }
			});
		const offline: typeof fetch = async () => {
			throw new TypeError('Failed to fetch');
		};
		await expect(
			createFetch({ fetch: refused })('https://s3w.si.unimib.it/x')
		).rejects.toMatchObject({
			name: RelayError.name,
			code: 'path_not_allowed'
		});
		await expect(
			createFetch({ fetch: offline })('https://s3w.si.unimib.it/x')
		).rejects.toBeInstanceOf(NetworkError);
	});

	it("stops a request when the transport's signal aborts", async () => {
		const hanging: typeof fetch = (_, init = {}) =>
			new Promise((_resolve, reject) => {
				init.signal?.addEventListener('abort', () => reject(init.signal?.reason));
			});
		const controller = new AbortController();
		const request = createFetch({ fetch: hanging, signal: controller.signal })(
			'https://s3w.si.unimib.it/e3rest/api/login'
		);
		controller.abort(new DOMException('Sign-in took too long', 'TimeoutError'));
		await expect(request).rejects.toMatchObject({ name: 'TimeoutError' });
	});

	it('sends a GET once more after a server failure, but never a write or a password', async () => {
		vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] });
		try {
			let failures = 0;
			const { fetch, sent } = relay(() =>
				failures++ % 2 === 0 ? { status: 503, body: 'down' } : { body: 'ok' }
			);
			const send = createFetch({ fetch });
			const read = send('https://s3w.si.unimib.it/e3rest/api/libretto', {
				headers: { Authorization: 'Bearer jwt' }
			});
			await vi.advanceTimersByTimeAsync(1_000);
			const response = await read;
			expect([response.status, await response.text()]).toEqual([200, 'ok']);
			expect(sent).toHaveLength(2);

			failures = 0;
			const write = await send('https://s3w.si.unimib.it/e3rest/api/x', { method: 'POST' });
			failures = 0;
			const login = await send('https://s3w.si.unimib.it/e3rest/api/login/jwt/new', {
				headers: { Authorization: 'Basic bTpw' }
			});
			expect([write.status, login.status]).toEqual([503, 503]);
			expect(sent).toHaveLength(4);
		} finally {
			vi.useRealTimers();
		}
	});

	it('gives up after ten redirects', async () => {
		const { fetch } = relay(() => ({ status: 302, location: '/loop' }));
		await expect(createFetch({ fetch })('https://idp-idm.unimib.it/loop')).rejects.toBeInstanceOf(
			NetworkError
		);
	});
});
