import { describe, expect, it, vi } from 'vitest';
import { createRelay } from './handler.ts';
import { AUTOLOGIN_PATH, decodeSetCookies, RelayHeader } from './protocol.ts';

type Upstream = (url: URL, init: RequestInit) => Response | Promise<Response>;

function relayWith(upstream: Upstream, now = () => 0) {
	const calls: Array<{ url: URL; init: RequestInit }> = [];
	const fetch = vi.fn(async (input: RequestInfo | URL, init: RequestInit = {}) => {
		const url = new URL(input instanceof Request ? input.url : String(input));
		calls.push({ url, init });
		return upstream(url, init);
	});
	return { handle: createRelay({ fetch, now }), calls };
}

function request(path: string, init: RequestInit = {}): Request {
	return new Request(`http://localhost:5173${path}`, init);
}

describe('relay', () => {
	it('forwards allowed headers and tunneled ones, and drops the rest', async () => {
		const { handle, calls } = relayWith(() => new Response('ok'));
		await handle(
			request('/relay/gestioneorari.didattica.unimib.it/portaleplanningAPI/api/servizi/2', {
				headers: {
					accept: 'application/json',
					'x-cliente': '2',
					'x-relay-h-cookie': 'jwt_public=t; entry-filters={}',
					'x-relay-h-origin': 'https://gestioneorari.didattica.unimib.it',
					'x-forwarded-for': '10.0.0.1',
					'x-custom': 'nope',
					'user-agent': 'Browser/1'
				}
			})
		);
		const sent = new Headers(calls[0].init.headers);
		expect(sent.get('cookie')).toBe('jwt_public=t; entry-filters={}');
		expect(sent.get('origin')).toBe('https://gestioneorari.didattica.unimib.it');
		expect(sent.get('x-cliente')).toBe('2');
		expect(sent.get('user-agent')).toBe('Browser/1');
		expect(sent.has('x-forwarded-for')).toBe(false);
		expect(sent.has('x-custom')).toBe(false);
		expect(calls[0].init.redirect).toBe('manual');
	});

	it('refuses a tunneled header the host may not receive', async () => {
		const { handle, calls } = relayWith(() => new Response('ok'));
		const response = await handle(
			request('/relay/s3w.si.unimib.it/e3rest/api/login', {
				headers: { 'x-relay-h-origin': 'https://evil.example' }
			})
		);
		expect(response.status).toBe(400);
		expect(response.headers.get(RelayHeader.error)).toBe('header_not_allowed');
		expect(calls).toHaveLength(0);
	});

	it('envelopes a redirect as a 200 with its status and absolute location', async () => {
		const { handle } = relayWith(
			() =>
				new Response(null, {
					status: 302,
					headers: [
						['location', '/idp/profile/SAML2/Redirect/SSO?execution=e1s1'],
						['set-cookie', 'AWSALB=x; Path=/']
					]
				})
		);
		const response = await handle(
			request('/relay/idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO?SAMLRequest=abc')
		);
		expect(response.status).toBe(200);
		expect(response.headers.get(RelayHeader.status)).toBe('302');
		expect(response.headers.get(RelayHeader.location)).toBe(
			'https://idp-idm.unimib.it/idp/profile/SAML2/Redirect/SSO?execution=e1s1'
		);
		expect(decodeSetCookies(response.headers.get(RelayHeader.setCookie))).toEqual([
			'AWSALB=x; Path=/'
		]);
	});

	it('hands back a non-http location untouched', async () => {
		const { handle } = relayWith(
			() => new Response(null, { status: 303, headers: { location: 'moodlemobile://token=YQ==' } })
		);
		const response = await handle(
			request('/relay/elearning.unimib.it/admin/tool/mobile/launch.php?service=moodle_mobile_app')
		);
		expect(response.headers.get(RelayHeader.location)).toBe('moodlemobile://token=YQ==');
	});

	it('passes other statuses through, with the status header and no upstream cookies', async () => {
		const { handle } = relayWith(
			() =>
				new Response('{"retCode":-1}', {
					status: 401,
					headers: [
						['content-type', 'application/json'],
						['set-cookie', 'JSESSIONID=1; Path=/e3rest; Secure; HttpOnly'],
						['access-control-allow-origin', '*']
					]
				})
		);
		const response = await handle(request('/relay/s3w.si.unimib.it/e3rest/api/login'));
		expect(response.status).toBe(401);
		expect(response.headers.get(RelayHeader.status)).toBe('401');
		expect(response.headers.has('set-cookie')).toBe(false);
		expect(response.headers.has('access-control-allow-origin')).toBe(false);
		expect(decodeSetCookies(response.headers.get(RelayHeader.setCookie))).toEqual([
			'JSESSIONID=1; Path=/e3rest; Secure; HttpOnly'
		]);
		expect(await response.text()).toBe('{"retCode":-1}');
	});

	it('refuses routes outside the allowlist with a typed error', async () => {
		const { handle, calls } = relayWith(() => new Response('ok'));
		const response = await handle(
			request('/relay/elearning.unimib.it/webservice/rest/server.php', { method: 'POST' })
		);
		expect(response.status).toBe(403);
		expect(response.headers.get(RelayHeader.error)).toBe('path_not_allowed');
		expect(await response.json()).toMatchObject({ error: 'path_not_allowed' });
		expect(calls).toHaveLength(0);
	});

	it('refuses cross-site callers', async () => {
		const { handle } = relayWith(() => new Response('ok'));
		const response = await handle(
			request('/relay/s3w.si.unimib.it/e3rest/api/login', {
				headers: { 'sec-fetch-site': 'cross-site' }
			})
		);
		expect(response.headers.get(RelayHeader.error)).toBe('cross_site');
	});

	it('times out the wait for an answer, not the transfer of its body', async () => {
		vi.useFakeTimers();
		try {
			const path = '/relay/s3w.si.unimib.it/e3rest/api/login';
			// As fetch does: the signal rejects a pending answer, and errors the body of a given one.
			const silent = relayWith(
				(_, init) =>
					new Promise((_, reject) =>
						init.signal?.addEventListener('abort', () => reject(init.signal?.reason))
					)
			);
			const waiting = silent.handle(request(path));
			await vi.advanceTimersByTimeAsync(60_000);
			expect((await waiting).headers.get(RelayHeader.error)).toBe('upstream_timeout');

			let finish = () => {};
			const slow = relayWith((_, init) => {
				const body = new ReadableStream<Uint8Array>({
					start(controller) {
						init.signal?.addEventListener('abort', () => controller.error(init.signal?.reason));
						finish = () => {
							controller.enqueue(new TextEncoder().encode('late'));
							controller.close();
						};
					}
				});
				return new Response(body);
			});
			const answered = await slow.handle(request(path));
			await vi.advanceTimersByTimeAsync(120_000);
			finish();
			expect(await answered.text()).toBe('late');
		} finally {
			vi.useRealTimers();
		}
	});

	it('reports a timeout and an unreachable upstream apart', async () => {
		const timeout = relayWith(() => {
			throw new DOMException('timed out', 'TimeoutError');
		});
		const unreachable = relayWith(() => {
			throw new TypeError('fetch failed');
		});
		const path = '/relay/s3w.si.unimib.it/e3rest/api/login';
		expect((await timeout.handle(request(path))).headers.get(RelayHeader.error)).toBe(
			'upstream_timeout'
		);
		expect((await unreachable.handle(request(path))).headers.get(RelayHeader.error)).toBe(
			'upstream_unreachable'
		);
	});
});

