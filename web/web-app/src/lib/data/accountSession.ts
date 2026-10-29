import {
	MoodleError,
	MoodleTokenAuth,
	createMoodleApi,
	createMoodleWebApi,
	type MoodleApi,
	type MoodleWebApi
} from '@mybicocca/data-api/elearning';
import { createEsse3Api, type Esse3Api } from '@mybicocca/data-api/esse3';
import {
	Esse3Token,
	withEsse3Auth,
	type Esse3Credentials,
	type Esse3Jwt
} from '#lib/api/auth/esse3Token.ts';
import { PORTAL_URL, esse3WebLogin } from '#lib/api/auth/esse3WebLogin.ts';
import { MOODLE_URL, type MoodleTokens } from '#lib/api/auth/moodleLogin.ts';
import { deviceClients, type DeviceClients } from '#lib/api/clients.ts';
import { CookieJar, type Cookie } from '#lib/api/cookies.ts';
import { LoginError, ReauthRequiredError, type AuthService } from '#lib/api/errors.ts';
import type { Transport } from '#lib/api/transport.ts';
import { getLocale } from '#lib/paraglide/runtime.js';
import { learningIdentity, type Account, type LearningIdentity } from './account.ts';
import { accountStore, type JarName } from './accountStore.ts';
import { DataFailure } from './errors.ts';
import { singleFlight, withLock } from './locks.ts';

/**
 * The SDK clients of one account, signed in and renewed as needed. Pass an account's own
 * `AccountApi` to its queries; never reuse one account's clients for another.
 */
export interface AccountApi extends DeviceClients {
	/** Esse3 REST: every call carries the rolling JWT, re-minted on expiry and on a 401. */
	readonly esse3: Esse3Api;
	/**
	 * Runs Moodle web service calls with the account's token and Moodle user, logging in first
	 * when the account has none yet; an `invalidtoken` logs in again (stored password) and runs
	 * `call` once more.
	 */
	moodle<T>(
		call: (moodle: MoodleApi, auth: MoodleTokenAuth, user: LearningIdentity) => Promise<T>
	): Promise<T>;
	/**
	 * Runs Moodle page scrapes with the account's `MoodleSession`. `call` returns null when the
	 * page shows the session expired (the login page, or no entry id): the session is renewed
	 * (autologin, else a full login) and `call` runs once more; a second null is returned as is.
	 */
	moodleWeb<T>(call: (moodleWeb: MoodleWebApi) => Promise<T | null>): Promise<T | null>;
	/**
	 * Runs Esse3 portal calls (certificates) with the portal's SSO session, logging in on first
	 * use. `call` returns null when the session expired (`sessionExpired`, a PDF that isn't one):
	 * one new login and one more run; a second null throws an `auth/session_expired` DataFailure.
	 */
	esse3Web<T>(call: (portal: Esse3Api) => Promise<T | null>): Promise<T>;
}

export interface AccountSessionOptions {
	transport: Transport;
	/** The account as stored now; it changes with career picks and refreshes. */
	account: () => Account;
	/** The password when "remember" is off: known to this tab only. */
	password?: string;
	/** Told when only the user can restore access to `service`. */
	onReauthRequired: (service: AuthService) => void;
	/** Told the Moodle identity a login found, for an account signed in without one. */
	onLearning: (learning: LearningIdentity) => void;
}

/** The {@link AccountApi} of one account. */
export class AccountSession implements AccountApi {
	readonly esse3: Esse3Api;
	readonly kaltura: DeviceClients['kaltura'];
	readonly easyStaff: DeviceClients['easyStaff'];
	readonly planning: DeviceClients['planning'];
	readonly affluences: DeviceClients['affluences'];
	readonly moodleWebPublic: DeviceClients['moodleWebPublic'];

	readonly #options: AccountSessionOptions;
	readonly #id: string;
	readonly #jars: Record<JarName, CookieJar>;
	readonly #moodle = new Map<string, MoodleApi>();
	readonly #moodleWeb: MoodleWebApi;
	readonly #portal: Esse3Api;
	/** Set once another session replaced this one, or the account signed out. */
	#retired = false;

