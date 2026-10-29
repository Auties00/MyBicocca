import { liveTransport, type Transport } from '#lib/api/transport.ts';
import {
	defaultCareerId,
	displayNameOf,
	needsCareerPick,
	normalizeUsername,
	reconcileCareers,
	toCareers,
	type Account,
	type AccountEvent,
	type Career,
	type LearningIdentity
} from './account.ts';
import type { AccountApi, AccountSession } from './accountSession.ts';
import { accountStore, readMeta, writeMeta } from './accountStore.ts';
import { accountPrefix } from './keys.ts';
import { cache } from './query.svelte.ts';
import type { SignInFailure } from './signIn.ts';

export type { AccountApi } from './accountSession.ts';

/** The signed-in context a page works in: the active account, its selected career, its clients. */
export interface SessionScope {
	account: Account;
	career: Career;
	api: AccountApi;
}

export type SignInResult =
	| {
			ok: true;
			account: Account;
			needsCareerPick: boolean;
			/** The Moodle half failed: e-learning signs in when first opened. */
			moodleFailed: boolean;
	  }
	| { ok: false; reason: SignInFailure };

const CAREERS_REFRESH_MS = 24 * 60 * 60_000;

/** The demo's fixtures, which only the demo build ships. */
const demoTransport = (): Promise<typeof import('#lib/demo/transport.ts')> =>
	__DEMO__
		? import('#lib/demo/transport.ts')
		: Promise.reject(new Error('The demo is part of the demo build only'));

/**
 * The accounts signed in on this browser, the active one, and their clients. Everything here is
 * reactive: read `session.current` in a component and it follows sign-ins, switches and picks.
 *
 * ```ts
 * import { session } from '#lib/data/session.svelte.ts';
 * const scope = session.current; // { account, career, api } once signed in and a career is picked
 * await session.signIn(username, password, remember);
 * await session.selectCareer(account.id, careerId);
 * ```
 */
class Session {
	/** Most recently used first. */
	accounts = $state.raw<Account[]>([]);
	activeId = $state.raw<string | null>(null);
	/** Accounts whose careers wait for a pick. */
	pendingPicks = $state.raw<string[]>([]);
	/** Events for the shell to show, oldest first, until dismissed. */
	events = $state.raw<AccountEvent[]>([]);
	/** Whether the stored accounts have been read; until then nobody is signed in or out. */
	loaded = $state(false);

	#apis = $state.raw<Readonly<Record<string, AccountSession>>>({});
	readonly #opening = new Map<string, Promise<AccountSession>>();
	#loading: Promise<void> | undefined;
	/** Passwords not to remember, known to this tab only. */
	#passwords: Record<string, string> = {};
	#channel: BroadcastChannel | undefined;

	readonly active: Account | undefined = $derived(
		this.accounts.find((account) => account.id === this.activeId)
	);

	readonly career: Career | undefined = $derived(
		this.active?.careers.find((career) => career.id === this.active?.selectedCareerId)
	);

	/** Whether the active account must pick a career before the app opens. */
	readonly needsCareerPick: boolean = $derived(
		this.active !== undefined && this.pendingPicks.includes(this.active.id)
	);

	readonly current: SessionScope | undefined = $derived.by(() => {
		const account = this.active;
		const career = this.career;
		const api = account === undefined ? undefined : this.#apis[account.id];
		return account !== undefined &&
			career !== undefined &&
			api !== undefined &&
			!this.needsCareerPick
			? { account, career, api }
			: undefined;
	});

	/** Reads the stored accounts once; every other method awaits it. */
	load(): Promise<void> {
		this.#loading ??= this.#load();
		return this.#loading;
	}

	/**
	 * Signs into Esse3 and Moodle with the university credentials, as a new account or again as a
	 * stored one (keeping its id and career). The password is stored, encrypted, only with
	 * `remember`; otherwise this tab keeps it until it closes.
	 */
	async signIn(username: string, password: string, remember: boolean): Promise<SignInResult> {
		await this.load();
		if (username.trim() === '' || password === '') return { ok: false, reason: 'bad_credentials' };
		return this.#signIn(liveTransport, normalizeUsername(username), password, remember, true);
	}

