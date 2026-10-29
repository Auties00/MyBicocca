import type {
	MoodleWebCatalog,
	MoodleWebCatalogCategory,
	MoodleWebCatalogCourse
} from '@mybicocca/data-api/elearning';
import { describe, expect, it } from 'vitest';
import { buildCatalogIndex, catalogLevel, catalogRoot, nodeKey } from './index.ts';
import type { CatalogSearchRow } from './model.ts';
import { MAX_SEARCH_ROWS, searchRows } from './rows.ts';
import { HIT_BUDGET, searchCatalog } from './search.ts';

const SITE = 'https://elearning.unimib.it';
let nextCourse = 1;
let nextCategory = 100;

function category(
	name: string,
	categories: MoodleWebCatalogCategory[] = [],
	courses: MoodleWebCatalogCourse[] = [],
	url = `course/index.php?categoryid=${nextCategory++}`
): MoodleWebCatalogCategory {
	return { name, url: `${SITE}/${url}`, categories, courses };
}

function course(name: string, code = `CDS-X${nextCourse}-DOC`): MoodleWebCatalogCourse {
	const id = nextCourse++;
	return { id, name, code, url: `${SITE}/course/view.php?id=${id}` };
}

const algorithms = course('Algoritmi e Strutture Dati', '2627-2-E3101Q106');
const databases = course('Basi di Dati', '2627-2-E3101Q107');
const oldDatabases = course('Basi di Dati', '2526-2-E3101Q107');
const olderDatabases = course('Basi di Dati', '2425-2-E3101Q107');
const maths = course('Matematica Generale', '2627-1-E1807M001');
// The programme changed code: last year's edition carries another one.
const oldMaths = course('Matematica Generale', '2526-1-E1802M034');
const statistics = course('Statistica', '2627-1-E1805M010');
const marketing = course(
	'CCD di CdL Marketing, Comunicazione Aziendale e Mercati Globali - Laboratori Tematici Multimediali Integrati'
);

const catalog: MoodleWebCatalog = {
	fetchedAt: '2026-10-26T00:59:54Z',
	areas: [
		{
			name: 'Didattica online',
			categories: [
				category(
					'Area Economico-Statistica',
					[
						category('Economia [E1805M]', [
							category('Insegnamenti', [
								category('A.A. 2026-2027', [category('1° anno', [], [maths, statistics])]),
								category('A.A. 2025-2026', [category('1° anno', [], [oldMaths])])
							])
						]),
						category('Marketing', [], [marketing, course('Igene ambientale')]),
						category('Tutorato')
					],
					[],
					'area-economicostatistica'
				),
				category(
					'Area di Scienze',
					[
						category(
							'Informatica [E3101Q]',
							[
								category('Insegnamenti', [
									category(
										'A.A. 2026-2027',
										[
											category(
												'2° anno',
												[],
												[algorithms, databases],
												'course/index.php?categoryid=11'
											)
										],
										[],
										'course/index.php?categoryid=14'
									),
									category('A.A. 2025-2026', [category('2° anno', [], [oldDatabases])]),
									category('A.A. 2024-2025', [category('2° anno', [], [olderDatabases])])
								])
							],
							[],
							'course/index.php?categoryid=10'
						),
						category('Strutture dati avanzate', [], [course('Grafi')])
					],
					[],
					'area-scienze'
				)
			]
		},
		{
			name: 'Post-Laurea',
			categories: [
				category('Scuole di Specializzazione', [], [course('Igiene')], 'scuole-specializzazione'),
				// An empty cross-listing of a category that lives elsewhere.
				category('Informatica [E3101Q]', [], [], 'course/index.php?categoryid=10')
			]
		}
	]
};

const index = buildCatalogIndex(catalog);