describe('moodle autologin', () => {
	const body = JSON.stringify({ wstoken: 'a'.repeat(32), privatetoken: 'p1', userid: 7 });
	const post = () => request(AUTOLOGIN_PATH, { method: 'POST', body });

	function moodle(keyAnswer: object, landing = '/user/profile.php'): Upstream {
		return (url, init) => {
			if (url.pathname === '/webservice/rest/server.php') {
				expect(new Headers(init.headers).get('user-agent')).toBe('MoodleMobile 4.5.0');
				return Response.json(keyAnswer);
			}
			if (url.pathname === '/admin/tool/mobile/autologin.php') {
				return new Response(null, {
					status: 303,
					headers: [
						['location', `https://elearning.unimib.it${landing}`],
						['set-cookie', 'MoodleSession=fresh; path=/; secure; HttpOnly']
					]
				});
			}
			expect(new Headers(init.headers).get('cookie')).toBe('MoodleSession=fresh');
			return new Response('<html></html>');
		};
	}

	const key = {
		key: 'k',
		autologinurl: 'https://elearning.unimib.it/admin/tool/mobile/autologin.php'
	};

	it('asks for the key and spends it in one call', async () => {
		const { handle, calls } = relayWith(moodle(key));
		const response = await handle(post());
		expect(await response.json()).toEqual({ moodleSession: 'fresh' });
		expect(calls[1].url.searchParams.get('key')).toBe('k');
		expect(calls[1].url.searchParams.get('userid')).toBe('7');
	});

	it('reports a refused key and a login page landing as refusals', async () => {
		const refused = relayWith(moodle({ errorcode: 'invalidtoken', exception: 'x' }));
		const landed = relayWith(moodle(key, '/login/index.php'));
		const first = await refused.handle(post());
		const second = await landed.handle(post());
		expect(first.status).toBe(409);
		expect(await first.json()).toMatchObject({ moodleCode: 'invalidtoken' });
		expect(second.status).toBe(409);
	});

	it('allows one call per user every six minutes', async () => {
		let now = 0;
		const { handle } = relayWith(moodle(key), () => now);
		expect((await handle(post())).status).toBe(200);
		now = 5 * 60_000;
		const second = await handle(post());
		expect(second.status).toBe(429);
		expect(second.headers.get(RelayHeader.error)).toBe('autologin_too_soon');
		now = 6 * 60_000;
		expect((await handle(post())).status).toBe(200);
	});

	it('maps Moodle lockout to too soon, and rejects malformed bodies', async () => {
		const locked = relayWith(moodle({ errorcode: 'autologinkeygenerationlockout' }));
		expect((await locked.handle(post())).status).toBe(429);
		const { handle } = relayWith(moodle(key));
		const bad = await handle(
			request(AUTOLOGIN_PATH, { method: 'POST', body: '{"wstoken":"x","userid":1}' })
		);
		expect(bad.headers.get(RelayHeader.error)).toBe('bad_request');
	});
});
