import IconArrowCounterClockwise from '~icons/ph/arrow-counter-clockwise';
import IconClockCounterClockwise from '~icons/ph/clock-counter-clockwise';
import IconTrash from '~icons/ph/trash';
import { m } from '#lib/paraglide/messages.js';
import type { PaletteItem } from '#lib/shell/palette.svelte.ts';
import { UNRECORDED, type HistoryEntry } from './history.ts';

/** Recent rows shown before typing; history keeps more, for its boosts. */
export const RECENT_ROWS = 5;

/**
 * Ids of what stays the same for good: a page, a course, a place, a record-book row. The others
 * (a result to accept, a call to book, a lesson) leave the index once done or past.
 */
const LASTING = ['page:', 'service:', 'action:', 'course:', 'transcript:', 'building:', 'room:'];

/** What the recent rows are made from: the history, and the index as it is now. */
export interface RecentSource {
	entries: readonly HistoryEntry[];
	/** What the last clear removed, while it can still be restored. */
	cleared: readonly HistoryEntry[] | undefined;
	/** The index's rows by id, once loaded. */
	byId: ReadonlyMap<string, PaletteItem> | undefined;
	/** Everything read and nothing failed: an entry missing from the index is gone. */
	settled: boolean;
	remove(id: string): void;
	clear(): void;
	restore(): void;
}

/**
 * The history rows before typing, each as the index shows it now (a stored copy until it loads),
 * newest first and five at most, then the row that clears them. Things that come and go are left
 * out once a settled index no longer holds them. After a clear, the row that restores them.
 */
export function recentRows(source: RecentSource): PaletteItem[] {
	const group = m.shell_palette_recent();
	if (source.entries.length === 0 && source.cleared) {
		return [
			{
				id: `${UNRECORDED}restore-history`,
				title: m.search_history_restore(),
				subtitle: m.search_history_cleared(),
				icon: IconArrowCounterClockwise,
				group,
				stay: true,
				run: () => source.restore()
			}
		];
	}
	const rows: PaletteItem[] = [];
	for (const entry of source.entries) {
		if (!entry.href || rows.some((row) => row.id === entry.id)) continue;
		const current = source.byId?.get(entry.id);
		const lasting = LASTING.some((prefix) => entry.id.startsWith(prefix));
		if (current === undefined && source.settled && !lasting) continue;
		rows.push({
			...(current ?? {
				id: entry.id,
				title: entry.title,
				subtitle: entry.subtitle,
				icon: IconClockCounterClockwise,
				href: entry.href
			}),
			group,
			remove: () => source.remove(entry.id)
		});
		if (rows.length === RECENT_ROWS) break;
	}
	if (rows.length > 0) {
		rows.push({
			id: `${UNRECORDED}clear-history`,
			title: m.search_history_clear(),
			icon: IconTrash,
			group,
			stay: true,
			run: () => source.clear()
		});
	}
	return rows;
}
