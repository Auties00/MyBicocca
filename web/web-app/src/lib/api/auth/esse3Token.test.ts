import { describe, expect, it } from 'vitest';
import { ReauthRequiredError } from '../errors.ts';
import {
	basic,
	Esse3Token,
	refusesCredentials,
	withEsse3Auth,
	type Esse3Jwt
} from './esse3Token.ts';

const credentials = { username: 'm.rossi99@campus.unimib.it', password: 'pàss' };

function jwt(serial: number): string {
	const claims = btoa(JSON.stringify({ iat: 1000, exp: 1300, n: serial }));
	return `h.${claims.replace(/=+$/, '')}.s`;
}

/** A fake Esse3 that mints numbered tokens and answers other calls when the token is current. */
function esse3() {
	let serial = 0;
	let current: string | undefined;
	const mints: string[] = [];
	const send: typeof fetch = async (input, init = {}) => {
		const authorization = new Headers(init.headers).get('authorization') ?? '';
		if (String(input).endsWith('/login/jwt/new')) {
			const scheme = authorization.split(' ')[0];
			mints.push(scheme);
			if (scheme === 'Basic' && authorization !== basic(credentials)) {
				return Response.json({ retCode: -1, retErrMsg: 'Credenziali non valide' }, { status: 401 });
			}
			if (scheme === 'Bearer' && authorization !== `Bearer ${current}`) {
				return new Response(null, { status: 401 });
			}
			current = jwt(++serial);
			return Response.json({ jwt: current });
		}
		return new Response(authorization === `Bearer ${current}` ? 'ok' : null, {
			status: authorization === `Bearer ${current}` ? 200 : 401
		});
	};
	return {
		send,
		mints,
		revoke: () => {
			current = undefined;
		}
	};
}

describe('Esse3Token', () => {
	it('mints with the password once, then rolls the token with itself', async () => {
		const server = esse3();
		let now = 0;
		const token = new Esse3Token({
			send: server.send,
			credentials: async () => credentials,
			now: () => now
		});
		expect(await token.authorization()).toBe(`Bearer ${jwt(1)}`);
		now = 200_000;
		expect(await token.authorization()).toBe(`Bearer ${jwt(1)}`);
		now = 250_000;
		expect(await token.authorization()).toBe(`Bearer ${jwt(2)}`);
		expect(server.mints).toEqual(['Basic', 'Bearer']);
	});

	it('falls back to the password once the token lapsed', async () => {
		const server = esse3();
		let now = 0;
		const token = new Esse3Token({
			send: server.send,
			credentials: async () => credentials,
			now: () => now
		});
		await token.authorization();
		now = 400_000;
		expect(await token.authorization()).toBe(`Bearer ${jwt(2)}`);
		expect(server.mints).toEqual(['Basic', 'Basic']);
	});

	it('mints once for concurrent callers', async () => {
		const server = esse3();
		const token = new Esse3Token({ send: server.send, credentials: async () => credentials });
		const headers = await Promise.all([
			token.authorization(),
			token.authorization(),
			token.authorization()
		]);
		expect(new Set(headers).size).toBe(1);
		expect(server.mints).toEqual(['Basic']);
	});

	it('adopts a fresh token another tab stored', async () => {
		const server = esse3();
		const shared: Esse3Jwt = { jwt: 'other-tab', expiresAt: 300_000 };
		const token = new Esse3Token({
			send: server.send,
			credentials: async () => credentials,
			store: { read: async () => shared, write: async () => {} },
			now: () => 0
		});
		expect(await token.authorization()).toBe('Bearer other-tab');
		expect(server.mints).toEqual([]);
	});

	it('asks for the user when the token lapsed and no password is stored', async () => {
		const server = esse3();
		const reported: Error[] = [];
		const token = new Esse3Token({
			send: server.send,
			credentials: async () => undefined,
			onReauthRequired: (error) => reported.push(error)
		});
		await expect(token.authorization()).rejects.toBeInstanceOf(ReauthRequiredError);
		expect(reported).toHaveLength(1);
	});

	it('reports a refused password as an Esse3 401', async () => {
		const server = esse3();
		const token = new Esse3Token({
			send: server.send,
			credentials: async () => ({ ...credentials, password: 'wrong' })
		});
		await expect(token.authorization()).rejects.toMatchObject({ name: 'Esse3Error', status: 401 });
	});
});

describe('refusesCredentials', () => {
	it('reads the 403 Esse3 answers a wrong password with as a refusal', async () => {
		const send: typeof fetch = async () =>
			Response.json(
				{
					retCode: 1003,
					retErrMsg: 'Errore sulla login: credenziali (user_id e/o password) non valide'
				},
				{ status: 403 }
			);
		const reported: Error[] = [];
		const token = new Esse3Token({
			send,
			credentials: async () => credentials,
			onReauthRequired: (error) => reported.push(error)
		});
		const error = await token.authorization().catch((caught: unknown) => caught);
		expect(refusesCredentials(error)).toBe(true);
		expect(reported).toHaveLength(1);
	});
});

describe('withEsse3Auth', () => {
	it('re-mints once on a 401 and retries the request', async () => {
		const server = esse3();
		const token = new Esse3Token({ send: server.send, credentials: async () => credentials });
		const send = withEsse3Auth(server.send, token);
		expect(await (await send('https://s3w.si.unimib.it/e3rest/api/x')).text()).toBe('ok');
		server.revoke();
		const response = await send('https://s3w.si.unimib.it/e3rest/api/x');
		expect(response.status).toBe(200);
		expect(server.mints).toEqual(['Basic', 'Basic']);
	});
});

describe('basic', () => {
	it('encodes the credentials as UTF-8', () => {
		expect(basic({ username: 'a', password: 'è' })).toBe('Basic YTrDqA==');
	});
});
