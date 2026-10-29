import { describe, expect, it } from 'vitest';
import { mergeResults, topHit, type PaletteItem } from '#lib/shell/palette.svelte.ts';
import { searchMemory, type HistoryEntry } from './history.ts';
import { prepareQuery } from './match.ts';
import { rank, searchDoc } from './rank.ts';

const row = (id: string, title: string, group = 'Corsi', order = 4): PaletteItem => ({
	id,
	title,
	group,
	order,
	href: '/x' as PaletteItem['href']
});

function search(query: string, docs: ReturnType<typeof searchDoc>[], history: HistoryEntry[] = []) {
	const prepared = prepareQuery(query);
	if (prepared === null) throw new Error('empty query');
	return rank(prepared, docs, searchMemory(history, query));
}

describe('rank', () => {
	const docs = [
		searchDoc(row('course:1', 'Basi di Dati')),
		searchDoc(row('course:2', 'Analisi Matematica')),
		searchDoc(row('room:U6/1', 'Aula 1', 'Luoghi', 8), ['U6 Aula 1']),
		searchDoc(row('transcript:1', 'Basi di Dati', 'Carriera', 9), ['E3101Q118'])
	];

	it('orders by score, then by the groups’ order', () => {
		const found = search('basi di dati', docs);
		expect(found.map((item) => [item.id, item.score])).toEqual([
			['course:1', 1],
			['transcript:1', 1]
		]);
	});

	it('marks the parts of the title the query matched, none for an alias', () => {
		expect(search('dati', docs)[0].highlight).toEqual([[8, 12]]);
		expect(search('E3101', docs)[0].highlight).toBeUndefined();
	});

	it('marks the whole words a typo stands for, not letters scattered across them', () => {
		const rooms = ['Aula 2151 (Sala Lauree)', 'Aula 2088 (Aula Tribunale)'].map((title, i) =>
			searchDoc(row(`room:${i}`, title, 'Luoghi', 8))
		);
		expect(search('aule', rooms).map((item) => item.highlight)).toEqual([[[0, 4]], [[0, 4]]]);
	});

	it('lifts the row picked for the same query over a better match', () => {
		const history = [{ query: 'basi', id: 'transcript:1', title: 'Basi di Dati', at: 0 }];
		const found = search('basi', docs, history);
		expect(found[0]).toMatchObject({ id: 'transcript:1', score: 1 });
		expect(found[1]).toMatchObject({ id: 'course:1', score: 0.85 });
	});

	it('keeps the best eight of a group and thirty in all', () => {
		const many = Array.from({ length: 12 }, (_, i) =>
			searchDoc(row(`room:${i}`, `Aula ${i}`, 'Luoghi'))
		);
		const found = search('aula', many);
		expect(found).toHaveLength(8);
	});

	it('keeps abbreviations that only crowd each other', () => {
		const docs = ['Sistemi Operativi', 'Sistemi Operativi', 'Valuta Sistemi Operativi'].map(
			(title, i) => searchDoc(row(`r${i}`, title))
		);
		expect(search('sisop', docs)).toHaveLength(3);
	});

	it('drops typos from a query that names three rows outright', () => {
		const named = ['Probabilità e Statistica', 'Statistica', 'Statistica medica'].map((title, i) =>
			searchDoc(row(`course:${i}`, title))
		);
		const seat = searchDoc(row('action:seat', 'Book a library seat', 'Azioni', 1));
		expect(search('stat', [seat]).map((item) => item.id)).toEqual(['action:seat']);
		expect(search('stat', [...named, seat]).map((item) => item.id)).not.toContain('action:seat');
	});

	it('drops abbreviations from a query that names three rows outright', () => {
		const rooms = ['Aula 1', 'Aula 2', 'Aula Magna'].map((title, i) =>
			searchDoc(row(`room:${i}`, title, 'Luoghi', 8))
		);
		const abbreviation = searchDoc(row('course:9', 'Auditorium e Laboratori'));
		expect(search('aula', [abbreviation]).map((item) => item.id)).toEqual(['course:9']);
		expect(search('aula', [...rooms, abbreviation]).map((item) => item.id)).not.toContain(
			'course:9'
		);
		const picked = [{ query: 'aula', id: 'course:9', title: 'x', at: 0 }];
		expect(search('aula', [...rooms, abbreviation], picked)[0].id).toBe('course:9');
	});

	it('says which alias found a row, unless its group and subtitle show it already', () => {
		const calendar = searchDoc({ ...row('page:calendar', 'Calendario', 'Pagine', 2) }, [
			'orario',
			'lezioni'
		]);
		const quiz = searchDoc(
			{ ...row('quiz:1', 'Quiz 3', 'Quiz', 6), subtitle: 'Analisi 1 · Scade gio 5 nov' },
			['Analisi 1', 'quiz Analisi 1']
		);
		expect(search('orario', [calendar])[0].subtitle).toBe('Corrisponde a “orario”');
		expect(search('analisi', [quiz])[0].subtitle).toBe('Analisi 1 · Scade gio 5 nov');
		expect(search('quiz analisi', [quiz])[0].subtitle).toBe('Analisi 1 · Scade gio 5 nov');
		const room = searchDoc(
			{ ...row('room:1', 'Aula T014', 'Luoghi', 8), subtitle: 'U14 – ABACUS' },
			['U14-T014']
		);
		expect(search('u14', [room])[0].subtitle).toBe('U14 – ABACUS');
	});

	it('takes rows scored elsewhere, boosted like the others', () => {
		const prepared = prepareQuery('domani');
		if (prepared === null) throw new Error('empty query');
		const day = { ...row('day:2026-10-29', 'Domani', 'Calendario', 7), score: 0.95 };
		const memory = searchMemory(
			[{ query: 'domani', id: 'day:2026-10-29', title: 'Domani', at: 0 }],
			'domani'
		);
		expect(rank(prepared, [], memory, [day])[0]).toMatchObject({ score: 1, highlight: [[0, 6]] });
	});
});

