import { describe, expect, it } from 'vitest';
import type { PaletteItem } from '#lib/shell/palette.svelte.ts';
import type { HistoryEntry } from './history.ts';
import { RECENT_ROWS, recentRows, type RecentSource } from './recent.ts';

const entry = (id: string, title = id): HistoryEntry => ({
	query: '',
	id,
	title,
	href: '/x' as HistoryEntry['href'],
	at: 0
});

function rows(source: Partial<RecentSource>) {
	return recentRows({
		entries: [],
		cleared: undefined,
		byId: undefined,
		settled: false,
		remove: () => undefined,
		clear: () => undefined,
		restore: () => undefined,
		...source
	});
}

const ids = (items: PaletteItem[]) => items.map((item) => item.id);

describe('recentRows', () => {
	it('lists the newest five, each once, then the row that clears them', () => {
		const entries = ['page:a', 'page:b', 'page:a', 'page:c', 'page:d', 'page:e', 'page:f'];
		const found = rows({ entries: entries.map((id) => entry(id)) });
		expect(ids(found)).toEqual([
			'page:a',
			'page:b',
			'page:c',
			'page:d',
			'page:e',
			'search:clear-history'
		]);
		expect(found.filter((item) => item.remove)).toHaveLength(RECENT_ROWS);
	});

	it('shows a row as the index has it now, its stored copy until then', () => {
		const stored = entry('course:1', 'Basi di dati (2024)');
		expect(rows({ entries: [stored] })[0].title).toBe('Basi di dati (2024)');
		const now = { id: 'course:1', title: 'Basi di Dati', group: 'Corsi' };
		const byId = new Map([[now.id, now]]);
		expect(rows({ entries: [stored], byId })[0]).toMatchObject({
			title: 'Basi di Dati',
			group: 'Recenti'
		});
	});

	it('drops what came and went once the index is settled, keeping what lasts', () => {
		const entries = [entry('booking:1'), entry('course:1'), entry('room:U6/1')];
		expect(ids(rows({ entries, byId: new Map() }))).toContain('booking:1');
		expect(ids(rows({ entries, byId: new Map(), settled: true }))).toEqual([
			'course:1',
			'room:U6/1',
			'search:clear-history'
		]);
	});

	it('leaves out picks with nowhere to go', () => {
		const action = { ...entry('action:theme'), href: undefined };
		expect(rows({ entries: [action] })).toEqual([]);
	});

	it('offers the restore after a clear, and runs it in place', () => {
		let restored = false;
		const [restore] = rows({
			cleared: [entry('page:a')],
			restore: () => (restored = true)
		});
		expect(restore).toMatchObject({ id: 'search:restore-history', stay: true });
		restore.run?.();
		expect(restored).toBe(true);
	});
});
