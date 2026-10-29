import { foldQuery } from '#lib/features/elearning/search/fold.ts';
import type { Href } from '#lib/ui/types.ts';

/**
 * A row the student opened, with the query that found it ('' when picked before typing). Rows
 * that navigate keep enough to be listed again after a reload.
 */
export interface HistoryEntry {
	query: string;
	id: string;
	title: string;
	subtitle?: string;
	href?: Href;
	/** Epoch milliseconds. */
	at: number;
}

/** As on Android: twenty entries per account. */
export const MAX_HISTORY = 20;

/** Ids of rows history never keeps: a day named in the query, the palette's own commands. */
export const UNRECORDED = 'search:';

function slot(entry: Pick<HistoryEntry, 'query' | 'id'>): string {
	const query = foldQuery(entry.query);
	return query === '' ? `id:${entry.id}` : `q:${query}`;
}

/**
 * Adds a pick, newest first: one entry per query (its latest pick, as Android keeps it), and one
 * per row picked before typing.
 */
export function recordPick(entries: readonly HistoryEntry[], entry: HistoryEntry): HistoryEntry[] {
	const key = slot(entry);
	return [entry, ...entries.filter((other) => slot(other) !== key)].slice(0, MAX_HISTORY);
}

/** Whether stored data is a list of entries; anything else is dropped. */
export function isHistory(data: unknown): data is HistoryEntry[] {
	return (
		Array.isArray(data) &&
		data.every(
			(entry: Partial<HistoryEntry> | null) =>
				typeof entry?.query === 'string' &&
				typeof entry.id === 'string' &&
				typeof entry.title === 'string' &&
				typeof entry.at === 'number'
		)
	);
}

/** Boosts from what the student searched and opened before (Android's `GlobalSearchUseCase`). */
export interface SearchMemory {
	/** What a result titled `title` gains: +0.05 when that title was once searched for. */
	recency(title: string): number;
	/** +0.4 for the row opened for this very query, +0.25 for one opened for a longer query it starts. */
	pick(id: string): number;
}

export const RECENCY_BOOST = 0.05;
export const PICK_BOOST_EXACT = 0.4;
export const PICK_BOOST_PREFIX = 0.25;

export function searchMemory(entries: readonly HistoryEntry[], query: string): SearchMemory {
	const norm = foldQuery(query);
	const searched = new Set(entries.map((entry) => foldQuery(entry.query)).filter(Boolean));
	const picks = new Map<string, number>();
	if (norm !== '') {
		for (const entry of entries) {
			const stored = foldQuery(entry.query);
			const boost =
				stored === norm ? PICK_BOOST_EXACT : stored.startsWith(norm) ? PICK_BOOST_PREFIX : 0;
			if (boost > (picks.get(entry.id) ?? 0)) picks.set(entry.id, boost);
		}
	}
	return {
		recency: (title) => (searched.has(foldQuery(title)) ? RECENCY_BOOST : 0),
		pick: (id) => picks.get(id) ?? 0
	};
}
