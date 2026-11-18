import { accountKey } from '#lib/data/keys.ts';
import { keyString } from '#lib/data/queryCache.ts';
import { idbPersistence } from '#lib/data/queryStore.ts';
import type { PaletteItem } from '#lib/shell/palette.svelte.ts';
import { UNRECORDED, isHistory, recordPick, type HistoryEntry } from './history.ts';

/**
 * What an account searched and opened. Kept encrypted under the account's prefix, so signing out
 * wipes it with the account's other data.
 */
export class SearchHistory {
	entries = $state.raw<HistoryEntry[]>([]);
	/** What the last clear removed, until it is restored or the palette closes. */
	cleared = $state.raw<HistoryEntry[]>();
	readonly #key: string;
	#ready: Promise<void> | undefined;

	constructor(accountId: string) {
		this.#key = keyString(accountKey({ id: accountId }, 'search.history'));
	}

	/** Reads the stored history, once; what is recorded before it arrives comes first. */
	load(): Promise<void> {
		this.#ready ??= idbPersistence
			.read(this.#key, true)
			.then((stored) => {
				if (stored === undefined || !isHistory(stored.data)) return;
				let entries = stored.data;
				for (const entry of this.entries.toReversed()) entries = recordPick(entries, entry);
				this.entries = entries;
			})
			.catch(() => undefined);
		return this.#ready;
	}

	record(item: PaletteItem, query: string): void {
		if (item.id.startsWith(UNRECORDED)) return;
		const entry: HistoryEntry = { query, id: item.id, title: item.title, at: Date.now() };
		if (item.subtitle) entry.subtitle = item.subtitle;
		if (item.href) entry.href = item.href;
		this.cleared = undefined;
		this.#save(recordPick(this.entries, entry));
	}

	/** Forgets every pick of one row. */
	async remove(id: string): Promise<void> {
		await this.load();
		this.#save(this.entries.filter((entry) => entry.id !== id));
	}

	async clear(): Promise<void> {
		await this.load();
		this.cleared = this.entries;
		this.#save([]);
	}

	/** Brings back what the last clear removed. */
	restore(): void {
		if (this.cleared === undefined) return;
		this.#save(this.cleared);
		this.cleared = undefined;
	}

	#save(entries: HistoryEntry[]): void {
		this.entries = entries;
		void this.load().then(() =>
			idbPersistence
				.write(this.#key, { data: this.entries, updatedAt: Date.now() }, true)
				.catch(() => undefined)
		);
	}
}

/**
 * One per account signed in. A plain record, not reactive: a lookup from a `$derived` that adds
 * an account must not count as a state change.
 */
const byAccount: Record<string, SearchHistory> = {};

export function historyOf(accountId: string): SearchHistory {
	byAccount[accountId] ??= new SearchHistory(accountId);
	return byAccount[accountId];
}

/** Drops the histories of accounts no longer signed in: their stored copy went with them. */
export function keepHistories(accountIds: readonly string[]): void {
	for (const id of Object.keys(byAccount)) if (!accountIds.includes(id)) delete byAccount[id];
}