describe('mergeResults', () => {
	it('merges a row two providers return, keeping the best score and the other’s details', () => {
		const shell = { ...row('page:map', 'Mappa', 'Pagine'), order: undefined, score: 0.9 };
		const search = {
			...row('page:map', 'Mappa', 'Pagine', 2),
			score: 0.85,
			highlight: [[0, 3]] as const
		};
		const [merged] = mergeResults([[shell], [search]]);
		expect(merged).toMatchObject({ score: 0.9, order: 2, highlight: [[0, 3]] });
	});

	it('lists the fallbacks last, whatever their score, outside the caps', () => {
		const many = Array.from({ length: 40 }, (_, i) => ({
			...row(`r${i}`, 'x', `G${i % 5}`),
			score: 0.5
		}));
		const fallback = { ...row('catalog:x', 'Cerca', 'Altrove', 11), fallback: true, score: 1 };
		const merged = mergeResults([[fallback, ...many]]);
		expect(merged).toHaveLength(31);
		expect(merged.at(-1)?.id).toBe('catalog:x');
	});

	it('leaves out a fallback once any provider’s row names the query', () => {
		const fallback = { ...row('catalog:x', 'Cerca', 'Altrove', 11), fallback: true };
		const catalog = { ...fallback, unlessScored: 0.75 };
		const fuzzy = { ...row('r1', 'x'), score: 0.6 };
		const named = { ...row('r2', 'x', 'Azioni'), score: 0.85 };
		expect(mergeResults([[fuzzy, catalog]]).at(-1)?.id).toBe('catalog:x');
		expect(mergeResults([[fuzzy, catalog], [named]]).map((item) => item.id)).toEqual(['r2', 'r1']);
		expect(mergeResults([[fallback], [named]]).at(-1)?.id).toBe('catalog:x');
	});

	it('caps every group at eight and the whole list at thirty', () => {
		const groups = ['A', 'B', 'C', 'D', 'E'].map((group) =>
			Array.from({ length: 10 }, (_, i) => ({ ...row(`${group}${i}`, 'x', group), score: 0.5 }))
		);
		const merged = mergeResults(groups);
		expect(merged).toHaveLength(30);
		expect(merged.filter((item) => item.group === 'A')).toHaveLength(8);
	});
});

describe('topHit', () => {
	const scored = (...scores: number[]) =>
		scores.map((score, i) => ({ ...row(`r${i}`, 'x'), score }));

	it('is a strong row clearly ahead of the next, or alone', () => {
		expect(topHit(scored(0.85, 0.75))?.id).toBe('r0');
		expect(topHit(scored(0.8))?.id).toBe('r0');
	});

	it('passes over the same name in a later group', () => {
		const course = { ...row('course:1', 'Basi di Dati', 'Corsi', 4), score: 1 };
		const career = { ...row('transcript:1', 'Basi di dati', 'Carriera', 9), score: 1 };
		expect(topHit([course, career])?.id).toBe('course:1');
		expect(topHit([career, course])).toBeUndefined();
	});

	it('is nobody when the best is weak or tied', () => {
		expect(topHit(scored(0.75))).toBeUndefined();
		expect(topHit(scored(1, 0.97))).toBeUndefined();
		expect(topHit([])).toBeUndefined();
	});
});
