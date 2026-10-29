import { decodeEsse3StudyPlan, decodeEsse3StudyPlanHeader } from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import { groupByYear, sentenceCase, toStudyPlan } from './plan.ts';

function wire<T>(decode: (json: unknown) => T | undefined, json: unknown): T {
	const value = decode(json);
	if (value === undefined) throw new Error('not an object');
	return value;
}

const header = wire(decodeEsse3StudyPlanHeader, {
	stuId: 1,
	pianoId: 2,
	tipoPiano: 'S',
	stato: 'A',
	dataUltimaVarStato: '14/10/2025',
	regsceId: 3,
	schemaId: 4
});

describe('toStudyPlan', () => {
	it('keeps only the chosen activities of the choice tree, with their fallbacks', () => {
		const plan = toStudyPlan(
			header,
			wire(decodeEsse3StudyPlan, {
				attivita: [
					{ adsceId: 10, sceltaFlg: 1, adLibCod: 'A1', adLibDes: 'ALGEBRA', peso: 8, annoCorso: 1 },
					{ adsceId: 11, sceltaFlg: 0, adLibCod: 'A2', adLibDes: 'NOT CHOSEN', peso: 6 },
					{
						sceltaAdId: 12,
						chiaveADContestualizzata: { adCod: 'A3', adDes: 'FISICA' },
						peso: 6,
						annoCorso: 2
					},
					{ adsceId: 13, sceltaFlg: 1, peso: 6 }
				]
			})
		);
		expect(plan).toMatchObject({ planId: 2, studentId: 1, type: 'standard', state: 'A' });
		expect(plan.lastChange?.toString()).toBe('2025-10-14');
		expect(plan.courses).toEqual([
			{ id: 10, name: 'ALGEBRA', code: 'A1', credits: 8, year: 1 },
			{ id: 12, name: 'FISICA', code: 'A3', credits: 6, year: 2 }
		]);
	});
});

describe('groupByYear', () => {
	const course = (id: number, year: number, credits: number) => ({
		id,
		name: `C${id}`,
		code: null,
		credits,
		year
	});
	const courses = [course(1, 2, 6), course(2, 0, 3), course(3, 1, 8), course(4, 2, 7.5)];

	it('orders the years with the year-less activities last and sums their credits', () => {
		const years = groupByYear(courses);
		expect(years.map((year) => [year.year, year.courses.length, year.credits])).toEqual([
			[1, 1, 8],
			[2, 2, 13.5],
			[0, 1, 3]
		]);
		expect(years.every((year) => year.earned === 0)).toBe(true);
	});

	it('counts the credits of the passed activities per year', () => {
		const years = groupByYear(courses, (item) => item.id !== 1);
		expect(years.map((year) => year.earned)).toEqual([8, 7.5, 3]);
	});
});

describe('sentenceCase', () => {
	it('turns capitals into a sentence and leaves mixed case alone', () => {
		expect(sentenceCase('PERCORSO COMUNE')).toBe('Percorso comune');
		expect(sentenceCase('Data Science')).toBe('Data Science');
	});
});