	/**
	 * Signs a stored account in again, as when its credentials stopped working, without changing
	 * which account is active. Its clients are swapped in one step: `current` never goes undefined
	 * and pages keep what they show. The demo build ignores `password`.
	 */
	async reauthenticate(
		accountId: string,
		password: string,
		remember: boolean
	): Promise<SignInResult> {
		await this.load();
		const account = this.accounts.find((item) => item.id === accountId);
		if (account === undefined) return { ok: false, reason: 'unknown' };
		if (__DEMO__) return this.#signInDemo(false);
		if (password === '') return { ok: false, reason: 'bad_credentials' };
		return this.#signIn(liveTransport, account.username, password, remember, false);
	}

	/** Whether the account keeps its password on this device, for silent re-logins. */
	async remembers(accountId: string): Promise<boolean> {
		return (await accountStore.secrets(accountId)).password !== undefined;
	}

	/**
	 * Removes the account and everything kept for it: data, tokens, cookies, cached queries. The
	 * demo build has only the demo account, which starts over instead.
	 */
	async signOut(accountId: string): Promise<void> {
		await this.load();
		if (!this.accounts.some((item) => item.id === accountId)) return;
		if (__DEMO__) return this.#restartDemo(accountId);
		this.#setApi(accountId, undefined);
		delete this.#passwords[accountId];
		const remaining = this.accounts.filter((item) => item.id !== accountId);
		const next = this.activeId === accountId ? (remaining[0]?.id ?? null) : this.activeId;
		this.accounts = remaining;
		this.events = this.events.filter((event) => event.accountId !== accountId);
		await this.#setMeta(
			next,
			this.pendingPicks.filter((id) => id !== accountId)
		);
		await Promise.all([cache.remove(accountPrefix(accountId)), accountStore.wipe(accountId)]);
		if (next !== null) await this.#open(next);
		this.#broadcast();
	}

	async switchAccount(accountId: string): Promise<void> {
		await this.load();
		const account = this.accounts.find((item) => item.id === accountId);
		if (account === undefined) throw new Error(`No account ${accountId}`);
		await this.#save({ ...account, lastUsedAt: Date.now() });
		await this.#open(accountId);
		await this.#setMeta(accountId, this.pendingPicks);
		this.#broadcast();
	}

	async selectCareer(accountId: string, careerId: number): Promise<void> {
		await this.load();
		const account = this.accounts.find((item) => item.id === accountId);
		if (account === undefined || !account.careers.some((career) => career.id === careerId)) {
			throw new Error(`Career ${careerId} is not part of account ${accountId}`);
		}
		await this.#save({ ...account, selectedCareerId: careerId, lastUsedAt: Date.now() });
		await this.#setMeta(
			this.activeId,
			this.pendingPicks.filter((id) => id !== accountId)
		);
		this.#broadcast();
	}

	/**
	 * Re-reads the careers from Esse3 and reports what changed as events; a selected career that
	 * vanished is replaced by the default one.
	 */
	async refreshCareers(accountId: string): Promise<void> {
		const api = await this.apiFor(accountId);
		const [session, careers, segments] = await Promise.all([
			api.esse3.auth.login(),
			api.esse3.careers.getCareers(),
			api.esse3.transcript.getCareerSegments({ optionalFields: 'tipoCorsoCod' }).catch(() => [])
		]);
		const account = this.accounts.find((item) => item.id === accountId);
		const refreshed = toCareers(careers, segments);
		if (account === undefined || refreshed.length === 0) return;
		const events = reconcileCareers(
			accountId,
			account.careers,
			refreshed,
			account.selectedCareerId
		);
		const missing = events.some((event) => event.type === 'selected_career_missing');
		await this.#save({
			...account,
			displayName: displayNameOf(session.user),
			careers: refreshed,
			selectedCareerId: missing ? defaultCareerId(refreshed) : account.selectedCareerId,
			lastSyncedAt: Date.now()
		});
		for (const event of events) this.#emit(event);
		this.#broadcast();
	}

	/** The clients of any signed-in account; `current.api` is the active one's. */
	async apiFor(accountId: string): Promise<AccountApi> {
		await this.load();
		return this.#apis[accountId] ?? (await this.#openApi(accountId));
	}

	dismissEvent(event: AccountEvent): void {
		this.events = this.events.filter((item) => item !== event);
	}

	async #load(): Promise<void> {
		try {
			const [accounts, meta] = await Promise.all([accountStore.accounts(), readMeta()]);
			this.accounts = byLastUse(accounts);
			this.pendingPicks = meta.careerPickPending;
			const active = accounts.some((account) => account.id === meta.activeAccountId)
				? meta.activeAccountId
				: null;
			if (active !== null) {
				await this.#openApi(active);
				this.activeId = active;
				this.#refreshCareersDaily(active);
			} else if (__DEMO__) {
				await this.#signInDemo(true);
			}
		} catch {
			// Storage that can't be read leaves the app signed out rather than blank; signing in works.
		} finally {
			this.loaded = true;
		}
		if (typeof BroadcastChannel !== 'undefined') {
			this.#channel = new BroadcastChannel('mybicocca-session');
			this.#channel.onmessage = () => void this.#reload();
		}
	}

	/** Another tab changed the accounts: read them again. */
	async #reload(): Promise<void> {
		const [accounts, meta] = await Promise.all([accountStore.accounts(), readMeta()]);
		const ids = new Set(accounts.map((account) => account.id));
		for (const id of Object.keys(this.#apis)) if (!ids.has(id)) this.#setApi(id, undefined);
		this.accounts = byLastUse(accounts);
		this.pendingPicks = meta.careerPickPending;
		this.activeId =
			meta.activeAccountId !== null && ids.has(meta.activeAccountId) ? meta.activeAccountId : null;
		if (this.activeId !== null) await this.apiFor(this.activeId);
	}

	/**
	 * Signs in and keeps what it yields. A sign-in that `activates` makes the account the active
	 * one; signing in again only renews its clients, which replace the old ones in one step.
	 */
	async #signIn(
		transport: Transport,
		username: string,
		password: string,
		remember: boolean,
		activates: boolean
	): Promise<SignInResult> {
		const { authenticate } = await import('./signIn.ts');
		const outcome = await authenticate(transport, username, password);
		if (!outcome.ok) return outcome;
		const { user, careers, moodle: learnt } = outcome.credentials;
		const existing = this.accounts.find((account) => account.recordUserId === user.userId);
		const id = existing?.id ?? crypto.randomUUID();
		const now = Date.now();
		const kept =
			existing !== undefined && careers.some((career) => career.id === existing.selectedCareerId);
		const account: Account = {
			id,
			username,
			displayName: displayNameOf(user),
			recordUserId: user.userId,
			personId: user.personId ?? 0,
			fiscalCode: user.fiscalCode,
			careers,
			selectedCareerId: kept ? existing.selectedCareerId : defaultCareerId(careers),
			learning: learnt?.learning ?? existing?.learning ?? null,
			createdAt: existing?.createdAt ?? now,
			lastUsedAt: now,
			lastSyncedAt: now
		};

		// A failed Moodle half keeps the Moodle tokens of an earlier sign-in, if any.
		await accountStore.updateSecrets(id, (current) => ({
			...(remember ? { password } : {}),
			esse3Jwt: outcome.credentials.esse3Jwt,
			wsToken: learnt?.tokens.wsToken ?? current.wsToken,
			privateToken: learnt === undefined ? current.privateToken : learnt.tokens.privateToken
		}));
		if (learnt !== undefined) await accountStore.saveJar(id, 'moodleWeb', learnt.cookies);
		await accountStore.saveJar(id, 'esse3Web', []);
		if (remember) delete this.#passwords[id];
		else this.#passwords[id] = password;
		await this.#save(account);

		const pick = existing === undefined && needsCareerPick(careers);
		this.#setApi(id, await this.#createApi(account, transport));
		if (activates) {
			const pending = this.pendingPicks.filter((item) => item !== id);
			await this.#setMeta(id, pick ? [...pending, id] : pending);
		}
		this.events = this.events.filter(
			(event) => !(event.accountId === id && event.type === 'reauth_required')
		);
		this.#broadcast();
		return { ok: true, account, needsCareerPick: pick, moodleFailed: learnt === undefined };
	}

	/** Signs into the demo account, whose every request the demo fixtures answer. */
	async #signInDemo(activates: boolean): Promise<SignInResult> {
		const demo = await demoTransport();
		return this.#signIn(
			demo.demoTransport,
			demo.DEMO_USERNAME,
			demo.DEMO_PASSWORD,
			true,
			activates
		);
	}

	/** Forgets everything the demo changed; the reload signs into it afresh, as a first visit. */
	async #restartDemo(accountId: string): Promise<void> {
		await Promise.all([
			cache.remove(['acct']),
			cache.remove(['public']),
			cache.remove(['device']),
			accountStore.wipe(accountId)
		]);
		(await demoTransport()).resetDemo();
		location.reload();
	}

	/** Makes `accountId` the active account, with its clients open. */
	async #open(accountId: string): Promise<void> {
		await this.#openApi(accountId);
		this.activeId = accountId;
	}

	/** The account's clients, opened once however many callers ask for them at the same time. */
	#openApi(accountId: string): Promise<AccountSession> {
		const opened = this.#apis[accountId];
		if (opened !== undefined) return Promise.resolve(opened);
		let opening = this.#opening.get(accountId);
		if (opening === undefined) {
			opening = (async () => {
				const account = this.accounts.find((item) => item.id === accountId);
				if (account === undefined) throw new Error(`No account ${accountId}`);
				// The SDK (see #createApi) downloads alongside the demo's transport, not after it.
				import('./accountSession.ts').catch(() => undefined);
				const transport = __DEMO__ ? (await demoTransport()).demoTransport : liveTransport;
				const api = await this.#createApi(account, transport);
				this.#setApi(accountId, api);
				return api;
			})().finally(() => this.#opening.delete(accountId));
			this.#opening.set(accountId, opening);
		}
		return opening;
	}

	async #createApi(account: Account, transport: Transport): Promise<AccountSession> {
		const accountId = account.id;
		const password = this.#passwords[accountId];
		// The SDK comes with the first account opened, not with the app: sign-in never waits for it.
		const { AccountSession } = await import('./accountSession.ts');
		return AccountSession.open({
			transport,
			account: () => this.accounts.find((item) => item.id === accountId) ?? account,
			...(password === undefined ? {} : { password }),
			onReauthRequired: () => this.#emit({ type: 'reauth_required', accountId }),
			onLearning: (learning) => void this.#learnt(accountId, learning)
		});
	}

	/** Keeps the Moodle identity a later Moodle login found. */
	async #learnt(accountId: string, learning: LearningIdentity): Promise<void> {
		const account = this.accounts.find((item) => item.id === accountId);
		if (account === undefined) return;
		await this.#save({ ...account, learning });
		this.#broadcast();
	}

	#emit(event: AccountEvent): void {
		const repeated =
			event.type === 'reauth_required' &&
			this.events.some((item) => item.type === event.type && item.accountId === event.accountId);
		if (!repeated) this.events = [...this.events, event];
	}

	#refreshCareersDaily(accountId: string): void {
		const account = this.accounts.find((item) => item.id === accountId);
		if (account !== undefined && Date.now() - account.lastSyncedAt > CAREERS_REFRESH_MS) {
			this.refreshCareers(accountId).catch(() => undefined);
		}
	}

	/** Puts `api` in the place of the account's clients, retiring the ones it replaces. */
	#setApi(accountId: string, api: AccountSession | undefined): void {
		const previous = this.#apis[accountId];
		if (previous === api) return;
		previous?.retire();
		const next = Object.fromEntries(Object.entries(this.#apis).filter(([id]) => id !== accountId));
		if (api !== undefined) next[accountId] = api;
		this.#apis = next;
	}

	async #save(account: Account): Promise<void> {
		await accountStore.saveAccount(account);
		this.accounts = byLastUse([...this.accounts.filter((item) => item.id !== account.id), account]);
	}

	async #setMeta(activeId: string | null, pendingPicks: string[]): Promise<void> {
		this.activeId = activeId;
		this.pendingPicks = pendingPicks;
		await writeMeta({ activeAccountId: activeId, careerPickPending: pendingPicks });
	}

	#broadcast(): void {
		this.#channel?.postMessage('changed');
	}
}

function byLastUse(accounts: readonly Account[]): Account[] {
	return [...accounts].sort((a, b) => b.lastUsedAt - a.lastUsedAt);
}

export const session = new Session();
