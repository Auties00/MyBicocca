import { Esse3Error } from '@mybicocca/data-api/esse3';
import { ApiFailure } from '@mybicocca/data-api/runtime';
import { ReauthRequiredError } from '../errors.ts';

const JWT_URL = 'https://s3w.si.unimib.it/e3rest/api/login/jwt/new';
/** Mint the next token this long before expiry, so no request leaves with a dying one. */
const RENEW_MARGIN_MS = 60_000;
/** A token closer than this to expiry can't be trusted to mint the next one. */
const CHAIN_MARGIN_MS = 5_000;
/** Esse3's tokens live five minutes; used when one carries no readable lifetime. */
const DEFAULT_LIFETIME_MS = 300_000;

export interface Esse3Credentials {
	/** The full e-mail form, as Basic auth expects it. */
	username: string;
	password: string;
}

/** A token and when it lapses, in this device's clock. */
export interface Esse3Jwt {
	jwt: string;
	expiresAt: number;
}

export interface Esse3TokenOptions {
	/** Reaches Esse3 (through the relay). */
	send: typeof fetch;
	/** The stored credentials, read only when a token must be minted from scratch. */
	credentials: () => Promise<Esse3Credentials | undefined>;
	/** Keeps tokens across reloads, and reads one another tab minted meanwhile. */
	store?: { read(): Promise<Esse3Jwt | undefined>; write(jwt: Esse3Jwt): Promise<void> };
	/** Serializes minting across tabs. */
	lock?: <T>(run: () => Promise<T>) => Promise<T>;
	/** Told when only the user can restore access: no password, or a refused one. */
	onReauthRequired?: (error: Error) => void;
	now?: () => number;
}

/**
 * The rolling Esse3 JWT. The password (Basic) only ever mints a token: every other call carries
 * `Bearer <jwt>`, and a still-valid token mints its successor, so the password crosses the relay
 * only after a token lapsed. `/jwt/refresh` is broken server-side and never used.
 */
export class Esse3Token {
	readonly #options: Esse3TokenOptions;
	#jwt: string | undefined;
	#expiresAt = 0;
	#minting: Promise<string> | undefined;

	constructor(options: Esse3TokenOptions, initial?: Esse3Jwt) {
		this.#options = options;
		if (initial !== undefined) this.#adopt(initial);
	}

	/** The token in use, to keep after signing in. */
	get current(): Esse3Jwt | undefined {
		return this.#jwt === undefined ? undefined : { jwt: this.#jwt, expiresAt: this.#expiresAt };
	}

	/** The `Authorization` for the next request, minting a token when this one is about to lapse. */
	async authorization(): Promise<string> {
		if (this.#jwt !== undefined && this.#expiresAt - this.#now() > RENEW_MARGIN_MS) {
			return `Bearer ${this.#jwt}`;
		}
		return `Bearer ${await this.#mint(false)}`;
	}

	/** Mints a token in place of one Esse3 refused, from the password. */
	async renew(): Promise<string> {
		return `Bearer ${await this.#mint(true)}`;
	}

	#mint(refused: boolean): Promise<string> {
		const lock = this.#options.lock ?? ((run) => run());
		this.#minting ??= lock(() => this.#mintNow(refused)).finally(() => {
			this.#minting = undefined;
		});
		return this.#minting;
	}

	async #mintNow(refused: boolean): Promise<string> {
		const rejected = refused ? this.#jwt : undefined;
		const stored = await this.#options.store?.read();
		if (stored !== undefined && stored.jwt !== this.#jwt && stored.jwt !== rejected) {
			this.#adopt(stored);
			if (this.#expiresAt - this.#now() > RENEW_MARGIN_MS) return stored.jwt;
		}
		if (!refused && this.#jwt !== undefined && this.#expiresAt - this.#now() > CHAIN_MARGIN_MS) {
			try {
				return await this.#keep(await this.#request(`Bearer ${this.#jwt}`));
			} catch (error) {
				if (!(error instanceof Esse3Error)) throw error;
			}
		}
		const credentials = await this.#options.credentials();
		if (credentials === undefined) {
			throw this.#reauth(new ReauthRequiredError('esse3', 'No stored password for Esse3'));
		}
		try {
			return await this.#keep(await this.#request(basic(credentials)));
		} catch (error) {
			if (refusesCredentials(error)) throw this.#reauth(error as Esse3Error);
			throw error;
		}
	}

	async #request(authorization: string): Promise<string> {
		const response = await this.#options.send(JWT_URL, {
			headers: { Authorization: authorization, Accept: 'application/json' }
		});
		const body = (await response.json().catch(() => undefined)) as
			Record<string, unknown> | undefined;
		const jwt = body?.jwt;
		if (!response.ok || typeof jwt !== 'string' || jwt === '') {
			const code = typeof body?.retCode === 'number' ? String(body.retCode) : undefined;
			const serverMessage = typeof body?.retErrMsg === 'string' ? body.retErrMsg : undefined;
			const message = serverMessage ?? code ?? `Request failed with status ${response.status}`;
			throw new Esse3Error(new ApiFailure(response.status, code, message, serverMessage), [
				'AUTHENTICATED_USER'
			]);
		}
		return jwt;
	}

	async #keep(jwt: string): Promise<string> {
		const minted = { jwt, expiresAt: this.#now() + lifetime(jwt) };
		this.#adopt(minted);
		await this.#options.store?.write(minted);
		return jwt;
	}

	#adopt({ jwt, expiresAt }: Esse3Jwt): void {
		this.#jwt = jwt;
		this.#expiresAt = expiresAt;
	}

	#reauth<E extends Error>(error: E): E {
		this.#options.onReauthRequired?.(error);
		return error;
	}

	#now(): number {
		return (this.#options.now ?? Date.now)();
	}
}

/**
 * Sends Esse3 requests with the rolling token; a 401 mints a new one and retries once, and a
 * second 401 reaches the SDK, which throws it as an `Esse3Error`.
 */
export function withEsse3Auth(send: typeof fetch, token: Esse3Token): typeof fetch {
	return async (input, init = {}) => {
		const attempt = (authorization: string) => {
			const headers = new Headers(init.headers);
			headers.set('Authorization', authorization);
			return send(input, { ...init, headers });
		};
		const response = await attempt(await token.authorization());
		if (response.status !== 401) return response;
		await response.body?.cancel();
		return attempt(await token.renew());
	};
}

/**
 * Whether Esse3 refused the credentials: a 401, or the 403 `/login/jwt/new` answers to a wrong
 * password ("credenziali (user_id e/o password) non valide").
 */
export function refusesCredentials(error: unknown): boolean {
	return (
		error instanceof Esse3Error &&
		(error.status === 401 || (error.serverMessage ?? '').toLowerCase().includes('credenziali'))
	);
}

/** `Basic` credentials; `btoa` alone fails on characters outside Latin-1. */
export function basic({ username, password }: Esse3Credentials): string {
	const bytes = new TextEncoder().encode(`${username}:${password}`);
	return `Basic ${btoa(String.fromCharCode(...bytes))}`;
}

/** How long `jwt` lives, from its own claims so a skewed device clock doesn't matter. */
function lifetime(jwt: string): number {
	try {
		const part = jwt.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
		const claims = JSON.parse(atob(part)) as { iat?: unknown; exp?: unknown };
		if (typeof claims.exp === 'number' && typeof claims.iat === 'number') {
			return (claims.exp - claims.iat) * 1000;
		}
	} catch {
		// An unreadable token gets the default lifetime.
	}
	return DEFAULT_LIFETIME_MS;
}
