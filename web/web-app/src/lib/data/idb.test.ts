import { afterEach, describe, expect, it, vi } from 'vitest';

/** A fresh copy of the module, so each test opens its own database. */
const load = async () => {
	vi.resetModules();
	return import('./idb.ts');
};

/** An `indexedDB` whose open request ends the way `settle` says, or never. */
function failingIndexedDb(settle?: (request: { onerror: (() => void) | null }) => void) {
	return {
		open: () => {
			const request = { error: new DOMException('Internal error', 'UnknownError'), onerror: null };
			if (settle) setTimeout(() => settle(request), 0);
			return request;
		}
	};
}

async function roundTrip(idb: Awaited<ReturnType<typeof load>>) {
	await idb.idbPut('vault', 'acct/a/account', 1);
	await idb.idbPut('vault', 'acct/a/secrets', 2);
	await idb.idbPut('vault', 'acct/b/account', 3);
	expect(await idb.idbGet('vault', 'acct/a/secrets')).toBe(2);
	expect(await idb.idbEntries('vault', 'acct/')).toEqual([
		['acct/a/account', 1],
		['acct/a/secrets', 2],
		['acct/b/account', 3]
	]);
	await idb.idbDeletePrefix('vault', 'acct/a/');
	await idb.idbDelete('vault', 'acct/b/account');
	expect(await idb.idbEntries('vault', 'acct/')).toEqual([]);
	expect(await idb.idbGet('cache', 'acct/a/secrets')).toBeUndefined();
}

describe('idb', () => {
	afterEach(() => {
		vi.unstubAllGlobals();
		vi.useRealTimers();
	});

	it('keeps the stores in memory where there is no IndexedDB', async () => {
		vi.stubGlobal('indexedDB', undefined);
		await roundTrip(await load());
	});

	it('falls back to memory when the database fails to open', async () => {
		vi.stubGlobal(
			'indexedDB',
			failingIndexedDb((request) => request.onerror?.())
		);
		await roundTrip(await load());
	});

	it('falls back to memory when the database never opens', async () => {
		vi.useFakeTimers();
		vi.stubGlobal('indexedDB', failingIndexedDb());
		const idb = await load();
		const read = idb.idbGet('meta', 'activeAccountId');
		await vi.advanceTimersByTimeAsync(10_000);
		expect(await read).toBeUndefined();
		vi.useRealTimers();
		await roundTrip(idb);
	});
});