	private constructor(
		options: AccountSessionOptions,
		cookies: Record<JarName, Cookie[]>,
		esse3Jwt: Esse3Jwt | undefined
	) {
		this.#options = options;
		const id = options.account().id;
		this.#id = id;
		const persisted = (name: JarName): CookieJar => {
			const jar: CookieJar = new CookieJar(cookies[name], () => {
				if (!this.#retired) void accountStore.saveJar(id, name, jar.cookies());
			});
			return jar;
		};
		const jars = { esse3Web: persisted('esse3Web'), moodleWeb: persisted('moodleWeb') };
		this.#jars = jars;
		const { transport } = options;
		const token = new Esse3Token(
			{
				send: transport.fetch({ jar: new CookieJar(), timeoutMs: 60_000 }),
				credentials: () => this.#credentials(),
				store: {
					read: async () => (await accountStore.secrets(id)).esse3Jwt,
					write: async (jwt) => {
						if (this.#retired) return;
						await accountStore.updateSecrets(id, (current) => ({ ...current, esse3Jwt: jwt }));
					}
				},
				lock: (run) => withLock(`auth:${id}:esse3`, run),
				onReauthRequired: () => this.#reauthRequired('esse3')
			},
			esse3Jwt
		);
		this.esse3 = createEsse3Api({
			fetch: withEsse3Auth(transport.fetch({ jar: new CookieJar(), timeoutMs: 60_000 }), token)
		});
		this.#moodleWeb = createMoodleWebApi({
			fetch: transport.fetch({ jar: jars.moodleWeb, redirect: 'follow', timeoutMs: 30_000 })
		});
		this.#portal = createEsse3Api({
			fetch: transport.fetch({ jar: jars.esse3Web, redirect: 'follow', timeoutMs: 60_000 })
		});
		const device = deviceClients(transport);
		this.kaltura = device.kaltura;
		this.easyStaff = device.easyStaff;
		this.planning = device.planning;
		this.affluences = device.affluences;
		this.moodleWebPublic = device.moodleWebPublic;
	}

	/** Opens the account's session from what the vault keeps. */
	static async open(options: AccountSessionOptions): Promise<AccountSession> {
		const id = options.account().id;
		const [secrets, esse3Web, moodleWeb] = await Promise.all([
			accountStore.secrets(id),
			accountStore.jar(id, 'esse3Web'),
			accountStore.jar(id, 'moodleWeb')
		]);
		return new AccountSession(options, { esse3Web, moodleWeb }, secrets.esse3Jwt);
	}

	/**
	 * Stops keeping what this session's requests still bring back (renewed tokens, cookies), once
	 * a new session replaced it or the account signed out: they would overwrite the new session's,
	 * or bring a wiped account back into the vault.
	 */
	retire(): void {
		this.#retired = true;
	}

	async moodle<T>(
		call: (moodle: MoodleApi, auth: MoodleTokenAuth, user: LearningIdentity) => Promise<T>
	): Promise<T> {
		const api = this.#moodleApi();
		const attempt = async (wsToken: string) => {
			const auth = new MoodleTokenAuth(wsToken);
			return call(api, auth, await this.#learning(api, auth));
		};
		const secrets = await accountStore.secrets(this.#id);
		const wsToken = secrets.wsToken ?? (await this.#moodleLogin(undefined)).wsToken;
		try {
			return await attempt(wsToken);
		} catch (error) {
			if (!(error instanceof MoodleError) || error.code !== MoodleError.InvalidToken) throw error;
			return attempt((await this.#moodleLogin(wsToken)).wsToken);
		}
	}

	/** The account's Moodle user, read from the site info when no login has stored it yet. */
	async #learning(api: MoodleApi, auth: MoodleTokenAuth): Promise<LearningIdentity> {
		const known = this.#options.account().learning;
		if (known !== null) return known;
		const learning = learningIdentity(await api.site.getSiteInfo(auth));
		this.#options.onLearning(learning);
		return learning;
	}

	async moodleWeb<T>(call: (moodleWeb: MoodleWebApi) => Promise<T | null>): Promise<T | null> {
		const session = this.#jars.moodleWeb.get('MoodleSession', new URL(MOODLE_URL));
		if (session !== undefined) {
			const first = await call(this.#moodleWeb);
			if (first !== null) return first;
		}
		await this.#renewMoodleSession(session);
		return call(this.#moodleWeb);
	}

	async esse3Web<T>(call: (portal: Esse3Api) => Promise<T | null>): Promise<T> {
		const host = new URL(PORTAL_URL).hostname;
		if (this.#jars.esse3Web.forHost(host).length === 0) await this.#portalLogin(undefined);
		const first = await call(this.#portal);
		if (first !== null) return first;
		await this.#portalLogin(fingerprint(this.#jars.esse3Web, host));
		const second = await call(this.#portal);
		if (second === null) {
			throw new DataFailure({ kind: 'auth', service: 'esse3Web', reason: 'session_expired' });
		}
		return second;
	}

	#moodleApi(): MoodleApi {
		// Moodle formats strings and picks multilang blocks in the client's language.
		const lang = getLocale() === 'it' ? 'it' : 'en';
		let api = this.#moodle.get(lang);
		if (api === undefined) {
			api = createMoodleApi({ fetch: this.#options.transport.fetch({ timeoutMs: 30_000 }) }, lang);
			this.#moodle.set(lang, api);
		}
		return api;
	}

	/** A full Moodle login with the stored password, unless another caller already replaced `stale`. */
	#moodleLogin(stale: string | undefined): Promise<MoodleTokens> {
		return singleFlight(`auth:${this.#id}:moodle`, async () => {
			const secrets = await accountStore.secrets(this.#id);
			if (secrets.wsToken !== undefined && secrets.wsToken !== stale) {
				return { wsToken: secrets.wsToken, privateToken: secrets.privateToken ?? null };
			}
			const credentials = await this.#require('moodle');
			const { tokens, cookies } = await this.#login('moodle', () =>
				this.#options.transport.moodleSignIn(credentials.username, credentials.password)
			);
			if (this.#retired) return tokens;
			await accountStore.updateSecrets(this.#id, (current) => ({
				...current,
				wsToken: tokens.wsToken,
				privateToken: tokens.privateToken ?? current.privateToken ?? null
			}));
			adoptCookies(this.#jars.moodleWeb, new CookieJar(cookies), new URL(MOODLE_URL).hostname);
			return tokens;
		});
	}

	/**
	 * A new `MoodleSession`: the relay's autologin from the tokens first (no password, no IdP), then
	 * a full login. Skipped when another tab replaced `stale` meanwhile.
	 */
	#renewMoodleSession(stale: string | undefined): Promise<void> {
		return singleFlight(`auth:${this.#id}:moodleWeb`, async () => {
			const moodle = new URL(MOODLE_URL);
			const stored = await accountStore.jar(this.#id, 'moodleWeb');
			const current = new CookieJar(stored).get('MoodleSession', moodle);
			if (current !== undefined && current !== stale) {
				adoptCookies(this.#jars.moodleWeb, new CookieJar(stored), moodle.hostname);
				return;
			}
			const secrets = await accountStore.secrets(this.#id);
			const userid = this.#options.account().learning?.userId;
			if (secrets.wsToken !== undefined && secrets.privateToken && userid !== undefined) {
				const session = await this.#options.transport.autologin({
					wstoken: secrets.wsToken,
					privatetoken: secrets.privateToken,
					userid
				});
				if (session !== undefined) {
					this.#jars.moodleWeb.set({
						name: 'MoodleSession',
						value: session,
						domain: moodle.hostname,
						hostOnly: true,
						path: '/',
						secure: true
					});
					return;
				}
			}
			await this.#moodleLogin(secrets.wsToken);
		});
	}

	/** A new Esse3 portal session, unless another caller already replaced the `stale` cookies. */
	#portalLogin(stale: string | undefined): Promise<void> {
		return singleFlight(`auth:${this.#id}:esse3Web`, async () => {
			const host = new URL(PORTAL_URL).hostname;
			const stored = new CookieJar(await accountStore.jar(this.#id, 'esse3Web'));
			const print = fingerprint(stored, host);
			if (print !== '' && print !== stale) {
				adoptCookies(this.#jars.esse3Web, stored, host);
				return;
			}
			const credentials = await this.#require('esse3Web');
			const jar = new CookieJar();
			const send = this.#options.transport.fetch({ jar, timeoutMs: 60_000 });
			await this.#login('esse3Web', () =>
				esse3WebLogin(send, credentials.username, credentials.password)
			);
			adoptCookies(this.#jars.esse3Web, jar, host);
		});
	}

	/** Runs a login; refused credentials mean only the user can restore access. */
	async #login<T>(service: AuthService, run: () => Promise<T>): Promise<T> {
		try {
			return await run();
		} catch (error) {
			if (error instanceof LoginError && error.badCredentials) this.#reauthRequired(service);
			throw error;
		}
	}

	/** A request still in flight when this session was replaced has no say about the new one. */
	#reauthRequired(service: AuthService): void {
		if (!this.#retired) this.#options.onReauthRequired(service);
	}

	async #credentials(): Promise<Esse3Credentials | undefined> {
		const password = this.#options.password ?? (await accountStore.secrets(this.#id)).password;
		return password === undefined
			? undefined
			: { username: this.#options.account().username, password };
	}

	async #require(service: AuthService): Promise<Esse3Credentials> {
		const credentials = await this.#credentials();
		if (credentials !== undefined) return credentials;
		this.#reauthRequired(service);
		throw new ReauthRequiredError(service, 'No stored password to sign in again');
	}
}

/** Replaces `target`'s cookies for `host` with those `source` holds for it. */
function adoptCookies(target: CookieJar, source: CookieJar, host: string): void {
	for (const cookie of target.forHost(host)) target.set({ ...cookie, expires: 0 });
	for (const cookie of source.forHost(host)) target.set(cookie);
}

/** Identifies the session `jar` holds for `host`, to tell whether another tab replaced it. */
function fingerprint(jar: CookieJar, host: string): string {
	return jar
		.forHost(host)
		.map((cookie) => `${cookie.name}=${cookie.value}`)
		.sort()
		.join('; ');
}
