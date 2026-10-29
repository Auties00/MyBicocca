import { describe, expect, it } from 'vitest';
import { MAX_HISTORY, isHistory, recordPick, searchMemory, type HistoryEntry } from './history.ts';

const entry = (query: string, id: string, at = 0): HistoryEntry => ({ query, id, title: id, at });

describe('recordPick', () => {
	it('keeps one pick per query, the latest, first', () => {
		const entries = recordPick(
			[entry('basi', 'course:1'), entry('mappa', 'page:map')],
			entry('Basì', 'course:2')
		);
		expect(entries.map((e) => [e.query, e.id])).toEqual([
			['Basì', 'course:2'],
			['mappa', 'page:map']
		]);
	});

	it('keeps rows picked before typing once each', () => {
		const entries = recordPick(
			[entry('', 'page:map'), entry('', 'page:calendar')],
			entry('', 'page:map', 1)
		);
		expect(entries.map((e) => e.id)).toEqual(['page:map', 'page:calendar']);
	});

	it('remembers twenty entries', () => {
		let entries: HistoryEntry[] = [];
		for (let i = 0; i < 30; i++) entries = recordPick(entries, entry(`q${i}`, `id:${i}`));
		expect(entries).toHaveLength(MAX_HISTORY);
		expect(entries[0].query).toBe('q29');
	});
});

describe('isHistory', () => {
	it('accepts entries and refuses anything else', () => {
		expect(isHistory([entry('a', 'b')])).toBe(true);
		expect(isHistory([{ query: 'a' }])).toBe(false);
		expect(isHistory({})).toBe(false);
	});
});

describe('searchMemory', () => {
	const history = [
		entry('basi di', 'course:1'),
		entry('Mappa', 'page:map'),
		entry('basi', 'transcript:9')
	];

	it('boosts the row picked for the same query most, then for a longer one', () => {
		const memory = searchMemory(history, 'BASI');
		expect(memory.pick('transcript:9')).toBe(0.4);
		expect(memory.pick('course:1')).toBe(0.25);
		expect(memory.pick('page:map')).toBe(0);
	});

	it('nudges titles once searched for', () => {
		const memory = searchMemory(history, 'x');
		expect(memory.recency('mappa')).toBe(0.05);
		expect(memory.recency('Calendario')).toBe(0);
	});
});
