import { decodeEsse3PersonTitles } from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import { groupTitles, latestTitle, titleDetails, titleHeadline, toTitles } from './degrees.ts';

/** `/titoli/{persId}?optionalFields=ALL` with the given lists. */
function titles(lists: { SUP?: unknown[]; TITIT?: unknown[]; TITSTRA?: unknown[] }) {
	const value = decodeEsse3PersonTitles({ persId: '1', SUP: [], TITIT: [], TITSTRA: [], ...lists });
	if (value === undefined) throw new Error('Undecodable titles');
	return toTitles(value);
}

const diploma = {
	id: 7,
	annoMaturita: 2025,
	dataMaturita: '10/07/2025 00:00:00',
	anniScolarita: 5,
	scuolaDes: 'Liceo Scientifico Statale',
	scuolaComuDes: 'Milano',
	tipiTitoloSupDes: 'Liceo scientifico',
	staTitItCod: 'C',
	voto: 92,
	votoMax: 100,
	dichiarazValoreFlg: 0
};

const degree = {
	titItId: 31,
	tipiTitItDes: 'Laurea',
	desCds: 'INFORMATICA',
	p06AteneiDes: 'Università degli Studi di Milano-Bicocca',
	p06AaDes: '2027/2028',
	staTitItCod: 'I',
	voto: 110,
	baseVoto: 110,
	lode: 1,
	mediaVoti: 28.4
};

describe('toTitles', () => {
	it('lists diplomas, then Italian, then foreign titles, with ids after Esse3’s', () => {
		const list = titles({
			TITSTRA: [{ titStraId: 5, cdsStraniero: 'Computer Science' }],
			TITIT: [degree],
			SUP: [diploma]
		});
		expect(list.map((title) => title.id)).toEqual(['sup-7', 'titit-31', 'titstra-5']);
		expect(list.map((title) => title.category)).toEqual(['high_school', 'italian', 'foreign']);
	});

	it('reads the grade on its scale, and none for a zero grade', () => {
		const [school, university] = titles({ SUP: [diploma], TITIT: [degree] });
		expect(school.grade).toEqual({ value: 92, base: 100 });
		expect(university).toMatchObject({ grade: { value: 110, base: 110 }, cumLaude: true });
		expect(titles({ SUP: [{ ...diploma, voto: 0 }] })[0].grade).toBeNull();
		expect(titles({ SUP: [{ ...diploma, votoMax: 0 }] })[0].grade).toEqual({
			value: 92,
			base: null
		});
	});

	it('reads the status codes', () => {
		const [school, university] = titles({ SUP: [diploma], TITIT: [degree] });
		expect(school.status).toBe('awarded');
		expect(university.status).toBe('hypothesised');
	});

	it('keeps the fields Esse3 filled, typed, in order', () => {
		const [school] = titles({ SUP: [diploma] });
		expect(school.attributes).toEqual([
			{ field: 'graduation_year', value: { kind: 'text', text: '2025' } },
			{
				field: 'award_date',
				value: { kind: 'date', date: expect.objectContaining({ year: 2025, month: 7, day: 10 }) }
			},
			{ field: 'duration_years', value: { kind: 'years', count: 5 } },
			{ field: 'institution', value: { kind: 'text', text: 'Liceo Scientifico Statale' } },
			{ field: 'city', value: { kind: 'text', text: 'Milano' } },
			{ field: 'value_declaration', value: { kind: 'flag', set: false } }
		]);
	});

	it('keeps a date it can’t read as written', () => {
		const [school] = titles({ SUP: [{ ...diploma, dataMaturita: 'luglio 2025' }] });
		expect(school.attributes[1]).toEqual({
			field: 'award_date',
			value: { kind: 'text', text: 'luglio 2025' }
		});
	});
});

describe('titleHeadline and titleDetails', () => {
	it('names a title by its subject, else its kind, and never repeats the headline', () => {
		const [school, university] = titles({ SUP: [diploma], TITIT: [degree] });
		expect(titleHeadline(school)).toBe('Liceo scientifico');
		expect(titleDetails(school)).toEqual(['Liceo Scientifico Statale', '2025']);
		expect(titleHeadline(university)).toBe('INFORMATICA');
		expect(titleDetails(university)).toEqual([
			'Laurea',
			'Università degli Studi di Milano-Bicocca',
			'2027/2028'
		]);
	});

	it('shows the country of foreign titles only', () => {
		const [foreign] = titles({
			TITSTRA: [{ titStraId: 1, cdsStraniero: 'Physics', p01NaziDes: 'Spagna', desAteneo: 'UAB' }]
		});
		expect(titleDetails(foreign)).toEqual(['UAB', 'Spagna']);
	});
});

describe('groupTitles', () => {
	it('groups by category in life order, leaving out empty ones', () => {
		const groups = groupTitles(titles({ TITIT: [degree], SUP: [diploma] }));
		expect(groups.map((group) => group.category)).toEqual(['high_school', 'italian']);
	});
});

describe('latestTitle', () => {
	it('picks the title earned last, by award date, else by the end of its year', () => {
		const exchange = { titStraId: 2, cdsStraniero: 'Physics', aaConsegId: 2026 };
		const list = titles({
			SUP: [diploma],
			TITIT: [{ ...degree, dataConsegTitolo: '20/03/2028' }],
			TITSTRA: [exchange]
		});
		expect(latestTitle(list)?.id).toBe('titit-31');
		const undated = titles({ SUP: [diploma], TITIT: [{ ...degree, p06AaDes: null }] });
		expect(latestTitle(undated)?.id).toBe('sup-7');
	});
});