/** Each row as `<depth>|<trail › >name`, courses with a bullet. */
function drawn(rows: readonly CatalogSearchRow[]): string[] {
	return rows.map((row) => {
		const name =
			row.kind === 'course'
				? `· ${row.course.name}`
				: row.kind === 'more'
					? `+${row.count} in ${row.name}`
					: row.name;
		return `${row.rails.length}|${[...row.trail, name].join(' › ')}`;
	});
}

const search = (query: string, scope: string | null = null, foldFrom?: number) =>
	searchRows(searchCatalog(index, query, scope), foldFrom).rows;

describe('catalog index', () => {
	it('keys categories by Moodle id, areas by their page', () => {
		expect(nodeKey(`${SITE}/course/index.php?categoryid=12510`)).toBe('12510');
		expect(nodeKey(`${SITE}/area-giuridica`)).toBe('area-giuridica');
		expect(nodeKey(`${SITE}/bicoccaacademy/`)).toBe('bicoccaacademy');
		expect(nodeKey(null)).toBeNull();
	});

	it('lists the areas with their course totals', () => {
		const root = catalogRoot(index);
		expect(root.sections.map((s) => s.areas.map((a) => [a.key, a.courses]))).toEqual([
			[
				['area-economicostatistica', 5],
				['area-scienze', 5]
			],
			[
				['scuole-specializzazione', 1],
				['10', 4]
			]
		]);
	});

	it('opens a level with its breadcrumb and siblings', () => {
		const level = catalogLevel(index, '11');
		expect(level?.ancestors.map((a) => a.name)).toEqual([
			'Area di Scienze',
			'Informatica [E3101Q]',
			'Insegnamenti',
			'A.A. 2026-2027'
		]);
		expect(level?.area.key).toBe('area-scienze');
		expect(level?.courses.map((c) => c.id)).toEqual([algorithms.id, databases.id]);
		expect(catalogLevel(index, '999')).toBeUndefined();
	});

	it('follows an empty cross-listing to the category that holds its contents', () => {
		const level = catalogLevel(index, '10');
		expect(level?.ancestors.map((a) => a.name)).toEqual(['Area di Scienze']);
		expect(level?.categories.map((c) => c.name)).toEqual(['Insegnamenti']);
	});

	it('dates courses by their code, else by their academic-year category', () => {
		const year = (id: number) => index.courses.find((c) => c.id === id)?.year;
		expect(year(oldDatabases.id)).toBe(2025);
		expect(year(marketing.id)).toBeNull();
	});
});

