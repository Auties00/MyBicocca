import { deserialize, serialize } from './codec.ts';
import { idbDeletePrefix, idbGet, idbPut } from './idb.ts';
import type { QueryPersistence } from './queryCache.ts';
import { seal, unseal, type Sealed } from './vault.ts';

type Stored = { updatedAt: number; plain: string } | { updatedAt: number; sealed: Sealed };

/** Keeps persisted queries in IndexedDB, personal ones encrypted with the vault key. */
export const idbPersistence: QueryPersistence = {
	async read(key, encrypted) {
		const stored = await idbGet<Stored>('cache', key);
		if (stored === undefined) return undefined;
		if ('sealed' in stored)
			return { updatedAt: stored.updatedAt, data: await unseal(stored.sealed) };
		// A record written in the clear is never served to a query that wants encryption.
		if (encrypted) return undefined;
		return { updatedAt: stored.updatedAt, data: deserialize(stored.plain) };
	},

	async write(key, entry, encrypted) {
		const stored: Stored = encrypted
			? { updatedAt: entry.updatedAt, sealed: await seal(entry.data) }
			: { updatedAt: entry.updatedAt, plain: serialize(entry.data) };
		await idbPut('cache', key, stored);
	},

	remove(prefix) {
		return idbDeletePrefix('cache', prefix);
	}
};
