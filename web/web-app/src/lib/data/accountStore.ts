import type { Esse3Jwt } from '#lib/api/auth/esse3Token.ts';
import type { Cookie } from '#lib/api/cookies.ts';
import type { Account } from './account.ts';
import { idbGet, idbPut } from './idb.ts';
import { vault } from './vault.ts';

/** What an account needs to call the services, kept encrypted in the vault. */
export interface Secrets {
	/** Only with "remember on this device": Esse3 tokens lapse after five minutes unused. */
	password?: string;
	esse3Jwt?: Esse3Jwt;
	wsToken?: string;
	privateToken?: string | null;
}

/** The cookie jars an account keeps across reloads; Esse3's REST jar stays in memory. */
export type JarName = 'esse3Web' | 'moodleWeb';

const prefix = (accountId: string) => `acct/${accountId}/`;
const ACCOUNT = 'account';
const updates = new Map<string, Promise<unknown>>();

/** Accounts, their secrets and cookie jars, all under `acct/{id}/` so one prefix wipes an account. */
export const accountStore = {
	async accounts(): Promise<Account[]> {
		const entries = await vault.entries<Account>('acct/');
		return entries.filter(([key]) => key.endsWith(`/${ACCOUNT}`)).map(([, account]) => account);
	},

	saveAccount(account: Account): Promise<void> {
		return vault.set(`${prefix(account.id)}${ACCOUNT}`, account);
	},

	async secrets(accountId: string): Promise<Secrets> {
		return (await vault.get<Secrets>(`${prefix(accountId)}secrets`)) ?? {};
	},

	/** Applies `update` after the account's earlier updates, so concurrent renewals don't lose writes. */
	updateSecrets(accountId: string, update: (secrets: Secrets) => Secrets): Promise<Secrets> {
		const run = (updates.get(accountId) ?? Promise.resolve()).then(async () => {
			const next = update(await this.secrets(accountId));
			await vault.set(`${prefix(accountId)}secrets`, next);
			return next;
		});
		updates.set(
			accountId,
			run.catch(() => undefined)
		);
		return run;
	},

	async jar(accountId: string, name: JarName): Promise<Cookie[]> {
		return (await vault.get<Cookie[]>(`${prefix(accountId)}jar/${name}`)) ?? [];
	},

	saveJar(accountId: string, name: JarName, cookies: readonly Cookie[]): Promise<void> {
		return vault.set(`${prefix(accountId)}jar/${name}`, cookies);
	},

	/** Forgets everything the vault holds for the account. */
	wipe(accountId: string): Promise<void> {
		return vault.deletePrefix(prefix(accountId));
	}
};

/** Plain values about the signed-in state, readable before the vault key is. */
export interface Meta {
	activeAccountId: string | null;
	/** Accounts whose careers still wait for a pick; survives reloads until the user picks. */
	careerPickPending: string[];
}

export async function readMeta(): Promise<Meta> {
	const [activeAccountId, careerPickPending] = await Promise.all([
		idbGet<string | null>('meta', 'activeAccountId'),
		idbGet<string[]>('meta', 'careerPickPending')
	]);
	return { activeAccountId: activeAccountId ?? null, careerPickPending: careerPickPending ?? [] };
}

export async function writeMeta(meta: Meta): Promise<void> {
	await Promise.all([
		idbPut('meta', 'activeAccountId', meta.activeAccountId),
		idbPut('meta', 'careerPickPending', meta.careerPickPending)
	]);
}