describe('searchCatalog', () => {
	it('folds a chain of waypoints into one row, keeping misses out', () => {
		expect(drawn(search('algoritmi'))).toEqual([
			'0|Area di Scienze',
			'1|Informatica [E3101Q] › Insegnamenti › A.A. 2026-2027 › 2° anno',
			'2|· Algoritmi e Strutture Dati'
		]);
		const [area, waypoint, hit] = search('algoritmi');
		expect(area).toMatchObject({ kind: 'category', match: false, area: 'area-scienze' });
		expect(waypoint).toMatchObject({ kind: 'category', match: false, key: '11' });
		expect(hit).toMatchObject({ nameRanges: [[0, 9]], area: 'area-scienze' });
	});

	it('shows the editions of a teaching as one hit, the newest standing for the others', () => {
		const rows = search('basi di dati');
		expect(drawn(rows)).toEqual([
			'0|Area di Scienze',
			'1|Informatica [E3101Q] › Insegnamenti › A.A. 2026-2027 › 2° anno',
			'2|· Basi di Dati'
		]);
		const hit = rows[2];
		expect(hit.kind === 'course' && hit.course.id).toBe(databases.id);
		expect(hit.kind === 'course' && hit.editions.map((e) => [e.course.id, e.year])).toEqual([
			[oldDatabases.id, 2025],
			[olderDatabases.id, 2024]
		]);
		expect(searchCatalog(index, 'basi di dati', null).total).toBe(1);
	});

	it('joins editions by name within a programme when the code changed', () => {
		const rows = search('matematica');
		const hit = rows.find((row) => row.kind === 'course');
		expect(hit?.kind === 'course' && hit.course.id).toBe(maths.id);
		expect(hit?.kind === 'course' && hit.editions.map((e) => e.course.id)).toEqual([oldMaths.id]);
	});

	it('matches codes only as written', () => {
		expect(drawn(search('E3101Q10')).filter((row) => row.includes('·'))).toEqual([
			'2|· Algoritmi e Strutture Dati',
			'2|· Basi di Dati'
		]);
		// One digit off is another course, not a typo.
		expect(drawn(search('E3101Q107')).filter((row) => row.includes('·'))).toEqual([
			'2|· Basi di Dati'
		]);
		const [, , hit] = search('E3101Q107');
		expect(hit).toMatchObject({ codeRanges: [[7, 16]], nameRanges: [] });
	});

	it('drops loose matches when close ones exist', () => {
		// "Igene ambientale" is an abbreviation of "igiene": shown only when nothing matches better.
		expect(drawn(search('igiene'))).toEqual(['0|Scuole di Specializzazione', '1|· Igiene']);
		expect(drawn(search('igiene', 'area-economicostatistica'))).toEqual([
			'0|Marketing',
			'1|· Igene ambientale'
		]);
	});

	it('shows letters scattered across a title only when nothing else matches', () => {
		const names = drawn(search('algortmi'));
		expect(names).toContain('2|· Algoritmi e Strutture Dati');
		expect(names.some((row) => row.includes('Marketing'))).toBe(false);
		expect(drawn(search('algortmi', 'area-economicostatistica'))).toEqual([
			'0|Marketing',
			'1|· CCD di CdL Marketing, Comunicazione Aziendale e Mercati Globali - Laboratori Tematici Multimediali Integrati'
		]);
	});

	it('puts a matching category before courses matching as well', () => {
		// Both branches hold an exact hit: the one whose category matches leads.
		const tree = searchCatalog(index, 'strutture dati', null).tree;
		expect(tree[0]).toMatchObject({ kind: 'category', name: 'Area di Scienze' });
		const area = tree[0].kind === 'category' ? tree[0].children : [];
		expect(area.map((node) => (node.kind === 'category' ? node.name : node.kind))).toEqual([
			'Strutture dati avanzate',
			'Informatica [E3101Q]'
		]);
	});

	it('ranks a match at the title start first, then at a word start, a category before a course', () => {
		const ranked = buildCatalogIndex({
			fetchedAt: '',
			areas: [
				{
					name: 'Didattica online',
					categories: [
						category(
							'Area',
							[
								category('Bioinformatica'),
								category('Corsi', [], [course('Laboratorio di Informatica')]),
								category('Corsi avanzati', [], [course('Informatica Generale')]),
								category('Informatica e Società')
							],
							[],
							'area'
						)
					]
				}
			]
		});
		const top = searchCatalog(ranked, 'informatica', 'area').tree.map((node) =>
			node.kind === 'category' ? node.name : node.kind
		);
		expect(top).toEqual(['Informatica e Società', 'Corsi avanzati', 'Corsi', 'Bioinformatica']);
	});

	it('searches only below the level it is scoped to', () => {
		const result = searchCatalog(index, 'dati', '10');
		expect(result.scope).toBe('10');
		expect(result.area).toBe('area-scienze');
		expect(drawn(searchRows(result).rows)).toEqual([
			'0|Insegnamenti',
			'1|A.A. 2026-2027 › 2° anno',
			'2|· Algoritmi e Strutture Dati',
			'2|· Basi di Dati'
		]);
		expect(searchCatalog(index, 'igiene', '10').tree).toEqual([]);
	});

	it('answers an unknown level as unknown, without searching elsewhere', () => {
		expect(searchCatalog(index, 'dati', 'gone')).toMatchObject({
			scope: 'gone',
			unknownScope: true,
			tree: [],
			total: 0
		});
		expect(searchCatalog(index, 'dati', '10').unknownScope).toBe(false);
	});

	it('keeps a row its id while a longer query keeps the row', () => {
		const idOf = (rows: CatalogSearchRow[], name: string) =>
			rows.find((row) => row.kind === 'course' && row.course.name === name)?.id;
		const short = search('dati');
		expect(idOf(short, 'Basi di Dati')).toBeDefined();
		expect(idOf(search('basi di dati'), 'Basi di Dati')).toBe(idOf(short, 'Basi di Dati'));
		const broad = search('a');
		expect(new Set(broad.map((row) => row.id)).size).toBe(broad.length);
	});

	it('folds every waypoint from the depth asked, for narrow screens', () => {
		expect(drawn(search('algoritmi', null, 1))).toEqual([
			'0|Area di Scienze',
			'1|Informatica [E3101Q] › Insegnamenti › A.A. 2026-2027 › 2° anno › · Algoritmi e Strutture Dati'
		]);
	});

	it('finds nothing for an empty query', () => {
		expect(searchCatalog(index, '   ', null)).toMatchObject({ tree: [], total: 0 });
	});

	it('shares the hits between the areas, each overflow searching its area alone', () => {
		const many = (area: string, count: number) =>
			category(
				area,
				[
					category(
						`${area} courses`,
						[],
						Array.from({ length: count }, (_, i) => course(`Fisica ${area} ${i}`))
					)
				],
				[],
				area.toLowerCase()
			);
		const wide = buildCatalogIndex({
			fetchedAt: '',
			areas: [{ name: 'All', categories: [many('One', 1), many('Two', 30), many('Three', 30)] }]
		});
		const result = searchCatalog(wide, 'fisica', null);
		expect(result.total).toBe(61);
		expect(result.branches).toEqual([
			{ key: 'one', name: 'One', count: 1 },
			{ key: 'two', name: 'Two', count: 30 },
			{ key: 'three', name: 'Three', count: 30 }
		]);
		const { rows, hits } = searchRows(result);
		expect(hits).toBe(HIT_BUDGET);
		const shown = (area: string) =>
			rows.filter((row) => row.kind === 'course' && row.area === area).length;
		expect([shown('one'), shown('two'), shown('three')]).toEqual([1, 19, 20]);
		expect(shown('one') + shown('two') + shown('three')).toBe(HIT_BUDGET);
		expect(rows.filter((row) => row.kind === 'more')).toMatchObject([
			{ key: 'two', name: 'Two', count: 11, rails: [true], last: true },
			{ key: 'three', name: 'Three', count: 10, rails: [false], last: true }
		]);
		expect(result.capped).toBe(false);
	});

	it('marks a level too broad to show whole', () => {
		const flat = buildCatalogIndex({
			fetchedAt: '',
			areas: [
				{
					name: 'Big',
					categories: [
						category(
							'Area',
							[],
							Array.from({ length: HIT_BUDGET + 5 }, (_, i) => course(`Chimica ${i}`)),
							'area'
						)
					]
				}
			]
		});
		const scoped = searchCatalog(flat, 'chimica', 'area');
		expect(scoped.tree).toHaveLength(HIT_BUDGET);
		expect(scoped.capped).toBe(true);
	});

	it('stops drawing at the row cap', () => {
		const deep = buildCatalogIndex({
			fetchedAt: '',
			areas: [
				{
					name: 'Deep',
					categories: Array.from({ length: 60 }, (_, a) =>
						category(
							`Area ${a}`,
							[category(`Corso ${a}`, [category(`Anno ${a}`, [], [course(`Ottica ${a}`)])])],
							[course(`Ottica bis ${a}`)],
							`area-${a}`
						)
					)
				}
			]
		});
		const { rows, capped } = searchRows(searchCatalog(deep, 'ottica', null));
		expect(rows).toHaveLength(MAX_SEARCH_ROWS);
		expect(capped).toBe(true);
	});
});
